package com.netifera.platform.host.processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.log.ILogger;

public class LinuxProcessManager implements IProcessManager {
	private ILogger logger;
	private static final String managerName = "Process Manager [Linux]";
	private final String procPath;
	
	public LinuxProcessManager(ILogger logger) {
		this.logger = logger;
		File f = new File("/proc");
		if(f.isDirectory()) {
			procPath = "/proc/";
			return;
		}
		f = new File("/peludo/osfs/proc");
		if(f.isDirectory()) {
			procPath = "/peludo/osfs/proc/";
		} else {
			procPath = null;
		}
	}

	private String readLine(String fileName) {
		try {
			InputStream in = new FileInputStream(fileName);
			
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			try {
				return br.readLine();
			} catch (IOException exception) {
				logger.error("I/O Error", exception);
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					logger.debug("I/O Error", e);
				}
				try {
					in.close();
				} catch (IOException e) {
					logger.debug("I/O Error", e);
				}
			}
		} catch (IOException e) {
			logger.error("I/O Error", e);
		}
		return null;
	}
	
	private Process getProcess(int pid) {
		String name = "?";
		int state = 0;
		int ppid = 0;
		int uid = 0;
		String commandLine = "?";
		long size = 0;

		String stat = readLine(procPath+pid+"/stat");
		if (stat != null) {
			String[] statArray = stat.split(" ");
			
			name = statArray[1];
			
			String stateString = statArray[2];
			if (stateString.equals("R"))
				state = Process.RUNNING;
			else if (stateString.equals("S"))
				state = Process.SLEEPING;
			else if (stateString.equals("Z"))
				state = Process.ZOMBIE;
			
			ppid = Integer.parseInt(statArray[3]);

			size = Long.parseLong(statArray[22]); // FIXME could overflow, this field is %lu
//			13 utime
//			22 vsize
//			23 rss
		}
		
		InputStream in = null;
		try {
			in = new FileInputStream(procPath+pid+"/status");
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			try {
				String line;
				if ((line = br.readLine()) == null) {
					logger.error("can not parse /proc/"+pid+"/status");
					return null;
				}
				name = line.substring(line.indexOf('\t')+1);
				if ((line = br.readLine()) == null) {
					logger.error("can not parse /proc/"+pid+"/status");
					return null;
				}
				br.readLine(); // skip State
				br.readLine(); // skip Tgid
				br.readLine(); // skip pid
				if ((line = br.readLine()) == null) {
					logger.error("can not parse /proc/"+pid+"/status");
					return null;
				}
//				ppid = Integer.parseInt(line.split("\\s")[1]);
				br.readLine(); // skip tracer pid
				if ((line = br.readLine()) == null) {
					logger.error("can not parse /proc/"+pid+"/status");
					return null;
				}
				uid = Integer.parseInt(line.split("\\s")[1]);
			} catch (IOException exception) {
				logger.error("I/O Error", exception);
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					logger.debug("I/O Error", e);
				}
				try {
					in.close();
				} catch (IOException e) {
					logger.debug("I/O Error", e);
				}
			}

			commandLine = readLine(procPath+pid+"/cmdline");

			/*
			 * memory /proc/statm
			 * memory map /proc/maps
			 * cwd /proc/cwd
			 * 
			 */
			
			return new Process(this, name, state, pid, ppid, uid, commandLine, size);
		} catch (FileNotFoundException e) {
			logger.error("Process "+pid+" not found");
			return null;
		}
	}

	public Process[] getProcessList() {
		List<Process> processList = new ArrayList<Process>();
		File proc = new File(procPath);
		
		if(!proc.exists()) {
			return new Process[0];
		}
		for (String pid: proc.list())
			if (pid.matches("\\d+")) {
				Process process = getProcess(Integer.parseInt(pid));
				if (process != null)
					processList.add(process);
			}
		return processList.toArray(new Process[processList.size()]);
	}

	public boolean kill(int pid, int signal) {
		try {
			java.lang.Process killpid = Runtime.getRuntime().exec(new String[] {"kill","-"+signal, ""+pid});
			try {
				killpid.waitFor();
				return true;
			} catch (InterruptedException exception) {
				logger.error("Interrupted", exception);
			} finally {
				killpid.getInputStream().close();
				killpid.getErrorStream().close();
				killpid.getOutputStream().close();
				killpid.destroy();
			}
		} catch (IOException exception) {
			logger.error("I/O Error", exception);
		}
		return false;
	}

	public boolean kill(int pid) {
		return kill(pid, 9);
	}
	
	@Override
	public String toString() {
		return managerName;
	}
}
