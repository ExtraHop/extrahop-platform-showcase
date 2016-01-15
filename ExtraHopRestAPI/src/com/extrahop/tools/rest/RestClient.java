package com.extrahop.tools.rest;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;

import com.extrahop.tools.domain.Assignment;
import com.extrahop.tools.domain.Device;
import com.extrahop.tools.domain.DeviceGroup;
import com.extrahop.tools.domain.Metric;
import com.extrahop.tools.domain.MetricStats;
import com.extrahop.tools.domain.Trigger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * RestClient is a Java wrapper for the ExtraHop REST API.
 * @author jeffbfry
 *
 */
public class RestClient {

	//base url to the api
	private String baseUrl;
	
	//host IP address
	private String host;
	
	//the ExtraHop Rest API Key that is generated from the admin page
	private String apiKey;

	/**
	 * Construct the RestClient with the host and API key.
	 * @param host
	 * @param apiKey
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	public RestClient(String host, String apiKey)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		this.baseUrl = "https://" + host + "/api/v1/";

		//set up UniRest to convert JSON to Java Objects
		Unirest.setObjectMapper(new CustomObjectMapper());

		//Set up SSL to ignore self-signed certs
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
		SSLConnectionSocketFactory sslSf = new SSLConnectionSocketFactory(sslContext,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		RequestConfig.Builder requestBuilder = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(5000);
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSf)
				.setDefaultRequestConfig(requestBuilder.build()).build();
		Unirest.setHttpClient(httpClient);
		
		//set the API key in the header
		Unirest.setDefaultHeader("Authorization", "ExtraHop apikey=" + apiKey);
		
		//set accept JSON
		Unirest.setDefaultHeader("accept", "application/json");
	}
	
	/**
	 * Returns the host.
	 * @return
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the API key.
	 * @return
	 */
	public String getApiKey() {
		return apiKey;
	}
	
	/**
	 * The following are helper methods for sending standard DELETE, GET, POST, and PATCH
	 * HTTP methods and processing responses. These are used by the element URI methods further down below.
	 */

	public JsonNode sendDeleteForJson(String uri) throws RestException {
		try {
			HttpResponse<JsonNode> response = Unirest.delete(uri).asJson();
			if(response.getStatus()>299){
				String error = response.getBody().getObject().getString("error_message");
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public String sendDeleteForString(String uri) throws RestException {
		try {
			HttpResponse<String> response = Unirest.delete(uri).asString();
			if(response.getStatus()>299){
				String error = response.getBody();
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public <T> T sendDeleteForObject(String uri,Class<? extends T> responseClass) throws RestException {
		try {
			HttpResponse<T> response = Unirest.delete(uri).asObject(responseClass);
			if(response.getStatus()>299){
				String error = response.getStatusText();
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public JsonNode sendGetForJson(String uri) throws RestException {
		try {
			HttpResponse<JsonNode> response = Unirest.get(uri).asJson();
			if(response.getStatus()>299){
				String error = response.getBody().getObject().getString("error_message");
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public String sendGetForString(String uri) throws RestException {
		try {
			HttpResponse<String> response = Unirest.get(uri).asString();
			if(response.getStatus()>299){
				String error = response.getBody();
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public <T> T sendGetForObject(String uri,Class<? extends T> responseClass) throws RestException {
		try {
			HttpResponse<T> response = Unirest.get(uri).asObject(responseClass);
			if(response.getStatus()>299){
				String error = response.getStatusText();
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public JsonNode sendPostForJson(String uri,Object object) throws RestException {
		try {
			HttpResponse<JsonNode> response = Unirest.post(uri).body(object).asJson(); 
			if(response.getStatus()>299){
				String error = response.getBody().getObject().getString("error_message");
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public String sendPostForString(String uri,Object body) throws RestException {
		try {
			HttpResponse<String> response = Unirest.post(uri).body(body).asString();
			if(response.getStatus()>299){
				String error = response.getBody();
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public <T> T sendPostForObject(String uri,Object body,Class<? extends T> responseClass) throws RestException {
		try {
			HttpResponse<T> response = Unirest.post(uri).body(body).asObject(responseClass);
			if(response.getStatus()>299){
				String error = response.getStatusText();
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public JsonNode sendPatchForJson(String uri,Object body) throws RestException {
		try {
			HttpResponse<JsonNode> response = Unirest.patch(uri).body(body).asJson(); 
			if(response.getStatus()>299){
				String error = response.getBody().getObject().getString("error_message");
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public String sendPatchForString(String uri,Object body) throws RestException {
		try {
			HttpResponse<String> response = Unirest.patch(uri).body(body).asString();
			if(response.getStatus()>299){
				String error = response.getBody();
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}
	
	public <T> T sendPatchForObject(String uri,Object body,Class<? extends T> responseClass) throws RestException {
		try {
			HttpResponse<T> response = Unirest.patch(uri).body(body).asObject(responseClass);
			if(response.getStatus()>299){
				String error = response.getStatusText();
				throw new RestException(uri, response.getStatus(),error);
			}
			return response.getBody();
		} catch (JSONException | UnirestException e) {
			throw new RestException(uri, e.getMessage());
		}
	}

	/**
	 * The following are methods that are mapped to the REST API calls. See the
	 * API Explorer at <host>/api/v1/explore/ for details.
	 */
	
	/**
	 * Gets the ExtraHop firmware version.
	 * @return The version string.
	 * @throws RestException
	 */
	public String getExtraHopVersion() throws RestException {
		String uri = baseUrl + "extrahop/version";
		return sendGetForJson(uri).getObject().getString("version");
	}

	/**
	 * Gets the ExtraHop platform.
	 * @return
	 * @throws RestException
	 */
	public String getExtraHopPlatform() throws RestException {
		String uri = baseUrl + "extrahop/platform";
		return sendGetForJson(uri).getObject().getString("platform");
	}

	/**
	 * Gets the list of triggers from the ExtraHop server.
	 * @return An array of trigger objects that has the full definition of the trigger.
	 * @throws RestException
	 */
	public Trigger[] getTriggers() throws RestException {
		String uri = baseUrl + "triggers";
		return sendGetForObject(uri, Trigger[].class);
	}
	
	/**
	 * Gets the list of active devices.
	 * @param lastNHours Specifies the period to query active devices.
	 * @return An array of devices that has the full definition of the device.
	 * @throws RestException
	 */
	public Device[] getDevices(int lastNHours) throws RestException {
		long since = System.currentTimeMillis() - lastNHours*3600000;
		String uri = baseUrl + "devices?active_from="+since;
		return sendGetForObject(uri, Device[].class);
	}
	
	/**
	 * Gets the list of device groups.
	 * @return An array of device groups that has the full definition of the device group.
	 * @throws RestException
	 */
	public DeviceGroup[] getDeviceGroups() throws RestException {
		String uri = baseUrl + "devicegroups";
		return sendGetForObject(uri, DeviceGroup[].class);
	}
	
	/**
	 * Returns the active devices for a device group.
	 * @param id The device group id.
	 * @param lastNHours Specifies the period to query active devices.
	 * @return An array of devices that has the full definition of the device.
	 * @throws RestException
	 */
	public Device[] getDevicesFromDeviceGroup(int id,int lastNHours) throws RestException {
		long since = System.currentTimeMillis() - lastNHours*3600000;
		String uri = baseUrl + "devicegroups/"+id+"/devices?active_from="+since;
		return sendGetForObject(uri, Device[].class);
	}
	
	/**
	 * Gets a trigger by name.
	 * @param name The name of the trigger.
	 * @return The Trigger object.
	 * @throws RestException
	 */
	public Trigger getTriggerByName(String name) throws RestException {
		Trigger[] triggers = getTriggers();
		for(Trigger trigger:triggers)
			if(trigger.getName().equals(name))
				return trigger;
		return null;
	}

	/**
	 * Adds a new trigger on the ExtraHop server.
	 * @param trigger The new trigger definition.
	 * @return A JSON response. See the REST API Explorer for more details.
	 * @throws RestException
	 */
	public JsonNode postTrigger(Trigger trigger) throws RestException {
		String uri = baseUrl + "triggers";
		return sendPostForJson(uri, trigger);
	}
	
	/**
	 * Updates an existing trigger on the ExtraHop server.
	 * @param trigger The new trigger definition.
	 * @param id ID of the trigger.
	 * @return A JSON response. See the REST API Explorer for more details.
	 * @throws RestException
	 */
	public JsonNode updateTrigger(Trigger trigger,int id) throws RestException {
		String uri = baseUrl + "triggers/"+id;
		return sendPatchForJson(uri, trigger);
	}
	
	/**
	 * Deletes a trigger.
	 * @param id The ID of the trigger to delete.
	 * @return A JSON response. See the REST API Explorer for more details.
	 * @throws RestException
	 */
	public JsonNode deleteTrigger(int id) throws RestException {
		String uri = baseUrl + "triggers/"+id;
		return sendDeleteForJson(uri);
	}
	
	/**
	 * Assigns a trigger to device groups.
	 * @param id The ID of the trigger.
	 * @param deviceGroups The device groups for assignment.
	 * @return A JSON response in String form. See the REST API Explorer for more details.
	 * @throws RestException
	 */
	public String assignTriggerToDeviceGroups(int id,DeviceGroup[] deviceGroups) throws RestException {
		if(deviceGroups.length==0)
			return null;
		Assignment assignments = new Assignment();
		int[] deviceGroupsIds = new int[deviceGroups.length];
		for(int i=0;i<deviceGroupsIds.length;i++)
			deviceGroupsIds[i]=deviceGroups[i].getId();
		assignments.setAssign(deviceGroupsIds);
		String uri = baseUrl + "triggers/"+id+"/devicegroups";
	
		return sendPostForString(uri,assignments);
	}
	
	/**
	 * Assigns a trigger to devices.
	 * @param id The ID of the trigger.
	 * @param devices The devices for assignment.
	 * @return A JSON response in String form. See the REST API Explorer for more details.
	 * @throws RestException
	 */
	public String assignTriggerToDevices(int id,Device[] devices) throws RestException {
		if(devices.length==0)
			return null;
		Assignment assignments = new Assignment();
		int[] deviceIds = new int[devices.length];
		for(int i=0;i<devices.length;i++)
			deviceIds[i]=devices[i].getId();
		assignments.setAssign(deviceIds);
		String uri = baseUrl + "triggers/"+id+"/devices";
		
		return sendPostForString(uri,assignments);
	}
	
	/**
	 * Unassigns a trigger from device groups.
	 * @param id The ID of the trigger.
	 * @param deviceGroups The device groups to unassign.
	 * @return A JSON response in String form. See the REST API Explorer for more details.
	 * @throws RestException
	 */
	public String unassignTriggerFromDeviceGroups(int id,DeviceGroup[] deviceGroups) throws RestException {
		if(deviceGroups.length==0)
			return null;
		Assignment assignments = new Assignment();
		int[] deviceGroupsIds = new int[deviceGroups.length];
		for(int i=0;i<deviceGroupsIds.length;i++)
			deviceGroupsIds[i]=deviceGroups[i].getId();
		assignments.setUnassign(deviceGroupsIds);
		String uri = baseUrl + "triggers/"+id+"/devicegroups";
	
		return sendPostForString(uri,assignments);
	}
	
	/**
	 * Unassign a trigger from devices.
	 * @param id The ID of the trigger.
	 * @param devices The devices to unassign.
	 * @return A JSON response in String form. See the REST API Explorer for more details.
	 * @throws RestException
	 */
	public String unassignTriggerFromDevices(int id,Device[] devices) throws RestException {
		if(devices.length==0)
			return null;
		Assignment assignments = new Assignment();
		int[] deviceIds = new int[devices.length];
		for(int i=0;i<devices.length;i++)
			deviceIds[i]=devices[i].getId();
		assignments.setUnassign(deviceIds);
		String uri = baseUrl + "triggers/"+id+"/devices";
		
		return sendPostForString(uri,assignments);
	}
	
	/**
	 * Gets the devices assigned to a trigger.
	 * @param id The ID of the trigger.
	 * @return An array of devices that has the full definition of the device.
	 * @throws RestException
	 */
	public Device[] getDevicesAssignedToTrigger(int id) throws RestException {
		String uri = baseUrl + "triggers/"+id+"/devices";
		return sendGetForObject(uri, Device[].class);
	}
	
	/**
	 * Gets the device groups assigned to a trigger.
	 * @param id The ID of the trigger.
	 * @return An array of device groups that has the full definition of the device group.
	 * @throws RestException
	 */
	public DeviceGroup[] getDeviceGroupsAssignedToTrigger(int id) throws RestException {
		String uri = baseUrl + "triggers/"+id+"/devicegroups";
		return sendGetForObject(uri, DeviceGroup[].class);
	}
	
	/**
	 * Queries for metrics.
	 * @param metric The metric to query for.
	 * @return The metric results.
	 * @throws RestException
	 */
	public MetricStats getMetrics(Metric metric) throws RestException{
		String uri = baseUrl + "metrics";
		return sendPostForObject(uri, metric, MetricStats.class);
	}
	
	/**
	 * Queries for the metric totals.
	 * @param metric The metric to query for.
	 * @return The metric results.
	 * @throws RestException
	 */
	public MetricStats getMetricsTotal(Metric metric) throws RestException{
		String uri = baseUrl + "metrics/total";
		return sendPostForObject(uri, metric, MetricStats.class);
	}
	
	/**
	 * Queries for metric totals and grouped by object.
	 * @param metric The metric to query for.
	 * @return The metric results.
	 * @throws RestException
	 */
	public MetricStats getMetricsTotalByObject(Metric metric) throws RestException{
		String uri = baseUrl + "metrics/totalbyobject";
		return sendPostForObject(uri, metric, MetricStats.class);
	}

	/**
	 * Returns the REST API base URL.
	 * @return The URL.
	 */
	public String getBaseUrl() {
		return baseUrl;
	}
	
	/**
	 * Call to shutdown the RestClient and release resources.
	 * @throws IOException
	 */
	public void shutdown() throws IOException{
		Unirest.shutdown();
	}

	/**
	 * This Object mapper converts the JSON string to Java objects for convenience.
	 * @author jeffbfry
	 *
	 */
	protected class CustomObjectMapper implements ObjectMapper {

		private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

		public CustomObjectMapper() {
			super();
			jacksonObjectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		}

		@Override
		public <T> T readValue(String value, Class<T> valueType) {
			try {
				return jacksonObjectMapper.readValue(value, valueType);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String writeValue(Object value) {
			try {
				return jacksonObjectMapper.writeValueAsString(value);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}

	}
}
