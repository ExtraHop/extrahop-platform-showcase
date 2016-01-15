package com.extrahop.tools.domain;

/**
 * This class represents a JSON hints group object which is embedded in a trigger. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public class Hints {

	private boolean packetCapture;
	private String metricTypes;
	private String cycle;
	private int payloadBytes;
	private int snaplen;
	private boolean flowUdpAll;
	private boolean flowPayloadTurn;
	private int flowServerPortMin;
	private int flowServerPortMax;
	private int flowClientPortMin;
	private int flowClientPortMax;
	private int flowServerBytes;
	private int flowClientBytes;
	public boolean isPacketCapture() {
		return packetCapture;
	}
	public void setPacketCapture(boolean packetCapture) {
		this.packetCapture = packetCapture;
	}
	public String getMetricTypes() {
		return metricTypes;
	}
	public void setMetricTypes(String metricTypes) {
		this.metricTypes = metricTypes;
	}
	public String getCycle() {
		return cycle;
	}
	public void setCycle(String cycle) {
		this.cycle = cycle;
	}
	public int getPayloadBytes() {
		return payloadBytes;
	}
	public void setPayloadBytes(int payloadBytes) {
		this.payloadBytes = payloadBytes;
	}
	public int getSnaplen() {
		return snaplen;
	}
	public void setSnaplen(int snaplen) {
		this.snaplen = snaplen;
	}
	public boolean isFlowUdpAll() {
		return flowUdpAll;
	}
	public void setFlowUdpAll(boolean flowUdpAll) {
		this.flowUdpAll = flowUdpAll;
	}
	public boolean isFlowPayloadTurn() {
		return flowPayloadTurn;
	}
	public void setFlowPayloadTurn(boolean flowPayloadTurn) {
		this.flowPayloadTurn = flowPayloadTurn;
	}
	public int getFlowServerPortMin() {
		return flowServerPortMin;
	}
	public void setFlowServerPortMin(int flowServerPortMin) {
		this.flowServerPortMin = flowServerPortMin;
	}
	public int getFlowServerPortMax() {
		return flowServerPortMax;
	}
	public void setFlowServerPortMax(int flowServerPortMax) {
		this.flowServerPortMax = flowServerPortMax;
	}
	public int getFlowClientPortMin() {
		return flowClientPortMin;
	}
	public void setFlowClientPortMin(int flowClientPortMin) {
		this.flowClientPortMin = flowClientPortMin;
	}
	public int getFlowClientPortMax() {
		return flowClientPortMax;
	}
	public void setFlowClientPortMax(int flowClientPortMax) {
		this.flowClientPortMax = flowClientPortMax;
	}
	public int getFlowServerBytes() {
		return flowServerBytes;
	}
	public void setFlowServerBytes(int flowServerBytes) {
		this.flowServerBytes = flowServerBytes;
	}
	public int getFlowClientBytes() {
		return flowClientBytes;
	}
	public void setFlowClientBytes(int flowClientBytes) {
		this.flowClientBytes = flowClientBytes;
	}
}
