package com.hd;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import rs117.hd.HdPlugin;

public class HdPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HdPlugin.class);
		RuneLite.main(args);
	}
}