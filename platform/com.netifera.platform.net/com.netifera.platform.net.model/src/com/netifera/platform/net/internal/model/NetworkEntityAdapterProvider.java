package com.netifera.platform.net.internal.model;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityAdapterProvider;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.net.model.NetblockEntity;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.InternetNetblock;

public class NetworkEntityAdapterProvider implements IEntityAdapterProvider {

	public Object getAdapter(IEntity entity, Class<?> adapterType) {
		if (adapterType == IPv4Address.class) {
			InternetAddress address = getInternetAddress(entity);
			if (address instanceof IPv4Address)
				return address;
		}
		
		if (adapterType == IPv6Address.class) {
			InternetAddress address = getInternetAddress(entity);
			if (address instanceof IPv6Address)
				return address;
		}
		
		if (adapterType.isAssignableFrom(InternetAddress.class))
			return getInternetAddress(entity);
		
		if (adapterType.isAssignableFrom(InternetNetblock.class)) {
			if (entity instanceof NetblockEntity) {
				return ((NetblockEntity) entity).getNetblock();
			}
		}
		return null;
	}
	
	private InternetAddress getInternetAddress(IEntity entity) {
		if (entity instanceof InternetAddressEntity)
			return ((InternetAddressEntity)entity).getAddress();
		if (entity instanceof HostEntity)
			return (InternetAddress) ((HostEntity)entity).getDefaultAddress().getAddress();
		return null;
	}
	
	public IndexedIterable<?> getIterableAdapter(IEntity entity, Class<?> iterableType) {
		if (iterableType.isAssignableFrom(InternetAddress.class) &&
				entity instanceof NetblockEntity) {
			NetblockEntity netblockEntity = (NetblockEntity)entity;
			return InternetNetblock.fromData(netblockEntity.getData(),
					netblockEntity.getMaskBitCount()).getIndexedIterable();
		}
		return null;
	}
}
