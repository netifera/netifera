package com.netifera.platform.util.patternmatching;

import java.util.Map;

public interface IPattern {
	Map<String, String> match(String data);
	boolean match(Map<String, String> answer, String data);
}
