package com.netifera.platform.net.http.service;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestUserAgent;

import com.netifera.platform.util.locators.TCPSocketLocator;

public class HTTPClient {
	public static final String DEFAULT_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)";
	
	final private TCPSocketLocator locator;
	final private DefaultHttpClientConnection connection = new DefaultHttpClientConnection();
//	final private ConnectionReuseStrategy connectionStrategy = new DefaultConnectionReuseStrategy();
	
	public HTTPClient(TCPSocketLocator locator) {
		this.locator = locator;
	}

	public void close() {
		try {
			connection.close();
		} catch (IOException e) {
		}
	}

	public HttpResponse execute(HttpRequest request) throws IOException, HttpException {
		HttpParams params = new BasicHttpParams();
		// FIXME should be HTTP/1.0, must test
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUserAgent(params, DEFAULT_USER_AGENT);
		HttpProtocolParams.setUseExpectContinue(params, true);

		BasicHttpProcessor processor = new BasicHttpProcessor();
		// Required protocol interceptors
		processor.addInterceptor(new RequestContent());
//		processor.addInterceptor(new RequestTargetHost());
		// Recommended protocol interceptors
		processor.addInterceptor(new RequestConnControl());
		processor.addInterceptor(new RequestUserAgent());
		processor.addInterceptor(new RequestExpectContinue());

		HttpRequestExecutor executor = new HttpRequestExecutor();

		HttpContext context = new BasicHttpContext(null);
		
		context.setAttribute(ExecutionContext.HTTP_CONNECTION, connection);

		if (!connection.isOpen()) {
				Socket socket = new Socket(locator.getAddress().toInetAddress(), locator.getPort());
				connection.bind(socket, params);
		}
//		System.out.println(">> Request URI: "
//				+ request.getRequestLine().getUri());

		context.setAttribute(ExecutionContext.HTTP_REQUEST, request);
		request.setParams(params);
		executor.preProcess(request, processor, context);
		HttpResponse response = executor.execute(request, connection, context);
		executor.postProcess(response, processor, context);

//		System.out.println("<< Response: " + response.getStatusLine());
//		System.out.println(EntityUtils.toString(response.getEntity()));
//		System.out.println("==============");
/*		if (!connectionStrategy.keepAlive(response, context)) {
			connection.close();
		} else {
			System.out.println("Connection kept alive...");
		}
*/
		return response;
	}
	
	public HttpResponse GET(URI url) throws IOException, HttpException {
		HttpRequest request = new BasicHttpRequest("GET", url.getRawPath()+"?"+url.getRawQuery());

		// copied from firefox
		request.addHeader("Host", url.getPort() == 0 || url.getPort() == 80 ? url.getHost() : url.getHost()+":"+url.getPort());
		request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		request.addHeader("Accept-Language", "en-us,en;q=0.5");
//		request.addHeader("Accept-Encoding", "gzip,deflate");
//		request.addHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
//		request.addHeader("Keep-Alive", "300");
//		request.addHeader("Connection", "keep-alive");
		return execute(request);
	}
}
