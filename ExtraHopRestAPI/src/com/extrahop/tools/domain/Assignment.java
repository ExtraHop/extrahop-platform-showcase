package com.extrahop.tools.domain;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This class represents a JSON assignment object that is used to assign/unassigns devices and device groups. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public class Assignment {

	private int[] assign = {};
	private int[] unassign = {};
	
	public int[] getAssign() {
		return assign;
	}
	public void setAssign(int[] assign) {
		Arrays.sort(assign);
		this.assign = assign;
	}
	public int[] getUnassign() {
		return unassign;
	}
	public void setUnassign(int[] unassign) {
		Arrays.sort(unassign);
		this.unassign = unassign;
	}
	
	@Override
	public String toString() {
		try {
			return (new com.fasterxml.jackson.databind.ObjectMapper()).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return e.getMessage();
		}
	}
}
