package rs117.hd.lighting;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Light
{
	public String description;
	public Integer worldX, worldY, plane, height;
	public Alignment alignment;
	public int radius;
	public float strength;
	public float[] color;
	public LightType type;
	public float duration;
	public float range;
	public Integer fadeInDuration;
	public int[] npcIds;
	public int[] objectIds;
	public int[] projectileIds;
}
