package com.netifera.platform.host.filesystem;

import com.netifera.platform.api.probe.IProbe;

public interface IFileSystemFactory {
	IFileSystem createForProbe(IProbe probe);
}
