package com.netifera.platform.net.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityReference;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class InternetAddressEntity extends NetworkAddressEntity {
	
	private static final long serialVersionUID = -4283823680888685989L;

	public final static String ENTITY_NAME = "address.ip";

	private final IEntityReference host;
	
	private Set<String> names = new HashSet<String>();
	
	private IEntityReference tcpPorts;
	private IEntityReference udpPorts;
	
	
	private InternetAddressEntity(IWorkspace workspace, HostEntity host, byte[] address) {
		super(ENTITY_NAME, workspace, host.getRealmId(), address);
		this.host = host.createReference();
	}

	public InternetAddressEntity(IWorkspace workspace, HostEntity host, String address) {
		this(workspace, host, InternetAddress.fromString(address).toBytes());
	}

	private InternetAddressEntity(IWorkspace workspace, IEntityReference hostReference, long realmId, byte[] address) {
		super(ENTITY_NAME, workspace, realmId, address);
		this.host = hostReference == null ? null : hostReference.createClone();
	}
	
	InternetAddressEntity() {
		host = null;
	}
	
	public InternetAddress getAddress() {
		return InternetAddress.fromBytes(getData());
	}
	
	public PortSetEntity getTcpPorts() {
		return (PortSetEntity) referenceToEntity(tcpPorts);
	}
	
	public PortSetEntity getUdpPorts() {
		return (PortSetEntity) referenceToEntity(udpPorts);
	}
	
	public void setTcpPorts(PortSetEntity ports) {
		tcpPorts = ports.createReference();
	}

	public void setUdpPorts(PortSetEntity ports) {
		udpPorts = ports.createReference();
	}
	
	public HostEntity getHost() {
		return (HostEntity) referenceToEntity(host);
	}

	public void addName(String name) {
		names.add(name);
	}
	
	public Set<String> getNames() {
		return Collections.unmodifiableSet(names);
	}
	
	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		super.synchronizeEntity(masterEntity);
		names = ((InternetAddressEntity)masterEntity).names;
		tcpPorts = ((InternetAddressEntity)masterEntity).tcpPorts;
		udpPorts = ((InternetAddressEntity)masterEntity).udpPorts;
	}
	
	protected IEntity cloneEntity() {
		InternetAddressEntity clone = new InternetAddressEntity(getWorkspace(), host, getRealmId(), getData());
		clone.names = names;
		clone.tcpPorts = tcpPorts;
		clone.udpPorts = udpPorts;
		return clone;
	}
	
	public static String createQueryKey(long realmId, InternetAddress address) {
		return NetworkAddressEntity.createQueryKey(ENTITY_NAME, realmId, address.toBytes());
	}
}
