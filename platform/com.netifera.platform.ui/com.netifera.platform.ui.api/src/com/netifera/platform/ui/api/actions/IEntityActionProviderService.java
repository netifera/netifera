package com.netifera.platform.ui.api.actions;

import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.model.IShadowEntity;

public interface IEntityActionProviderService {
	List<IAction> getActions(IShadowEntity shadow);
	List<IAction> getQuickActions(IShadowEntity shadow);
}
