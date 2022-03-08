package rs117.hd.panel.components.graph;

import lombok.Getter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class DataTimer extends Timer implements ActionListener
{

	@Getter
	private Consumer consumer;

	public DataTimer(int interval, Consumer consumer)
	{
		super(interval, null);
		this.consumer = consumer;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		consumer.accept(this);
	}
}