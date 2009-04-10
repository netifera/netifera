package com.netifera.platform.util.patternmatching;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InternetAddressMatcher implements ITextMatcher {
	/** regex matching an IPv4 address. */
	private static final String IPV4_ADDRESS_REGEX =
		"(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)" +		/* 0 to 255 */
		"(\\."
			+ "(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)" +	/* '.' 0 to 255, 3 times */
		"){3}";
	
	/** regex matching an IPv6 address. */
	private static final String IPV6_ADDRESS_REGEX =
		"^(?:\\p{XDigit}{0,4}:){2,7}"
		+ "(?:"
			+ "(\\d+(\\.\\d+){3})"				// Mapped IPv4
		+ "|"
			+ "(?:\\p{XDigit}{0,4}"				// IPv6
			+ "(?:%\\d+)?)"						// optional scope
		+ ")$";
	
	private final String value;
	private final boolean invalid;
	
	/**
	 * @param   text
	 *          the string to be matched
	 */
	public InternetAddressMatcher(final String text) {
		value = text;
		invalid = text == null || text.length() < 2;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	/**
	 * Tells whether or not this string matches an IPv4/IPv6 address.
	 * @return  <tt>true</tt> if, and only if, this string matches a IP address.
	 */
	public boolean matches() {
		if (invalid || value.matches("[g-zG-Z]")) { // [AlNum && ^XDigit]
			return false;
		}
		return matchesIPv4() || matchesIPv6();
	}
	
	/**
	 * Tells whether or not this string matches an IPv4 address.
	 * @return  <tt>true</tt> if, and only if, this string matches a IPv4
	 *          address.
	 */
	public boolean matchesIPv4() {
		return !invalid && value.matches(IPV4_ADDRESS_REGEX);
	}
	
	/**
	 * Tells whether or not this string matches an IPv4 address.
	 * @param   text
	 *          the string to be matched
	 * @return  <tt>true</tt> if, and only if, this string matches a IPv4
	 *          address.
	 */
	public static boolean matchesIPv4(final String text) {
		return text.matches(IPV4_ADDRESS_REGEX);
	}
	
	/** Maximum number of 16-bits members in an IPv6 address. */
	private static final int MAX_IPV6_PARTS = 8;
	
	/**
	 * Tells whether or not this string matches an IPv6 address.
	 * @return  <tt>true</tt> if, and only if, this string matches a IPv6
	 *          address.
	 */
	@SuppressWarnings("fallthrough")
	public boolean matchesIPv6() {
		if (invalid || value.contains(":::")) {
			return false;
		}
		if (value.contains("-") && matchesIPv6RFC2732()) {
			return true;
		}
		String nvalue = value;
		// literal notation
		if (value.charAt(0) == '[') {
			int len = value.length();
			if (value.charAt(len - 1) == ']') {
				nvalue = value.substring(1, len - 1);
			}
		}
		int secondExt = 0;
		// ipv4 mapped
		if (nvalue.contains(".")) {
			String ipv4String = nvalue.substring(nvalue.lastIndexOf(':') + 1);
			if (!ipv4String.matches(IPV4_ADDRESS_REGEX)) {
				return false;
			}
			/* [1]: mapped IPv4 matched */
			secondExt = 1; // 2(v4=2x16bits) - 1(this)
		}
		// compressed notation
		if (nvalue.contains("::")) {
			String[] splitted = nvalue.split("::", -1);
			switch (splitted.length) {
			case 0: // loopback
				return true;
			case 2:
				secondExt = splitted[1].split(":").length;
			case 1:
				if (splitted[0].split(":").length + secondExt
						> MAX_IPV6_PARTS - 1) {
					return false;
				}
				break;
			default:
				return false;
			}
		} else if (nvalue.split(":").length + secondExt != MAX_IPV6_PARTS) {
			return false;
		}
		Matcher matcher = IPV6_ADDRESS_PATTERN.matcher(nvalue);
		
		if (!matcher.find()) {
			return false;
		}
		
		if (matcher.group(1) != null) {
			return true; /* already matched in [1] */
		}
		
		return true;
	}
	
	/**
	 * Tells whether or not this string matches an IPv6 address.
	 * @param   text
	 *          the string to be matched
	 * @return  <tt>true</tt> if, and only if, this string matches a IPv6
	 *          address.
	 */
	public static boolean matchesIPv6(final String text) {
		return new InternetAddressMatcher(text).matchesIPv6();
	}
	
	/**
	 * Compiled representation of the {@link #IPV6_ADDRESS_REGEX
	 * IPV6_ADDRESS_REGEX} regular expression.
	 */
	private static final Pattern IPV6_ADDRESS_PATTERN
		= Pattern.compile(IPV6_ADDRESS_REGEX);
	
	/**
	 * Compiled representation of a regular expression matching the RFC 2732
	 * IPv6 notation.
	 */
	private static final Pattern RFC2732_PATTERN
		= Pattern.compile("([\\p{XDigit}0-9sS-]+)\\.ipv6-literal\\.net\\.?",
			Pattern.CASE_INSENSITIVE);
	
	/**
	 * Tells whether or not this string matches an IPv6 address using the RFC
	 * 2732 notation.
	 * @return  <tt>true</tt> if, and only if, this string matches a IPv6
	 *          address using the RFC 2732 notation.
	 */
	public boolean matchesIPv6RFC2732() {
		Matcher matcher = RFC2732_PATTERN.matcher(value);
		if (!matcher.matches()) {
			return false;
		}
		return matchesIPv6(matcher.group(1)
				.replace('-', ':').replace('s', '%'));
	}
	
	/**
	 * Tells whether or not this string matches an IPv6 address using the RFC
	 * 2732 notation.
	 * @param   text
	 *          the string to be matched
	 * @return  <tt>true</tt> if, and only if, this string matches a IPv6
	 *          address using the RFC 2732 notation.
	 */
	public static boolean matchesIPv6RFC2732(final String text) {
		return new InternetAddressMatcher(text).matchesIPv6RFC2732();
	}
	
	/**
	 * Tells whether or not this string matches an IPv4/IPv6 address.
	 * @param   text
	 *          the string to be matched
	 * @return  <tt>true</tt> if, and only if, this string matches a IP address.
	 */
	public static boolean matches(final String text) {
		return new InternetAddressMatcher(text).matches();
	}
}
