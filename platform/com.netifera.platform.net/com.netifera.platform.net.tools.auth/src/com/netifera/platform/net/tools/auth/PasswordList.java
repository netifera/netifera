package com.netifera.platform.net.tools.auth;

import java.util.Iterator;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.iterables.RandomIterator;
import com.netifera.platform.api.iterables.SequentialIterator;
import com.netifera.platform.net.services.credentials.Password;

public class PasswordList implements IndexedIterable<Password> {
	
	private static final long serialVersionUID = 7004761316748963152L;
	
	IndexedIterable<String> passwords;
	
	public PasswordList(IndexedIterable<String> passwords) {
		this.passwords = passwords;
	}
	
	public Password itemAt(int index) {
		return new Password(passwords.itemAt(index));
	}

	public int itemCount() {
		return passwords.itemCount();
	}

	public Iterator<Password> iterator() {
		return new SequentialIterator<Password>(this);
	}

	public Iterator<Password> randomIterator() {
		return new RandomIterator<Password>(this);
	}
}
