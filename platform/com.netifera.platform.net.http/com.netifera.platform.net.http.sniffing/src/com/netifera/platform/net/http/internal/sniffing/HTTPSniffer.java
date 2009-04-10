package com.netifera.platform.net.http.internal.sniffing;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.netifera.platform.net.daemon.sniffing.IStreamModuleContext;
import com.netifera.platform.net.daemon.sniffing.ITCPBlockSniffer;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferConfig;
import com.netifera.platform.net.sniffing.stream.ISessionKey;
import com.netifera.platform.util.locators.TCPSocketLocator;

public class HTTPSniffer implements ITCPBlockSniffer {

	public IPacketFilter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "HTTP Sniffer";
	}

	public void initialize(IBlockSnifferConfig config) {
		config.setTotalLimit(1024);
	}

	private static Pattern responsePattern = Pattern.compile("(?s)^HTTP/1\\.[01] [\\d]+ \\w.*");
	private static Pattern requestPattern = Pattern.compile("(?s)^.+ .+ HTTP/1\\.[01].*");
	
	public void handleBlock(IStreamModuleContext ctx, ByteBuffer clientData,
			ByteBuffer serverData) {

		String requestString = stringFromByteBuffer(clientData);
		String responseString = stringFromByteBuffer(serverData);
		
		if (!responsePattern.matcher(responseString).matches())
			return;

		if (!requestPattern.matcher(requestString).matches())
			return;

		String[] requestLines = requestString.split("[\\r\\n]");
		String[] responseLines = responseString.split("[\\r\\n]");

		String userAgent = null;
		for (String header: requestLines) {
			if (header.startsWith("User-Agent: ")) {
				userAgent = header.substring(12);
				break;
			}
		}
		String contentType = null;
		for (String header: responseLines) {
			if (header.startsWith("Content-Type: ")) {
				contentType = header.substring(14);
				break;
			}
		}
		
		ISessionKey key = ctx.getKey();
		long realm = ctx.getRealm();
		long spaceId = ctx.getSpaceId();
		TCPSocketLocator service = new TCPSocketLocator(key.getServerAddress(),
				key.getServerPort());
		//if (contentType != null) // FIXME
		Activator.getInstance().getWebEntityFactory().createRequestResponse(
				realm, spaceId, key.getClientAddress(), getClientInfo(userAgent), service,
				requestLines[0], responseLines[0], contentType);
	}
	
	private static Pattern mozillaOsPattern =
		Pattern.compile("Mozilla[^\\(]*\\(([^\\)]*).*");
	
	private Map<String, String> getClientInfo(String userAgent) {
		Map<String, String> info = new HashMap<String, String>();
		info.put("serviceType", "HTTP");
		if (userAgent != null) {
			info.put("product", userAgent);
			Matcher m = mozillaOsPattern.matcher(userAgent);
			if (m.matches()) {
				info.put("os", m.group(1));
			}
		}
		return info;
	}
	
	private String stringFromByteBuffer(ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		try {
			return new String(bytes, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding: ISO-8859-1");
		}
	}
}
