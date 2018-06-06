package j4u.demo;

import java.util.Collection;

import j4u.ArgumentSpecification;
import j4u.CommandLine;
import j4u.OptionSpecification;
import j4u.Utilities;
import toools.io.file.Directory;
import toools.io.file.RegularFile;


public class list_installed_applications extends Java4UnixCommand
{
	public list_installed_applications(RegularFile f)
	{
		super(f);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getShortDescription()
	{
		return "list java4unix applications on this system";
	}
	

	@Override
	public int runScript(CommandLine cmdLine)
	{
		for (Directory d : Utilities.findInstalledApplicationDirs())
		{
			printMessage(d.getName().substring(1));
		}

		return 0;
	}
}
