package com.bproj.skilltree.exception;

import java.util.Map;

public class NotFoundException extends RuntimeException {
	private final Map<String, String> query;
	public NotFoundException(Map<String, String> query) {
		super("Could not find element");
		this.query = query;
	}
	public Map<String, String> getQuery() { return query; }
}
