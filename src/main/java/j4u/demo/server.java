package j4u.demo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import j4u.Command;
import j4u.CommandLine;
import j4u.CommandLineSpecification;
import toools.net.NetUtilities;

public class server extends Command {

	@Override
	public int runScript(CommandLine cmdLine) {
		cmdLine.findParameters().parallelStream().map(p -> Integer.valueOf(p)).forEach(port -> {
			try {
				ServerSocket serverSocket = new ServerSocket(port);
				printMessage("Listening on port " + serverSocket.getLocalPort() + "...");

				while (true) {
					Socket client = serverSocket.accept();
					printMessage("Accepting client from " + client.getInetAddress());
					String msg = "You are connected to the null server on computer "
							+ NetUtilities.determineLocalHostName() + ". Your host is " + client.getInetAddress() + ".";
					client.getOutputStream().write(msg.getBytes());
					BufferedInputStream bis = new BufferedInputStream(client.getInputStream());

					while (true) {
						int n = bis.read();

						if (n == -1) {
							break;
						} else {
							printMessage("" + ((char) n));
						}
					}
				}
			} catch (BindException ex) {
				printFatalError("cannot create server on port " + port + "; probably it is already in use.");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});

		return 0;
	}

	public String getDescription() {
		return "create a server (which does nothing else than listening) on the specified port";
	}

	@Override
	protected void specifyCmdLine(CommandLineSpecification spec) {
		// TODO Auto-generated method stub

	}

}
