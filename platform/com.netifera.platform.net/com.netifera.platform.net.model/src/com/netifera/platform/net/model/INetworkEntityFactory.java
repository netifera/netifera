package com.netifera.platform.net.model;

import java.util.Map;

import com.netifera.platform.util.PortSet;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.InternetNetblock;
import com.netifera.platform.util.locators.ISocketLocator;

public interface INetworkEntityFactory {

	InternetAddressEntity createAddress(long realm, long space, InternetAddress address);
	NetblockEntity createNetblock(long realm, long space, InternetNetblock netblock);

	void addOpenTCPPorts(long realm, long space, InternetAddress address, PortSet ports);
	void addOpenUDPPorts(long realm, long space, InternetAddress address, PortSet ports);

	ServiceEntity createService(long realm, long space, ISocketLocator locator, String serviceType, Map<String,String> info);
	ClientEntity createClient(long realm, long space, InternetAddress address, String serviceType, Map<String,String> info, ISocketLocator service);
	ClientServiceConnectionEntity createConnection(long space, ClientEntity client, ServiceEntity service, String identity);

	void setOperatingSystem(long realm, long space, InternetAddress address, String os);

	UserEntity createUser(long realm, long space, InternetAddress address, String username);
	
	PasswordEntity createPassword(long realm, long space, ISocketLocator service, String password);
	UsernameAndPasswordEntity createUsernameAndPassword(long realm, long space, ISocketLocator service, String username, String password);
}
