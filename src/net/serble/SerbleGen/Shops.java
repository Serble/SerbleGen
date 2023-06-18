package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.ShopLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent;
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

            {
                List<Location> pos1s = new ArrayList<>();
                List<Location> pos2s = new ArrayList<>();
                for (String pointKey : res.getStringList("points")) {
                    String[] locSplit = pointKey.split(" ");
                    pos1s.add(new Location(world, Integer.parseInt(locSplit[0]), Integer.parseInt(locSplit[1]), Integer.parseInt(locSplit[2])));
                    pos2s.add(new Location(world, Integer.parseInt(locSplit[3]), Integer.parseInt(locSplit[4]), Integer.parseInt(locSplit[5])));
                }

                loc.pos1s = pos1s.toArray(new Location[0]);
                loc.pos2s = pos2s.toArray(new Location[0]);
            }

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
            if (SerbleGen.isInArea(e.getTo(), loc)) {
                e.getPlayer().setBedSpawnLocation(loc.spawnPos, true);
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
}
