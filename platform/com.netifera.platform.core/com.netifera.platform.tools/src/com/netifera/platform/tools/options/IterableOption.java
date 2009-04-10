package com.netifera.platform.tools.options;

import java.util.Iterator;

public class IterableOption extends Option {
	private static final long serialVersionUID = 8033466926304681495L;
	
	private Iterable<?> value;
	
	/* This is the type of object that this option will iterate over. */
	private Class<?> iterableType;
		
	public IterableOption(Class<?> iterableType, String name, String label, String description, Iterable<?> value) {
		super(name, label, description);
		this.iterableType = iterableType;
		this.value = value;
	}

	@Override
	public boolean isDefault() {
		return value == null;
	}

	@Override
	public void setToDefault() {
		value = null;
	}

	public Class<?> getIterableType() {
		return iterableType;
	}
	
	public Iterable<?> getValue() {
		return value;
	}
	
	public Iterator<?> getIterator() {
		return value.iterator();
	}
	
	public void setValue(Iterable<?> iterable) {
		this.value = iterable;
	}
}
