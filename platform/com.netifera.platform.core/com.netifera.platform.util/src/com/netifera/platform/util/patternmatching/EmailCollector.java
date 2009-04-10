package com.netifera.platform.util.patternmatching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailCollector {
	private static final int PARSE_NOTHING	= 0x00;
	public static final int PARSE_HTML		= 0x01;
	public static final int PARSE_STRINGS	= 0x02;
	public static final int PARSE_ALL		= PARSE_HTML | PARSE_STRINGS;
	
	private final static Pattern p_email_text = Pattern.compile(
			"(?i)" +								// CASE_INSENSITIVE
			"[\\w-\\.]+" +							// account
			"(?:([\\s_])+(?:at|\\(\\s*\\))\\1+)" +	// '@'
	// TODO use similar HostnameMatcher.HOSTNAME_REGEX
			"(?:[\\w-]+(?:\\s+(dot|!)\\s+|\\.))+" +	// domain + '.'
			"[a-zA-Z]{2,4}");						// tld
	
	//private final static Pattern p_ip_mail =Pattern.compile(
	//		"(.*@)\\[([\\d\\.]+)\\]");
	
	//private final static Pattern p_quoted_text = Pattern.compile("\"(.*)\"");

	// FIXME use hostname matcher also here?
	public static final String EMAIL_REGEX =
		"[\\w-\\.\\+=/]*[\\w-]" +					// account
		"@" +										// '@'
		"(?:" + HostnameMatcher.HOSTNAME_REGEX + "\\.)+" + // domain 
		HostnameMatcher.HOSTNAME_REGEX;				// tld
	private final static Pattern p_email = Pattern.compile(EMAIL_REGEX);
	
	private final Set<String> emailsSet;
	private final SpecialHTMLCharactersUnescaper unescaper;
	
	public EmailCollector() {
		 emailsSet = new HashSet<String>();
		 unescaper = new SpecialHTMLCharactersUnescaper();
	}
	
	/**
	 * Add the email addresses parsed in the list of collected emails
	 * @return List of email addresses contained in the text argument.
	 */
	public List<String> parse(final String text) {
		return parse(text, PARSE_NOTHING);
	}
	
	public List<String> parse(final String text, final int level) {
		List<String> list = new ArrayList<String>();
		// parse...
		String lines = text;
		Matcher matcher;
		
		/* process special HTML codes (called "character entities") */
		if ((level & PARSE_HTML) != 0 && lines.contains("&#")) {
			lines = unescaper.unescape(lines);
		}
		if ((level & PARSE_STRINGS) == 0 && !lines.contains("@")) {
			return list;
		}
		
		matcher = p_email.matcher(lines);
		while (matcher.find()) {
			/*
			 * local-part is case sensitive, however, discouraged by RFC 2821
			 */
			addEmail(list, matcher.group(0));
		}
		
		if ((level & PARSE_STRINGS) != 0) {
			matcher = p_email_text.matcher(lines);
			while (matcher.find()) {
				addEmail(list, matcher.group(0).toLowerCase(Locale.ENGLISH)
						.replaceAll("[\\s_]+(at|\\(\\))[\\s_]+", "@") // at, ()
						.replaceAll("\\s+(dot|!)\\s+", "."));
			}
		}
		
		emailsSet.addAll(list);
		return list;
	}
	
	private void addEmail(List<String> list, String email) {
		/* sanitize */
		if (EmailMatcher.matches(email)) {
			list.add(email);
		}
	}
	
	/**
	 * @return Set of all email addresses parsed so far.
	 */
	public Set<String> results() {
		return emailsSet;
	}
	
	/**
	 * @return List of email addresses contained in the text argument.
	 */
	public static List<String> emailListBuilder(final String text) {
		return emailListBuilder(text, 0);
	}
	
	public static List<String> emailListBuilder(final String text, final int level) {
		return new EmailCollector().parse(text, level);
	}
}
