package j4u.chain;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import toools.text.TextUtilities;

public class PluginConfig
{
	final Properties properties = new Properties();

	public PluginConfig(String s)
	{
		int indexOfDoubleDots = s.indexOf(':');

		if (indexOfDoubleDots == - 1)
		{
			properties.put("name", s);
			return;
		}

		properties.put("name", s.substring(0, indexOfDoubleDots));
		String p = s.substring(indexOfDoubleDots + 1);
		p = p.replace(',', '\n');

		try
		{
			properties.load(new StringReader(p));
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public String get(String name)
	{
		if ( ! contains(name))
			throw new IllegalArgumentException("parameter not specified: " + name);

		return (String) properties.remove(name);
	}

	public boolean contains(String name)
	{
		return properties.containsKey(name);
	}

	public boolean containsAndRemove(String name)
	{
		boolean b = properties.containsKey(name);
		properties.remove(name);
		return b;
	}

	public void ensureAllUsed()
	{
		if ( ! properties.isEmpty())
			throw new IllegalStateException("unsupported parm: " + properties);
	}

	public int getInt(String name)
	{
		return Integer.valueOf(get(name));
	}

	public long getLong(String name)
	{
		return Long.valueOf(get(name));
	}

	public boolean getBoolean(String name)
	{
		return TextUtilities.toBoolean(get(name));
	}

	public double getDouble(String s)
	{
		return Double.valueOf(get(s));
	}

	@Override
	public String toString()
	{
		return properties.toString();
	}

	public long getLong(String name, long defaultValue)
	{
		return contains(name) ? Long.valueOf(get(name)) : defaultValue;
	}
}
