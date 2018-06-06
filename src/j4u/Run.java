package java4unix;

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
			// if there is no message
			if (arguments.contains("--Xdebug") || t.getMessage() == null
					|| t.getMessage().trim().isEmpty())
			{
				t.printStackTrace();
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
		if (arguments.size() == 0)
		{
			System.err.println("Usage: java " + Run.class.getName()
					+ " appClass triggerFile [arg1] [arg2] ... [argN]");
			return 1;
		}
		else
		{
			String applicationClassName = arguments.remove(0);
			Class clazz = Class.forName(applicationClassName);
			RegularFile triggerFile = new RegularFile(arguments.remove(0));
			CommandLineApplication script = (CommandLineApplication) clazz
					.getConstructors()[0].newInstance(triggerFile);
			return script.run(arguments);
		}
	}
}
