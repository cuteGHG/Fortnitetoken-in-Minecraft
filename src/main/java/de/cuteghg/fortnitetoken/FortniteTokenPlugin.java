package de.cuteghg.fortnitetoken;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.loot.Lootable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class FortniteTokenPlugin extends JavaPlugin implements Listener {

    private NamespacedKey tokenKey;
    private NamespacedKey naturalContainerKey;

    @Override
    public void onEnable() {
        tokenKey = new NamespacedKey(this, "fortnite_token");
        naturalContainerKey = new NamespacedKey(this, "natural_loot_container");
        Bukkit.getPluginManager().registerEvents(this, this);
        // Bereits geladene Chunks ebenfalls erfassen (z. B. nach Installation bei laufendem Server).
        for (var world : Bukkit.getWorlds()) {
            for (var chunk : world.getLoadedChunks()) {
                markNaturalLootContainers(chunk);
            }
        }
        getLogger().info("FortniteToken aktiviert. Chance: 1:1000 bei natürlich generierten Loot-Containern.");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        markNaturalLootContainers(event.getChunk());
    }

    private void markNaturalLootContainers(org.bukkit.Chunk chunk) {
        // Markiert Loot-Container beim Laden des Chunks. Die Markierung bleibt
        // auch nach dem ersten Öffnen erhalten, wenn Minecraft die Loot-Tabelle entfernt.
        for (BlockState state : chunk.getTileEntities()) {
            if (!(state instanceof TileState tile) || !(state instanceof Lootable lootable)) {
                continue;
            }
            if (lootable.getLootTable() == null) continue;
            if (tile.getPersistentDataContainer().has(naturalContainerKey, PersistentDataType.BYTE)) continue;
            tile.getPersistentDataContainer().set(naturalContainerKey, PersistentDataType.BYTE, (byte) 1);
            tile.update(true, false);
        }
    }

    @EventHandler
    public void onContainerOpen(InventoryOpenEvent event) {
        if (event.isCancelled() || !(event.getPlayer() instanceof Player player)) {
            return;
        }

        Location location = event.getInventory().getLocation();
        if (location == null) return;

        Block block = location.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof TileState tile)) return;

        // Nur Container, die beim Chunk-Laden als Vanilla-Loot-Container
        // erkannt wurden. Keine Prüfung auf eine bestimmte Dungeon-Loot-Tabelle:
        // damit funktionieren auch andere natürlich generierte Truhen.
        if (!tile.getPersistentDataContainer().has(naturalContainerKey, PersistentDataType.BYTE)) {
            return;
        }

        if (ThreadLocalRandom.current().nextInt(1000) != 0) return;
        giveTokenAndCelebrate(player, block.getLocation().add(0.5, 1.0, 0.5));
    }

    private void giveTokenAndCelebrate(Player player, Location animationCenter) {
        var leftovers = player.getInventory().addItem(createToken());
        leftovers.values().forEach(item ->
                player.getWorld().dropItemNaturally(player.getLocation(), item));
        playWinAnimation(player, animationCenter);
    }

    private ItemStack createToken() {
        ItemStack token = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = token.getItemMeta();
        meta.displayName(Component.text("Token", NamedTextColor.GOLD));
        meta.lore(List.of(
                Component.text("Ein seltener Fortnite-Token", NamedTextColor.GRAY),
                Component.text("Gefunden in einer natürlich generierten Truhe", NamedTextColor.DARK_GRAY)
        ));
        meta.getPersistentDataContainer().set(tokenKey, PersistentDataType.BYTE, (byte) 1);
        token.setItemMeta(meta);
        return token;
    }

    private void playWinAnimation(Player player, Location center) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("✦ ", NamedTextColor.GOLD)
                .append(Component.text("ULTRA RARER TOKEN GEFUNDEN!", NamedTextColor.YELLOW))
                .append(Component.text(" ✦", NamedTextColor.GOLD)));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);

        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (tick >= 40 || !player.isOnline()) {
                    cancel();
                    return;
                }
                double angle = tick * Math.PI / 8.0;
                double radius = 0.8 + tick / 40.0;
                Location particleLocation = center.clone().add(
                        Math.cos(angle) * radius, tick * 0.045, Math.sin(angle) * radius);
                center.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 3, 0.08, 0.08, 0.08, 0.01);
                center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 2, 0.35, 0.5, 0.35, 0.02);
                if (tick == 12 || tick == 26) {
                    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
                }
                tick++;
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender,
                             org.bukkit.command.Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("fttoken")) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern benutzt werden.");
            return true;
        }
        if (!player.hasPermission("fortnitetoken.test")) {
            player.sendMessage(Component.text("Dafür hast du keine Berechtigung.", NamedTextColor.RED));
            return true;
        }
        if (args.length != 1 || !args[0].equalsIgnoreCase("test")) {
            player.sendMessage(Component.text("Verwendung: /fttoken test", NamedTextColor.YELLOW));
            return true;
        }
        giveTokenAndCelebrate(player, player.getLocation().add(0, 1, 0));
        player.sendMessage(Component.text("Test erfolgreich: Token und Animation ausgelöst.", NamedTextColor.GREEN));
        return true;
    }
}