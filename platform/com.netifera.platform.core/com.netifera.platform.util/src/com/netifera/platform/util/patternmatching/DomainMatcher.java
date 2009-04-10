package com.netifera.platform.util.patternmatching;

public final class DomainMatcher implements ITextMatcher {
	/**
	 * RFC 2181 section 11:
	 * A full domain name is limited to 255 octets (including the separators).
	 */
	private static final int MAX_DOMAINNAME_CHARS = 255;
	private final String value;
	
	/**
	 * @param   text
	 *          the string to be matched
	 */
	public DomainMatcher(final String text) {
		value = text;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	/**
	 * Test if the input text matches a domain name.
	 * 
	 * @return  <tt>true</tt> if, and only if, this string matches a domain
	 * name.
	 */
	public boolean matches() {
		if (value == null) {
			return false;
		}
		return value.length() > 1 && value.charAt(0) == '.' // starts with dot
			&& value.length() < MAX_DOMAINNAME_CHARS
			&& HostnameMatcher.matches(value.substring(1));
	}
	
	/**
	 * Test if the input text matches a domain name.
	 * 
	 * @param text The string to match.
	 * @return  <tt>true</tt> if, and only if, this string matches a domain
	 * name.
	 */
	public static boolean matches(final String text) {
		return new DomainMatcher(text).matches();
	}
	
	/* TODO
	public boolean isIANATLD() {
		// check http://data.iana.org/TLD/tlds-alpha-by-domain.txt
	}
	*/
}
