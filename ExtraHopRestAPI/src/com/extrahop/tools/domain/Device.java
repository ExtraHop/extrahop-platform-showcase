package com.extrahop.tools.domain;

/**
 * This class represents a JSON device object. Properties are automatically mapped to JSON properties during serialization
 * and de-serialization.
 * @author jeffbfry
 *
 */
public class Device extends EHObject {

	private String node_id;
	private String extrahop_id;
	private String description="";
	private long user_mod_time=0;
	private long discover_time;
	private int vlanid=0;
	private int parent_id;
	private String macaddr;
	private String vendor;
	private boolean is_l3=false;
	private String ipaddr4;
	private String ipaddr6;
	private String device_class;
	private String default_name;
	private String custom_name;
	private String cdp_name;
	private String dhcp_name;
	private String netbios_name;
	private String dns_name;
	private String custom_type;
	private int analysis_level;
	
	public Device() {
		super();
	}

	public String getNode_id() {
		return node_id;
	}

	public void setNode_id(String node_id) {
		this.node_id = node_id;
	}

	public String getExtrahop_id() {
		return extrahop_id;
	}

	public void setExtrahop_id(String extrahop_id) {
		this.extrahop_id = extrahop_id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getUser_mod_time() {
		return user_mod_time;
	}

	public void setUser_mod_time(long user_mod_time) {
		this.user_mod_time = user_mod_time;
	}

	public long getDiscover_time() {
		return discover_time;
	}

	public void setDiscover_time(long discover_time) {
		this.discover_time = discover_time;
	}

	public int getVlanid() {
		return vlanid;
	}

	public void setVlanid(int vlanid) {
		this.vlanid = vlanid;
	}

	public int getParent_id() {
		return parent_id;
	}

	public void setParent_id(int parent_id) {
		this.parent_id = parent_id;
	}

	public String getMacaddr() {
		return macaddr;
	}

	public void setMacaddr(String macaddr) {
		this.macaddr = macaddr;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public boolean isIs_l3() {
		return is_l3;
	}

	public void setIs_l3(boolean is_l3) {
		this.is_l3 = is_l3;
	}

	public String getIpaddr4() {
		return ipaddr4;
	}

	public void setIpaddr4(String ipaddr4) {
		this.ipaddr4 = ipaddr4;
	}

	public String getIpaddr6() {
		return ipaddr6;
	}

	public void setIpaddr6(String ipaddr6) {
		this.ipaddr6 = ipaddr6;
	}

	public String getDevice_class() {
		return device_class;
	}

	public void setDevice_class(String device_class) {
		this.device_class = device_class;
	}

	public String getDefault_name() {
		return default_name;
	}

	public void setDefault_name(String default_name) {
		this.default_name = default_name;
	}

	public String getCustom_name() {
		return custom_name;
	}

	public void setCustom_name(String custom_name) {
		this.custom_name = custom_name;
	}

	public String getCdp_name() {
		return cdp_name;
	}

	public void setCdp_name(String cdp_name) {
		this.cdp_name = cdp_name;
	}

	public String getDhcp_name() {
		return dhcp_name;
	}

	public void setDhcp_name(String dhcp_name) {
		this.dhcp_name = dhcp_name;
	}

	public String getNetbios_name() {
		return netbios_name;
	}

	public void setNetbios_name(String netbios_name) {
		this.netbios_name = netbios_name;
	}

	public String getDns_name() {
		return dns_name;
	}

	public void setDns_name(String dns_name) {
		this.dns_name = dns_name;
	}

	public String getCustom_type() {
		return custom_type;
	}

	public void setCustom_type(String custom_type) {
		this.custom_type = custom_type;
	}

	public int getAnalysis_level() {
		return analysis_level;
	}

	public void setAnalysis_level(int analysis_level) {
		this.analysis_level = analysis_level;
	}
}
