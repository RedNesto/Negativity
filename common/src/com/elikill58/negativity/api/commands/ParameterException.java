package com.elikill58.negativity.api.commands;

public class ParameterException extends RuntimeException {
	
	public ParameterException() {
	}
	
	public ParameterException(String message) {
		super(message);
	}
	
	public ParameterException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ParameterException(Throwable cause) {
		super(cause);
	}
}
