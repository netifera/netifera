package com.netifera.platform.net.packets.tcpip;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.decoders.IPDecoder;
import com.netifera.platform.util.NetworkConstants;

public class ICMPv4 extends AbstractPacket implements IPv4Encapsulable {

	enum TYPE {
		ECHO_REPLY           (0,  "Echo Reply"),
		DEST_UNREACH         (3,  "Destination Unreachable"),
		SOURCE_QUENCH        (4,  "Source Quench"),
		REDIRECT             (5,  "Redirect (change route)"),
		ECHO_REQUEST         (8,  "Echo Request"),
		ROUTER_ADVERTISEMENT (9,  "Router advertisement"),
		ROUTER_SOLICITATION  (10, "Router solicitation"),
		TIME_EXCEEDED        (11, "Time Exceeded"),
		PARAMETER_PROBLEM    (12, "Parameter Problem"),
		TIMESTAMP_REQUEST    (13, "Timestamp Request"),
		TIMESTAMP_REPLY      (14, "Timestamp Reply"),
		INFO_REQUEST         (15, "Information Request"),
		INFO_REPLY           (16, "Information Reply"),
		ADDRESS_REQUEST      (17, "Address Mask Request"),
		ADDRESS_REPLY        (18, "Address Mask Reply"),
		TRACEROUTE           (30, "Traceroute"),
		CONVERSION_ERROR     (31, "Address Mask Reply"),
		MOBILE_HOST_REDIRECT (32, "Mobile Host Redirect"),
		IPV6_WHERE_ARE_YOU   (33, "IPv6 Where-Are-You"),
		IPV6_I_AM_HERE       (34, "IPv6 I-Am-Here"),
		MOBILE_REG_REQUEST   (35, "Mobile Registration Request"),
		MOBILE_REG_REPLY     (36, "Mobile Registration Reply"),
		DOMAIN_NAME_REQUEST  (37, "Domain Name request"),
		DOMAIN_NAME_REPLY    (38, "Domain Name reply"),
		SKIP_DISCOVERY       (39, "SKIP Algorithm Discovery Protocol");
		
		final int type;
		final String description;
		TYPE(int type, String description) {
			this.type = type;
			this.description = description;
		};
		@Override
		public String toString() {
			return description;
		}
	}
	
	enum UNREACH_CODE {
		NET_UNREACH    (0,  "Network Unreachable"),
		HOST_UNREACH   (1,  "Host Unreachable"),
		PROT_UNREACH   (2,  "Protocol Unreachable"),
		PORT_UNREACH   (3,  "Port Unreachable"),
		FRAG_NEEDED    (4,  "Fragmentation Needed/DF set"),
		SROUTE_FAILED  (5,  "Source Route Failed"),
		NET_UNKNOWN    (6,  "Network Unknown"),
		HOST_UNKNOWN   (7,  "Host Unknown"),
		HOST_ISOLATED  (8,  "Source Host isolated (Obsolete)"),
		NET_ANO        (9,  "Network Administratively Prohibited"),
		HOST_ANO       (10, "Host Administratively Prohibited"),
		NET_UNR_TOS    (11, "Network Unreachable for Type Of Service"),
		HOST_UNR_TOS   (12, "Host Unreachable for Type Of Service"),
		PKT_FILTERED   (13, "Packet Filtered"),
		PREC_VIOLATION (14, "Precedence violation"),
		PREC_CUTOFF    (15, "Precedence cut off");

		final int code;
		final String description;
		private UNREACH_CODE(int code, String description) {
			this.code = code;
			this.description = description;
		}
		@Override
		public String toString() {
			return description;
		}
	}
	
	enum REDIRECT_CODE {
		NET      (0, "Redirect Net"),
		HOST     (1, "Redirect Host"),
		NET_TOS  (2, "Redirect Net for TOS"),
		HOST_TOS (3, "Redirect Host for TOS");

		final int code;
		final String description;
		private REDIRECT_CODE(int code, String description) {
			this.code = code;
			this.description = description;
		}
		@Override
		public String toString() {
			return description;
		}
	}

	enum TIME_EXCEEDED_Code {
		TTL      (0, "TTL count exceeded"),
		FRAGTIME (1, "Fragment Reass time exceeded");
		
		final int code;
		final String description;
		private TIME_EXCEEDED_Code(int code, String description) {
			this.code = code;
			this.description = description;
		}
		@Override
		public String toString() {
			return description;
		}
	}
	
	static Map<Integer, Enum<?>> typesMap = new HashMap<Integer, Enum<?>>();
	static Map<Integer, Enum<?>> unreachMap = new HashMap<Integer, Enum<?>>();
	static Map<Integer, Enum<?>> redirectMap = new HashMap<Integer, Enum<?>>();
	static Map<Integer, Enum<?>> timeExceedMap = new HashMap<Integer, Enum<?>>();
	static {
		for (TYPE value : TYPE.values()) {
			typesMap.put(value.type, value);
		}
		for (UNREACH_CODE value : UNREACH_CODE.values()) {
			unreachMap.put(value.code, value);
		}
		for (REDIRECT_CODE value : REDIRECT_CODE.values()) {
			redirectMap.put(value.code, value);
		}
		for (TIME_EXCEEDED_Code value : TIME_EXCEEDED_Code.values()) {
			timeExceedMap.put(value.code, value);
		}
	}
	
	private static String valueDescription(Map<Integer, Enum<?>> map, int value) {
		if (map.containsKey(value)) {
			return map.get(value).toString();
		}
		return Integer.toString(value);
	}
	
	private int type;
	private int code;
	private int checksum;
	private int identifier;
	private int sequenceNumber;
	
	/**
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |     Type      |     Code      |          Checksum             |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |           Identifier          |        Sequence Number        |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     
	 */
	@Override
	protected void packHeader() {
		pack8(type);
		pack8(code);
		pack16(checksum);
		pack16(identifier);
		pack16(sequenceNumber);	
	}
	
	@Override
	protected void unpackHeader() {
		type = unpack8();
		code = unpack8();
		checksum = unpack16();
		identifier = unpack16();
		sequenceNumber = unpack16();
	}
	
	@Override
	protected boolean hasPayload() {
		return true;
	}
	
	private boolean checksumSet;
	
	public ICMPv4() {
	}
	
	public ICMPv4(IPacketHeader next) {
		super(next);
	}

	public static ICMPv4 echoRequest() {
		ICMPv4 icmp = new ICMPv4(PacketPayload.emptyPayload());
		icmp.setType(8);
		icmp.setCode(0);
		return icmp;
	}
	
	public void setType(int value) {
		verifyMaximum(value, 0xFF);
		type = value;
	}
	
	public int getType() {
		return type;
	}
	
	public void setCode(int value) {
		verifyMaximum(value, 0xFF);
		code = value;
	}
	
	public int getCode() {
		return code;
	}
	
	public void setChecksum(int value) {
		verifyMaximum(value, 0xFFFF);
		checksum = value;
		checksumSet = true;
	}
	
	public int getChecksum() {
		return checksum;
	}
	
	public void setIdentifier(int value) {
		verifyMaximum(value, 0xFFFF);
		identifier = value;
	}
	
	public int getIdentifier() {
		return identifier;
	}
	
	public void setSequenceNumber(int value) {
		verifyMaximum(value, 0xFFFF);
		sequenceNumber = value;
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	
	@Override
	protected void populateGeneratedFields() {
		if(!checksumSet) {
			checksum = 0;
		}
	}
	
	@Override
	protected void calculateChecksum() {
		if(!checksumSet) {
			int sum = generateChecksum(getLength());			
			pack16(sum, 2);
		}
	}
	
	public int protocolOverIPv4() {
		return NetworkConstants.IPPROTO_ICMP;
	}
	
	@Override
	public int minimumHeaderLength() {
		return 8;
	}
	
	public boolean isEchoReply() {
		return getType() == TYPE.ECHO_REPLY.type;
	}
	
	public boolean isDestinationUnreachable() {
		return getType() == TYPE.DEST_UNREACH.type;
	}
	
	public boolean isNetworkUnreachable() {
		return isDestinationUnreachable() && getCode() == UNREACH_CODE.NET_UNREACH.code;
	}

	public boolean isHostUnreachable() {
		return isDestinationUnreachable() && getCode() == UNREACH_CODE.HOST_UNREACH.code;
	}

	public boolean isProtocolUnreachable() {
		return isDestinationUnreachable() && getCode() == UNREACH_CODE.PROT_UNREACH.code;
	}

	public boolean isPortUnreachable() {
		return isDestinationUnreachable() && getCode() == UNREACH_CODE.PORT_UNREACH.code;
	}
	
	public boolean isSourceQuench() {
		return getType() == TYPE.SOURCE_QUENCH.type;
	}

	public boolean isRedirect() {
		return getType() == TYPE.REDIRECT.type;
	}

	public boolean isEchoRequest() {
		return getType() == TYPE.ECHO_REQUEST.type;
	}

	public boolean isRouterAdvertisement() {
		return getType() == 9;
	}

	public boolean isRouterSolicitation() {
		return getType() == 10;
	}

	public boolean isTimeExceeded() {
		return getType() == TYPE.TIME_EXCEEDED.type;
	}
	
	public boolean isParameterProblem() {
		return getType() == TYPE.PARAMETER_PROBLEM.type;
	}
	
	public boolean isTimestampRequest() {
		return getType() == TYPE.TIMESTAMP_REQUEST.type;
	}

	public boolean isTimestampReply() {
		return getType() == TYPE.TIMESTAMP_REPLY.type;
	}

	public boolean isInfoRequest() {
		return getType() == TYPE.INFO_REQUEST.type;
	}
	
	public boolean isInfoReply() {
		return getType() == TYPE.INFO_REPLY.type;
	}

	public boolean isAddressMaskRequest() {
		return getType() == TYPE.ADDRESS_REQUEST.type;
	}
	
	public boolean isAddressMaskReply() {
		return getType() == TYPE.ADDRESS_REPLY.type;
	}
	
	public boolean isError() {
		return isDestinationUnreachable() || isSourceQuench() || isRedirect() || isTimeExceeded() || isParameterProblem();
	}

	
	private static final IPDecoder quoteDecoder = new IPDecoder();

	// Answer the IP packet quoted by an ICMP error
	public IPv4 quote() {	
		IPacketHeader header = quoteDecoder.getIPv4Decoder().decode(headerBufferSlice());
		if(header instanceof IPv4) {
			return (IPv4) header;
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("ICMP (type: ");
		out.append(valueDescription(typesMap, type));
		if (isDestinationUnreachable()) {
			out.append(valueDescription(unreachMap, code));
			out.append(", code: ");
		} else if (isRedirect()) {
			out.append(valueDescription(redirectMap, code));
			out.append(", code: ");
		} else if (isTimeExceeded()) {
			out.append(valueDescription(timeExceedMap, code));
			out.append(", code: ");
		}
		out.append(')');

		return out.toString();
	}
	
	@Override
	public String print() {
		StringBuffer out = new StringBuffer();
		out.append(this);

		if (isError()) {
			out.append(" : ");
			IPv4 ipv4 = quote();
			if(ipv4 != null) {
				out.append(ipv4);
			}
		} else if(getNextHeader() != null) {
			out.append(" + ");
			out.append(getNextHeader().print());
		}
		
		return out.toString();
	}
}
