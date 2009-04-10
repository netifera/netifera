package com.netifera.platform.net.http.internal.tools;

import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolProvider;
import com.netifera.platform.net.http.tools.AddWebSite;
import com.netifera.platform.net.http.tools.HTTPBasicAuthBruteforcer;
import com.netifera.platform.net.http.tools.HTTPProxy;
import com.netifera.platform.net.http.tools.WebApplicationScanner;
import com.netifera.platform.net.http.tools.WebCrawler;

public class ToolProvider implements IToolProvider {
	private final static String[] toolClassNames = { 
		AddWebSite.class.getName(),
		HTTPProxy.class.getName(),
		WebApplicationScanner.class.getName(),
		WebCrawler.class.getName(),
		HTTPBasicAuthBruteforcer.class.getName()
	};
	
	public ITool createToolInstance(String className) {
		if(className.equals(AddWebSite.class.getName())) {
			return new AddWebSite();
		} else if(className.equals(HTTPProxy.class.getName())) {
			return new HTTPProxy();
		} else if(className.equals(WebApplicationScanner.class.getName())) {
			return new WebApplicationScanner();
		} else if (className.equals(WebCrawler.class.getName())) {
			return new WebCrawler();
		} else if (className.equals(HTTPBasicAuthBruteforcer.class.getName())) {
			return new HTTPBasicAuthBruteforcer();
		}
		return null;
	}

	public String[] getProvidedToolClassNames() {
		return toolClassNames;
	}
}
