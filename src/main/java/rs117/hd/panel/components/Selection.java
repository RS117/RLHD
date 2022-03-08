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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import static net.runelite.client.ui.PluginPanel.BORDER_OFFSET;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;
import net.runelite.client.util.SwingUtil;
import rs117.hd.panel.debug.Debug;
import static rs117.hd.panel.debug.Debug.SECTION_EXPAND_ICON;
import static rs117.hd.panel.debug.Debug.SECTION_EXPAND_ICON_HOVER;
import static rs117.hd.panel.debug.Debug.SECTION_RETRACT_ICON;
import static rs117.hd.panel.debug.Debug.SECTION_RETRACT_ICON_HOVER;

public class Selection extends JPanel
{
	@Getter
	private String title;
	@Setter
	private boolean selected;
	@Getter
	private JButton sectionToggle;
	@Getter
	private final JPanel sectionContents;

	public Selection(String title)
	{
		this.title = title;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setMinimumSize(new Dimension(PANEL_WIDTH, 0));
		JPanel sectionHeader = new JPanel();
		sectionHeader.setLayout(new BorderLayout());
		sectionHeader.setMinimumSize(new Dimension(PANEL_WIDTH, 0));
		MouseListener mouseListener = new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				toggleSelection();
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{

			}

			@Override
			public void mouseEntered(MouseEvent e)
			{

			}

			@Override
			public void mouseExited(MouseEvent e)
			{

			}

		};
		sectionHeader.addMouseListener(mouseListener);
		// For whatever reason, the header extends out by a single pixel when closed. Adding a single pixel of
		// border on the right only affects the width when closed, fixing the issue.
		sectionHeader.setBorder(new CompoundBorder(
			new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
			new EmptyBorder(0, 0, 3, 1)));
		add(sectionHeader, BorderLayout.NORTH);

		sectionToggle = new JButton(selected ? SECTION_RETRACT_ICON : SECTION_EXPAND_ICON);
		sectionToggle.setRolloverIcon(selected ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
		sectionToggle.setPreferredSize(new Dimension(18, 0));
		sectionToggle.setBorder(new EmptyBorder(0, 0, 0, 5));
		sectionToggle.setToolTipText(selected ? "Retract" : "Expand");

		SwingUtil.removeButtonDecorations(sectionToggle);
		sectionHeader.add(sectionToggle, BorderLayout.WEST);
		sectionToggle.addActionListener(e -> toggleSelection());
		final JLabel sectionName = new JLabel(title);

		sectionName.setForeground(ColorScheme.BRAND_ORANGE);
		sectionName.setFont(FontManager.getRunescapeBoldFont());
		sectionHeader.add(sectionName, BorderLayout.CENTER);

		sectionContents = new JPanel();
		sectionContents.setLayout(new DynamicGridLayout(0, 1, 0, 5));
		sectionContents.setMinimumSize(new Dimension(PANEL_WIDTH, 0));
		sectionContents.setBorder(new CompoundBorder(
			new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
			new EmptyBorder(BORDER_OFFSET, 0, BORDER_OFFSET, 0)));

		add(sectionContents, BorderLayout.SOUTH);

	}

	public void toggleSelection()
	{
		JPanel panel = Debug.getExpanding().get(this);
		panel.setVisible(!panel.isVisible());
		getParent().validate();

		setSelected(!selected);
		sectionToggle.setIcon(selected ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
		sectionToggle.setRolloverIcon(selected ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
		sectionToggle.setToolTipText(selected ? "Retract" : "Expand");
		sectionContents.getComponent(0).setVisible(selected);
		sectionContents.setVisible(selected);
		repaint();
	}


}