package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.ResourceLocation;
import net.serble.SerbleGen.Schemas.ShopLocation;
import net.serble.SerbleGen.Util.EventManager;
import net.serble.serblenetworkplugin.API.DebugService;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SerbleGen extends JavaPlugin {

    public static SerbleGen plugin;
    public static List<String> genWorlds;
    public static List<String> fullFeaturedWorlds;
    public static Random random = new Random();
    public static DebugService debugService;

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();

        OreResources.init();
        Bags.init();
        Shops.init();
        RareDrops.init();

        genWorlds = getConfig().getStringList("gen-worlds");
        fullFeaturedWorlds = getConfig().getStringList("full-featured-worlds");

        getServer().getPluginManager().registerEvents(new EventManager(), this);

        RegisteredServiceProvider<DebugService> debugServiceProvider = getServer().getServicesManager().getRegistration(DebugService.class);
        if (debugServiceProvider != null) {
            debugService = debugServiceProvider.getProvider();
        } else {
            Bukkit.getLogger().warning("DebugService not found!");
        }
    }

    @Override
    public void onDisable() {
        Bags.onDisable();
    }

    public static void addXp(Player p, float amount) {
        float xp = p.getExp() + amount;
        while (xp > 1f) {
            xp -= 1f;
            p.setLevel(p.getLevel() + 1);
        }

        p.setExp(xp);
    }

    public static boolean isInGenWorld(Entity e) {
        return genWorlds.contains(e.getWorld().getName());
    }

    public static boolean isInGenWorld(Location loc) {
        return genWorlds.contains(loc.getWorld().getName());
    }

    public static boolean isInFullFeaturedWorld(Entity e) {
        return fullFeaturedWorlds.contains(e.getWorld().getName());
    }

    public static boolean isInFullFeaturedWorld(Location loc) {
        return fullFeaturedWorlds.contains(loc.getWorld().getName());
    }

    public static boolean isInArea(Location loc, ResourceLocation res) {
        for (int i = 0; i < res.pos1s.length; i++) {
            if (isInArea(loc, res.pos1s[i], res.pos2s[i])) {
                return true;
            }
        }

        return false;
    }

    public static boolean isInArea(Location loc, ShopLocation shop) {
        for (int i = 0; i < shop.pos1s.length; i++) {
            if (isInArea(loc, shop.pos1s[i], shop.pos2s[i])) {
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

    public static void giveItem(Player p, Location blockLocation, ItemStack... items) {
        HashMap<Integer, ItemStack> failedItems = p.getInventory().addItem(items);

        for (ItemStack item : failedItems.values()) {
            p.getWorld().dropItemNaturally(blockLocation, item);
        }
    }
}
