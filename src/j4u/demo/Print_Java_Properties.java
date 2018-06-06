package j4u.demo;

import java.util.Properties;

import j4u.CommandLine;
import toools.io.file.RegularFile;

public class Print_Java_Properties extends Java4UnixCommand
{

	public Print_Java_Properties(RegularFile f)
	{
		super(f);
		addOption("--only-keys", "-k", null, null, "print only available keys");
		addOption("--print-value", "-p", ".+", ".+", "print the value of the given key");
	}

	@Override
	public String getShortDescription()
	{
		return "Print Java properties.";
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		boolean onlykeys = isOptionSpecified(cmdLine, "-k");
		Properties p = System.getProperties();

		if (isOptionSpecified(cmdLine, "-p"))
		{
			String key = getOptionValue(cmdLine, "-p");

			if ( ! p.containsKey(key))
			{
				printFatalError("unexisting key: " + key);
				return 1;
			}
			else
			{
				printMessage((String) p.get(key));
			}
		}
		else
		{
			for (Object o : p.keySet())
			{
				printMessage(o + (onlykeys ? "" : "=" + p.get(o)));
			}
		}

		return 0;
	}

}
