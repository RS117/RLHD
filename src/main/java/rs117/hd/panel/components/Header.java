/*
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

import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Header extends JPanel
{
	private final JLabel noResultsTitle = new JShadowedLabel();
	private final JLabel noResultsDescription = new JShadowedLabel();

	public Header()
	{
		setOpaque(false);
		setBorder(new EmptyBorder(10, 0, 7, 0));
		setLayout(new BorderLayout());

		noResultsTitle.setForeground(Color.WHITE);
		noResultsTitle.setHorizontalAlignment(SwingConstants.CENTER);

		noResultsDescription.setFont(FontManager.getRunescapeSmallFont());
		noResultsDescription.setForeground(Color.GRAY);
		noResultsDescription.setHorizontalAlignment(SwingConstants.CENTER);

		add(noResultsTitle, BorderLayout.NORTH);
		add(noResultsDescription, BorderLayout.CENTER);

		setVisible(false);
	}

	/**
	 * Changes the content of the panel to the given parameters.
	 * The description has to be wrapped in html so that its text can be wrapped.
	 */
	public void setContent(String title, String description)
	{
		noResultsTitle.setText(title);
		noResultsDescription.setText("<html><body style = 'text-align:center'>" + description + "</body></html>");
		setVisible(true);
	}
}