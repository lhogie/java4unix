package j4u.demo;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import j4u.Command;
import j4u.CommandLine;
import j4u.CommandLineSpecification;
import toools.io.Utilities;
import toools.io.file.RegularFile;
import toools.io.ser.JavaSerializer;

public class consolidate_filename extends Command {
	private final Map<Character, String> m;
	private final RegularFile mapFile = getDataFile("map.txt");
	private final String allowedChars = "azertyuiopqsdfghjklmwxcvbnAZERTYUIOPQSDFGHJKLMWXCVBN1234567890 _-+=:.,()'[]{}&@'!#?$°";

	public consolidate_filename() {

		if (mapFile.exists()) {
			m = (Map<Character, String>) JavaSerializer.getDefaultSerializer().fromBytes(mapFile.getContent());
			printMessage(m);
		} else {
			m = new HashMap<>();
		}

	}

	@Override
	protected void specifyCmdLine(CommandLineSpecification spec) {
		spec.addOption("--yes", "-y", null, null, "assumes 'yes' to any question");
		spec.addOption("--spaces", "-s", null, null, "remove unecessary whitespaces");
	}

	@Override
	public String getDescription() {
		return "Replace text in filenames.";
	}

	@Override
	public int runScript(CommandLine cmdLine) throws IOException {
		List<Path> l = new ArrayList<>();

		Files.walkFileTree(Paths.get(System.getProperty("user.dir")), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path p, BasicFileAttributes attrs) {
				l.add(p);
				return FileVisitResult.CONTINUE;
			}
		});

		printMessage(l.size() + " files found");

		Collections.reverse(l);

		for (Path p : l) {
			File f = p.toFile();

			String oldName = f.getName();
			String newName = "";

			for (char c : oldName.toCharArray()) {
				if (allowedChars.indexOf(c) < 0) {
					printMessage("not allowed: " + c);

					if (m.containsKey(c)) {
						newName += m.get(c);
					} else {
						String s = Utilities
								.readUserInput("replace char '" + c + "' (" + (int) c + ") in '" + p + "' by?", ".*");
						m.put(c, s);
						saveMap();
						newName += s;
					}
				} else {
					newName += c;
				}
			}

			if (cmdLine.isOptionSpecified("--spaces")) {
				newName = newName.trim();
				newName = newName.replaceAll(" +", " ");
			}

			if (!newName.equals(oldName)) {
				printMessage("old: " + oldName);
				printMessage("new: " + newName);

				if (cmdLine.isOptionSpecified("-y") || Utilities.readUserInput("Ok?", "y|n").equals("y")) {
					boolean success = f.renameTo(new File(f.getParentFile(), newName));

					if (success) {
						// printMessage(oldName + " -> " + newName);
					} else {
						printFatalError(oldName + " -> " + newName);
						return 1;
					}
				}
			}

		}

		return 0;
	}

	private void saveMap() {
		mapFile.getParent().ensureExists();
		mapFile.setContent(JavaSerializer.getDefaultSerializer().toBytes(m));
	}

	public static void main(String[] args) throws Throwable {
		new consolidate_filename().run(args);
	}
}
