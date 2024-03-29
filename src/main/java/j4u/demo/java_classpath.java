package j4u.demo;

import java.util.Collection;
import java.util.HashSet;

import j4u.Command;
import j4u.CommandLine;
import j4u.CommandLineSpecification;
import toools.collections.relation.HashRelation;
import toools.collections.relation.Relation;
import toools.io.FileUtilities;
import toools.io.file.AbstractFile;
import toools.io.file.RegularFile;
import toools.reflect.ClassContainer;
import toools.reflect.ClassPath;

public class java_classpath extends Command {

	public static void main(String[] args) {
		new java_classpath().run( args);
	}

	@Override
	public String getDescription() {
		return "Check classpath";
	}

	@Override
	public int runScript(CommandLine cmdLine) {
		ClassPath classpath = ClassPath.retrieveSystemClassPath();

		if (isOptionSpecified(cmdLine, "--list-classes")) {
			listClasses(classpath);
		} else if (isOptionSpecified(cmdLine, "--list-entries")) {
			listEntries(classpath);
		} else if (cmdLine.findOptions().isEmpty()) {
			// if no options were given
			checkNonExistingEntries(classpath);
			checkConflictingEntries(classpath);
		}

		return 0;
	}

	private void listEntries(ClassPath classpath) {
		for (ClassContainer f : classpath) {
			printMessage(f.getFile().getPath());
		}
	}

	private void checkNonExistingEntries(ClassPath classpath) {
		for (ClassContainer e : classpath) {
			AbstractFile f = e.getFile();

			if (!f.exists()) {
				printNonFatalError("entry does not exist: " + f.getPath());
			} else if (!f.canRead()) {
				printNonFatalError("entry is not readable: " + f.getPath());
			}
		}
	}

	/**
	 * Checks if this classpath defines two entries with the same name but different
	 * content
	 * 
	 * @param classpath
	 */
	private void checkConflictingEntries(ClassPath classpath) {
		Relation<String, ClassContainer> r = new HashRelation<String, ClassContainer>();

		for (ClassContainer u : classpath) {
			// if the entry is a zip or a jar
			if (u.getFile() instanceof RegularFile) {
				r.add(u.getFile().getName(), u);
			}
		}

		for (String e : r.keySet()) {
			Collection<RegularFile> files = new HashSet<RegularFile>();

			for (ClassContainer thisEntry : new HashSet<ClassContainer>(r.getValues(e))) {
				files.add((RegularFile) thisEntry.getFile());
			}

			if (!FileUtilities.ensureSameFile(files)) {
				printNonFatalError("Entry " + e + " refers to conflicting files: " + files);
			}
		}

	}

	private void listClasses(ClassPath classpath) {
		for (Class<?> c : classpath.listAllClasses()) {
			printMessage(c.getName());
		}
	}

	@Override
	protected void specifyCmdLine(CommandLineSpecification spec) {
		spec.addOption("--list-classes", "-c", null, null, "list all classes");
		spec.addOption("--list-entries", "-e", null, null, "list all entries");
		
	}

}
