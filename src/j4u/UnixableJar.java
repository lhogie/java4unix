package j4u;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.reflect.ClassContainer;
import toools.reflect.Clazz;

public class UnixableJar
{
	private final AbstractFile file;
	private String applicationName;

	public UnixableJar(AbstractFile jarfile)
	{
		this(jarfile, guessApplicationName(jarfile.getName()));
	}

	public UnixableJar(AbstractFile jarfile, String applicationName)
	{
		this.file = jarfile;
		this.applicationName = applicationName;
	}

	public static String guessApplicationName(String filename)
	{
		return filename.replaceAll("-.*", "");
	}

	public void deployTo(Directory baseDir, boolean verbose)
			throws IOException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, SecurityException
	{
		if ( ! baseDir.exists())
			baseDir.mkdirs();

		if (verbose)
			System.out.println("Scanning " + file);

		// ask the user if he wants something else
		System.out.println(
				"Application name (optional, found '" + applicationName + "'): ");
		String appNameGivenByTheUser = new Scanner(System.in).nextLine();

		if (appNameGivenByTheUser != null && appNameGivenByTheUser.length() > 0)
		{
			applicationName = appNameGivenByTheUser;
		}

		// search for the classes that correspond to scripts
		System.out.println("searching scripts for application " + applicationName);
		Collection<Class<CommandLineApplication>> unixableClasses = findScriptClasses();

		for (Class<CommandLineApplication> thisClass : unixableClasses)
		{
			RegularFile f = new RegularFile(baseDir, applicationName + "/bin/"
					+ CommandLineApplication.computeFileName(applicationName, thisClass));
			CommandLineApplication unixableClass = (CommandLineApplication) thisClass
					.getConstructors()[0].newInstance(f);

			if ( ! unixableClass.getApplicationName().equals(applicationName))
				throw new IllegalStateException("Cmd "
						+ unixableClass.getClass().getName() + " belongs to application "
						+ unixableClass.getApplicationName() + " instead of "
						+ applicationName + ". Skipping it.");

			if (verbose)
				System.out.println("creating " + f.getPath());

			unixableClass.createLauncher();
		}
	}

	public Collection<Class<CommandLineApplication>> findScriptClasses()
	{
		Collection<Class<CommandLineApplication>> scriptClasses = new HashSet<Class<CommandLineApplication>>();

		for (Class<?> thisClass : new ClassContainer(file).listAllClasses())
		{
			if ( ! Clazz.isAbstract(thisClass)
					&& CommandLineApplication.class.isAssignableFrom(thisClass))
			{
				scriptClasses.add((Class<CommandLineApplication>) thisClass);
			}
		}

		return scriptClasses;
	}
}
