package net.serble.SerbleGen.Util;

import net.serble.SerbleGen.SerbleGen;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class NbtHandler {
    public static <T, Z> boolean itemStackSetTag(ItemStack itemStack, String tagName, PersistentDataType<T, Z> type, Z tagValue) {
        // Get the ItemStack's ItemMeta
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        // Set the tag
        setTag(itemMeta.getPersistentDataContainer(), tagName, type, tagValue);

        // Set the ItemStack's ItemMeta
        itemStack.setItemMeta(itemMeta);
        return true;
    }

    public static <T, Z> void setTag(PersistentDataContainer data, String tagName, PersistentDataType<T, Z> type, Z tagValue) {
        data.set(new NamespacedKey(SerbleGen.plugin, tagName), type, tagValue);
    }

    public static <T, Z> Z itemStackGetTag(ItemStack itemStack, String tagName, PersistentDataType<T, Z> type) {
        // Get the ItemStack's ItemMeta
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }

        // Get the tag
        return getTag(itemMeta.getPersistentDataContainer(), tagName, type);
    }

    public static <T, Z> Z getTag(PersistentDataContainer data, String tagName, PersistentDataType<T, Z> type) {
        return data.get(new NamespacedKey(SerbleGen.plugin, tagName), type);
    }

    public static <T, Z> boolean itemStackHasTag(ItemStack itemStack, String tagName, PersistentDataType<T, Z> type) {
        // Get the ItemStack's ItemMeta
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Check if the item meta is not null and has the given tag
        return itemMeta != null && hasTag(itemMeta.getPersistentDataContainer(), tagName, type);
    }

    public static <T, Z> boolean hasTag(PersistentDataContainer data, String tagName, PersistentDataType<T, Z> type) {
        return data.has(new NamespacedKey(SerbleGen.plugin, tagName), type);
    }
}
