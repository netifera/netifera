package com.netifera.platform.api.tools;

import com.netifera.platform.api.tasks.TaskException;

public class ToolException extends TaskException {
	private static final long serialVersionUID = -7107844800374491479L;

	public ToolException() {
		super();
	}

	public ToolException(final String message) {
		super(message);
	}
	
	public ToolException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
