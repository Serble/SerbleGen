package net.serble.SerbleGen.Util;

import net.serble.SerbleGen.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class EventManager implements Listener {
    // This class exists to prevent checks similar to isPlayerInGenWorld() from being repeated in multiple classes
    // It also allows for the use of one event handler preventing another from being called

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Check if the block broken is in a gen world
        if (!SerbleGen.isInGenWorld(e.getBlock().getLocation())) {
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
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
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

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
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
    public void onHurt(EntityDamageByEntityEvent e) {
        // Check if the player is in a gen world and is a player
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player) || !SerbleGen.isInGenWorld(e.getEntity())) {
            return;
        }

        Shops.onHurt(e);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player) || !SerbleGen.isInGenWorld(e.getEntity())) {
            return;
        }

        Shops.onProjectileHit(e);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (SerbleGen.isInGenWorld(e.getWhoClicked())) {
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

        // Disallow right clicking sweet berry bushes
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.SWEET_BERRY_BUSH) {
            SerbleGen.debugService.debug(e.getPlayer(), "interact");
            e.setCancelled(true);
        }

        // Prevent opening containers
        if (switch (e.getClickedBlock().getType()) {
            case CHEST, TRAPPED_CHEST, BARREL, FURNACE, BLAST_FURNACE, SMOKER, HOPPER,
                    DROPPER, DISPENSER, BREWING_STAND -> true;
            default -> false;
        }) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if (!SerbleGen.isInGenWorld(e.getPlayer())) {
            return;
        }

        ItemStack item = e.getItemDrop().getItemStack();
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE &&
                NbtHandler.itemStackHasTag(item, "no_drop", PersistentDataType.STRING)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (!SerbleGen.isInGenWorld(e.getBlock().getLocation())) {
            return;
        }

        // Prevent crop trampling
        if (e.getBlock().getType() == Material.FARMLAND && e.getTo() == Material.DIRT) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
        if (!SerbleGen.isInFullFeaturedWorld(e.getPlayer())) {
            return;
        }

        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        StartingItems.onPlayerJoin(e);
    }
}
