package com.netifera.platform.util.patternmatching;

/* FIXME: <kevin> should be OSGi service so can add new matcher
 (phone number i.e.) */

/**
 * An engine that performs match operations on a {@link java.lang.CharSequence
 * <code>character sequence</code>}.
 * 
 * <p>This interface contains an unique method used to determine the results of
 * a match against a regular expression.
 * 
 * @see InternetAddressMatcher
 * @see HostnameMatcher
 * @see DomainMatcher
 * @see HttpUrlMatcher
 * @see EmailMatcher
 * @see MacAddressMatcher
 * @see TcpUdpPortMatcher
 */
public interface ITextMatcher {	// FIXME: rename?
	
	/** Alpha + ISO-8859 Latin-1. */
	String ALPHALATIN1 = "[\\p{Alpha}\\xc0-\\xff&&[^\\xd7\\xf7]]";
	
	/** Digit + Alpha + ISO-8859 Latin-1. */
	String ALNUMLATIN1 = "[\\p{Alnum}\\xc0-\\xff&&[^\\xd7\\xf7]]";
	
	/** Latin-1 word character. */
	String CHARLATIN1 = "[\\w\\xc0-\\xff-]";
	
	/**
	 * Tells whether or not this string matches.
	 * 
	 * @return <tt>true</tt> if, and only if, this string matches.
	 */
	boolean matches();
}
