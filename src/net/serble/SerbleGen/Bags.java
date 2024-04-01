package net.serble.SerbleGen;

import net.minecraft.util.Tuple;
import net.serble.SerbleGen.Util.ConfigManager;
import net.serble.SerbleGen.Util.NbtHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Bags implements Listener {
    private static final Map<Long, ItemStack[]> bags = new HashMap<>();
    private static final Map<UUID, Tuple<Integer, Long>> openBags = new HashMap<>();
    private static final Random idGen = new Random();

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Check if the player is holding a bag
        ItemStack item = p.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (!NbtHandler.hasTag(data, "bag", PersistentDataType.STRING)) {
            return;
        }

        e.setCancelled(true);

        if (item.getAmount() > 1) {
            p.sendMessage(ChatColor.DARK_RED + "Please split the stack to use the bag!");
            return;
        }

        // Get the bag's data
        int size = NbtHandler.getTag(data, "bag_size", PersistentDataType.INTEGER);
        String name = NbtHandler.getTag(data, "bag_name", PersistentDataType.STRING);
        Long id = NbtHandler.getTag(data, "bag_id", PersistentDataType.LONG);

        if (id == null) {
            id = idGen.nextLong();
            NbtHandler.setTag(data, "bag_id", PersistentDataType.LONG, id);
            item.setItemMeta(meta);
        }

        // make sure size is a multiple of 9
        int realSize = (int) Math.ceil(size / 9.0) * 9;
        ItemStack[] contents = loadContents(id, realSize);

        // fill the extra slots with barrier blocks
        if (size != realSize) {
            // Create item
            ItemStack fillItem = new ItemStack(Material.BARRIER);
            ItemMeta fillMeta = fillItem.getItemMeta();
            fillMeta.setDisplayName(ChatColor.DARK_RED + "Upgrade Bag For More Space");
            NbtHandler.setTag(fillMeta.getPersistentDataContainer(), "bag_no_interact", PersistentDataType.STRING, "1");
            fillItem.setItemMeta(fillMeta);

            Bukkit.getLogger().info("Filling bag " + name + " with " + (realSize - size) + " barrier blocks " + realSize + " " + size + " " + contents.length);

            // Fill the slots
            for (int i = size; i < realSize; i++) {
                contents[i] = fillItem;
            }
        }

        Inventory inv = Bukkit.createInventory(null, realSize, name);
        inv.setContents(contents);
        p.openInventory(inv);

        openBags.put(p.getUniqueId(), new Tuple<>(size, id));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        UUID pUid = e.getPlayer().getUniqueId();
        if (!openBags.containsKey(pUid)) {
            return;
        }

        Tuple<Integer, Long> tuple = openBags.get(pUid);
        openBags.remove(pUid);
        int size = tuple.a();
        long id = tuple.b();

        ItemStack[] contents = new ItemStack[size];
        {
            ItemStack[] rawContents = e.getInventory().getContents();

            // Shrink to remove extra slots
            if (rawContents.length != size) {
                System.arraycopy(rawContents, 0, contents, 0, size);
            } else {
                contents = rawContents;
            }
        }

        // save the bag
        bags.put(id, contents);
        saveBag(id, contents);
    }

    private static void saveBag(long id, ItemStack[] contents) {
        FileConfiguration save = new YamlConfiguration();
        save.set("contents", contents);
        ConfigManager.save("bags/" + id + ".yml", save);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!openBags.containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }

        // Check if the player is clicking a backpack_no_interact item
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }

        if (NbtHandler.itemStackHasTag(item, "bag_no_interact", PersistentDataType.STRING)) {
            Bukkit.getLogger().info("no interact");
            e.setCancelled(true);
        }
    }

    private ItemStack[] loadContents(long id, int realSize) {
        // if it's already loaded return it
        if (bags.containsKey(id)) {
            ItemStack[] contents = bags.get(id);

            if (contents.length != realSize) {
                ItemStack[] newContents = new ItemStack[realSize];
                System.arraycopy(contents, 0, newContents, 0, contents.length);
                contents = newContents;
            }

            return contents;
        } else {
            FileConfiguration save = ConfigManager.load("bags/" + id + ".yml");

            // If save doesn't exist return an empty array
            if (save == null) {
                return new ItemStack[realSize];
            }

            // otherwise attempt to load the contents
            try {
                ItemStack[] contents = ((ArrayList<ItemStack>) save.get("contents")).toArray(new ItemStack[0]);

                // This will happen for every bag whose size is not a multiple of 9
                // This is because it does not save the filler blocks
                if (contents.length != realSize) {
                    ItemStack[] newContents = new ItemStack[realSize];
                    System.arraycopy(contents, 0, newContents, 0, Math.min(contents.length, realSize));
                    return newContents;
                }

                return contents;
            } catch (Exception e) {
                return new ItemStack[realSize];
            }
        }
    }

    public static void init() {
        Bukkit.getPluginManager().registerEvents(new Bags(), SerbleGen.plugin);
    }

    public static void onDisable() {
        for (Map.Entry<UUID, Tuple<Integer, Long>> entry : openBags.entrySet()) {
            long id = entry.getValue().b();

            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null) {
                continue;
            }

            ItemStack[] contents = p.getOpenInventory().getTopInventory().getContents();
            bags.put(id, contents);
            saveBag(id, contents);

            p.closeInventory();
        }
    }
}
