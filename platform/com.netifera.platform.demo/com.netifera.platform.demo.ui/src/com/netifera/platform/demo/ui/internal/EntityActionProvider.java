package com.netifera.platform.demo.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.iterables.ListIndexedIterable;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.demo.ExploitTestService;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.tools.options.IterableOption;
import com.netifera.platform.tools.options.StringOption;
import com.netifera.platform.ui.actions.ToolAction;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.TCPSocketLocator;

public class EntityActionProvider implements IEntityActionProvider {
	
	public List<IAction> getActions(IShadowEntity entity) {
		List<IAction> answer = new ArrayList<IAction>();

		if (entity instanceof ServiceEntity) {
			ServiceEntity serviceEntity = (ServiceEntity) entity;
			if (serviceEntity.getServiceType().equals("TEST")) {
				TCPSocketLocator tcpLocator = (TCPSocketLocator) entity.getAdapter(TCPSocketLocator.class);
				if (tcpLocator != null) {
					ListIndexedIterable<InternetAddress> addresses = new ListIndexedIterable<InternetAddress>(tcpLocator.getAddress());
					assert addresses.itemAt(0).isUniCast();
					ToolAction exploit = new ToolAction("Exploit Test Service At "+tcpLocator, ExploitTestService.class.getName());
					exploit.addFixedOption(new IterableOption(InternetAddress.class, "target", "Target", "Target addresses", addresses));
					exploit.addFixedOption(new StringOption("port", "Port", "Ports to exploit", ((Integer)tcpLocator.getPort()).toString()));
					answer.add(exploit);
				}
			}
		}
		return answer;
	}

	public List<IAction> getQuickActions(IShadowEntity entity) {
		return Collections.emptyList();
	}
}
