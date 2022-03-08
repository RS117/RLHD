package rs117.hd.panel.components.graph;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class Chart extends JPanel
{

	private final Client client;

	@Getter
	private HashMap<Integer, TimeSeries> items = new HashMap<>();

	@Getter
	private final TimeSeriesCollection dataset = new TimeSeriesCollection();

	@Getter
	@Setter
	private DataTimer data;

	private final ChartBuilder builder;

	private final Font tickLabelFont = new Font("SansSerif", Font.PLAIN, 12);
	private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
	private final Color darkGray = new Color(ColorScheme.DARK_GRAY_COLOR.getRGB());
	private final Color lightGray = new Color(ColorScheme.LIGHT_GRAY_COLOR.getRGB());

	public Chart(Client client, ChartBuilder builder)
	{
		super(new BorderLayout());
		this.client = client;
		this.builder = builder;
		setSize(new Dimension(577, 367));
		setPreferredSize(new Dimension(577, 367));


		for (int index = 0; index < builder.getDataset().size(); index++)
		{
			DataSet data = builder.getDataset().get(index);
			TimeSeries series = new TimeSeries(data.getName());
			series.setMaximumItemAge(builder.getMaxAge());
			dataset.addSeries(series);
			items.put(index, series);
		}

		DateAxis domain = new DateAxis(builder.getBottomAxisLabel());
		NumberAxis range = new NumberAxis(builder.getRightAxisLabel());

		domain.setLabelFont(labelFont);
		range.setLabelFont(labelFont);
		range.setLabelPaint(lightGray);
		domain.setLabelPaint(lightGray);
		range.setTickLabelPaint(lightGray);
		domain.setTickLabelPaint(lightGray);

		range.setNumberFormatOverride(builder.getNumberFormat());

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		BasicStroke stroke = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
		for (int index = 0; index < builder.getDataset().size(); index++)
		{
			DataSet data = builder.getDataset().get(index);
			renderer.setSeriesPaint(index, data.getColor());
			renderer.setSeriesStroke(index, stroke);
		}

		XYPlot plot = new XYPlot(dataset, domain, range, renderer);
		plot.setBackgroundPaint(darkGray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		domain.setAutoRange(true);
		domain.setLowerMargin(0.0);
		domain.setUpperMargin(0.0);
		domain.setTickLabelsVisible(true);
		range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		Font titleFont = new Font("SansSerif", Font.BOLD, 24);
		boolean createLegend = true;
		JFreeChart chart = new JFreeChart(builder.getTitle(), titleFont, plot, createLegend);

		chart.getTitle().setPaint(new Color(ColorScheme.LIGHT_GRAY_COLOR.getRGB()));
		ChartPanel chartPanel = new ChartPanel(chart);

		LegendTitle legend = chart.getLegend();
		legend.setBackgroundPaint(new Color(ColorScheme.DARK_GRAY_COLOR.getRGB()));
		legend.setItemPaint(new Color(ColorScheme.LIGHT_GRAY_COLOR.getRGB()));

		plot.setDomainGridlinePaint(ColorScheme.DARK_GRAY_COLOR);
		plot.setRangeGridlinePaint(ColorScheme.DARK_GRAY_COLOR);

		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), BorderFactory.createLineBorder(ColorScheme.DARK_GRAY_COLOR)));
		add(chartPanel);

	}

}