package com.netifera.platform.api.iterables;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;




public class RandomIterator<E> implements Iterator<E> {
	private final IndexedIterable<E> base;
	private Random random;
	private BitSet history;
	private int countRemaining;
	
	public RandomIterator(IndexedIterable<E> base) {
		this.base = base;
		history = new BitSet(base.itemCount());
		random = new Random();
		countRemaining = base.itemCount();
	}
	
	public boolean hasNext() {
		return countRemaining > 0;
	}
	
	public E next() {
		if (!hasNext()) throw new NoSuchElementException();
		int index = 0;
		for (int r=0; r<8; r++) {
			index = random.nextInt(base.itemCount());
			if (!history.get(index)) break;
		}
		index = history.nextClearBit(index);
		if (index >= base.itemCount())
			index = history.nextClearBit(0);
		history.set(index);
		countRemaining--;
		return base.itemAt(index);
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
