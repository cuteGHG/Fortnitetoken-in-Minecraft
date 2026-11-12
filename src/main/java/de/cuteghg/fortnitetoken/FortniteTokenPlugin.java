package de.cuteghg.fortnitetoken;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private NamespacedKey tokenKey;
    private NamespacedKey naturalContainerKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        tokenKey = new NamespacedKey(this, "fortnite_token");
        naturalContainerKey = new NamespacedKey(this, "natural_loot_container");
        Bukkit.getPluginManager().registerEvents(this, this);
        for (var world : Bukkit.getWorlds()) {
            for (var chunk : world.getLoadedChunks()) {
                markNaturalLootContainers(chunk);
            }
        }
        getLogger().info("FortniteToken aktiviert. Drop-Chance: 1:" + getDropDenominator() + ".");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        markNaturalLootContainers(event.getChunk());
    }

    private void markNaturalLootContainers(org.bukkit.Chunk chunk) {
        for (BlockState state : chunk.getTileEntities()) {
            if (!(state instanceof TileState tile) || !(state instanceof Lootable lootable)) continue;
            if (lootable.getLootTable() == null) continue;
            if (tile.getPersistentDataContainer().has(naturalContainerKey, PersistentDataType.BYTE)) continue;
            tile.getPersistentDataContainer().set(naturalContainerKey, PersistentDataType.BYTE, (byte) 1);
            tile.update(true, false);
        }
    }

    @EventHandler
    public void onContainerOpen(InventoryOpenEvent event) {
        if (event.isCancelled() || !(event.getPlayer() instanceof Player player)) return;
        Location location = event.getInventory().getLocation();
        if (location == null) return;

        Block block = location.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof TileState tile)) return;
        if (!tile.getPersistentDataContainer().has(naturalContainerKey, PersistentDataType.BYTE)) return;
        if (ThreadLocalRandom.current().nextInt(getDropDenominator()) != 0) return;

        giveTokenAndCelebrate(player, block.getLocation().add(0.5, 1.0, 0.5));
    }

    private int getDropDenominator() {
        return Math.max(1, getConfig().getInt("drop-chance.denominator", 1000));
    }

    private void giveTokenAndCelebrate(Player player, Location animationCenter) {
        var leftovers = player.getInventory().addItem(createToken());
        leftovers.values().forEach(item ->
                player.getWorld().dropItemNaturally(player.getLocation(), item));
        playWinAnimation(player, animationCenter);
    }

    private ItemStack createToken() {
        String materialName = getConfig().getString("token.material", "SUNFLOWER");
        Material material = Material.matchMaterial(materialName == null ? "SUNFLOWER" : materialName);
        if (material == null || !material.isItem()) {
            getLogger().warning("Ungültiges token.material in config.yml. Verwende SUNFLOWER.");
            material = Material.SUNFLOWER;
        }

        ItemStack token = new ItemStack(material);
        ItemMeta meta = token.getItemMeta();
        String displayName = getConfig().getString("token.display-name", "&6Token");
        meta.displayName(LEGACY.deserialize(displayName == null ? "&6Token" : displayName));
        List<String> lore = getConfig().getStringList("token.lore");
        if (!lore.isEmpty()) {
            meta.lore(lore.stream().map(LEGACY::deserialize).toList());
        }
        meta.getPersistentDataContainer().set(tokenKey, PersistentDataType.BYTE, (byte) 1);
        token.setItemMeta(meta);
        return token;
    }

    private void playWinAnimation(Player player, Location center) {
        if (getConfig().getBoolean("animation.announcement.enabled", true)) {
            String message = getConfig().getString("animation.announcement.message",
                    "&6✦ &eULTRA RARER TOKEN GEFUNDEN! &6✦");
            player.sendMessage(Component.empty());
            player.sendMessage(LEGACY.deserialize(message == null ? "" : message));
        }

        boolean soundsEnabled = getConfig().getBoolean("animation.sounds.enabled", true);
        boolean particlesEnabled = getConfig().getBoolean("animation.particles.enabled", true);
        if (soundsEnabled) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        }
        if (!particlesEnabled && !soundsEnabled) return;

        int duration = Math.max(1, Math.min(200, getConfig().getInt("animation.duration-ticks", 40)));
        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (tick >= duration || !player.isOnline()) {
                    cancel();
                    return;
                }
                if (particlesEnabled) {
                    double angle = tick * Math.PI / 8.0;
                    double radius = 0.8 + tick / (double) duration;
                    Location particleLocation = center.clone().add(
                            Math.cos(angle) * radius, tick * 0.045, Math.sin(angle) * radius);
                    center.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 3, 0.08, 0.08, 0.08, 0.01);
                    center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 2, 0.35, 0.5, 0.35, 0.02);
                }
                if (soundsEnabled && (tick == 12 || tick == 26)) {
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

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("fortnitetoken.admin")) {
                sender.sendMessage(LEGACY.deserialize("&cDafür hast du keine Berechtigung."));
                return true;
            }
            reloadConfig();
            sender.sendMessage(LEGACY.deserialize("&aFortniteToken-Konfiguration neu geladen."));
            getLogger().info("Konfiguration wurde von " + sender.getName() + " neu geladen.");
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("test")) {
            sender.sendMessage(LEGACY.deserialize("&eVerwendung: /fttoken test oder /fttoken reload"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern benutzt werden.");
            return true;
        }
        if (!player.hasPermission("fortnitetoken.test")) {
            player.sendMessage(Component.text("Dafür hast du keine Berechtigung.", net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        giveTokenAndCelebrate(player, player.getLocation().add(0, 1, 0));
        player.sendMessage(Component.text("Test erfolgreich: Token und Animation ausgelöst.",
                net.kyori.adventure.text.format.NamedTextColor.GREEN));
        return true;
    }
}
