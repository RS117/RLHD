package rs117.hd.lighting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
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
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import rs117.hd.utils.HDUtils;

@SuppressWarnings("deprecation")
public class ExportLightsToJson
{
	/**
	 * Round floats to an int if they are within this small value of a whole number
	 */
	private static final float eps = .00001f;

	public static void main(String[] args) throws IOException
	{
		OptionParser parser = new OptionParser();
		OptionSpec<?> linearToGammaOption = parser.accepts("linear1-to-gamma255",
			"Convert current light configuration from linear colors " +
			"in the range [0, 1] to gamma colors in the range [0, 255]");
		ArgumentAcceptingOptionSpec<String> configPathOption = parser.accepts("config",
				"Path to lights.json file to read from and write to")
			.withRequiredArg()
			.defaultsTo(Paths.get(
					"src/main/resources",
					Light.class.getPackage().getName().replace(".", "/"),
					"lights.json")
				.toString());
		OptionSpec<?> skipLoadingCurrentConfig = parser.accepts("skip-loading-current-config",
			"Don't load current lights from the JSON config, instead overwrite them");
		OptionSpec<?> convertOldFormats = parser.accepts("convert-old-formats",
			"Load lights from the old formats and convert to JSON format");
		OptionSpec<?> serializeNulls = parser.accepts("serialize-nulls",
			"Include null values when writing to JSON");
		OptionSpec<?> minify = parser.accepts("minify", "Output minified JSON");
		OptionSpec<?> disableValidationOption = parser.accepts("disable-validation", "Skip ");
		OptionSpec<?> dryRun = parser.accepts("dry-run", "Don't write the resulting JSON to file");

		OptionSet options = parser.parse(args);
		Path configPath = Paths.get(options.valueOf(configPathOption));
		boolean enableValidation = !options.has(disableValidationOption);

		Set<Light> uniqueLights = new LinkedHashSet<>();

		if (!options.has(skipLoadingCurrentConfig))
		{
			System.out.println("Loading current lights from JSON...");
			// Load all lights from current lights.json
			Light.THROW_WHEN_PARSING_FAILS = true;
			Light[] currentLights = LightConfig.loadRawLights(new FileInputStream(configPath.toFile()));
			Collections.addAll(uniqueLights, currentLights);
			System.out.println("Loaded " + currentLights.length + " lights");
		}

		if (options.has(convertOldFormats))
		{
			System.out.println("Loading old lights from old formats...");

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
			System.out.println("Loaded " + sceneLights.size() + " lights from old txt format");

		}

		if (options.has(linearToGammaOption))
		{
			enableValidation = false;
			System.out.println("Converting colors from linear color space in the range [0, 1] to gamma [0, 255]");

			for (Light l : uniqueLights)
			{
				for (int i = 0; i < l.color.length; i++)
				{
					l.color[i] = HDUtils.linearToGamma(l.color[i]) * 255f;
					int nearestInt = Math.round(l.color[i]);
					if (Math.abs(nearestInt - l.color[i]) <= eps)
					{
						l.color[i] = nearestInt;
					}
				}
			}
		}

		if (enableValidation)
		{
			for (Light l : uniqueLights)
			{
				// TODO: Potentially allow alpha component if it at some point ends up actually being used
				// TODO: Consider allowing colors outside the normal range, either for darkness lights or HDR

				if (l.color.length > 3)
				{
					System.err.printf("More than 3 colors provided for light '%s'%n", l.description);
				}

				boolean allBelow1 = true;
				boolean invalidRange = false;
				for (float c : l.color)
				{
					// Warn if all values are at or below 1, indicating a possible mistake
					allBelow1 = allBelow1 && c <= 1;
					// Also warn if values lie outside the expected range
					invalidRange = invalidRange || c < 0 || c > 255;
				}

				if (allBelow1)
				{
					System.err.printf("Probable incorrect color range for light: '%s'. " +
						"Should be 0-255. Actual values are all close to zero: %s",
						l.description, Arrays.toString(l.color));
				}

				if (invalidRange)
				{
					System.err.printf("One or more colors in light '%s' lie outside the normal range of 0 to 255: %s%n",
						l.description, Arrays.toString(l.color));
				}
			}
		}

		if (!options.has(dryRun))
		{
			GsonBuilder gsonBuilder = new GsonBuilder()
				.disableHtmlEscaping(); // Allow characters like apostrophes in descriptions

			if (!options.has(minify))
			{
				gsonBuilder.setPrettyPrinting();
			}
			if (options.has(serializeNulls))
			{
				gsonBuilder.serializeNulls();
			}

			// Strip unnecessary decimals
			gsonBuilder.registerTypeAdapter(Float.class, (JsonSerializer<Float>) (src, typeOfSrc, context) ->
			{
				int nearestInt = Math.round(src);
				if (Math.abs(src - nearestInt) <= eps)
				{
					return new JsonPrimitive(nearestInt);
				}
				return new JsonPrimitive(src);
			});

			Gson gson = gsonBuilder.create();

			// Write combined lights.json
			String json = gson.toJson(uniqueLights);

			System.out.println("Writing " + uniqueLights.size() + " lights to JSON file: " + configPath.toAbsolutePath());
			configPath.toFile().getParentFile().mkdirs();

			OutputStreamWriter os = new OutputStreamWriter(
				new FileOutputStream(configPath.toFile()),
				StandardCharsets.UTF_8);

			os.write(json);
			os.close();
		}
	}

	private static HashSet<Integer> toSet(int[] ints)
	{
		return Arrays.stream(ints).boxed().collect(Collectors.toCollection(HashSet::new));
	}
}
