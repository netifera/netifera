package com.netifera.platform.net.dns.internal.ui;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.layers.ITreeLayerProvider;
import com.netifera.platform.net.dns.model.AAAARecordEntity;
import com.netifera.platform.net.dns.model.ARecordEntity;
import com.netifera.platform.net.dns.model.DNSRecordEntity;
import com.netifera.platform.net.dns.model.DomainEntity;
import com.netifera.platform.net.dns.model.EmailAddressEntity;
import com.netifera.platform.net.dns.model.PTRRecordEntity;

public class DomainsTreeLayerProvider implements ITreeLayerProvider {
	
	public IEntity[] getParents(IEntity entity) {
		if(entity instanceof DomainEntity) {
			if (((DomainEntity)entity).getParent() != null)
				return new IEntity[] {((DomainEntity)entity).getParent()};
		} else if(entity instanceof DNSRecordEntity) {
			if (entity instanceof ARecordEntity || entity instanceof AAAARecordEntity || entity instanceof PTRRecordEntity)
				return new IEntity[0];
			DomainEntity domain = ((DNSRecordEntity)entity).getDomain();
			return new IEntity[] { domain };
		} else if(entity instanceof EmailAddressEntity) {
			return new IEntity[] {((EmailAddressEntity) entity).getDomain()};
		}
		return new IEntity[0];
	}

	public boolean isRealmRoot(IEntity entity) {
		return entity instanceof DomainEntity;
	}

	public String getLayerName() {
		return "Domains";
	}

	public boolean isDefaultEnabled() {
		return true;
	}
}
