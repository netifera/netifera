package com.netifera.platform.net.internal.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.layers.IGroupLayerProvider;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.NetworkAddressEntity;
import com.netifera.platform.util.addresses.INetworkAddress;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.InternetNetblock;

public class NetblockLayerProvider implements IGroupLayerProvider {

	private static final Map<InternetNetblock, Integer> netblock4Map;
	static {
		netblock4Map = new HashMap<InternetNetblock, Integer>();
		netblock4Map.put(InternetNetblock.fromString("192.168.0.0/16"), 24);
		netblock4Map.put(InternetNetblock.fromString("172.16.0.0/12"), 16);
		netblock4Map.put(InternetNetblock.fromString("10.0.0.0/8"), 8);
	}

	public Set<String> getGroups(IEntity entity) {
		if (entity instanceof HostEntity) {
			Set<String> answer = new HashSet<String>();
			for (NetworkAddressEntity addressEntity: ((HostEntity)entity).getAddresses()) {
				INetworkAddress address = addressEntity.getAddress();
				if (address instanceof InternetAddress) {
					String aggregate = getNetblock((InternetAddress)address, 32 + 128);
					if (aggregate != null)
						answer.add(aggregate);
				}
			}
			return answer;
		}
		
		return Collections.emptySet();
	}
	
	public String getLayerName() {
		return "Hosts By Netblock";
	}

	public boolean isDefaultEnabled() {
		return true;
	}
	
	// Some heuristics for IP networks, this is not perfect
	private String getNetblock(InternetAddress address, int maskBit) {
		if (address.isMultiCast()) {
			return "Multicast";
		} else if (address.isLinkLocal()) {
			if (address instanceof IPv4Address) {
				if (maskBit >= 16) return "169.254.0.0/16";
			} else {
				if (maskBit >= 10) return "fe80::/10";
			}
		} else if (address.isPrivate()) {
			if (address instanceof IPv4Address) {
				for (InternetNetblock netblock: netblock4Map.keySet()) {
					int netblockMaskBit = netblock4Map.get(netblock);
					if (maskBit >= netblockMaskBit && netblock.contains(address)) {
						return address.createNetblock(netblockMaskBit).toString();
					}
				}
			}
		} else if (address.isLoopback()) {
			if (address instanceof IPv4Address) {
				return "127.0.0.0/8";
			}
		} else if (address.isUnspecified()) {
			// nothing
		} else { // not specific IP address
			if (address instanceof IPv4Address) {
				if (maskBit >= 24) {
					return address.createNetblock(24).toString();
				}
			} else {
				IPv6Address address6 = (IPv6Address)address;
				if (maskBit >= 48) {
					if (address6.isSiteLocal()) {
						return "fec0::/48";
					} else if (address6.isV4Mapped() || address6.isV4Compatible()) {
						return getNetblock(address6.toIPv4Address(), maskBit);
					} else {
						/* see RFC 3177: Recommendations on IPv6 Address Allocations
						 * and http://www.sixxs.net/tools/grh/dfp/
						 */
						return address.createNetblock(48).toString();
						// FIXME
						//return address.createNetblock(64).toString();
					}
				}
			}
		}
		return null;
	}
}
