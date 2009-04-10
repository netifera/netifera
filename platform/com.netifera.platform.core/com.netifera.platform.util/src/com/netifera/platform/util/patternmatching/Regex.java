package com.netifera.platform.util.patternmatching;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex implements IPattern {
	private Pattern pattern;
	private final Map<Integer, String> groupNames =
		new HashMap<Integer, String>();
	private final Map<String, String> defaults = new HashMap<String, String>();
	
	public static Regex caseInsensitive(final String pattern) {
		return new Regex(Pattern.compile(pattern, Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE));
	}
	
	public Regex(final String pattern) {
		this(Pattern.compile(pattern, Pattern.MULTILINE|Pattern.DOTALL));
	}
	
	public Regex(final Pattern pattern) {
		this.pattern = pattern;
	}
	
	@Override
	public String toString() {
		return pattern.toString();
	}
	
	public void add(final Integer index, final String name) {
		groupNames.put(index, name);
	}
	
	public void add(final Integer index, final String name, final String defaultValue) {
		add(index, name);
		defaults.put(name, defaultValue);
	}
	
	public void add(final String name, final String defaultValue) {
		defaults.put(name, defaultValue);
	}
	
	public boolean match(final Map<String, String> answer, final String data) {
		Matcher matcher = pattern.matcher(data);
		
		if (!matcher.matches()) return false;
		fillOutAnswer(answer, matcher);
		return true;
	}
	
	public Map<String, String> match(final String data) {
		Matcher matcher = pattern.matcher(data);
		
		if (!matcher.matches()) return null;
		Map<String, String> answer = new HashMap<String,String>();
		fillOutAnswer(answer, matcher);
		return answer;
	}
	
	private void fillOutAnswer(final Map<String,String> answer, final Matcher matcher) {
		for (String name: defaults.keySet()) {
			answer.put(name, defaults.get(name));
		}
		for (Integer groupNumber: groupNames.keySet()) {
			answer.put(groupNames.get(groupNumber), new String(matcher.group(groupNumber.intValue()).trim()));
		}
	}
}
