package com.netifera.platform.kernel.tools;

import java.io.Serializable;
import java.util.List;

import com.netifera.platform.api.tools.IOption;
import com.netifera.platform.api.tools.IToolConfiguration;
import com.netifera.platform.tools.options.Option;

public class ToolConfiguration implements IToolConfiguration, Serializable {
	private static final long serialVersionUID = -3475399402626119702L;
	private final List<IOption> options;

	public ToolConfiguration(List<IOption> options) {
		this.options = options;
	}

	public List<IOption> getOptions() {
		return options;
	}

	public Object get(String name) {
		for (IOption o: options) {
			if (o.getName().equals(name))
				return o.getValue();
		}
		return null;
	}
	
	public boolean isFixed() {
		for (IOption o: options)
			if ((o instanceof Option) && !((Option)o).isFixed())
				return false;
		return true;
	}
}
