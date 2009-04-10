package com.netifera.platform.net.model;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.util.HexaEncoding;
import com.netifera.platform.util.addresses.inet.InternetNetblock;

public class NetblockEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -8976034297129679105L;

	public final static String ENTITY_NAME = "netblock.ip";

	/*
	 * byte representation of the network
	 */
	private byte[] data;
	
	/* CIDR
	 * i.e.: 24 means /24
	 */
	private int maskBitCount;

	public NetblockEntity(IWorkspace workspace, long realmId) {
		super(ENTITY_NAME, workspace, realmId);
	}
	
	NetblockEntity() {}
	
	public void setData(byte[] data) {
		assert(data.length == 4 || data.length == 16);
		this.data = data.clone();
	}
	
	public byte[] getData() {
		return data; // FIXME readonly 
	}
	
	public void setMaskBitCount(int count) {
		assert(count > 0 && count <= 128); // FIXME data.length
		maskBitCount = count;
	}
	
	public int getMaskBitCount() {
		return maskBitCount;
	}
	
	public InternetNetblock getNetblock() {
		return InternetNetblock.fromData(data, maskBitCount);
	}
	
	@Override
	protected void synchronizeEntity(AbstractEntity masterEntity) {
		data = ((NetblockEntity)masterEntity).data; // FIXME
		maskBitCount = ((NetblockEntity)masterEntity).maskBitCount;
	}
	
	@Override
	protected IEntity cloneEntity() {
		NetblockEntity clone = new NetblockEntity(getWorkspace(), getRealmId());
		clone.data = data.clone();
		clone.maskBitCount = maskBitCount;
		return clone;
	}
	
	public static String createQueryKey(long realmId, InternetNetblock netblock) {
		return createQueryKey(realmId, netblock.getNetworkAddress().toBytes(), netblock.getCIDR());
	}
	
	private static String createQueryKey(long realmId, byte[] data, int maskBits) {
		return ENTITY_NAME + ":" + realmId + ":" + HexaEncoding.bytes2hex(data) + "/" + maskBits;
	}
	
	@Override
	protected String generateQueryKey() {
		return createQueryKey(getRealmId(), data, maskBitCount);
	}
}
