package com.netifera.platform.net.daemon.sniffing.modules;

import java.nio.ByteBuffer;

import com.netifera.platform.net.daemon.sniffing.IStreamModuleContext;
import com.netifera.platform.net.daemon.sniffing.ITCPBlockSniffer;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferConfig;
import com.netifera.platform.util.HexDump;

public class BlockTest  implements ITCPBlockSniffer {
	private final HexDump hex = new HexDump();
	public IPacketFilter getFilter() {
		return null;
	}

	public String getName() {
		return "Block Sniffing Test";
	}

	public void handleBlock(IStreamModuleContext ctx, ByteBuffer clientData,
			ByteBuffer serverData) {
		ctx.printOutput("New TCP (block) Session: " + ctx.getKey() + "\n");
		ctx.printOutput("Client --> Server: " + clientData.remaining() + " bytes\n");
		print(ctx, clientData);
		ctx.printOutput("Server -> Client:" + serverData.remaining() + " bytes\n");
		print(ctx, serverData);
	
		
	}

	public void initialize(IBlockSnifferConfig config) {
		config.setTotalLimit(1024);
			
	}
	
	private void print(IStreamModuleContext ctx, ByteBuffer data) {
		int idx = firstNonAscii(data);
		if(idx == -1) {
			byte[] dataBytes = new byte[data.remaining()];
			data.get(dataBytes);
			ctx.printOutput(new String(dataBytes));
		} else if(idx == 0) {
			ctx.printOutput(hex.bufferDump(data));
		} else {
			byte[] dataBytes = new byte[idx];
			data.get(dataBytes, 0, idx);
			ctx.printOutput(new String(dataBytes) + "\n");
			hex.setInitialOffset(idx);
			ctx.printOutput(hex.bufferDump(data));
			hex.setInitialOffset(0);
		}
		ctx.printOutput("\n");

	}
	
	private int firstNonAscii(ByteBuffer data) {
		int idx = 0;
		while(data.hasRemaining()) {
			int c = data.get() & 0xFF;
			if((c & 0x80) != 0 ||
					(c < 0x20 && !(c == 0x0a || c == 0x0d))) {
				data.rewind();
				return idx;
			}
			idx++;
		}
		data.rewind();
		return -1;
	}
	

}
