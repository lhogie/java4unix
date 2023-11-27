package j4u.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import j4u.Command;
import j4u.CommandLine;
import j4u.CommandLineSpecification;
import toools.io.FileUtilities;

public class find_common_files extends Command {

	@Override
	public int runScript(CommandLine cmdLine) {
		List<String> args = cmdLine.findParameters();
		List<File> l = new ArrayList<File>();

		for (String a : args) {
			l.add(new File(a));
		}

		for (String filename : FileUtilities.computeCommonFiles(l.toArray(new File[0]))) {
			printMessage(filename);
		}

		return 0;
	}

	@Override
	public String getDescription() {
		return "Find common files in the given directories";
	}

	@Override
	protected void specifyCmdLine(CommandLineSpecification spec) {
		// TODO Auto-generated method stub
		
	}

}
