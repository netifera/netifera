package com.netifera.platform.host.filesystem.probe;

import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.host.filesystem.IFileSystemFactory;

public class FileSystemFactory implements IFileSystemFactory {

	private ILogger logger;
	private Map<IProbe, RemoteFileSystem> probeMap =
		new HashMap<IProbe, RemoteFileSystem>();
	
	
	public IFileSystem createForProbe(IProbe probe) {
		if(probeMap.containsKey(probe)) {
			return probeMap.get(probe);
		}
		final RemoteFileSystem rfs = new RemoteFileSystem(probe, logger);
		probeMap.put(probe, rfs);
		return rfs;
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("File System");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
}
