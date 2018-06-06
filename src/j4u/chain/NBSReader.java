package j4u.chain;

import toools.io.Cout;
import toools.io.file.nbs.NBSFile;

public class NBSReader implements TooolsPlugin<Void, long[]>
{
	public NBSFile f;

	@Override
	public long[] process(Void g)
	{
		Cout.result("encoding: " + f.readEncoding() + " bytes");
		return f.readValues(8);
	}

	@Override
	public void setup(PluginConfig p)
	{

	}

}
