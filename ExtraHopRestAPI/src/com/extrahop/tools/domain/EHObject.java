package com.extrahop.tools.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This abstract class provides some common properties and methods for other classes. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public abstract class EHObject extends JSONObject {
	/**
	 * JSON serialization:id,mod_time are retrieved from the server, but never sent.
	 */
	@JsonIgnore
	protected int id;
	@JsonIgnore
	protected long mod_time;
	protected String name="";
	
	public EHObject() {
		id=-1;
		mod_time = System.currentTimeMillis();
	}

	@JsonIgnore
	public int getId() {
		return id;
	}
	@JsonProperty
	public void setId(int id) {
		this.id = id;
	}

	@JsonIgnore
	public long getMod_time() {
		return mod_time;
	}
	@JsonProperty
	public void setMod_time(long mod_time) {
		this.mod_time = mod_time;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
