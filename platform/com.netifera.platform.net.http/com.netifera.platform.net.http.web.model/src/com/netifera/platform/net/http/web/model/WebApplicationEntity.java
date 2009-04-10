package com.netifera.platform.net.http.web.model;

import java.net.URI;
import java.net.URISyntaxException;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.ServiceEntity;

public class WebApplicationEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -2658295107593119953L;

	final public static String ENTITY_TYPE = "web.app";

	private final IEntityReference http;
	private String url;
	private final String serviceType;
	
	public WebApplicationEntity(IWorkspace workspace, long realm, IEntityReference http, String url, String serviceType) {
		super(ENTITY_TYPE, workspace, realm);
		
		try {
			this.url = new URI(url).normalize().toASCIIString();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		this.http = http;
		this.serviceType = serviceType;
	}
	
	WebApplicationEntity() {
		http = null;
		serviceType = null;
	}
	
	public ServiceEntity getHTTP() {
		return (ServiceEntity) referenceToEntity(http);
	}
	
	public void setURL(String url) {
		this.url = url;
	}
	
	public String getURL() {
		return url;
	}
	
	public String getServiceType() {
		return serviceType;
	}

	public void setVersion(String version) {
		setNamedAttribute("version", version);
	}
	
	public String getVersion() {
		return getNamedAttribute("version");
	}

	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		WebApplicationEntity webService = (WebApplicationEntity) masterEntity;
		url = webService.getURL();
	}

	@Override
	protected IEntity cloneEntity() {
		return new WebApplicationEntity(getWorkspace(),getRealmId(),http,url,serviceType);
	}
}
