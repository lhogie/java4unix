package j4u;

import java.util.Collection;
import java.util.HashSet;

import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.text.TextUtilities;
import toools.text.csv.CSV;

public class User {
	private static Collection<User> users;

	public static Collection<User> getUsers() {
		if (users == null) {
			users = new HashSet<User>();
			String text = TextUtilities.removeComments(new RegularFile("/etc/passwd").getContentAsText());

			CSV.disassemble(text, ":").forEach(line -> users.add(new User(Integer.valueOf(line.get(2)), line.get(0))));
		}

		return users;
	}

	private final int uid;
	private String name;
	private Directory homeDirectory;

	public User(int id, String name) {
		if (id < 0)
			throw new IllegalArgumentException();

		name = name.trim();

		if (name == null || name.length() == 0)
			throw new IllegalArgumentException();

		this.uid = id;
		this.name = name;
	}

	public int getUID() {
		return uid;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == getClass() && obj.hashCode() == hashCode();
	}

	@Override
	public int hashCode() {
		return getUID();
	}

	@Override
	public String toString() {
		return getName() + "(" + getUID() + ")";
	}

	public String getName() {
		return name;
	}

	public Directory getHomeDirectory() {
		return homeDirectory;
	}

	public static void main(String... string) {
		System.out.println(User.getUsers());
	}

	public static User getUserByName(String name) {
		for (User u : getUsers()) {
			if (u.getName().equals(name)) {
				return u;
			}
		}

		return null;
	}

	public boolean isAdmin() {
		return getName().equals("root");
	}

}
