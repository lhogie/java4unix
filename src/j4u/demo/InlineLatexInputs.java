package j4u.demo;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import j4u.CommandLine;
import j4u.OptionSpecification;
import toools.io.FileUtilities;
import toools.io.file.RegularFile;

public class InlineLatexInputs extends Java4UnixCommand
{

	public InlineLatexInputs(RegularFile f)
	{
		super(f);
		addOption("--file", "-f", ".+", true, "LaTeX file");

	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		for (String arg : cmdLine.findParameters())
		{
			try
			{
				File f = new File(arg);
				printMessage("processing file " + f.getAbsolutePath());
				inline(f);
			}
			catch (IOException e)
			{
				printNonFatalError("I/O error on file: " + arg);
				e.printStackTrace();
			}

		}

		return 0;
	}

	private void inline(File file) throws IOException
	{
		String text = new String(FileUtilities.getFileContent(file));
		text = inline(text);
		FileUtilities.setFileContent(file, text.getBytes());
	}

	private String inline(String text) throws IOException
	{
		while (true)
		{
			int a = text.indexOf("\\input{");

			if (a == - 1)
			{
				return text;
			}
			else
			{
				int b = text.indexOf("}", a);

				if (b == - 1)
				{
					throw new IllegalStateException("unterminated \\input{} command");
				}
				else
				{
					String inputFileName = text.substring(a + 7, b);

					try
					{
						printMessage("inputing file " + inputFileName);
						File f = new File(inputFileName);

						if ( ! f.exists() && ! inputFileName.endsWith(".tex"))
						{
							f = new File(inputFileName + ".tex");
						}

						String inputFileText = new String(
								FileUtilities.getFileContent(f));
						String inputCmd = text.substring(a, b + 1);
						// do not use replaceAll() because it uses regexps
						// instead of litteral strings
						text = text.replace(inputCmd, inputFileText);
					}
					catch (IOException e)
					{
						throw new IOException("unable to read file " + inputFileName);
					}
				}
			}
		}
	}

	public void declareOptions(Collection<OptionSpecification> specs)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Inline all \\input{} in the given LaTeX document.";
	}
}
