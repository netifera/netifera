package com.netifera.platform.util.patternmatching;

import java.util.Map;

public class SessionPattern implements ISessionPattern {
	private final IPattern clientPattern;
	private final IPattern serverPattern;
	
	public SessionPattern(final IPattern clientPattern, final IPattern serverPattern) {
		this.clientPattern = clientPattern;
		this.serverPattern = serverPattern;
	}
	
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		if (clientPattern != null) {
			buff.append("CLIENT: ");
			buff.append(clientPattern.toString());
		}
		if (serverPattern != null) {
			if (buff.length() > 0) {
				buff.append('\n');
			}
			buff.append("SERVER: ");
			buff.append(serverPattern.toString());
		}
		return buff.toString();
	}
	
	public Map<String, String> match(final String clientData, final String serverData) {
		Map<String,String> answer;
		if (clientPattern == null) {
			return serverPattern.match(serverData);
		}
		answer = clientPattern.match(clientData);
		if (answer == null) return null;
		if (serverPattern == null) return answer;
		if (!serverPattern.match(answer, serverData)) return null;
		return answer;
	}
}
