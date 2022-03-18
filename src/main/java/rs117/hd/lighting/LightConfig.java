package rs117.hd.lighting;

import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import rs117.hd.HDUtils;

@Slf4j
public class LightConfig
{
	public static void load(
		ArrayList<SceneLight> worldLights,
		ListMultimap<Integer, Light> npcLights,
		ListMultimap<Integer, Light> objectLights,
		ListMultimap<Integer, Light> projectileLights
	)
	{
		String filename = "lights.json";
		InputStream is = LightConfig.class.getResourceAsStream(filename);
		if (is == null)
		{
			throw new RuntimeException("Missing resource: " + Paths.get(
				LightConfig.class.getPackage().getName().replace(".", "/"), filename));
		}
		load(is, worldLights, npcLights, objectLights, projectileLights);
	}

	public static void load(
		File jsonFile,
		ArrayList<SceneLight> worldLights,
		ListMultimap<Integer, Light> npcLights,
		ListMultimap<Integer, Light> objectLights,
		ListMultimap<Integer, Light> projectileLights
	)
	{
		try
		{
			load(new FileInputStream(jsonFile), worldLights, npcLights, objectLights, projectileLights);
		}
		catch (IOException ex)
		{
			log.error("Lights config file not found: " + jsonFile.toPath() + ". Falling back to default config...", ex);
			load(worldLights, npcLights, objectLights, projectileLights);
		}
	}

	public static void load(
		InputStream jsonInputStream,
		ArrayList<SceneLight> worldLights,
		ListMultimap<Integer, Light> npcLights,
		ListMultimap<Integer, Light> objectLights,
		ListMultimap<Integer, Light> projectileLights
	)
	{
		try
		{
			Light[] lights = loadRawLights(jsonInputStream);

			for (Light l : lights)
			{
				// Map values from [0, 255] in gamma color space to [0, 1] in linear color space
				// Also ensure that each color always has 4 components with sensible defaults
				float[] linearRGBA = { 0, 0, 0, 1 };
				for (int i = 0; i < Math.min(l.color.length, linearRGBA.length); i++)
				{
					linearRGBA[i] = HDUtils.gammaToLinear(l.color[i] /= 255f);
				}
				l.color = linearRGBA;

				if (l.worldX != null && l.worldY != null)
				{
					worldLights.add(new SceneLight(l));
				}
				l.npcIds.forEach(id -> npcLights.put(id, l));
				l.objectIds.forEach(id -> objectLights.put(id, l));
				l.projectileIds.forEach(id -> projectileLights.put(id, l));
			}

			log.info("Loaded {} lights", lights.length);
		}
		catch (Exception ex)
		{
			log.error("Failed to parse light configuration", ex);
		}
	}

	public static Light[] loadRawLights(InputStream is)
	{
		Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

		Gson gson = new GsonBuilder()
			.setLenient()
			.create();

		return gson.fromJson(reader, Light[].class);
	}
}
