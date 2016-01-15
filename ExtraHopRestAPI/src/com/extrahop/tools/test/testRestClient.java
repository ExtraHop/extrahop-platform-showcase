/**
 * 
 */
package com.extrahop.tools.test;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.extrahop.tools.domain.Device;
import com.extrahop.tools.domain.DeviceGroup;
import com.extrahop.tools.domain.Metric;
import com.extrahop.tools.domain.MetricStats;
import com.extrahop.tools.rest.RestClient;
import com.extrahop.tools.rest.RestException;

/**
 * This JUnit class tests the various API calls and has examples of how to use the RestClient.
 * @author jeffbfry
 *
 */
public class testRestClient {
	
	private static RestClient restClient;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		restClient = new RestClient("10.7.65.54","dad42ec48c644983924d5c349406d99d");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		restClient.shutdown();
	}

	@Test
	public void testMetric() {
		try {
			//create the metric query
			Metric metricQuery = new Metric();
			metricQuery.setCycle("auto");
			metricQuery.setObject_type("application");
			metricQuery.setMetric_category("http_server_addr_detail");
			metricQuery.addSpec("name", "req");
			
			//calls /metrics
			MetricStats results = restClient.getMetrics(metricQuery);
			System.out.println("getMetrics:"+results.toPrettyPrintString());
			
			//calls /metrics/total
			results = restClient.getMetricsTotal(metricQuery);
			System.out.println("getMetricsTotal:"+results.toPrettyPrintString());
			
			//calls /metrics/totalbyobject
			results = restClient.getMetricsTotalByObject(metricQuery);
			System.out.println("getMetricsTotalByObject:"+results.toPrettyPrintString());
		} catch (RestException e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testExtraHopPlatformAndVersion() {
		try {
			String platform = restClient.getExtraHopVersion();
			String version = restClient.getExtraHopVersion();
			System.out.println("Platform:"+platform);
			System.out.println("Version:"+version);
		} catch (RestException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testDeviceGroups() {
		try {
			DeviceGroup[] deviceGroups = restClient.getDeviceGroups();
			System.out.println("Device Groups:\n");
			for(DeviceGroup deviceGroup:deviceGroups)
				System.out.println(deviceGroup.toPrettyPrintString());
		} catch (RestException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testDevices() {
		try {
			Device[] activeDevices = restClient.getDevices(24);
			System.out.println("Active devices in last 24 hours:\n");
			for(Device device:activeDevices)
				System.out.println(device.toPrettyPrintString());
		} catch (RestException e) {
			fail(e.getMessage());
		}
	}

}
