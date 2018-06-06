package j4u.demo;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;

import j4u.CommandLine;
import j4u.OptionSpecification;
import toools.io.file.RegularFile;

public class Port_Is_Free extends Java4UnixCommand
{

	public Port_Is_Free(RegularFile f)
	{
		super(f);
		addOption("--port", "-p", ".+", true, "port number");
	}

	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Checks if the given port is free";
	}

	public int runScript(CommandLine cmdLine)
	{
		for (String portT : cmdLine.findParameters())
		{
			int port = Integer.valueOf(portT);

			try
			{
				ServerSocket s = new ServerSocket(port);

				try
				{
					s.close();
				}
				catch (IOException e)
				{
				}

				printMessage("port " + port + " is free");
			}
			catch (IOException e)
			{
				printMessage("port " + port + " is allocated");
			}
		}

		return 0;
	}

	public void declareOptions(Collection<OptionSpecification> specs)
	{

	}

}
