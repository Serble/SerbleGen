package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.AreaLocation;
import net.serble.SerbleGen.Util.EventManager;
import net.serble.serblenetworkplugin.API.DebugService;
import net.serble.serblenetworkplugin.API.IdService;
import net.serble.serblenetworkplugin.API.InventoryManagementService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SerbleGen extends JavaPlugin {

    public static SerbleGen plugin;
    public static List<String> genWorlds;
    public static List<String> fullFeaturedWorlds;
    public static Random random = new Random();
    public static DebugService debugService;
    public static InventoryManagementService inventoryManagementService;
    public static IdService idService;

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();

        OreResources.init();
        MonsterResources.init();
        Bags.init();
        Shops.init();
        RareDrops.init();
        StartingItems.init();

        genWorlds = getConfig().getStringList("gen-worlds");
        fullFeaturedWorlds = getConfig().getStringList("full-featured-worlds");

        getServer().getPluginManager().registerEvents(new EventManager(), this);

//        debugService = registerService(DebugService.class);
//        inventoryManagementService = registerService(InventoryManagementService.class);

        RegisteredServiceProvider<DebugService> debugServiceProvider = getServer().getServicesManager().getRegistration(DebugService.class);
        if (debugServiceProvider != null) {
            debugService = debugServiceProvider.getProvider();
        } else {
            Bukkit.getLogger().warning("DebugService not found!");
        }

        RegisteredServiceProvider<InventoryManagementService> invServiceProvider = getServer().getServicesManager().getRegistration(InventoryManagementService.class);
        if (invServiceProvider != null) {
            inventoryManagementService = invServiceProvider.getProvider();
        } else {
            Bukkit.getLogger().warning("InvService not found!");
        }

        RegisteredServiceProvider<IdService> idServiceProvider = getServer().getServicesManager().getRegistration(IdService.class);
        if (idServiceProvider != null) {
            idService = idServiceProvider.getProvider();
        } else {
            Bukkit.getLogger().warning("IdService not found!");
        }
    }

    @Override
    public void onDisable() {
        Bags.onDisable();
        MonsterResources.despawnAll();
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

    public static boolean isInArea(Location loc, AreaLocation location) {
        if (location.pos1s.length == 0) {
            return false;
        }

        if (location.pos1s[0].getWorld().getUID() != loc.getWorld().getUID()) {
            return false;
        }

        for (int i = 0; i < location.pos1s.length; i++) {
            if (isInArea(loc, location.pos1s[i], location.pos2s[i])) {
                return true;
            }
        }

        return false;
    }

    public static boolean isInArea(Location loc, Location pos1, Location pos2) {
        return loc.getX() >= pos1.getX() && loc.getX() <= pos2.getX() &&
                loc.getY() >= pos1.getY() && loc.getY() <= pos2.getY() &&
                loc.getZ() >= pos1.getZ() && loc.getZ() <= pos2.getZ();
    }

    public static void giveItem(Player p, Location dropLocation, ItemStack... items) {
        HashMap<Integer, ItemStack> failedItems = p.getInventory().addItem(items);

        for (ItemStack item : failedItems.values()) {
            p.getWorld().dropItemNaturally(dropLocation, item);
        }
    }

    public static void getLocations(World world, ConfigurationSection res, AreaLocation loc) {
        List<Location> pos1s = new ArrayList<>();
        List<Location> pos2s = new ArrayList<>();
        for (String pointKey : res.getStringList("points")) {
            String[] locSplit = pointKey.split(" ");

            int x1 = Integer.parseInt(locSplit[0]);
            int y1 = Integer.parseInt(locSplit[1]);
            int z1 = Integer.parseInt(locSplit[2]);
            int x2 = Integer.parseInt(locSplit[3]);
            int y2 = Integer.parseInt(locSplit[4]);
            int z2 = Integer.parseInt(locSplit[5]);

            int xMin = Math.min(x1, x2);
            int yMin = Math.min(y1, y2);
            int zMin = Math.min(z1, z2);
            int xMax = Math.max(x1, x2);
            int yMax = Math.max(y1, y2);
            int zMax = Math.max(z1, z2);

            pos1s.add(new Location(world, xMin, yMin, zMin));
            pos2s.add(new Location(world, xMax, yMax, zMax));
        }

        loc.pos1s = pos1s.toArray(new Location[0]);
        loc.pos2s = pos2s.toArray(new Location[0]);
    }
}
