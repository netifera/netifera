package com.netifera.platform.net.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.iterables.ListIndexedIterable;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.tools.IToolConfiguration;
import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.host.filesystem.ui.OpenFileSystemViewAction;
import com.netifera.platform.host.terminal.ui.OpenTerminalAction;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.model.NetblockEntity;
import com.netifera.platform.net.model.PortSetEntity;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.net.services.basic.FTP;
import com.netifera.platform.net.services.basic.FTPFileSystem;
import com.netifera.platform.net.services.basic.POP3;
import com.netifera.platform.net.services.basic.Telnet;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.net.services.detection.IServerDetectorService;
import com.netifera.platform.net.services.examples.IMAP;
import com.netifera.platform.net.tools.auth.FTPAuthBruteforcer;
import com.netifera.platform.net.tools.auth.IMAPAuthBruteforcer;
import com.netifera.platform.net.tools.auth.POP3AuthBruteforcer;
import com.netifera.platform.net.tools.portscanning.TCPConnectScanner;
import com.netifera.platform.net.tools.portscanning.UDPScanner;
import com.netifera.platform.tools.options.BooleanOption;
import com.netifera.platform.tools.options.GenericOption;
import com.netifera.platform.tools.options.IntegerOption;
import com.netifera.platform.tools.options.IterableOption;
import com.netifera.platform.tools.options.StringOption;
import com.netifera.platform.ui.actions.SpaceAction;
import com.netifera.platform.ui.actions.ToolAction;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;
import com.netifera.platform.util.PortSet;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv4Netblock;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.InternetNetblock;
import com.netifera.platform.util.locators.TCPSocketLocator;
import com.netifera.platform.util.locators.UDPSocketLocator;

public class EntityActionProvider implements IEntityActionProvider {

	private IServerDetectorService serverDetector;
	private INetworkEntityFactory entityFactory;
	
	@SuppressWarnings("unchecked")
	private IndexedIterable<InternetAddress> getInternetAddressIndexedIterable(IEntity entity) {
		return (IndexedIterable<InternetAddress>) entity.getIterableAdapter(InternetAddress.class);
	}

	private ToolAction createTCPScanner(IndexedIterable<InternetAddress> addresses) {
		assert addresses.itemAt(0).isUniCast();
		ToolAction tcpConnectScanner = new ToolAction("Discover TCP Services", TCPConnectScanner.class.getName());
//		tcpConnectScanner.setSummary("Scan target for listening TCP ports by making many connections in parallel.");
		tcpConnectScanner.addFixedOption(new IterableOption(InternetAddress.class, "target", "Target", "Target addresses", addresses));
		PortSet portset = serverDetector.getTriggerablePorts("tcp");
		assert portset.itemCount() > 0;
		tcpConnectScanner.addOption(new StringOption("ports", "Ports", "Ports to scan", portset.toString()));
		return tcpConnectScanner;
	}

	private ToolAction createUDPScanner(IndexedIterable<InternetAddress> addresses) {
		ToolAction udpScanner = new ToolAction("Discover UDP Services", UDPScanner.class.getName());
//		udpScanner.setSummary("Scan target for UDP services");
		udpScanner.addFixedOption(new IterableOption(InternetAddress.class, "target", "Target", "Target addresses", addresses));
		PortSet portset = serverDetector.getTriggerablePorts("udp");
		assert portset.itemCount() > 0;
		udpScanner.addOption(new StringOption("ports", "Ports", "Ports to scan", portset.toString()));
		udpScanner.addOption(new IntegerOption("delay", "Delay", "Milliseconds to wait between sending packets", 10));
		udpScanner.addOption(new IntegerOption("timeout", "Timeout", "Seconds to wait for any response after sending all requests", 10));
		return udpScanner;
	}
	
	public List<IAction> getActions(IShadowEntity entity) {
		List<IAction> answer = new ArrayList<IAction>();

		IndexedIterable<InternetAddress> addresses = getInternetAddressIndexedIterable(entity);
		if(addresses != null) {
			if (!addresses.itemAt(0).isMultiCast()) {
				answer.add(createTCPScanner(addresses));
			}
			answer.add(createUDPScanner(addresses));
		}
		
		FTP ftp = (FTP) entity.getAdapter(FTP.class);
		if (ftp != null) {
			ToolAction ftpAuthBruteforcer = new ToolAction("Bruteforce authentication", FTPAuthBruteforcer.class.getName());
//			ftpAuthBruteforcer.setSummary("Try credentials on FTP service.");
			ftpAuthBruteforcer.addFixedOption(new GenericOption(TCPSocketLocator.class, "target", "Target", "Target FTP service", ftp.getLocator()));
			ftpAuthBruteforcer.addOption(new IterableOption(UsernameAndPassword.class, "credentials", "Credentials", "List of credentials to try", null));
			answer.add(ftpAuthBruteforcer);
		}
		
		POP3 pop3 = (POP3) entity.getAdapter(POP3.class);
		if (pop3 != null) {
			ToolAction pop3AuthBruteforcer = new ToolAction("Bruteforce authentication", POP3AuthBruteforcer.class.getName());
			pop3AuthBruteforcer.addFixedOption(new GenericOption(TCPSocketLocator.class, "target", "Target", "Target POP3 service", pop3.getLocator()));
			pop3AuthBruteforcer.addOption(new IterableOption(UsernameAndPassword.class, "credentials", "Credentials", "List of credentials to try", null));
			answer.add(pop3AuthBruteforcer);
		}

		IMAP imap = (IMAP) entity.getAdapter(IMAP.class);
		if (imap != null) {
			ToolAction imapAuthBruteforcer = new ToolAction("Bruteforce authentication", IMAPAuthBruteforcer.class.getName());
			imapAuthBruteforcer.addFixedOption(new GenericOption(TCPSocketLocator.class, "target", "Target", "Target IMAP service", imap.getLocator()));
			imapAuthBruteforcer.addOption(new IterableOption(UsernameAndPassword.class, "credentials", "Credentials", "List of credentials to try", null));
			answer.add(imapAuthBruteforcer);
		}

		addNetblockActions(entity, answer);
		
		return answer;
	}

	private void addNetblockActions(final IShadowEntity entity, final List<IAction> list) {
		long realm = entity.getRealmId();
		
		/* TODO the idea if that action is to create netblocks to sort/order
		 * sub-netblocks, no to scan the whole internet. since this feature is
		 * not yet implemented, let's comment it to keep people sane. // james
		 */
		int[] masks4 = new int[] {/*8,*/ 16, 24};
		int[] masks6 = new int[] {/*32, 48, 64, 96,*/ 112, 120};
		
		InternetAddress address = (InternetAddress) entity.getAdapter(InternetAddress.class);
		if (address != null) {
			int[] masks = address instanceof IPv4Address ? masks4 : masks6;
			for (int cidr : masks) {
				addCreateNetworkAction(realm, list, address, cidr);
			}
		}
		
		InternetNetblock netblock = (InternetNetblock) entity.getAdapter(InternetNetblock.class);
		if (netblock != null) {
			int[] masks = netblock instanceof IPv4Netblock ? masks4 : masks6;
			for (int cidr : masks) {
				if (netblock.getCIDR() > cidr) {
					addCreateNetworkAction(realm, list, netblock.getNetworkAddress(), cidr);
				}
			}
		}
	}

	private void addCreateNetworkAction(final long realm, List<IAction> answer, InternetAddress address, int cidr) {
		final InternetNetblock biggerNetwork = address.createNetblock(cidr);
		IAction createNetblock = new SpaceAction("Add "
				+ biggerNetwork.toString() + " to Targets") {
			@Override
			public void run() {
				NetblockEntity entity = entityFactory.createNetblock(realm, 0, biggerNetwork);
				entity.addTag("Target");
				entity.update();
				entity.addToSpace(getSpace().getId());
			}
		};
		answer.add(createNetblock);
	}

	public List<IAction> getQuickActions(IShadowEntity entity) {
		List<IAction> answer = new ArrayList<IAction>();

		IndexedIterable<InternetAddress> addresses = getInternetAddressIndexedIterable(entity);
		if(addresses != null) {
			final ToolAction tcpConnectScanner;
			final ToolAction udpScanner = createUDPScanner(addresses);
			String actionName;
			if (addresses.itemAt(0).isMultiCast()) {
				tcpConnectScanner = null;
				actionName = "Scan Common UDP Services";
			} else {
				tcpConnectScanner = createTCPScanner(addresses);
				actionName = "Scan Common TCP and UDP Services";
			}
			SpaceAction quickScan = new SpaceAction(actionName) {
				@Override
				public void run() {
					if (tcpConnectScanner != null) {
						tcpConnectScanner.run(getSpace());
					}
					udpScanner.run(getSpace());
				}
			};
			quickScan.setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/discover.png"));
			answer.add(quickScan);
		}

		if (entity instanceof PortSetEntity) {
			PortSetEntity portSet = (PortSetEntity)entity;
			addresses = getInternetAddressIndexedIterable(portSet.getAddress());
			if (portSet.getProtocol().equals("tcp") && !addresses.itemAt(0).isMultiCast()) {
				ToolAction tcpConnectScanner = new ToolAction("Discover TCP Services On Ports "+portSet.getPorts(), TCPConnectScanner.class.getName());
				tcpConnectScanner.setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/discover.png"));
				tcpConnectScanner.addFixedOption(new IterableOption(InternetAddress.class, "target", "Target", "Target addresses", addresses));
				tcpConnectScanner.addOption(new StringOption("ports", "Ports", "Ports to scan", portSet.getPorts()));
				answer.add(tcpConnectScanner);
			}
			if (portSet.getProtocol().equals("udp")) {
				ToolAction udpScanner = new ToolAction("Discover UDP Services On Ports "+portSet.getPorts(), UDPScanner.class.getName());
				udpScanner.setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/discover.png"));
				udpScanner.addFixedOption(new IterableOption(InternetAddress.class, "target", "Target", "Target addresses", addresses));
				udpScanner.addOption(new StringOption("ports", "Ports", "Ports to scan", portSet.getPorts()));
				udpScanner.addOption(new IntegerOption("delay", "Delay", "Milliseconds to wait between sending packets", 10));
				udpScanner.addOption(new IntegerOption("timeout", "Timeout", "Seconds to wait for any response after sending all requests", 10));
				answer.add(udpScanner);
			}
		}
		
		if (!(entity instanceof ServiceEntity)) {
			TCPSocketLocator tcpLocator = (TCPSocketLocator) entity.getAdapter(TCPSocketLocator.class);
			if (tcpLocator != null) {
				addresses = new ListIndexedIterable<InternetAddress>(tcpLocator.getAddress());
				assert addresses.itemAt(0).isUniCast();
				ToolAction tcpConnectScanner = new ToolAction("Discover Services At "+tcpLocator, TCPConnectScanner.class.getName());
				tcpConnectScanner.setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/discover.png"));
				tcpConnectScanner.addFixedOption(new IterableOption(InternetAddress.class, "target", "Target", "Target addresses", addresses));
				tcpConnectScanner.addOption(new StringOption("ports", "Ports", "Ports to scan", ((Integer)tcpLocator.getPort()).toString()));
				answer.add(tcpConnectScanner);
			}
			
			UDPSocketLocator udpLocator = (UDPSocketLocator) entity.getAdapter(UDPSocketLocator.class);
			if (udpLocator != null) {
				addresses = new ListIndexedIterable<InternetAddress>(udpLocator.getAddress());
				ToolAction udpScanner = new ToolAction("Discover Services At "+udpLocator, UDPScanner.class.getName());
				udpScanner.setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/discover.png"));
				udpScanner.addFixedOption(new IterableOption(InternetAddress.class, "target", "Target", "Target addresses", addresses));
				udpScanner.addOption(new StringOption("ports", "Ports", "Ports to scan", ((Integer)udpLocator.getPort()).toString()));
				udpScanner.addOption(new IntegerOption("delay", "Delay", "Milliseconds to wait between sending packets", 10));
				udpScanner.addOption(new IntegerOption("timeout", "Timeout", "Seconds to wait for any response after sending all requests", 10));
				answer.add(udpScanner);
			}
		}

		FTP ftp = (FTP) entity.getAdapter(FTP.class);
		if (ftp != null) {
			SpaceAction action = new OpenFileSystemViewAction("Browse File System") {
				@Override
				public IFileSystem createFileSystem() {
					IToolConfiguration config = getConfiguration();
					return new FTPFileSystem((FTP)config.get("ftp"), new UsernameAndPassword((String)config.get("username"),(String)config.get("password")));
				}
			};
			action.addFixedOption(new GenericOption(FTP.class, "ftp", "FTP", "FTP server to connect to", ftp));
			action.addOption(new StringOption("username", "Username", "", "ftp"));
			action.addOption(new StringOption("password", "Password", "", "", true));
			answer.add(action);
		}

		Telnet telnet = (Telnet) entity.getAdapter(Telnet.class);
		if (telnet != null) {
			SpaceAction action = new OpenTerminalAction("Open Telnet Terminal", ((ServiceEntity)entity).getAddress().getHost());
			action.addOption(new StringOption("host", "Host", "Host to connect to", telnet.getLocator().getAddress().toString()));
			action.addOption(new IntegerOption("port", "Port", "Port to connect to", telnet.getLocator().getPort(), 0xFFFF));
			action.addFixedOption(new StringOption("connector", "Connector", "", "org.eclipse.tm.internal.terminal.telnet.TelnetConnector"));
			answer.add(action);
		} else {
			TCPSocketLocator locator = (TCPSocketLocator) entity.getAdapter(TCPSocketLocator.class);
			if (locator != null) {
				SpaceAction action = new OpenTerminalAction("Open Raw Terminal", ((ServiceEntity)entity).getAddress().getHost());
				action.addOption(new StringOption("host", "Host", "Host to connect to", locator.getAddress().toString()));
				action.addOption(new IntegerOption("port", "Port", "Port to connect to", locator.getPort(), 0xFFFF));
				action.addFixedOption(new StringOption("connector", "Connector", "", "org.eclipse.tm.internal.terminal.telnet.TelnetConnector"));
				action.addFixedOption(new BooleanOption("commandInputField", "Set command input field", "Wether to enable the command input field", true));
				answer.add(action);
			}
		}

		return answer;
	}

	protected void setServerDetector(IServerDetectorService serverDetector) {
		this.serverDetector = serverDetector;
	}

	protected void unsetServerDetector(IServerDetectorService serverDetector) {
		this.serverDetector = null;
	}

	protected void setEntityFactory(INetworkEntityFactory entityFactory) {
		this.entityFactory = entityFactory;
	}

	protected void unsetEntityFactory(INetworkEntityFactory entityFactory) {
		this.entityFactory = null;
	}
}
