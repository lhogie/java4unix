package j4u.demo;

import j4u.Command;
import j4u.CommandLine;
import j4u.CommandLineSpecification;
import j4u.Utilities;
import toools.io.file.Directory;

public class list_installed_applications extends Command {

	@Override
	public String getDescription() {
		return "list java4unix applications on this system";
	}

	@Override
	public int runScript(CommandLine cmdLine) {
		for (Directory d : Utilities.findInstalledApplicationDirs()) {
			printMessage(d.getName().substring(1));
		}

		return 0;
	}

	@Override
	protected void specifyCmdLine(CommandLineSpecification spec) {
		// TODO Auto-generated method stub

	}
}
