package com.netifera.platform.host.processes;


public interface IProcessManager {
	Process[] getProcessList();
	boolean kill(int pid);
}
