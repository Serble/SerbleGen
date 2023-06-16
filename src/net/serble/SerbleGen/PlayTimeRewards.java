package net.serble.SerbleGen;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayTimeRewards implements Listener {
    private static final Map<UUID, PlayerPointCounter> playerPointCounters = new HashMap<>();

    public static void onBlockBreak(BlockBreakEvent e) {
        UUID pUid = e.getPlayer().getUniqueId();

        if (playerPointCounters.containsKey(pUid)) {
            playerPointCounters.get(pUid).addPoint();
        }
        else {
            PlayerPointCounter ppc = new PlayerPointCounter();
            ppc.player = e.getPlayer();
            ppc.lastBreak = System.currentTimeMillis();
            ppc.points = 1;

            playerPointCounters.put(pUid, ppc);
        }
    }

    private static class PlayerPointCounter {
        public long lastBreak;
        public int points;
        public Player player;

        public void addPoint() {
            long now = System.currentTimeMillis();

            if (now - lastBreak < 5000) {
                return;
            }

            lastBreak = now;
            points++;

            if (points > 60) {
                points = 0;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sysgivexp " + player.getName() + " 10 Playtime");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sysgivemoney " + player.getName() + " 15 Playtime");
            }
        }
    }
}
