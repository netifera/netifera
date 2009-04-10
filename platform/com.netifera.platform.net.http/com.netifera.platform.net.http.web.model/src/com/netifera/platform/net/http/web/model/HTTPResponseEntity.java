package com.netifera.platform.net.http.web.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.net.model.ClientServiceConnectionEntity;

public class HTTPResponseEntity extends AbstractEntity {
	
	private static final long serialVersionUID = 8560224246576871994L;

	final public static String ENTITY_TYPE = "http.response";

	private final IEntityReference connection;
	private IEntityReference request;
	private final String statusLine;
//	private Map<String,String> header;
//	private String contents;
	
	public HTTPResponseEntity(IWorkspace workspace, ClientServiceConnectionEntity connection, String statusLine) {
		super(ENTITY_TYPE, workspace, connection.getRealmId());
		
		this.connection = connection.createReference();
		this.statusLine = statusLine;
	}

	HTTPResponseEntity() {
		connection = null;
		statusLine = null;
	}
	
	public ClientServiceConnectionEntity getConnection() {
		return (ClientServiceConnectionEntity) referenceToEntity(connection);
	}
	
	public HTTPRequestEntity getRequest() {
		return (HTTPRequestEntity) referenceToEntity(request);
	}
	
	public String getStatusLine() {
		return statusLine;
	}
	
	public int getStatusCode() {
		return Integer.parseInt(statusLine.split(" ")[1]);
	}
	
	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		request = ((HTTPResponseEntity)masterEntity).request;
	}

	@Override
	protected IEntity cloneEntity() {
		HTTPResponseEntity clone = new HTTPResponseEntity(getWorkspace(),getConnection(),statusLine);
		clone.request = request;
		return clone;
	}
}
