package net.serble.SerbleGen;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;

public class EventManager implements Listener {
    // This class exists to prevent checks similar to isPlayerInGenWorld() from being repeated in multiple classes
    // It also allows for the use of one event handler preventing another from being called

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Check if the player is in a gen world
        if (!SerbleGen.isPlayerInGenWorld(e.getPlayer().getName())) {
            return;
        }

        PlayTimeRewards.onBlockBreak(e);

        if (OreResources.onBlockBreak(e)) {
            return;
        }

        // Prevent breaking blocks in gen worlds
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

        if (OreResources.onBlockPlace(e)) {
            return;
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
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killer = player.getKiller();

        if (!SerbleGen.isPlayerInGenWorld(player.getName())) {
            return;
        }

        // If there is a killer
        if (killer != null && SerbleGen.isPlayerInGenWorld(killer.getName()) && player.getUniqueId() != killer.getUniqueId()) {
            SerbleGen.addXp(killer, player.getLevel() / 2f + player.getExp() / 2f);
        }

        // Code to run when a player dies in a gen world, regardless of whether there is a killer
        player.setExp(0);
        player.setLevel(0);
    }
}
