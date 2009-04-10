package com.netifera.platform.net.daemon.sniffing.modules.passivefp;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.net.packets.tcpip.TCP;

public class SignatureSet {
	private static final boolean DEBUG_PARSING = false;
	
	List<Signature> synSignatures = new ArrayList<Signature>();
	List<Signature> ackSignatures = new ArrayList<Signature>();
	
	SignatureSet(boolean syn, boolean ack, boolean rst, boolean open) {
		if(syn) {
			for(String s : SignatureData.synSignatures) {
				try {
					Signature sig = Signature.createSynSignature(s);
					synSignatures.add(sig);
				} catch (ParseException e) {
					if (DEBUG_PARSING) {
						System.out.println("Failed to parse: "+ s + "(" + e.getMessage() + ")");
					}
				}
			}
		}
		
		if(ack) {
			for(String s : SignatureData.ackSignatures) {
				try {
					Signature sig = Signature.createAckSignature(s);
					ackSignatures.add(sig);
				} catch (ParseException e) {
					if (DEBUG_PARSING) {
						System.out.println("Failure to parse (ACK) :" + s + "(" + e.getMessage() + ")");
					}
				}
			}
		}
	}
	
	
	Signature match(int len, boolean df, int ttl, int wss, byte[] ops, int nops,
			int mss, int wsc, int tstamp, int tos, int quirks, int type)
	{
		switch(type) {
		case PassiveFingerprint.MODE_SYN:
			return matchSyn(len, df, ttl, wss, ops, nops, mss, wsc, tstamp, tos, quirks);
		case PassiveFingerprint.MODE_ACK:
			return matchAck(len, df, ttl, wss, ops, nops, mss, wsc, tstamp, tos, quirks);
		default:
			return null;
		}
	}
	
	private Signature matchSyn(int totalLen, boolean df, int ttl, int wss,
			byte[] ops, int opCount, int mss, int wsc, int tstamp, int tos,
			int quirks) {
		
		for(Signature s : synSignatures) {
			if(s.match(totalLen, df, ttl, wss, ops, opCount, mss,
					wsc, tstamp, tos, quirks)) {
				return s;
			}
		}
		if(!df) {
			df = true;
			for(Signature s : synSignatures) {
				if(s.match(totalLen, df, ttl, wss, ops, opCount, mss,
						wsc, tstamp, tos, quirks)) {
					return s;
				}
			}
		}
	
		return null;
	}
	
	private Signature matchAck(int len, boolean df, int ttl, int wss,
			byte[] ops, int nops, int mss, int wsc, int tstamp, int tos,
			int quirks) {
	
		for(Signature s : ackSignatures) {
			if(s.match(len, df, ttl, wss, ops, nops, mss,
					wsc, tstamp, tos, quirks)) {
				return s;
			}
			
		}
		if(!df) {
			df = true;
			for(Signature s : ackSignatures) {
				if(s.match(len, df, ttl, wss, ops, nops, mss,
						wsc, tstamp, tos, quirks)) {
					return s;
				}
			}
		}
		
		return null;
	}
	
	String printSignature(int len, boolean df, int ttl, int wss, byte[] ops, int nops,
			int mss, int wsc, int tstamp, int tos, int quirks, int mode) {
		StringBuilder sb = new StringBuilder();
	
		if(mss != 0 && wss != 0 && (wss % mss) == 0) {
			sb.append("S" + (wss / mss));
		} else if(wss != 0 && (wss % 1460) == 0) {
			sb.append("S" + (wss / 1460));
		} else if(mss != 0 && wss != 0 && (wss % (mss + 40)) == 0) {
			sb.append("T" + (wss / (mss + 40)));
		} else if(wss != 0 && (wss % 1500) == 0) {
			sb.append("T" + wss / 1500);
		} else if(wss == 12345) {
			sb.append("*(12345)");
		} else {
			sb.append(wss);
		}
		
		if(mode != PassiveFingerprint.MODE_OPEN) {
			if(len < 100) {
				sb.append(":" + ttl + ":" + (df?"1":"0") + ":" + len + ":");
			} else {
				sb.append(":" + ttl + ":" + (df?"1":"0") + ":*:");
			}
		} else {
			sb.append(":" + ttl + ":" + (df?"1":"0") + ":*:");
		}
		
		for(int i = 0; i < nops; i++) {
			switch(ops[i]) {
			case TCP.OPT_NOP:
				sb.append('N');
				break;
			case TCP.OPT_WSCALE:
				sb.append("W" + wsc);
				break;
			case TCP.OPT_MAXSEG:
				sb.append("M" + mss);
				break;
			case TCP.OPT_TIMESTAMP:
				sb.append('T');
				if(tstamp == 0) {
					sb.append('0');
				}
				break;
			case TCP.OPT_SACKOK:
				sb.append('S');
				break;
			case TCP.OPT_EOL:
				sb.append('E');
				break;
			default:
				sb.append('?');
				sb.append(ops[i]);
			}
			if(i != (nops - 1)) {
				sb.append(',');
			}
		}
		
		if(nops == 0) {
			sb.append('.');
		}
		
		sb.append(':');
		
		if(quirks == 0) {
			sb.append('.');
		}
		
		if((quirks & Signature.QUIRK_RSTACK) != 0) sb.append('K');
		if((quirks & Signature.QUIRK_SEQEQ) != 0) sb.append('Q');
		if((quirks & Signature.QUIRK_SEQ0) != 0) sb.append('0');
		if((quirks & Signature.QUIRK_PAST) != 0) sb.append('P');
		if((quirks & Signature.QUIRK_ZEROID) != 0) sb.append('Z');
		if((quirks & Signature.QUIRK_IPOPT) != 0) sb.append('I');
		if((quirks & Signature.QUIRK_URG) != 0) sb.append('U');
		if((quirks & Signature.QUIRK_X2) != 0) sb.append('X');
		if((quirks & Signature.QUIRK_ACK) != 0) sb.append('A');
		if((quirks & Signature.QUIRK_T2) != 0) sb.append('T');
		if((quirks & Signature.QUIRK_FLAGS) != 0) sb.append('F');
		if((quirks & Signature.QUIRK_DATA) != 0) sb.append('D');
		if((quirks & Signature.QUIRK_BROKEN) != 0) sb.append('!');

		return sb.toString();
	}
}
