package com.netifera.platform.net.dns.internal.ui;

import java.util.Iterator;
import java.util.Locale;

import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.net.dns.model.AAAARecordEntity;
import com.netifera.platform.net.dns.model.ARecordEntity;
import com.netifera.platform.net.dns.model.DomainEntity;
import com.netifera.platform.net.dns.model.EmailAddressEntity;
import com.netifera.platform.net.dns.model.MXRecordEntity;
import com.netifera.platform.net.dns.model.NSRecordEntity;
import com.netifera.platform.net.dns.model.PTRRecordEntity;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.net.model.NetworkAddressEntity;
import com.netifera.platform.ui.api.model.IEntityInformationProvider;
import com.netifera.platform.ui.api.model.IEntityLabelProvider;
import com.netifera.platform.ui.images.ImageCache;

public class EntityLabelProvider implements IEntityLabelProvider, IEntityInformationProvider {
	private final static String PLUGIN_ID = "com.netifera.platform.net.dns.ui";

	private ImageCache images = new ImageCache(PLUGIN_ID);

	private static final String DOMAIN = "icons/domain.png";
	private static final String EMAIL = "icons/email_address.png";
	private static final String ARECORD = "icons/pointer.png";
	private static final String PTRRECORD = "icons/pointer-back.png";
	private static final String MXRECORD = "icons/email.png";
	private static final String NSRECORD = "icons/dns.png";

	public String getText(IShadowEntity e) {
		if (e instanceof DomainEntity) {
			return ((DomainEntity) e).getFQDM();
		} else if (e instanceof EmailAddressEntity) {
			EmailAddressEntity email = (EmailAddressEntity) e;
			if (email.getName() != null)
				return email.getName()+" <"+email.getAddress()+">";
			else
				return email.getAddress();
		} else if (e instanceof ARecordEntity) {
			ARecordEntity hostname = (ARecordEntity) e;
			return hostname.getHostName() + "  A  "
					+ hostname.getAddressEntity().getAddress();
		} else if (e instanceof AAAARecordEntity) {
			AAAARecordEntity hostname = (AAAARecordEntity) e;
			return hostname.getHostName() + "  AAAA  "
					+ hostname.getAddressEntity().getAddress();
		} else if (e instanceof PTRRecordEntity) {
			PTRRecordEntity ptr = (PTRRecordEntity) e;
			return ptr.getAddressEntity().getAddress() + "  PTR  "
					+ ptr.getName();
		} else if (e instanceof NSRecordEntity) {
			NSRecordEntity ns = (NSRecordEntity) e;
			return "NS  " + ns.getTarget();
		} else if (e instanceof MXRecordEntity) {
			MXRecordEntity mx = (MXRecordEntity) e;
			return "MX  " + mx.getPriority() + "  " + mx.getTarget();
		}
		return null;
	}
	
	public String getFullText(IShadowEntity e) {
		return getText(e);
	}

	public Image getImage(IShadowEntity e) {
		if (e instanceof DomainEntity) {
			return images.get(DOMAIN);
		} else if (e instanceof EmailAddressEntity) {
			return images.get(EMAIL);
		} else if (e instanceof ARecordEntity || e instanceof AAAARecordEntity) {
			return images.get(ARECORD);
		} else if (e instanceof PTRRecordEntity) {
			return images.get(PTRRECORD);
		} else if (e instanceof NSRecordEntity) {
			return images.get(NSRECORD);
		} else if (e instanceof MXRecordEntity) {
			return images.get(MXRECORD);
		}
		return null;
	}

	public Image decorateImage(Image image, IShadowEntity e) {
		return null;
	}

	public String getInformation(IShadowEntity e) {
		if (e instanceof HostEntity) {
			return getHostInformation((HostEntity)e);
		}
		return null;
	}

	private String getHostInformation(HostEntity e) {
		StringBuffer buffer = new StringBuffer();
		
		if (e.getLabel() != null) {
			Iterator<NetworkAddressEntity> addresses = e.getAddresses().iterator();
			boolean hasNames = false;
			while (addresses.hasNext()) {
				NetworkAddressEntity a = addresses.next();
				if (a instanceof InternetAddressEntity) {
					Iterator<String> names = ((InternetAddressEntity)a).getNames().iterator();
					if (names.hasNext()) {
						if (!hasNames) {
							buffer.append("<p>Names: ");
							hasNames = true;
						}
						while (names.hasNext()) {
							buffer.append(escape(names.next()));
							if (names.hasNext())
								buffer.append(", ");
						}
						buffer.append(" ("+escape(a.getAddressString())+")");
						if (addresses.hasNext())
							buffer.append("; ");
					}
				}
			}
			if (hasNames)
				buffer.append("</p>");
		}

		String countryCode = e.getDefaultAddress().getNamedAttribute("country");
		if (countryCode != null) {
			Locale locale = new Locale("en", countryCode);
			String countryName = locale.getDisplayCountry(Locale.ENGLISH);
			buffer.append("<p>Country: "+escape(countryName)+"</p>");
		}
		
		return buffer.toString();
	}
	
	private String escape(String data) {
		data = data.replaceAll("&", "&amp;");
		data = data.replaceAll("<", "&lt;");
		data = data.replaceAll(">", "&gt;");
		data = data.trim().replaceAll("[\\r\\n]+", "</p><p>");
		return data.replaceAll("[^\\p{Print}\\p{Blank}]", "."); // non-printable chars
	}

	public void dispose() {
		images.dispose();
	}

	public Integer getSortingCategory(IShadowEntity e) {
		if(e instanceof DomainEntity) 
			return 1;
		return null;
	}

	public Integer compare(IShadowEntity e1, IShadowEntity e2) {
		return null;
	}
}
