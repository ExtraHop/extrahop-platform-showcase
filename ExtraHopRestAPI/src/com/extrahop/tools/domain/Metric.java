package com.extrahop.tools.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a JSON metric object for querying metrics. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public class Metric extends JSONObject{
	
	public final static String CYCLE_AUTO="auto";
	public final static String CYCLE_30SEC="30sec";
	public final static String CYCLE_5MIN="5min";
	public final static String CYCLE_1HR="1hr";
	public final static String CYCLE_24HR="24hr";
	
	private String cycle="auto";
	private String object_type;
	private int[] object_ids;
	private String metric_category;
	private Map<String,Object> metric_specs = new HashMap<String,Object>();
	private long from;
	private long until;
	

	public Metric() {
		super();
	}


	public String getCycle() {
		return cycle;
	}


	public void setCycle(String cycle) {
		this.cycle = cycle;
	}


	public String getObject_type() {
		return object_type;
	}


	public void setObject_type(String object_type) {
		this.object_type = object_type;
	}


	public int[] getObject_ids() {
		return object_ids;
	}


	public void setObject_ids(int[] object_ids) {
		this.object_ids = object_ids;
	}


	public String getMetric_category() {
		return metric_category;
	}


	public void setMetric_category(String metric_category) {
		this.metric_category = metric_category;
	}


	public List<Map<String,Object>> getMetric_specs() {
		List<Map<String,Object>> maps = new ArrayList<Map<String,Object>>();
		maps.add(metric_specs);
		return maps;
	}


	public void setMetric_specs(Map<String,Object> metric_specs) {
		this.metric_specs = metric_specs;
	}
	
	public void addSpec(String key,Object value){
		this.metric_specs.put(key, value);
	}


	public long getFrom() {
		return from;
	}


	public void setFrom(long from) {
		this.from = from;
	}


	public long getUntil() {
		return until;
	}


	public void setUntil(long until) {
		this.until = until;
	}

}
