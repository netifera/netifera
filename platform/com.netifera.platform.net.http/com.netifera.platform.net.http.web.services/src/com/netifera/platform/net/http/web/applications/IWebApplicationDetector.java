package com.netifera.platform.net.http.web.applications;

import java.util.List;
import java.util.Map;

public interface IWebApplicationDetector {
	public List<String> getTriggers();
	public Map<String,String> detect(String request, String response);
}
