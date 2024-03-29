package j4u;

import java.io.IOException;

import toools.net.NetUtilities;

public class License {
	public final static License GPL = new License("GPL", "http://www.gnu.org/licenses/gpl.txt");
	public final static License LGPL = new License("LGPL", "http://www.gnu.org/licenses/lgpl-2.1.txt");
	public final static License ApacheV2 = new License("ApacheV2", null);

	private String name;
	private String url;
	private String terms;

	public License(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getTerms() {
		if (this.terms == null) {
			try {
				this.terms = new String(NetUtilities.retrieveURLContentAsString(url));
			} catch (IOException e) {
				return null;
			}
		}

		return this.terms;
	}

	@Override
	public String toString() {
		return getName();
	}
}
