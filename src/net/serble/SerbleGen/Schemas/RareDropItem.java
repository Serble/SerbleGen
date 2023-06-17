package net.serble.SerbleGen.Schemas;

import net.serble.SerbleGen.SerbleGen;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RareDropItem {
    public float weight;
    public int type;

    public String command;
    public ItemStack item;

    public void execute(Player p) {
        if (type == 0) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.formatted(p.getName()));
        }
        else {
            SerbleGen.giveItem(p, p.getLocation(), item);
        }
    }
}
