package com.netifera.platform.net.http.internal.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.net.http.service.HTTP;
import com.netifera.platform.net.http.tools.HTTPBasicAuthBruteforcer;
import com.netifera.platform.net.http.tools.WebApplicationScanner;
import com.netifera.platform.net.http.tools.WebCrawler;
import com.netifera.platform.net.http.web.model.HTTPBasicAuthenticationEntity;
import com.netifera.platform.net.http.web.model.WebPageEntity;
import com.netifera.platform.net.http.web.model.WebSiteEntity;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.tools.options.BooleanOption;
import com.netifera.platform.tools.options.GenericOption;
import com.netifera.platform.tools.options.IntegerOption;
import com.netifera.platform.tools.options.IterableOption;
import com.netifera.platform.tools.options.StringOption;
import com.netifera.platform.ui.actions.ToolAction;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;

public class EntityActionProvider implements IEntityActionProvider {

	public List<IAction> getActions(IShadowEntity entity) {
		List<IAction> answer = new ArrayList<IAction>();
		
		HTTP http = (HTTP) entity.getAdapter(HTTP.class);
		if (http != null) {
			if (entity instanceof ServiceEntity) {
				Set<String> names = ((ServiceEntity)entity).getAddress().getNames();
				if (names.isEmpty()) {
					addWebCrawler("Crawl web site", answer, http, http.getURI());
					addWebApplicationScanner("Scan for web applications", answer, http, null);
				} else {
					for (String vhost: names) {
						addWebCrawler("Crawl web site " + vhost, answer, http, http.getURI(vhost));
					}
					for (String vhost: names) {
						addWebApplicationScanner("Scan for web applications at " + vhost, answer, http, vhost);
					}
				}
			}
		} else if (entity instanceof WebPageEntity) {
			WebPageEntity page = (WebPageEntity) entity;
			http = (HTTP) page.getWebSite().getHTTP().getAdapter(HTTP.class);

			addWebCrawler("Crawl web site starting at "+page.getPath(), answer, http, page.getURL());
			
			if (page.getAuthentication() instanceof HTTPBasicAuthenticationEntity) {
//				HTTPBasicAuthenticationEntity auth = (HTTPBasicAuthenticationEntity) page.getAuthentication();
				ToolAction bruteforcer = new ToolAction("Bruteforce authentication", HTTPBasicAuthBruteforcer.class.getName());
				bruteforcer.addFixedOption(new GenericOption(HTTP.class, "target", "Target", "Target HTTP service", http));
				bruteforcer.addOption(new StringOption("hostname", "Host name", "Host name for the web site", page.getWebSite().getHostName()));
				bruteforcer.addOption(new StringOption("path", "Path", "Path that requires authentication", page.getPath()));
				bruteforcer.addOption(new StringOption("method", "Method", "GET/POST", "GET"));
				bruteforcer.addOption(new IterableOption(UsernameAndPassword.class, "credentials", "Credentials", "List of credentials to try", null));
				answer.add(bruteforcer);
			}
		} else if (entity instanceof WebSiteEntity) {
			WebSiteEntity site = (WebSiteEntity) entity;
			http = (HTTP) site.getHTTP().getAdapter(HTTP.class);
			addWebCrawler("Crawl web site", answer, http, site.getRootURL());
			addWebApplicationScanner("Scan for web applications", answer, http, site.getHostName());
		}
		
		return answer;
	}
	
	private void addWebCrawler(String name, List<IAction> answer, HTTP http, String startURL) {
		// Visit every page of the web server, starting from the given page.
		ToolAction webCrawler = new ToolAction(name, WebCrawler.class.getName());
		webCrawler.addFixedOption(new GenericOption(HTTP.class, "target", "Target", "Target HTTP service", http));
		webCrawler.addOption(new StringOption("url", "Base URL", "URL to start to crawl from", startURL));
		webCrawler.addOption(new BooleanOption("followLinks", "Follow links", "Follow links inside this website?", true));
		webCrawler.addOption(new BooleanOption("fetchImages", "Fetch images", "Fetch images following <img> tags?", false));
		webCrawler.addOption(new BooleanOption("scanWebApplications", "Scan common web applications", "Try common URLs for known web applications?", false));
		webCrawler.addOption(new IntegerOption("maximumConnections", "Maximum connections", "Maximum number of simultaneous connections", 10));
		answer.add(webCrawler);
	}
	
	private void addWebApplicationScanner(String name, List<IAction> answer, HTTP http, String hostname) {
		// Attempt to detect web applications
		ToolAction webApplicationScanner = new ToolAction(name, WebApplicationScanner.class.getName());
		webApplicationScanner.addFixedOption(new GenericOption(HTTP.class, "target", "Target", "Target HTTP service", http));
		webApplicationScanner.addOption(new StringOption("hostname", "Host name", "Host name for the web site", hostname != null ? hostname : http.getURIHost()));
		webApplicationScanner.addOption(new IntegerOption("maximumConnections", "Maximum connections", "Maximum number of simultaneous connections", 10));
		answer.add(webApplicationScanner);
	}

	public List<IAction> getQuickActions(IShadowEntity shadow) {
		// TODO Auto-generated method stub
		return null;
	}
}
