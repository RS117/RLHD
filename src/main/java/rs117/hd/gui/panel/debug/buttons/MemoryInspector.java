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
package rs117.hd.gui.panel.debug.buttons;

import com.google.inject.Inject;
import net.runelite.api.Client;
import org.jfree.data.time.Millisecond;
import rs117.hd.gui.panel.components.DebugFrame;
import rs117.hd.gui.panel.components.graph.Chart;
import rs117.hd.gui.panel.components.graph.builder.ChartBuilder;
import rs117.hd.gui.panel.components.graph.builder.DataSet;
import rs117.hd.gui.panel.components.graph.DataTimer;

import java.awt.*;
import java.text.NumberFormat;

public class MemoryInspector extends DebugFrame
{

	private final Client client;

	@Inject
	public MemoryInspector(Client client)
	{
		this.client = client;

		setTitle("JVM Memory Usage");

		setSize(new Dimension(577, 367));
		setPreferredSize(new Dimension(577, 367));
		pack();
	}

	private Chart memoryUsage;
	public NumberFormat format = NumberFormat.getInstance();
	private Runtime runtime = Runtime.getRuntime();

	@Override
	public void open()
	{

		ChartBuilder builder = new ChartBuilder().setMaxAge(60000)
			.setTitle("JVM Memory Usage")
			.setRightAxisLabel("Memory")
			.setBottomAxisLabel("Time")
			.setNumberFormat(format)
			.addDataset(new DataSet("Total Memory", Color.decode("#F68229")).build())
			.addDataset(new DataSet("Free Memory", Color.decode("#40B094")).build())
			.build();

		memoryUsage = new Chart(client, builder);
		memoryUsage.setData(new DataTimer(600, e -> {
			memoryUsage.getItems().get(0).add(new Millisecond(), runtime.totalMemory());
			memoryUsage.getItems().get(1).add(new Millisecond(), runtime.totalMemory() - runtime.freeMemory());
		}));
		memoryUsage.getData().start();
		add(memoryUsage);

		setVisible(true);
		super.open();
	}

	@Override
	public void close()
	{

		dispose();
		super.close();
	}

}
