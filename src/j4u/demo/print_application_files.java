package j4u.demo;

import j4u.CommandLine;
import toools.io.file.Directory;
import toools.io.file.RegularFile;

public class print_application_files extends Java4UnixCommand
{

	public print_application_files(RegularFile f)
	{
		super(f);
		// TODO Auto-generated constructor stub
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
