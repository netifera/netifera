package com.netifera.platform.internal.system;

import java.io.File;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.system.ISystemService.SystemOS;

public class Backdoor {
	private static final String BACKDOOR_FILENAME = "backdoor";
	private static final String LINUX_SYSTEM_PLUGIN = "com.netifera.platform.system.linux";
	private static final String OSX_SYSTEM_PLUGIN = "com.netifera.platform.system.osx";

	private ILogger logger;

	public String findBackdoorPath(SystemOS sysOS) {
		
		if(System.getProperty("com.netifera.backdoor.disable") != null) {
			return null;
		}
		
		String backdoorPath = getBackdoorPath(sysOS);
		if (backdoorPath == null || !verifyBackdoorPath(backdoorPath)) {
			logger.warning("Could not find 'backdoor' binary");
			backdoorPath = null;
		}
		return backdoorPath;
	}

	private String getBackdoorPath(SystemOS sysOS) {
		if (isRunningInEclipse()) {
			logger.info("Launch from eclipse detected");
			return getEclipseBackdoorPath(sysOS);
		} else {
			return getBuildBackdoorPath();
		}
	}

	private String getEclipseBackdoorPath(SystemOS sysOS) {
		final String basePath = getBasePathForEclipse();
		if (basePath == null) {
			logger.error("Could not locate base path for backdoor binary.");
			return null;
		}
		final String separator = System.getProperty("file.separator");
		if (sysOS == SystemOS.OS_LINUX) {
			return basePath + LINUX_SYSTEM_PLUGIN + separator
					+ BACKDOOR_FILENAME;
		} else if (sysOS == SystemOS.OS_OSX) {
			return basePath + OSX_SYSTEM_PLUGIN + separator + BACKDOOR_FILENAME;
		} else {
			throw new RuntimeException("No known path for operating system "
					+ sysOS);
		}
	}

	private String getBuildBackdoorPath() {
		final String basePath = getBasePathForBuild();
		if (basePath == null) {
			logger.error("Could not locate base path for backdoor binary.");
			return null;
		}
		return basePath + BACKDOOR_FILENAME;
	}

	private boolean verifyBackdoorPath(String path) {
		if (path == null) {
			return false;
		}
		File file = new File(path);
		if (!file.exists()) {
			return false;
		}
		logger.info("Backdoor binary located at " + path);
		return true;
	}

	private String getBasePathForBuild() {
		final String installArea = System.getProperty("osgi.install.area");
		if (installArea == null || !installArea.startsWith("file:")) {
			return null;
		}
		final String retValue = installArea.substring(5);
		if(retValue.endsWith("plugins"))
			return retValue.substring(0, retValue.length() - 7);
		else
			return retValue;
	}

	private String getBasePathForEclipse() {
		final String configArea = System.getProperty("osgi.configuration.area");
		if (configArea == null || !configArea.startsWith("file:")) {
			return null;
		}
		final String trimmedPath = configArea.substring(5);
		int metadataIndex = trimmedPath.indexOf(".metadata");
		if (metadataIndex == -1)
			return null;
		return trimmedPath.substring(0, metadataIndex);
	}

	private boolean isRunningInEclipse() {
		return System.getProperty("osgi.dev") != null;
	}
	protected void setLogger(ILogger logger) {
		this.logger = logger;
	}
}
