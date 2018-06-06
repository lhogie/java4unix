package j4u.chain;

import java.util.ArrayList;
import java.util.List;

import j4u.CommandLine;
import j4u.CommandLineApplication;
import toools.io.Cout;
import toools.io.file.RegularFile;
import toools.text.TextUtilities;

public abstract class Run extends CommandLineApplication
{

	public Run(RegularFile launcher)
	{
		super(launcher);
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{
		List<TooolsPlugin> plugins = toPlugins(cmdLine.findParameters());

		TooolsPlugin reader = plugins.get(0);
		Cout.info(TextUtilities.box("executing plugin: " + reader.getClass()));

		Object o = reader.process(null);

		for (int i = 1; i < cmdLine.findParameters().size(); ++i)
		{
			String arg = cmdLine.findParameters().get(i);
			TooolsPlugin plugin = plugins.get(i);
			Cout.info(TextUtilities.box("executing plugin: " + plugin.getClass()));
			o = plugin.process(o);
		}

		Cout.result(o);
		return 0;
	}

	List<TooolsPlugin> toPlugins(List<String> parms)
	{
		List<TooolsPlugin> r = new ArrayList<>();

		for (int i = 0; i < parms.size(); ++i)
		{
			TooolsPlugin plugin = (TooolsPlugin) TooolsPlugin.fromCmdLine(parms.get(i),
					i == 0, getPluginFactory());
			r.add(plugin);
		}

		return r;
	}

	protected abstract PluginFactory getPluginFactory();

	@Override
	public String getShortDescription()
	{
		return "chain plugins";
	}
}
