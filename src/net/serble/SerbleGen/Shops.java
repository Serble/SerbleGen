package net.serble.SerbleGen;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.serble.SerbleGen.Schemas.ShopLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public class Shops {
    public static List<ShopLocation> locations;

    public static void init() {
        ConfigurationSection configLocations = SerbleGen.plugin.getConfig().getConfigurationSection("shops");

        locations = new ArrayList<>();
        for (String resKey : configLocations.getKeys(false)) {
            ConfigurationSection res = configLocations.getConfigurationSection(resKey);

            ShopLocation loc = new ShopLocation();
            World world = Bukkit.getWorld(res.getString("world"));

            SerbleGen.getLocations(world, res, loc);

            {
                String[] spawnSplit = res.getString("spawn").split(" ");
                loc.spawnPos = new Location(world, Double.parseDouble(spawnSplit[0]), Double.parseDouble(spawnSplit[1]), Double.parseDouble(spawnSplit[2]));
                loc.spawnPos.setYaw(Float.parseFloat(spawnSplit[3]));
            }

            locations.add(loc);
        }
    }

    public static void onMove(PlayerMoveEvent e) {
        for (ShopLocation loc : locations) {
            if (SerbleGen.isInArea(e.getTo(), loc) && !SerbleGen.isInArea(e.getFrom(), loc)) {
                SerbleGen.inventoryManagementService.setSpawnPoint(e.getPlayer(), loc.spawnPos);
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Spawn point set!"));
            }
        }
    }

    public static void onDamage(EntityDamageEvent e) {
        for (ShopLocation loc : locations) {
            if (SerbleGen.isInArea(e.getEntity().getLocation(), loc)) {
                e.setCancelled(true);
            }
        }
    }

    public static void onHurt(EntityDamageByEntityEvent e) {
        for (ShopLocation loc : locations) {
            if (SerbleGen.isInArea(e.getEntity().getLocation(), loc) || SerbleGen.isInArea(e.getDamager().getLocation(), loc)) {
                e.setCancelled(true);
            }
        }
    }

    public static void onProjectileHit(ProjectileHitEvent e) {
        Player shooter = (Player) e.getEntity().getShooter();

        for (ShopLocation loc : locations) {
            if (SerbleGen.isInArea(shooter.getLocation(), loc) || SerbleGen.isInArea(e.getHitEntity().getLocation(), loc)) {
                e.setCancelled(true);
            }
        }
    }
}
