package com.extrahop.tools.rest.web;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.extrahop.tools.domain.Metric;
import com.extrahop.tools.domain.MetricData;
import com.extrahop.tools.domain.MetricStats;
import com.extrahop.tools.rest.RestClient;
import com.extrahop.tools.rest.RestException;

/**
 * Servlet implementation class GetMetricsForChartServlet. This servlet queries metrics from the ExtraHop REST API
 * and outputs the JSON format for ChartJS.
 * See http://www.chartjs.org/docs/#line-chart-data-structure
 */
@WebServlet(description = "Retrieves the metrics from the REST API and formats for chartJS.", urlPatterns = {
		"/GetMetricsForChartServlet" })
public class GetMetricsForChartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	private static SimpleDateFormat labelDateFormat = new SimpleDateFormat("HH:mm");

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetMetricsForChartServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// gather and check parameters
		String server = request.getParameter("server");
		String apiKey = request.getParameter("apiKey");
		String metric = request.getParameter("metric");
		String chartType = request.getParameter("chartType");
		String cycle = request.getParameter("cycle");
		String dateFrom = request.getParameter("dateFrom");
		String dateTo = request.getParameter("dateTo");
		
		//validate parameters
		if (server == null || apiKey == null || metric == null || chartType == null ||cycle == null || dateFrom == null
				|| dateTo == null) {
			response.setStatus(400);
			return;
		}

		try {
			
			//create the RestClient for accessing the ExtraHop Rest API
			RestClient restClient = new RestClient(server, apiKey);
			
			//create the metric to query. we can look up the metric rest details
			//in the REST section of the ExtraHop Metric Catalog
			Metric metricQuery = new Metric();
			metricQuery.setCycle(cycle);
			metricQuery.setObject_type("application");
			metricQuery.setObject_ids(new int[]{1});
			metricQuery.setMetric_category("http");
			metricQuery.addSpec("name", metric);
			metricQuery.setFrom(dateFormat.parse(dateFrom).getTime());
			metricQuery.setUntil(dateFormat.parse(dateTo).getTime());

			//query for our metrics
			MetricStats results = restClient.getMetrics(metricQuery);
			
			//here we convert the MetricStats object to a json string format for ChartJS
			//see http://www.chartjs.org/docs/#line-chart-data-structure
			StringBuilder jsonData = new StringBuilder();
			StringBuilder jsonLabels = new StringBuilder();
			for(MetricData data:results.getStats()){
				jsonData.append(data.getValues()[0]);
				jsonData.append(",");
				jsonLabels.append("\"");
				jsonLabels.append(labelDateFormat.format(data.getTime()));
				jsonLabels.append("\"");
				jsonLabels.append(",");
			}
			if(jsonData.length()>0)
				jsonData.deleteCharAt(jsonData.length()-1);
			if(jsonLabels.length()>0)
				jsonLabels.deleteCharAt(jsonLabels.length()-1);
			
			StringBuilder json = new StringBuilder("{ \"labels\":[");
			json.append(jsonLabels);
			json.append("],"+"\"datasets\": [{ \"label\":\"");
			json.append(metric);
			json.append("\",\n");
			json.append("\"fillColor\": \"rgba(220,220,220,0.2)\",\n");
			json.append("\"strokeColor\": \"rgba(220,220,220,1)\",\n"); 
			json.append("\"pointColor\": \"rgba(220,220,220,1)\",\n"); 
			json.append("\"pointStrokeColor\": \"#fff\",\n"); 
			json.append("\"pointHighlightFill\": \"#fff\",\n");
			json.append("\"pointHighlightStroke\": \"rgba(220,220,220,1)\",\n");
			json.append("\"data\": [");
			json.append(jsonData);
			json.append("] } ] }");
				
			//write out the json format for ChartJS
			response.getWriter().println(json);
			
			//shutdown the RestClient
			restClient.shutdown();
		} catch (RestException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException | ParseException e) {
			e.printStackTrace(response.getWriter());
		}
	}

}
