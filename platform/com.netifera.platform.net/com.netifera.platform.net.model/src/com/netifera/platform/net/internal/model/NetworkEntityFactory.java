package com.netifera.platform.net.internal.model;

import java.util.Map;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.model.IWorkspaceEx;
import com.netifera.platform.net.model.ClientEntity;
import com.netifera.platform.net.model.ClientServiceConnectionEntity;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.net.model.NetblockEntity;
import com.netifera.platform.net.model.PasswordEntity;
import com.netifera.platform.net.model.PortSetEntity;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.net.model.UserEntity;
import com.netifera.platform.net.model.UsernameAndPasswordEntity;
import com.netifera.platform.util.PortSet;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.InternetNetblock;
import com.netifera.platform.util.locators.ISocketLocator;
import com.netifera.platform.util.locators.TCPSocketLocator;
import com.netifera.platform.util.locators.UDPSocketLocator;

public class NetworkEntityFactory implements INetworkEntityFactory {

	private IModelService model;
	
	
	protected void setModelService(IModelService model) {
		this.model = model;
	}

	protected void unsetModelService(IModelService model) {
		this.model = null;
	}
	
	private IWorkspaceEx getWorkspace() {
		if(model.getCurrentWorkspace() == null) {
			throw new IllegalStateException("Cannot create entities because no workspace is currently open");
		}
		return (IWorkspaceEx) model.getCurrentWorkspace();
	}

	public synchronized InternetAddressEntity createAddress(long realm, long spaceId, InternetAddress address) {
		InternetAddressEntity addr = (InternetAddressEntity) getWorkspace().findByKey(InternetAddressEntity.createQueryKey(realm, address));
		if(addr != null) {
			addr.getHost().addToSpace(spaceId);
			addr.addToSpace(spaceId);
			return addr;
		}
		
		HostEntity hostEntity = new HostEntity(getWorkspace(), realm);
		
		// First the HostEntity must be saved so that InternetAddressEntity can store a reference to it
		hostEntity.save();
		
		InternetAddressEntity addressEntity = new InternetAddressEntity(getWorkspace(), hostEntity, address.toString());
		// Now save the address so that we can create a reference to it in the HostEntity
		addressEntity.save();
		addressEntity.addToSpace(spaceId);
		
		// It's now safe to assign the InternetAddressEntity 
		hostEntity.addAddress(addressEntity);
		hostEntity.save();
		hostEntity.addToSpace(spaceId);
		
		return addressEntity;
	}
	
	public synchronized NetblockEntity createNetblock(long realm, long spaceId, InternetNetblock netblock) {
		// address needs to be the netblock's one (the first addr in netblock).
		InternetAddress address = netblock.getNetworkAddress();
		byte[] addressData = address.toBytes();
		int maskBitCount = netblock.getCIDR();

		NetblockEntity nb = (NetblockEntity) getWorkspace().findByKey(NetblockEntity.createQueryKey(realm, netblock));
		if(nb != null) {
			nb.addToSpace(spaceId);
			return nb;
		}
		
		NetblockEntity netblockEntity = new NetblockEntity(getWorkspace(), realm);
		netblockEntity.setData(addressData);
		netblockEntity.setMaskBitCount(maskBitCount);
		netblockEntity.save();
		netblockEntity.addToSpace(spaceId);
		
		// add IP if v4/32 or v6/128
		if (address.getDataSize() == maskBitCount) {
			createAddress(realm, spaceId, address);
		}
		return netblockEntity;
	}
	
	public synchronized void addOpenTCPPorts(long realm, long space, InternetAddress address,
			PortSet ports) {
		InternetAddressEntity addressEntity = createAddress(realm, space, address);
		addOpenPorts(realm, space, addressEntity, ports, true);
	}

	public synchronized void addOpenUDPPorts(long realm, long space, InternetAddress address,
			PortSet ports) {
		InternetAddressEntity addressEntity = createAddress(realm, space, address);
		addOpenPorts(realm, space, addressEntity, ports, false);
	}
	
	private void addOpenPorts(long realm, long spaceId, InternetAddressEntity addressEntity, PortSet ports, 
			boolean isTCP) {
		PortSetEntity portsEntity =
			isTCP ? addressEntity.getTcpPorts() : addressEntity.getUdpPorts();
		
		if (portsEntity == null) {
			portsEntity = new PortSetEntity(getWorkspace(), addressEntity, 
					isTCP ? "tcp" : "udp");
			portsEntity.setPorts(ports.toString());
			portsEntity.save();
			if(isTCP) {
				addressEntity.setTcpPorts(portsEntity);
			} else {
				addressEntity.setUdpPorts(portsEntity);
			}
			portsEntity.save();
			portsEntity.addToSpace(spaceId);
			addressEntity.save();
			addressEntity.getHost().addToSpace(spaceId);
		} else {
			PortSet ports2 = new PortSet(portsEntity.getPorts());
			for (int port: ports)
				ports2.addPort(port);

			portsEntity.addToSpace(spaceId);

			if(portsEntity.getPorts().equals(ports2.toString())) {
				// No change.
				return;
			}
			portsEntity.setPorts(ports2.toString());
			portsEntity.update();
		}
	}

	private boolean updateAttribute(String name, Map<String, String> info, AbstractEntity e) {
		if(!info.containsKey(name))
			return false;
		String value = info.get(name);
		if (value == null || value.equals(e.getNamedAttribute(name)))
			return false;
		e.setNamedAttribute(name, value);
		return true;
	}

	private boolean updateSystem(AbstractEntity entity, Map<String,String> info) {
		boolean changed = false;

		changed |= updateAttribute("os", info, entity);
		changed |= updateAttribute("distribution", info, entity);
		changed |= updateAttribute("arch", info, entity);

		return changed;
	}

	public synchronized void setOperatingSystem(long realm, long spaceId, InternetAddress address, String os) {
		InternetAddressEntity addressEntity = createAddress(realm, spaceId, address);
		HostEntity hostEntity = addressEntity.getHost();
		if(os != null && !os.equals(hostEntity.getNamedAttribute("os"))) {
			hostEntity.setNamedAttribute("os", os);
			hostEntity.update();
		}
	}

	private ServiceEntity findService(long realm, ISocketLocator locator) {
		return (ServiceEntity) getWorkspace().findByKey(ServiceEntity.createQueryKey(realm, locator.getAddress(), locator.getPort(), locator.getProtocol()));
	}
		
	private ServiceEntity createNewService(InternetAddressEntity address, ISocketLocator locator, String serviceType,
			Map<String,String> info, long space) {

		ServiceEntity answer = new ServiceEntity(getWorkspace(), address, locator.getPort(), locator.getProtocol(), serviceType);

		if (info != null) {
			updateAttribute(ServiceEntity.BANNER_KEY, info, answer);
			updateAttribute(ServiceEntity.PRODUCT_KEY, info, answer);
			updateAttribute(ServiceEntity.VERSION_KEY, info, answer);
			updateSystem(answer, info);
		}
		
		answer.save();
		answer.addToSpace(space);
		
		return answer;
	}
	
	public synchronized ServiceEntity createService(long realm, long spaceId,
			ISocketLocator locator, String serviceType, Map<String, String> info) {
		
		if (serviceType == null)
			throw new IllegalArgumentException("serviceType cannot be null");

		InternetAddressEntity address = createAddress(realm, spaceId, locator.getAddress());
		
		if(info != null) {
			if (updateSystem(address.getHost(), info))
				address.getHost().update();
		}
		
		ServiceEntity answer = findService(realm, locator);

		if (answer == null) {
			PortSet ports = new PortSet();
			ports.addPort(locator.getPort());
			if (locator instanceof UDPSocketLocator) {
				addOpenPorts(realm, spaceId, address, ports, false);
			} else if (locator instanceof TCPSocketLocator) {
				addOpenPorts(realm, spaceId, address, ports, true);			
			}
			answer = createNewService(address, locator, serviceType, info, spaceId);
		} else {
			if (info != null) {
				boolean changed = false;
				changed |= updateAttribute(ServiceEntity.BANNER_KEY, info, answer);
				changed |= updateAttribute(ServiceEntity.PRODUCT_KEY, info, answer);
				changed |= updateAttribute(ServiceEntity.VERSION_KEY, info, answer);
				changed |= updateSystem(answer, info);
				if(changed)
					answer.update();
			}
			answer.addToSpace(spaceId);
		}
		
		if (info != null) {
			if (info.containsKey("password")) {
				if (info.containsKey("username"))
					createUsernameAndPassword(realm, spaceId, locator, info.get("username"), info.get("password"));
				else
					createPassword(realm, spaceId, locator, info.get("password"));
			}
			if (info.containsKey("hostname")) {
				address.addName(info.get("hostname"));
				address.update();
//				if (address.getHost().getLabel()==null)
//					address.getHost().setLabel(info.get("hostname"));
				address.getHost().update();
			}
		}
		
		return answer;
	}

	private ClientEntity findClient(HostEntity host, String serviceType, String product) {
		long realm = host.getRealmId();
		String key = ClientEntity.createQueryKey(realm, host.getId(), serviceType, product);
		ClientEntity clientEntity = (ClientEntity) getWorkspace().findByKey(key);
		if (clientEntity != null)
			return clientEntity;
		key = ClientEntity.createQueryKey(realm, host.getId(), serviceType, null);
		clientEntity = (ClientEntity) getWorkspace().findByKey(key);
		if (clientEntity != null) {
			clientEntity.setProduct(product);
			clientEntity.update();
		}
		return clientEntity;
	}

	private ClientEntity createNewClient(long spaceId, HostEntity host, String serviceType, Map<String,String> info) {
		ClientEntity answer = new ClientEntity(getWorkspace(), host, serviceType);
		
		if (info != null) {
			updateAttribute(ServiceEntity.BANNER_KEY, info, answer);
			updateAttribute(ServiceEntity.PRODUCT_KEY, info, answer);
			updateAttribute(ServiceEntity.VERSION_KEY, info, answer);
			updateSystem(answer, info);
		}
		
		answer.save();
		answer.addToSpace(spaceId);
		
		return answer;
	}

	public synchronized ClientEntity createClient(long realm, long spaceId,
			InternetAddress address, String serviceType, Map<String, String> info, ISocketLocator service) {

		if (serviceType == null)
			throw new IllegalArgumentException("serviceType cannot be null");
		
		InternetAddressEntity addressEntity = createAddress(realm, spaceId, address);
		HostEntity hostEntity = addressEntity.getHost();

		if(info != null) {
			if (updateSystem(hostEntity, info))
				hostEntity.update();
		}

		ClientEntity answer = findClient(hostEntity, serviceType, info != null ? info.get("product") : null);
		
		if (answer == null)
			return createNewClient(spaceId, addressEntity.getHost(), serviceType, info);
		else
			answer.addToSpace(spaceId);
		
		String identity = null;
		
		if (info != null) {
			boolean isChanged = false;
			isChanged |= updateAttribute(ServiceEntity.BANNER_KEY, info, answer);
			isChanged |= updateAttribute(ServiceEntity.PRODUCT_KEY, info, answer);
			isChanged |= updateAttribute(ServiceEntity.VERSION_KEY, info, answer);
			isChanged |= updateSystem(answer, info);
			if(isChanged)
				answer.update();
			
			if (info.containsKey("identity"))
				identity = info.get("identity");
			else
				identity = info.get("username"); // or null
			
			if (info.containsKey("password")) {
				if (info.containsKey("username"))
					createUsernameAndPassword(realm, spaceId, service, info.get("username"), info.get("password"));
				else
					createPassword(realm, spaceId, service, info.get("password"));
			}
		}

		if (service != null) {
			ServiceEntity serviceEntity = findService(realm, service);
			if (serviceEntity == null)
				System.err.println("ERROR: connection to unknown service: "+address+" -> "+service);
			else
				createConnection(spaceId, answer, serviceEntity, identity);
		}
		
		return answer;
	}

	public ClientServiceConnectionEntity createConnection(long spaceId, ClientEntity client, ServiceEntity service, String identity) {
		long realm = client.getRealmId();
		long clientId = client.getId();
		long serviceId = service.getId();
		
		ClientServiceConnectionEntity answer = (ClientServiceConnectionEntity) getWorkspace().findByKey(ClientServiceConnectionEntity.createQueryKey(realm, clientId, serviceId, identity));
		if(answer != null) {
			answer.addToSpace(spaceId);
			return answer;
		}
	
		answer = new ClientServiceConnectionEntity(getWorkspace(), client, service, identity);
		answer.save();
		answer.addToSpace(spaceId);
		return answer;
	}
	
	public synchronized UserEntity createUser(long realm, long spaceId, InternetAddress address, String username) {
		HostEntity hostEntity = createAddress(realm, spaceId, address).getHost();
		
		UserEntity user = (UserEntity) getWorkspace().findByKey(UserEntity.createQueryKey(realm, username, hostEntity.getId()));
		if(user != null) {
			user.addToSpace(spaceId);
			return user;
		}
		
		UserEntity answer = new UserEntity(getWorkspace(), hostEntity, username);
		answer.save();
		answer.addToSpace(spaceId);
		return answer;
	}
	
	public synchronized PasswordEntity createPassword(long realm, long spaceId, ISocketLocator service, String password) {
		IEntity serviceEntity = findService(realm, service);
		
		PasswordEntity pw = (PasswordEntity) getWorkspace().findByKey(PasswordEntity.createQueryKey(realm, serviceEntity.getId(), password));
		if(pw != null) {
			pw.addToSpace(spaceId);
			return pw;
		}
	
		PasswordEntity answer = new PasswordEntity(getWorkspace(), serviceEntity, password);
		answer.save();
		answer.addToSpace(spaceId);
		return answer;
	}
	
	public synchronized UsernameAndPasswordEntity createUsernameAndPassword(long realm, long spaceId, ISocketLocator service, String username, String password) {
		IEntity serviceEntity = findService(realm, service);
		
		UsernameAndPasswordEntity unp = (UsernameAndPasswordEntity) getWorkspace().findByKey(UsernameAndPasswordEntity.createQueryKey(realm, serviceEntity.getId(), username, password));
		if(unp != null) {
			unp.addToSpace(spaceId);
			return unp;
		}
		
		UsernameAndPasswordEntity answer = new UsernameAndPasswordEntity(getWorkspace(), serviceEntity, username, password);
		answer.save();
		answer.addToSpace(spaceId);
		return answer;
	}
}
