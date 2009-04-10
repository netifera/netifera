package com.netifera.platform.tools.options;

import com.netifera.platform.api.tools.IParsableOption;

public class StringOption extends Option implements IParsableOption {
	private static final long serialVersionUID = -9151568925045179643L;
	
	String value;
	String defaultValue;
	boolean allowEmpty;

	public StringOption(final String name, final String label, final String description, final String value) {
		this(name, label, description, value, false);
	}

	public StringOption(final String name, final String label, final String description, final String value, final boolean allowEmpty) {
		super(name, label, description);
		this.value = value;
		this.defaultValue = value;
		this.allowEmpty = allowEmpty;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getDefault() {
		return defaultValue;
	}
	
	public void setDefault(final String s) {
		defaultValue = s;
	}
	
	public void setValue(final String s) {
		value = s; // FIXME can corrupt?
	}
	
	@Override
	public boolean isDefault() {
		return value.equals(defaultValue);
	}
	
	@Override
	public void setToDefault() {
		value = new String(defaultValue);
	}

	public boolean allowsEmptyValue() {
		return allowEmpty;
	}
	
	public boolean fromString(final String text) {
		value = text;
		return true;
	}
}
