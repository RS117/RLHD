package rs117.hd.utils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Keybind;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import rs117.hd.HdPlugin;
import rs117.hd.HdPluginConfig;
import rs117.hd.opengl.shader.Template;

import javax.inject.Inject;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class DeveloperTools
{
	public static final String ENV_SHADER_PATH = "RLHD_SHADER_PATH";

	@Inject
	private HdPluginConfig config;

	@Inject
	private HdPlugin plugin;

	private Path shaderPath;
	private FileWatcher shaderSourceWatcher;

	public void activate() {

		shaderPath = Env.getPath(ENV_SHADER_PATH);
		if (shaderPath != null)
		{
			try
			{
				shaderSourceWatcher = new FileWatcher()
					.watchPath(shaderPath)
					.addChangeHandler(path ->
					{
						if (path.getFileName().toString().endsWith(".glsl"))
						{
							log.info("Reloading shaders...");
							plugin.recompilePrograms();
						}
					});
			}
			catch (IOException ex)
			{
				throw new RuntimeException(ex);
			}
		}
	}

	public void deactivate() {
		if (shaderSourceWatcher != null)
		{
			shaderSourceWatcher.close();
			shaderSourceWatcher = null;
		}

	}

	public String shaderResolver(String path) {
		Path fullPath = shaderPath.resolve(path);
		try
		{
			log.debug("Loading shader from file: {}", fullPath);
			return Template.inputStreamToString(new FileInputStream(fullPath.toFile()));
		}
		catch (FileNotFoundException ex)
		{
			throw new RuntimeException("Failed to load shader from file: " + fullPath, ex);
		}
	}

}
