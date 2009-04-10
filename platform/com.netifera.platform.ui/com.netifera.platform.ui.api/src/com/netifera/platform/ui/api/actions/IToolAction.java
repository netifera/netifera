package com.netifera.platform.ui.api.actions;

import java.util.List;

public interface IToolAction extends ISpaceAction {
	List<String> getBundleDependencies();
//	String getBundleName();
	String getClassName();
}
