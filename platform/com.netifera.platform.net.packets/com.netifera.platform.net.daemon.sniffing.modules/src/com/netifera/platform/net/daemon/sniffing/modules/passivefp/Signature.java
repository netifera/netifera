package com.netifera.platform.net.daemon.sniffing.modules.passivefp;

import java.nio.CharBuffer;

import com.netifera.platform.net.packets.tcpip.TCP;

final class Signature {

	final public static int QUIRK_PAST		= 0x0001;
	final public static int QUIRK_ZEROID	= 0x0002;
	final public static int QUIRK_IPOPT		= 0x0004;
	final public static int QUIRK_URG		= 0x0008;
	final public static int QUIRK_X2		= 0x0010;
	final public static int QUIRK_ACK		= 0x0020;
	final public static int QUIRK_T2		= 0x0040;
	final public static int QUIRK_FLAGS		= 0x0080;
	final public static int QUIRK_DATA		= 0x0100;
	final public static int QUIRK_BROKEN	= 0x0200;
	final public static int QUIRK_RSTACK	= 0x0400;
	final public static int QUIRK_SEQEQ		= 0x0800;
	final public static int QUIRK_SEQ0		= 0x1000;

	public final static int MAXOPT				= 16;

	final private static int IDX_WINSIZE	= 0;
	final private static int IDX_TTL 		= 1;
	final private static int IDX_DF			= 2;
	final private static int IDX_SIZE		= 3;
	final private static int IDX_OPTIONS	= 4;
	final private static int IDX_QUIRKS		= 5;
	final private static int IDX_GENRE		= 6;
	final private static int IDX_DESC		= 7;
	
	final private static int PACKET_BIG		= 100;
	
	enum WindowMod { MOD_NONE, MOD_CONST, MOD_MSS, MOD_MTU };
	enum SignatureType { TYPE_SYN, TYPE_ACK, TYPE_RST, TYPE_OPEN };
	private final SignatureType type;
	private String OSGenre;
	private String OSVersion;
	private String OSNotes;
	
	@SuppressWarnings("unused")
	private boolean generic;
	@SuppressWarnings("unused")
	private boolean userland;
	@SuppressWarnings("unused")
	private boolean noDetail;
	private int windowSize;
	private WindowMod windowSizeMod;
	private int ttl;
	private int packetSize;
	private boolean dfFlag;
	
	private int wsc;
	private WindowMod wscMod;
	
	private int mss;
	private WindowMod mssMod;
	
	private boolean zeroStamp;
	private byte[] optionValues = new byte[MAXOPT];
	private int optionCount = 0;
	
	private int quirks;
		
	
	static Signature createSynSignature(String signatureString) throws ParseException {
		Signature s = new Signature(SignatureType.TYPE_SYN);
		s.parse(signatureString);
		return s;
	}
	
	static Signature createAckSignature(String signatureString) throws ParseException {
		Signature s = new Signature(SignatureType.TYPE_ACK);
		s.parse(signatureString);
		return s;
	}
	
	static Signature createRstSignature(String signatureString) throws ParseException {
		Signature s = new Signature(SignatureType.TYPE_RST);
		s.parse(signatureString);
		return s;
	}
	
	static Signature createOpenSignature(String signatureString) throws ParseException {
		Signature s = new Signature(SignatureType.TYPE_OPEN);
		s.parse(signatureString);
		return s;
	}
	
	private Signature(SignatureType type) {
		this.type = type;
	}
	
	String getOSGenre() {
		return OSGenre;
	}
	
	String getOSVersion() {
		return OSVersion;
	}
	
	String getOSNotes() {
		return OSNotes;
	}
	
	boolean match(int len, boolean df, int ttl, int wss, byte[] ops, int nops,
			int mss, int wsc, int tstamp, int tos, int quirks) {
		if(packetSize == 0 && len < PACKET_BIG)  return false;
		
		if(len != packetSize) return false;
		
		if(nops != optionCount) return false;
		
		if(zeroStamp) {
			if(tstamp != 0) return false;
		} else { 
			if(tstamp == 0) return false;
		}
		
		if(df != dfFlag) return false;
		if(quirks != this.quirks) return false;
		
		if(mssMod == WindowMod.MOD_NONE) {
			if(mss != this.mss) return false;
		} else {
			if(mss % this.mss != 0) return false;
		}
		if(wscMod == WindowMod.MOD_NONE) {
			if(wsc != this.wsc) return false;
		} else {
			if(wsc % this.wsc != 0) return false;
		}
		switch(windowSizeMod) {
		case MOD_NONE:
			if(wss != windowSize) return false;
			break;
		case MOD_CONST:
			if(wss % windowSize != 0) return false;
			break;
		case MOD_MSS:
			if(mss > 0 && (wss % mss) == 0) {
				if((wss / mss) != windowSize) return false;
			} else if(wss % 1460 == 0) {
				if(wss / 1460 != windowSize) return false;
			} else {
				return false;
			}
			break;
		case MOD_MTU:
			if(mss > 0 && (wss % (mss + 40)) == 0) {
				if(wss / (mss + 40) != windowSize) return false;
			} else if(wss % 1500 == 0) {
				if(wss / 1500 != windowSize) return false;
			} else {
				return false;
			}
			break;
		}
		for(int i = 0; i < nops; i++) {
			if(optionValues[i] != ops[i]) return false;
		}
		if(this.ttl < ttl) {
			// if(use_fuzzy) fuzzy = p;
			return false;
		}
		
		return true;
		
		
		
	}
	
	private void parseGenre(String genre) throws ParseException {
		String os = genre;
		while(true) {
			char c = os.charAt(0);
			switch(c) {
			case '-':
				userland = true;
				break;
			case '*':
				noDetail = true;
				break;
			case '@':
				generic = true;
				break;
			default:
				OSGenre = os;
				return;
			}
			os = os.substring(1);
			if(os.length() == 0) {
				throw new ParseException("Empty genre field");
			}
			
		}
	}
	
	private void parseDescription(String description) {
		String[] parts = description.split(" ", 2);
		
		if(parts.length > 0 && parts[0].length() > 0) {
			OSVersion = parts[0];
		}
		
		if(parts.length > 1 && parts[1].length() > 0) {
			OSNotes = parts[1];
		}		
	}
	
	private void parseWindowSize(String wsize) throws ParseException {
		
		if(wsize.length() == 0) {
			throw new ParseException("Empty window size");
		}
		
		try {
			switch(wsize.charAt(0)) {
			case '*':
				windowSizeMod = WindowMod.MOD_CONST;
				windowSize = 1;
				break;
			case 'S':
				windowSizeMod = WindowMod.MOD_MSS;
				windowSize = Integer.parseInt(wsize.substring(1));
				break;
			case 'T':
				windowSizeMod = WindowMod.MOD_MTU;
				windowSize = Integer.parseInt(wsize.substring(1));
				break;
			case '%':
				windowSizeMod = WindowMod.MOD_CONST;
				windowSize = Integer.parseInt(wsize.substring(1));
				break;
			default:
				windowSizeMod = WindowMod.MOD_NONE;
				windowSize = Integer.parseInt(wsize);
				break;	
			}
		} catch(NumberFormatException e) {
			throw new ParseException("Error parsing integer in window size");
		}
				
	}
	
	private void addOption(int option) throws ParseException {
		if(optionCount >= MAXOPT) {
			throw new ParseException("Too many options");
		}
		optionValues[optionCount++] = (byte) option;
	}
	private void parseOptions(CharBuffer optionsBuffer) throws ParseException {
		zeroStamp = true;
		wscMod = WindowMod.MOD_NONE;
		mssMod = WindowMod.MOD_NONE;
		
		if(optionsBuffer.charAt(0) == '.') {
			optionsBuffer.get();
		}
		
		while(optionsBuffer.hasRemaining()) {
			switch(optionsBuffer.get()) {
			case 'N':
				addOption(TCP.OPT_NOP);
				break;
			case 'E':
				addOption(TCP.OPT_EOL);
				if(optionsBuffer.hasRemaining()) {
					throw new ParseException("EOL is not the last option");
				}
				break;
			case 'S':
				addOption(TCP.OPT_SACKOK);
				break;
			case 'T':
				addOption(TCP.OPT_TIMESTAMP);
				if(optionsBuffer.hasRemaining() && optionsBuffer.charAt(0) != '0') {
					zeroStamp = false;
				}
				break;
			case 'W':
				addOption(TCP.OPT_WSCALE);
				parseWScale(optionsBuffer);
				break;
			case 'M':
				addOption(TCP.OPT_MAXSEG);
				parseMaxSeg(optionsBuffer);
				break;
			case '?':
				addOption(atoi(optionsBuffer));
				break;
			default:
				throw new ParseException("Unknown option value");
			}
			
			// Skip characters that are not letters or '?'
			while(optionsBuffer.hasRemaining() && !Character.isLetter(optionsBuffer.charAt(0)) && optionsBuffer.charAt(0) != '?') {
				optionsBuffer.get();
			}
		}
		
		
	}
	
	private void parseQuirks(CharBuffer quirkBuffer) throws ParseException {
		while(quirkBuffer.hasRemaining()) {
			switch(quirkBuffer.get()) {
			case 'K':
				if(type != SignatureType.TYPE_RST) {
					throw new ParseException("Quirk 'K' is only valid in RST mode");
				}
				quirks |= QUIRK_RSTACK;
				break;
			case 'D':
				if(type == SignatureType.TYPE_OPEN) {
					throw new ParseException("Quirk 'D' is not valid in open mode");
				}
				quirks |= QUIRK_DATA;
				break;
			case 'Q':
				quirks |= QUIRK_SEQEQ;
				break;
			case '0':
				quirks |= QUIRK_SEQ0;
				break;
			case 'P':
				quirks |= QUIRK_PAST;
				break;
			case 'Z':
				quirks |= QUIRK_ZEROID;
				break;
			case 'I':
				quirks |= QUIRK_IPOPT;
				break;
			case 'U':
				quirks |= QUIRK_URG;
				break;
			case 'X':
				quirks |= QUIRK_X2;
				break;
			case 'A':
				quirks |= QUIRK_ACK;
				break;
			case 'T':
				quirks |= QUIRK_T2;
				break;
			case 'F':
				quirks |= QUIRK_FLAGS;
				break;
			case '!':
				quirks |= QUIRK_BROKEN;
				break;
			case '.':
				break;
			default:
				throw new ParseException("Bad quirk type");
			}
		}
	}
	
	private int atoi(CharBuffer buffer) {
		int val = 0;
		while( buffer.hasRemaining() && Character.isDigit( buffer.charAt(0) )) {
			val *= 10;
			val += Character.digit(buffer.get(), 10);
		}
		return val;
	}
	
	private void parseWScale(CharBuffer wscaleBuffer) throws ParseException {
		if(!wscaleBuffer.hasRemaining()) {
			throw new ParseException("Empty window scale field");
		}
		
		char c = wscaleBuffer.charAt(0);
		if(c == '*') {
			wscaleBuffer.get();
			wsc = 1;
			wscMod = WindowMod.MOD_CONST;
		} else if (c == '%') {
			wscaleBuffer.get();
			wsc = atoi(wscaleBuffer);
			if(wsc == 0) {
				throw new ParseException("Illegal null modulo for window scale");
			}
			wscMod = WindowMod.MOD_CONST;
		} else {
			wsc = atoi(wscaleBuffer);
			wscMod = WindowMod.MOD_NONE;
		}
		
	}
	
	private void parseMaxSeg(CharBuffer mssBuffer) throws ParseException {
		if(!mssBuffer.hasRemaining()) {
			throw new ParseException("Empty max segment size field");
		}
		char c = mssBuffer.charAt(0);
		if(c == '*') {
			mssBuffer.get();
			mss = 1;
			mssMod= WindowMod.MOD_CONST;
			
		} else if(c == '%') {
			mssBuffer.get();
			mss = atoi(mssBuffer);
			if(mss == 0) {
				throw new ParseException("Illegal null modulo for MSS");
			}
			mssMod = WindowMod.MOD_CONST;
		} else {
			mss = atoi(mssBuffer);
			wscMod = WindowMod.MOD_NONE;
		}
	
	}
	
	private void parse(String signature) throws ParseException {
		String[] parts = signature.split(":");
		if(parts.length != 8) {
			throw new ParseException("Too few fields (" + parts.length + ")");
		}
		
		parseGenre(parts[IDX_GENRE]);
		parseDescription(parts[IDX_DESC]);
	
		try {
			ttl = Integer.parseInt(parts[IDX_TTL]);
		
			if(parts[IDX_SIZE].charAt(0) == '*') {
				packetSize = 0;
			} else {
				if(type == SignatureType.TYPE_OPEN) {
					throw new ParseException("Packet size must be '*' in open mode signatures");
				}
				packetSize = Integer.parseInt(parts[IDX_SIZE]);
			}
		
			int df = Integer.parseInt(parts[IDX_DF]);
			if(df != 0) {
				dfFlag = true;
			}
		} catch(NumberFormatException e) {
			throw new ParseException("Integer parsing error");
		}
		
		parseWindowSize(parts[IDX_WINSIZE]);
		
		parseOptions(CharBuffer.wrap(parts[IDX_OPTIONS]));
		
		parseQuirks(CharBuffer.wrap(parts[IDX_QUIRKS]));
		
	
		
	}
	
}
