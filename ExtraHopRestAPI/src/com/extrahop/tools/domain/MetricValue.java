package com.extrahop.tools.domain;

import java.util.Map;

/**
 * This class represents a JSON MetricValue object. This is a child of MetricData. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public class MetricValue extends JSONObject{
	
	private Map<String,Object>[] key;
	private String vtype;
	private String value;
	
	public Map<String,Object>[] getKey() {
		return key;
	}
	public void setKey(Map<String,Object>[] key) {
		this.key = key;
	}
	public String getVtype() {
		return vtype;
	}
	public void setVtype(String vtype) {
		this.vtype = vtype;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
