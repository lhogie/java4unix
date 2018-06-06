package j4u.demo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;

import j4u.CommandLine;
import j4u.OptionSpecification;
import toools.collections.Collections;
import toools.io.file.RegularFile;
import toools.text.TextUtilities;



public class Get_Ip_Addresses extends Java4UnixCommand
{

    public Get_Ip_Addresses(RegularFile f)
	{
		super(f);
	}

	@Override
    public int runScript(CommandLine cmdLine)
    {
        try
        {
            Collection<NetworkInterface> e = Collections.convertEnumerationToList(NetworkInterface.getNetworkInterfaces());
            printMessage("Found "  + e.size() + " network interface(s):");

            for (NetworkInterface i : e)
            {
                printMessage("\nName:             " + i.getDisplayName());
                
                if (i.getHardwareAddress() != null)
                {
                    printMessage("Hardware address: " + TextUtilities.toHex(i.getHardwareAddress(), "-").toUpperCase());
                }
                
                for (InetAddress address : Collections.convertEnumerationToList(i.getInetAddresses()))
                {
                    printMessage("IP address:       " + address.getHostAddress());
                    
                    if (!address.getHostName().equals(address.getHostAddress()))
                    {
                        printMessage("Hostname:         " + address.getHostName());
                    }
                }
            }

            return 0;
        }
        catch (SocketException ex)
        {
            printNonFatalError("Cannot list the network interfaces");
            return 1;
        }
    }

    public void declareOptions(Collection<OptionSpecification> specs)
    {
    }



	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "List the IP addresses associated to this computer";
	}
}
