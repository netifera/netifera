package com.netifera.platform.net.http.service.html;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpRequest;

public class WebForm extends WebLink {
	
	public WebForm(URI uri) {
		super(uri);
	}

	final private Map<String, String> parameters = new HashMap<String, String>();

	@Override
	public String method() {
		return "POST";
	}

	@Override
	public HttpRequest request() {
		HttpRequest answer = super.request();
		
		StringBuffer content = new StringBuffer();
		boolean first = true;
		for (String name: parameters.keySet()) {
			content.append(name);
			content.append("=");
			content.append(parameters.get(name)); // should url-encode
			if (!first) content.append("&");
			first = false;
		}
// XXX missing content		
//		answer.content(content.toString(), "application/x-www-form-urlencoded");
		return answer;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null || !(obj instanceof WebForm)) {
			return false;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
