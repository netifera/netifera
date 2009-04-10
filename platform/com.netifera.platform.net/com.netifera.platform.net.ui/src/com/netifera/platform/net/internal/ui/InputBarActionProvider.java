package com.netifera.platform.net.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.net.tools.basic.AddHost;
import com.netifera.platform.net.tools.basic.AddNetblock;
import com.netifera.platform.tools.options.GenericOption;
import com.netifera.platform.ui.actions.ToolAction;
import com.netifera.platform.ui.api.inputbar.IInputBarActionProvider;
import com.netifera.platform.util.addresses.AddressFormatException;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.InternetNetblock;
import com.netifera.platform.util.patternmatching.InternetAddressMatcher;
import com.netifera.platform.util.patternmatching.NetblockMatcher;

public class InputBarActionProvider implements IInputBarActionProvider {
	
	public List<IAction> getActions(final long realm, final long spaceId, String input) {
		List<IAction> answer = new ArrayList<IAction>();
		NetblockMatcher netblockMatcher = new NetblockMatcher(input);
		if (netblockMatcher.matches()) {
			InternetAddress address = InternetAddress.fromString(netblockMatcher.getNetwork());
			InternetNetblock netblock = address.createNetblock(netblockMatcher.getCIDR());
			ToolAction addNetblock = new ToolAction("Add netblock "+netblock, AddNetblock.class.getName());
			addNetblock.addFixedOption(new GenericOption(InternetNetblock.class, "netblock", "Netblock", "Netblock to add to the model", netblock));
			answer.add(addNetblock);
		} else {
			InternetAddress address = null;
			if (InternetAddressMatcher.matches(input)) {
				address = InternetAddress.fromString(input);
			} else if (input.matches("^(\\p{XDigit}+\\.)+i.*\\.arpa\\.?$")) {
				try {
					address = InternetAddress.fromARPA(input);
				} catch (AddressFormatException e) {
					// nothing
				}
			}
			if (address != null) {
				ToolAction addHost = new ToolAction("Add host "+address, AddHost.class.getName());
				addHost.addFixedOption(new GenericOption(InternetAddress.class, "address", "Address", "Address of the host to add to the model", address));
				answer.add(addHost);
			}
		}
		return answer;
	}
}
