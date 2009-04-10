package com.netifera.platform.tools.options;

import java.util.LinkedList;
import java.util.List;

public class OptionClass extends Option {

	private static final long serialVersionUID = -5985258092173565765L;
	
	List<Option> options;
	
	public void addOption(Option option) {
		if(options == null) {
			options = new LinkedList<Option>();
		}
		
		options.add(option);
	}
	
	public List<Option> getOptions() {
		return options;
	}
	
	public Object getValue() {
		return null;
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	@Override
	public void setToDefault() {
	}
}
