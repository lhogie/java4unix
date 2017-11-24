package java4unix;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import toools.io.file.Directory;
import toools.io.file.RegularFile;

public class DeployUnixableJar
{

	public static void main(String... args) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException
	{
		// this is to force windows jdk to adopt Cygwin's home
		System.setProperty("user.home", System.getenv("HOME"));

		UnixableJar unixableJar = new UnixableJar(new RegularFile(args[0]));
		Directory targetDirectory = new Directory(args[1]);
		unixableJar.deployTo(targetDirectory, true);
	}
}
