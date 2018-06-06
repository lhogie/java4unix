package j4u;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import toools.io.JavaResource;
import toools.reflect.ClassContainer;
import toools.reflect.ClassPath;
import toools.text.TextUtilities;

public abstract class Application
{
	private final List<String> vmOptions = new ArrayList<>();

	public List<String> getVMOptions()
	{
		return vmOptions;
	}

	public String getVersion()
	{
		JavaResource r = new JavaResource("/" + getApplicationName() + "-version.txt");

		try
		{
			return new String(r.getByteArray());
		}
		catch (IOException e)
		{
			return "0.0.0";
		}
	}

	public abstract String getApplicationName();

	public abstract String getAuthor();

	public abstract License getLicence();

	public abstract String getYear();

	public abstract String getShortDescription();


	public ClassContainer getClasspathEntry()
	{
		for (ClassContainer f : ClassPath.retrieveSystemClassPath())
		{
			if (f.getFile().getName().startsWith(getApplicationName() + "-"))
			{
				return f;
			}
		}

		return null;
	}

}
