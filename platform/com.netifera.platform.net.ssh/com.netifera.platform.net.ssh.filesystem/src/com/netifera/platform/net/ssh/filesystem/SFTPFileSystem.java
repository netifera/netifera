package com.netifera.platform.net.ssh.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.netifera.platform.host.filesystem.File;
import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.host.filesystem.IFileSystemListener;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.net.services.ssh.SSH;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;

public class SFTPFileSystem implements IFileSystem {

	private SSH ssh;
	private Credential credential;

	private List<IFileSystemListener> listeners = new ArrayList<IFileSystemListener>();

	public SFTPFileSystem(SSH ssh, UsernameAndPassword credential) {
		this.ssh = ssh;
		this.credential = credential;
	}
	
	public File createDirectory(String directoryName) throws IOException {
		SFTPv3Client client = new SFTPv3Client(ssh.createConnection(credential));
		try {
			client.mkdir(directoryName, 0775);
			File file = new File(this, directoryName, File.DIRECTORY, 0, 0);
			for (IFileSystemListener listener: listeners)
				listener.added(file);
			return file;
		} finally {
			client.close();
		}
	}

	public boolean delete(String fileName) throws IOException {
		SFTPv3Client client = new SFTPv3Client(ssh.createConnection(credential));
		try {
			client.rm(fileName);
			File file = new File(this, fileName, File.FILE, 0, 0);
			for (IFileSystemListener listener: listeners)
				listener.removed(file);
			return true;
		} finally {
			client.close();
		}
//		return false;
	}

	public boolean deleteDirectory(String directoryName) throws IOException {
		SFTPv3Client client = new SFTPv3Client(ssh.createConnection(credential));
		try {
			client.rmdir(directoryName);
			File file = new File(this, directoryName, File.DIRECTORY, 0, 0);
			for (IFileSystemListener listener: listeners)
				listener.removed(file);
			return true;
		} finally {
			client.close();
		}
//		return false;
	}

	public boolean rename(String oldName, String newName) throws IOException {
		SFTPv3Client client = new SFTPv3Client(ssh.createConnection(credential));
		try {
			client.mv(oldName, newName);
			File oldFile = new File(this, oldName, File.FILE, 0, 0);
			File newFile = new File(this, newName, File.FILE, 0, 0);
			for (IFileSystemListener listener: listeners) {
				listener.removed(oldFile);
				listener.added(newFile);
			}
			return true;
		} finally {
			client.close();
		}
//		return false;
	}

	private File convert(String directoryPath, SFTPv3DirectoryEntry sftpFile) {
		int attributes = 0;
		if (sftpFile.attributes.isDirectory())
			attributes |= File.DIRECTORY;
		if (sftpFile.attributes.isRegularFile())
			attributes |= File.FILE;
		if (!directoryPath.endsWith("/"))
			directoryPath += "/";
		String fullPath = directoryPath+sftpFile.filename;
		if (fullPath.endsWith("/"))
			fullPath = fullPath.substring(0, fullPath.length()-1);
		return new File(this, fullPath, attributes, sftpFile.attributes.size, sftpFile.attributes.mtime);
	}

	private File[] convert(String directoryPath, Vector<SFTPv3DirectoryEntry> sftpFiles) {
		List<File> files = new ArrayList<File>();
		for (SFTPv3DirectoryEntry sftpFile: sftpFiles)
			if (!sftpFile.filename.equals("..") && !sftpFile.filename.equals("."))
				files.add(convert(directoryPath, sftpFile));
		return files.toArray(new File[files.size()]);
	}

	public File[] getDirectoryList(String directoryName) throws IOException {
		SFTPv3Client client = new SFTPv3Client(ssh.createConnection(credential));
		try {
			return convert(directoryName, client.ls(directoryName));
		} finally {
			client.close();
		}
	}

	public String getNameSeparator() {
		return "/";
	}

	public InputStream getInputStream(String fileName) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public OutputStream getOutputStream(String fileName) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public File[] getRoots() {
		return new File[] {new File(this, "/", File.DIRECTORY, 0, 0)};
	}

	public void addListener(IFileSystemListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IFileSystemListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public String toString() {
		return ssh.toString();
	}
}
