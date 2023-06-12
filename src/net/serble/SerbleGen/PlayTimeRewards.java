package net.serble.SerbleGen;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayTimeRewards implements Listener {

    private final Map<UUID, Long> lastBlockBreak;

    public PlayTimeRewards() {
        SerbleGen.plugin.getServer().getScheduler().runTaskTimer(SerbleGen.plugin, new GivePlaytimeExperienceTask(), 0, 20 * 60 * 5);
        lastBlockBreak = new HashMap<>();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        lastBlockBreak.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    private class GivePlaytimeExperienceTask implements Runnable {
        @Override
        public void run() {
            for (String world : SerbleGen.genWorlds) {
                for (Player player : Objects.requireNonNull(Bukkit.getWorld(world)).getPlayers()) {
                    if (isMining(player)) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sysgivexp " + player.getName() + " 10 Playtime");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sysgivemoney " + player.getName() + " 15 Playtime");
                    }
                }
            }
        }

        private boolean isMining(Player player) {
            Long lastBreakTime = lastBlockBreak.get(player.getUniqueId());
            if (lastBreakTime != null) {
                return (System.currentTimeMillis() - lastBreakTime) <= (5 * 60 * 1000);
            }
            return false;
        }

    }

}
