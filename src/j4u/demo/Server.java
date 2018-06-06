package j4u.demo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;

import j4u.CommandLine;
import j4u.OptionSpecification;
import toools.io.file.RegularFile;
import toools.net.NetUtilities;

public class Server extends Java4UnixCommand
{

	public Server(RegularFile f)
	{
		super(f);
		addOption("--port", "-p", ".+", false, "port number");

	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		int port = Integer.valueOf(cmdLine.findParameters().get(0));

		try
		{
			ServerSocket serverSocket = new ServerSocket(port);
			printMessage("Listening on port " + serverSocket.getLocalPort() + "...");

			while (true)
			{
				Socket client = serverSocket.accept();
				printMessage("Accepting client from " + client.getInetAddress());
				String msg = "You are connected to the null server on computer "
						+ NetUtilities.determineLocalHostName() + ". Your host is "
						+ client.getInetAddress() + ".";
				client.getOutputStream().write(msg.getBytes());
				BufferedInputStream bis = new BufferedInputStream(
						client.getInputStream());

				while (true)
				{
					int n = bis.read();

					if (n == - 1)
					{
						break;
					}
					else
					{
						printMessage("" + ((char) n));
					}
				}
			}
		}
		catch (BindException ex)
		{
			printFatalError("cannot create server on port " + port
					+ "; probably it is already in use.");
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		return 0;
	}

	public void declareOptions(Collection<OptionSpecification> specs)
	{
		specs.add(new OptionSpecification("--port", "-p", "[0-9]+", null,
				"specify the port to listen on"));

	}

	public String getShortDescription()
	{
		return "create a server (which does nothing else than listening) on the specified port";
	}

}
