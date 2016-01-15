package com.extrahop.tools.rest;

/**
 * Wraps an exception thrown from the RestClient.
 * @author jeffbfry
 *
 */
@SuppressWarnings("serial")
public class RestException extends Exception {

	private String uri;
	private int statusCode=0;
	
	public RestException(String uri,int statusCode,String message) {
		super(message);
		this.uri=uri;
		this.statusCode=statusCode;
	}
	
	public RestException(String uri,String message) {
		super(message);
		this.uri=uri;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
}
