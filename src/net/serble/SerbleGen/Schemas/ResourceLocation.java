package net.serble.SerbleGen.Schemas;

import org.bukkit.Location;
import org.bukkit.Material;

public class ResourceLocation {
    public Location[] pos1s;
    public Location[] pos2s;
    public Material blockType;
    public Material dropItem;
    public int dropMin;
    public int dropVariation;
    public int breakCount;
    public long respawnTime;
    public String permTag;
    public ToolType toolType;
}
