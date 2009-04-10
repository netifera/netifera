package com.netifera.platform.ui.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;
import com.netifera.platform.ui.api.actions.IEntityActionProviderService;

public class EntityActionProviderService implements IEntityActionProviderService {
	private final List<IEntityActionProvider> providers =
		new LinkedList<IEntityActionProvider>();

	protected void registerProvider(IEntityActionProvider provider) {
		providers.add(provider);
	}

	protected void unregisterProvider(IEntityActionProvider provider) {
		providers.remove(provider);
	}

	public List<IAction> getActions(IShadowEntity shadow) {
		List<IAction> answer = new ArrayList<IAction>();
		for (IEntityActionProvider provider: providers)
			try {
				List<IAction> actions = provider.getActions(shadow);
				if (actions != null)
					answer.addAll(actions);
			} catch (Throwable exception) {
				logger.error("provider:" + provider + ", entity:" + shadow,
						exception);
			}
		return answer;
	}
	
	public List<IAction> getQuickActions(IShadowEntity shadow) {
		List<IAction> answer = new ArrayList<IAction>();
		for (IEntityActionProvider provider: providers)
			try {
				List<IAction> actions = provider.getQuickActions(shadow);
				if (actions != null)
					answer.addAll(actions);
			} catch (Throwable exception) {
				logger.error("provider:" + provider + ", entity:" + shadow,
						exception);
			}
		return answer;
	}
	
	/* logging */

	private ILogger logger;
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Entity Action Provider");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
}
