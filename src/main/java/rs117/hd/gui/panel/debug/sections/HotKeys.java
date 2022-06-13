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
package rs117.hd.gui.panel.debug.sections;

import net.runelite.client.config.Keybind;
import net.runelite.client.ui.ColorScheme;
import rs117.hd.HdPlugin;
import rs117.hd.gui.panel.components.HotkeyButton;
import rs117.hd.gui.panel.components.ToggleButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class HotKeys extends JPanel
{

	private ArrayList<HotkeyButton> buttons = new ArrayList<>();

	public HotKeys(HdPlugin plugin) {
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new GridLayout(0, 2, 1, 2));

		buttons.add(new HotkeyButton(new Keybind(KeyEvent.VK_F3, InputEvent.CTRL_DOWN_MASK),"Tile Overlays", e -> {
			plugin.getPanel().getDebugPanel().getButtons().getTileInfoOverlay().toggle();
		}));

		for(HotkeyButton button: buttons) {
			button.setPlugin(plugin);
			add(new JLabel(button.getKey() + ":"));
			plugin.getKeyManager().registerKeyListener(button);
			add(button);
		}

	}

}
