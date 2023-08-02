package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.RareDropItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RareDrops {
    public static final List<RareDropItem> drops = new ArrayList<>();
    public static float totalWeight = 0f;

    public static void init() {
        ConfigurationSection section = SerbleGen.plugin.getConfig().getConfigurationSection("drops");

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);

            RareDropItem item = new RareDropItem();
            if (itemSection.contains("command")) {
                item.command = itemSection.getString("command");
                item.type = 0;
            }
            else {
                item.item = new ItemStack(Material.getMaterial(itemSection.getString("material")), itemSection.getInt("count", 1));
                item.type = 1;
            }


            item.weight = (float) itemSection.getDouble("weight");
            totalWeight += item.weight;

            drops.add(item);
        }
    }

    public static void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();

        // If the player is not in a full featured world, don't do anything
        if (!SerbleGen.isInFullFeaturedWorld(p)) {
            return;
        }

        SerbleGen.addXp(p, 1.0f/18.0f);

        // Make item only drop randomly depending on the player's xp level
        if (SerbleGen.random.nextFloat() > Math.pow(p.getLevel(), 0.5) / 100f) {
            return;
        }

        float rand = SerbleGen.random.nextFloat() * totalWeight;

        for (RareDropItem item : drops) {
            if (rand < item.weight) {
                item.execute(p);
                break;
            }

            rand -= item.weight;
        }
    }

    public static void onPlayerKill(Player player, Player killer) {
        // If the player is not in a full featured world, don't do anything
        if (!SerbleGen.isInFullFeaturedWorld(player)) {
            return;
        }

        // If there is a killer
        if (killer != null && SerbleGen.isInGenWorld(killer) && player.getUniqueId() != killer.getUniqueId()) {
            SerbleGen.addXp(killer, player.getLevel() / 2f + player.getExp() / 2f);
        }

        // Code to run when a player dies in a gen world, regardless of whether there is a killer
        player.setExp(0);
        player.setLevel(0);
    }
}
