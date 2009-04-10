package com.netifera.platform.util.addresses;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/* to use with unix sockets
 * man  unix(7)
 */
public class UnixPath implements IPathAddress, Serializable, //Cloneable,
		Comparable<UnixPath> {

	private static final long serialVersionUID = 7935508823343724657L;

	/* this is not PATH_MAX from <limits.h> */
	// XXX?
	public static final int UNIX_PATH_MAX = 1024; //ZystemConstants.get("UNIX_PATH_MAX");

	private final String path;

	public UnixPath(String path) {
		if (path.length() > UNIX_PATH_MAX)
			throw new IllegalArgumentException("Path too long (" +
					path.length() + "chars).");
		this.path = path;
	}
	
	public int getDataSize() {
		return UNIX_PATH_MAX * 8; /* an ASCII character holds 8 bits */
	}
	
	public byte[] toBytes() {
		try {
			return path.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// XXX what happening here?
			e.printStackTrace();
			return new byte[0];
		}
	}

	@Override
	public String toString() {
		return path;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UnixPath) {
			return path.equals(((UnixPath)obj).path);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	public int compareTo(UnixPath other) {
		return path.compareTo(other.path);
	}
}
