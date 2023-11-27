package j4u.demo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;

import j4u.Command;
import j4u.CommandLine;
import j4u.CommandLineSpecification;
import j4u.OptionSpecification;
import toools.collections.Collections;
import toools.io.file.RegularFile;
import toools.text.TextUtilities;

public class Get_Ip_Addresses extends Command {

	
	@Override
	public int runScript(CommandLine cmdLine) {
		try {
			Collection<NetworkInterface> e = Collections
					.convertEnumerationToList(NetworkInterface.getNetworkInterfaces());
			printMessage("Found " + e.size() + " network interface(s):");

			for (NetworkInterface i : e) {
				printMessage("\nName:             " + i.getDisplayName());

				if (i.getHardwareAddress() != null) {
					printMessage("Hardware address: " + TextUtilities
							.toHex(i.getHardwareAddress(), '-').toUpperCase());
				}

				for (InetAddress address : Collections
						.convertEnumerationToList(i.getInetAddresses())) {
					printMessage("IP address:       " + address.getHostAddress());

					if ( ! address.getHostName().equals(address.getHostAddress())) {
						printMessage("Hostname:         " + address.getHostName());
					}
				}
			}

			return 0;
		}
		catch (SocketException ex) {
			printNonFatalError("Cannot list the network interfaces");
			return 1;
		}
	}



	@Override
	public String getDescription() {
		return "List the IP addresses associated to this computer";
	}



	@Override
	protected void specifyCmdLine(CommandLineSpecification spec) {
		// TODO Auto-generated method stub
		
	}
}
