package com.netifera.platform.api.tools;

public interface IToolProvider {
	String[] getProvidedToolClassNames();
	
	/* return null if tool does not exist */
	ITool createToolInstance(String className);
}
