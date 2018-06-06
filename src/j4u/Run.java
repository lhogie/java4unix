package j4u;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import toools.io.file.RegularFile;

public class Run
{
	public static void main(String[] args)
	{
		// force windows jdk to adopt Cygwin's home
		System.setProperty("user.home", System.getenv("HOME"));

		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		try
		{
			int returnCode = main(arguments);
			System.exit(returnCode);
		}
		catch (Throwable t)
		{
			if (arguments.contains("--Xdebug") || t.getMessage() == null
					|| t.getMessage().trim().isEmpty())
			{
				t.printStackTrace();

				if (t.getCause() != null)
					t.getCause().printStackTrace();
			}
			else
			{
				System.err.println("Failure! " + t.getMessage());
			}

			System.exit(1);
		}
	}

	public static int main(List<String> arguments) throws Throwable
	{
		if (arguments.size() < 2)
		{
			System.err.println("Usage: java " + Run.class.getName()
					+ " appClass triggerFile [arg]...");
			return 1;
		}
		else
		{
			String applicationClassName = arguments.remove(0);
			Class clazz = Class.forName(applicationClassName);
			String triggerFilename = arguments.remove(0);

			RegularFile triggerFile = triggerFilename.trim().isEmpty() ? null
					: new RegularFile(triggerFilename);

			Constructor constructor = clazz
					.getConstructor(new Class[] { RegularFile.class });
			CommandLineApplication script = (CommandLineApplication) constructor
					.newInstance(triggerFile);
			return script.run(arguments);
		}
	}
}
