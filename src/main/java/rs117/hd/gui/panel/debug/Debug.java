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
package rs117.hd.gui.panel.debug;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import rs117.hd.HdPlugin;
import rs117.hd.gui.panel.HdPanel;
import rs117.hd.gui.panel.components.CategoryBuilder;
import rs117.hd.gui.panel.components.FixedWidthPanel;
import rs117.hd.gui.panel.components.Header;
import rs117.hd.gui.panel.debug.overlays.LightInfoOverlay;
import rs117.hd.gui.panel.debug.overlays.TileInfoOverlay;
import rs117.hd.gui.panel.debug.sections.Buttons;
import rs117.hd.gui.panel.debug.sections.HotKeys;
import rs117.hd.gui.panel.debug.sections.Specifications;

public class Debug extends PluginPanel
{

	@Getter
	private static final HashMap<Integer, CategoryBuilder> category = new HashMap<>();
	public static ImageIcon SECTION_EXPAND_ICON;
	public static ImageIcon SECTION_EXPAND_ICON_HOVER;
	public static ImageIcon SECTION_RETRACT_ICON;
	public static ImageIcon SECTION_RETRACT_ICON_HOVER;

	@Getter
	@Setter
	private Specifications specifications;

	@Getter
	@Setter
	public Buttons buttons;

	@Getter
	@Setter
	private HotKeys hotKeys;

	@Inject
	private LightInfoOverlay lightInfoOverlay;

	@Inject
	private TileInfoOverlay tileInfoOverlay;

	static
	{
		BufferedImage sectionRetractIcon = ImageUtil.loadImageResource(HdPanel.class, "arrow_right.png");
		sectionRetractIcon = ImageUtil.luminanceOffset(sectionRetractIcon, -121);
		SECTION_EXPAND_ICON = new ImageIcon(sectionRetractIcon);
		SECTION_EXPAND_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(sectionRetractIcon, -100));
		final BufferedImage sectionExpandIcon = ImageUtil.rotateImage(sectionRetractIcon, Math.PI / 2);
		SECTION_RETRACT_ICON = new ImageIcon(sectionExpandIcon);
		SECTION_RETRACT_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(sectionExpandIcon, -100));
	}

	@Inject
	public Debug(HdPlugin plugin)
	{

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(0, 0, 0, 0));

		Header errorPanel = new Header();
		errorPanel.setContent("Debug Panel", "Some of these options may drop Performance");

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 7, 7, 7));
		mainPanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel mainPanelWrapper = new FixedWidthPanel();
		mainPanelWrapper.setLayout(new BorderLayout());
		mainPanelWrapper.add(mainPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		// Can't use Short.MAX_VALUE like the docs say because of JDK-8079640
		scrollPane.setPreferredSize(new Dimension(0x7000, 0x7000));
		scrollPane.setViewportView(mainPanelWrapper);

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(errorPanel)
			.addGap(3).addComponent(scrollPane));

		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(errorPanel, 0, Short.MAX_VALUE, Short.MAX_VALUE)
			.addGroup(layout.createSequentialGroup()
				.addComponent(scrollPane)));


		setSpecifications(new Specifications(plugin));
		setButtons(new Buttons(plugin));
		setHotKeys(new HotKeys(plugin));

		new CategoryBuilder().
			setName("Toggle Buttons").
			setPosition(0).
			setPanel(buttons).
			setEnabled(true)
		.build();

		new CategoryBuilder().
			setName("Computer Specifications").
			setPosition(1).
			setPanel(specifications).
			setEnabled(false)
		.build();

		new CategoryBuilder().
				setName("Developer Hotkeys").
				setPosition(2).
				setPanel(hotKeys).
				setEnabled(false)
		.build();

		List<Map.Entry<Integer, CategoryBuilder>> result =
			category.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toList());


		result.forEach(entry -> {
			mainPanel.add(entry.getValue().getCategory());
			mainPanel.add(entry.getValue().getPanel());
			entry.getValue().getCategory().getSectionContents().add(entry.getValue().getPanel());
			entry.getValue().getCategory().toggleSelection(entry.getValue().getEnabled());
			entry.getValue().getCategory().repaint();
		});


	}


}

