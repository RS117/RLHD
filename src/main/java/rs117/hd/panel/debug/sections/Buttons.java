package rs117.hd.panel.debug.sections;

import java.awt.GridLayout;
import javax.swing.JPanel;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import rs117.hd.HdPlugin;
import rs117.hd.panel.debug.buttons.FpsInspector;
import rs117.hd.panel.debug.buttons.MemoryInspector;
import rs117.hd.panel.components.ToggleButton;

public class Buttons extends JPanel
{

	@Getter
	private ToggleButton lightInfo;
	private ToggleButton memoryInspector;
	private ToggleButton fpsInspector;
	private ToggleButton points;
	private ToggleButton shadowMap;

	public Buttons(HdPlugin plugin) {

		points = new ToggleButton("Light Points", "Show Light points as tiles");
		shadowMap = new ToggleButton("Shadow Map", "Show Current Shadow Map");
		lightInfo = new ToggleButton("Light Info", "Show Light Information");
		memoryInspector = new ToggleButton("Memory Inspector", "Memory Inspector");
		fpsInspector = new ToggleButton("Fps Inspector", "Fps Inspector");

		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new GridLayout(0, 2, 1, 2));

		memoryInspector.addFrame(new MemoryInspector(plugin.getClient()));
		fpsInspector.addFrame(new FpsInspector(plugin.getClient()));

		add(points);
		add(shadowMap);
		add(lightInfo);
		add(memoryInspector);
		add(fpsInspector);

	}

}
