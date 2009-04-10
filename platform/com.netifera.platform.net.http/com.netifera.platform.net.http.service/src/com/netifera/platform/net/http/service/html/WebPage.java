package com.netifera.platform.net.http.service.html;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.netifera.platform.util.patternmatching.EmailCollector;

public class WebPage {
	final private URI url;
	final private String content;
	
	public WebPage(URI uri, String content) {
		this.url = uri;
		this.content = content;
	}
	
	public URI url() {
		return url;
	}
	
	public String fileType() {
		String words[] = url.getPath().split("\\.");
		String ext = words[words.length - 1].toLowerCase();
		if (ext.length()>4) return null;
		return ext;
	}
	
	public Set<WebLink> links() {
		Set<WebLink> answer = new HashSet<WebLink>();
//		String protocolPattern = "[-a-z0-9]+://";
		String protocolPattern = "https?://";
		String hostPattern = "[-a-z0-9]+(\\.[-a-z0-9]+)*";
//		String pathPattern = "[-a-z0-9_:\\@&?=+,.!/~*'%\\$]*";
		String pathPattern = "[-a-z0-9_:\\@&?=+,.!/~*%\\$]*";
		String linkPattern = "("+protocolPattern+")?("+hostPattern+")?"+pathPattern;
		Pattern links = Pattern.compile("(href|src|action)=[\"'\\\\]*("+linkPattern+")[\"'\\\\]*", Pattern.CASE_INSENSITIVE);
		Pattern otherURLs = Pattern.compile("("+protocolPattern+"("+hostPattern+")?"+pathPattern+")", Pattern.CASE_INSENSITIVE);
		
		Matcher matcher = links.matcher(content);
		while (matcher.find()) {
			try {
				answer.add(new WebLink(url.resolve(matcher.group(2))));
//				System.out.println(url+" -> "+matcher.group(2));
			} catch (IllegalArgumentException e) {
//				System.err.println("Illegal URI: \""+matcher.group(2)+"\"");
			}
		}
		
		matcher = otherURLs.matcher(content);
		while (matcher.find()) {
			try {
				answer.add(new WebLink(url.resolve(matcher.group(1))));
//				System.out.println(url+" (other) -> "+matcher.group(1));
			} catch (IllegalArgumentException e) {
//				System.err.println("Illegal URI: \""+matcher.group(1)+"\"");
			}
		}

		return answer;
	}
	
	public Set<String> emails() {
		EmailCollector collector = new EmailCollector();
		collector.parse(content, EmailCollector.PARSE_ALL);
		return collector.results();
	}
}
