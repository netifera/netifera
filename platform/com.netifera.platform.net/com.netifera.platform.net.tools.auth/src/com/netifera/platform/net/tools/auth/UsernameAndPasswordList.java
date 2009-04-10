package com.netifera.platform.net.tools.auth;

import java.util.Iterator;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.iterables.RandomIterator;
import com.netifera.platform.api.iterables.SequentialIterator;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;

public class UsernameAndPasswordList implements IndexedIterable<UsernameAndPassword> {
	
	private static final long serialVersionUID = 1206032843239407999L;
	
	IndexedIterable<String> usernames;
	IndexedIterable<String> passwords;
	
	public UsernameAndPasswordList(IndexedIterable<String> usernames, IndexedIterable<String> passwords) {
		this.usernames = usernames;
		this.passwords = passwords;
	}
	
	public UsernameAndPassword itemAt(int index) {
		return new UsernameAndPassword(
				usernames.itemAt(index / passwords.itemCount()),
				passwords.itemAt(index % passwords.itemCount()));
	}

	public int itemCount() {
		return usernames.itemCount() * passwords.itemCount();
	}

	public Iterator<UsernameAndPassword> iterator() {
		return new SequentialIterator<UsernameAndPassword>(this);
	}

	public Iterator<UsernameAndPassword> randomIterator() {
		return new RandomIterator<UsernameAndPassword>(this);
	}
}
