package com.netifera.platform.util;

import java.nio.ByteBuffer;

public class HexDump {
	private final static int LINE_LENGTH = 16;
	private final static String SEPARATOR = "      ";
	private StringBuffer hexBuffer;
	private StringBuffer asciiBuffer;
	private StringBuffer outputBuffer;
	private boolean displayAscii = true;
	private boolean displayOffset = true;
	private int initialOffset = 0;
	
	private int count;
	
	
	public void setInitialOffset(int offset) {
		this.initialOffset = offset;
	}
	
	public String bufferDump(ByteBuffer buffer) {
		return bufferDump(buffer, buffer.remaining());
	}
	
	public String bufferDump(ByteBuffer buffer, int length) {
		final ByteBuffer slice = buffer.slice();
		
		
		resetBuffers();
		
		while(slice.remaining() > 0 && length-- > 0) {
			byte b = slice.get();
			addByte(b);
		}
		
		if(hexBuffer.length() > 0) {
			processEOL();
		}
		
		return outputBuffer.toString();
	}
	
	
	private void resetBuffers() {
		count = 0;
		outputBuffer = new StringBuffer();
		if(displayAscii) {
			asciiBuffer = new StringBuffer();
		}
		hexBuffer = new StringBuffer();
	}
	
	private void addByte(byte b) {
		addHex(b);
		if(displayAscii) {
			addAscii(b);
		}
		
		count ++;
		
		if(count % LINE_LENGTH == 0) {
			processEOL();
		}	
		
	}
	private void processEOL() {	
		// Add padding to hex buffer if necessary (ie: last line)
		if(displayAscii && (count % LINE_LENGTH != 0)) {
			int paddingCount = LINE_LENGTH - (count % LINE_LENGTH);
			for(int i = 0; i < paddingCount; i++) {
				// three spaces
				hexBuffer.append("   ");
				count++;
			}
		}
		
		if(displayOffset) {
			assert(count >= LINE_LENGTH);
		
			int offset = count - LINE_LENGTH;
		
			outputBuffer.append(String.format("%04x:  ", offset + initialOffset));
		}

		
		// remove trailing space
		if(hexBuffer.length() > 0) {
			hexBuffer.deleteCharAt(hexBuffer.length() - 1);
		}
		
		outputBuffer.append(hexBuffer);
		hexBuffer = new StringBuffer();
		
		if(displayAscii) {
			outputBuffer.append(SEPARATOR);
			outputBuffer.append(asciiBuffer);
			asciiBuffer = new StringBuffer();
		}
		
		outputBuffer.append("\n");
				
	}
	private void addAscii(byte b) {
		if(isPrintable(b)) {
			asciiBuffer.append(Character.toString((char)b));
		} else {
			asciiBuffer.append(".");
		}
		
	}
	private void addHex(byte b) {
		hexBuffer.append( String.format("%02x ", b));
	}
	
	
	private boolean isPrintable(byte b) {
		return (b >= 32 && b <= 126);
	}

}
