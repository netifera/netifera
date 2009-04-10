package com.netifera.platform.util.patternmatching;

public final class TcpUdpPortMatcher implements ITextMatcher {
	private static final int PORT_MIN = 0;
	private static final int PORT_MAX = 0xffff;
	private final String value;
	
	/**
	 * @param   text
	 *          the string to be matched
	 */
	public TcpUdpPortMatcher(final String text) {
		value = text;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	/**
	 * Tells whether or not this string matches a TCP or UDP port.
	 * @return  <tt>true</tt> if, and only if, this string matches a TCP/UDP
	 *          port.
	 */
	public boolean matches() {
		if (value == null) {
			return false;
		}
		int port;
		try {
			port = Integer.decode(value).intValue();
		} catch (NumberFormatException e) {
			return false;
		}
		return port >= PORT_MIN && port <= PORT_MAX;
	}
	
	/**
	 * Tells whether or not this string matches a TCP or UDP port.
	 * @param   text
	 *          the string to be matched
	 * @return  <tt>true</tt> if, and only if, this string matches a TCP/UDP
	 *          port.
	 */
	public static boolean matches(final String text) {
		return new TcpUdpPortMatcher(text).matches();
	}
}
