package com.netifera.platform.util.patternmatching;

public final class HostnameMatcher implements ITextMatcher {
	/**
	 * RFC 2181 section 11:
	 * The length of any one label is limited to between 1 and 63 octets.
	 */
	private static final int MAX_LABEL_CHARS = 63;
	
	/** regex matching a host name. */
	static final String HOSTNAME_REGEX = // TODO document (james)
		ALNUMLATIN1 + "+"
		+ "(?:"
			+ CHARLATIN1 + "*"
			+ ALNUMLATIN1
		+ ")*";
	
	/** The value is used for string storage. */
	private final String value;
	
	/**
	 * @param   text
	 *          the string to be matched
	 */
	public HostnameMatcher(final String text) {
		value = text;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	/**
	 * Test if the input text matches a host name.
	 * 
	 * @return  <tt>true</tt> if, and only if, this string matches a host name.
	 */
	public boolean matches() {
		if (value == null || value.contains(":")) { // no ipv6
			return false;
		}
		String ctext = value;
		if (value.endsWith(".")) { // allow 1 optional trailing dot '.'
			ctext = value.substring(0, value.length() - 1);
		}
		String[] parts = ctext.split("\\.");
		if (parts == null || parts.length == 0) {
			return false;
		}
		 // TLD must contains letter (else is probably an IP address)
		if (parts[parts.length - 1].matches("\\d+")) {
			return false;
		}
		for (String part : parts) {
			if (part.length() == 0 // illegal: 2 consecutives dots
					|| part.length() >= MAX_LABEL_CHARS
					|| !part.matches(HOSTNAME_REGEX)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Test if the input text matches a host name.
	 * 
	 * @param text The string to match.
	 * @return  <tt>true</tt> if, and only if, this string matches a host name.
	 */
	public static boolean matches(final String text) {
		return new HostnameMatcher(text).matches();
	}
}
