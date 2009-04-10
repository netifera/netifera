package com.netifera.platform.tools.options;

import java.util.LinkedList;
import java.util.List;


public class EnableOption extends BooleanOption {

	public EnableOption(String name, String label, String description, boolean value) {
		super(name, label, description, value);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 369387450709128611L;
	
	List<Option> options;
	
	public void addOption(Option op) {
		if(options == null) {
			options = new LinkedList<Option>();
		}
		
		options.add(op);
	}

	public List<Option> getOptions() {
		return options;
	}
}
