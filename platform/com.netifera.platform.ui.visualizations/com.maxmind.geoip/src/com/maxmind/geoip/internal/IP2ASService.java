package com.maxmind.geoip.internal;


import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.ComponentContext;

import com.maxmind.geoip.LookupService;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.routes.AS;
import com.netifera.platform.net.routes.IIP2ASService;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class IP2ASService implements IIP2ASService {
	static final private String DB_FILENAME = "GeoIPASNum.dat";
	
	private LookupService lookupService;
	
	private ILogger logger;
	
	private Pattern asnumPattern = Pattern.compile("AS([\\d]+)[^\\d]*");
	
	public synchronized AS getAS(InternetAddress address) {
		final String as = lookupService.getOrg(address.toInetAddress());
		if (as == null)
			return null;
		return new AS() {
			public String getDescription() {
				return as;
			}
			public long getNumber() {
				Matcher matcher = asnumPattern.matcher(as);
				String asnum = matcher.group(1);
				if (asnum != null && asnum.length()>0)
					return Long.parseLong(asnum);
				return 0;
			}
		};
	}
	
	protected void activate(ComponentContext context) {
		if (lookupService == null)
			try {
				String path = getDBPath();
				verifyDBPath(path);
				lookupService = new LookupService(path, LookupService.GEOIP_MEMORY_CACHE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}
	}
	
	protected void deactivate(ComponentContext context) {
		if (lookupService != null) {
			try {
				lookupService.close();
			} finally {
				lookupService = null;
			}
		}
	}

	private String getDBPath() {
		if(isRunningInEclipse()) {
			return getEclipseDBPath();
		} else {
			return getBuildDBPath();
		}
	}
	
	private String getEclipseDBPath() {
		final String basePath = getBasePathForEclipse();
		if(basePath == null) {
			logger.error("Could not locate base path for DB binary.");
			return null;
		}
		return basePath + DB_FILENAME;
	}

	private String getBuildDBPath() {
		final String basePath = getBasePathForBuild();
		if(basePath == null) {
			logger.error("Could not locate base path for DB binary.");
			return null;
		}
		return basePath + DB_FILENAME;
	}
	
	private boolean verifyDBPath(String path) {
		if(path == null) { 
			return false;
		}
		File file = new File(path);
		if (!file.exists()) {
			logger.info("DB binary not found at " + path);
			return false;
		} 
		logger.info("DB binary located at " + path);
		return true;
	}
	
	private String getBasePathForBuild() {
		final String installArea =  System.getProperty("osgi.install.area");
		if(installArea == null || !installArea.startsWith("file:")) {
			return null;
		}
		return installArea.substring(5);
	}
	
	private String getBasePathForEclipse() {
		final String configArea = System.getProperty("osgi.configuration.area");
		if(configArea == null || !configArea.startsWith("file:")) {
			return null;
		}
		final String trimmedPath = configArea.substring(5);
		int metadataIndex = trimmedPath.indexOf(".metadata");
		if(metadataIndex == -1)
			return null;
		return trimmedPath.substring(0, metadataIndex);
	}
	
	private boolean isRunningInEclipse() {
		return System.getProperty("osgi.dev") != null;
	}
	
	protected void setLogManager(ILogManager logManager) {
		this.logger = logManager.getLogger("IP2AS Service");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
	}
}
