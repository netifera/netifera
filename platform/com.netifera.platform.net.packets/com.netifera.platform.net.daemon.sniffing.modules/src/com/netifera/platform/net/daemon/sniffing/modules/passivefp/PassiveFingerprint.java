package com.netifera.platform.net.daemon.sniffing.modules.passivefp;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import com.netifera.platform.net.daemon.sniffing.IIPSniffer;
import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.internal.daemon.sniffing.modules.Activator;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.packets.tcpip.IPv4;
import com.netifera.platform.net.packets.tcpip.IPv6;
import com.netifera.platform.net.packets.tcpip.TCP;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.util.NetworkConstants;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class PassiveFingerprint implements IIPSniffer {
	
	final static int MODE_SYN = 1;
	final static int MODE_ACK = 2;
	final static int MODE_RST = 3;
	final static int MODE_OPEN = 4;
	private static final boolean synMode = true;
	private static final boolean openMode = false;
	private static final boolean rstMode = false;
	private static final boolean ackMode = true;
	private final SignatureSet sigSet = new SignatureSet(synMode, ackMode, rstMode, openMode);
	private final Set<InternetAddress> foundAddresses = new HashSet<InternetAddress>();
	
	public IPacketFilter getFilter() {
		return null;
	}
	public String getName() {
		return "Passive OS Fingerprinting";
	}
	
	public void handleIPv4Packet(IPv4 ipv4, IPacketModuleContext ctx) {
		if(ipv4.getNextProtocol() != NetworkConstants.IPPROTO_TCP) {
			return;
		}
		
		TCP tcp = (TCP) ipv4.findHeader(TCP.class);
		
		if(tcp == null) {
			return;
		}
		if(foundAddresses.contains(ipv4.getSourceAddress())) {
			return;
		}
		
		Signature s = handleTCP(ipv4, tcp, ctx);
		if(s == null) return;
		
		log(ctx, "match! [" + ipv4.getSourceAddress() +
				"] --> " + ipv4.getDestinationAddress() + " is " + s.getOSGenre() + " " + s.getOSVersion());
		foundAddresses.add(ipv4.getSourceAddress());
		
		INetworkEntityFactory factory = Activator.getInstance().getNetworkEntityFactory();
		factory.setOperatingSystem(ctx.getRealm(), ctx.getSpaceId(), ipv4.getSourceAddress(), s.getOSGenre() + " " + s.getOSVersion());
	}
	
	public void handleIPv6Packet(IPv6 ipv6, IPacketModuleContext ctx) {
		// not supported since we rely on IPv4 header fields
	}
	
	private Signature handleTCP(IPv4 ip, TCP tcp, IPacketModuleContext ctx) {
		if(synMode && tcp.getSYN() && !tcp.getACK()) {
			return matchPacket(ip, tcp, MODE_SYN, ctx);
		} else if(ackMode && tcp.getSYN() && tcp.getACK()) {
			return matchPacket(ip, tcp, MODE_ACK, ctx);
		} else if(rstMode && tcp.getRST()) {
			return matchPacket(ip, tcp, MODE_RST, ctx);
		} else if(openMode && tcp.getACK() && !tcp.getSYN()) {
			return matchPacket(ip, tcp, MODE_OPEN, ctx);
		} else {
			return null;
		}
	}
	
	private Signature matchPacket(IPv4 ip, TCP tcp, int mode, IPacketModuleContext ctx) {
		int quirks = 0;
		
		if(ip.getHeaderLength32() > 5) {
			quirks |= Signature.QUIRK_IPOPT;
		}
		
		if(mode == MODE_RST && tcp.getACK()) {
			quirks |= Signature.QUIRK_RSTACK;
		}
		
		if(tcp.sequence().equals(tcp.ackSequence())) {
			quirks |= Signature.QUIRK_SEQEQ;
		}
		
		if(tcp.sequence().toInteger() == 0) {
			quirks |= Signature.QUIRK_SEQ0;
		}
		
		if(mode == MODE_OPEN) {
			if(tcp.getURG() || tcp.getFIN()) {
				quirks |= Signature.QUIRK_FLAGS;
			}
		
		} else {
			if(tcp.getPSH() || tcp.getURG() || tcp.getFIN()) {
				quirks |= Signature.QUIRK_FLAGS;
			}
		}
		
		if(tcp.getNextHeader() != null) {
			quirks |= Signature.QUIRK_DATA;
		}
		
		ByteBuffer options = ByteBuffer.wrap(tcp.getOptionsBuffer());
		int mss = 0;
		int wsc = 0;
		int tstamp = 0;
		byte[] op = new byte[Signature.MAXOPT];
		int opCount = 0;
		boolean done = false;
		
		while(!done && options.hasRemaining()) {
			
			int currentOp = options.get() & 0xFF;
			switch(currentOp) {
			case TCP.OPT_EOL:
				op[opCount++] = TCP.OPT_EOL;
				if(options.hasRemaining()) {
					quirks |= Signature.QUIRK_PAST;
					done = true;
				}
				break;
			case TCP.OPT_NOP:
				op[opCount++] = TCP.OPT_NOP;
				break;
			case TCP.OPT_SACKOK:
				op[opCount++] = TCP.OPT_SACKOK;
				options.get(); // length byte
				break;
			case TCP.OPT_MAXSEG:
				if(options.remaining() < 3) {
					quirks |= Signature.QUIRK_BROKEN;
					done = true;
					break;
				}
				op[opCount++] = TCP.OPT_MAXSEG;
				options.get(); // length byte
				mss = options.getShort() & 0xFFFF;
				break;
				
				
			case TCP.OPT_WSCALE:
				if(options.remaining() < 2) {
					quirks |= Signature.QUIRK_BROKEN;
					done = true;
					break;
				}
				op[opCount++] = TCP.OPT_WSCALE;
				options.get(); // length byte
				wsc = options.get() & 0xFF;
				break;
			case TCP.OPT_TIMESTAMP:
				if(options.remaining() < 9) {
					quirks |= Signature.QUIRK_BROKEN;
					done = true;
					break;
				}
				op[opCount++] = TCP.OPT_TIMESTAMP;
				options.get(); // length byte
				tstamp = options.getInt();
				if(options.getInt() != 0) {
					quirks |= Signature.QUIRK_T2;
				}
				break;
				
			default:
				if(!options.hasRemaining()) {
					quirks |= Signature.QUIRK_BROKEN;
					done = true;
					break;
				}
				op[opCount++] = (byte) currentOp;
				int olen = options.get() & 0xFF;
				if(olen > 32 || olen > options.remaining()) {
					// p0f has a bug and won't set the QUIRK flag
					// in the second case.
					quirks |= Signature.QUIRK_BROKEN;
					done = true;
					break;
				}
				
				options.position(options.position() + olen);
				if(opCount >= Signature.MAXOPT) {
					quirks |= Signature.QUIRK_BROKEN;
					done = true;
				}
			}
			
		}
		
		if(tcp.getACK()) quirks |= Signature.QUIRK_ACK;
		if(tcp.getURG()) quirks |= Signature.QUIRK_URG;
		if(tcp.getReserved() != 0) quirks |= Signature.QUIRK_X2;
		if(ip.getIdentification() == 0) quirks |= Signature.QUIRK_ZEROID;
		
		Signature res = sigSet.match(ip.getTotalLength(), ip.getDF(), ip.getTimeToLive(),
				tcp.getWindow(), op, opCount, mss, wsc, tstamp, ip.getTypeOfService(), quirks, mode);
		if(res == null) {
			log(ctx, "Unmatched [" + ip.getSourceAddress() + "] --> " + ip.getDestinationAddress() +
					" has signature " + sigSet.printSignature(ip.getTotalLength(), ip.getDF(), ip.getTimeToLive(),
							tcp.getWindow(), op, opCount, mss, wsc, tstamp, ip.getTypeOfService(), quirks, mode));
		}
		
		return res;
	
		
	}
	
	// logging
	
	private final static boolean LOGGING_ENABLED = false;
	
	private void log(IPacketModuleContext ctx, String message) {
		if (LOGGING_ENABLED) {
			ctx.printOutput(message);
		}
	}
}
