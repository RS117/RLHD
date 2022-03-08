package rs117.hd.panel.components;

import java.awt.Color;
import java.util.function.Consumer;
import javax.swing.JButton;
import lombok.Getter;
import net.runelite.client.util.LinkBrowser;

public class ToggleButton extends JButton
{
	@Getter
	private boolean toggled;

	public ToggleButton(String title, String tooltip)
	{
		super(title);
		addActionListener((ev) -> setToggled(!toggled));
		this.setToolTipText(tooltip);
	}

	public ToggleButton(String title, String tooltip, Consumer consumer)
	{
		super(title);
		addActionListener((ev) -> consumer.accept(null));
		this.setToolTipText(tooltip);
	}

	public ToggleButton(String title, String tooltip, String url)
	{
		super(title);
		addActionListener((ev) -> LinkBrowser.browse(url));
		this.setToolTipText(tooltip);
	}

	void setToggled(boolean toggled)
	{
		this.toggled = toggled;

		if (toggled)
		{
			setBackground(Color.GREEN);
		}
		else
		{
			setBackground(null);
		}
	}

	public void setMode(boolean enabled)
	{
		setEnabled(enabled);
		if (!enabled)
		{
			setBackground(Color.RED);
		}
		else
		{
			setBackground(null);
		}
	}

	public void addFrame(DebugFrame frame)
	{
		frame.setToolsButton(this);
		addActionListener(ev ->
		{
			if (!isEnabled())
			{
				frame.close();
			}
			else
			{
				frame.open();
			}
		});
	}

}