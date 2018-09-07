// Time-stamp: <2018-09-07 14:43:52 (dtucholski)>
//
// Description: Retrieve Storage Account metrics and send them to your ExtraHop
// Author(s): Dan Tucholski and ExtraHop Networks

///////////////////////////////////////////////////////////////////////////////
// This file is part of an ExtraHop Supported Bundle.  Make NO MODIFICATIONS //
///////////////////////////////////////////////////////////////////////////////

const util = require('util');
const msRestAzure = require('ms-rest-azure');
const monitorManagement = require('azure-arm-monitor');
const resourceManagement = require('azure-arm-resource');
const storageManagement = require('azure-arm-storage');
const moment = require('moment');
const memcached = require('memcached');

// Helper function to parse the resource group from a resource id
function parseResourceGroup(resourceId) {
    let prefix = 'resourceGroups/';
    let prefixIndex = resourceId.indexOf(prefix) + prefix.length;
    let suffix = '/providers';
    let suffixIndex = resourceId.indexOf(suffix);
    return resourceId.substring(prefixIndex, suffixIndex);
}

module.exports = function (context, myTimer) {

    const tagName = 'extrahop-azure-integration';
    const resourceType = 'Microsoft.Storage/storageAccounts';
    const metricList = {
        'Availability': 'average',
        'Egress': 'total',
        'Ingress': 'total',
        'SuccessE2ELatency': 'maximum',
        'SuccessServerLatency': 'maximum',
        'Transactions': 'total'
    };
    
    // Pull in the environment vars
    const subscriptionId = process.env["AZURE_SUBSCRIPTION_ID"];
    const extrahopIp = process.env["EXTRAHOP_IP"];
    const extrahopPort = process.env["EXTRAHOP_PORT"];

    let saValues = {};

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
        let metricFilter = "APIName eq '*'";
        
        // Configure metric options
        let metricOptions = {
            metricnames: metricNames,
            timespan: metricTimespan,
            interval: metricInterval,
            aggregation: metricAggregation,
            filter: metricFilter
        };

        let storageClient = new storageManagement(credentials, subscriptionId);

        // Setup azure resource client
        let resourceClient = new resourceManagement.ResourceManagementClient(credentials, subscriptionId);
        let resourceOptions = {
            // Only one filter at a time appears to work
            filter: `tagname eq '${tagName}'`
        };

        // Get list of all tagged resources
        resourceClient.resources.list(resourceOptions).then(function getAllResources (allResources) {

            // Filter by resource type
            let saResources = allResources.filter(resource =>
                resource.type === resourceType
            );
            context.log.info("Found " + saResources.length + " resource(s) of type: " + resourceType + ", tagged: " + tagName);

            // Return array of promises to asynchronously pull SA resource details and metrics 
            let saPromises = saResources.map(resource => {
                            
                saValues[resource.id] = {};
                saValues[resource.id]["name"] = resource.name;
                            
                // Get Azure Monitoring Metrics
                return monitorClient.metrics.list(resource.id, metricOptions).then(function getSAMetrics (metrics) {
                    saValues[resource.id]['region'] = metrics.resourceregion;
                    // Populate metric values
                    saValues[resource.id]["metrics"] = {};
                    metrics.value.forEach(metric => {
                        let metricName = metric.name.value;
                        // Go through by API name      
                        metric.timeseries.forEach(metricByAPI => {
                            // Gracefully handle missing metric values
                            if (metricByAPI.hasOwnProperty('metadatavalues') && metricByAPI.hasOwnProperty('data')) {
                                let apiName = metricByAPI.metadatavalues[0].value;
                                let metricData = metricByAPI.data;
                                let aggregationType = metricList[metricName];

                                if (metricData[metricData.length - 2] && metricData[metricData.length - 2].hasOwnProperty(aggregationType)) {
                                    // Create metric name object the first time that we know that we have values
                                    if (!saValues[resource.id]["metrics"].hasOwnProperty(metricName)) {
                                        saValues[resource.id]["metrics"][metricName] = {total: 0};
                                    }
                                    // Use the second most recent item since the most recent seems to usually be empty
                                    saValues[resource.id]["metrics"][metricName][apiName] = metricData[metricData.length - 2][aggregationType];
                                    // Keep track of totals
                                    saValues[resource.id]["metrics"][metricName]["total"] += metricData[metricData.length - 2][aggregationType];
                                }
                            }
                            
                        });
                    });

                }).catch(err =>
                    // getSAMetrics error
                    context.log.error("getSAMetrics " + err)
                );

            });

            // After all SA resource metrics have been populated now pull by Service metrics
            Promise.all(saPromises).then(function getServiceMetrics () {
                // Create array to hold an array of 4 promises for each SA
                let saServicePromises = [];
                for (let resourceId in saValues) {
                    // Only get service metrics if there are storage account metrics
                    if (Object.keys(saValues[resourceId].metrics).length) { 
                        saValues[resourceId]["services"] = {};
                        
                        let serviceEndpoints = [
                            {
                                'endpoint': '/blobServices/default',
                                'service': 'blob'
                            },
                            {
                                'endpoint': '/tableServices/default',
                                'service': 'table'
                            },
                            {
                                'endpoint': '/queueServices/default',
                                'service': 'queue'
                            },
                            {
                                'endpoint': '/fileServices/default',
                                'service': 'file'
                            }
                        ];

                        // Create and push an array of 4 promises for each SA to asynchronously pull SA Service metrics 
                        saServicePromises.push(
                            serviceEndpoints.map(serviceEndpoint => {
                                let serviceId = resourceId + serviceEndpoint.endpoint;
                                saValues[resourceId]["services"][serviceEndpoint.service] = {};
                                let saServiceValues = saValues[resourceId]["services"][serviceEndpoint.service];
                                
                                return monitorClient.metrics.list(serviceId, metricOptions).then(function getSpecificServiceMetrics (serviceMetrics) { 
                                    // Populate service metric values
                                    saServiceValues["metrics"] = {};
                                    serviceMetrics.value.forEach(metric => {
                                        let metricName = metric.name.value;
                                        // Go through by API name      
                                        metric.timeseries.forEach(metricByAPI => {
                                            // Gracefully handle missing metric values
                                            if (metricByAPI.hasOwnProperty('metadatavalues') && metricByAPI.hasOwnProperty('data')) {
                                                let apiName = metricByAPI.metadatavalues[0].value;
                                                let metricData = metricByAPI.data;
                                                let aggregationType = metricList[metricName];

                                                if (metricData[metricData.length - 2] && metricData[metricData.length - 2].hasOwnProperty(aggregationType)) {
                                                    // Create metric name object the first time that we know that we have values
                                                    if (!saServiceValues["metrics"].hasOwnProperty(metricName)) {
                                                        saServiceValues["metrics"][metricName] = {total: 0};
                                                    }
                                                    // Use the second most recent item since the most recent seems to usually be empty
                                                    saServiceValues["metrics"][metricName][apiName] = metricData[metricData.length - 2][aggregationType];
                                                    // Keep track of totals
                                                    saServiceValues["metrics"][metricName]["total"] += metricData[metricData.length - 2][aggregationType];
                                                }
                                            }
                                            
                                        });
                                    });

                                    // Pull additional container information for the blob service 
                                    if (serviceEndpoint.service === 'blob') {
                                        // Get blob container details
                                        return storageClient.blobContainers.list(parseResourceGroup(resourceId), saValues[resourceId].name).then(function getBlobContainers(containers) {
                                            saServiceValues["containers"] = [];
                                            containers.value.forEach(container => {
                                                let containerDetails = {
                                                    'name': container.name,
                                                    'public':  container.publicAccess === 'None' ? false : true
                                                };
                                                saServiceValues["containers"].push(containerDetails);
                                            });
                                        }).catch(err =>
                                            // getBlobContainers error
                                            context.log.error("getBlobContainers " + err)
                                        );
                                    }

                                }).catch(err =>
                                    // getSpecificServiceMetrics error
                                    context.log.error("getSpecificServiceMetrics " + err)
                                );

                            })
                        );
                    }
                }

                // After all nested SA Service metric promises have been resolved then send metrics object to ExtraHop over ODC
                Promise.all(saServicePromises.map(innerPromises => 
                    Promise.all(innerPromises)
                )).then(function sendExtraHopODC () { 
                    // Configure ExtraHop ODC 
                    let memcacheServer = extrahopIp + ":" + extrahopPort;
                    let extrahopODC = new memcached(memcacheServer);

                    // Go through and send each metric record to ExtraHop
                    for (let saResourceId in saValues) {
                        let saResource = saValues[saResourceId];
                        // Only send if there are associated metrics
                        if (Object.keys(saResource.metrics).length) {
                            extrahopODC.set("azure-sa-metrics-" + saResource.name, JSON.stringify(saResource), 60, function (err) {
                                if (err) {
                                    context.log.error("ExtraHop ODC set error: " + err);
                                } else {
                                    context.log.verbose("SA Metric Values Sent: " + JSON.stringify(saResource));
                                }
                            });
                        }
                    }
                }).catch(err =>
                    // sendExtraHopODC error
                    context.log.error("sendExtraHopODC " + err)
                );

            }).catch(err =>
                // getServiceMetrics error
                context.log.error("getServiceMetrics " + err)
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

