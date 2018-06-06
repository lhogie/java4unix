package j4u.demo;

import j4u.CommandLineApplication;
import j4u.License;
import toools.io.file.RegularFile;

public abstract class Java4UnixCommand extends CommandLineApplication
{
	public Java4UnixCommand(RegularFile f)
	{
		super(f);
	}

	@Override
	public final String getAuthor()
	{
		return "Luc Hogie, Aurelien Lancin, Benoit Ferrero (CNRS/INRIA/Universte Nice Sophia Antipolis)";
	}

	@Override
	public String getApplicationName()
	{
		return "java4unix";
	}

	@Override
	public final License getLicence()
	{
		return License.ApacheLicenseV2;
	}

	@Override
	public final String getYear()
	{
		return "2008-2018";
	}

}
