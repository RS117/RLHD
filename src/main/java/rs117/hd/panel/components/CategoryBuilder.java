package rs117.hd.panel.components;

import javax.swing.JPanel;
import lombok.Data;
import rs117.hd.panel.debug.Debug;

@Data
public class CategoryBuilder
{

	private int position;
	private Category category;
	private JPanel panel;
	private String name;
	private Boolean enabled;

	public CategoryBuilder()
	{

	}
	public CategoryBuilder(int position, JPanel panel, String name, boolean enabled, Category category)
	{
		this.position = position;
		this.category = category;
		this.panel = panel;
		this.name = name;
		this.enabled = enabled;
	}

	public CategoryBuilder setPosition(int position)
	{
		this.position = position;
		return this;
	}

	public CategoryBuilder setName(String name)
	{
		this.name = name;
		return this;
	}

	public CategoryBuilder setEnabled(Boolean enabled)
	{
		this.enabled = enabled;
		return this;
	}

	public CategoryBuilder setPanel(JPanel panel)
	{
		this.panel = panel;
		return this;
	}

	public CategoryBuilder build()
	{
		CategoryBuilder cat = new CategoryBuilder(position, panel,name,enabled, new Category(name));
		Debug.getCategory().put(position,cat);
		return cat;
	}

}