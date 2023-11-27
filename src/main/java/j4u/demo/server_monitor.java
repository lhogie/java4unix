package j4u.demo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import j4u.Command;
import j4u.CommandLine;
import j4u.CommandLineSpecification;
import toools.extern.Proces;
import toools.extern.ProcesException;
import toools.io.Cout;
import toools.io.file.RegularFile;
import toools.spreadsheet.SpreadSheet;
import toools.text.TextUtilities;
import toools.text.TextUtilities.HORIZONTAL_ALIGNMENT;

public class server_monitor extends Command {

	@Override
	public String getDescription() {
		return "Print information on servers";
	}

	int row = 0;

	public int runScript(CommandLine cmdLine) throws IOException, InterruptedException {
		Cout.debugSuperVisible("server_monitor     " + cmdLine.getArguments());

		final List<String> serverNames = getServerNames(cmdLine);
		final int timeoutMs = Integer.valueOf(getOptionValue(cmdLine, "--timeout"));
		List<Thread> threads = new ArrayList<>();

		SpreadSheet ss = new SpreadSheet(serverNames.size() + 2, 6);
		ss.set(0, 0, "serverName");
		ss.getColumnProperty(0).setWidth(30);
		ss.getColumnProperty(0).setA(HORIZONTAL_ALIGNMENT.LEFT);
		ss.set(0, 1, "RAM");
		ss.getColumnProperty(1).setWidth(10);
		ss.set(0, 2, "#cores");
		ss.getColumnProperty(2).setWidth(6);
		ss.set(0, 3, "Drive");
		ss.getColumnProperty(3).setWidth(10);
		ss.set(0, 4, "stress");
		ss.getColumnProperty(4).setWidth(10);
		ss.set(0, 5, "CPU");
		ss.getColumnProperty(5).setWidth(20);

		for (int c = 0; c < ss.getNbColumn(); ++c) {
			ss.set(1, c, TextUtilities.repeat('-', ss.getColumnProperty(c).getWidth()));
		}

		boolean parallel = isOptionSpecified(cmdLine, "--parallel");
		row = 2;

		printMessage(ss.getRowAsString(0));
		printMessage(ss.getRowAsString(1));

		for (String serverName : serverNames) {
			Thread t = new Thread() {
				@Override
				public void run() {
					Info i = getInfo(serverName, timeoutMs);

					synchronized (getClass()) {
						if (i.values() == null) {
							System.out.println(i.serverName + " " + i.error);
						} else {
							ss.setRow(row, i.values());
							System.out.println(ss.getRowAsString(row));
						}

						++row;
					}
				}
			};

			threads.add(t);

			if (parallel)
				t.start();
			else
				t.run();
		}

		for (Thread t : threads) {
			t.join();
		}

		return 0;
	}

	private List<String> getServerNames(CommandLine cmdLine) throws IOException {
		if (cmdLine.getArguments().isEmpty()) {
			Cout.debugSuperVisible("mca");
			return Arrays.asList("localhost");
		} else if (isOptionSpecified(cmdLine, "--file")) {
			String filename = getOptionValue(cmdLine, "--file");
			return new RegularFile(filename).getLines();
		} else {
			return new ArrayList<String>(cmdLine.findParameters());
		}
	}

	static class Info {
		final String serverName;
		final Error error;
		final int nbGig, nbCore;
		final boolean ssd;
		final String cpu;
		final int time;

		Info(String serverName, String sshOutput) {
			this.serverName = serverName;
			String[] lines = sshOutput.split("\n");

			nbGig = Integer.valueOf(lines[0].replace("MemTotal:", "").replace("kB", "").trim()) / 1000000;
			nbCore = Integer.valueOf(lines[1]);
			ssd = lines[2].trim().equals("0");
			cpu = lines[3].substring(lines[3].indexOf(':') + 1).trim().replaceAll(" +", " ");
			time = Integer.valueOf(lines[4]);
			error = null;
		}

		public Info(String serverName, Error err) {
			this.serverName = serverName;
			nbGig = nbCore = -1;
			ssd = false;
			time = -1;
			this.cpu = null;
			this.error = err;
		}

		public Object[] values() {
			if (error != null) {
				return null;
			} else {
				Object[] r = new Object[6];
				r[0] = serverName;
				r[1] = nbGig + "GB";
				r[2] = nbCore;
				r[3] = ssd ? "SSD" : "mechanical";
				r[4] = time;
				r[5] = cpu;
				return r;
			}
		}
	}

	private Info getInfo(String serverName, int timeoutMs) {
		String s = "cat /proc/meminfo | grep MemTotal" + "&& nproc" + " && cat /sys/block/sda/queue/rotational"
				+ " && cat /proc/cpuinfo | grep \"model name\" | uniq"
				+ " && E=$(($(date +%s) + 2)); N=0; while (($(date +%s) < $E)); do N=$((N+1)); done; echo $N";

		try {
			String sshReply = new String(Proces.exec("ssh", "-o", "ConnectTimeout=5", "-o",
					"PreferredAuthentications=publickey", "-o", "StrictHostKeyChecking=no", serverName, s));

			return new Info(serverName, sshReply);
		} catch (ProcesException e) {
			try {
				if (!InetAddress.getByName(serverName).isReachable(timeoutMs))
					return new Info(serverName, new Error("can't reach"));
			} catch (UnknownHostException e1) {
				return new Info(serverName, new Error("unknown host"));
			} catch (IOException e1) {
				return new Info(serverName, new Error("I/O error"));
			}

			return new Info(serverName, new Error("can't connect to SSH"));
		}
	}

	public static void main(String[] args) throws Throwable {
		new server_monitor().run(args);
	}

	@Override
	protected void specifyCmdLine(CommandLineSpecification spec) {
		spec.addOption("--timeout", "-t", "[0-9]+", "1000", "The acceptable wait (in millisecond)");
		spec.addOption("--file", "-f", ".+", null, "a file containing a list of hostnames");
		spec.addOption("--parallel", "-p", null, null, "checks in parallel");

	}
}
