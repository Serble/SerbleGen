package net.serble.SerbleGen.Util;

import net.serble.SerbleGen.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class EventManager implements Listener {
    // This class exists to prevent checks similar to isPlayerInGenWorld() from being repeated in multiple classes
    // It also allows for the use of one event handler preventing another from being called

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Check if the block broken is in a gen world
        if (!SerbleGen.isInGenWorld(e.getBlock().getLocation())) {
            return;
        }

        // Disallow breaking if it was not done by a player
        if (!SerbleGen.isInGenWorld(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }

        PlayTimeRewards.onBlockBreak(e);

        // Returns 2 if the block is an ore resource and the player has the correct tool
        // Returns 1 if the event should be cancelled
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
        if (!SerbleGen.isInGenWorld(e.getPlayer())) {
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
        if (!SerbleGen.isInGenWorld(e.getPlayer())) {
            return;
        }

        Shops.onMove(e);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        // Check if the player is in a gen world and is a player
        if (!(e.getEntity() instanceof Player) || !SerbleGen.isInGenWorld(e.getEntity())) {
            return;
        }

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
            return;
        }

        Shops.onDamage(e);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (SerbleGen.isInGenWorld((Player) e.getWhoClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killer = player.getKiller();

        if (!SerbleGen.isInGenWorld(player)) {
            return;
        }

        RareDrops.onPlayerKill(player, killer);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!SerbleGen.isInGenWorld(e.getPlayer()) ||
                e.getPlayer().getGameMode() == GameMode.CREATIVE ||
                e.getClickedBlock() == null) {
            return;
        }

        // Prevent crop trampling
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }

        // Prevent opening containers
        if (switch (e.getClickedBlock().getType()) {
            case CHEST, TRAPPED_CHEST, BARREL, FURNACE, BLAST_FURNACE, SMOKER, HOPPER,
                    DROPPER, DISPENSER, BREWING_STAND, ENCHANTING_TABLE -> true;
            default -> false;
        }) {
            e.setCancelled(true);
        }
    }
}
