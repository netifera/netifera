package com.netifera.platform.net.http.tools;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.protocol.HttpRequestExecutionHandler;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.net.dns.model.EmailAddressEntity;
import com.netifera.platform.net.http.internal.tools.Activator;
import com.netifera.platform.net.http.service.AsynchronousHTTPClient;
import com.netifera.platform.net.http.service.HTTP;
import com.netifera.platform.net.http.service.html.WebLink;
import com.netifera.platform.net.http.service.html.WebPage;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.TCPSocketLocator;
import com.netifera.platform.util.patternmatching.InternetAddressMatcher;

public class WebSpider {
	final private HTTP http;
	private boolean followLinks = true;
	private boolean fetchImages = false;
	private int maximumConnections = 5;
	private URI base;// = URI.create("http:///");
	private String hostname = null;
	private final Set<String> knownSites = new HashSet<String>();
	private final Set<String> knownPaths = new HashSet<String>();
	private final Queue<URI> urlsQueue = new LinkedList<URI>();
	private long realm;

	private IToolContext toolContext;

	private volatile int successCount = 0;
	private volatile int errorsCount = 0;
	private volatile boolean interrupted = false;

	
	class WebSpiderWorker implements HttpRequestExecutionHandler {
		private URI url = null;

		public void initalizeContext(final HttpContext context,
				final Object attachment) {
//			if (base != null && base.getHost() != null)
//				context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, new HttpHost(base.getHost()));
//			else
//				context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, new HttpHost(""));
		}

		public void finalizeContext(final HttpContext context) {
			if (context.getAttribute("url")!=null) {
				errorsCount += 1;
				retryURL((URI)context.getAttribute("url"));
			}
		}

		public HttpRequest submitRequest(final HttpContext context) {
			if (interrupted || isExceededErrorThreshold()) {
				return null;
			}
			
			url = nextURLOrNull();
			if (url == null) {
				toolContext.debug("No more requests to submit");
				return null; // no new request to submit
			}
			
			String page = url.getRawPath();
			if (page.length() == 0)
				page = "/";
			if (url.getRawQuery() != null) page += "?"+url.getRawQuery();
			
			HttpRequest request = new BasicHttpRequest("GET", page);
			
			request.addHeader("Host", http.getURIHostPort(hostname));
			
			context.setAttribute("request", request);
			context.setAttribute("url", url);
			
			return request;
		}

		public void handleResponse(final HttpResponse response,
				final HttpContext context) {
			successCount += 1;
			HttpEntity entity = response.getEntity();
			try {
				HttpRequest request = (HttpRequest) context.getAttribute("request");
				int status = response.getStatusLine().getStatusCode();
				URI url = (URI)context.getAttribute("url");
				context.setAttribute("url", null);

				byte[] contentBytes = null;
				String content = null;
				
				if (status < 200 || status >= 400) {
					toolContext.error(request.getRequestLine()+" ->  "+response.getStatusLine().toString());
					if (status == 401) {
						Header header = response.getFirstHeader("WWW-Authenticate");
						if (header != null) {
							String method = header.getValue().split(" ")[0];
							if (method.toLowerCase().equals("basic")) {
								String authRealm = header.getValue().split("\"")[1];
								toolContext.info("Basic authentication realm \""+authRealm+"\" at "+url);
								Activator.getInstance().getWebEntityFactory().createBasicAuthentication(realm, toolContext.getSpaceId(), http.getLocator(), url, authRealm);
							}
						}
					}
				} else {
					toolContext.info(request.getRequestLine()+" ->  "+response.getStatusLine().toString());
				}
				
				if (entity.getContentType() != null) {
					String contentType = entity.getContentType().getValue();
					
					// is favicon? get it and add it to the model
					if (status == 200 && url.getPath().equals("/favicon.ico") && contentType.matches("image/x-icon|application/octet-stream|text/plain")) {
						entity.consumeContent();
						int length = (int) entity.getContentLength();
						if (length > 0) {
							contentBytes = new byte[length];
							if (entity.getContent().read(contentBytes) == length) {
								Activator.getInstance().getWebEntityFactory().setFavicon(realm, toolContext.getSpaceId(), http.getLocator(), url, contentBytes);
							}
						}
					} else {
						if (status == 200)
							Activator.getInstance().getWebEntityFactory().createWebPage(realm, toolContext.getSpaceId(), http.getLocator(), url, contentType);

						if (contentType.matches("(text/|application/x-javascript).*")) {
							content = EntityUtils.toString(entity);
							WebPage page = new WebPage(url, content);
							if (followLinks) {
								for (WebLink link : page.links()) {
									if (interrupted) return;
									follow(link.url());
								}
							}
							for (String email: page.emails()) {
								if (interrupted) return;
								EmailAddressEntity e = Activator.getInstance().getDomainEntityFactory().createEmailAddress(realm, 0, email);
								e.addTag(base.toString());
								e.save();
								e.addToSpace(toolContext.getSpaceId());
							}
						}
					}
				}

				// redirect
				if (status >= 300) {
					Header locationHeader = response.getFirstHeader("Location");
					if (locationHeader != null) {
						URI location = URI.create(locationHeader.getValue());
						toolContext.warning("Redirect "+url+" to "+location);
						int port = location.getPort() == -1 ? 80 : location.getPort();
						String hostname = location.getHost();
						List<InternetAddress> addresses;
						if (InternetAddressMatcher.matches(hostname)) {
							addresses = new ArrayList<InternetAddress>(1);
							addresses.add(InternetAddress.fromString(hostname));
						} else {
							addresses = Activator.getInstance().getNameResolver().getAddressesByName(hostname);
						}
						for (InternetAddress address : addresses) {
							Activator.getInstance().getWebEntityFactory().createWebSite(realm, toolContext.getSpaceId(), new TCPSocketLocator(address, port), hostname);
						}
						if (followLinks)
							follow(url.resolve(location));
					}
				}
				
				// now attempt web service detection
				if (contentBytes == null) {
					if (content != null) {
						contentBytes = content.getBytes();
					} else {
						if (entity.getContentLength() > 0 && entity.getContentLength() < 1024) {
							entity.consumeContent();
							contentBytes = new byte[(int)entity.getContentLength()];
							entity.getContent().read(contentBytes); // XXX if retval < contentLength content appears broken
						} else {
							contentBytes = new byte[0];
						}
					}
				}
				detectWebService(url, request, response, contentBytes);
			} catch (IOException ex) {
				toolContext.exception("I/O error when handling response: " + ex.getMessage(), ex);
			} catch (Exception ex) {
				toolContext.exception("Error when handling response: " + ex.getMessage(), ex);
			}
		}
	}

	public void setRealm(long realm) {
		this.realm = realm;
	}
	
	public void setContext(IToolContext context) {
		this.toolContext = context;
	}

	public void setBaseURL(URI base) {
		this.base = base;
		
		if (hostname == null && base.getHost() != null && base.getHost().length()>0)
			hostname = base.getHost();
	}
	
	public URI getBaseURL() {
		return base;
	}
	
	public void setHostName(String hostname) {
		this.hostname = hostname;
		this.base = URI.create(http.getURI(hostname));
	}
	
	public void setFollowLinks(boolean followLinks) {
		this.followLinks = followLinks;
	}
	
	public void setFetchImages(boolean fetchImages) {
		this.fetchImages = fetchImages;
	}

	public void setMaximumConnections(int maximumConnections) {
		this.maximumConnections = maximumConnections;
	}
	
	public WebSpider(HTTP service) {
		this.http = service;
	}

	private void detectWebService(URI url, HttpRequest request, HttpResponse response, byte[] content) {
		// FIXME there must be a more efficient way to do this
		StringBuffer requestBuffer = new StringBuffer();
		StringBuffer responseBuffer = new StringBuffer();
		
		requestBuffer.append(request.getRequestLine());
		requestBuffer.append('\n');
		for (Header header: request.getAllHeaders()) {
			requestBuffer.append(header);
			requestBuffer.append('\n');
		}
		
		responseBuffer.append(response.getStatusLine());
		responseBuffer.append('\n');
		for (Header header: response.getAllHeaders()) {
			responseBuffer.append(header);
			responseBuffer.append('\n');
		}
		responseBuffer.append('\n');
		
		responseBuffer.append(new String(content));
		
		Map<String,String> serviceInfo = Activator.getInstance().getWebApplicationDetector().detect(requestBuffer.toString(), responseBuffer.toString());
		if (serviceInfo != null) {
			Activator.getInstance().getWebEntityFactory().createWebApplication(realm, toolContext.getSpaceId(), http.getLocator(), url, serviceInfo);
			toolContext.info(serviceInfo.get("serviceType")+" detected at "+url);
		}
	}
	
	public void run() throws InterruptedException, IOException {
		interrupted = false;
		final AsynchronousHTTPClient client = http.createAsynchronousClient(new WebSpiderWorker());
		try {
			while (!Thread.currentThread().isInterrupted()) {
				if (!hasNextURL()) {
					toolContext.debug("No next URL.. waiting..");
					Thread.sleep(2000);
				}
				if (!hasNextURL())
					Thread.sleep(5000);
				if (!hasNextURL())
					Thread.sleep(5000);
				
				if (!hasNextURL()) {
					while (client.getConnectionsCount() >= maximumConnections && !isExceededErrorThreshold() && !Thread.currentThread().isInterrupted()) {
						toolContext.debug("Waiting, still "+client.getConnectionsCount()+" active connections");
						Thread.sleep(1000);
					}
					break;
				}
				
				// XXX gracefully handle the case when cannot connect
				// for example blogspot, tripod or wikipedia from cn
				toolContext.debug(">>>> launch new client");
				client.connect(new SessionRequestCallback() {

					public void cancelled(SessionRequest request) {
						errorsCount++;
					}

					public void completed(SessionRequest request) {
						successCount++;						
					}

					public void failed(SessionRequest request) {
						if (request.getException() != null) {
							toolContext.error("Can not connect: " + request.getException().getMessage());
						}
						errorsCount++;
					}

					public void timeout(SessionRequest request) {
						errorsCount++;
					}
					
				});
				
				Thread.sleep(500);
				
				while (client.getConnectionsCount() >= maximumConnections && !isExceededErrorThreshold() && !Thread.currentThread().isInterrupted()) {
					toolContext.debug("Waiting, currently already "+client.getConnectionsCount()+" connections");
					Thread.sleep(1000);
				}
				
				if (isExceededErrorThreshold()) {
					toolContext.error("Exceeded maximum number of errors: "+errorsCount+"/"+successCount);
					break;
				}
			}
		} finally {
			if (Thread.currentThread().isInterrupted())
				interrupted = true;
			client.shutdown();
		}
	}

	private boolean isExceededErrorThreshold() {
		return errorsCount > ((successCount + 1) * 5);
	}
	
	private synchronized boolean hasNextURL() {
		return !urlsQueue.isEmpty();
	}

	private synchronized URI nextURLOrNull() {
		return urlsQueue.poll();
	}

	private synchronized void follow(URI url) {
		url = url.normalize();
		String path = url.getPath();
		if (path == null) {
			toolContext.debug("Bad URL, null path: "+url);
			return; // bad url, like javascript:void
		}
		String host = url.getHost();
		if (host == null) {
			toolContext.debug("Bad URL, null host: "+url);
			return; // bad url, like javascript:void
		}
		if (!fetchImages && path.matches(".*(jpg|gif|png)$"))
			return;
		// TODO improve "outside site" concept
		int basePort = base.getPort() == -1 ? 80 : base.getPort();
		int urlPort = url.getPort() == -1 ? 80 : url.getPort();
		// follow redirects only to subdomains
		if (!host.equals(base.getHost()) || basePort != urlPort) {
			toolContext.debug("Ignoring "+url+" (outside site)");
			String site = url.resolve("/").toString();
			knownSites.add(site);
			return;
		}
		addURL(url);
	}
	
	public synchronized void addURL(URI url) {
		String path = url.getPath();
		if (path.length() == 0)
			path = "/";
		if (knownPaths.contains(path))
			return; //FIXME what if we want to send again the same request with different query parameters?
		knownPaths.add(path);
		urlsQueue.add(url);
	}
	
	public synchronized void retryURL(URI url) {
		toolContext.warning("Retrying "+url);
		urlsQueue.add(url);
	}
}
