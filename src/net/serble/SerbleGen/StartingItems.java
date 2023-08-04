package net.serble.SerbleGen;

import net.serble.SerbleGen.Util.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StartingItems {
    private static List<UUID> players = new ArrayList<>();
    private static List<ItemStack> items = new ArrayList<>();

    public static void init() {
        FileConfiguration joinedPlayers = ConfigManager.load("joined_players.yml");
        if (joinedPlayers != null) {
            List<String> playersS = joinedPlayers.getStringList("players");
            for (String player : playersS) {
                players.add(UUID.fromString(player));
            }
        }

        List<?> itemRaw = SerbleGen.plugin.getConfig().getList("starting-items");
        for (Object item : itemRaw) {
            items.add((ItemStack) item);
        }
    }

    private static void save() {
        FileConfiguration joinedPlayers = ConfigManager.load("joined_players.yml");
        if (joinedPlayers == null) {
            joinedPlayers = new YamlConfiguration();
        }

        List<String> playersS = new ArrayList<>();
        for (UUID player : players) {
            playersS.add(player.toString());
        }

        joinedPlayers.set("players", playersS);
        ConfigManager.save("joined_players.yml", joinedPlayers);
    }

    public static void onPlayerJoin(PlayerChangedWorldEvent e) {
        UUID uuid = SerbleGen.idService.getPlayerUuid(e.getPlayer());

        if (players.contains(uuid)) {
            return;
        }

        players.add(uuid);
        save();

        for (ItemStack item : items) {
            e.getPlayer().getInventory().addItem(item.clone());
        }
    }
}
