package com.netifera.platform.net.internal.geoip;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.net.geoip.IGeoIPService;
import com.netifera.platform.net.geoip.IGeographicalLayerProvider;
import com.netifera.platform.net.geoip.ILocation;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.net.model.NetworkAddressEntity;

public class HostsGeographicalLayerProvider implements IGeographicalLayerProvider {

	private IGeoIPService geoipService;


	private Map<String,double[]> countryToPosition = new HashMap<String,double[]>();
	
	{
//	"iso 3166 country","latitude","longitude"
		countryToPosition.put("AD",new double[] {42.5000,1.5000});
		countryToPosition.put("AE", new double[] {24.0000,54.0000});
		countryToPosition.put("AF", new double[] {33.0000,65.0000});
		countryToPosition.put("AG", new double[] {17.0500,-61.8000});
		countryToPosition.put("AI", new double[] {18.2500,-63.1667});
		countryToPosition.put("AL", new double[] {41.0000,20.0000});
		countryToPosition.put("AM", new double[] {40.0000,45.0000});
		countryToPosition.put("AN", new double[] {12.2500,-68.7500});
		countryToPosition.put("AO", new double[] {-12.5000,18.5000});
		countryToPosition.put("AP", new double[] {35.0000,105.0000});
		countryToPosition.put("AQ", new double[] {-90.0000,0.0000});
		countryToPosition.put("AR", new double[] {-34.0000,-64.0000});
		countryToPosition.put("AS", new double[] {-14.3333,-170.0000});
		countryToPosition.put("AT", new double[] {47.3333,13.3333});
		countryToPosition.put("AU", new double[] {-27.0000,133.0000});
		countryToPosition.put("AW", new double[] {12.5000,-69.9667});
		countryToPosition.put("AZ", new double[] {40.5000,47.5000});
		countryToPosition.put("BA", new double[] {44.0000,18.0000});
		countryToPosition.put("BB", new double[] {13.1667,-59.5333});
		countryToPosition.put("BD", new double[] {24.0000,90.0000});
		countryToPosition.put("BE", new double[] {50.8333,4.0000});
		countryToPosition.put("BF", new double[] {13.0000,-2.0000});
		countryToPosition.put("BG", new double[] {43.0000,25.0000});
		countryToPosition.put("BH", new double[] {26.0000,50.5500});
		countryToPosition.put("BI", new double[] {-3.5000,30.0000});
		countryToPosition.put("BJ", new double[] {9.5000,2.2500});
		countryToPosition.put("BM", new double[] {32.3333,-64.7500});
		countryToPosition.put("BN", new double[] {4.5000,114.6667});
		countryToPosition.put("BO", new double[] {-17.0000,-65.0000});
		countryToPosition.put("BR", new double[] {-10.0000,-55.0000});
		countryToPosition.put("BS", new double[] {24.2500,-76.0000});
		countryToPosition.put("BT", new double[] {27.5000,90.5000});
		countryToPosition.put("BV", new double[] {-54.4333,3.4000});
		countryToPosition.put("BW", new double[] {-22.0000,24.0000});
		countryToPosition.put("BY", new double[] {53.0000,28.0000});
		countryToPosition.put("BZ", new double[] {17.2500,-88.7500});
		countryToPosition.put("CA", new double[] {60.0000,-95.0000});
		countryToPosition.put("CC", new double[] {-12.5000,96.8333});
		countryToPosition.put("CD", new double[] {0.0000,25.0000});
		countryToPosition.put("CF", new double[] {7.0000,21.0000});
		countryToPosition.put("CG", new double[] {-1.0000,15.0000});
		countryToPosition.put("CH", new double[] {47.0000,8.0000});
		countryToPosition.put("CI", new double[] {8.0000,-5.0000});
		countryToPosition.put("CK", new double[] {-21.2333,-159.7667});
		countryToPosition.put("CL", new double[] {-30.0000,-71.0000});
		countryToPosition.put("CM", new double[] {6.0000,12.0000});
		countryToPosition.put("CN", new double[] {35.0000,105.0000});
		countryToPosition.put("CO", new double[] {4.0000,-72.0000});
		countryToPosition.put("CR", new double[] {10.0000,-84.0000});
		countryToPosition.put("CU", new double[] {21.5000,-80.0000});
		countryToPosition.put("CV", new double[] {16.0000,-24.0000});
		countryToPosition.put("CX", new double[] {-10.5000,105.6667});
		countryToPosition.put("CY", new double[] {35.0000,33.0000});
		countryToPosition.put("CZ", new double[] {49.7500,15.5000});
		countryToPosition.put("DE", new double[] {51.0000,9.0000});
		countryToPosition.put("DJ", new double[] {11.5000,43.0000});
		countryToPosition.put("DK", new double[] {56.0000,10.0000});
		countryToPosition.put("DM", new double[] {15.4167,-61.3333});
		countryToPosition.put("DO", new double[] {19.0000,-70.6667});
		countryToPosition.put("DZ", new double[] {28.0000,3.0000});
		countryToPosition.put("EC", new double[] {-2.0000,-77.5000});
		countryToPosition.put("EE", new double[] {59.0000,26.0000});
		countryToPosition.put("EG", new double[] {27.0000,30.0000});
		countryToPosition.put("EH", new double[] {24.5000,-13.0000});
		countryToPosition.put("ER", new double[] {15.0000,39.0000});
		countryToPosition.put("ES", new double[] {40.0000,-4.0000});
		countryToPosition.put("ET", new double[] {8.0000,38.0000});
		countryToPosition.put("EU", new double[] {47.0000,8.0000});
		countryToPosition.put("FI", new double[] {64.0000,26.0000});
		countryToPosition.put("FJ", new double[] {-18.0000,175.0000});
		countryToPosition.put("FK", new double[] {-51.7500,-59.0000});
		countryToPosition.put("FM", new double[] {6.9167,158.2500});
		countryToPosition.put("FO", new double[] {62.0000,-7.0000});
		countryToPosition.put("FR", new double[] {46.0000,2.0000});
		countryToPosition.put("GA", new double[] {-1.0000,11.7500});
		countryToPosition.put("GB", new double[] {54.0000,-2.0000});
		countryToPosition.put("GD", new double[] {12.1167,-61.6667});
		countryToPosition.put("GE", new double[] {42.0000,43.5000});
		countryToPosition.put("GF", new double[] {4.0000,-53.0000});
		countryToPosition.put("GH", new double[] {8.0000,-2.0000});
		countryToPosition.put("GI", new double[] {36.1833,-5.3667});
		countryToPosition.put("GL", new double[] {72.0000,-40.0000});
		countryToPosition.put("GM", new double[] {13.4667,-16.5667});
		countryToPosition.put("GN", new double[] {11.0000,-10.0000});
		countryToPosition.put("GP", new double[] {16.2500,-61.5833});
		countryToPosition.put("GQ", new double[] {2.0000,10.0000});
		countryToPosition.put("GR", new double[] {39.0000,22.0000});
		countryToPosition.put("GS", new double[] {-54.5000,-37.0000});
		countryToPosition.put("GT", new double[] {15.5000,-90.2500});
		countryToPosition.put("GU", new double[] {13.4667,144.7833});
		countryToPosition.put("GW", new double[] {12.0000,-15.0000});
		countryToPosition.put("GY", new double[] {5.0000,-59.0000});
		countryToPosition.put("HK", new double[] {22.2500,114.1667});
		countryToPosition.put("HM", new double[] {-53.1000,72.5167});
		countryToPosition.put("HN", new double[] {15.0000,-86.5000});
		countryToPosition.put("HR", new double[] {45.1667,15.5000});
		countryToPosition.put("HT", new double[] {19.0000,-72.4167});
		countryToPosition.put("HU", new double[] {47.0000,20.0000});
		countryToPosition.put("ID", new double[] {-5.0000,120.0000});
		countryToPosition.put("IE", new double[] {53.0000,-8.0000});
		countryToPosition.put("IL", new double[] {31.5000,34.7500});
		countryToPosition.put("IN", new double[] {20.0000,77.0000});
		countryToPosition.put("IO", new double[] {-6.0000,71.5000});
		countryToPosition.put("IQ", new double[] {33.0000,44.0000});
		countryToPosition.put("IR", new double[] {32.0000,53.0000});
		countryToPosition.put("IS", new double[] {65.0000,-18.0000});
		countryToPosition.put("IT", new double[] {42.8333,12.8333});
		countryToPosition.put("JM", new double[] {18.2500,-77.5000});
		countryToPosition.put("JO", new double[] {31.0000,36.0000});
		countryToPosition.put("JP", new double[] {36.0000,138.0000});
		countryToPosition.put("KE", new double[] {1.0000,38.0000});
		countryToPosition.put("KG", new double[] {41.0000,75.0000});
		countryToPosition.put("KH", new double[] {13.0000,105.0000});
		countryToPosition.put("KI", new double[] {1.4167,173.0000});
		countryToPosition.put("KM", new double[] {-12.1667,44.2500});
		countryToPosition.put("KN", new double[] {17.3333,-62.7500});
		countryToPosition.put("KP", new double[] {40.0000,127.0000});
		countryToPosition.put("KR", new double[] {37.0000,127.5000});
		countryToPosition.put("KW", new double[] {29.3375,47.6581});
		countryToPosition.put("KY", new double[] {19.5000,-80.5000});
		countryToPosition.put("KZ", new double[] {48.0000,68.0000});
		countryToPosition.put("LA", new double[] {18.0000,105.0000});
		countryToPosition.put("LB", new double[] {33.8333,35.8333});
		countryToPosition.put("LC", new double[] {13.8833,-61.1333});
		countryToPosition.put("LI", new double[] {47.1667,9.5333});
		countryToPosition.put("LK", new double[] {7.0000,81.0000});
		countryToPosition.put("LR", new double[] {6.5000,-9.5000});
		countryToPosition.put("LS", new double[] {-29.5000,28.5000});
		countryToPosition.put("LT", new double[] {56.0000,24.0000});
		countryToPosition.put("LU", new double[] {49.7500,6.1667});
		countryToPosition.put("LV", new double[] {57.0000,25.0000});
		countryToPosition.put("LY", new double[] {25.0000,17.0000});
		countryToPosition.put("MA", new double[] {32.0000,-5.0000});
		countryToPosition.put("MC", new double[] {43.7333,7.4000});
		countryToPosition.put("MD", new double[] {47.0000,29.0000});
		countryToPosition.put("ME", new double[] {42.0000,19.0000});
		countryToPosition.put("MG", new double[] {-20.0000,47.0000});
		countryToPosition.put("MH", new double[] {9.0000,168.0000});
		countryToPosition.put("MK", new double[] {41.8333,22.0000});
		countryToPosition.put("ML", new double[] {17.0000,-4.0000});
		countryToPosition.put("MM", new double[] {22.0000,98.0000});
		countryToPosition.put("MN", new double[] {46.0000,105.0000});
		countryToPosition.put("MO", new double[] {22.1667,113.5500});
		countryToPosition.put("MP", new double[] {15.2000,145.7500});
		countryToPosition.put("MQ", new double[] {14.6667,-61.0000});
		countryToPosition.put("MR", new double[] {20.0000,-12.0000});
		countryToPosition.put("MS", new double[] {16.7500,-62.2000});
		countryToPosition.put("MT", new double[] {35.8333,14.5833});
		countryToPosition.put("MU", new double[] {-20.2833,57.5500});
		countryToPosition.put("MV", new double[] {3.2500,73.0000});
		countryToPosition.put("MW", new double[] {-13.5000,34.0000});
		countryToPosition.put("MX", new double[] {23.0000,-102.0000});
		countryToPosition.put("MY", new double[] {2.5000,112.5000});
		countryToPosition.put("MZ", new double[] {-18.2500,35.0000});
		countryToPosition.put("NA", new double[] {-22.0000,17.0000});
		countryToPosition.put("NC", new double[] {-21.5000,165.5000});
		countryToPosition.put("NE", new double[] {16.0000,8.0000});
		countryToPosition.put("NF", new double[] {-29.0333,167.9500});
		countryToPosition.put("NG", new double[] {10.0000,8.0000});
		countryToPosition.put("NI", new double[] {13.0000,-85.0000});
		countryToPosition.put("NL", new double[] {52.5000,5.7500});
		countryToPosition.put("NO", new double[] {62.0000,10.0000});
		countryToPosition.put("NP", new double[] {28.0000,84.0000});
		countryToPosition.put("NR", new double[] {-0.5333,166.9167});
		countryToPosition.put("NU", new double[] {-19.0333,-169.8667});
		countryToPosition.put("NZ", new double[] {-41.0000,174.0000});
		countryToPosition.put("OM", new double[] {21.0000,57.0000});
		countryToPosition.put("PA", new double[] {9.0000,-80.0000});
		countryToPosition.put("PE", new double[] {-10.0000,-76.0000});
		countryToPosition.put("PF", new double[] {-15.0000,-140.0000});
		countryToPosition.put("PG", new double[] {-6.0000,147.0000});
		countryToPosition.put("PH", new double[] {13.0000,122.0000});
		countryToPosition.put("PK", new double[] {30.0000,70.0000});
		countryToPosition.put("PL", new double[] {52.0000,20.0000});
		countryToPosition.put("PM", new double[] {46.8333,-56.3333});
		countryToPosition.put("PR", new double[] {18.2500,-66.5000});
		countryToPosition.put("PS", new double[] {32.0000,35.2500});
		countryToPosition.put("PT", new double[] {39.5000,-8.0000});
		countryToPosition.put("PW", new double[] {7.5000,134.5000});
		countryToPosition.put("PY", new double[] {-23.0000,-58.0000});
		countryToPosition.put("QA", new double[] {25.5000,51.2500});
		countryToPosition.put("RE", new double[] {-21.1000,55.6000});
		countryToPosition.put("RO", new double[] {46.0000,25.0000});
		countryToPosition.put("RS", new double[] {44.0000,21.0000});
		countryToPosition.put("RU", new double[] {60.0000,100.0000});
		countryToPosition.put("RW", new double[] {-2.0000,30.0000});
		countryToPosition.put("SA", new double[] {25.0000,45.0000});
		countryToPosition.put("SB", new double[] {-8.0000,159.0000});
		countryToPosition.put("SC", new double[] {-4.5833,55.6667});
		countryToPosition.put("SD", new double[] {15.0000,30.0000});
		countryToPosition.put("SE", new double[] {62.0000,15.0000});
		countryToPosition.put("SG", new double[] {1.3667,103.8000});
		countryToPosition.put("SH", new double[] {-15.9333,-5.7000});
		countryToPosition.put("SI", new double[] {46.0000,15.0000});
		countryToPosition.put("SJ", new double[] {78.0000,20.0000});
		countryToPosition.put("SK", new double[] {48.6667,19.5000});
		countryToPosition.put("SL", new double[] {8.5000,-11.5000});
		countryToPosition.put("SM", new double[] {43.7667,12.4167});
		countryToPosition.put("SN", new double[] {14.0000,-14.0000});
		countryToPosition.put("SO", new double[] {10.0000,49.0000});
		countryToPosition.put("SR", new double[] {4.0000,-56.0000});
		countryToPosition.put("ST", new double[] {1.0000,7.0000});
		countryToPosition.put("SV", new double[] {13.8333,-88.9167});
		countryToPosition.put("SY", new double[] {35.0000,38.0000});
		countryToPosition.put("SZ", new double[] {-26.5000,31.5000});
		countryToPosition.put("TC", new double[] {21.7500,-71.5833});
		countryToPosition.put("TD", new double[] {15.0000,19.0000});
		countryToPosition.put("TF", new double[] {-43.0000,67.0000});
		countryToPosition.put("TG", new double[] {8.0000,1.1667});
		countryToPosition.put("TH", new double[] {15.0000,100.0000});
		countryToPosition.put("TJ", new double[] {39.0000,71.0000});
		countryToPosition.put("TK", new double[] {-9.0000,-172.0000});
		countryToPosition.put("TM", new double[] {40.0000,60.0000});
		countryToPosition.put("TN", new double[] {34.0000,9.0000});
		countryToPosition.put("TO", new double[] {-20.0000,-175.0000});
		countryToPosition.put("TR", new double[] {39.0000,35.0000});
		countryToPosition.put("TT", new double[] {11.0000,-61.0000});
		countryToPosition.put("TV", new double[] {-8.0000,178.0000});
		countryToPosition.put("TW", new double[] {23.5000,121.0000});
		countryToPosition.put("TZ", new double[] {-6.0000,35.0000});
		countryToPosition.put("UA", new double[] {49.0000,32.0000});
		countryToPosition.put("UG", new double[] {1.0000,32.0000});
		countryToPosition.put("UM", new double[] {19.2833,166.6000});
		countryToPosition.put("US", new double[] {38.0000,-97.0000});
		countryToPosition.put("UY", new double[] {-33.0000,-56.0000});
		countryToPosition.put("UZ", new double[] {41.0000,64.0000});
		countryToPosition.put("VA", new double[] {41.9000,12.4500});
		countryToPosition.put("VC", new double[] {13.2500,-61.2000});
		countryToPosition.put("VE", new double[] {8.0000,-66.0000});
		countryToPosition.put("VG", new double[] {18.5000,-64.5000});
		countryToPosition.put("VI", new double[] {18.3333,-64.8333});
		countryToPosition.put("VN", new double[] {16.0000,106.0000});
		countryToPosition.put("VU", new double[] {-16.0000,167.0000});
		countryToPosition.put("WF", new double[] {-13.3000,-176.2000});
		countryToPosition.put("WS", new double[] {-13.5833,-172.3333});
		countryToPosition.put("YE", new double[] {15.0000,48.0000});
		countryToPosition.put("YT", new double[] {-12.8333,45.1667});
		countryToPosition.put("ZA", new double[] {-29.0000,24.0000});
		countryToPosition.put("ZM", new double[] {-15.0000,30.0000});
		countryToPosition.put("ZW", new double[] {-20.0000,30.0000});
	};
	
	public ILocation getLocation(IEntity entity) {
		if (!(entity instanceof HostEntity))
			return null;
		
		for (NetworkAddressEntity addressEntity: ((HostEntity)entity).getAddresses()) {
				if (addressEntity instanceof InternetAddressEntity) {
					ILocation location = geoipService.getLocation(((InternetAddressEntity)addressEntity).getAddress());
					if (location != null) return location;
				}
		}
		
		final String countryCode = ((HostEntity)entity).getNamedAttribute("country");
		if (countryCode != null) {
			return new ILocation() {
				public String getCity() {
					return null;
				}
				public String getCountry() {
					Locale locale = new Locale("en", countryCode);
					return locale.getDisplayCountry(Locale.ENGLISH);
				}
				public String getCountryCode() {
					return countryCode;
				}
				public double[] getPosition() {
					double[] pos = countryToPosition.get(countryCode);
					if (pos != null)
						return new double[] {pos[0],pos[1]};
					return null;
				}
				public String getPostalCode() {
					return null;
				}
			};
		}
		return null;
	}

	public String getLayerName() {
		return "Hosts";
	}

	public boolean isDefaultEnabled() {
		return true;
	}
	
	protected void setGeoIPService(IGeoIPService service) {
		this.geoipService = service;
	}
	
	protected void unsetGeoIPService(IGeoIPService service) {
		this.geoipService = null;
	}
}
