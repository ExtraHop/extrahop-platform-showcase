package com.extrahop.tools.domain;

/**
 * This class represents a JSON MetricStats object. This class contains metric results. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public class MetricStats extends JSONObject {
	
	private MetricData[] stats;
	private String cycle;
	private long node_id;
	private long from;
	private long until;
	
	public MetricStats() {
		super();
	}

	public MetricData[] getStats() {
		return stats;
	}

	public void setStats(MetricData[] stats) {
		this.stats = stats;
	}

	public String getCycle() {
		return cycle;
	}

	public void setCycle(String cycle) {
		this.cycle = cycle;
	}

	public long getNode_id() {
		return node_id;
	}

	public void setNode_id(long node_id) {
		this.node_id = node_id;
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
