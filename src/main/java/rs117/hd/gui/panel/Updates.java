package rs117.hd.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;
import rs117.hd.HdPlugin;
import rs117.hd.gui.panel.components.FixedWidthPanel;
import rs117.hd.gui.panel.components.Header;

public class Updates extends JPanel
{

	@Getter
	private static List<File> updates = new ArrayList();

	private static final HashMap<Integer, String> contentCache = new HashMap<>();

	private final Header errorPanel = new Header();

	private JComboBox<String> environmentDropdown;

	private final JPanel topPanel = new JPanel();

	private final JLabel newsContent = new JLabel();


	public Updates()
	{

		setBackground(ColorScheme.DARK_GRAY_COLOR);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		topPanel.setBorder(new EmptyBorder(2, 0, 25, 0));
		errorPanel.setContent("Loading", "Loading Latest Updates");
		topPanel.add(errorPanel);

		loadUpdates();
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
			.addComponent(topPanel)
			.addGap(3).addComponent(scrollPane));

		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(topPanel, 0, Short.MAX_VALUE, Short.MAX_VALUE)
			.addGroup(layout.createSequentialGroup()
				.addComponent(scrollPane)));

		mainPanel.add(newsContent);
	}

	public void loadUpdates()
	{
		try
		{
			Collections.addAll(updates, Objects.requireNonNull((new File(HdPlugin.class.getResource("updates/").toURI())).listFiles()));
			updates.sort((file1, file2) -> getVersion(file2.getName()).compareTo(getVersion(file1.getName())));

			topPanel.removeAll();
			topPanel.add(createDropdown());
			displayNews(0);
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
			errorPanel.setContent("Error", "Could not retrieve updates please check logs.");
			environmentDropdown.setVisible(false);
			errorPanel.setVisible(true);
		}
	}

	private JPanel createDropdown()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1, 0, 5));
		panel.setMinimumSize(new Dimension(PANEL_WIDTH, 0));

		environmentDropdown = new JComboBox<>();
		environmentDropdown.setPreferredSize(new Dimension(225, 30));
		environmentDropdown.setForeground(Color.WHITE);
		environmentDropdown.setFocusable(false);
		environmentDropdown.addItem("Latest");
		for (File file : updates)
		{
			environmentDropdown.addItem(getFormattedName(file));
		}

		environmentDropdown.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				int selected = environmentDropdown.getSelectedIndex();
				displayNews(selected);
			}
		});
		panel.add(environmentDropdown);


		return panel;
	}


	public Integer getVersion(String name)
	{
		return Integer.valueOf(name.split("-")[0].replace(".", ""));
	}

	public void displayNews(int index)
	{
		int selected = index == 0 ? 0 : index - 1;
		if (contentCache.containsKey(index))
		{
			newsContent.setText(contentCache.get(selected));
			return;
		}

		newsContent.setText(updates.get(selected).getName());

		Parser parser = Parser.builder().build();

		try (InputStreamReader reader = new InputStreamReader(HdPlugin.class.getResource("updates/" + updates.get(selected).getName()).openStream(), Charset.forName("UTF-8")))
		{
			Node document = parser.parseReader(reader);
			HtmlRenderer renderer = HtmlRenderer.builder().build();
			String text = "<html>" + renderer.render(document) + "</html>";
			newsContent.setText("<html>" + renderer.render(document) + "</html>");
			contentCache.put(selected, text);
		}
		catch (IOException e)
		{
			errorPanel.setContent("Error", "Could not convert file to HTML");
			environmentDropdown.setVisible(false);
			errorPanel.setVisible(true);
			e.printStackTrace();
		}

	}


	public String getFormattedName(File file)
	{
		String[] info = file.getName().replace(".md", "").split("-");
		return info[1] + " - v" + info[0];
	}

}
