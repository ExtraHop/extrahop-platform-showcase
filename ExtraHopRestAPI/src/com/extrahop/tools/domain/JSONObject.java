package com.extrahop.tools.domain;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This abstract class provides JSON related helper methods. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public abstract class JSONObject {
	
	@Override
	public String toString() {
		try {
			return (new com.fasterxml.jackson.databind.ObjectMapper()).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return e.getMessage();
		}
	}
	
	public String toPrettyPrintString(){
		try {
			return (new com.fasterxml.jackson.databind.ObjectMapper()).writerWithDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return e.getMessage();
		}
	}
	
	public static <T> T convertJsonToObject(String json,Class<? extends T> objClass) throws JsonParseException, JsonMappingException, IOException{
			return (new com.fasterxml.jackson.databind.ObjectMapper()).readValue(json, objClass);
	}

}
