package com.netifera.platform.util.patternmatching;

public final class MacAddressMatcher implements ITextMatcher {
	/** a regular expression to match a MAC address. */
	private static final String MAC_ADDRESS_REGEX =
		"(?:[\\p{XDigit}]{1,2}:){5}[\\p{XDigit}]{1,2}";
	
	private final String value;
	
	/**
	 * @param   text
	 *          the string to be matched
	 */
	public MacAddressMatcher(final String text) {
		value = text;
	}
	
	/**
	 * Tells whether or not this string matches a MAC address.
	 * @return  <tt>true</tt> if, and only if, this string matches a MAC
	 *          address.
	 */
	public boolean matches() {
		return value == null ? false : value.matches(MAC_ADDRESS_REGEX);
	}
	
	/**
	 * Tells whether or not this string matches a MAC address.
	 * @param   text
	 *          the string to be matched
	 * @return  <tt>true</tt> if, and only if, this string matches a MAC
	 *          address.
	 */
	public static boolean matches(final String text) {
		return new MacAddressMatcher(text).matches();
	}
}
