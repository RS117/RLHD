package rs117.hd.lighting.data;

import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.TileObject;
import rs117.hd.lighting.LightLocation;

public class Light
{
    public int worldX;
    public int worldY;
    public int plane;
    public int height;
    public Alignment alignment;
    public int size;
    public float strength;
    public int[] color;
    public LightType type;
    public float duration;
    public float range;
    public int fadeInDuration = 0;

    public int currentSize;
    public float currentStrength;
    public int[] currentColor;
    public float currentAnimation = 0.5f;
    public int currentFadeIn = 0;
    public boolean visible = true;

    public int x;
    public int y;
    public int z;
    public int distance = 0;
    public boolean belowFloor = false;
    public boolean aboveFloor = false;

    public Projectile projectile = null;
    public NPC npc = null;
    public TileObject object = null;

    public Light(int worldX, int worldY, int plane, int height, Alignment alignment, int size, float strength, int[] color, LightType type, float duration, float range, int fadeInDuration)
    {
        this.worldX = worldX;
        this.worldY = worldY;
        this.plane = plane;
        this.height = height;
        this.alignment = alignment;
        this.size = size;
        this.strength = strength;
        this.color = color;
        this.type = type;
        this.duration = duration;
        this.range = range;
        this.fadeInDuration = fadeInDuration;

        this.currentSize = size;
        this.currentStrength = strength;
        this.currentColor = color;

        if (type == LightType.PULSE)
        {
            this.currentAnimation = (float)Math.random();
        }
    }

    public Light(LightLocation location, int height, int size, float strength, int[] color, LightType type, float duration, float range, int fadeInDuration)
    {
        this.worldX = location.getX();
        this.worldY = location.getY();
        this.plane = location.getPlane();
        this.height = height;
        this.alignment = location.getAlignment();
        this.size = size;
        this.strength = strength;
        this.color = color;
        this.type = type;
        this.duration = duration;
        this.range = range;
        this.fadeInDuration = fadeInDuration;

        this.currentSize = size;
        this.currentStrength = strength;
        this.currentColor = color;

        if (type == LightType.PULSE)
        {
            this.currentAnimation = (float)Math.random();
        }
    }

}