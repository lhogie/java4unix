package j4u;

import java.util.Collection;
import java.util.HashSet;

import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;

public class Utilities {

	public static Collection<Directory> findInstalledApplicationDirs() {
		Collection<Directory> s = new HashSet<Directory>();

		for (AbstractFile c : Directory.getHomeDirectory().findChildFilesWhoseTheNameMatches("^\\..*$")) {
			if (c instanceof Directory) {
				Directory d = (Directory) c;

				if (new RegularFile(d, ".java4unix").exists()) {
					s.add(d);
				}
			}
		}

		return s;
	}

}
