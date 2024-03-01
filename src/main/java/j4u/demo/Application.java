package j4u.demo;

import j4u.License;

public abstract class Application extends j4u.Application {
	@Override
	public final String getAuthor() {
		return "Luc Hogie (CNRS)";
	}

	@Override
	public final License getLicence() {
		return License.ApacheV2;
	}

	@Override
	public final String getYear() {
		return "2008-2023";
	}

	@Override
	public String getVersion() {
		return ":)";
	}

	@Override
	public String getName() {
		return "java4unix";
	}
}
