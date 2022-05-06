package rs117.hd.lighting;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import rs117.hd.scene.lighting.Alignment;
import rs117.hd.scene.lighting.Light;
import rs117.hd.scene.lighting.LightType;

@Slf4j
public class LightConfigParser
{
	private static final Pattern PATTERN = Pattern.compile("^[ \\t]*(?<expr>" +
		"//.*$|" + // //comment
		"/\\*.*$|" + // /* start comment block
		"\\*/.*$|" + //    end comment block */
		"Reset|" + // sets all variables to defaults
		"(?<x>[0-9-]+)(,)[ \\t]*(?<y>[0-9-]+)((,)[ \\t]*(?<alignment>[A-Za-z]+))?|" + // 3124, 2843
		"#([ \\t]*(?<color>[0-9a-fA-F]{6}|[0-9a-fA-F]{3}))|" + // #<RRGGBB> or #<RGB> (hex color)
		"Color[ \\t]*(?<r>[0-9-]+)(,)[ \\t]*(?<g>[0-9-]+)(,)[ \\t]*(?<b>[0-9-]+)|" + // C 255, 128, 0 (RGB color)
		"Strength[ \\t]*(?<strength>[0-9-]+)|" + // S 100 (strength)
		"Radius[ \\t]*(?<radius>[0-9-]+)|" + // R 500 (radius)
		"Range[ \\t]*(?<range>[0-9-]+)|" + // R 500 (radius)
		"Duration[ \\t]*(?<duration>[0-9-]+)|" + // R 500 (radius)
		"Plane[ \\t]*(?<plane>[0-9-]+)|" + // P 0 (plane)
		"Height[ \\t]*(?<h>[0-9-]+)|" + // H 128 (height)
		"Type[ \\t]*(?<type>[a-z]+)|" + // T flicker (type)
		")[ \\t]*");

	public static ArrayList<Light> loadLightsFromFile() throws IOException
	{
		return loadLightsFromFile(false);
	}

	public static ArrayList<Light> loadLightsFromFile(boolean parseDescription) throws IOException
	{
		// create arraylist of lights from text file
		ArrayList<Light> lights = new ArrayList<>();

		String filename = "lights.txt";
		boolean commentBlock = false;

		float[] defaultColor = new float[]{1.0f, 1.0f, 1.0f};
		int defaultRadius = 500;
		float defaultStrength = 1.0f;
		float defaultRange =  0.2f;
		int defaultDuration = 1000;
		int defaultHeight = 0;
		int defaultPlane = 0;
		LightType defaultType = LightType.STATIC;

		float[] color = defaultColor;
		int radius = defaultRadius;
		float strength = defaultStrength;
		float range =  defaultRange;
		int duration = defaultDuration;
		int height = defaultHeight;
		int plane = defaultPlane;
		LightType type = defaultType;

		int lineNo = 1;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(LightConfigParser.class.getResourceAsStream(filename))))
		{
			Matcher m = PATTERN.matcher("");

			String description = null;
			boolean readingComment = true;
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.trim().length() == 0)
				{
					description = null;
					readingComment = true;
				}

				m.reset(line);
				int end = 0;
				while (end < line.length())
				{
					m.region(end, line.length());
					if (!m.find())
					{
						throw new IllegalArgumentException("Unexpected: \"" + line.substring(end) + "\" (" + filename + ":" + lineNo + ")");
					}
					end = m.end();

					String expr = m.group("expr");
					if (expr == null || expr.length() <= 0)
					{
						continue;
					}

					if (expr.startsWith("//"))
					{
						if (readingComment)
						{
							String comment = expr.substring(2).trim();
							if (description == null)
							{
								description = comment;
							}
							else
							{
								description += "\n" + comment;
							}
						}
						continue;
					}

					if (expr.startsWith("/*")) {
						commentBlock = true;
						continue;
					} else if (expr.startsWith("*/")) {
						commentBlock = false;
						continue;
					}

					if (commentBlock) {
						continue;
					}

					readingComment = false;

					if (expr.toLowerCase().startsWith("reset"))
					{
						color = defaultColor;
						radius = defaultRadius;
						range = defaultRange;
						duration = defaultDuration;
						strength = defaultStrength;
						height = defaultHeight;
						plane = defaultPlane;
						type = defaultType;
						continue;
					}

					char cha = expr.toLowerCase().charAt(0);
					switch (cha)
					{
						case '#':
							String sColor = m.group("color");
							Color RGB = Color.decode("#" + sColor);
							float[] RGBTmp = new float[3];
							RGB.getRGBColorComponents(RGBTmp);
							color = RGBTmp.clone();
							break;
						case 'c':
							int r = Integer.parseInt(m.group("r"));
							int g = Integer.parseInt(m.group("g"));
							int b = Integer.parseInt(m.group("b"));
							color = new float[]{r / 255f, g / 255f, b / 255f};
							break;
						case 's':
							strength = Integer.parseInt(m.group("strength")) / 100f;
							break;
						case 'r':
							if (expr.toLowerCase().startsWith("radius")) {
								radius = Integer.parseInt(m.group("radius"));
								break;
							} else if (expr.toLowerCase().startsWith("range")) {
								range = Integer.parseInt(m.group("range"));
								break;
							}
						case 'd':
							duration = Integer.parseInt(m.group("duration"));
							break;
						case 'p':
							plane = Integer.parseInt(m.group("plane"));
							break;
						case 'h':
							height = Integer.parseInt(m.group("h"));
							break;
						case 't':
							String typeStr = m.group("type").toLowerCase().trim();
							switch (typeStr) {
								case "flicker":
									type = LightType.FLICKER;
									break;
								case "pulse":
									type = LightType.PULSE;
									break;
								default:
									type = LightType.STATIC;
									break;
							}
							break;
						default:
							int x = Integer.parseInt(m.group("x"));
							int y = Integer.parseInt(m.group("y"));
							Alignment alignment = Alignment.CENTER;
							if (m.group("alignment") != null) {
								switch (m.group("alignment").toLowerCase().trim()) {
									case "n":
										alignment = Alignment.NORTH;
										break;
									case "ne":
										alignment = Alignment.NORTHEAST;
										break;
									case "e":
										alignment = Alignment.EAST;
										break;
									case "se":
										alignment = Alignment.SOUTHEAST;
										break;
									case "s":
										alignment = Alignment.SOUTH;
										break;
									case "sw":
										alignment = Alignment.SOUTHWEST;
										break;
									case "w":
										alignment = Alignment.WEST;
										break;
									case "nw":
										alignment = Alignment.NORTHWEST;
										break;
									default:
										alignment = Alignment.CENTER;
										break;
								}
							}
							lights.add(new Light(description,
								x, y, plane, height, alignment, radius,
								strength, color, type, duration, range, 0,
								null, null, null));
							break;
					}
				}
			}
		}
		catch (NumberFormatException ex)
		{
			throw new IllegalArgumentException("Expected number (" + filename + ":" + lineNo + ")", ex);
		}

		log.debug("loaded {} lights from file", lights.size());
		return lights;
	}
}
