<canvas id="myChart" width="800" height="400"></canvas>
<script type="text/javascript">
	//get a request parameter
	function getReqParam(name) {
		if (name = (new RegExp('[?&]' + encodeURIComponent(name) + '=([^&]*)'))
				.exec(location.search))
			return decodeURIComponent(name[1]);
	}

	var ctx = document.getElementById("myChart").getContext("2d");

	var options = {

		///Boolean - Whether grid lines are shown across the chart
		scaleShowGridLines : true,

		//String - Colour of the grid lines
		scaleGridLineColor : "rgba(0,0,0,.05)",

		//Number - Width of the grid lines
		scaleGridLineWidth : 1,

		//Boolean - Whether to show horizontal lines (except X axis)
		scaleShowHorizontalLines : true,

		//Boolean - Whether to show vertical lines (except Y axis)
		scaleShowVerticalLines : true,

		//Boolean - Whether the line is curved between points
		bezierCurve : true,

		//Number - Tension of the bezier curve between points
		bezierCurveTension : 0.4,

		//Boolean - Whether to show a dot for each point
		pointDot : true,

		//Number - Radius of each point dot in pixels
		pointDotRadius : 4,

		//Number - Pixel width of point dot stroke
		pointDotStrokeWidth : 1,

		//Number - amount extra to add to the radius to cater for hit detection outside the drawn point
		pointHitDetectionRadius : 20,

		//Boolean - Whether to show a stroke for datasets
		datasetStroke : true,

		//Number - Pixel width of dataset stroke
		datasetStrokeWidth : 2,

		//Boolean - Whether to fill the dataset with a colour
		datasetFill : true,
	};

	var ajaxReq = new XMLHttpRequest();
	ajaxReq.open('GET', '/ExtraHopRestAPIWeb/GetMetricsForChartServlet?server='
			+ getReqParam('server') + '&apiKey=' + getReqParam('apiKey')
			+ '&metric=' + getReqParam('metric') + '&chartType='
			+ getReqParam('chartType') + '&cycle='
			+ getReqParam('cycle') + '&dateFrom=' + getReqParam('dateFrom')
			+ '&dateTo=' + getReqParam('dateTo'));
	ajaxReq.onload = function(e) {
		var data = JSON.parse(this.response);

		if (data)
			var myChart = new Chart(ctx).Line(data, options);
	}
	ajaxReq.send();
</script>