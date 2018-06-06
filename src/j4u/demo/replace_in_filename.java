package j4u.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import j4u.CommandLine;
import toools.io.file.RegularFile;

public class replace_in_filename extends Java4UnixCommand
{

	public replace_in_filename(RegularFile f)
	{
		super(f);
		addOption("--pattern", "-p", ".+", false, "pattern to look for");
		addOption("--replacement", "-r", ".+", false, "pattern to look for");
		addOption("--file", "-f", ".+", true, "text file");
	}

	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Replace text in filenames.";
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		List<String> args = new ArrayList<String>(cmdLine.findParameters());
		String regexp = args.remove(0);
		String replacement = args.remove(0);

		for (String filename : args)
		{
			File f = new File(filename);
			String oldName = f.getName();
			String newName = oldName.replaceFirst(regexp, replacement);
			f.renameTo(new File(f.getParentFile(), newName));

			if ( ! oldName.equals(newName))
			{
				printMessage(f.getAbsolutePath());
			}
		}

		return 0;
	}
}
