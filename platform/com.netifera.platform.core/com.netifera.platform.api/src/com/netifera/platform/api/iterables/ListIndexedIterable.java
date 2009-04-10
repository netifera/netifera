package com.netifera.platform.api.iterables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ListIndexedIterable<E> implements IndexedIterable<E> {

	private static final long serialVersionUID = -4005175960188711993L;
	
	private final List<E> list;
	
	public ListIndexedIterable(List<E> list) {
		this.list = list;
	}

	public ListIndexedIterable(E element) {
		list = new ArrayList<E>();
		list.add(element);
	}

	public E itemAt(int index) {
		return list.get(index);
	}

	public int itemCount() {
		return list.size();
	}

	public Iterator<E> iterator() {
		return new SequentialIterator<E>(this);
	}
	
	@Override
    public String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator<E> iterator = iterator();
		boolean isFirst = true;
		while (iterator.hasNext()) {
			if (!isFirst) buffer.append(", ");
			buffer.append(iterator.next());
			if (buffer.length() > 20 && iterator.hasNext()) {
				buffer.append(".. ("+itemCount()+")");
				break;
			}
			isFirst = false;
		}
		return buffer.toString();
	}
}
