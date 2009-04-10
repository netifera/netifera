package com.netifera.platform.util.patternmatching;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO rename WebServiceMatcher ?
// FIXME can this also support FTP, SVN, ... ?
// TODO see http://www.ietf.org/rfc/rfc3986.txt
// xml:urn?

public final class HttpUrlMatcher implements ITextMatcher {
	private enum KEY { SCHEME, HOSTNAME, PORT, USERNAME, PASSWORD, PATH }
	
	// FIXME vCard scheme?
	/*
	 * RFC 1738:
	 * 
	 * In general, URLs are written as follows:
	 * 
	 * 		<scheme>:<scheme-specific-part>
	 * 
	 * URL schemes that involve the direct use of an IP-based protocol to a
	 * specified host on the Internet use a common syntax for the
	 * scheme-specific data:
	 * 
	 * 		//<user>:<password>@<host>:<port>/<url-path>
	 * 
	 * Some or all of the parts "<user>:<password>@", ":<password>", ":<port>",
	 * and "/<url-path>" may be excluded.
	 * 
	 * Note that the "/" between the host (or port) and the url-path is NOT
	 * part of the url-path.
	 */
	private static final String HTTPURL_REGEX =
		"^(https?):\\/\\/"									// scheme
		+ "(?:"												// user(:pass)@
		+ 		"([\\S&&[^:]]*)"	// user (chars but ':')
		+ 		"(?::(\\S+))?"		// ':' + pass
		+ 		"@"					// '@'
		+ ")?"
		+ "("												// ip or hostname
		+ 		"\\[[\\p{XDigit}:\\.%]*\\]" //  IP
		+ 	"|"								//  or
		+ 		"[\\S&&[^:\\/]]+"			// host
		+ ")"
		+ "(?::(\\d+))?"									// tcp port
		+ "(?:\\/(\\S*))?$";								// path
	
	/** Compiled representation of a regular expression matching a http URL. */
	private static final Pattern HTTPURL_PATTERN =
		Pattern.compile(HTTPURL_REGEX);
	
	private final boolean matched;
	
	private final Map<KEY, String> map;
	
	// by default, match RFC compliant HTTP URL.
	public HttpUrlMatcher(final String text) {
		this(text, false);
	}
	
	/**
	 * @param matchtext The text to match.
	 * @param extendedFlag If <code>true</code>, this class with match some
	 * extended URL commonly used by web users (not strictly following the RFC).
	 */
	public HttpUrlMatcher(final String matchtext, final boolean extendedFlag) {
		if (matchtext == null) {
			matched = false;
			map = null;
			return;
		}
		String text = matchtext;
		// FIXME: lazy user
		if (extendedFlag && !matchtext.matches("^https?:\\/\\/.+")) {
			text = "http://" + matchtext; // FIXME correct scheme?
		}
		
		Matcher matcher = HTTPURL_PATTERN.matcher(text);
		if (!matcher.matches()) {
			matched = false;
			map = null;
			return;
		}
		String port = matcher.group(5);
		if (port == null) {
			if (matcher.group(1).length() == 4) {
				port = "80";
			} else {
				port = "443";
			}
		} else if (!TcpUdpPortMatcher.matches(port)) {
			matched = false;
			map = null;
			return;
		}
		String host = matcher.group(4);
		if (!HostnameMatcher.matches(host)
				&& !InternetAddressMatcher.matches(host)) {
			matched = false;
			map = null;
			return;
		}
		if (host.charAt(0) == '[') { // normalize ipv6
			host = host.substring(1, host.length() - 1); // ']' safely parsed
		}
		map = new EnumMap<KEY, String>(KEY.class);
		matched = true;
		map.put(KEY.SCHEME, matcher.group(1));
		map.put(KEY.USERNAME, matcher.group(2));
		map.put(KEY.PASSWORD, matcher.group(3));
		map.put(KEY.HOSTNAME, host);
		map.put(KEY.PORT, port);
		map.put(KEY.PATH, matcher.group(6));
	}
	
	@Override
	public String toString() {
		return matched ? map.toString() : "No match";
	}
	
	public boolean matches() {
		return matched;
	}
	
	public String getScheme() {
		return matched ? map.get(KEY.SCHEME) : null;
	}
	
	public String getHostname() {
		return matched ? map.get(KEY.HOSTNAME) : null;
	}
	
	public String getPath() {
		return matched ? map.get(KEY.PATH) : null;
	}
	
	public String getPort() {
		return matched && map.containsKey(KEY.PORT) ?
				map.get(KEY.PORT) : null;
	}
	
	public String getUsername() {
		return matched && map.containsKey(KEY.USERNAME) ?
				map.get(KEY.USERNAME) : null;
	}
	
	public String getPassword() {
		return matched && map.containsKey(KEY.PASSWORD) ?
				map.get(KEY.PASSWORD) : null;
	}
	
	public static boolean matches(final String text) {
		return new HttpUrlMatcher(text).matches();
	}
	
	public static boolean matchesExtended(final String text) {
		return new HttpUrlMatcher(text, true).matches();
	}
	
	/*
	public static boolean matchesFast(final String text) {
		URI uri;
		try {
			uri = new URI(text);
		} catch (URISyntaxException e) {
			return false;
		}
		if ("http".compareToIgnoreCase(uri.getScheme()) != 0) {
			return false;
		}
		return CommonMatcher.urlPathMatcher(uri.getPath())
			&& HostnameMatcher.matches(uri.getHost())
			|| InternetAddressMatcher.matches(uri.getHost());
	}
	*/
}
