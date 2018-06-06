package java4unix.impl;

import java.io.IOException;

import toools.Version;
import toools.io.JavaResource;
import toools.log.Logger;
import toools.net.NetUtilities;

public class NewVersionNotifications
{
	public static boolean enabled = true;

	public static void checkForNewVersion(final String project)
	{
		final String url = "http://www.i3s.unice.fr/~hogie/" + project +"/";
		
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					String s = new String(NetUtilities.retrieveURLContent(url + "releases/last-version.txt"));

					// in some case an access to the network exists but the
					// access to internet is block by a portal
					// so we get a valid HTTP result but not the content of the
					// file (we get the result from the portal
					// which is likely to ask for login information
					if (s.matches("[0-9.]+"))
					{
						Version lastVersion = new Version();
						lastVersion.set(s);

						if (lastVersion.isNewerThan(getVersion(project)))
						{
							System.out.println("New version " + lastVersion + " is available at " + url);
							System.out.println("Current version is " + getVersion(project));
						}
					}
				}
				catch (Throwable e)
				{
				}
			}
		}.start();
	}

	/**
	 * Computes the version of the BigGrph library installed.
	 * 
	 * @return a string denoting the version of BigGrph.
	 */
	public static Version getVersion(String project)
	{
		try
		{
			Version v = new Version();
			v.set(new String(new JavaResource("/" + project + "-version.txt").getByteArray()));
			return v;
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}
}
