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
