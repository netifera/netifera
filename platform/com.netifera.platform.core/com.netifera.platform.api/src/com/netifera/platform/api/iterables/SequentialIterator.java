package com.netifera.platform.api.iterables;

import java.util.Iterator;
import java.util.NoSuchElementException;



public class SequentialIterator<E> implements Iterator<E> {

	private final int count;
	private final IndexedIterable<E> base;
	private int current;
	
	public SequentialIterator(IndexedIterable<E> base) {
		this.count = base.itemCount();
		this.base = base;
		current = 0;
	}
	
	public boolean hasNext() {
		return current < count;		
	}

	public E next() {
		if(!hasNext()) {
			throw new NoSuchElementException();
		}
		return base.itemAt(current++);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
