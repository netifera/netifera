package com.netifera.platform.tools;

import com.netifera.platform.api.tools.ToolException;

public class RequiredOptionMissingException extends ToolException {
	
	private static final long serialVersionUID = -8948150808912374250L;
	
	private String optionName;
	
	public RequiredOptionMissingException(String option) {
		super("Required '"+option+"' option missing");
		optionName = option;
	}
	
	public String getOptionName() {
		return optionName;
	}
}
