package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.ResourceLocation;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OreResources {
    public static List<ResourceLocation> locations;
    public static long respawnTime;

    public static void init() {
        respawnTime = (long)(SerbleGen.plugin.getConfig().getDouble("respawn-time") * 20);

        ConfigurationSection configLocations = SerbleGen.plugin.getConfig().getConfigurationSection("resources");

        locations = new ArrayList<>();
        for (String resKey : configLocations.getKeys(false)) {
            ConfigurationSection res = configLocations.getConfigurationSection(resKey);

            ResourceLocation loc = new ResourceLocation();
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
            loc.blockType = Material.getMaterial(res.getString("block"));
            loc.dropItem = Material.getMaterial(res.getString("drop"));
            loc.permTag = res.getString("perm");
            if (loc.permTag == null) {
                loc.permTag = "none";
            }

            locations.add(loc);
        }

        SerbleGen.plugin.getServer().getScheduler().runTaskLater(SerbleGen.plugin, OreResources::respawnAll, 1);
    }

    public static boolean onBlockBreak(BlockBreakEvent e) {
        // Check if the block is a specific tool area
        for (ResourceLocation loc : locations) {
            if (!SerbleGen.isInArea(e.getBlock().getLocation(), loc)) {
                continue;
            }

            // Check if the player is using the correct tool
            Player p = e.getPlayer();

            // Check if the player's tool has the correct permission
            if (!loc.permTag.equalsIgnoreCase("none")) {
                ItemStack mainHand = p.getInventory().getItemInMainHand();
                if (p.getGameMode() == GameMode.SURVIVAL && !NbtHandler.itemStackHasTag(mainHand, loc.permTag, PersistentDataType.STRING)) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "This tool cannot break this block!");
                    return true;
                }
            }

            // Respawn the block after delay
            SerbleGen.plugin.getServer().getScheduler().runTaskLater(SerbleGen.plugin, () ->
                    e.getBlock().setType(loc.blockType), respawnTime);

            // Auto-pickup the item and give xp
            if (e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                e.setDropItems(false);
                SerbleGen.giveItem(p, e.getBlock().getLocation(), new ItemStack(loc.dropItem, 1));

                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, SerbleGen.random.nextFloat() * 1.5f + 0.5f);
            }
            return true;
        }

        return false;
    }

    public static boolean onBlockPlace(BlockPlaceEvent e) {
        // Only allow if the block is in a specific resource and the type matches the resource type
        for (ResourceLocation loc : locations) {
            if (SerbleGen.isInArea(e.getBlock().getLocation(), loc) && e.getBlockPlaced().getType() == loc.blockType) {
                return true;
            }
        }

        return false;
    }

    public static void respawnAll() {
        for (ResourceLocation loc : locations) {
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
}
