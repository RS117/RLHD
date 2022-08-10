package rs117.hd.utils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Keybind;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.ui.overlay.OverlayManager;
import rs117.hd.HdPlugin;
import rs117.hd.HdPluginConfig;
import rs117.hd.opengl.shader.Template;
import rs117.hd.overlays.TileInfoOverlay;

@Slf4j
public class DeveloperTools implements KeyListener
{
	public static final String ENV_SHADER_PATH = "RLHD_SHADER_PATH";

	// This could be part of the config if we had developer mode config sections
	private static final Keybind KEY_TOGGLE_TILE_INFO = new Keybind(KeyEvent.VK_F3, InputEvent.CTRL_DOWN_MASK);

	@Inject
	private HdPluginConfig config;

	@Inject
	private HdPlugin plugin;

	@Inject
	private KeyManager keyManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TileInfoOverlay tileInfoOverlay;

	private Path shaderPath;
	private FileWatcher shaderSourceWatcher;
	private boolean tileInfoOverlayEnabled = false;

	public void activate() {
		keyManager.registerKeyListener(this);
		if (tileInfoOverlayEnabled)
		{
			overlayManager.add(tileInfoOverlay);
		}

		shaderPath = Env.getPath(ENV_SHADER_PATH);
		if (shaderPath != null)
		{
			try
			{
				shaderSourceWatcher = new FileWatcher(shaderPath, path -> {
					if (path.getFileName().toString().endsWith(".glsl")) {
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

		keyManager.unregisterKeyListener(this);
		overlayManager.remove(tileInfoOverlay);
	}

	public String shaderResolver(String path) {
		if (shaderPath == null)
			return null;

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

	@Override
	public void keyPressed(KeyEvent event)
	{
		if (KEY_TOGGLE_TILE_INFO.matches(event))
		{
			event.consume();
			tileInfoOverlayEnabled = !tileInfoOverlayEnabled;
			if (tileInfoOverlayEnabled)
			{
				overlayManager.add(tileInfoOverlay);
			}
			else
			{
				overlayManager.remove(tileInfoOverlay);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent event)
	{

	}

	@Override
	public void keyTyped(KeyEvent event)
	{

	}
}
