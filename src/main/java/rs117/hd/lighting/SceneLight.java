package rs117.hd.lighting;

import java.util.Random;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.TileObject;

public class SceneLight extends Light
{

	private static final Random randomizer = new Random();

	public final int randomOffset = randomizer.nextInt();

	public int currentSize;
	public float currentStrength;
	public float[] currentColor;
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

	public SceneLight(Light l)
	{
		this(l.description, l.worldX, l.worldY, l.plane, l.height, l.alignment, l.radius,
			l.strength, l.color, l.type, l.duration, l.range, l.fadeInDuration);
	}

	public SceneLight(int worldX, int worldY, int plane, int height, Alignment alignment, int radius, float strength, float[] color, LightType type, float duration, float range, int fadeInDuration)
	{
		this(null, worldX, worldY, plane, height, alignment, radius,
			strength, color, type, duration, range, fadeInDuration);
	}

	public SceneLight(String description, int worldX, int worldY, int plane, int height, Alignment alignment, int radius, float strength, float[] color, LightType type, float duration, float range, int fadeInDuration)
	{
		super(description, worldX, worldY, plane, height, alignment, radius,
			strength, color, type, duration, range, fadeInDuration,
			null, null, null);

		this.currentSize = radius;
		this.currentStrength = strength;
		this.currentColor = color;

		if (type == LightType.PULSE)
		{
			this.currentAnimation = (float) Math.random();
		}
	}
}
