package j4u.demo;

import j4u.Command;
import j4u.CommandLine;
import j4u.CommandLineSpecification;

public class replace_filename extends Command {
	@Override
	public int runScript(CommandLine cmdLine) {
		String sep = getOptionValue(cmdLine, "-s");
		String prev = null;

		while (true) {
			String line = readUserInput("", ".*");

			if (line == null) {
				break;
			} else {
				if (prev != null) {
					printMessageWithNoNewLine(sep);
				}

				printMessageWithNoNewLine(line);
				prev = line;
			}
		}

		printMessage("");
		return 0;
	}

	@Override
	public String getDescription() {
		return "concatene the given lines";
	}

	@Override
	protected void specifyCmdLine(CommandLineSpecification spec) {
		spec.addOption("--separator", "-s", ".*", "", "separator");
	}

	public static void main(String[] args) {
		new replace_filename().run(args);
	}
}
