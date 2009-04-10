package com.netifera.platform.tools.options;

import com.netifera.platform.api.tools.IParsableOption;

public class IntegerOption extends Option implements IParsableOption {
	private static final long serialVersionUID = -390336028552394422L;
	
	Integer value;
	Integer defaultValue;
	Integer maximumValue;
	
	public IntegerOption(String name, String label, String description, Integer value, Integer maximum) {
		super(name,label,description);
		this.value = value;
		this.defaultValue = value;
		this.maximumValue = maximum;
	}
	public IntegerOption(final String name, final String label, final String description, final Integer value) {
		super(name, label, description);
		this.value = value;
		this.defaultValue = value;
		this.maximumValue = null;
	}
	
	public Integer getValue() {
		return value;
	}
	
	public Integer getDefault() {
		return defaultValue;
	}
	
	public boolean hasMaximumValue() {
		return maximumValue != null;
	}
	
	public Integer getMaximumValue() {
		return maximumValue;
	}
	
	public void setDefault(final int number) {
		defaultValue = Integer.valueOf(number);
	}
	
	public void setValue(final int number) {
		value = Integer.valueOf(number);
	}
	
	@Override
	public boolean isDefault() {
		return value.equals(defaultValue);
	}
	
	@Override
	public void setToDefault() {
		value = defaultValue;
	}
	
	public boolean fromString(final String text) {
		try {
			value = Integer.decode(text);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
}
