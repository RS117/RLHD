package rs117.hd.panel.debug.sections;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.RuneLite;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.util.LinkBrowser;
import rs117.hd.HdPlugin;
import rs117.hd.Utils;
import rs117.hd.panel.components.ToggleButton;

public class Specifications extends JPanel
{

	private static String specs;

	public Specifications(HdPlugin plugin) {

		setBorder(BorderFactory.createEmptyBorder(0, 7, 7, 7));
		setLayout(new DynamicGridLayout(0, 1, 0, 5));
		setAlignmentX(Component.LEFT_ALIGNMENT);

		ToggleButton copy = new ToggleButton("Copy Specifications", "Copy Specifications to clipboard", e -> {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(Utils.stripHtml(specs)), null);
		});

		ToggleButton open = new ToggleButton("Open Logs", "Open Log Folders", e -> {
			LinkBrowser.open(RuneLite.LOGS_DIR.toString());
		});
		ToggleButton latest = new ToggleButton("Open Latest Logs", "Open Latest Logs", e -> {
			LinkBrowser.open(new File(RuneLite.LOGS_DIR, "client.log").toString());
		});

		JPanel versionPanel = new JPanel();
		versionPanel.setLayout(new GridLayout(0, 1, 0, 10));

		String[] spec = {
			join("Runelite Version: ", plugin.getRuneliteVersion()),
			join("Plugin Version: ", plugin.getPluginVersion()),
			join("OpenGL Version: ", plugin.getGlContext().getGLVersion()),
			join("Renderer: ", plugin.getRenderer()),
			join("Version: ", plugin.getVersion()),
			join("Client Architecture: ", System.getProperty("sun.arch.data.model")),
			join("System Architecture: ", System.getProperty("os.arch")),
			join("Operating System: ", System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")"),
			join("Processor: ", System.getenv("PROCESSOR_IDENTIFIER")),
			join("Free memory (bytes): ", "" + Utils.bytesToMb(Runtime.getRuntime().freeMemory())),
			join("Maximum memory: ", "" + Utils.bytesToMb(Runtime.getRuntime().maxMemory()))
		};

		specs = setSpecText(spec);

		versionPanel.add(new JLabel(specs));
		add(versionPanel);
		add(copy);
		add(open);
		add(latest);

	}

	public String setSpecText(String[] text)
	{

		StringBuilder temp = new StringBuilder("<html>");

		for (int index = 0; index <= text.length - 1; index++)
		{
			String content = text[index] + "<br>";
			temp.append(content);
		}

		temp.append("</html>");
		return temp.toString();
	}

	private static String join(String key, String value)
	{
		return "<font color='#a5a5a5'>" + key + "</font><font = color='white'>" + value + "</font>";
	}


}
