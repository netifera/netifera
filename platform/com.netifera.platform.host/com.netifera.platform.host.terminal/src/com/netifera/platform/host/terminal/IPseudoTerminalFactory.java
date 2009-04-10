package com.netifera.platform.host.terminal;

public interface IPseudoTerminalFactory {
	ITerminal openTerminal(String command, ITerminalOutputHandler outputHandler);
}
