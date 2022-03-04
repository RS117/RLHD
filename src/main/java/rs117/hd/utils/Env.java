package rs117.hd.utils;

import java.util.HashMap;
import java.util.function.Supplier;

public class Env
{
	private static HashMap<String, String> env = new HashMap<>(System.getenv());

	public static boolean has(String variableName)
	{
		return env.containsKey(variableName);
	}

	public static boolean missing(String variableName)
	{
		return !has(variableName);
	}

	public static String get(String variableName)
	{
		return env.get(variableName);
	}

	public static String getDefault(String variableName, String defaultValue)
	{
		String value = env.get(variableName);
		return value == null ? defaultValue : value;
	}

	public static String getDefault(String variableName, Supplier<String> defaultValueSupplier)
	{
		String value = env.get(variableName);
		if (value == null)
		{
			return defaultValueSupplier.get();
		}
		return value;
	}

	public static void set(String variableName, String value)
	{
		if (value == null)
		{
			unset(variableName);
		}
		else
		{
			env.put(variableName, value);
		}
	}

	public static void unset(String variableName)
	{
		env.remove(variableName);
	}
}
