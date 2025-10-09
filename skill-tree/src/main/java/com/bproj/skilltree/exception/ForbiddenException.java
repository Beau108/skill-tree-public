package com.bproj.skilltree.exception;

public class ForbiddenException extends RuntimeException {
	public ForbiddenException() {
		super("Could not find user by authenticated session.");
	}
	public ForbiddenException(String message) {
		super("Could not find user by authenticated session." + " : " + message);
	}
}
