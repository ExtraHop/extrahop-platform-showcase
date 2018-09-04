// Time-stamp: <2018-08-10 10:36:13 (dtucholski)>
//
// Description: Retrieve Load Balancer metrics and send them to your ExtraHop
// Author(s): Dan Tucholski and ExtraHop Networks

///////////////////////////////////////////////////////////////////////////////
// This file is part of an ExtraHop Supported Bundle.  Make NO MODIFICATIONS //
///////////////////////////////////////////////////////////////////////////////

const util = require('util');
const msRestAzure = require('ms-rest-azure');
const monitorManagement = require('azure-arm-monitor');
const resourceManagement = require('azure-arm-resource');
const moment = require('moment');
const memcached = require('memcached');

// Helper function to print object
function inspectObj(obj) {
    return util.inspect(obj, {showHidden: false, depth: null})
}

module.exports = function (context, myTimer) {

    const tagName = 'extrahop-azure-bundle';
    const resourceType = 'Microsoft.Network/loadBalancers';
    const metricList = {
        'AllocatedSnatPorts': 'total',
        'ByteCount': 'total',
        'DipAvailability': 'average',
        'PacketCount': 'total',
        'SYNCount': 'total',
        'SnatConnectionCount': 'total',
        'UsedSnatPorts': 'total',
        'VipAvailability': 'average'
    };
    // Pull in the environment vars
    const subscriptionId = process.env["AZURE_SUBSCRIPTION_ID"];
    const extrahopIp = process.env["EXTRAHOP_IP"];
    const extrahopPort = process.env["EXTRAHOP_PORT"];

    let lbValues = {};

    // Login using Managed Service Identity
    msRestAzure.loginWithAppServiceMSI().then(function msiLogin (credentials) {
        
        // Setup azure monitor client
        let monitorClient = new monitorManagement(credentials, subscriptionId);
        
        // Pull 1 minute metrics for only the last 5 minutes
        let endDate = new Date();
        let startDate = new Date(endDate.getTime() - 300000); // 5 mins * 60,000 ms
        let metricTimespan = startDate.toISOString() + "/" + endDate.toISOString();
        let metricInterval = moment.duration(1, 'minutes');
        let metricAggregation = 'maximum, total, average';
        let metricNames = Object.keys(metricList).join(',');
        
        // Configure metric options
        let metricOptions = {
            metricnames: metricNames,
            timespan: metricTimespan,
            interval: metricInterval,
            aggregation: metricAggregation
        };

        // Setup azure resource client
        let resourceClient = new resourceManagement.ResourceManagementClient(credentials, subscriptionId);
        let resourceOptions = {
            // Only one filter at a time appears to work
            // i.e. `tagname eq '${tagName}' and resourceType eq ${resourceType}` fails, but they work separateyly
            filter: `tagname eq '${tagName}'`
        };

        // Get list of all tagged resources
        resourceClient.resources.list(resourceOptions).then(function getAllResources (allResources) {

            // Filter by resource type
            let lbResources = allResources.filter(resource =>
                resource.type === resourceType
            );
            context.log.info("Found " + lbResources.length + " resource(s) of type: " + resourceType + ", tagged: " + tagName);

            // Return array of promises to asynchronously pull LB resource details and metrics 
            let lbPromises = lbResources.map(resource => {
                            
                lbValues[resource.id] = {};
                lbValues[resource.id]["name"] = resource.name;
                            
                // Get Azure Monitoring Metrics
                return monitorClient.metrics.list(resource.id, metricOptions).then(function getMetrics (metrics) {
                    //context.log.verbose("Metrics: " + inspectObj(metrics));
                    lbValues[resource.id]["region"] = metrics.resourceregion;
                    // Populate metric values
                    lbValues[resource.id]["metrics"] = {};
                    metrics.value.forEach(metric => {
                        let metricName = metric.name.value;
                        // Gracefully handle missing metric values
                        if (metric.timeseries.length && metric.timeseries[0].hasOwnProperty('data')) {
                            let metricData = metric.timeseries[0].data;
                            let aggregationType = metricList[metricName];

                            if (metricData[metricData.length - 2] && metricData[metricData.length - 2].hasOwnProperty(aggregationType)) {
                                // Use the second most recent item since the most recent seems to usually be empty
                                lbValues[resource.id]["metrics"][metricName] = metricData[metricData.length - 2][aggregationType];
                            }
                        }
                    });
                }).catch(err =>
                    // getMetrics error
                    context.log.error("getMetrics " + err)
                );
            });

            // After all LB resource details and metrics have been populated send metrics object to ExtraHop over ODC
            Promise.all(lbPromises).then(function sendExtraHopODC () {
                // Configure ExtraHop ODC 
                let memcacheServer = extrahopIp + ":" + extrahopPort;
                let extrahopODC = new memcached(memcacheServer);
                
                // Go through and send each metric record to ExtraHop
                for (let lbResourceId in lbValues) {
                    let lbResource = lbValues[lbResourceId];
                    // Only send if there are associated metrics
                    if (Object.keys(lbResource.metrics).length) {
                        extrahopODC.set("azure-lb-metrics-" + lbResource.name, JSON.stringify(lbResource), 60, function (err) {
                            if (err) {
                                context.log.error("ExtraHop ODC set error: " + err);
                            } else {
                                context.log.verbose("LB Metric Values Sent: " + JSON.stringify(lbResource));
                                // TODO will be removed
                                // test getting the value back
                                extrahopODC.get("azure-lb-metrics-" + lbResource.name, function (err, data) {
                                    if (err) {
                                        context.log.error("ExtraHop ODC get error: " + err + " data: " + data);
                                    } else {
                                        context.log.verbose("Got the value back: " + JSON.stringify(data));
                                    }
                                    
                                });
                                // TODO will be removed
                            }
                        });
                    }
                }

            }).catch(err =>
                // sendExtraHopODC error
                context.log.error("sendExtraHopODC " + err)
            );
        }).catch(err =>
            // getAllResources error
            context.log.error("getAllResources " + err)
        );
    }).catch(err =>
        // msiLogin error
        context.log.error("msiLogin " + err)
    );

    context.done();
};

