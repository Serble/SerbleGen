package net.serble.SerbleGen;

import net.serble.SerbleGen.Schemas.MonsterLocation;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MonsterResources {
    public static List<MonsterLocation> locations;

    public static void init() {
        double defaultRespawnTime = SerbleGen.plugin.getConfig().getDouble("monster-respawn-time");

        ConfigurationSection configLocations = SerbleGen.plugin.getConfig().getConfigurationSection("monster_gens");

        locations = new ArrayList<>();

        for (String resKey : configLocations.getKeys(false)) {
            ConfigurationSection res = configLocations.getConfigurationSection(resKey);

            MonsterLocation loc = new MonsterLocation();
            World world = Bukkit.getWorld(res.getString("world"));
            SerbleGen.getLocations(world, res, loc);

            // calculate area of poss
            loc.areas = new int[loc.pos1s.length];
            loc.totalArea = 0;

            for (int i = 0; i < loc.pos1s.length; i++) {
                Location pos1 = loc.pos1s[i];
                Location pos2 = loc.pos2s[i];

                int x = pos2.getBlockX() - pos1.getBlockX();
                int y = pos2.getBlockY() - pos1.getBlockY();
                int z = pos2.getBlockZ() - pos1.getBlockZ();
                x = x == 0 ? 1 : x;
                y = y == 0 ? 1 : y;
                z = z == 0 ? 1 : z;

                int area = x * y * z;

                loc.areas[i] = area;
                loc.totalArea += area;
            }

            loc.type = EntityType.valueOf(res.getString("monster"));
            loc.count = res.getInt("count");

            loc.dropItem = Material.getMaterial(res.getString("drop"));

            {
                String dropCountString = res.getString("drop-count", null);
                if (dropCountString != null) {
                    String[] dropCountSplit = dropCountString.split("-");

                    if (dropCountSplit.length == 1) {
                        loc.dropMin = Integer.parseInt(dropCountSplit[0]);
                        loc.dropVariation = 1;
                    } else {
                        loc.dropMin = Integer.parseInt(dropCountSplit[0]);
                        loc.dropVariation = Integer.parseInt(dropCountSplit[1]) - loc.dropMin + 1;
                    }
                } else {
                    loc.dropMin = 1;
                    loc.dropVariation = 1;
                }
            }

            loc.permTag = res.getString("perm");
            if (loc.permTag == null) {
                loc.permTag = "none";
            }

            loc.respawnTime = (long)(res.getDouble("respawn-time", defaultRespawnTime) * 20);

            locations.add(loc);
        }

        SerbleGen.plugin.getServer().getScheduler().runTaskLater(SerbleGen.plugin, MonsterResources::respawnAll, 1);
        SerbleGen.plugin.getServer().getScheduler().runTaskTimer(SerbleGen.plugin, MonsterResources::checkAll, 40, 40);
    }

    public static void spawnMonster(MonsterLocation location) {
        int area = SerbleGen.random.nextInt(location.totalArea);

        int i;
        for (i = 0; area > location.areas[i]; i++) {
            area -= location.areas[i];
        }

        Location pos1 = location.pos1s[i];
        Location pos2 = location.pos2s[i];

        double x = pos1.getX() == pos2.getX() ? pos1.getX() : SerbleGen.random.nextDouble(pos1.getX(), pos2.getX());
        double y = pos1.getY() == pos2.getY() ? pos1.getY() : SerbleGen.random.nextDouble(pos1.getY(), pos2.getY());
        double z = pos1.getZ() == pos2.getZ() ? pos1.getZ() : SerbleGen.random.nextDouble(pos1.getZ(), pos2.getZ());

        World world = pos1.getWorld();
        assert world != null;

        Location spawnLocation = new Location(world, x, y, z);
        if (!spawnLocation.getChunk().isLoaded()) { // I don't think this works
            return;
        }

        Entity entity = world.spawnEntity(spawnLocation, location.type);

        if (entity instanceof Slime) {
            ((Slime) entity).setSize(0);
        }

        location.monsters.add(entity);
    }

    public static void respawnAll() {
        for (MonsterLocation location : locations) {
            for (int i = 0; i < location.count; i++) {
                spawnMonster(location);
            }
        }
    }

    public static void despawnAll() {
        for (MonsterLocation location : locations) {
            for (Entity entity : location.monsters) {
                entity.remove();
            }
        }
    }

    public static void checkAll() {
        // put all locations that need to be respawned in a separate list to avoid ConcurrentModificationException
        List<MonsterLocation> locationsToSpawn = new ArrayList<>();

        for (MonsterLocation location : locations) {
            location.monsters.removeIf(entity -> entity == null || entity.isDead());

            int respawnCount = location.count - (location.monsters.size() + location.monstersCurrentlySpawning);
            for (int i = 0; i < respawnCount; i++) {
                locationsToSpawn.add(location);
            }
        }

        for (MonsterLocation location : locationsToSpawn) {
            spawnMonster(location);
        }
    }

    public static void onMonsterDamageByPlayer(EntityDamageByEntityEvent e) {

    }

    public static void onMonsterDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();

        for (MonsterLocation location : locations) {
            if (location.monsters.contains(entity)) {
                location.monsters.remove(entity);

                location.monstersCurrentlySpawning++;
                SerbleGen.plugin.getServer().getScheduler().runTaskLater(SerbleGen.plugin, () -> {
                    spawnMonster(location);
                    location.monstersCurrentlySpawning--;
                }, location.respawnTime);

                e.setDroppedExp(0);
                e.getDrops().clear();

                Player p = e.getEntity().getKiller();
                if (p == null || p.getGameMode() == GameMode.CREATIVE) {
                    return;
                }

                SerbleGen.addXp(p, 1.0f/18.0f);

                SerbleGen.giveItem(p, e.getEntity().getLocation(), new ItemStack(location.dropItem,
                        SerbleGen.random.nextInt(location.dropVariation) + location.dropMin));
                return;
            }
        }
    }
}
