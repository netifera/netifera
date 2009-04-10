package com.netifera.platform.api.tools;

/**
 * This interface allow an <code>IOption</code> object to be set parsing
 * a string.
 * <p>
 * See the description on {@link IOption}.
 * </p>
 * @author james
 */
public interface IParsableOption extends IOption {
	/**
	 * Set the option value parsing a string.
	 * @param text The text to parse.
	 * @return <tt>true</tt> if the text could be parsed, <tt>false</tt> else.
	 */
	boolean fromString(String text);
}
