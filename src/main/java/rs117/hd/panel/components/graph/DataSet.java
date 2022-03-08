package rs117.hd.panel.components.graph;

import lombok.Data;
import org.jfree.data.time.TimeSeries;

import java.awt.*;

@Data
public class DataSet
{

	private TimeSeries value;
	private String name;
	private Color color;

	public DataSet(String name, Color color)
	{
		this.name = name;
		this.color = color;
	}

	public DataSet setName(String name)
	{
		this.name = name;
		return this;
	}

	public DataSet setColor(Color color)
	{
		this.color = color;
		return this;
	}

	public DataSet build()
	{
		return new DataSet(name, color);
	}

}