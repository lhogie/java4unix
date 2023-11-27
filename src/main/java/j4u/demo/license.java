package j4u.demo;

import j4u.Command;
import j4u.CommandLine;
import j4u.CommandLineSpecification;
import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;

public class license extends Command {
	public static void main(String[] args) {
		new license().run(args);
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable {
		RegularFile licenceFile = new RegularFile("license-header.txt");
		String header = new String(licenceFile.getContent());
		boolean add = getOptionValue(cmdLine, "-a").equals("add");

		for (AbstractFile f : Directory.getCurrentDirectory().retrieveTree()) {
			if (f.getName().endsWith(".java")) {
				RegularFile javaFile = (RegularFile) f;
				String sourceCode = new String(javaFile.getContent());
				boolean containsLicense = sourceCode.startsWith(header);

				if (add && !containsLicense) {
					printMessage("Adding license header to " + javaFile.getPath());
					javaFile.setContent((header + sourceCode).getBytes());
				} else if (!add && containsLicense) {
					printMessage("Removing license header from " + javaFile.getPath());
					javaFile.setContent(sourceCode.substring(header.length()).getBytes());
				}
			}
		}

		return 0;
	}

	@Override
	public String getDescription() {
		return "Adds/remove the given license to/from the source files in the current directory and its sub-directories. The licence"
				+ "is expected to be in a file called license-header.txt";
	}

	@Override
	protected void specifyCmdLine(CommandLineSpecification spec) {
		spec.addOption("--action", "-a", "add|remove", null, "add or remove license");
	}

}
