package java4unix.impl;

import java4unix.CommandLineApplication;
import java4unix.License;
import toools.io.file.RegularFile;

public abstract class J4UScript extends CommandLineApplication
{
	public J4UScript(RegularFile f)
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
		return License.GPL;
	}

	@Override
	public final String getYear()
	{
		return "2008-2016";
	}

}
