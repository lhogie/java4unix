package j4u;

import toools.io.file.Directory;

public abstract class Application {
	public final Directory directory = new Directory(Directory.getHomeDirectory(), ".java4unix");
	public final Directory dataDirectory = new Directory(directory, "data");
	public final Directory classesDirectory = new Directory(directory, "bin");

	public abstract String getAuthor();

	public abstract License getLicence();

	public abstract String getYear();

	public abstract String getVersion();

	public abstract String getName();

	protected abstract String getDescription();
}
