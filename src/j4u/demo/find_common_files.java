package j4u.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import j4u.CommandLine;
import toools.io.FileUtilities;
import toools.io.file.RegularFile;


public class find_common_files extends Java4UnixCommand
{

	public find_common_files(RegularFile f)
	{
		super(f);
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		List<String> args = cmdLine.findParameters();
		List<File> l = new ArrayList<File>();

		for (String a : args)
		{
			l.add(new File(a));
		}
		
		for (String filename : FileUtilities.computeCommonFiles(l.toArray(new File[0])))
		{
			printMessage(filename);
		}

		return 0;
	}



	@Override
	public String getShortDescription()
	{
		return "Find common files in the given directories";
	}

}
