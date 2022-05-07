package rs117.hd;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import static rs117.hd.HdPlugin.SHADER_PATH;
import rs117.hd.scene.lighting.LightManager;
import static rs117.hd.scene.lighting.LightManager.LIGHTS_CONFIG_ENV;
import rs117.hd.utils.Env;
import rs117.hd.utils.FileWatcher;

public class HdPluginTest
{
	public static void main(String[] args) throws Exception
	{
		if (Env.missing(LIGHTS_CONFIG_ENV))
		{
			Env.set(LIGHTS_CONFIG_ENV, FileWatcher.getResourcePath(LightManager.class).resolve("lights.json"));
		}
		if (Env.missing(SHADER_PATH))
		{
			Env.set(SHADER_PATH, FileWatcher.getResourcePath(HdPlugin.class));
		}

		ExternalPluginManager.loadBuiltin(HdPlugin.class);
		RuneLite.main(args);
	}
}
