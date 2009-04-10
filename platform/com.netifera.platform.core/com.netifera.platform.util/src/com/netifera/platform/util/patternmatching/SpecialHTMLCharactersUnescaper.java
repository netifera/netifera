package com.netifera.platform.util.patternmatching;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecialHTMLCharactersUnescaper {

	/* see http://wdvl.internet.com/Authoring/HTML/Entities/common.html */
	private final static Pattern p_html_code =
		Pattern.compile("&(#\\p{Digit}+|#x\\p{XDigit}+|\\p{Alpha}+);");

	public String unescape(String lines) {
		Matcher matcher = p_html_code.matcher(lines);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String htmlCode = matcher.group(1);
			String unescapedCode;
			if (htmlCode.charAt(0) == '#') {
				if (htmlCode.charAt(1) == 'x') {
					unescapedCode = valueOfHexa(htmlCode);
				} else {
					unescapedCode = valueOfInteger(htmlCode);
				}
			} else {
				unescapedCode = valueOfName(htmlCode);
			}
			matcher.appendReplacement(sb, unescapedCode);
		}
		return matcher.appendTail(sb).toString();
	}
	
	private static String replaceableValueOfChar(char c) {
		switch (c) {
		case '$':
		case '\\':
			return '\\' + String.valueOf(c);
		default:
			return String.valueOf(c);
		}
	}
	
	private static String valueOfInteger(String code) {
		return replaceableValueOfChar(
				(char)Integer.parseInt(code.substring(1)));
	}
	
	private static String valueOfHexa(String code) {
		return replaceableValueOfChar(
				(char)Integer.parseInt(code.substring(2), 0x10));
	}
	
	@Deprecated // until correctly implemented // james
	// kludged this code is safe though
	private static String valueOfName(String code) {
		// <KLUDGE> to avoid IndexOutOfBoundsException
		StringBuilder result = new StringBuilder();
		StringCharacterIterator iterator = new StringCharacterIterator(code);
		char character =  iterator.current();
		while (character != CharacterIterator.DONE ) {
			if (character == '$') {
				result.append("\\$");
			} else if (character == '\\') {
					result.append("\\\\");
			} else {
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
		// </KLUDGE>
	}
}
