package com.netifera.platform.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.tools.IOption;
import com.netifera.platform.api.tools.IToolConfiguration;
import com.netifera.platform.kernel.tools.ToolConfiguration;
import com.netifera.platform.tools.options.Option;
import com.netifera.platform.tools.options.OptionClass;
import com.netifera.platform.ui.api.actions.ISpaceAction;

public class SpaceAction extends Action implements ISpaceAction {
	private final List<IOption> options = new ArrayList<IOption>();
	private ISpace space;
	
	public SpaceAction(String name) {
		super(name);
	}
	
	@Override
	public String toString() {
		return getText();
	}
	
	public void setSpace(ISpace space) {
		this.space = space;
	}
	
	public ISpace getSpace() {
		return space;
	}

	public void run(ISpace space) {
		setSpace(space);
		run();
	}

	public IToolConfiguration getConfiguration() {
		return new ToolConfiguration(options);
	}
	
	public void addOption(Option p) {
		if(p instanceof OptionClass) {
			for(Option o : ((OptionClass)p).getOptions()) {
				addOption(o);
			}
			return;
		}
		
		options.add(p);
	}
	
	public void addFixedOption(Option o) {
		o.fix();
		addOption(o);
	}
}
