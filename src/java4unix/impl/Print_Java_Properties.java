package java4unix.impl;

import java.util.Collection;
import java.util.Properties;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;
import toools.io.file.RegularFile;


public class Print_Java_Properties extends J4UScript
{


	public Print_Java_Properties(RegularFile f)
	{
		super(f);
	}

	@Override
	public String getShortDescription()
	{
		return "Print Java properties.";
	}
	
	@Override
    public int runScript(CommandLine cmdLine)
    {
        boolean onlykeys = isOptionSpecified(cmdLine, "-k");
        Properties p = System.getProperties();

        if (isOptionSpecified(cmdLine, "-p"))
        {
            String key = getOptionValue(cmdLine, "-p");
            
            if (!p.containsKey(key))
            {
                printFatalError("unexisting key: " + key);
                return 1;
            }
            else
            {
                printMessage((String) p.get(key));
            }
        }
        else
        {
            for (Object o : p.keySet())
            {
                printMessage(o + (onlykeys ? "" : "=" + p.get(o)));
            }
        }

        return 0;
    }


	@Override
    public void declareOptions(Collection<OptionSpecification> specs)
    {
        specs.add(new OptionSpecification("--only-keys", "-k", null, null, "print only available keys"));
        specs.add(new OptionSpecification("--print-value", "-p", ".+", ".+", "print the value of the given key"));
    }
	

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		
	}
	
}
