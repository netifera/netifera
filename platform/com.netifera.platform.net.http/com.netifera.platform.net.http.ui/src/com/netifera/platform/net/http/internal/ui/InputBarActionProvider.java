package com.netifera.platform.net.http.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.net.http.tools.AddWebSite;
import com.netifera.platform.tools.options.StringOption;
import com.netifera.platform.ui.actions.ToolAction;
import com.netifera.platform.ui.api.inputbar.IInputBarActionProvider;
import com.netifera.platform.util.patternmatching.HttpUrlMatcher;

public class InputBarActionProvider implements IInputBarActionProvider {

	public List<IAction> getActions(final long realm, final long view, final String input) {
		List<IAction> answer = new ArrayList<IAction>();
		final HttpUrlMatcher matcher = new HttpUrlMatcher(input);
		if (matcher.matches()) {
			ToolAction addWebSite = new ToolAction("Add web site "+input, AddWebSite.class.getName());
			addWebSite.addFixedOption(new StringOption("url", "URL", "Web site URL to add to the model", input));
			answer.add(addWebSite);
		}
		return answer;
	}
}
