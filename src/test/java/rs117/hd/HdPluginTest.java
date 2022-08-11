package rs117.hd;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.externalplugins.ExternalPluginManager;
import rs117.hd.scene.lighting.LightManager;
import rs117.hd.utils.Env;
import rs117.hd.utils.FileWatcher;

import java.io.InputStream;
import java.util.Properties;

import static rs117.hd.scene.lighting.LightManager.ENV_LIGHTS_CONFIG;
import static rs117.hd.utils.DeveloperTools.ENV_SHADER_PATH;

@SuppressWarnings("unchecked")
@Slf4j
public class HdPluginTest
{
	public static void main(String[] args) throws Exception
	{
		useLatestPluginHub();

		if (Env.missing(ENV_LIGHTS_CONFIG))
		{
			Env.set(ENV_LIGHTS_CONFIG, FileWatcher.getResourcePath(LightManager.class).resolve("lights.json"));
		}
		if (Env.missing(ENV_SHADER_PATH))
		{
			Env.set(ENV_SHADER_PATH, FileWatcher.getResourcePath(HdPlugin.class));
		}

		ExternalPluginManager.loadBuiltin(HdPlugin.class);
		RuneLite.main(args);
	}

	private static void useLatestPluginHub()
	{
		if (System.getProperty("runelite.pluginhub.version") == null)
		{
			try
			{
				Properties props = new Properties();
				try (InputStream in = RuneLiteProperties.class.getResourceAsStream("runelite.properties"))
				{
					props.load(in);
				}

				String version = props.getProperty("runelite.pluginhub.version");
				String[] parts = version.split("[.-]");
				if (parts.length > 3 && parts[3].equals("SNAPSHOT"))
				{
					int patch = Integer.parseInt(parts[2]) - 1;
					version = parts[0] + "." + parts[1] + "." + patch;
					log.info("Detected SNAPSHOT version with no manually specified plugin-hub version. " +
							"Setting runelite.pluginhub.version to {}", version);
					System.setProperty("runelite.pluginhub.version", version);
				}
			}
			catch (Exception ex)
			{
				log.error("Failed to automatically use latest plugin-hub version", ex);
			}
		}
	}
}
