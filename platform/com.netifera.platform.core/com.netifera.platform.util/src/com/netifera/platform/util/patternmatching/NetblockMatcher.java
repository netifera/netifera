package com.netifera.platform.util.patternmatching;

// TODO Cisco style
// TODO cache result

public final class NetblockMatcher {
	public enum Mode {
		Any,
		IPv4Only,
		IPv6Only
	}
	
	private final boolean matched;
	private String network;
	private int cidr;
	
	/**
	 * matches any IPv4 or IPv6 network block.
	 * 
	 * @param text The string to match.
	 * @return <code>true</code> if the string matches.
	 */
	public NetblockMatcher(final String text) {
		this(text, Mode.Any);
	}
	
	public NetblockMatcher(final String text, final Mode mode) {
		if (text == null) {
			matched = false;
		} else if (text.contains("/")) {
			matched = matchesCIDR(text, mode);
		} else if (text.contains("-")) {
			matched = matchesRange(text, mode);
		} else {
			matched = false;
		}
	}
	
	@Override
	public String toString() {
		return matched ? network + "/" + cidr : "No match";
	}
	
	public boolean matches() {
		return matched;
	}
	
	public String getNetwork() {
		return network;
	}
	
	public int getCIDR() {
		return cidr;
	}
	
	private boolean matchesCIDR(final String text, final Mode mode) {
		String parts[] = text.split("\\/");
		
		if (parts.length != 2) {
			return false;
		}
		try {
			cidr = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e) {
			return false;
		}
		if (cidr < 0) {
			return false;
		}
		if ((mode != Mode.IPv6Only
				&& matchesIPv4CIDR(parts[0], cidr))
			|| (mode != Mode.IPv4Only
				&& matchesIPv6CIDR(parts[0], cidr))) {
			network = parts[0];
			return true;
		}
		return false;
	}
	
	private boolean matchesRange(final String text, final Mode mode) {
		String parts[] = text.split("-");
		
		if (parts.length != 2) {
			return false;
		}
		
		if (mode != Mode.IPv6Only
				&& InternetAddressMatcher.matchesIPv4(parts[0])
				&& InternetAddressMatcher.matchesIPv4(parts[1])) {
			network = parts[0];
			cidr = 32; // XXX
			return true;
		}
		if (mode != Mode.IPv4Only
				&& InternetAddressMatcher.matchesIPv6(parts[0])
				&& InternetAddressMatcher.matchesIPv6(parts[1])) {
			network = parts[0];
			cidr = 128; // XXX
			return true;
		}
		return false;
	}
	
	private boolean matchesIPv4CIDR(final String addr, final int cidr) {
		return cidr <= 32 && InternetAddressMatcher.matchesIPv4(addr);
	}
	
	private boolean matchesIPv6CIDR(final String addr, final int cidr) {
		return cidr <= 128 && InternetAddressMatcher.matchesIPv6(addr);
	}
	
	/**
	 * matches any IPv4 or IPv6 network block.
	 * 
	 * @param text The string to match.
	 * @return <code>true</code> if the string matches.
	 */
	public static boolean matches(final String text) {
		return new NetblockMatcher(text, Mode.Any).matches();
	}
	
	/**
	 * matches an IPv4 network block.
	 * 
	 * @param text The string to match.
	 * @return <code>true</code> if the string matches.
	 */
	public static boolean matchesIPv4(final String text) {
		return new NetblockMatcher(text, Mode.IPv4Only).matches();
	}
	
	/**
	 * matches an IPv6 network block.
	 * 
	 * @param text The string to match.
	 * @return <code>true</code> if the string matches.
	 */
	public static boolean matchesIPv6(final String text) {
		return new NetblockMatcher(text, Mode.IPv6Only).matches();
	}
}
