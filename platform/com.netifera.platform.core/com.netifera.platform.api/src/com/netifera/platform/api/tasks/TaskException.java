package com.netifera.platform.api.tasks;

public class TaskException extends Exception {
	private static final long serialVersionUID = -5355009580531123517L;

	public TaskException() {
		super();
	}

	public TaskException(final String message) {
		super(message);
	}
	
	public TaskException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
