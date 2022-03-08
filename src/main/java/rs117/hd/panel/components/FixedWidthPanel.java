package rs117.hd.panel.components;

import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;

public class FixedWidthPanel extends JPanel
{
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PluginPanel.PANEL_WIDTH, super.getPreferredSize().height);
	}

}