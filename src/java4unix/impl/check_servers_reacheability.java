package java4unix.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;
import toools.io.file.RegularFile;
import toools.text.TextUtilities;
import toools.thread.Threads;

public class check_servers_reacheability extends J4UScript
{
	public check_servers_reacheability(RegularFile f)
	{
		super(f);
	}

	@Override
	public String getShortDescription()
	{
		return "Checks if the given servers are accessible.";
	}

	public int runScript(CommandLine cmdLine) throws IOException
	{
		final List<String> serverNames = getServerNames(cmdLine);
		final int timeoutMs = Integer.valueOf(getOptionValue(cmdLine, "--timeout"));
		final int port = Integer.valueOf(getOptionValue(cmdLine, "--port"));

		for (int serverIndex = 0; serverIndex < serverNames.size(); ++serverIndex)
		{
			final String serverName = serverNames.get(serverIndex);
			final int row = serverIndex;

			new Thread()
			{
				@Override
				public void run()
				{
					System.out.println("checking " + serverNames.get(row));
					STATUS status = serverIsAlive(serverNames.get(row), port, timeoutMs);

					printMessage(TextUtilities.flushLeft(serverName, 20, ' ') + "\t"
							+ status.name());
				}
			}.start();
		}

		Threads.sleepForever();
		return 0;
	}

	private List<String> getServerNames(CommandLine cmdLine) throws IOException
	{
		if (isOptionSpecified(cmdLine, "--file"))
		{
			String filename = getOptionValue(cmdLine, "--file");
			RegularFile f = new RegularFile(filename);
			return f.getLines();
		}
		else
		{
			return new ArrayList<String>(cmdLine.findParameters());
		}
	}

	public void declareOptions(Collection<OptionSpecification> specs)
	{
		addOption("--port", "-p", "[0-9]+", "22", "The port to check to");
		addOption("--timeout", "-t", "[0-9]+", "1000",
				"The acceptable wait (in millisecond)");
		addOption("--file", "-f", ".*", null,
				"A file containing the list of server names to monitor");
	}

	@Override
	protected void declareArguments(
			Collection<ArgumentSpecification> argumentSpecifications)
	{
	}

	enum STATUS
	{
		UNREACHEABLE, REACHEABLE, RESPONDING
	}

	private STATUS serverIsAlive(String serverName, int port, int timeoutMs)
	{
		try
		{
			if ( ! InetAddress.getByName(serverName).isReachable(timeoutMs))
				return STATUS.UNREACHEABLE;
		}
		catch (Exception e1)
		{
			return STATUS.UNREACHEABLE;
		}

		try
		{
			Socket s = new Socket();
			SocketAddress ip = new InetSocketAddress(serverName, port);
			s.connect(ip, timeoutMs);

			try
			{
				s.close();
			}
			catch (IOException e)
			{
			}

			return STATUS.RESPONDING;
		}
		catch (IOException e)
		{
			return STATUS.REACHEABLE;
		}
	}
}
