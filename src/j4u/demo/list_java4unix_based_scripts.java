package j4u.demo;

import java.util.Collection;

import j4u.ArgumentSpecification;
import j4u.CommandLine;
import j4u.OptionSpecification;
import j4u.Utilities;
import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;


public class list_java4unix_based_scripts extends Java4UnixCommand
{
	public list_java4unix_based_scripts(RegularFile f)
	{
		super(f);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getShortDescription()
	{
		return "list java4unix scripts on this system";
	}

	

	@Override
	public int runScript(CommandLine cmdLine)
	{
		for (Directory d : Utilities.findInstalledApplicationDirs())
		{
			Directory binDir = new Directory(d, "bin");

			for (AbstractFile s : binDir.getChildren())
			{
				printMessage(s.getPath());
			}
		}

		return 0;
	}
}
