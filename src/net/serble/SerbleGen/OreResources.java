package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.OreLocation;
import net.serble.SerbleGen.Schemas.ToolType;
import net.serble.SerbleGen.Util.NbtHandler;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class OreResources {
    public static List<OreLocation> locations;
    public static Map<Location, Float> breakCounter = new HashMap<>();

    public static void init() {
        double defaultRespawnTime = SerbleGen.plugin.getConfig().getDouble("respawn-time");

        ConfigurationSection configLocations = SerbleGen.plugin.getConfig().getConfigurationSection("ore_gens");

        locations = new ArrayList<>();
        for (String resKey : configLocations.getKeys(false)) {
            ConfigurationSection res = configLocations.getConfigurationSection(resKey);

            OreLocation loc = new OreLocation();
            World world = Bukkit.getWorld(res.getString("world"));
            SerbleGen.getLocations(world, res, loc);

            loc.blockType = Material.getMaterial(res.getString("block"));
            loc.dropItem = loc.blockType;

            String dropItemString = res.getString("drop", null);
            if (dropItemString != null) {
                loc.dropItem = Material.getMaterial(dropItemString);
            }

            loc.breakCount = res.getInt("break-count", 1);

            {
                String dropCountString = res.getString("drop-count", null);
                if (dropCountString != null) {
                    String[] dropCountSplit = dropCountString.split("-");

                    if (dropCountSplit.length == 1) {
                        loc.dropMin = Integer.parseInt(dropCountSplit[0]);
                        loc.dropVariation = 1;
                    } else {
                        loc.dropMin = Integer.parseInt(dropCountSplit[0]);
                        loc.dropVariation = Integer.parseInt(dropCountSplit[1]) - loc.dropMin + 1;
                    }
                } else {
                    loc.dropMin = 1;
                    loc.dropVariation = 1;
                }
            }

            loc.permTag = res.getString("perm");
            if (loc.permTag == null) {
                loc.permTag = "none";
            }

            loc.respawnTime = (long)(res.getDouble("respawn-time", defaultRespawnTime) * 20);

            loc.toolType = ToolType.valueOf(res.getString("tool", "Axe"));

            locations.add(loc);
        }

        SerbleGen.plugin.getServer().getScheduler().runTaskLater(SerbleGen.plugin, OreResources::respawnAll, 1);
    }

    public static int onBlockBreak(BlockBreakEvent e) {
        // Check if the block is a specific tool area
        for (OreLocation loc : locations) {
            if (!SerbleGen.isInArea(e.getBlock().getLocation(), loc)) {
                continue;
            }

            // Check if the player is using the correct tool
            Player p = e.getPlayer();

            // Check if the player's tool has the correct permission
            if (!loc.permTag.equalsIgnoreCase("none")) {
                ItemStack mainHand = p.getInventory().getItemInMainHand();
                if (p.getGameMode() == GameMode.SURVIVAL && !NbtHandler.itemStackHasTag(mainHand, loc.permTag, PersistentDataType.STRING)) {
                    p.sendMessage(ChatColor.RED + "This tool cannot break this block!");
                    return 1;
                }
            }

            if (loc.breakCount > 1 && p.getGameMode() != GameMode.CREATIVE) {
                // Check if the block has been broken enough times
                Location blockLoc = e.getBlock().getLocation();
                Float count = breakCounter.getOrDefault(blockLoc, null);

                ItemStack hand = p.getInventory().getItemInMainHand();
                float breakPower = getBreakPower(hand.getType(), loc.toolType);

                // Check if the player is using the correct tool
                if (breakPower != 0.75f) {
                    int efficiency = hand.getEnchantmentLevel(Enchantment.DIG_SPEED);
                    breakPower += 1 + (efficiency << 1);
                }

                if (count == null) {
                    breakCounter.put(blockLoc, breakPower);
                    return 1;
                }

                if (count >= loc.breakCount) {
                    breakCounter.remove(blockLoc);
                } else {
                    breakCounter.put(blockLoc, count + breakPower);
                    return 1;
                }
            }

            // Respawn the block after delay, ignore if its a regrow pickaxe from genitems
            PersistentDataContainer itemData = NbtHandler.getPersistentDataContainer(p.getInventory().getItemInMainHand());
            if (itemData == null || !Objects.equals(itemData.get(
                    Objects.requireNonNull(NamespacedKey.fromString("serbleitems:id")),
                    PersistentDataType.STRING), "regrow_pickaxe")) {

                SerbleGen.plugin.getServer().getScheduler().runTaskLater(SerbleGen.plugin, () ->
                        e.getBlock().setType(loc.blockType), loc.respawnTime);
            }

            // Auto-pickup the item and give xp
            if (p.getGameMode() != GameMode.CREATIVE) {
                e.setDropItems(false);
                SerbleGen.giveItem(p, e.getBlock().getLocation(), new ItemStack(loc.dropItem, SerbleGen.random.nextInt(loc.dropVariation) + loc.dropMin));

                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, SerbleGen.random.nextFloat() * 1.5f + 0.5f);
            }
            return 2;
        }

        return 0;
    }

    private static float getBreakPower(Material tool, ToolType type) {
        String toolSuffix = "_" + type.name().toUpperCase();

        String toolName = tool.toString();
        if (toolName.endsWith(toolSuffix)) {
            toolName = toolName.substring(0, toolName.length() - toolSuffix.length());
        }

        return switch (toolName) {
            case "WOODEN" -> 1;
            case "STONE" -> 2;
            case "IRON" -> 3;
            case "DIAMOND" -> 4;
            case "NETHERITE" -> 8;

            default -> 0.75f;
        };
    }

    public static boolean onBlockPlace(BlockPlaceEvent e) {
        // Only allow if the block is in a specific resource and the type matches the resource type
        for (OreLocation loc : locations) {
            if (SerbleGen.isInArea(e.getBlock().getLocation(), loc) && e.getBlockPlaced().getType() == loc.blockType) {
                return true;
            }
        }

        return false;
    }

    public static void respawnAll() {
        for (OreLocation loc : locations) {
            for (int i = 0; i < loc.pos1s.length; i++) {
                Location pos1 = loc.pos1s[i];
                Location pos2 = loc.pos2s[i];

                // Loop through and fill blocks
                for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
                    for (int y = pos1.getBlockY(); y <= pos2.getBlockY(); y++) {
                        for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {
                            Objects.requireNonNull(pos1.getWorld()).getBlockAt(x, y, z).setType(loc.blockType);
                        }
                    }
                }
            }
        }
    }
}
