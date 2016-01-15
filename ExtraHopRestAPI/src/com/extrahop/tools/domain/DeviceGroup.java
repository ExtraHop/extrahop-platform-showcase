package com.extrahop.tools.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class represents a JSON device group object. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public class DeviceGroup extends EHObject {
	
	private String description="";
	private boolean include_custom_devices;
	private boolean dynamic=true;
	private String field;
	private String value;
	
	@JsonIgnore
	private List<Device> devices = new ArrayList<Device>();
	
	public DeviceGroup() {
		super();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isInclude_custom_devices() {
		return include_custom_devices;
	}

	public void setInclude_custom_devices(boolean include_custom_devices) {
		this.include_custom_devices = include_custom_devices;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public void addDevice(Device device){
		devices.add(device);
	}

	public List<Device> getDevices() {
		return devices;
	}
}
