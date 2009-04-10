package com.netifera.platform.host.filesystem;

import java.io.IOException;
import java.io.Serializable;


public class File implements Serializable {
	
	private static final long serialVersionUID = -5668415766862514374L;
	public static final int DIRECTORY = 1;
	public static final int FILE = 2;
	public static final int SYMBOLIC_LINK = 4;
	public static final int HIDDEN = 8;
	
	transient private IFileSystem fileSystem;
	private String path;
	final private int attributes;
	
	final private long length;
	final private long lastModified;

	public File(IFileSystem fileSystem, String path, int attributes, long length, long lastModified) {
		this.fileSystem = fileSystem;
		this.path = path;
		this.attributes = attributes;
		this.length = length;
		this.lastModified = lastModified;
	}
	
	public void setFileSystem(IFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}
	
	public IFileSystem getFileSystem() {
		return fileSystem;
	}
	
	public String getName() {
		return path.substring(path.lastIndexOf(fileSystem.getNameSeparator())+1);
	}
	
	public String getAbsolutePath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public boolean isDirectory() {
		return (attributes & DIRECTORY) != 0;
	}

	public boolean isFile() {
		return (attributes & FILE) != 0;
	}
	
	public boolean isSymbolicLink() {
		return (attributes & SYMBOLIC_LINK) != 0;
	}

	public boolean isHidden() {
		return (attributes & HIDDEN) != 0;
	}

	public long length() {
		return length;
	}
	
	public long lastModified() {
		return lastModified;
	}
	
	public File getParent() {
		int lastIndex = path.lastIndexOf(fileSystem.getNameSeparator());
		if (lastIndex <= 0)
			return null;
		return new File(fileSystem, path.substring(0, lastIndex), DIRECTORY, 0, 0);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof File))
			return false;
		File file = (File) o;
		return fileSystem == file.fileSystem && (path.equals(file.path));
	}
	
	public int hashCode() {
		return path.hashCode();
	}
	
	public String toString() {
		return path;
	}
	
	public boolean delete() throws IOException {
		if (isDirectory()) {
			return fileSystem.deleteDirectory(path);
		} else {
			return fileSystem.delete(path);
		}
	}
	
	public boolean renameTo(String newName) throws IOException {
		return fileSystem.rename(getAbsolutePath(), newName);
	}
}
