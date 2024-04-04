package net.serble.SerbleGen.Schemas;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class MonsterLocation extends ResourceLocation {
    public EntityType type;
    public int count;


    // runtime variables
    public List<Entity> monsters = new ArrayList<>();
    public int monstersCurrentlySpawning = 0;
    public int[] areas;
    public int totalArea;
}
