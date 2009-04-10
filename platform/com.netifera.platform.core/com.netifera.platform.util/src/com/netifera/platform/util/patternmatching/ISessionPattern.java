package com.netifera.platform.util.patternmatching;

import java.util.Map;

public interface ISessionPattern {
	Map<String, String> match(String clientData, String serverData);
}
