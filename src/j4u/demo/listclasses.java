package java4unix.impl;

import java.util.Collection;

import toools.ClassContainer;
import toools.ClassPath;
import toools.io.file.AbstractFile;
import toools.io.file.RegularFile;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;

public class listclasses extends J4UScript
{
	public listclasses(RegularFile f)
	{
		super(f);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Throwable
	{
		new listclasses(null).run();
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		if (cmdLine.findParameters().isEmpty())
		{
			for (ClassContainer cc : ClassPath.retrieveSystemClassPath())
			{
				for (Class c : cc.listAllClasses())
				{
					printMessage(c.getName());
				}
			}
		}
		else
		{
			ClassContainer cc = new ClassContainer(AbstractFile.map(cmdLine
					.findParameters().get(0), null));
			
			for (Class c : cc.listAllClasses())
			{
				printMessage(c.getName());
			}
		}

		return 0;
	}

	public void declareOptions(Collection<OptionSpecification> specs)
	{

	}

	@Override
	public String getShortDescription()
	{
		return "list the class in the given jar or directories.";
	}

	@Override
	protected void declareArguments(
			Collection<ArgumentSpecification> argumentSpecifications)
	{

	}
}
