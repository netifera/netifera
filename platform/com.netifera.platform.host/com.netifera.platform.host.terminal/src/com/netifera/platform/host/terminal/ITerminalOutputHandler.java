package com.netifera.platform.host.terminal;

public interface ITerminalOutputHandler {
	void terminalOutput(String ptyName, byte[] data, int length);
	void terminalClosed(String ptyName);
}
