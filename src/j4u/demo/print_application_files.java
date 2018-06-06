package java4unix.impl;

import java.util.Collection;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;
import toools.io.file.Directory;
import toools.io.file.RegularFile;

public class print_application_files extends J4UScript
{

	public print_application_files(RegularFile f)
	{
		super(f);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void declareArguments(
			Collection<ArgumentSpecification> argumentSpecifications)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{
		{
			Directory d = getInstallationDirectory();
			System.out.println("installation directory: " + d);
		}

		{
			Directory d = getDataDirectory();
			System.out.println("data directory: " + d);
		}

		{
			RegularFile f = getUserConfigurationFile();
			System.out.println("user configuration file: " + f);

			if (f.exists())
			{
				System.out.println(extractConfiguration());
			}
		}

		return 0;
	}

	@Override
	public String getShortDescription()
	{
		return "only print config";
	}

}
