package rs117.hd.lighting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ExportLightsToJson
{
	public static void main(String[] args) throws IOException
	{
		final Path outputPath = Paths.get(
			"src/main/resources",
			rs117.hd.lighting.Light.class.getPackage().getName().replace(".", "/"),
			"lights.json");

		Set<Light> uniqueLights = new LinkedHashSet<>();

		// Load all lights from current lights.json
		Light.THROW_WHEN_PARSING_FAILS = true;
		Light[] currentLights = LightConfig.loadRawLights(new FileInputStream(outputPath.toFile()));
		Collections.addAll(uniqueLights, currentLights);

		Gson gson = new GsonBuilder()
//			.serializeNulls()
			.setPrettyPrinting()
			.create();

		ArrayList<Light> sceneLights = LightConfigParser.loadLightsFromFile(true);
		uniqueLights.addAll(sceneLights.stream()
			.map(l -> new Light(
				l.description,
				l.worldX, l.worldY, l.plane, l.height,
				l.alignment,
				l.radius,
				l.strength,
				l.color,
				l.type,
				l.duration,
				l.range,
				l.fadeInDuration,
				null, null, null))
			.collect(Collectors.toList()));

		uniqueLights.addAll(Arrays.stream(NpcLight.values())
			.map(l -> new Light(
				l.name(),
				null, null, null, l.getHeight(),
				l.getAlignment(),
				l.getSize(),
				l.getStrength(),
				l.getColor(),
				l.getLightType(),
				l.getDuration(),
				l.getRange(),
				null,
				toSet(l.getId()),
				null,
				null))
			.collect(Collectors.toList()));

		uniqueLights.addAll(Arrays.stream(ObjectLight.values())
			.map(l -> new Light(
				l.name(),
				null, null, null, l.getHeight(),
				l.getAlignment(),
				l.getSize(),
				l.getStrength(),
				l.getColor(),
				l.getLightType(),
				l.getDuration(),
				l.getRange(),
				null,
				null,
				toSet(l.getId()),
				null))
			.collect(Collectors.toList()));

		uniqueLights.addAll(Arrays.stream(ProjectileLight.values())
			.map(l -> new Light(
				l.name(),
				null, null, null, null,
				null,
				l.getSize(),
				l.getStrength(),
				l.getColor(),
				l.getLightType(),
				l.getDuration(),
				l.getRange(),
				l.getFadeInDuration(),
				null,
				null,
				toSet(l.getId())))
			.collect(Collectors.toList()));

		// Write combined lights.json
		String json = gson.toJson(uniqueLights);

		System.out.println("Writing config for " + uniqueLights.size() + " lights to " + outputPath.toAbsolutePath());
		outputPath.toFile().getParentFile().mkdirs();

		OutputStreamWriter os = new OutputStreamWriter(
			new FileOutputStream(outputPath.toFile()),
			StandardCharsets.UTF_8);

		os.write(json);
		os.close();
	}

	private static HashSet<Integer> toSet(int[] ints)
	{
		return Arrays.stream(ints).boxed().collect(Collectors.toCollection(HashSet::new));
	}
}
