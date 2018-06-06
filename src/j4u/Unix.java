package j4u;

import java.io.File;
import java.util.Collection;

public class Unix
{
	private static final Unix localUnix = new Unix();

	public boolean isMacOSX()
	{
		return new File("/Users").isDirectory();
	}

	public boolean isLinux()
	{
		return new File("/proc").isDirectory();
	}

	public boolean isCygwin()
	{
		return new File("/cygdrive").isDirectory();
	}

	public Collection<User> getUsers()
	{
		return User.getUsers();
	}

	//
	// private Collection<Group> groups = new HashSet<Group>();
	//
	// public Collection<Group> getGroups()
	// {
	// try
	// {
	// for (String name :
	// Collections.getElementsAt(split(FileUtilities.getFileLines(new
	// File("/etc/group"))), 0))
	// {
	// Group g = new Group();
	// g.set
	// groups.add();
	// }
	//
	// return groups;
	// }
	// catch (IOException e)
	// {
	// throw new IllegalStateException();
	// }
	//
	// }
	// public Collection<Filesystem> getFilesystems()
	// {
	// return Filesystem.find();
	// }

}
