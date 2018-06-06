package j4u.demo;

import j4u.CommandLine;
import toools.io.file.RegularFile;

public class Concatene_Lines extends Java4UnixCommand
{
	public Concatene_Lines(RegularFile f)
	{
		super(f);
		addOption("--separator", "-s", ".*", "", "separator");
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		String sep = getOptionValue(cmdLine, "-s");
		String prev = null;

		while (true)
		{
			String line = readUserInput("", ".*");

			if (line == null)
			{
				break;
			}
			else
			{
				if (prev != null)
				{
					printMessageWithNoNewLine(sep);
				}

				printMessageWithNoNewLine(line);
				prev = line;
			}
		}

		printMessage("");
		return 0;
	}

	@Override
	public String getShortDescription()
	{
		return "concatene the given lines";
	}
}
