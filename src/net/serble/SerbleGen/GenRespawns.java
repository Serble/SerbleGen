package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.ResourceLocation;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Objects;

public class GenRespawns {
    public static void init() {
        for (ResourceLocation loc : SerbleGen.locations) {
            for (int i = 0; i < loc.pos1s.length; i++) {
                Location pos1 = loc.pos1s[i];
                Location pos2 = loc.pos2s[i];

                int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
                int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
                int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
                int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
                int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
                int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

                // Loop through and fill blocks
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            Objects.requireNonNull(pos1.getWorld()).getBlockAt(x, y, z).setType(loc.blockType);
                        }
                    }
                }
            }
        }
    }

    public static void onLocBreak(BlockBreakEvent e, ResourceLocation loc) {
        SerbleGen.plugin.getServer().getScheduler().runTaskLater(SerbleGen.plugin, () -> e.getBlock().setType(loc.blockType), 20 * 24);
    }
}
