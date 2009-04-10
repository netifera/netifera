package com.netifera.platform.net.internal.tools.auth;

import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolProvider;
import com.netifera.platform.net.tools.auth.FTPAuthBruteforcer;
import com.netifera.platform.net.tools.auth.IMAPAuthBruteforcer;
import com.netifera.platform.net.tools.auth.POP3AuthBruteforcer;

public class ToolProvider implements IToolProvider {

	private final static String[] toolClassNames = { 
		FTPAuthBruteforcer.class.getName(),
		POP3AuthBruteforcer.class.getName(),
		IMAPAuthBruteforcer.class.getName()
	};
	
	public ITool createToolInstance(String className) {
		if(className.equals(FTPAuthBruteforcer.class.getName()))
			return new FTPAuthBruteforcer();
		if(className.equals(POP3AuthBruteforcer.class.getName()))
			return new POP3AuthBruteforcer();
		if(className.equals(IMAPAuthBruteforcer.class.getName()))
			return new IMAPAuthBruteforcer();
		return null;
	}

	public String[] getProvidedToolClassNames() {
		return toolClassNames;
	}
}
