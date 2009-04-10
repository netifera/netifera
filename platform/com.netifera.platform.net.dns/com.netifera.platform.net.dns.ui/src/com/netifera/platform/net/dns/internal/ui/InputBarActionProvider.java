package com.netifera.platform.net.dns.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.net.dns.service.DNS;
import com.netifera.platform.net.dns.tools.AddDomain;
import com.netifera.platform.net.dns.tools.AddEmailAddress;
import com.netifera.platform.net.dns.tools.AddHostByName;
import com.netifera.platform.tools.options.GenericOption;
import com.netifera.platform.tools.options.StringOption;
import com.netifera.platform.ui.actions.ToolAction;
import com.netifera.platform.ui.api.inputbar.IInputBarActionProvider;
import com.netifera.platform.util.patternmatching.DomainMatcher;
import com.netifera.platform.util.patternmatching.EmailMatcher;
import com.netifera.platform.util.patternmatching.HostnameMatcher;

public class InputBarActionProvider implements IInputBarActionProvider {

	public List<IAction> getActions(final long realm, final long spaceId, final String input) {
		List<IAction> answer = new ArrayList<IAction>();
		if (EmailMatcher.matches(input)) {
			ToolAction addEmailAddress = new ToolAction("Add email address "+input, AddEmailAddress.class.getName());
			addEmailAddress.addFixedOption(new StringOption("address", "Address", "Email address to add to model", input));
			answer.add(addEmailAddress);
		}
		
		if (input.matches("^.*\\.arpa\\.?$")) {
			/*
			 * .arpa are textual representation of IP address and do not need to
			 * be resolved, they are processed as IP addresses in the .net.ui
			 * plugin.
			 */
		} else if (HostnameMatcher.matches(input)) {
			/*
			 * first try hostnameMatcher, since if it matches the domain will be
			 * also created. 
			 */
			ToolAction nameDiscoverer = new ToolAction("Resolve name "+input, AddHostByName.class.getName());
			nameDiscoverer.addFixedOption(new StringOption("name", "Name", "Target host or domain name", input));
			answer.add(nameDiscoverer);
		} else if (DomainMatcher.matches(input)) {
			String domain = input.substring(1);
			ToolAction domainDiscovery = new ToolAction("Add domain "+input, AddDomain.class.getName());
			domainDiscovery.addFixedOption(new StringOption("domain", "Domain", "Target domain", domain));
			domainDiscovery.addOption(new GenericOption(DNS.class, "dns", "Name Server", "Target Name Server", null));
			answer.add(domainDiscovery);
		}
		return answer;
	}
}
