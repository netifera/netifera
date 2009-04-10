package com.netifera.platform.net.services.sniffing;

import java.util.List;

public interface ICredentialSnifferProvider {
	List<ICredentialSniffer> getSniffers();
}
