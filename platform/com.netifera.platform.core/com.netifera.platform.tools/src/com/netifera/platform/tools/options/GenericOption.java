package com.netifera.platform.tools.options;

import java.io.Serializable;


public class GenericOption extends Option {

	private static final long serialVersionUID = -4949284024082224859L;

	private Class<?> type;
	private Serializable defaultValue, value;
	
	public GenericOption(Class<?> type, String name, String label, String description, Serializable value) {
		super(name, label, description);
		this.type = type;
		this.value = value;
		this.defaultValue = value;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	@Override
	public boolean isDefault() {
		return value == defaultValue;
	}

	@Override
	public void setToDefault() {
		value = defaultValue;
	}
	
	public Serializable getValue() {
		return value;
	}
	
	public void setValue(Serializable value) {
		this.value = value;
	}
	
	public void setDefault(Serializable value) {
		this.defaultValue = value;
	}
}
