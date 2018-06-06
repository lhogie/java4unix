package j4u.chain;

import java.util.ArrayList;
import java.util.List;

import toools.reflect.Clazz;

public class DefaultPlugins implements PluginFactory
{
	public final List<Package> importPackages = new ArrayList<>();

	public DefaultPlugins()
	{
		// the null package allows the search of the class by its name only
		importPackages.add(null);
	}

	@Override
	public TooolsPlugin<?, ?> create(String name, boolean in)
	{
		if (name.equals("print"))
		{
			return new ObjectPrinter();
		}
		else
		{
			for (Package pkg : importPackages)
			{
				String fullname = pkg == null ? name : pkg.getName() + "." + name;
				fullname = fullname.replace("..", ".");
				Class<TooolsPlugin> clazz = Clazz.findClass(fullname);

				if (clazz != null)
				{
					if ( ! TooolsPlugin.class.isAssignableFrom(clazz))
					{
						throw new IllegalStateException(clazz + " is not a plugin");
					}
					else
					{
						return Clazz.makeInstance(clazz);
					}
				}
			}

		}

		return null;
	}

}
