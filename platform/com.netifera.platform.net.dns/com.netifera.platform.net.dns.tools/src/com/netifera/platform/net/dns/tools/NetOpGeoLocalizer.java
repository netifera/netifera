package com.netifera.platform.net.dns.tools;

import java.util.List;
import java.util.Locale;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.dns.internal.tools.Activator;
import com.netifera.platform.net.dns.service.nameresolver.INameResolver;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class NetOpGeoLocalizer implements ITool {
	private IToolContext context;
	private INameResolver resolver;
	private IndexedIterable<InternetAddress> addresses;
	private long realm;
	
	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;

		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();

		setupToolOptions();
		context.setTitle("Geo-localize " + addresses);
		
		resolver = Activator.getInstance().getNameResolver();

		// FIXME
		if (addresses.itemCount() > 0 && !(addresses.itemAt(0) instanceof IPv4Address)) {
			context.warning("IPv6 not yet supported by NetOp");
			context.done();
			return;
		}
		context.setTotalWork(addresses.itemCount());
		for (InternetAddress address: addresses) {
			localizate(address);
		}
		
		context.done();
	}

	@SuppressWarnings("unchecked")
	private void setupToolOptions() {
		addresses = (IndexedIterable<InternetAddress>) context.getConfiguration().get("target");
	}
	
	private void localizate(final InternetAddress address) {
		if (!(address instanceof IPv4Address)) {
			// FIXME
			context.warning("IPv6 not yet supported by NetOp");
			return;
		}
		
		context.setStatus("Localizing "+address);
		
		// XXX lame
		String[] octets = address.toString().split("\\.");
		InternetAddress revAddress = InternetAddress.fromString(octets[3]+"."+octets[2]+"."+octets[1]+"."+octets[0]); // IPv4 only

		Lookup lookup;
		String revName = revAddress.toString();
		try {
			lookup = new Lookup(revName + ".country.netop.org.", Type.TXT);
		} catch (TextParseException e) {
			context.warning("Malformed host name: " + revName);
			return;
		}
		lookup.setResolver(resolver.getExtendedResolver());
		lookup.setSearchPath((Name[])null);

		Record [] records = lookup.run();
		context.worked(1);
		if (records != null && records.length != 0) {
			for (Record record: records) {
				if (record instanceof TXTRecord) {
					List<String> list = txtStrings(record);
					if (!list.isEmpty()) {
						String countryCode = list.get(0);
						if (!countryCode.equals("ZZ")) {
							Locale locale = new Locale("en", list.get(0));
							String countryName = locale.getDisplayCountry(Locale.ENGLISH);
							context.info(address.toString() + " is in " + countryName);
							
							InternetAddressEntity entity = Activator.getInstance().getNetworkEntityFactory().createAddress(realm, context.getSpaceId(), address);
							entity.setNamedAttribute("country", countryCode);
							entity.update();
							entity.getHost().setNamedAttribute("country", countryCode);
//							entity.getHost().addTag(countryName);
							entity.getHost().update(); //HACK or the tree builder never gets called to update
							
	//						Activator.getInstance().getNetworkEntityFactory().createAttributeFolder(realm, context.getViewId(), countryName, "host", "country", countryCode);
							return;
						}
					}
				} else {
					context.warning("Unhandled DNS record: " + record);
				}
			}
		}
		context.info("No TXT records found for " + address.toString());
	}
	
	@SuppressWarnings("unchecked")
	private List<String> txtStrings(Record record) {
		return ((TXTRecord)record).getStrings();
	}
}
