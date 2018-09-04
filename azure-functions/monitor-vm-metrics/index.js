// Time-stamp: <2018-08-08 15:42:15 (dtucholski)>
//
// Description: Retrieve Virtual Machine metrics and send them to your ExtraHop
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
    const resourceType = 'Microsoft.Compute/virtualMachines';
    const metricList = {
        'CPU Credits Consumed': 'average', // TODO should be average?
        'CPU Credits Remaining': 'average',
        'Disk Read Bytes': 'total',
        'Disk Write Bytes': 'total',
        'Disk Read Operations/Sec' : 'average',
        'Disk Write Operations/Sec': 'average',
        'Network In': 'total',
        'Network Out': 'total',
        'Percentage CPU': 'maximum',
    };
    // Pull in the environment vars
    const subscriptionId = process.env["AZURE_SUBSCRIPTION_ID"];
    const extrahopIp = process.env["EXTRAHOP_IP"];
    const extrahopPort = process.env["EXTRAHOP_PORT"];

    let vmValues = {};

    // Login using Managed Service Identity
    msRestAzure.loginWithAppServiceMSI().then(function msiLogin (credentials) {
        
        // Setup azure monitor client
        let monitorClient = new monitorManagement(credentials, subscriptionId);
        
        // Pull 1 minute metrics for only the last 5 minutes
        let endDate = new Date();
        let startDate = new Date(endDate.getTime() - 300000); // 5 mins * 60,000 ms
        let metricTimespan = startDate.toISOString() + "/" + endDate.toISOString();
        let metricInterval = moment.duration(1, 'minutes');
        let metricAggregation = 'average, maximum, total';
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
        let resourceGetByIdApiVersion = "2018-06-01";

        // Get list of all tagged resources
        resourceClient.resources.list(resourceOptions).then(function getAllResources (allResources) {

            // Filter by resource type
            let vmResources = allResources.filter(resource =>
                resource.type === resourceType
            );
            context.log.info("Found " + vmResources.length + " resource(s) of type: " + resourceType + ", tagged: " + tagName);

            // Return array of promises to asynchronously pull VM resource details and metrics 
            let vmPromises = vmResources.map(resource => {
                // get full VM Resource
                return resourceClient.resources.getById(resource.id, resourceGetByIdApiVersion).then(function getResource (vmResource) {

                    let networkInterfaceId = vmResource.properties.networkProfile.networkInterfaces[0].id;
                    // Get Network Interface details
                    return resourceClient.resources.getById(networkInterfaceId, resourceGetByIdApiVersion).then(function getNetworkDetails (networkInterface) {

                        let ipAddr = networkInterface.properties.ipConfigurations[0].properties.privateIPAddress;
                        let macAddr = networkInterface.properties.macAddress;

                        // We require an IP and MAC address
                        if (ipAddr && macAddr) {
                            
                            vmValues[resource.id] = {};
                            vmValues[resource.id]["IPAddr"] = ipAddr;
                            vmValues[resource.id]["HWAddr"] = macAddr;
                            
                            // Get Azure Monitoring Metrics
                            return monitorClient.metrics.list(resource.id, metricOptions).then(function getMetrics (metrics) {
                                //context.log.verbose("Metrics: " + inspectObj(metrics));
                                vmValues[resource.id]['region'] = metrics.resourceregion;
                                // Populate metric values
                                vmValues[resource.id]["metrics"] = {};
                                metrics.value.forEach(metric => {
                                    let metricName = metric.name.value;
                                    // Gracefully handle missing metric values
                                    if (metric.timeseries.length && metric.timeseries[0].hasOwnProperty('data')) {
                                        let metricData = metric.timeseries[0].data;
                                        let aggregationType = metricList[metricName];

                                        if (metricData[metricData.length - 2] && metricData[metricData.length - 2].hasOwnProperty(aggregationType)) {
                                            // Use the second most recent item since the most recent seems to usually be empty
                                            vmValues[resource.id]["metrics"][metricName] = metricData[metricData.length - 2][aggregationType];
                                        } 
                                    }
                                });
                            }).catch(err =>
                                // getMetrics error
                                context.log.error("getMetrics " + err)
                            );

                        } else {
                            context.log.warn("Skipping resource since both IP and MAC were not found for " + resource.id);
                        }
                    }).catch(err =>
                        // getNetworkDetails error
                        context.log.error("getNetworkDetails " + err)
                    );
                }).catch(err =>
                    // getResource error
                    context.log.error("getResource " + err)
                );
            });

            // After all VM resource details and metrics have been populated send metrics object to ExtraHop over ODC
            Promise.all(vmPromises).then(function sendExtraHopODC () {
                // Configure ExtraHop ODC 
                let memcacheServer = extrahopIp + ":" + extrahopPort;
                let extrahopODC = new memcached(memcacheServer);
                
                // Go through and send each metric record to ExtraHop
                for (let vmResourceId in vmValues) {
                    let vmResource = vmValues[vmResourceId];
                    // Only send if there are associated metrics
                    if (Object.keys(vmResource.metrics).length) {
                        extrahopODC.set("azure-vm-metrics-" + vmResource.HWAddr, JSON.stringify(vmResource), 60, function (err) {
                            if (err) {
                                context.log.error("ExtraHop ODC set error: " + err);
                            } else {
                                context.log.verbose("VM Metric Values Sent: " + JSON.stringify(vmResource));
                                // TODO will be removed
                                // test getting the value back
                                extrahopODC.get("azure-vm-metrics-" + vmResource.HWAddr, function (err, data) {
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

