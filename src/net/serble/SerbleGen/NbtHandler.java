package net.serble.SerbleGen;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class NbtHandler {

    public static boolean itemStackContainsTag(ItemStack itemStack, String tagName) {
        // Get the ItemStack's ItemMeta
        ItemMeta itemMeta = itemStack.getItemMeta();
        // Check if the item meta is not null and has the given tag
        return itemMeta != null && itemMeta.getPersistentDataContainer().has(new NamespacedKey(SerbleGen.plugin, tagName), PersistentDataType.STRING);
    }

}
