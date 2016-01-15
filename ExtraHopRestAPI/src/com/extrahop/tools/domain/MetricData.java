package com.extrahop.tools.domain;

/**
 * This class represents a JSON MetricData object. This is a child of MetricStats. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public class MetricData extends JSONObject{
	
	private long oid;
	private long time;
	private long duration;
	private Object[] values;

	public MetricData() {
		super();
	}

	public long getOid() {
		return oid;
	}

	public void setOid(long oid) {
		this.oid = oid;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}
}
