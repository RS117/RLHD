package rs117.hd.environments;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum SkyColor
{

	RFD_QUIZ(Color.BLACK,
		new Rect(new WorldPoint(2579, 4625, 0), new WorldPoint(2579, 4625, 0))
	);

	private final Rect[] rects;
	private float[] color;

	SkyColor(Color color, Rect... rects)
	{
		this.rects = rects;
		this.color = new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f};
	}

	public static HashMap<Rect[], float[]> skyColors = new HashMap<>();

	public static float[] validArea(WorldPoint point)
	{

		for (Map.Entry<Rect[], float[]> entry : skyColors.entrySet())
		{

			for (Rect rect : entry.getKey())
			{
				if (rect.containsPoint(point.getX(), point.getY(), point.getPlane()))
				{
					return entry.getValue();
				}
			}
		}

		return null;
	}


	static
	{
		for (SkyColor sky : SkyColor.values())
		{
			skyColors.put(sky.getRects(), sky.getColor());
		}
	}

}
