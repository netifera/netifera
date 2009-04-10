package com.netifera.platform.net.http.web.model;

import java.util.Locale;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.util.HexaEncoding;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class WebSiteEntity extends AbstractEntity {
	
	private static final long serialVersionUID = 8287912875498773311L;

	final public static String ENTITY_TYPE = "web.site";

	private final IEntityReference http;
	private final String hostname;
	private byte[] favicon;
	
	public WebSiteEntity(IWorkspace workspace, long realm, ServiceEntity http, String hostname) {
		super(ENTITY_TYPE, workspace, realm);
		
		this.hostname = hostname.toLowerCase(Locale.ENGLISH);
		this.http = http.createReference();
	}

	WebSiteEntity() {
		http = null;
		hostname = null;
	}
	
	public ServiceEntity getHTTP() {
		return (ServiceEntity) referenceToEntity(http);
	}
	
	public String getHostName() {
		return hostname == null ? "" : hostname;
	}
	
	public String getVirtualHostName() {
		if (hostname != null && hostname.length() > 0) {
			return hostname;
		}
		return getHTTP().getAddress().getAddress().toStringLiteral();
	}
	
	private boolean isDefaultPort(int port) {
		if (getHTTP().isSSL()) {
			return port == 443;
		}
		return port == 80;
	}
	
	public String getRootURL() {
		StringBuffer sb = new StringBuffer(128);
		
		sb.append("http");
		if (getHTTP().isSSL()) {
			sb.append('s');
		}
		sb.append("://");
		
		sb.append(getVirtualHostName());
		
		int port = getHTTP().getPort();
		if (!isDefaultPort(port)) {
			sb.append(':');
			sb.append(Integer.valueOf(port));
		}
		
		return sb.append('/').toString();
	}
	
	public void setFavicon(byte[] bytes) {
		favicon = bytes.clone();
	}
	
	public byte[] getFaviconBytes() {
		return favicon; // TODO read-only?
	}

	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		WebSiteEntity webSite = (WebSiteEntity) masterEntity;
		favicon = webSite.favicon;
	}

	@Override
	protected IEntity cloneEntity() {
		WebSiteEntity answer = new WebSiteEntity(getWorkspace(), getRealmId(), getHTTP(), hostname);
		answer.favicon = favicon;
		return answer;
	}
	
	public static String createQueryKey(long realmId, InternetAddress address, int port, String hostname) {
		return ENTITY_TYPE + ":" + realmId + ":" + HexaEncoding.bytes2hex(address.toBytes()) + ":" + port + ":" + hostname;
	}
	
	@Override
	protected String generateQueryKey() {
		ServiceEntity http = getHTTP();
		return createQueryKey(getRealmId(), http.getAddress().getAddress(), http.getPort(), hostname);
	}
}
