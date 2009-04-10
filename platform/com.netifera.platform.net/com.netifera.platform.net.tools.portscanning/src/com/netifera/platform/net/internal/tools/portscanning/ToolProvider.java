package com.netifera.platform.net.internal.tools.portscanning;

import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolProvider;
import com.netifera.platform.net.tools.basic.AddHost;
import com.netifera.platform.net.tools.basic.AddNetblock;
import com.netifera.platform.net.tools.portscanning.TCPConnectScanner;
import com.netifera.platform.net.tools.portscanning.UDPScanner;

public class ToolProvider implements IToolProvider {

	private final static String[] toolClassNames = { 
		AddHost.class.getName(),
		AddNetblock.class.getName(),
		TCPConnectScanner.class.getName(),
		UDPScanner.class.getName()
	};
	
	public ITool createToolInstance(String className) {
		if(className.equals(AddHost.class.getName())) {
			return new AddHost();
		} else if(className.equals(AddNetblock.class.getName())) {
			return new AddNetblock();
		} else if(className.equals(TCPConnectScanner.class.getName())) {
			return new TCPConnectScanner();
		} else if(className.equals(UDPScanner.class.getName())) {
			return new UDPScanner();
		}
		return null;
	}

	public String[] getProvidedToolClassNames() {
		return toolClassNames;
	}
}
