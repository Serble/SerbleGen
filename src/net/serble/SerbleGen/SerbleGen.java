package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.ResourceLocation;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class SerbleGen extends JavaPlugin implements Listener {

    public static SerbleGen plugin;
    public static List<String> genWorlds;
    public static List<ResourceLocation> locations;
    public static Random random = new Random();

    @Override
    public void onEnable() {
        plugin = this;

        ConfigurationSection configLocations = SerbleGen.plugin.getConfig().getConfigurationSection("resources");

        locations = new ArrayList<>();
        for (String resKey : configLocations.getKeys(false)) {
            ConfigurationSection res = configLocations.getConfigurationSection(resKey);

            ResourceLocation loc = new ResourceLocation();
            World world = Bukkit.getWorld(res.getString("world"));
            {
                List<Location> pos1s = new ArrayList<>();
                List<Location> pos2s = new ArrayList<>();
                for (String pointKey : res.getString("points").split(",")) {
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

        GenRespawns.init();
        Bags.init();
        getServer().getPluginManager().registerEvents(new PlayTimeRewards(), this);
        getServer().getPluginManager().registerEvents(this, this);

        this.saveDefaultConfig();
        genWorlds = getConfig().getStringList("gen-worlds");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Check if the player is in a gen world
        if (!SerbleGen.isPlayerInGenWorld(e.getPlayer().getName())) {
            return;
        }

        Bukkit.getLogger().info("Player broke block!");

        // Check if the block is a specific tool area
        for (ResourceLocation loc : locations) {
            if (SerbleGen.isInArea(e.getBlock().getLocation(), loc)) {
                Bukkit.getLogger().info("Player broke block in area!" + loc.permTag + " " + e.getPlayer().getName() + " " + loc.blockType.toString() + " " + loc.permTag);

                // Check if the player is using the correct tool
                Player p = e.getPlayer();

                if (!loc.permTag.equalsIgnoreCase("none")) {
                    ItemStack mainHand = p.getInventory().getItemInMainHand();
                    if (p.getGameMode() == GameMode.SURVIVAL && !NbtHandler.itemStackHasTag(mainHand, loc.permTag, PersistentDataType.STRING)) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(ChatColor.RED + "This tool cannot break this block!");
                        return;
                    }
                }

                GenRespawns.onLocBreak(e, loc);

                if (e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    e.setDropItems(false);
                    p.getInventory().addItem(new ItemStack(loc.dropItem, 1));
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, random.nextFloat() * 1.5f + 0.5f);

                    addXp(p, random.nextFloat() * 0.05f + 0.05f);
                }
                return;
            }
        }

        if (e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        // Check if the player is in a gen world
        if (!SerbleGen.isPlayerInGenWorld(e.getPlayer().getName())) {
            return;
        }

        // Check if the block is a specific tool area
        for (ResourceLocation loc : locations) {
            if (SerbleGen.isInArea(e.getBlock().getLocation(), loc) && e.getBlockPlaced().getType() == loc.blockType) {
                return;
            }
        }

        if (e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (SerbleGen.isPlayerInGenWorld(e.getWhoClicked().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPVP(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killer = player.getKiller();

        if (killer == null || !SerbleGen.isPlayerInGenWorld(killer.getName()) || !SerbleGen.isPlayerInGenWorld(player.getName()) || player == killer) {
            return;
        }

        addXp(killer, player.getLevel()/2f + player.getExp()/2f);
        player.setExp(0);
        player.setLevel(0);
    }

    public static void addXp(Player p, float amount) {
        float xp = p.getExp() + amount;
        while (xp > 1f) {
            xp -= 1f;
            p.setLevel(p.getLevel() + 1);
        }

        p.setExp(xp);
    }

    public static boolean isPlayerInGenWorld(String playerName) {
        return genWorlds.contains(Objects.requireNonNull(plugin.getServer().getPlayer(playerName)).getWorld().getName());
    }

    public static boolean isInArea(Location loc, ResourceLocation res) {
        for (int i = 0; i < res.pos1s.length; i++) {
            if (isInArea(loc, res.pos1s[i], res.pos2s[i])) {
                return true;
            }
        }

        return false;
    }

    public static boolean isInArea(Location loc, Location pos1, Location pos2) {
        return loc.getX() >= Math.min(pos1.getX(), pos2.getX()) && loc.getX() <= Math.max(pos1.getX(), pos2.getX()) &&
                loc.getY() >= Math.min(pos1.getY(), pos2.getY()) && loc.getY() <= Math.max(pos1.getY(), pos2.getY()) &&
                loc.getZ() >= Math.min(pos1.getZ(), pos2.getZ()) && loc.getZ() <= Math.max(pos1.getZ(), pos2.getZ());
    }

    private static Location parseLocation(World world, String loc) {
        String[] locSplit = loc.split(" ");
        return new Location(world, Double.parseDouble(locSplit[0]), Double.parseDouble(locSplit[1]), Double.parseDouble(locSplit[2]));
    }
}
