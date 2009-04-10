package com.netifera.platform.host.filesystem;

public interface IFileSystemListener {
	void added(File file);
	void removed(File file);
	void update(File file);
}
