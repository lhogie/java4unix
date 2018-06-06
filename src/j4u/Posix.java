package java4unix;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import toools.collections.relation.HashRelation;
import toools.collections.relation.Relation;
import toools.extern.Proces;
import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.os.OperatingSystem;
import toools.text.TextUtilities;

public class Posix
{
	public static Collection<AbstractFile> find(Directory file)
	{
		List<AbstractFile> files = new ArrayList<AbstractFile>();

		for (String filename : TextUtilities.splitInLines(new String(Proces.exec("find", file.getPath()))))
		{
			files.add(AbstractFile.map(filename, null));
		}

		return files;
	}

	public static Relation<Integer, File> findFileInodes(File searchRoot)
	{
		Relation<Integer, File> rel = new HashRelation<Integer, File>();

		for (String line : TextUtilities.splitInLines(new String(Proces.exec("find", searchRoot.getAbsolutePath(), "-type", "f", "-printf",
				"%i %p\\n"))))
		{
			int pos = line.indexOf(' ');
			int i = Integer.valueOf(line.substring(0, pos));
			File file = new File(line.substring(pos + 1));
			rel.add(i, file);
		}

		return rel;
	}

	public static String cat(RegularFile... file)
	{
		String s = "";

		for (RegularFile f : file)
		{
			s += new String(Proces.exec("cat", f.getPath()));
		}

		return s;
	}

	public static double getLoadAverage()
	{
		return  OperatingSystem.getLocalOperatingSystem().getLocalOperatingSystem().getLoadAverage();
	}

	public static void chmod(AbstractFile file, String mode)
	{
		Proces.exec("chmod", mode, file.getPath());
	}

	public static boolean isSymbolicLink(AbstractFile file)
	{
		return new String(Proces.exec("ls", "-l", file.getPath())).startsWith("l");
	}

	public static String getFileUser(AbstractFile file)
	{
		return new String(Proces.exec("ls", "-l", file.getPath())).split(" +")[2];
	}

	public static String getFileGroup(AbstractFile file)
	{
		return new String(Proces.exec("ls", "-l", file.getPath())).split(" +")[3];
	}

	public static String getFilePermissions(AbstractFile file)
	{
		return new String(Proces.exec("ls", "-l", file.getPath())).split(" +")[0];
	}

	public static void makeSymbolicLink(AbstractFile source, AbstractFile link)
	{
		Proces.exec("ln", "-s", source.getPath(), link.getPath());
	}

	public static AbstractFile getSymbolicLinkTarget(AbstractFile symlink)
	{
		String[] tokens = new String(Proces.exec("ls", "-l", symlink.getPath())).split(" +");

		if (tokens.length == 10)
		{
			return AbstractFile.map(tokens[9], null);
		}
		else
		{
			throw new IllegalArgumentException("file " + symlink.getPath() + " is not a symbolic link");
		}
	}

	public static void main(String... args)
	{
		System.out.println(getLoadAverage());
	}

}
