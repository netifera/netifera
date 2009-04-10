package com.netifera.platform.tools.options;

import com.netifera.platform.api.tools.IParsableOption;

public class BooleanOption extends Option implements IParsableOption {
	private static final long serialVersionUID = 3379708339337531970L;
	boolean value;
	boolean defaultValue;
	
	public BooleanOption(final String name, final String label, final String description, final boolean value) {
		super(name, label, description);
		this.value = value;
		this.defaultValue = value;
	}
	
	public Boolean getValue() {
		return Boolean.valueOf(value);
	}
	
	public boolean getDefault() {
		return defaultValue;
	}
	
	public void setDefault(final boolean b) {
		defaultValue = b;
	}
	public void setValue(final boolean b) {
		value = b;
	}
	
	@Override
	public boolean isDefault() {
		return value == defaultValue;
	}
	
	@Override
	public void setToDefault() {
		value = defaultValue;
	}
	
	public boolean fromString(final String text) {
		if (text.compareToIgnoreCase("true") == 0) {
			value = true;
			return true;
		} else if (text.compareToIgnoreCase("false") == 0) {
			value = false;
			return true;
		}
		return false;
	}
}
