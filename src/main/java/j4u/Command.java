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

public abstract class Command {
	public final RegularFile callingScript;
	public final RegularFile configFile;
	private final List<String> vmOptions = new ArrayList<>();
	private JFrame frame;
	private JTextArea textArea;

	CommandLineSpecification cmdLineSpec = new CommandLineSpecification();
	private double verbosity = 0.5;
	private Configuration configuration;
	private Application app;

	public Command() {
		cmdLineSpec.addOption("--help", "-h", null, null, "print the help message");
		cmdLineSpec.addOption("--longhelp", null, null, null,
				"print the help message, showing additional utility options");
		cmdLineSpec.addOption("--version", null, null, null, "print version and copyright information");
		cmdLineSpec.addOption("--verbosity", null, "(0\\.[0-9]+)|1", "0.5",
				"set the verbosity level, which must be comprised between 0 and 1");
		cmdLineSpec.addOption("--Xgui", null, null, null,
				"use a window for user interaction, instead of stdin/stdout/stderr");
		cmdLineSpec.addOption("--Xprint-configuration", null, null, null, "print configuration file as is");
		cmdLineSpec.addOption("--Xgenerate-default-configuration", null, null, null,
				"generates a default configuration file");
		cmdLineSpec.addOption("--Xprint-source", null, null, null, "print the script source code, if available");
		cmdLineSpec.addOption("--Xprint-classpath", null, null, null, "print the classpath");
		cmdLineSpec.addOption("--Xprint-source-information", null, null, null,
				"print information about the script code");
		cmdLineSpec.addOption("--Xdebug", null, null, null, "print the full java stack when an error occurs");
		cmdLineSpec.addOption("--nbThreads", null, "[0-9]+", Runtime.getRuntime().availableProcessors(),
				"number of threads allowed to use");
		specifyCmdLine(cmdLineSpec);
		this.app = getApplication();
		this.callingScript = new RegularFile(app.directory, getName());
		this.configFile = new RegularFile(app.directory, getName() + ".conf");
	}

	protected abstract void specifyCmdLine(CommandLineSpecification spec);

	protected boolean isOptionSpecified(CommandLine cmdLine, String s) {
		return cmdLine.isOptionSpecified(s);
	}

	public List<String> getVMOptions() {
		return vmOptions;
	}

	public abstract String getDescription();

	public ClassContainer getClasspathEntry() {
		for (ClassContainer f : ClassPath.retrieveSystemClassPath()) {
			if (f.getFile().getName().startsWith(getApplication().getName() + "-")) {
				return f;
			}
		}

		return null;
	}

	public final String getUsage() {
		String s = getName() + " [OPTIONS]";

		for (ArgumentSpecification as : cmdLineSpec.argumentSpecifications) {
			s += ' ' + as.getAbbrv();
		}

		if (!cmdLineSpec.argumentSpecifications.isEmpty()) {
			s += "\n\nWhere:";

			for (ArgumentSpecification as : cmdLineSpec.argumentSpecifications) {
				s += "\n\t" + as.getAbbrv() + '\t' + as.getDescription();
			}
		}

		return s;
	}

	/**
	 * Computes the name of the script out of the name of the class For example,
	 * "lucci.cmdline.MyScript" becomes "MyScript".
	 * 
	 * @return
	 */
	public final String getName() {
		return getClass().getSimpleName();
	}

	public final String getHelp(boolean printUtilityOptions) {
		StringBuffer buf = new StringBuffer();
		buf.append("Usage: ");
		buf.append(getUsage());
		buf.append("\n\nDescription:\n\t");
		buf.append(getDescription());
		buf.append("\n\nOptions:");
		buf.append(cmdLineSpec.toString(printUtilityOptions));

		buf.append("\n\n");
		buf.append("Note: arguments are ordered, options are not.\n");

		if (configFile.exists()) {
			buf.append("Configuration file: " + configFile);
		}

		return buf.toString().replace("\t", "    ");
	}

	public void run(String... args) {
		// force windows jdk to adopt Cygwin's home
		System.setProperty("user.home", System.getenv("HOME"));
		var argsList = new ArrayList<>(List.of(args));

		// if at least one JAR is from outside the BIN dir, the script is assumed to be
		// run from the IDE
		boolean runFromIDE = ClassPath.retrieveSystemClassPath().stream()
				.anyMatch(cc -> !cc.getFile().isChildOf(app.classesDirectory));
		boolean xdebug = argsList.contains("--Xdebug");

		try {
			if (runFromIDE) {
				System.err.println(getClass().getName() + " is probably run from the IDE");
				System.err.println("writing files in " + app.directory);
				ClassPath nibcp = new ClassPath();
				ClassPath.retrieveSystemClassPath().stream().filter(cc -> !cc.getFile().isChildOf(app.classesDirectory))
						.forEach(cc -> nibcp.add(cc));

				nibcp.rsyncTo(app.classesDirectory, out -> stdout("rsync: " + out), err -> stdout("rsync: " + err));

				if (app.getLicence() != null) {
					new RegularFile(app.directory, "license.txt").setContentAsASCII(app.getLicence().getTerms());
				}

				new RegularFile(app.directory, "readme.txt").setContentAsASCII(app.getName() + " v" + app.getVersion()
						+ "\nCopyrighted: " + app.getAuthor() + " " + app.getYear() + "\n" + app.getDescription());
				callingScript.setContent(getLauncherCode().getBytes());
				callingScript.setExecutable(true);
				System.out.println(callingScript);
			}

			CommandLine cmdLine = new CommandLine(argsList, cmdLineSpec);
			int exitCode = run(cmdLine);
			System.exit(exitCode);
		} catch (Throwable t) {
			if (runFromIDE || xdebug || t.getMessage() == null || t.getMessage().trim().isEmpty()) {
				t.printStackTrace();
			} else {
				System.err.println("Failure! " + t.getMessage());
			}

			System.exit(1);
		}
	}

	protected void stdout(Object o) {
		System.out.println(o);
	}

	protected void stderr(Object o) {
		System.err.println(o);
	}

	/**
	 * Search for a class named "Application" in the package
	 */
	protected Application getApplication() {
		Class<Application> c = Clazz.findClass(getClass().getPackage().getName() + ".Application");

		if (c != null) {
			return Clazz.makeInstance(c);
		}

		return new Application() {

			@Override
			public String getYear() {
				return null;
			}

			@Override
			public String getVersion() {
				return null;
			}

			@Override
			public License getLicence() {
				return null;
			}

			@Override
			public String getAuthor() {
				return null;
			}

			@Override
			public String getName() {
				return Command.this.getClass().getPackageName();
			}

			@Override
			protected String getDescription() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	private final int run(CommandLine cmdLine) throws Throwable {
		// checkProgrammerData();

		loadConfiguration(cmdLine);

		int errorCode = 0;

		if (cmdLine.isOptionSpecified("--Xgui")) {
			this.frame = new JFrame(getName());
			this.textArea = new JTextArea();
			this.textArea.setEditable(false);
			this.frame.setContentPane(new JScrollPane(this.textArea));
			this.frame.setSize(800, 600);
			this.frame.setVisible(true);
		}

		if (cmdLine.isOptionSpecified("--help")) {
			printMessage(getHelp(false));
		} else if (cmdLine.isOptionSpecified("--longhelp")) {
			printMessage(getHelp(true));
		} else if (cmdLine.isOptionSpecified("--version")) {
			if (app.getVersion() != null)
				printMessage(getName() + " v" + app.getVersion());

			if (app.getYear() != null && app.getAuthor() != null)
				printMessage("Copyright(C) " + app.getYear() + " " + app.getAuthor());

			if (app.getLicence() == null)
				printMessage("License: " + app.getLicence());
			else
				printMessage("Unlicensed");
		} else if (cmdLine.isOptionSpecified("--Xprint-configuration")) {
			RegularFile file = configFile;

			if (file.exists()) {
				printMessage(file.getPath() + ":\n" + new String(file.getContent()));
			} else {
				printMessage("Configuration file " + file.getPath() + " does not exist");
			}
		} else if (cmdLine.isOptionSpecified("--Xgenerate-default-configuration")) {
			if (!configFile.exists()) {
				String s = "";

				for (OptionSpecification os : cmdLineSpec.getOptionSpecifications()) {
					if (os.getDefaultValue() != null) {
						s += "# " + os.getDescription() + "\n";
						s += "# default value is: " + os.getDefaultValue() + "\n";
						s += os.getLongName().substring(2) + "=" + os.getDefaultValue() + "\n\n";
					}
				}

				configFile.setContent(s.getBytes());
				printMessage("Configuration file created: " + configFile);
			} else {
				printWarning("configuration file " + configFile + " already exists.");
			}
		} else if (cmdLine.isOptionSpecified("--Xprint-source")) {
			String text = Source.getClassSourceCode(getClass());

			if (text == null) {
				printWarning("the source for class " + getClass().getName() + " is not available");
			} else {
				printMessage(text);
			}
		} else if (cmdLine.isOptionSpecified("--Xprint-classpath")) {
			for (ClassContainer f : ClassPath.retrieveSystemClassPath()) {
				printMessage(TextUtilities.flushLeft(f.getFile().getPath() + " ", 70, '.') + " "
						+ TextUtilities.toHumanString(f.getFile().getSize()) + "B");
			}
		} else if (cmdLine.isOptionSpecified("--Xprint-source-information")) {
			printMessage("Script implementation is in java class : " + getClass().getName());
			ClassPath urls = Clazz.findClassContainer(getClass().getName(), ClassPath.retrieveSystemClassPath());

			if (!urls.isEmpty()) {
				printMessage("This class is in directory: " + urls.get(0).getFile().getPath());
			}
		} else {
			// ensureMandatoryStuffAreSpecified(cmdLine);
			setVerbosity(Double.valueOf(getOptionValue(cmdLine, "--verbosity")));
			errorCode = runScript(cmdLine);
		}

		if (this.frame != null) {
			this.frame.setTitle("Terminated - " + this.frame.getTitle());
			final Thread thread = Thread.currentThread();
			this.frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					thread.resume();
				}
			});
			thread.suspend();
			this.frame.dispose();
		}

		return errorCode;
	}

	public User getUser() {
		return User.getUserByName(System.getProperty("user.name"));
	}

	private void loadConfiguration(CommandLine cmdLine)
			throws InvalidOptionValueException, UnsupportedOptionException, IOException {
		Configuration config = extractConfiguration();

		for (String k : config.getKeys()) {
			OptionSpecification os = cmdLineSpec.findOptionSpecification("--" + k);

			if (os == null) {
				throw new UnsupportedOptionException("invalid option in config file: " + k);
			} else {
				Option option = cmdLine.findOption("--" + k);

				if (option != null) {
					// if the option requires a value
					if (os.getValueRegexp() != null) {
						option.setValue(config.getValue(k));
					}
				}
			}
		}
	}

	public String getLauncherCode() {
		String t = "";
		t += "#!/bin/sh\n";
		t += "\n# this script was generated by Java4unix.";
		t += "\n# PLEASE DO NOT EDIT";
		t += "\n\n";
		t += "export CLASSPATH=" + app.classesDirectory.getPath() + ":$(ls " + app.classesDirectory.getPath()
				+ "/*.jar | tr '\\n' '" + System.getProperty("path.separator") + "')\n";
		t += "java " + TextUtilities.concatene(getVMOptions(), " ") + " " + getClass().getName() + " \"$@\"";
		t += "\n";
		return t;
	}

	public abstract int runScript(CommandLine cmdLine) throws Throwable;

	private void checkProgrammerData() {
		Collection<String> options = new HashSet<String>();

		for (OptionSpecification spec : cmdLineSpec.getOptionSpecifications()) {
			if (spec.getLongName() != null) {
				Assertions.ensure(!options.contains(spec.getLongName()), spec.getLongName() + " is already used");
				options.add(spec.getLongName());
			}

			if (spec.getShortName() != null) {
				Assertions.ensure(!options.contains(spec.getShortName()), spec.getShortName() + " is already used");
				options.add(spec.getShortName());
			}
		}
	}

	public CommandLineSpecification getCommandLineSpecification() {
		return cmdLineSpec;
	}

	protected void printMessage(PrintStream os, Object msg, double importance, boolean addnewline) {
		if (!(0 <= importance && importance <= 1))
			throw new IllegalArgumentException("message importance should be comprised between 0 and 1.");

		// if the msg is important enough to be printed
		if (importance >= 1 - getVerbosity()) {
			if (frame == null) {
				os.print(msg + (addnewline ? "\n" : ""));
			} else {
				this.textArea.append(msg + (addnewline ? "\n" : ""));
				this.textArea.setCaretPosition(textArea.getText().length());
			}
		}
	}

	private String lastcancellablePrint;

	protected void cancelLastPrint() {
		if (lastcancellablePrint == null) {
			printWarning("Messaging error: unable to cancel last print (none found)");
		} else {
			printMessageWithNoNewLine(TextUtilities.repeat(String.valueOf((char) 0x08), lastcancellablePrint.length()));
		}
	}

	protected final void printMessageWithNoNewLine(Object msg) {
		printMessage(System.out, msg, 0.5, false);
		this.lastcancellablePrint = msg.toString();
	}

	protected final synchronized void printMessage(PrintStream os, Object msg) {
		printMessage(os, msg, 0.5, true);
	}

	protected final void printMessage(Object... msg) {
		for (Object m : msg) {
			printMessage(System.out, m == null ? m : m.toString(), 0.5, true);
		}
	}

	protected final void printWarning(Object... msg) {
		for (Object m : msg) {
			printMessage(System.err, "Warning: " + m, 0.75, true);
		}
	}

	protected final void printDebugMessage(Object... msg) {
		for (Object m : msg) {
			printMessage(System.out, "#Debug: " + m, 0.75, true);

		}
	}

	protected final void printNonFatalError(Object... msg) {
		for (Object m : msg) {
			printMessage(System.err, "Error: " + m, 0.8, true);
		}
	}

	protected final void printFatalError(Object... msg) {
		for (Object m : msg) {
			printMessage(System.err, "Fatal error: " + m, 1, true);
		}
	}

	protected final void printExceptionStrackTrace(Exception e) {
		printMessage(System.err, "Java Exception: " + ExceptionUtilities.toString(e), 1, true);
	}

	BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	protected final String readUserInput(String invitation, String regexp) {
		if (this.frame == null) {
			try {
				String line = null;

				while (true) {
					if (invitation != null) {
						printMessageWithNoNewLine(invitation);
					}

					line = stdin.readLine();

					if (regexp == null || line == null || line.matches(regexp)) {
						return line;
					} else {
						printNonFatalError("Input error, the string must match " + regexp + "\n");
					}
				}
			} catch (IOException e) {
				printFatalError("Error while reading user input");
				return null;
			}
		} else {
			final String[] currentLine = new String[1];
			final Thread thread = Thread.currentThread();
			KeyListener listener = new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					if (e.getKeyChar() == '\n') {
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

	public final double getVerbosity() {
		return verbosity;
	}

	public final void setVerbosity(double verbosity) {
		Assertions.ensure(0 <= verbosity && verbosity <= 1, "script verbosity should be comprised between 0 and 1.");
		this.verbosity = verbosity;
	}

	public final Directory getHomeDirectory() {
		return Directory.getHomeDirectory();
	}

	public final int select(List<String> list) {
		int n = -1;

		while (n < 0 || n >= list.size()) {
			printMessage("\n");

			for (int i = 0; i < list.size(); ++i) {
				printMessage((i + 1) + ") " + list.get(i));
			}

			printMessage("\n");
			n = Integer.parseInt(readUserInput("Your choice: ", "[0-9]+")) - 1;
		}

		return n;
	}

	public final Directory getDataDirectory() {
		return app.dataDirectory;
	}

	public RegularFile getDataFile(String filename) {
		return app.dataDirectory.getChildRegularFile(filename);
	}

	public String getLastPrint() {
		return lastcancellablePrint;
	}

	protected Configuration extractConfiguration() throws IOException {

		if (configFile.exists()) {
			return Configuration.readFromFile(configFile);
		} else {
			return new Configuration();
		}

	}

	public void saveConfiguration(String key, String value) throws IOException {
		Configuration config = extractConfiguration();
		config.remove(key);
		config.add(key, value);

		if (!configFile.getParent().exists()) {
			configFile.getParent().mkdirs();
		}

		configuration.saveToFile(configFile);
	}

	public static String getOptionValue(CommandLine cmdLine, String optionName) {
		return cmdLine.getOptionValue(optionName);
	}

}
