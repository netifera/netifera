package com.netifera.platform.net.dns.internal.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.layers.IGroupLayerProvider;
import com.netifera.platform.api.model.layers.ITreeLayerProvider;
import com.netifera.platform.net.dns.model.DomainEntity;
import com.netifera.platform.net.dns.model.EmailAddressEntity;

public class EmailsTreeLayerProvider implements ITreeLayerProvider, IGroupLayerProvider {

	public String getLayerName() {
		return "Emails";
	}

	public boolean isDefaultEnabled() {
		return true;
	}

	public IEntity[] getParents(IEntity entity) {
		if(entity instanceof EmailAddressEntity) {
			return new IEntity[] {((EmailAddressEntity) entity).getDomain()};
		}
		return new IEntity[0];
	}

	public boolean isRealmRoot(IEntity entity) {
		return entity instanceof DomainEntity;
	}

	public Set<String> getGroups(IEntity entity) {
		if(entity instanceof EmailAddressEntity) {
			Set<String> answer = new HashSet<String>();
			answer.add(((EmailAddressEntity) entity).getDomain().getFQDM());
			return answer;
		}
		return Collections.emptySet();
	}
}
