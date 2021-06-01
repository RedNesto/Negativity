package com.elikill58.negativity.api.commands;

public class InvalidCommandException extends RuntimeException {
	
	public InvalidCommandException() {
		super();
	}
	
	public InvalidCommandException(String message) {
		super(message);
	}
	
	public InvalidCommandException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidCommandException(Throwable cause) {
		super(cause);
	}
	
	protected InvalidCommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
