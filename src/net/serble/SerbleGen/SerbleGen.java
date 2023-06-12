package net.serble.SerbleGen;

import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public class SerbleGen extends JavaPlugin implements Listener {

    public static SerbleGen plugin;
    public static List<String> genWorlds;

    @Override
    public void onEnable() {
        plugin = this;
        GenRespawns.loadLocations();
        SpecificToolAreas.loadLocations();
        getServer().getPluginManager().registerEvents(new PlayTimeRewards(), this);
        getServer().getPluginManager().registerEvents(new SpecificToolAreas(), this);
        getServer().getPluginManager().registerEvents(new GenRespawns(), this);

        this.saveDefaultConfig();
        genWorlds = getConfig().getStringList("gen-worlds");
    }

    public static boolean isPlayerInGenWorld(String playerName) {
        return genWorlds.contains(Objects.requireNonNull(plugin.getServer().getPlayer(playerName)).getWorld().getName());
    }

    public static boolean isInArea(Location loc, Location pos1, Location pos2) {
        return loc.getX() >= Math.min(pos1.getX(), pos2.getX()) && loc.getX() <= Math.max(pos1.getX(), pos2.getX()) &&
                loc.getY() >= Math.min(pos1.getY(), pos2.getY()) && loc.getY() <= Math.max(pos1.getY(), pos2.getY()) &&
                loc.getZ() >= Math.min(pos1.getZ(), pos2.getZ()) && loc.getZ() <= Math.max(pos1.getZ(), pos2.getZ());
    }
}
