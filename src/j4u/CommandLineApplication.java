package j4u;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import toools.config.Configuration;
import toools.exceptions.ExceptionUtilities;
import toools.gui.Utilities;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.reflect.ClassContainer;
import toools.reflect.ClassPath;
import toools.reflect.Clazz;
import toools.src.Source;
import toools.text.TextUtilities;
import toools.util.assertion.Assertions;

public abstract class CommandLineApplication extends Application
{
	private final Directory installationDirectory;
	private final Directory userApplicationDirectory;
	private final Directory dataDirectory;
	private final RegularFile launcher;

	CommandLineSpecification cmdLineSpec = new CommandLineSpecification();
	private double verbosity = 0.5;
	private Configuration configuration;
	private boolean interactive;

	public CommandLineApplication(RegularFile launcher)
	{
		addOption("--help", "-h", null, null, "print the help message");
		addOption("--longhelp", null, null, null,
				"print the help message, showing additional utility options");
		addOption("--version", null, null, null,
				"print version and copyright information");
		addOption("--verbosity", null, "(0\\.[0-9]+)|1", "0.5",
				"set the verbosity level, which must be comprised between 0 and 1");
		addOption("--Xgui", null, null, null,
				"use a window for user interaction, instead of stdin/stdout/stderr");
		addOption("--Xprint-configuration", null, null, null,
				"print configuration file as is");
		addOption("--Xgenerate-default-configuration", null, null, null,
				"generates a default configuration file");
		addOption("--Xprint-source", null, null, null,
				"print the script source code, if available");
		addOption("--Xprint-classpath", null, null, null, "print the classpath");
		addOption("--Xprint-source-information", null, null, null,
				"print information about the script code");
		addOption("--Xdebug", null, null, null,
				"print the full java stack when an error occurs");
		this.launcher = launcher;
		this.installationDirectory = launcher == null ? null
				: launcher.getParent().getParent();
		this.userApplicationDirectory = Directory.getHomeDirectory()
				.getChildDirectory("." + getApplicationName());
		this.dataDirectory = userApplicationDirectory.getChildDirectory("data");
	}

	public Directory getInstallationDirectory()
	{
		return installationDirectory;
	}

	public void addOption(String longName, String shortName, String regex,
			Object defaultValue, String description)
	{
		OptionSpecification alreadyExisting = cmdLineSpec
				.getOptionSpecification(longName);

		if (alreadyExisting != null)
			throw new IllegalArgumentException(
					longName + "' already belongs to " + alreadyExisting);

		alreadyExisting = cmdLineSpec.getOptionSpecification(shortName);

		if (alreadyExisting != null)
			throw new IllegalArgumentException(
					shortName + "' already belongs to " + alreadyExisting);

		cmdLineSpec.getOptionSpecifications().add(new OptionSpecification(longName,
				shortName, regex, defaultValue, description));
	}

	public final String getUsage()
	{
		String s = getName() + " [OPTIONS]";

		for (ArgumentSpecification as : cmdLineSpec.argumentSpecifications)
		{
			s += ' ' + as.getAbbrv();
		}

		if ( ! cmdLineSpec.argumentSpecifications.isEmpty())
		{
			s += "\n\nWhere:";

			for (ArgumentSpecification as : cmdLineSpec.argumentSpecifications)
			{
				s += "\n\t" + as.getAbbrv() + '\t' + as.getDescription();
			}
		}

		return s;
	}

	synchronized protected final boolean isOptionSpecified(CommandLine cmdLine,
			String optionName)
	{
		OptionSpecification spec = cmdLineSpec.getOptionSpecification(optionName);

		if (spec == null)
			throw new IllegalArgumentException("no such option: " + optionName);

		return cmdLine.findOption(spec).isSpecifiedOnTheCommandLine();
	}

	/**
	 * Computes the name of the script out of the name of the class For example,
	 * "lucci.cmdline.MyScript" becomes "MyScript".
	 * 
	 * @return
	 */
	public final String getName()
	{
		return computeFileName(getApplicationName(), getClass());
	}

	public static String computeFileName(String appName,
			Class<? extends CommandLineApplication> c)
	{
		String s = c.getName();

		// removes the package information
		s = s.substring(s.lastIndexOf('.') + 1);

		// prefix by the name of the application
		s = appName + '-' + s;

		s = s.replace('_', '-');

		// all in lower case
		s = s.toLowerCase();
		return s;
	}

	public final String getHelp(boolean printUtilityOptions)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Usage: ");
		buf.append(getUsage());
		buf.append("\n\nDescription:\n\t");
		buf.append(getShortDescription());
		buf.append("\n\nOptions:");
		buf.append(cmdLineSpec.toString(printUtilityOptions));

		buf.append("\n\n");
		buf.append("Note: arguments are ordered, options are not.\n");

		if (getActualConfigurationFile() != null && getActualConfigurationFile().exists())
		{
			buf.append("Configuration file: " + getActualConfigurationFile().getPath()
					+ " (" + getActualConfigurationFile().getSize() + " bytes)");
		}

		return buf.toString().replace("\t", "    ");
	}

	public final int run(String... args) throws Throwable
	{
		return run(new ArrayList<>(Arrays.asList(args)));
	}

	public final int run(List<String> args) throws Throwable
	{
		checkProgrammerData();
		CommandLine cmdLine = new CommandLine(args, cmdLineSpec);

		if (launcher != null)
		{
			loadConfiguration(cmdLine);
		}

		int errorCode = 0;

		if (isOptionSpecified(cmdLine, "--Xgui"))
		{
			this.frame = new JFrame(getName());
			this.textArea = new JTextArea();
			this.textArea.setEditable(false);
			this.frame.setContentPane(new JScrollPane(this.textArea));
			this.frame.setSize(800, 600);
			this.frame.setVisible(true);
		}

		if (isOptionSpecified(cmdLine, "--help"))
		{
			printMessage(getHelp(false));
		}
		else if (isOptionSpecified(cmdLine, "--longhelp"))
		{
			printMessage(getHelp(true));
		}
		else if (isOptionSpecified(cmdLine, "--version"))
		{
			printMessage(getName() + " v" + getVersion());
			printMessage("Copyright(C) " + getYear() + " " + getAuthor());
			printMessage("License: " + getLicence().getName());
		}
		else if (isOptionSpecified(cmdLine, "--Xprint-configuration"))
		{
			RegularFile file = getActualConfigurationFile();

			if (file.exists())
			{
				printMessage(file.getPath() + ":\n" + new String(file.getContent()));
			}
			else
			{
				printMessage("Configuration file " + file.getPath() + " does not exist");
			}
		}
		else if (isOptionSpecified(cmdLine, "--Xgenerate-default-configuration"))
		{
			if ( ! getUserConfigurationFile().exists())
			{
				String s = "";

				for (OptionSpecification os : cmdLineSpec.getOptionSpecifications())
				{
					if (os.getDefaultValue() != null)
					{
						s += "# " + os.getDescription() + "\n";
						s += "# default value is: " + os.getDefaultValue() + "\n";
						s += os.getLongName().substring(2) + "=" + os.getDefaultValue()
								+ "\n\n";
					}
				}

				getUserConfigurationFile().setContent(s.getBytes());
				printMessage("Configuration file created: "
						+ getUserConfigurationFile().getPath());
			}
			else
			{
				printWarning("configuration file " + getUserConfigurationFile().getPath()
						+ " already exists.");
			}
		}
		else if (isOptionSpecified(cmdLine, "--Xprint-source"))
		{
			String text = Source.getClassSourceCode(getClass());

			if (text == null)
			{
				printWarning("the source for class " + getClass().getName()
						+ " is not available");
			}
			else
			{
				printMessage(text);
			}
		}
		else if (isOptionSpecified(cmdLine, "--Xprint-classpath"))
		{
			for (ClassContainer f : ClassPath.retrieveSystemClassPath())
			{
				printMessage(TextUtilities.flushLeft(f.getFile().getPath() + " ", 70, '.')
						+ " " + f.getFile().getSize() + " bytes");
			}
		}
		else if (isOptionSpecified(cmdLine, "--Xprint-source-information"))
		{
			printMessage(
					"Script implementation is in java class : " + getClass().getName());
			ClassPath urls = Clazz.findClassContainer(getClass().getName(),
					ClassPath.retrieveSystemClassPath());

			if ( ! urls.isEmpty())
			{
				printMessage(
						"This class is in directory: " + urls.get(0).getFile().getPath());
			}
		}
		else
		{
			// ensureMandatoryStuffAreSpecified(cmdLine);
			setVerbosity(Double.valueOf(getOptionValue(cmdLine, "--verbosity")));
			errorCode = runScript(cmdLine);
		}

		if (this.frame != null)
		{
			this.frame.setTitle("Terminated - " + this.frame.getTitle());
			final Thread thread = Thread.currentThread();
			this.frame.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					thread.resume();
				}
			});
			thread.suspend();
			this.frame.dispose();
		}

		return errorCode;
	}

	private boolean isRunAsRoot()
	{
		return getUser().getName().equals("root");
	}

	private User getUser()
	{
		return User.getUserByName(System.getProperty("user.name"));
	}

	private void loadConfiguration(CommandLine cmdLine)
			throws InvalidOptionValueException, UnsupportedOptionException, IOException
	{
		Configuration config = extractConfiguration();

		for (String k : config.getKeys())
		{
			OptionSpecification os = cmdLineSpec.getOptionSpecification("--" + k);

			if (os == null)
			{
				throw new UnsupportedOptionException(
						"invalid option in config file: " + k);
			}
			else
			{
				Option option = cmdLine.findOption(os);

				if ( ! option.isSpecifiedOnTheCommandLine())
				{
					// if the option requires a value
					if (os.getValueRegexp() != null)
					{
						option.setValue(config.getValue(k));
					}
				}
			}
		}
	}

	public final Directory getJarsDirectory()
	{
		return new Directory(getInstallationDirectory(), "jars");
	}

	public final void createLauncher() throws IOException
	{
		if ( ! launcher.getParent().exists())
			launcher.getParent().mkdirs();

		launcher.setContent(getLauncherCode().getBytes());
		Posix.chmod(launcher, "+x");
	}

	public String getLauncherCode()
	{
		String t = "";
		t += "#!/bin/sh\n";
		t += "\n# this script was generated by Java4unix.";
		t += "\n# DO NOT EDIT.";
		t += "\n\n";
		t += "export CLASSPATH=$(find '" + getJarsDirectory().getPath()
				+ "' -name '*.jar' | tr '\\n' '" + System.getProperty("path.separator")
				+ "')\n";
		t += "java " + TextUtilities.concatene(getVMOptions(), " ") + " "
				+ Run.class.getName() + " " + getClass().getName() + " \"$0\" \"$@\"";
		return t;
	}

	public abstract int runScript(CommandLine cmdLine) throws Throwable;

	private void checkProgrammerData()
	{
		Assertions.ensure(getAuthor() != null && ! getAuthor().trim().isEmpty(),
				"author not set: " + getAuthor());
		Assertions.ensure(
				getShortDescription() != null && ! getShortDescription().trim().isEmpty(),
				"description not set: " + getShortDescription());
		Assertions.ensure(getVersion() != null && ! getVersion().trim().isEmpty(),
				"invalid version: " + getVersion());
		Assertions.ensure(getYear() != null && ! getYear().trim().isEmpty(),
				"invalid date: " + getYear());
		Assertions.ensure(getLicence() != null, "licence not set: " + getLicence());
		Assertions.ensure(getApplicationName() != null, "application name not set");

		Collection<String> options = new HashSet<String>();

		for (OptionSpecification spec : cmdLineSpec.getOptionSpecifications())
		{
			if (spec.getLongName() != null)
			{
				Assertions.ensure( ! options.contains(spec.getLongName()),
						spec.getLongName() + " is already used");
				options.add(spec.getLongName());
			}

			if (spec.getShortName() != null)
			{
				Assertions.ensure( ! options.contains(spec.getShortName()),
						spec.getShortName() + " is already used");
				options.add(spec.getShortName());
			}
		}
	}

	public CommandLineSpecification getCommandLineSpecification()
	{
		return cmdLineSpec;
	}

	public final void ensureMandatoryStuffAreSpecified(CommandLine cmdLine)
			throws InvalidOptionValueException
	{
		for (OptionSpecification spec : cmdLineSpec.getOptionSpecifications())
		{
			// the option waits a value and no default value if available
			if (spec.getValueRegexp() != null && spec.getDefaultValue() == null)
			{
				Option o = cmdLine.findOption(spec);

				if ( ! o.isSpecifiedOnTheCommandLine())
				{
					throw new InvalidOptionValueException(
							"option " + spec.getLongName() + " is not specified");
				}
				else if (o.getValue() == null)
				{
					throw new InvalidOptionValueException("option " + spec.getLongName()
							+ " has no value. It must be given a value matching "
							+ spec.getValueRegexp());
				}
			}
		}
	}

	private JFrame frame;

	private JTextArea textArea;

	protected void printMessage(PrintStream os, Object msg, double importance,
			boolean addnewline)
	{
		if ( ! (0 <= importance && importance <= 1))
			throw new IllegalArgumentException(
					"message importance should be comprised between 0 and 1.");

		// if the msg is important enough to be printed
		if (importance >= 1 - getVerbosity())
		{
			if (frame == null)
			{
				os.print(msg + (addnewline ? "\n" : ""));
			}
			else
			{
				this.textArea.append(msg + (addnewline ? "\n" : ""));
				this.textArea.setCaretPosition(textArea.getText().length());
			}
		}
	}

	private String lastcancellablePrint;

	protected void cancelLastPrint()
	{
		if (lastcancellablePrint == null)
		{
			printWarning("Messaging error: unable to cancel last print (none found)");
		}
		else
		{
			printMessageWithNoNewLine(TextUtilities.repeat(String.valueOf((char) 0x08),
					lastcancellablePrint.length()));
		}
	}

	protected final void printMessageWithNoNewLine(Object msg)
	{
		printMessage(System.out, msg, 0.5, false);
		this.lastcancellablePrint = msg.toString();
	}

	protected final synchronized void printMessage(PrintStream os, Object msg)
	{
		printMessage(os, msg, 0.5, true);
	}

	protected final void printMessage(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.out, m == null ? m : m.toString(), 0.5, true);
		}
	}

	protected final void printWarning(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.err, "Warning: " + m, 0.75, true);
		}
	}

	protected final void printDebugMessage(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.out, "#Debug: " + m, 0.75, true);

		}
	}

	protected final void printNonFatalError(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.err, "Error: " + m, 0.8, true);
		}
	}

	protected final void printFatalError(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.err, "Fatal error: " + m, 1, true);
		}
	}

	protected final void printExceptionStrackTrace(Exception e)
	{
		printMessage(System.err, "Java Exception: " + ExceptionUtilities.toString(e), 1,
				true);
	}

	BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	protected final String readUserInput(String invitation, String regexp)
	{
		if (this.frame == null)
		{
			try
			{
				String line = null;

				while (true)
				{
					if (invitation != null)
					{
						printMessageWithNoNewLine(invitation);
					}

					line = stdin.readLine();

					if (regexp == null || line == null || line.matches(regexp))
					{
						return line;
					}
					else
					{
						printNonFatalError(
								"Input error, the string must match " + regexp + "\n");
					}
				}
			}
			catch (IOException e)
			{
				printFatalError("Error while reading user input");
				return null;
			}
		}
		else
		{
			final String[] currentLine = new String[1];
			final Thread thread = Thread.currentThread();
			KeyListener listener = new KeyAdapter()
			{
				public void keyTyped(KeyEvent e)
				{
					if (e.getKeyChar() == '\n')
					{
						textArea.setCaretPosition(textArea.getCaretPosition() - 1);
						currentLine[0] = Utilities.getCurrentLine(textArea);
						thread.resume();
					}
				}

			};

			this.textArea.addKeyListener(listener);
			this.textArea.setEditable(true);
			thread.suspend();
			this.textArea.setEditable(false);
			this.textArea.removeKeyListener(listener);
			return currentLine[0];
		}
	}

	public final double getVerbosity()
	{
		return verbosity;
	}

	public final void setVerbosity(double verbosity)
	{
		Assertions.ensure(0 <= verbosity && verbosity <= 1,
				"script verbosity should be comprised between 0 and 1.");
		this.verbosity = verbosity;
	}

	public final Directory getHomeDirectory()
	{
		return Directory.getHomeDirectory();
	}

	public final int select(List<String> list)
	{
		int n = - 1;

		while (n < 0 || n >= list.size())
		{
			printMessage("\n");

			for (int i = 0; i < list.size(); ++i)
			{
				printMessage((i + 1) + ") " + list.get(i));
			}

			printMessage("\n");
			n = Integer.parseInt(readUserInput("Your choice: ", "[0-9]+")) - 1;
		}

		return n;
	}

	public final Directory getDataDirectory()
	{
		return dataDirectory;
	}

	public final RegularFile getDefaultConfigurationFile()
	{
		// if the application is not run via a shell script
		// there is no installation directory
		if (getInstallationDirectory() == null)
		{
			return null;
		}
		else
		{
			return new RegularFile(getInstallationDirectory(), getName() + ".conf");
		}
	}

	public final RegularFile getUserConfigurationFile()
	{
		return getUserFile(getName() + ".conf");
	}

	public Directory getUserApplicationDirectory()
	{
		return userApplicationDirectory;
	}
	
	public RegularFile getUserFile(String filename)
	{
		return userApplicationDirectory.getChildRegularFile(filename);
	}

	public final RegularFile getActualConfigurationFile()
	{
		RegularFile userConfigFile = getUserConfigurationFile();

		if (userConfigFile != null && userConfigFile.exists())
		{
			return userConfigFile;
		}
		else
		{
			RegularFile defaultConfigFile = getDefaultConfigurationFile();
			return defaultConfigFile != null && defaultConfigFile.exists()
					? defaultConfigFile
					: userConfigFile;
		}
	}

	/*
	
	*/
	public RegularFile findFile(String name)
	{
		List<Directory> searchDirectories = new ArrayList<>(
				Arrays.asList(userApplicationDirectory, installationDirectory));

		for (Directory d : searchDirectories)
		{
			RegularFile f = new RegularFile(d, name);

			if ( ! f.getParent().exists())
			{
				f.getParent().mkdirs();
			}

			return f;
		}

		return searchDirectories.get(0).getChildRegularFile(name);
	}

	public String getLastPrint()
	{
		return lastcancellablePrint;
	}

	protected Configuration extractConfiguration() throws IOException
	{
		RegularFile configFile = getActualConfigurationFile();

		if (configFile.exists())
		{
			return Configuration.readFromFile(getActualConfigurationFile());
		}
		else
		{
			return new Configuration();
		}

	}

	public void saveConfiguration(String key, String value) throws IOException
	{
		Configuration config = extractConfiguration();
		config.remove(key);
		config.add(key, value);

		RegularFile configFile = getUserConfigurationFile();

		if ( ! configFile.getParent().exists())
		{
			configFile.getParent().mkdirs();
		}

		configuration.saveToFile(configFile);
	}

	public boolean isInteractive()
	{
		return interactive;
	}

	public void setInteractive(boolean interactive)
	{
		this.interactive = interactive;
	}

	protected RegularFile getDataFile(String filename)
	{
		return dataDirectory.getChildRegularFile(filename);
	}

	public String getOptionValue(CommandLine cmdLine, String optionName)
	{
		OptionSpecification spec = cmdLineSpec.getOptionSpecification(optionName);

		if (spec == null)
			throw new IllegalArgumentException("option does not exist: " + optionName);

		Option o = cmdLine.findOption(spec);

		if (spec.getValueRegexp() == null)
			throw new IllegalStateException(
					"option " + optionName + " cannot have any value");

		String value = o.getValue();

		if (value != null)
			return o.getValue();

		if (interactive)
			return toools.io.Utilities.readUserInput(
					"Please enter value for '" + optionName + "': ",
					spec.getValueRegexp());

		throw new InvalidOptionValueException("option " + spec.getLongName()
				+ " has no value. It must be given a value matching "
				+ spec.getValueRegexp());
	}
}
