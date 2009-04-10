package com.netifera.platform.tools.options;

import java.util.LinkedList;
import java.util.List;


public class EnumOption extends StringOption {

	public EnumOption(String name, String label, String description,
			String value) {
		super(name, label, description, value);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1646720157673179863L;
	
	List<String[]> options;
	
	public void addOption(String label, String value) {
		if(options == null) {
			options = new LinkedList<String[]>();
		}
		
		String[] op = new String[2];
		
		op[0] = label;
		op[1] = value;
		
		options.add(op);
		
		if(defaultValue == null) {
			defaultValue = value;
			setToDefault();
		}
	}
	
	private boolean isValid(String value) {
		for(String[] op : options) {
			if(op[1].equals(value)) {
				return true;
			}
		}
		return false;
	}
	@Override
	public void setValue(String val) {
		if(isValid(val)) {
			value = val;
		}
	}
	
	@Override
	public void setDefault(String val) {
		if(isValid(val)) {
			defaultValue = val;
		}		
	}
	
	public String[][] getOptions() {
		return options.toArray(new String[0][0]);
	}
}
