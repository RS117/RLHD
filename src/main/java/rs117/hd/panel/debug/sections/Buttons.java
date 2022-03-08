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
