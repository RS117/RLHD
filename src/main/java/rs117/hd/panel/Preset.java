package rs117.hd.panel;

import com.google.inject.Inject;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.PluginErrorPanel;
import rs117.hd.HdPlugin;

import javax.swing.*;

public class Preset extends JPanel
{

	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	private final HdPlugin plugin;

	@Inject
	private Preset(HdPlugin plugin)
	{
		super();
		this.plugin = plugin;

		setBackground(ColorScheme.DARK_GRAY_COLOR);

		errorPanel.setContent("Coming Soon", "Preset Panel Coming Soon.");
		add(errorPanel);

	}


}
