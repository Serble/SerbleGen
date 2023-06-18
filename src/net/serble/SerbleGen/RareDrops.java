package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.RareDropItem;
import org.bukkit.Bukkit;
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

        SerbleGen.addXp(p, SerbleGen.random.nextFloat() * 0.1f + 0.05f);

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
}
