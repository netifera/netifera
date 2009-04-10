package com.netifera.platform.net.packets.tcpip;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.util.NetworkConstants;

// TODO create Abstract class with ICMP ?
// See http://www.iana.org/assignments/icmpv6-parameters
public class ICMPv6 extends AbstractPacket implements IPv6Encapsulable {
	enum TYPE {
		// TODO complete with http://www.networksorcery.com/enp/protocol/icmpv6.htm
		DEST_UNREACH           (1,   "Destination Unreachable"),
		PKT_TOOBIG             (2,   "Packet Too Big"),
		TIME_EXCEEDED          (3,   "Time Exceeded"),
		PARAMETER_PROBLEM      (4,   "Parameter Problem"),
		ECHO_REQUEST           (128, "Echo Request"),
		ECHO_REPLY             (129, "Echo Reply"),
		ROUTER_SOLICITATION    (133, "Router solicitation"),
		ROUTER_ADVERTISEMENT   (134, "Router advertisement"),
		NEIGHBOR_SOLICITATION  (135, "Neighbor solicitation"),
		NEIGHBOR_ADVERTISEMENT (136, "Neighbor advertisement"),
		REDIRECT               (137, "Redirect");
				
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
		NO_ROUTE            (0,  "No Route to Host"),
		ADM_PROHIBITED      (1,  "Host Administratively Prohibited"),
		NOT_NEIGHBOUR       (2,  "Beyond Scope of Source Address"),
		ADDR_UNREACH        (3,  "Address Unreachable"),
		PORT_UNREACH        (4,  "Port Unreachable"),
		SADDR_FAILED_POLICY (5,  "Source Address failed ingress/egress Policy"),
		REJECT_ROUTE        (6,  "reject route to destination");

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
	
	enum TIME_EXCEEDED_Code {
		HOPLIMIT (0, "Hop Limit exceeded in transit"),
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
	
	enum PARAMETER_PROBLEM_Code {
		HDR_FIELD   (0, "erroneous header field encountered"),
		UNK_NEXTHDR (1, "unrecognized Next Header type encountered"),
		UNK_OPTION  (2, "unrecognized IPv6 option encountered");
		
		final int code;
		final String description;
		private PARAMETER_PROBLEM_Code(int code, String description) {
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
	static Map<Integer, Enum<?>> timeExceedMap = new HashMap<Integer, Enum<?>>();
	static Map<Integer, Enum<?>> parameterProblemMap = new HashMap<Integer, Enum<?>>();
	static {
		for (TYPE value : TYPE.values()) {
			typesMap.put(value.type, value);
		}
		for (UNREACH_CODE value : UNREACH_CODE.values()) {
			unreachMap.put(value.code, value);
		}
		for (TIME_EXCEEDED_Code value : TIME_EXCEEDED_Code.values()) {
			timeExceedMap.put(value.code, value);
		}
		for (PARAMETER_PROBLEM_Code value : PARAMETER_PROBLEM_Code.values()) {
			parameterProblemMap.put(value.code, value);
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

	@Override
	protected int minimumHeaderLength() {
		return 4;
	}

	@Override
	public int headerLength() {
		return 4;
	}

	@Override
	protected void packHeader() {
		pack8(type);
		pack8(code);
		pack16(checksum);
	}

	@Override
	protected void unpackHeader() {
		type = unpack8();
		code = unpack8();
		checksum = unpack16();
	}

	public int protocolOverIPv6() {
		return NetworkConstants.IPPROTO_ICMPV6;
	}
	
	public void setType(int value) {
		verifyMaximum(value, 0xFF);
		type = value;
	}
	
	public int getType() {
		return type;
	}
	
	public int getCode() {
		return code;
	}
	
	public int getChecksum() {
		return checksum;
	}
	
	public boolean isError() {
		return type < 128;
	}
	
	public boolean isInformation() {
		return type > 127;
	}
	
	public boolean isEchoRequest() {
		return type == 128;
	}
	
	public boolean isEchoReply() {
		return type == 129;
	}
	
	public boolean isDestinationUnreachable() {
		return type == 1;
	}
	
	public boolean isPacketTooBig() {
		return type == 2;
	}
	
	public boolean isTimeExceeded() {
		return type == 3;
	}
	
	public boolean isParameterProblem() {
		return type == 4;
	}
	
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("ICMPv6 (type: ");
		out.append(valueDescription(typesMap, type));
		if (isDestinationUnreachable()) {
			out.append(valueDescription(unreachMap, code));
			out.append(", code: ");
		} else if (isTimeExceeded()) {
			out.append(valueDescription(timeExceedMap, code));
			out.append(", code: ");
		} else if (isParameterProblem()) {
			out.append(valueDescription(parameterProblemMap, code));
			out.append(", code: ");
		}
		out.append(')');

		return out.toString();
	}
}
