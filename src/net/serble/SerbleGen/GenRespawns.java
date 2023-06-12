package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.BlockRespawnLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenRespawns implements Listener {
    public static BlockRespawnLocation[] locations;

    public static void loadLocations() {
        // Get config entries
        List<String> configLocations = SerbleGen.plugin.getConfig().getStringList("respawns");

        List<BlockRespawnLocation> respawnLocations = new ArrayList<>();
        for (String locRaw : configLocations) {
            // Parse gen|0,0,0|0,0,0|OAK_LOG to a BlockRespawnLocation
            String[] locSplit = locRaw.split("\\|");
            BlockRespawnLocation loc = new BlockRespawnLocation();
            World world = Bukkit.getWorld(locSplit[0]);
            loc.pos1 = parseLocation(world, locSplit[1]);
            loc.pos2 = parseLocation(world, locSplit[2]);
            loc.blockType = Material.getMaterial(locSplit[3]);
            respawnLocations.add(loc);
            Bukkit.getLogger().info("Loaded respawn location: " + locRaw);
        }

        locations = respawnLocations.toArray(new BlockRespawnLocation[0]);
        
        for (BlockRespawnLocation loc : GenRespawns.locations) {
            int minX = Math.min(loc.pos1.getBlockX(), loc.pos2.getBlockX());
            int maxX = Math.max(loc.pos1.getBlockX(), loc.pos2.getBlockX());
            int minY = Math.min(loc.pos1.getBlockY(), loc.pos2.getBlockY());
            int maxY = Math.max(loc.pos1.getBlockY(), loc.pos2.getBlockY());
            int minZ = Math.min(loc.pos1.getBlockZ(), loc.pos2.getBlockZ());
            int maxZ = Math.max(loc.pos1.getBlockZ(), loc.pos2.getBlockZ());

            // Loop through and fill blocks
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Objects.requireNonNull(loc.pos1.getWorld()).getBlockAt(x, y, z).setType(loc.blockType);
                    }
                }
            }

        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Check if the player is in a gen world
        if (!SerbleGen.isPlayerInGenWorld(e.getPlayer().getName())) {
            return;
        }

        // Check if the block is a respawn location
        for (BlockRespawnLocation loc : locations) {
            if (SerbleGen.isInArea(e.getBlock().getLocation(), loc.pos1, loc.pos2)) {
                SerbleGen.plugin.getServer().getScheduler().runTaskLater(SerbleGen.plugin, () -> e.getBlock().setType(loc.blockType), 20 * 24);
                return;
            }
        }
    }

    private static Location parseLocation(World world, String loc) {
        String[] locSplit = loc.split(",");
        return new Location(world, Double.parseDouble(locSplit[0]), Double.parseDouble(locSplit[1]), Double.parseDouble(locSplit[2]));
    }

}
