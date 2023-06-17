package net.serble.SerbleGen;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class EventManager implements Listener {
    // This class exists to prevent checks similar to isPlayerInGenWorld() from being repeated in multiple classes
    // It also allows for the use of one event handler preventing another from being called

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Check if the player is in a gen world
        if (!SerbleGen.isPlayerInGenWorld(e.getPlayer())) {
            return;
        }

        PlayTimeRewards.onBlockBreak(e);

        // Returns true if the block is an ore resource and the player has the correct tool
        int oreResourcesResult = OreResources.onBlockBreak(e);
        if (oreResourcesResult == 1) {
            e.setCancelled(true);
            return;
        }

        if (oreResourcesResult == 2) {
            RareDrops.onBlockBreak(e);
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
        if (!SerbleGen.isPlayerInGenWorld(e.getPlayer())) {
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
    public void onMove(PlayerMoveEvent e) {
        // Check if the player is in a gen world
        if (!SerbleGen.isPlayerInGenWorld(e.getPlayer())) {
            return;
        }

        Shops.onMove(e);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        // Check if the player is in a gen world and is a player
        if (!(e.getEntity() instanceof Player) || !SerbleGen.isPlayerInGenWorld((Player) e.getEntity())) {
            return;
        }

        Shops.onDamage(e);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (SerbleGen.isPlayerInGenWorld((Player) e.getWhoClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killer = player.getKiller();

        if (!SerbleGen.isPlayerInGenWorld(player)) {
            return;
        }

        // If there is a killer
        if (killer != null && SerbleGen.isPlayerInGenWorld(killer) && player.getUniqueId() != killer.getUniqueId()) {
            SerbleGen.addXp(killer, player.getLevel() / 2f + player.getExp() / 2f);
        }

        // Code to run when a player dies in a gen world, regardless of whether there is a killer
        player.setExp(0);
        player.setLevel(0);
    }
}
