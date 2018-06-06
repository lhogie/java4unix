package j4u.chain;

import java.io.OutputStream;
import java.io.PrintStream;

import toools.io.Cout;
import toools.io.file.RegularFile;

public class ObjectPrinter implements TooolsPlugin<Object, Object>
{
	public RegularFile to;

	@Override
	public Object process(Object o)
	{
		if (to == null)
		{
			Cout.result(o);
		}
		else
		{

			OutputStream os = to.createWritingStream();
			PrintStream ps = new PrintStream(os);
			ps.print(o.toString());
			ps.close();

		}

		return o;
	}

	@Override
	public void setup(PluginConfig p)
	{
		if (p.contains("to"))
			to = new RegularFile(p.get("to"));
	}

}
