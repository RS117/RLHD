package rs117.hd;

import java.nio.file.Paths;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import rs117.hd.lighting.LightManager;
import static rs117.hd.lighting.LightManager.LIGHTS_CONFIG_ENV;
import rs117.hd.utils.Env;

public class HdPluginTest
{
	public static void main(String[] args) throws Exception
	{
		if (Env.missing(LIGHTS_CONFIG_ENV))
		{
			Env.set(LIGHTS_CONFIG_ENV, Paths
				.get("src/main/resources",
					LightManager.class.getPackage().getName().replace(".", "/"),
					"lights.json")
				.toString());
		}

		ExternalPluginManager.loadBuiltin(HdPlugin.class);
		RuneLite.main(args);
	}
}
