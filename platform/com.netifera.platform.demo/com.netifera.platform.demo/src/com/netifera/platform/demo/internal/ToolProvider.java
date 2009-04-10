package com.netifera.platform.demo.internal;

import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolProvider;
import com.netifera.platform.demo.ExploitTestService;

public class ToolProvider implements IToolProvider {

	private final static String[] toolClassNames = { 
		ExploitTestService.class.getName()
	};
	
	public ITool createToolInstance(String className) {
		if(className.equals(ExploitTestService.class.getName())) {
			return new ExploitTestService();
		}
		return null;
	}

	public String[] getProvidedToolClassNames() {
		return toolClassNames;
	}
}
