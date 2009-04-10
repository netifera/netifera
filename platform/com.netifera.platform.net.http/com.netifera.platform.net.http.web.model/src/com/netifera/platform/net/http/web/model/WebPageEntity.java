package com.netifera.platform.net.http.web.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.util.HexaEncoding;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class WebPageEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -7452193469186147847L;

	final public static String ENTITY_TYPE = "web.page";

	private final IEntityReference site;
	private final String path;
	
	private String contentType;
	private IEntityReference authentication;
	
	public WebPageEntity(IWorkspace workspace, long realm, WebSiteEntity site, String path, String contentType) {
		super(ENTITY_TYPE, workspace, realm);
		
		this.site = site.createReference();
		this.path = path;
		this.contentType = contentType;
	}

	WebPageEntity() {
		site = null;
		path = null;
	}
	
	public WebSiteEntity getWebSite() {
		return (WebSiteEntity) referenceToEntity(site);
	}
	
	public String getPath() {
		return path;
	}
	
	public String getURL() {
		return getWebSite().getRootURL()+(path.charAt(0) == '/' ? path.substring(1) : path);
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void setAuthentication(IEntity authentication) {
		this.authentication = authentication.createReference();
	}
	
	public IEntity getAuthentication() {
		return referenceToEntity(authentication);
	}

	protected void synchronizeEntity(AbstractEntity masterEntity) {
		WebPageEntity page = (WebPageEntity) masterEntity;
		this.contentType = page.contentType;
		this.authentication = page.authentication.createClone();
	}
	
	@Override
	protected IEntity cloneEntity() {
		WebPageEntity answer = new WebPageEntity(getWorkspace(),getRealmId(),getWebSite(),path,contentType);
		answer.authentication = authentication == null ? null : authentication.createClone();
		return answer;
	}

	public static String createQueryKey(long realmId, InternetAddress address, int port, String hostname, String path) {
		return ENTITY_TYPE + ":" + realmId + ":" + HexaEncoding.bytes2hex(address.toBytes()) + ":" + port + ":" + hostname + ":" + path;
	}
	
	@Override
	protected String generateQueryKey() {
		WebSiteEntity site = getWebSite();
		String hostname = site.getHostName();
		ServiceEntity http = site.getHTTP();
		return createQueryKey(getRealmId(), http.getAddress().getAddress(), http.getPort(), hostname, path);
	}
}
