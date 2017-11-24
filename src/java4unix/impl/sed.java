package java4unix.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;
import toools.io.file.RegularFile;


public class sed extends J4UScript
{


	public sed(RegularFile f)
	{
		super(f);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getShortDescription()
	{
		return "Replace text in files";
	}
	
    @Override
    protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
    {
    }


	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification("text",".+", false, "pattern to look for"));
		argumentSpecifications.add(new ArgumentSpecification("replacement",".+", false, "pattern to look for"));
		argumentSpecifications.add(new ArgumentSpecification("file",".+", true, "text file"));
	}

    @Override
    public int runScript(CommandLine cmdLine)
    {
    	List<String> args = new ArrayList<String>(cmdLine.findParameters());
    	String regex = args.remove(0);
    	String replacement = args.remove(0);
    	
    	for (String filename : args)
    	{
    		RegularFile f = new RegularFile(filename);

    		try
    		{
        		String text = new String(f.getContent());
        		int p = -1;	
        		
        		while (true)
        		{
        			p = text.indexOf(text);
            		
            		if (p == -1)
            		{
            			break;
            		}
            		else
            		{
            			text = text.substring(0, p) + replacement + text.substring(p + text.length());
            		}
        		}
        		
        		if (p > 0)
        		{
        			f.setContentAsUTF8(text);
            		printMessage(f.getPath());
        		}
    		}
    		catch (IOException e)
    		{
    			printNonFatalError("I/O error while processing: " + f.getPath());
    		}
    	}

    	return 0;
    }
}
