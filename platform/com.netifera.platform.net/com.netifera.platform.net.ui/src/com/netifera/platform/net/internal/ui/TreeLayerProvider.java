package com.netifera.platform.net.internal.ui;


import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.layers.ITreeLayerProvider;
import com.netifera.platform.model.ProbeEntity;
import com.netifera.platform.net.model.ClientEntity;
import com.netifera.platform.net.model.ClientServiceConnectionEntity;
import com.netifera.platform.net.model.CredentialEntity;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.net.model.NetblockEntity;
import com.netifera.platform.net.model.NetworkAddressEntity;
import com.netifera.platform.net.model.PortSetEntity;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.net.model.UserEntity;

public class TreeLayerProvider implements ITreeLayerProvider {
	
	public IEntity[] getParents(IEntity entity) {
		if(entity instanceof UserEntity) {
			return new IEntity[] {((UserEntity)entity).getHost()};
		} else if(entity instanceof PortSetEntity) {
			return new IEntity[] {((PortSetEntity)entity).getAddress().getHost()};
		} else if(entity instanceof ServiceEntity) {
			return new IEntity[] {((ServiceEntity)entity).getAddress().getHost()};
		} else if(entity instanceof ClientEntity) {
			NetworkAddressEntity address = ((ClientEntity)entity).getHost().getDefaultAddress();
			if (address instanceof InternetAddressEntity && ((InternetAddressEntity)address).getAddress().isMultiCast()) {
				/* no multicast client (nonsens) */
				return new IEntity[0];
			}
			return new IEntity[] {((ClientEntity)entity).getHost()};
		} else if(entity instanceof ClientServiceConnectionEntity) {
			return new IEntity[] {((ClientServiceConnectionEntity)entity).getClient()};
		} else if(entity instanceof CredentialEntity) {
			return new IEntity[] {((CredentialEntity)entity).getAuthenticable()};
		} else if(entity instanceof ProbeEntity) {
			ProbeEntity probe = (ProbeEntity) entity;
			if(probe.getHostEntity() != null) {
				return new IEntity[] { probe.getHostEntity() };
			}
		}
		return new IEntity[0];
	}

	public boolean isRealmRoot(IEntity entity) {
		return entity instanceof NetblockEntity || entity instanceof HostEntity;
	}

	public String getLayerName() {
		return "Services and Clients";
	}

	public boolean isDefaultEnabled() {
		return true;
	}
}
