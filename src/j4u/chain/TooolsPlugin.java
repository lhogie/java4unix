package j4u.chain;

public interface TooolsPlugin<IN, OUT>
{
	OUT process(IN in);

	void setup(PluginConfig p);

	public static TooolsPlugin<?, ?> fromCmdLine(String s, boolean in, PluginFactory f)
	{
		PluginConfig p = new PluginConfig(s);
		String name = p.get("name");
		TooolsPlugin<?, ?> processor = f.create(name, in);

		if (processor == null)
			throw new IllegalArgumentException("no graph plugin '" + s + "'");

		processor.setup(p);
		p.ensureAllUsed();
		return processor;
	}


}
