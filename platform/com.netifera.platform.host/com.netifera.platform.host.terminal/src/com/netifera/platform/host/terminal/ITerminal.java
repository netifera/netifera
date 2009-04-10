package com.netifera.platform.host.terminal;

public interface ITerminal {
	String getName();
	void sendInput(byte[] data);
	void setSize(int width, int height);
	void close();
}
