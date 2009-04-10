package com.netifera.platform.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.ui.api.inputbar.IInputBarActionProvider;
import com.netifera.platform.ui.api.inputbar.IInputBarActionProviderService;

public class InputBarActionProviderService implements IInputBarActionProviderService {
	final private List<IInputBarActionProvider> providers =
		new ArrayList<IInputBarActionProvider>();
	
	protected void registerProvider(IInputBarActionProvider provider) {
		providers.add(provider);
	}

	protected void unregisterProvider(IInputBarActionProvider provider) {
		providers.remove(provider);
	}

	public List<IAction> getActions(long realm, long space, String input) {
		List<IAction> answer = new ArrayList<IAction>();
		String trimmedInput = input.trim();
		for (IInputBarActionProvider provider: providers)
			try {
				answer.addAll(provider.getActions(realm, space, trimmedInput));
			} catch (Throwable exception) {
				logger.error("InputBar: " + input, exception);
			}
		return answer;
	}
	
	/* logging */

	private ILogger logger;
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("InputBar Action Provider");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
}
