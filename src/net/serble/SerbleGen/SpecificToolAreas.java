package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.SpecificToolArea;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;

public class SpecificToolAreas implements Listener {

    public static SpecificToolArea[] locations;

    public static void loadLocations() {
        // Get config entries
        List<String> configLocations = SerbleGen.plugin.getConfig().getStringList("specific-tool-areas");

        List<SpecificToolArea> respawnLocations = new ArrayList<>();
        for (String locRaw : configLocations) {
            // Parse gen|0,0,0|0,0,0|nbttag to a SpecificToolArea
            String[] locSplit = locRaw.split("\\|");
            SpecificToolArea loc = new SpecificToolArea();
            World world = Bukkit.getWorld(locSplit[0]);
            loc.pos1 = parseLocation(world, locSplit[1]);
            loc.pos2 = parseLocation(world, locSplit[2]);
            loc.nbtTag = locSplit[3];
            respawnLocations.add(loc);
            Bukkit.getLogger().info("Loaded specific tool area: " + locRaw);
        }

        locations = respawnLocations.toArray(new SpecificToolArea[0]);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Check if the player is in a gen world
        if (!SerbleGen.isPlayerInGenWorld(e.getPlayer().getName())) {
            return;
        }

        // Check if the block is a specific tool area
        for (SpecificToolArea loc : locations) {
            if (SerbleGen.isInArea(e.getBlock().getLocation(), loc.pos1, loc.pos2)) {
                // Check if the player is using the correct tool
                e.getPlayer().getInventory().getItemInMainHand();
                if (!NbtHandler.itemStackContainsTag(e.getPlayer().getInventory().getItemInMainHand(), loc.nbtTag)) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "This tool cannot break this block!");
                    return;
                }
                return;
            }
        }
    }

    private static Location parseLocation(World world, String loc) {
        String[] locSplit = loc.split(",");
        return new Location(world, Double.parseDouble(locSplit[0]), Double.parseDouble(locSplit[1]), Double.parseDouble(locSplit[2]));
    }

}
