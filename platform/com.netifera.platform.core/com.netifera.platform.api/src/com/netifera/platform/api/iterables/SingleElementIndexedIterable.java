package com.netifera.platform.api.iterables;

import java.util.Iterator;

public class SingleElementIndexedIterable<E> implements IndexedIterable<E> {
	
	private static final long serialVersionUID = -7056543270998893217L;
	
	private E element;
	
	public SingleElementIndexedIterable(E element) {
		this.element = element;
	}
	
	public E itemAt(int index) {
		if (index == 0) return element;
		throw new IndexOutOfBoundsException();
	}
	
	public int itemCount() {
		return 1;
	}
	
	public Iterator<E> iterator() {
		return new SequentialIterator<E>(this);
	}
	
	public String toString() {
		return element.toString();
	}
}
