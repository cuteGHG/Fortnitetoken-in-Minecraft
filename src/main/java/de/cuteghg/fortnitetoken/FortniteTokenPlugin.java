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
import org.bukkit.block.Lootable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public final class FortniteTokenPlugin extends JavaPlugin implements Listener {

    private NamespacedKey tokenKey;

    @Override
    public void onEnable() {
        this.tokenKey = new NamespacedKey(this, "fortnite_token");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("FortniteToken aktiviert. Dungeon-Drop-Chance: 1:1000.");
    }

    @EventHandler
    public void onDungeonChestOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Location location = event.getInventory().getLocation();
        if (location == null) {
            return;
        }

        Block block = location.getBlock();
        BlockState state = block.getState();

        // Natürliche Dungeon-Truhen besitzen die Vanilla-Loot-Tabelle
        // minecraft:chests/simple_dungeon. Spielerplatzierte Truhen besitzen
        // diese Loot-Tabelle nicht und werden daher ignoriert.
        if (!(state instanceof Lootable lootable) || lootable.getLootTable() == null) {
            return;
        }

        NamespacedKey lootTableKey = lootable.getLootTable().getKey();
        if (!NamespacedKey.minecraft("chests/simple_dungeon").equals(lootTableKey)) {
            return;
        }

        // Genau 1 Gewinnchance bei 1000 Öffnungen einer Dungeon-Truhe.
        if (ThreadLocalRandom.current().nextInt(1000) != 0) {
            return;
        }

        player.getInventory().addItem(createToken());
        playWinAnimation(player, block.getLocation().add(0.5, 1.0, 0.5));
    }

    private ItemStack createToken() {
        ItemStack token = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = token.getItemMeta();
        meta.displayName(Component.text("Token", NamedTextColor.GOLD));
        meta.lore(java.util.List.of(
                Component.text("Ein seltener Fortnite-Token", NamedTextColor.GRAY),
                Component.text("Gefunden in einer Dungeon-Truhe", NamedTextColor.DARK_GRAY)
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
                double radius = 0.8 + (tick / 40.0);
                Location particleLocation = center.clone().add(
                        Math.cos(angle) * radius,
                        tick * 0.045,
                        Math.sin(angle) * radius
                );

                center.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 3, 0.08, 0.08, 0.08, 0.01);
                center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 2, 0.35, 0.5, 0.35, 0.02);

                if (tick == 12 || tick == 26) {
                    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
                }
                tick++;
            }
        }.runTaskTimer(this, 0L, 1L);
    }
}
