package j4u.demo;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import j4u.CommandLine;
import j4u.OptionSpecification;
import toools.io.file.RegularFile;

public class Last_Modified_File_Seconds extends Java4UnixCommand
{

	public Last_Modified_File_Seconds(RegularFile f)
	{
		super(f);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Comptes the date of the more recent file.";
	}

	public int runScript(CommandLine cmdLine)
	{
		File lastModified = null;
		String line;

		while ((line = readUserInput(null, ".*")) != null)
		{
			File f = new File(line);

			if (lastModified == null)
			{
				lastModified = f;
			}
			else
			{
				if (f.lastModified() > lastModified.lastModified())
				{
					lastModified = f;
				}
			}
		}

		if (lastModified != null)
		{
			printMessage(getDateRepresentation(lastModified.lastModified()));

			if (isOptionSpecified(cmdLine, "--printFileName"))
			{
				printMessage("\t" + lastModified.getAbsolutePath());
			}

			return 0;
		}
		else
		{
			return 1;
		}
	}

	private String getDateRepresentation(long millis)
	{
		// Calendar cal = Calendar.getInstance();
		// cal.setTimeInMillis(millis);
		// int year = cal.get(Calendar.YEAR);
		// int month = cal.get(Calendar.MONTH) + 1;
		// int day = cal.get(Calendar.DAY_OF_MONTH);
		// int hour = cal.get(Calendar.HOUR_OF_DAY);
		// int minute = cal.get(Calendar.MINUTE);
		// int second = cal.get(Calendar.SECOND);
		// // project-2008-01-03.15'45'33
		// String s = year + "-" + month + "-" + day + "." + hour + "'" + minute
		// + "'" + second;

		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		Date date = new Date(millis);
		return dateFormat.format(date);

	}

	public void declareOptions(Collection<OptionSpecification> specs)
	{
		specs.add(new OptionSpecification("--printFileName", "-n", null, null,
				"Print the name of the newest file"));
	}

}
