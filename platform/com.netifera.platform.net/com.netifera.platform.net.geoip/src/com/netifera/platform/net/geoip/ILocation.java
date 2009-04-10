package com.netifera.platform.net.geoip;

public interface ILocation {
	double[] getPosition();
	String getCountry();
	String getCountryCode();
	String getCity();
	String getPostalCode();
}
