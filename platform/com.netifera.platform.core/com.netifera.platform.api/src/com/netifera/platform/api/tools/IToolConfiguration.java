package com.netifera.platform.api.tools;

import java.util.List;

/**
 * Classes implementing this interface must also implement {@link
 * java.io.Serializable}.
 * 
 * @see com.netifera.platform.kernel.tools.ToolConfiguration
 */
public interface IToolConfiguration {
	List<IOption> getOptions();
	Object get(String name);
}
