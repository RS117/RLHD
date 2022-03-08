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
package rs117.hd.panel.debug.buttons;

import com.google.inject.Inject;
import net.runelite.api.Client;
import org.jfree.data.time.Millisecond;
import rs117.hd.panel.components.DebugFrame;
import rs117.hd.panel.components.graph.Chart;
import rs117.hd.panel.components.graph.builder.ChartBuilder;
import rs117.hd.panel.components.graph.builder.DataSet;
import rs117.hd.panel.components.graph.DataTimer;

import java.awt.*;
import java.text.NumberFormat;

public class FpsInspector extends DebugFrame
{

	private final Client client;

	@Inject
	public FpsInspector(Client client)
	{
		this.client = client;
		setTitle("Fps Usage");
		setSize(new Dimension(577, 367));
		setPreferredSize(new Dimension(577, 367));
		pack();
	}

	public NumberFormat format = NumberFormat.getInstance();

	private Chart fpsUsage;

	@Override
	public void open()
	{

		ChartBuilder builder = new ChartBuilder().setMaxAge(60000)
			.setTitle("Fps Graph")
			.setRightAxisLabel("Fps")
			.setBottomAxisLabel("Time")
			.setNumberFormat(format)
			.addDataset(new DataSet("Fps", Color.decode("#F68229")).build())
			.build();

		fpsUsage = new Chart(client, builder);
		fpsUsage.setData(new DataTimer(600, e -> fpsUsage.getItems().get(0).add(new Millisecond(), client.getFPS())));
		fpsUsage.getData().start();
		add(fpsUsage);

		setVisible(true);
		super.open();
	}

	@Override
	public void close()
	{
		fpsUsage.getData().stop();
		fpsUsage = null;
		super.close();
	}

}
