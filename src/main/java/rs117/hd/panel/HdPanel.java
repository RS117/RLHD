package rs117.hd.panel;

import com.google.inject.Inject;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import rs117.hd.panel.debug.Debug;

public class HdPanel extends PluginPanel
{

	private final BufferedImage DISCORD_ICON;
	private final BufferedImage GITHUB_ICON;
	private final BufferedImage PATREON_ICON;
	private final BufferedImage TWITTER_ICON;

	// this panel will hold either the ge search panel or the ge offers panel
	private final JPanel display = new JPanel();

	private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);
	private MaterialTab preset;
	private MaterialTab debug;

	@Getter
	private final Preset presetPanel;
	@Getter
	private final Debug debugPanel;

	@Inject
	private HdPanel(Preset presetPanel, Debug debugPanel)
	{
		super(false);

		DISCORD_ICON = ImageUtil.resizeImage(ImageUtil.loadImageResource(getClass(), "discord.png"), 16, 16);
		GITHUB_ICON = ImageUtil.resizeImage(ImageUtil.loadImageResource(getClass(), "github.png"), 16, 16);
		PATREON_ICON = ImageUtil.resizeImage(ImageUtil.loadImageResource(getClass(), "patreon.png"), 16, 16);
		TWITTER_ICON = ImageUtil.resizeImage(ImageUtil.loadImageResource(getClass(), "twitter.png"), 16, 16);

		this.presetPanel = presetPanel;
		this.debugPanel = debugPanel;

		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		add(setupHeader(), BorderLayout.NORTH);
		add(setupTabs(), BorderLayout.CENTER);

	}

	private JPanel setupTabs()
	{


		preset = new MaterialTab("Presets", tabGroup, presetPanel);
		debug = new MaterialTab("Debug", tabGroup, debugPanel);

		JPanel container = new JPanel();
		container.setBorder(new EmptyBorder(10, 0, 5, 0));
		container.setLayout(new BorderLayout());

		tabGroup.addTab(preset);
		tabGroup.addTab(debug);

		tabGroup.select(preset);

		container.add(tabGroup, BorderLayout.NORTH);
		container.add(display, BorderLayout.CENTER);

		return container;
	}

	private JPanel setupHeader()
	{

		JPanel container = new JPanel();
		container.setBorder(new EmptyBorder(10, 10, 2, 10));
		container.setLayout(new BorderLayout());

		JLabel title = new JLabel();
		title.setText("117 HD");
		title.setForeground(Color.WHITE);
		container.add(title, BorderLayout.WEST);

		final JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 0));
		buttons.setBackground(ColorScheme.DARK_GRAY_COLOR);

		buttons.add(titleButton(DISCORD_ICON, "Get help or make suggestions", "https://discord.gg/U4p6ChjgSE"));
		buttons.add(titleButton(GITHUB_ICON, "Report issues or contribute on GitHub", "https://github.com/RS117/RLHD"));
		buttons.add(titleButton(TWITTER_ICON, "View updates on Twitter", "https://twitter.com/117scape"));
		buttons.add(titleButton(PATREON_ICON, "Support development on Patreon", "https://www.patreon.com/RS_117"));

		container.add(buttons, BorderLayout.EAST);
		return container;
	}

	public JButton titleButton(BufferedImage image, String tooltip, String link)
	{
		JButton button = new JButton();
		SwingUtil.removeButtonDecorations(button);
		button.setIcon(new ImageIcon(image));
		button.setToolTipText(tooltip);
		button.setBackground(ColorScheme.DARK_GRAY_COLOR);
		button.setUI(new BasicButtonUI());
		button.addActionListener((ev) -> LinkBrowser.browse(link));
		return button;
	}

}