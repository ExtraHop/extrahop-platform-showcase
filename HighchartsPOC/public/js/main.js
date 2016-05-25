$(function() {

    // API POST Parameters
    // Application OID to display stats from
    // can be extracted from the ExtraHop URL by navigating to:
    //   Metrics > Applications and clicking on the application you want
    // OID will appear in the url as: (example: applicationOid=<id_here>)
    var APPLICATION_OID = 0, // TODO: Populate this!
        // API Key can be generated from the ExtraHop Admin panel
        // for more information,
        // please visit: https://docs.extrahop.com/current/admin-ui-users-guide/#api-access
        // Populate if not using Express backend
        EXTRAHOP_API_KEY = '',
        // Populate EXTRAHOP_HOST if not using Express backend and you've added your host as a
        // 'trusted origin' inside the ExtraHop admin
        EXTRAHOP_HOST = '',
        LOOKBACK_HOURS = 6;

    var EH_COLORS = ['#6BAAC7', '#716BC7', '#B66BC7', '#C76B93', '#C7876B', '#C0C76B', '#7BC76B', '#6BC79F'];

    // API Request Body Parameters for HTTP Responses by Status Code
    // More information can be found here:
    // https://docs.extrahop.com/current/rest-api-guide/
    var BY_STATUS_CODE_PARAMS = {
        cycle: 'auto',
        from: -(LOOKBACK_HOURS * 60 * 60 * 1000),
        metric_category: 'http',
        metric_specs: [{
            name: 'status_code'
        }],
        object_ids: [APPLICATION_OID],
        object_type: 'application',
        until: 0
    };

    // API Request Body Parameters for HTTP Requests by Sever
    // More information can be found here:
    // https://docs.extrahop.com/current/rest-api-guide/
    var BY_SERVER_PARAMS = {
        cycle: 'auto',
        from: -(LOOKBACK_HOURS * 60 * 60 * 1000),
        metric_category: 'http_server_addr_detail',
        metric_specs: [{
            name: 'req'
        }],
        object_ids: [APPLICATION_OID],
        object_type: 'application',
        until: 0
    };

    // Highcharts horizontal bar graph base configuration
    var HORIZONTAL_BAR_GRAPH_OPTIONS = {
        chart: {
            type: 'bar',
            marginRight: 0,
            spacingRight: 0,
            backgroundColor: '#191919', // Space theme
            style: {
                fontFamily: 'Lato'
            }
        },
        colors: EH_COLORS,
        tooltip: {
            enabled: false
        },
        legend: {
            enabled: false
        },
        xAxis: {
            type: 'category',
            labels: {
                enabled: true,
                style: {
                    color: 'white'
                }
            },
            tickLength: 0,
            lineWidth: 0
        },
        yAxis: {
            labels: {
                enabled: false
            },
            title: {
                text: null
            },
            gridLineWidth: 0
        },
        plotOptions: {
            bar: {
                dataLabels: {
                    enabled: true,
                    style: {
                        textShadow: 'none'
                    }
                },
                pointPadding: 0.01,
                borderWidth: 0
            },
            series: {
                pointWidth: 40
            }
        },
        credits: {
            enabled: false
        }
    };

    // Configure authorization key (if available)
    var authKey = '';
    if (EXTRAHOP_API_KEY) {
        authKey = 'ExtraHop apikey=' + EXTRAHOP_API_KEY;
    }

    // Make API call for HTTP Responses by Status Code
    $.ajax({
        type: 'POST',
        url: EXTRAHOP_HOST + '/api/v1/metrics/total',
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        headers: {
            authorization: authKey
        },
        data: JSON.stringify(BY_STATUS_CODE_PARAMS),
        success: populateByStatusChart
    });

    // Make API call for HTTP Request by Server
    $.ajax({
        type: 'POST',
        url: EXTRAHOP_HOST + '/api/v1/metrics/total',
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        headers: {
            authorization: authKey
        },
        data: JSON.stringify(BY_SERVER_PARAMS),
        success: populateByServerTable
    });

    // Parse the response from the ExtraHop API for
    // HTTP - Responses by Status Code
    function parseByStatusCodeResponse(data) {
        // Generate a data structure that works with Highcharts
        // Convert into a two dimensional array of format:
        // [
        //  ['code': value],
        //  ['code': value]
        // ]
        // This is format that is required by highcharts.
        // Feel free to use any other format based upon your charting library
        var retStatusCodes = [];
        data.stats.forEach(function(currentCycle) {
            if (currentCycle.values.length === 1) {
                currentCycle.values[0].forEach(function(metricValue) {
                    var code = metricValue.key.str,
                        count = metricValue.value;

                    retStatusCodes.push([code, count]);
                });
            }
        });

        return retStatusCodes;
    }

    // Parse the response from the ExtraHop API format
    // HTTP - Request by Server
    function parseByServerResponse(data) {
        // Generate a data structure that works with Highcharts
        // Convert into a two dimensional array of format:
        // [
        //  ['host | IP': value],
        //  ['host | IP': value]
        // ]
        // This is format that is required by highcharts.
        // Feel free to use any other format based upon your charting library
        var retServers = [];
        data.stats.forEach(function(currentCycle) {
            if (currentCycle.values.length === 1) {
                currentCycle.values[0].forEach(function(metricValue) {
                    // Use host if available
                    var device = metricValue.key.host || metricValue.key.addr,
                        count = metricValue.value;

                    retServers.push([device, count]);
                });
            }
        });

        return retServers;
    }

    // Configure data for display within highcharts and attach to DOM
    function populateByStatusChart(rawData) {
        // Parse the response
        var byStatusJson = parseByStatusCodeResponse(rawData);
        // Sort by count (high to low)
        byStatusJson.sort(function(a, b) {
            return b[1] - a[1];
        });
        // Limit to top N
        var TOP_N_LIMIT = 8;
        if (byStatusJson.length > TOP_N_LIMIT - 1) {
            byStatusJson = byStatusJson.slice(0, TOP_N_LIMIT);
        }
        // Configure chart options and wire-up to div via hichartts
        var byStatusOptions = {
            title: {
                text: 'HTTP Responses by Status Code',
                style: {
                    color: 'white'
                }
            },
            series: [{
                data: byStatusJson,
                colorByPoint: true,
                dataLabels: {
                    enabled: true,
                    color: '#FFFFFF',
                    align: 'right',
                    formatter: function() {
                        return numeral(this.y).format('0.[0] a');
                    }
                }
            }]
        };
        $('#response-status-chart').highcharts(
            $.extend(HORIZONTAL_BAR_GRAPH_OPTIONS, byStatusOptions)
        );
    }

    // Configure data for display within highcharts and attach to DOM
    function populateByServerTable(rawData) {
        // Parse the response
        var byServerJson = parseByServerResponse(rawData);
        // Sort by count (high to low)
        byServerJson.sort(function(a, b) {
            return b[1] - a[1];
        });
        // Limit to top N
        var TOP_N_LIMIT = 8;
        if (byServerJson.length > TOP_N_LIMIT - 1) {
            byServerJson = byServerJson.slice(0, TOP_N_LIMIT);
        }
        // Configure chart options and wire-up to div via hichartts
        var byStatusOptions = {
            title: {
                text: 'HTTP Requests by Server',
                style: {
                    color: 'white'
                }
            },
            series: [{
                data: byServerJson,
                colorByPoint: true,
                dataLabels: {
                    enabled: true,
                    color: '#FFFFFF',
                    align: 'right',
                    formatter: function() {
                        return numeral(this.y).format('0.[0] a');
                    }
                }
            }]
        };
        $('#request-by-server-chart').highcharts(
            $.extend(HORIZONTAL_BAR_GRAPH_OPTIONS, byStatusOptions)
        );
    }
});
