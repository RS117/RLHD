package rs117.hd.panel.components;

import lombok.Setter;
import net.runelite.client.ui.ClientUI;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DebugFrame extends JFrame
{
	@Setter()
	protected ToggleButton toolsButton;

	public DebugFrame()
	{
		setIconImage(ClientUI.ICON);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				close();
				toolsButton.setToggled(false);
			}
		});
	}

	public void open()
	{

		setVisible(true);
		toFront();
		repaint();
	}

	public void close()
	{
		setVisible(false);
	}
}
