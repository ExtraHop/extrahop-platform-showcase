<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/1.0.2/Chart.min.js"></script>
<title>ExtraHopRestAPIWeb</title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
</head>
<body>
	<div style="margin: 15px">
		<div style="display: inline-block; vertical-align: top;">
			<!-- selection form -->
			<form class="form" name="form">
				<ul>
					<li>
						<h2>Display HTTP Metrics</h2>
					</li>
					<li><label for="server">ExtraHop</label> 
						<input name="server" type="text" id="server" placeholder="Host Name or IP" required pattern="^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3})$|^((([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9]))$">
					</li>
					<li><label for="apiKey">API Key</label> 
						<input name="apiKey" type="text" id="apiKey" placeholder="API Key String" required>
					</li>
					<li><label for="metric">Metric</label> <select name="metric"
						id="metric">
							<option value="req">Requests</option>
							<option value="req_abort">Aborted Requests</option>
							<option value="req_bytes">Request Goodput Bytes</option>
							<option value="req_l2_bytes">Request L2 Bytes</option>
							<option value="req_pkts">Request Packets</option>
							<option value="req_rto">Request RTOs</option>
							<option value="req_zwnd">Request Zero Windows</option>
							<option value="rsp">Responses</option>
							<option value="rsp_abort">Aborted Responses</option>
							<option value="rsp_bytes">Response Goodput Bytes</option>
							<option value="rsp_error">Errors</option>
							<option value="rsp_l2_bytes">Response L2 Bytes</option>
							<option value="rsp_pkts">Response Packets</option>
							<option value="rsp_rto">Response RTOs</option>
							<option value="rsp_zwnd">Response Zero Windows</option>
					</select></li>
					<li><label for="chartType">Chart Type</label> <select
						name="chartType" id="chartType">
							<option value="line">Line</option>
							<option value="bar">Bar</option>
					</select></li>
					<li><label for="cycle">Cycle</label> <select name="cycle"
						id="cycle">
							<option value="auto">Auto</option>
							<option value="30sec">30 sec</option>
							<option value="5min">5 min</option>
							<option value="1hr">1 hr</option>
							<option value="24hr">24 hr</option>
					</select></li>
					<li class="date"><label for="dateFrom">From</label> <input
						type="datetime-local" id="dateFrom" name="dateFrom"></li>
					<li class="date"><label for="dateTo">To</label> <input
						type="datetime-local" id="dateTo" name="dateTo"></li>
					<li>
						<button class="submit" type="submit">Submit</button>
					</li>
				</ul>
			</form>
		</div>

		<div style="display: inline-block;">
			<!-- chart to be displayed -->
			<c:choose>
				<c:when test="${param.chartType=='line'}">
					<jsp:include page="/line.jsp" />
				</c:when>
				<c:when test="${param.chartType=='bar'}">
					<jsp:include page="/bar.jsp" />
				</c:when>
			</c:choose>
		</div>
	</div>
	<script type="text/javascript">
		//locat time formatting
		function formatLocalDate(date) {
	        var pad = function(num) {
	            var norm = Math.abs(Math.floor(num));
	            return (norm < 10 ? '0' : '') + norm;
	        };
		    return date.getFullYear() 
		        + '-' + pad(date.getMonth()+1)
		        + '-' + pad(date.getDate())
		        + 'T' + pad(date.getHours())
		        + ':' + pad(date.getMinutes());
		}
	
		//get a request parameter
		function getReqParam(name){
		   if(name=(new RegExp('[?&]'+encodeURIComponent(name)+'=([^&]*)')).exec(location.search))
		      return decodeURIComponent(name[1]);
		}
		
		//set the form parameter values based on whether they have been already set
		var server = getReqParam('server');
		if(server!=null)
			document.getElementById('server').value = server;
		
		var apiKey = getReqParam('apiKey');
		if(apiKey!=null)
			document.getElementById('apiKey').value = apiKey;
		
		var metric = getReqParam('metric');
		if(metric!=null)
			document.getElementById('metric').value = metric;
		var chartType = getReqParam('chartType');
		if(chartType!=null)
			document.getElementById('chartType').value = chartType;
		
		var cycle = getReqParam('cycle');
		if(cycle!=null)
			document.getElementById('cycle').value = cycle;
		
		var dateFrom = getReqParam('dateFrom');
		if(dateFrom!=null)
			document.getElementById('dateFrom').value = dateFrom;
		else {
			var dayAgo = new Date(Date.now() - 86400000);
			document.getElementById('dateFrom').value = formatLocalDate(dayAgo);
		}
		
		var dateTo = getReqParam('dateTo');
		if(dateTo!=null)
			document.getElementById('dateTo').value = dateTo;
		else 
			document.getElementById('dateTo').value = formatLocalDate(new Date());
	</script>

</body>
</html>