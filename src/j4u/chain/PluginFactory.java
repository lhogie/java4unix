package j4u.chain;

public interface PluginFactory
{
	TooolsPlugin<?, ?> create(String name, boolean bootstrap);
}
