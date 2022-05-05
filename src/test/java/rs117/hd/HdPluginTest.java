package rs117.hd;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import static rs117.hd.HdPlugin.ENV_SHADER_PATH;
import rs117.hd.scene.lighting.LightManager;
import static rs117.hd.scene.lighting.LightManager.ENV_LIGHTS_CONFIG;
import rs117.hd.utils.Env;
import rs117.hd.utils.FileWatcher;

@SuppressWarnings("unchecked")
public class HdPluginTest
{
	public static void main(String[] args) throws Exception
	{
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
}
