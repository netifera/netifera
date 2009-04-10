package com.netifera.platform.host.filesystem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalFileSystem implements IFileSystem {
	private List<IFileSystemListener> listeners = new ArrayList<IFileSystemListener>();
	
	public String getNameSeparator() {
		return java.io.File.separator;
	}

	public File[] getRoots() {
		return convert(java.io.File.listRoots());
	}

	public File[] getDirectoryList(String directoryName) {
		return convert((new java.io.File(directoryName)).listFiles());
	}
	
	private File convert(java.io.File javaFile) {
		int attributes = 0;
		if (javaFile.isDirectory())
			attributes |= File.DIRECTORY;
		if (javaFile.isFile())
			attributes |= File.FILE;
		if (javaFile.isHidden())
			attributes |= File.HIDDEN;
		return new File(this, javaFile.getAbsolutePath(), attributes, javaFile.length(), javaFile.lastModified());
	}

	private File[] convert(java.io.File[] javaFiles) {
		if (javaFiles == null) return null;
		List<File> files = new ArrayList<File>();
		for (java.io.File javaFile: javaFiles)
			files.add(convert(javaFile));
		return files.toArray(new File[files.size()]);
	}

	public InputStream getInputStream(String fileName) throws FileNotFoundException {
		return new FileInputStream(fileName);
	}

	public OutputStream getOutputStream(String fileName) throws FileNotFoundException {
		return new FileOutputStream(fileName);
	}

	public boolean delete(String path) {
		java.io.File file = new java.io.File(path);
		if (file.delete()) {
			File deletedFile = convert(file);
			for (IFileSystemListener listener: listeners)
				listener.removed(deletedFile);
			return true;
		}
		return false;
	}

	public boolean deleteDirectory(String path) {
		return false;
	}

	public File createDirectory(String path) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean rename(String oldName, String newName) {
		java.io.File oldFile = new java.io.File(oldName);
		java.io.File newFile = new java.io.File(newName);
		return oldFile.renameTo(newFile);
	}

	public void addListener(IFileSystemListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IFileSystemListener listener) {
		listeners.remove(listener);
	}
	
	public String toString() {
		return "Local File System";
	}
}
