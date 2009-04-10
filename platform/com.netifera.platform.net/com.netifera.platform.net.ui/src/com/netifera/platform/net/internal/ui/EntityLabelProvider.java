package com.netifera.platform.net.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.IStructureContext;
import com.netifera.platform.model.FolderEntity;
import com.netifera.platform.model.TreeStructureContext;
import com.netifera.platform.net.model.ClientEntity;
import com.netifera.platform.net.model.ClientServiceConnectionEntity;
import com.netifera.platform.net.model.CredentialEntity;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.net.model.NetblockEntity;
import com.netifera.platform.net.model.NetworkAddressEntity;
import com.netifera.platform.net.model.PasswordEntity;
import com.netifera.platform.net.model.PortSetEntity;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.net.model.UserEntity;
import com.netifera.platform.net.model.UsernameAndPasswordEntity;
import com.netifera.platform.ui.api.model.IEntityLabelProvider;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.InternetNetblock;
import com.netifera.platform.util.patternmatching.InternetAddressMatcher;
import com.netifera.platform.util.patternmatching.NetblockMatcher;

public class EntityLabelProvider implements IEntityLabelProvider {
	private final static String HOST_WORKSTATION = "icons/host_workstation.png";
	private final static String HOST_SERVER = "icons/host_server.png";
//	private final static String HOST_MAINFRAME = "icons/host_mainframe.png";
	private final static String HOST_ROUTER = "icons/host_router.png";
//	private final static String HOST_SWITCH = "icons/host_switch.png";
	private final static String HOST_AP = "icons/host_ap.png";
	private final static String HOST_IPHONE = "icons/host_iphone.png";
	
	private final static String ADDRESS = "icons/address.png";
	private final static String NETWORK = "icons/network.png";
	private final static String LOCAL_NETWORK = "icons/network_local.png";
	private final static String PORTS = "icons/ports.png";
	private final static String CREDENTIAL = "icons/credential.png";
	private final static String USER = "icons/user.png";
	
	private final static String FOLDER = "icons/folder.png";
	private final static String TARGET = "icons/target.png";
	private final static String VULNERABLE = "icons/vulnerable.png";
	private final static String CONTROLLED = "icons/controlled.png";
	
	private final static String TARGET_OVERLAY = "icons/target_overlay.png";
	private final static String VULNERABLE_OVERLAY = "icons/vulnerable_overlay.png";
	private final static String CONTROLLED_OVERLAY = "icons/controlled_overlay.png";
//	private final static String ERROR_OVERLAY = "icons/error_overlay.png";
	
	//private final static String HOST_SERVER_IBM = "icons/host_server_ibm.png";
	//private final static String HOST_SERVER_HP = "icons/host_server_hp.png";
	//private final static String HOST_SERVER_AS400 = "icons/host_server_as400.png";
	
	private final static String CISCO_OVERLAY = "icons/cisco_overlay.png";
	private final static String JUNIPER_OVERLAY = "icons/juniper_overlay.png";
	
	private final static String HOST_PRINTER = "icons/host_printer.png";
	
	private final static String SERVICE = "icons/service.png";
	private final static String CLIENT = "icons/client.png";
	private final static String SERVICE_SHELL = "icons/service_shell.png";
	private final static String SERVICE_MAIL = "icons/service_mail.png";
	private final static String SERVICE_FILES = "icons/service_file.png";
	private final static String SERVICE_HTTP = "icons/service_http.png";
	private final static String SERVICE_DATABASE = "icons/service_database.png";
	
	private final static String OS_BSD = "icons/os_bsd_overlay.png";
	//private final static String OS_OPENBSD = "icons/os_openbsd_overlay.png";
	private final static String OS_LINUX = "icons/os_linux_overlay.png";
	private final static String OS_MAC = "icons/os_apple_overlay.png";
	private final static String OS_SUN = "icons/os_sun_overlay.png";
	private final static String OS_WINDOWS = "icons/os_windows_overlay.png";
	//private final static String OS_NOVELL = "icons/os_novell_overlay.png";
	private final static String OS_RTOS = "icons/os_rtos_overlay.png";
	private final static String OS_HPUX = "icons/os_hpux_overlay.png";

	
	public String getText(IShadowEntity e) {
		if(e instanceof NetworkAddressEntity) {
			return getAddressText((NetworkAddressEntity)e);
		} else if(e instanceof NetblockEntity) {
			return getNetblockText((NetblockEntity)e);
		} else if(e instanceof HostEntity) {
			return getHostText((HostEntity) e);
		} else if(e instanceof UserEntity) {
			return ((UserEntity)e).getName();
//		} else if(e instanceof LocalNetworkEntity) {
//			return ((LocalNetworkEntity)e).getName();
		} else if(e instanceof FolderEntity) {
			return ((FolderEntity) e).getLabel()+" ("+((TreeStructureContext)((FolderEntity) e).getStructureContext()).getChildren().size()+")";
		} else if(e instanceof PortSetEntity) {
			return getPortsetText((PortSetEntity)e);
		} else if(e instanceof ServiceEntity) {
			return getServiceText((ServiceEntity)e);
		} else if(e instanceof ClientEntity) {
			return getClientText((ClientEntity)e);
		} else if(e instanceof ClientServiceConnectionEntity) {
			return getServiceConnectionText((ClientServiceConnectionEntity)e);
		} else if(e instanceof PasswordEntity) {
			return ((PasswordEntity)e).getPassword();
		} else if(e instanceof UsernameAndPasswordEntity) {
			UsernameAndPasswordEntity credential = (UsernameAndPasswordEntity)e;
			String pw = credential.getPassword();
			if (pw == null || pw.length() == 0) {
				pw = "<no password>";
			}
			return credential.getUsername() + ":" + pw;
		}
		return null;
	}
	
	public String getFullText(IShadowEntity e) {
		if(e instanceof ServiceEntity) {
			ServiceEntity serviceEntity = (ServiceEntity)e;
			return getAddressText(serviceEntity.getAddress())+":"+getText(serviceEntity);
		} else if(e instanceof ClientEntity) {
			ClientEntity clientEntity = (ClientEntity)e;
			return getText(clientEntity.getHost())+" "+getText(clientEntity);
		}
		return getText(e);
	}

	public Image getImage(IShadowEntity e) {
		if(e instanceof NetworkAddressEntity) {
			return Activator.getInstance().getImageCache().get(ADDRESS);
		} else if(e instanceof HostEntity) {
			return getHostImage((HostEntity)e);
		} else if(e instanceof UserEntity) {
			return Activator.getInstance().getImageCache().get(USER);
		} else if(e instanceof NetblockEntity) {
			InternetAddress net = ((NetblockEntity)e).getNetblock().getNetworkAddress();
			if (net.isLinkLocal() || net.isPrivate() || net.isLoopback()) { // TODO use InternetNetblock
				return Activator.getInstance().getImageCache().get(LOCAL_NETWORK);
			}
			return Activator.getInstance().getImageCache().get(NETWORK);
/*		} else if(e instanceof LocalNetworkEntity) {
			InternetAddress net = InternetAddress.fromBytes(((LocalNetworkEntity)e).getAddressData());
			if (net.isLinkLocal() || net.isPrivate() || net.isLoopback()) {
				return localNetworkImage;
			}
			return networkImage;
*/		} else if(e instanceof FolderEntity) {
			return getFolderImage((FolderEntity)e);
		} else if(e instanceof PortSetEntity) {
			return Activator.getInstance().getImageCache().get(PORTS);
		} else if(e instanceof ServiceEntity) {
			return getServiceImage((ServiceEntity)e);
		} else if(e instanceof ClientServiceConnectionEntity) {
			return getImage(((ClientServiceConnectionEntity)e).getService());
		} else if(e instanceof ClientEntity) {
			return getClientImage((ClientEntity)e);
		} else if(e instanceof CredentialEntity) {
			return Activator.getInstance().getImageCache().get(CREDENTIAL);
		}
		return null;
	}

	public Image decorateImage(Image image, IShadowEntity e) {
		ImageDescriptor[] overlays = new ImageDescriptor[5];
		boolean decorated = false;
		if (e.getTags().contains("Target")) {
			decorated = true;
			overlays[IDecoration.TOP_LEFT] = Activator.getInstance().getImageCache().getDescriptor(TARGET_OVERLAY);
		}
		if (e.getTags().contains("Vulnerable")) {
			decorated = true;
			overlays[IDecoration.TOP_RIGHT] = Activator.getInstance().getImageCache().getDescriptor(VULNERABLE_OVERLAY);
		}
		if (!decorated)
			return null;
		
		return Activator.getInstance().getImageCache().get(new DecorationOverlayIcon(image, overlays));
	}
	
	private Image getFolderImage(FolderEntity e) {
		if (e.getTag().equals("Target"))
			return Activator.getInstance().getImageCache().get(TARGET);
		if (e.getTag().equals("Vulnerable"))
			return Activator.getInstance().getImageCache().get(VULNERABLE);
		if (e.getTag().equals("Controlled"))
			return Activator.getInstance().getImageCache().get(CONTROLLED);
		
		//TODO optimise, maybe this is too slow:
		String overlayKeys[] = new String[5];
		IStructureContext context = e.getStructureContext();
		if (context instanceof TreeStructureContext) {
			for (IShadowEntity child: ((TreeStructureContext) context).getChildren()) {
				if (child.getTags().contains("Target"))
					overlayKeys[IDecoration.TOP_LEFT] = TARGET_OVERLAY;
				if (child.getTags().contains("Controlled"))
					overlayKeys[IDecoration.TOP_RIGHT] = CONTROLLED_OVERLAY;
				else if (overlayKeys[IDecoration.TOP_RIGHT] == null && child.getTags().contains("Vulnerable"))
					overlayKeys[IDecoration.TOP_RIGHT] = VULNERABLE_OVERLAY;
			}
		}
		return Activator.getInstance().getImageCache().getDecorated(FOLDER, overlayKeys);
	}
	
	private Image getHostImage(HostEntity e) {
		String os = e.getNamedAttribute("os");
		if (os == null) os = "";
		os = os.toLowerCase(Locale.ENGLISH);
		String base = HOST_WORKSTATION;
		if (os.matches(".*cisco.*"))
			base = HOST_ROUTER;
		else if (os.matches(".*junos.*"))
			base = HOST_ROUTER;
//		else if (os.matches(".*(as\\/?400|z\\/(os|vm)|os\\/?390).*"))
//			base = HOST_MAINFRAME;
		else if (os.matches(".*printer.*"))
			base = HOST_PRINTER;
		else if (os.matches(".*i(phone|touch).*"))
			base = HOST_IPHONE;
//		else if(e.hasChildren(ServiceEntity.class))
//			base = HOST_SERVER;
		
		String overlayKeys[] = new String[5];
		overlayKeys[IDecoration.BOTTOM_LEFT] = getOSDecoration(e);
//		if (e.getTags().contains("Target"))
//			overlayKeys[IDecoration.TOP_LEFT] = TARGET_OVERLAY;
		if (e.getTags().contains("Controlled"))
			overlayKeys[IDecoration.TOP_RIGHT] = CONTROLLED_OVERLAY;
//		else if (e.getTags().contains("Vulnerable"))
//			overlayKeys[IDecoration.TOP_RIGHT] = VULNERABLE_OVERLAY;

		return Activator.getInstance().getImageCache().getDecorated(base, overlayKeys);
	}

	private String getOSDecoration(AbstractEntity e) {
		String os = e.getNamedAttribute("os");
		if (os == null) os = "";
		os = os.toLowerCase(Locale.ENGLISH);
		if (os.matches(".*linux.*"))
			return OS_LINUX;
//		if (os.matches(".*openbsd.*"))
//			return OS_OPENBSD;
		if (os.matches(".*bsd.*"))
			return OS_BSD;
		if (os.matches(".*(sunos|solaris).*"))
			return OS_SUN;
		if (os.matches(".*(macos|darwin|osx|os x).*"))
			return OS_MAC;
		if (os.matches(".*win.*"))
			return OS_WINDOWS;
//		if (os.matches(".*(novell|netware).*"))
//			return OS_NOVELL;
		if (os.matches(".*(hp.?ux|(open)?vms).*"))
			return OS_HPUX;
		if (os.matches(".*vxworks.*"))
			return OS_RTOS;
		if (os.matches(".*cisco.*"))
			return CISCO_OVERLAY;
		if (os.matches(".*junos.*"))
			return JUNIPER_OVERLAY;
//		if (os.matches(".*(as\\/?400|z\\/(os|vm)|os\\/?390).*"))
//			return IBM_OVERLAY;
		return null;
	}

	private Image getServiceImage(ServiceEntity e) {
		String type = e.getServiceType();
		String base = SERVICE;
		if (type.matches(".*SQL.*") || type.equals("Oracle"))
			base = SERVICE_DATABASE;
		else if (type.matches("SMTP|POP3|IMAP"))
			base = SERVICE_MAIL;
		else if (type.matches("SSH|Telnet"))
			base = SERVICE_SHELL;
		else if (type.matches("HTTP|HTTPS"))
			base = SERVICE_HTTP;
		else if (type.matches("FTP"))
			base = SERVICE_FILES;
		
		String overlayKeys[] = new String[5];
		overlayKeys[IDecoration.BOTTOM_LEFT] = getOSDecoration(e);
		return Activator.getInstance().getImageCache().getDecorated(base, overlayKeys);
	}

	private Image getClientImage(ClientEntity e) {
		String overlayKeys[] = new String[5];
		overlayKeys[IDecoration.BOTTOM_LEFT] = getOSDecoration(e);
		return Activator.getInstance().getImageCache().getDecorated(CLIENT, overlayKeys);
	}

	private String getNetblockText(NetblockEntity netblock) {
		return netblock.getNetblock().toString();
	}
	
	private String getHostText(HostEntity host) {
		IStructureContext context;
		try {
			context = host.getStructureContext();
		} catch (IllegalStateException e) {
			context = null;
		}
		
		String tag = "";
		if (context instanceof TreeStructureContext) {
			IShadowEntity parent = ((TreeStructureContext) context).getParent();
			if (parent instanceof FolderEntity)
				tag = "."+((FolderEntity) parent).getTag();
		}
		
		String firstName = null;
		for (NetworkAddressEntity address: host.getAddresses()) {
			if (address instanceof InternetAddressEntity) {
				for (String name: ((InternetAddressEntity)address).getNames()) {
					if (firstName == null) {
						firstName = name;
					}
					if (name.endsWith(tag)) {
						firstName = name;
						break;
					}
				}
			}
		}

		if (firstName != null) {
			StringBuffer buf = new StringBuffer();
			buf.append(firstName);

			List<String> otherNames = new ArrayList<String>();
			for (NetworkAddressEntity address: host.getAddresses()) {
				if (address instanceof InternetAddressEntity) {
					for (String name: ((InternetAddressEntity)address).getNames()) {
						if (!name.equals(firstName))
							otherNames.add(name);
					}
				}
			}

			if (otherNames.size()>0) {
				buf.append(" [");
				int last = otherNames.size()-1;
				for (int i=0; i<otherNames.size(); i++) {
					buf.append(otherNames.get(i));
					if (i != last) {
						if (buf.length()>40) {
							buf.append("..");
							break;
						}
						buf.append(", ");
					}
				}
				buf.append("]");
			}
			
			List<NetworkAddressEntity> addresses = host.getAddresses();
			buf.append(" (");
			int last = addresses.size()-1;
			for (int i=0; i<addresses.size(); i++) {
				buf.append(addresses.get(i).getAddress());
				if (i != last) {
					if (buf.length()>50) {
						buf.append("..");
						break;
					}
					buf.append(", ");
				}
			}
			buf.append(")");
			
			return buf.toString();
		}
		
		return host.getLabel() == null ? host.getDefaultAddress().getAddressString() : host.getLabel();
	}
	
	private String getAddressText(NetworkAddressEntity e) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(e.getAddress());
		if (e instanceof InternetAddressEntity) {
			InternetAddressEntity i = (InternetAddressEntity) e;
			if (i.getNames().size() > 0) {
				buffer.append(" (");
				Iterator<String> iterator = i.getNames().iterator();

				while (iterator.hasNext()) {
					buffer.append(iterator.next());
					if (iterator.hasNext())
						buffer.append(", ");
				}
				buffer.append(")");
			}
		}
		return buffer.toString();
	}

	private String getClientText(ClientEntity e) {
		StringBuilder label = new StringBuilder();
		
		label.append(e.getServiceType()+" client");
						
		if (e.getProduct() != null && e.getProduct().length() > 0) {
			label.append(" [" + e.getProduct());
			
			if (e.getVersion() != null && e.getVersion().length() > 0) {
				label.append(" " + e.getVersion());
			}
			label.append("]");
		}
		
		return label.toString();
	}
	
	private String getServiceText(ServiceEntity e) {
		StringBuilder label = new StringBuilder();
				
//		if(useServiceAddress) {
//			InternetAddressEntity address = new InternetAddressEntity(e.getParent());
//			label.append(address.getKey() + ":");
//		}
		label.append(e.getPort()+"/"+e.getProtocol()+" ");
		
		label.append(e.getServiceType()+" ");

		if (e.getProduct() != null && e.getProduct().length() > 0 && !e.getProduct().equals(e.getServiceType())) {
			label.append(" ["+e.getProduct());
			if (e.getVersion() != null && e.getVersion().length() > 0)
				label.append(" "+e.getVersion());
			label.append("]");
		} else if (e.getVersion() != null && e.getVersion().length()>0) {
			label.append(" ["+e.getVersion()+"]");
		}
		
		return label.toString();
	}

	private String getServiceConnectionText(ClientServiceConnectionEntity e) {
		InternetAddressEntity address = e.getService().getAddress();
		HostEntity host = address.getHost();
		
		StringBuffer buffer = new StringBuffer();
		
		if (host.getLabel() != null) {
			buffer.append(host.getLabel());
		} else {
			buffer.append(address.getAddress().toStringLiteral());
		}
		buffer.append(':'); // FIXME '/' for IPv6?
		buffer.append(getServiceText(e.getService()));
		if (e.getIdentity() != null) {
			buffer.append('(' + e.getIdentity() + ')');
		}
		return buffer.toString();
	}
	
	private final static int PORTS_FORMATTED_LINE_LENGTH = 40;
	private final static int PORTS_TRUNCATE_LENGTH = 50;

	private String getPortsetText(PortSetEntity portset) {
		return portset.getProtocol().toUpperCase(Locale.ENGLISH) + " [" + getPortString(portset) + "]";
		
	}
	
	private String getPortString(PortSetEntity portset) {
		final String ports = portset.getPorts();
		if(ports.length() < PORTS_TRUNCATE_LENGTH) return ports;
		int idx = ports.lastIndexOf(',', PORTS_TRUNCATE_LENGTH);
		if(idx == -1) return ports;
		return ports.substring(0,idx) + "...";
		
	}
	
	public void dispose() {
	}

	public Integer getSortingCategory(IShadowEntity e) {
		/* top level ordering */
		if(e instanceof FolderEntity) {
			FolderEntity folder = (FolderEntity) e;
			if(folder.getTag().equals("Target"))
				return 0;
			else if(folder.getTag().equals("Controlled"))
				return 1;
			else if(NetblockMatcher.matchesIPv4(folder.getTag()))
				return 5;
			else if(NetblockMatcher.matchesIPv6(folder.getTag()))
				return 6;
			else
				return 3;
		}
		
		/* ordering below host entity */
		if(e instanceof PortSetEntity) {
			return 0;
		}
		if(e instanceof ServiceEntity) {
			return 2;
		}
		/* ordering inside network folders */
		if(e instanceof NetblockEntity) {
			return 0;
		}
		if(e instanceof HostEntity) {
			return 1;
		}
		return null;
	}

	public Integer compare(IShadowEntity e1, IShadowEntity e2) {
		if(e1 instanceof ServiceEntity && e2 instanceof ServiceEntity) {
			return compareServiceEntities((ServiceEntity)e1, (ServiceEntity) e2);
		}
		if(e1 instanceof HostEntity && e2 instanceof HostEntity) {
			return compareHostEntities((HostEntity)e1, (HostEntity) e2);
		}
		if(e1 instanceof FolderEntity && e2 instanceof FolderEntity) {
			return compareFolderEntities((FolderEntity)e1, (FolderEntity) e2);
		}
		return null;
	}
	
	private int compareServiceEntities(ServiceEntity e1, ServiceEntity e2) {
		if(e1.getPort() < e2.getPort()) 
			return -1;
		else if(e1.getPort() > e2.getPort())
			return 1;
		else 
			return e1.getProtocol().compareToIgnoreCase(e2.getProtocol());
	}
	
	// TODO review (james: compare addresses?)
	private Integer compareHostEntities(HostEntity e1, HostEntity e2) {
		String s1 = getHostText(e1).split("\\s")[0];
		String s2 = getHostText(e2).split("\\s")[0];
		InternetAddressMatcher m1 = new InternetAddressMatcher(s1);
		InternetAddressMatcher m2 = new InternetAddressMatcher(s2);
		
		if (m1.matches() && m2.matches()) {
			return InternetAddress.fromString(s1).compareTo(InternetAddress.fromString(s2));
		}
		
		return null;
	}
	
	private Integer compareFolderEntities(FolderEntity e1, FolderEntity e2) {
		String tag1 = e1.getTag();
		String tag2 = e2.getTag();
		
		// need to do v4 and v6 separately
		if (NetblockMatcher.matchesIPv4(tag1) && NetblockMatcher.matchesIPv4(tag2)) {
			return InternetNetblock.fromString(tag1).compareTo(
					InternetNetblock.fromString(tag2));
		}
		else if (NetblockMatcher.matchesIPv6(tag1) && NetblockMatcher.matchesIPv6(tag2)) {
			return InternetNetblock.fromString(tag1).compareTo(
					InternetNetblock.fromString(tag2));
		}

		return null;
//		return tag1.compareToIgnoreCase(tag2);
	}
}
