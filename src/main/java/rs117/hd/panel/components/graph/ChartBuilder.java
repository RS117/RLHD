package rs117.hd.panel.components.graph;

import lombok.Data;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChartBuilder
{

	private int maxAge;
	private String title;
	private String bottomAxisLabel;
	private String rightAxisLabel;

	private NumberFormat numberFormat;
	private List<DataSet> dataset = new ArrayList<>();

	public ChartBuilder()
	{
	}

	public ChartBuilder(int maxAge, String bottomAxisLabel, String rightAxisLabel, String title, List<DataSet> dataset, NumberFormat numberFormat)
	{
		this.maxAge = maxAge;
		this.bottomAxisLabel = bottomAxisLabel;
		this.rightAxisLabel = rightAxisLabel;
		this.title = title;
		this.dataset = dataset;
		this.numberFormat = numberFormat;

	}

	public ChartBuilder setMaxAge(int maxAge)
	{
		this.maxAge = maxAge;
		return this;
	}

	public ChartBuilder setTitle(String title)
	{
		this.title = title;
		return this;
	}

	public ChartBuilder setBottomAxisLabel(String bottomAxisLabel)
	{
		this.bottomAxisLabel = bottomAxisLabel;
		return this;
	}

	public ChartBuilder setRightAxisLabel(String rightAxisLabel)
	{
		this.rightAxisLabel = rightAxisLabel;
		return this;
	}

	public ChartBuilder setNumberFormat(NumberFormat format)
	{
		this.numberFormat = format;
		return this;
	}


	public ChartBuilder addDataset(DataSet dataset)
	{
		this.dataset.add(dataset);
		return this;
	}

	public ChartBuilder build()
	{
		return new ChartBuilder(maxAge, bottomAxisLabel, rightAxisLabel, title, dataset, numberFormat);
	}

}