package com.netifera.platform.net.internal.routes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.layers.IGroupLayerProvider;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.net.model.NetworkAddressEntity;
import com.netifera.platform.net.routes.AS;
import com.netifera.platform.net.routes.IIP2ASService;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class ASLayerProvider implements IGroupLayerProvider {

	private IIP2ASService ip2asService;
	
	public Set<String> getGroups(IEntity entity) {
		if (ip2asService == null)
			return Collections.emptySet();
		
/*		InternetAddress address = (InternetAddress) entity.getAdapter(InternetAddress.class);
		if (address != null) {
			String as = ip2asService.getAS(address);
			if (as != null) {
				Set<String> answer = new HashSet<String>();
				answer.add(as);
				return answer;
			}
		}
*/
		if (entity instanceof HostEntity) {
			Set<String> answer = new HashSet<String>();
			for (NetworkAddressEntity addressEntity: ((HostEntity)entity).getAddresses()) {
				if (addressEntity instanceof InternetAddressEntity) {
					InternetAddress address = ((InternetAddressEntity)addressEntity).getAddress();
					if (address != null) {
						AS as = ip2asService.getAS(address);
						if (as != null) {
							answer.add(as.getDescription());
						}
					}
				}
			}
			return answer;
		}

		return Collections.emptySet(); // or return "Unknown AS"
	}
	
	public String getLayerName() {
		return "Hosts By AS";
	}

	public boolean isDefaultEnabled() {
		return false;
	}
	
	protected void setIP2ASService(IIP2ASService service) {
		this.ip2asService = service;
	}
	
	protected void unsetIP2ASService(IIP2ASService service) {
		this.ip2asService = null;
	}
}
