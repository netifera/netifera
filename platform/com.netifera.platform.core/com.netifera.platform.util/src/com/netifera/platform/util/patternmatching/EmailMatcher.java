package com.netifera.platform.util.patternmatching;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

// FIXME 'mailto:' scheme?
// TODO jdoc
public final class EmailMatcher implements ITextMatcher {
	/**
	 * RFC 2822 section 3.4.1:
	 * The "local-part" of an e-mail address can be up to 64 characters.
	 */
	private static final int MAX_LOCALPART_CHARS = 64;
	/**
	 * RFC 2822 section 3.4.1:
	 * The domain name a maximum of 255 characters.
	 */
	private static final int MAX_DOMAIN_CHARS = 255;
	
	private enum KEY { ACCOUNT, DOMAIN, ADDRESS, NORMALIZED }
	
	/** Boolean indicating whether or not the input matched. */
	private final boolean matched;
	
	private final Map<KEY, String> map;
	
	/**
	 * @param   text
	 *          the string to be matched
	 */
	public EmailMatcher(final String text) {
		boolean addressMatched = false, domainMatched = false;
		
		if (text == null) {
			matched = false;
			map = null;
			return;
		}
		int atIndex = text.lastIndexOf('@');
		if (atIndex <= 0) {
			matched = false;
			map = null;
			return;
		}
		String accountname = text.substring(0, atIndex);
		String destination = text.substring(atIndex + 1);
		if (accountname.length() == 0 || destination.length() == 0) {
			matched = false;
			map = null;
			return;
		}
		
		// first check the destination
		if (destination.charAt(0) == '[' && destination.endsWith("]")) {
			destination = destination.substring(1, destination.length() - 1);
			if (!InternetAddressMatcher.matches(destination)) {
				matched = false;
				map = null;
				return;
			}
			addressMatched = true;
		} else if (destination.length() <= MAX_DOMAIN_CHARS
				&& HostnameMatcher.matches(destination)) {
			// TODO normalize domain
			destination = destination.toLowerCase(Locale.ENGLISH);
			domainMatched = true;
		} else {
			matched = false;
			map = null;
			return;
		}
		
		// then check the username part
		if (accountname.startsWith("\"") && accountname.endsWith("\"")) {
			accountname = accountname.substring(1, accountname.length() - 1);
			// FIXME allow without parsing?
		} else if (!accountname.matches("[\\w-\\.\\+=/]*[\\w-]")) { // TODO doc
			matched = false;
			map = null;
			return;
		}
		
		if (accountname.length() > MAX_LOCALPART_CHARS) {
			matched = false;
			map = null;
			return;
		}
		
		matched = true;
		map = new EnumMap<KEY, String>(KEY.class);
		map.put(KEY.ACCOUNT, accountname);
		if (domainMatched) {
			map.put(KEY.DOMAIN, destination);
			map.put(KEY.NORMALIZED, accountname + "@" + destination);
		}
		if (addressMatched) {
			map.put(KEY.ADDRESS, destination);
			map.put(KEY.NORMALIZED, accountname + "@[" + destination + "]");
		}
	}
	
	@Override
	public String toString() {
		return matched ? map.toString() : "no email matched";
	}
	
	/**
	 * Test if this object matched an RFC 2822 email address format.
	 * 
	 * @return  <tt>true</tt> if, and only if, this string matches an email
	 *          address.
	 */
	public boolean matches() {
		return matched;
	}
	
	private String get(final KEY key) {
		if (matched && map.containsKey(key)) {
			return map.get(key);
		}
		return null;
	}
	
	public String getNormalizedEmail() {
		return get(KEY.NORMALIZED);
	}
	
	public String getAccountName() {
		return get(KEY.ACCOUNT);
	}
	
	public String getDomain() {
		return get(KEY.DOMAIN);
	}
	
	public String getInternetAddress() {
		return get(KEY.ADDRESS);
	}
	
	// TODO normalized() ?
	
	/**
	 * Test if the input text matches an RFC 2822 email address format.
	 * 
	 * @param text
	 *            The string to match.
	 * @return  <tt>true</tt> if, and only if, this string matches an email
	 *          address.
	 */
	public static boolean matches(final String text) {
		return new EmailMatcher(text).matches();
	}
}
