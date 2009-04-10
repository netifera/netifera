package com.netifera.platform.net.http.web.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;

public class WebFormAuthenticationEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -8897665125312793990L;

	final public static String ENTITY_TYPE = "web.auth.forms";

	private IEntityReference site;
	private String path;
	private String method = "POST"; // GET or POST
	private String usernameField;
	private String passwordField;
	
	public WebFormAuthenticationEntity(IWorkspace workspace, long realm, IEntityReference site, String path, String usernameField, String passwordField) {
		super(ENTITY_TYPE, workspace, realm);
		
		this.site = site;
		this.path = path;
		this.usernameField = usernameField;
		this.passwordField = passwordField;
	}

	WebFormAuthenticationEntity() {}
	
	public WebSiteEntity getWebSite() {
		return (WebSiteEntity) referenceToEntity(site);
	}
	
	@Override
	protected IEntity cloneEntity() {
		//FIXME
		return this;
	}

}
