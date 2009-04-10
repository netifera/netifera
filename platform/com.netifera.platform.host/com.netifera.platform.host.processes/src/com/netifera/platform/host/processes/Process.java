package com.netifera.platform.host.processes;

import java.io.Serializable;

public class Process implements Serializable {
	
	private static final long serialVersionUID = -3103187458909163222L;
	
	public static final int RUNNING = 1;
	public static final int SLEEPING = 2;
	public static final int ZOMBIE = 3;

	transient private IProcessManager manager; //FIXME NPE this is null when comming form a remote probe
	
	final private String name;
	final private int state;
	final private int pid;
	final private int ppid;
	final private int uid; //replace with User
	final private String commandLine;
//	final private Map<String,String> environment;
	final private long size;

	public Process(IProcessManager manager, String name, int state, int pid, int ppid, int uid, String commandLine, long size) {
		this.manager = manager;
		this.name = name;
		this.state = state;
		this.pid = pid;
		this.ppid = ppid;
		this.uid = uid;
		this.commandLine = commandLine;
		this.size = size;
	}
		
	public String getName() {
		return name;
	}
	
	public int getState() {
		return state;
	}

	public int getPID() {
		return pid;
	}

	public int getPPID() {
		return ppid;
	}

	public int getUID() {
		return uid;
	}
	
	public String getCommandLine() {
		return commandLine;
	}
	
	public long getSize() {
		return size;
	}
	
	public boolean kill() {
		return manager.kill(pid);
	}
	
	public boolean isPriviledged() {
		return uid == 0;
	}

	public String toString() {
		return name+" ("+pid+")";
	}
}
