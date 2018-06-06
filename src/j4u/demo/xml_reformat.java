package j4u.demo;

import java.io.File;
import java.util.Collection;

import j4u.ArgumentSpecification;
import j4u.CommandLine;
import j4u.OptionSpecification;
import toools.io.FileUtilities;
import toools.io.file.RegularFile;
import toools.text.xml.XMLNode;
import toools.text.xml.XMLUtilities;


public class xml_reformat extends Java4UnixCommand
{

	public xml_reformat(RegularFile f)
	{
		super(f);
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{

		for (String arg : cmdLine.findParameters())
		{
			File f = new File(arg);
			String xmlText = new String(FileUtilities.getFileContent(f));
			XMLNode root = XMLUtilities.xml2node(xmlText, false);
			FileUtilities.setFileContent(f, root.toString().getBytes());
		}

		return 0;
	}

	public String getShortDescription()
	{

		return "reformat the given XML";
	}

	
}
