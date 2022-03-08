package rs117.hd.panel.debug.buttons;

import com.google.inject.Inject;
import net.runelite.api.Client;
import org.jfree.data.time.Millisecond;
import rs117.hd.panel.components.DebugFrame;
import rs117.hd.panel.components.graph.Chart;
import rs117.hd.panel.components.graph.ChartBuilder;
import rs117.hd.panel.components.graph.DataSet;
import rs117.hd.panel.components.graph.DataTimer;

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
