package com.netifera.platform.net.http.tools;

import java.io.IOException;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.http.internal.tools.Activator;
import com.netifera.platform.net.http.service.HTTP;

public class WebApplicationScanner implements ITool {
	private IToolContext context;
	private HTTP http;
	private String hostname;

	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;

		context.setTitle("Web applications scanner");
		setupToolOptions();

		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		long realm = probe.getEntity().getId();

		context.setTitle("Scan web applications at "+http.getLocator());
		try {
			WebSpider spider = new WebSpider(http);
			spider.setHostName(hostname);
			spider.setFollowLinks(false);
			spider.setContext(context);
			spider.setRealm(realm);
			if (context.getConfiguration().get("maximumConnections") != null)
				spider.setMaximumConnections((Integer)context.getConfiguration().get("maximumConnections"));
			for (String url: Activator.getInstance().getWebApplicationDetector().getTriggers())
				spider.addURL(spider.getBaseURL().resolve(url));
			spider.run();
		} catch (IOException e) {
			context.exception("I/O error: " + e.getMessage(), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			context.warning("Interrupted");
		} finally {
			context.done();
		}
	}
	
	private void setupToolOptions() {
		http = (HTTP) context.getConfiguration().get("target");
		hostname = (String) context.getConfiguration().get("hostname");
		if (hostname == null) {
			hostname = http.getURIHost();
		}
	}
}
