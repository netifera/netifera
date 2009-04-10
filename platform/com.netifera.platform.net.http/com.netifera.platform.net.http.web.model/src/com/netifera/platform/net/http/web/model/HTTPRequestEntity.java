package com.netifera.platform.net.http.web.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.ClientServiceConnectionEntity;

public class HTTPRequestEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -6097777228498470391L;

	final public static String ENTITY_TYPE = "http.request";

	private IEntityReference connection;
	private String requestLine;
//	private Map<String,String> header;
	
	private IEntityReference response;
	
	public HTTPRequestEntity(IWorkspace workspace, ClientServiceConnectionEntity connection, String requestLine) {
		super(ENTITY_TYPE, workspace, connection.getRealmId());
		
		this.connection = connection.createReference();
		this.requestLine = requestLine;
	}

	HTTPRequestEntity() {}
	
	public ClientServiceConnectionEntity getConnection() {
		return (ClientServiceConnectionEntity) referenceToEntity(connection);
	}

	public String getRequestLine() {
		return requestLine;
	}
	
	public String getMethod() {
		return getRequestLine().split(" ")[0];
	}

	public String getURL() {
		return getRequestLine().split(" ")[1];
	}

	public void setResponse(HTTPResponseEntity response) {
		this.response = response.createReference();
	}
	
	public HTTPResponseEntity getResponse() {
		return (HTTPResponseEntity) referenceToEntity(response);
	}
	
	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		requestLine = ((HTTPRequestEntity)masterEntity).requestLine;
		connection = ((HTTPRequestEntity)masterEntity).connection;
		response = ((HTTPRequestEntity)masterEntity).response;
	}
	
	@Override
	protected IEntity cloneEntity() {
		HTTPRequestEntity clone = new HTTPRequestEntity(getWorkspace(),getConnection(),requestLine);
		clone.response = response;
		return clone;
	}
}
