package java4unix.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;
import toools.io.NullOutputStream;
import toools.io.data_transfer.DataTransfer;
import toools.io.data_transfer.DataTransferMonitor;
import toools.io.file.RegularFile;
import toools.text.TextUtilities;
import toools.thread.Threads;

public class Test_IO_Speed_For_File extends J4UScript
{
	public Test_IO_Speed_For_File(RegularFile f)
	{
		super(f);
	}

	@Override
	protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
	{
	}

	@Override
	protected void declareArguments(
			Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications
				.add(new ArgumentSpecification("file", ".+", false, "the file to test"));

	}

	@Override
	public String getShortDescription()
	{
		return "TestIOSpeedForFile";
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		File file = new File(cmdLine.findParameters().get(0));

		try
		{
			if (file.exists())
			{
				testRead(file);
			}
			else
			{
				testWrite(file);
			}
		}
		catch (IOException e)
		{
			printFatalError("I/O error on file " + file.getAbsolutePath());
			return 1;
		}

		return 0;
	}

	private void testWrite(File file) throws FileNotFoundException, IOException
	{
		System.out.println("Write test");
		FileOutputStream out = new FileOutputStream(file);
		long startTimeMs = System.currentTimeMillis();

		InputStream in = new InputStream()
		{
			int n = 0;

			@Override
			public int read(byte[] a, int offset, int len) throws IOException
			{
				// if > 10s
				if (System.currentTimeMillis() - startTimeMs > 10000)
					return - 1;

				return len;
			}

			@Override
			public int read() throws IOException
			{
				return 0;
			}
		};

		DataTransferMonitor m = new DataTransferMonitor();
		DataTransfer dt = new DataTransfer(in, out, m);

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (true)
				{
					Threads.sleepMs(500);

					printMessage(
							TextUtilities.toHumanString(m.getNbBytesPerSecond()) + "B/s");

					if (m.transferTerminated())
						break;
				}
			}
		}).start();
		dt.blockUntilCompletion();
	}

	private void testRead(File file) throws IOException
	{
		System.out.println("Read test");

		FileInputStream in = new FileInputStream(file);
		OutputStream out = new NullOutputStream();
		DataTransferMonitor m = new DataTransferMonitor();

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (true)
				{
					Threads.sleepMs(500);

					printMessage(
							TextUtilities.toHumanString(m.getNbBytesPerSecond()) + "B/s");

					if (m.transferTerminated())
						break;
				}
			}
		}).start();

		DataTransfer dt = new DataTransfer(in, out, m);

		dt.blockUntilCompletion();
	}

}
