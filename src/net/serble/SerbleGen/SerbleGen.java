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

public class SerbleGen extends JavaPlugin {

    public static SerbleGen plugin;
    public static List<String> genWorlds;
    public static Random random = new Random();

    @Override
    public void onEnable() {
        plugin = this;

        OreResources.init();
        Bags.init();

        getServer().getPluginManager().registerEvents(new EventManager(), this);

        this.saveDefaultConfig();
        genWorlds = getConfig().getStringList("gen-worlds");
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
