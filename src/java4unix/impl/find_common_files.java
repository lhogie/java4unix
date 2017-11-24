package java4unix.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import toools.io.FileUtilities;
import toools.io.file.RegularFile;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class find_common_files extends J4UScript
{

	public find_common_files(RegularFile f)
	{
		super(f);
		// TODO Auto-generated constructor stub
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

	public void declareOptions(Collection<OptionSpecification> specs)
	{

	}

	@Override
	public String getShortDescription()
	{
		return "Find common files in the given directories";
	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{

	}
}
