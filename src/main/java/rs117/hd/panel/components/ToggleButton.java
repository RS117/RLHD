/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * Copyright (c) 2021 Mark_ <https://github.com/Mark7625/>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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