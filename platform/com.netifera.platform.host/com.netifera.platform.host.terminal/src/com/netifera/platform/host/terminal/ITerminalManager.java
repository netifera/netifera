package com.netifera.platform.host.terminal;

public interface ITerminalManager {
	ITerminal openTerminal(String command, ITerminalOutputHandler outputHandler);

}
