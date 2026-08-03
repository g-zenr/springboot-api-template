package com.template.api.common.error;

public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}
}
