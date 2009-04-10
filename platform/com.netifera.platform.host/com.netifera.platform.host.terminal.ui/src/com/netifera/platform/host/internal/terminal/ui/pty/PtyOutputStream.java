package com.netifera.platform.host.internal.terminal.ui.pty;

import java.io.IOException;
import java.io.OutputStream;

import com.netifera.platform.host.terminal.ITerminal;

public class PtyOutputStream extends OutputStream {

	private final ITerminal terminal;
	
	public PtyOutputStream(ITerminal terminal) {
		this.terminal = terminal;
	}
	@Override
	public void write(int b) throws IOException {
		send(new byte[] {(byte) b}, 0, 1);		
	}
	
	public void write(byte b[]) throws IOException {
		send(b, 0, b.length);
	}

	private void send(byte data[], int offset, int length) {
		byte[] sendBuffer = new byte[length];
		System.arraycopy(data, offset, sendBuffer, 0, length);
		terminal.sendInput(sendBuffer);
	}
}
