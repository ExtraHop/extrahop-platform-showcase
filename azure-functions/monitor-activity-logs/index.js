// Time-stamp: <2018-09-18 10:52:32 (dtucholski)>
//
// Description: Retrieve Activity Logs and send them to your ExtraHop
// Author(s): Dan Tucholski and ExtraHop Networks

///////////////////////////////////////////////////////////////////////////////
// This file is part of an ExtraHop Supported Bundle.  Make NO MODIFICATIONS //
///////////////////////////////////////////////////////////////////////////////

const util = require('util');
const msRestAzure = require('ms-rest-azure');
const monitorManagement = require('azure-arm-monitor');
const memcached = require('memcached');

module.exports = function (context, myTimer) {

    // Pull in the environment vars
    const subscriptionId = process.env["AZURE_SUBSCRIPTION_ID"];
    const extrahopIp = process.env["EXTRAHOP_IP"];
    const extrahopPort = process.env["EXTRAHOP_PORT"];

    let activityLogRecords = {};

    // Login using Managed Service Identity
    msRestAzure.loginWithAppServiceMSI().then(function msiLogin (credentials) {
        
        // Setup azure monitor client
        let monitorClient = new monitorManagement(credentials, subscriptionId);
        
        // Pull logs for 1 minute from 5 minutes ago
        let now = new Date().getTime();
        let startDate = new Date(Math.floor((now - 300000) / 60000) * 60000); // 1 minute = 60,000 ms
        let endDate = new Date(startDate.getTime() + 60000); // 1 minute = 60,000 ms
        let logFilter = "eventTimestamp ge '" + startDate.toISOString() + "' and eventTimestamp le '" +  endDate.toISOString() + "'";

        // Configure metric options
        let activityLogOptions = {
            filter: logFilter
        };

        // Get Azure Activity Logs
        monitorClient.activityLogs.list(activityLogOptions).then(function getActivityLogs (activityLogs) {
            activityLogs.forEach(log => {
                let logRecord = {};
                // Required record fields
                try {
                    logRecord = {
                        category: log.category.value,
                        correlationId: log.correlationId,
                        eventTimestamp: log.eventTimestamp,
                        id: log.id, 
                        level: log.level,
                        operationName: log.operationName.value,
                        resourceId: log.resourceId,
                        status: log.status.localizedValue,
                        subscriptionId: log.subscriptionId
                    };
                } catch (err) {
                    context.log.warn("Activity log event did not contain all required fields, skipping record");
                    return; // go to next iteration
                }

                // Additional optional record fields for all categories
                if (log.hasOwnProperty('eventName') && log.eventName.localizedValue) {
                    logRecord['eventName'] = log.eventName.localizedValue;
                } 
                // Only capture if different than status
                if (log.hasOwnProperty('subStatus') && log.subStatus.localizedValue && log.subStatus.localizedValue != log.status) {
                    logRecord['subStatus'] = log.subStatus.localizedValue;
                } 
                if (log.operationName.localizedValue != log.operationName.value) {
                    logRecord['operationDetail'] = log.operationName.localizedValue;
                }
                if (log.hasOwnProperty('description') && log.description) {
                    logRecord['description'] = log.description;
                }
                if (log.hasOwnProperty('tenantId')) {
                    logRecord['tenantId'] = log.tenantId;
                }
                // Store entire properties object
                if (log.hasOwnProperty('properties')) {
                    logRecord['properties'] = log.properties;
                }

                // Category specific optional record fields
                switch(logRecord.category){
                    case 'Administrative':
                        if (log.hasOwnProperty('caller')) {
                            logRecord['caller'] = log.caller;
                        }
                        if (log.hasOwnProperty('claims') && log.claims.hasOwnProperty('name')) {
                            logRecord['callerName'] = log.claims.name;
                        }
                        if (log.hasOwnProperty('claims') && log.claims.hasOwnProperty('ipaddr')) {
                            logRecord['callerIp'] = log.claims.ipaddr;
                        }
                        if (log.hasOwnProperty('httpRequest')) {
                            if (log.httpRequest.hasOwnProperty('method')) {
                                logRecord['httpMethod'] = log.httpRequest.method;
                            }
                            // Preferred ipaddr if present 
                            if (log.httpRequest.hasOwnProperty('clientIpAddress')) {
                                logRecord['callerIp'] = log.httpRequest.clientIpAddress;
                            }
                        }
                        if (log.hasOwnProperty('properties')) {
                            // Only store status message for errors
                            if (log.properties.hasOwnProperty('statusMessage') && log.properties.statusMessage.includes('error')) {
                                try {
                                    let statusMessageJson = JSON.parse(log.properties.statusMessage);
                                    if (statusMessageJson.hasOwnProperty('error')) {
                                        // Store basic error code and message
                                        if (statusMessageJson.error.hasOwnProperty('code')) {
                                            logRecord['errorCode'] = statusMessageJson.error.code;
                                        }
                                        if (statusMessageJson.error.hasOwnProperty('message')) {
                                            logRecord['errorMessage'] = statusMessageJson.error.message;
                                        }
                                        // Try to override with more granular details
                                        if (statusMessageJson.error.hasOwnProperty('details') && statusMessageJson.error.details.length) {
                                            if (statusMessageJson.error.details[0].hasOwnProperty('code')) {
                                                logRecord['errorCode'] = statusMessageJson.error.details[0].code;
                                            }
                                            if (statusMessageJson.error.details[0].hasOwnProperty('message')) {
                                                logRecord['errorMessage'] = statusMessageJson.error.details[0].message;
                                            }
                                        }
                                        
                                    }
                                } catch (err) {
                                    context.log.warn("Unable to pull error code/message out of the statusMessage");
                                }
                            }
                        }
                        break;
                    case 'ServiceHealth':
                        if (log.hasOwnProperty('properties')) {
                            if (log.properties.hasOwnProperty('title')) {
                                logRecord['eventName'] = log.properties.title;
                            }
                            if (log.properties.hasOwnProperty('communication')) {
                                logRecord['description'] = log.properties.communication;
                            }
                            if (log.properties.hasOwnProperty('stage') && log.properties.stage != log.status) {
                                logRecord['subStatus'] = log.properties.stage;
                            }
                            if (log.properties.hasOwnProperty('incidentType')) {
                                logRecord['incidentType'] = log.properties.incidentType;
                            }
                            if (log.properties.hasOwnProperty('service')) {
                                logRecord['impactedService'] = log.properties.service;
                            }
                            if (log.properties.hasOwnProperty('region')) {
                                logRecord['impactedRegion'] = log.properties.region;
                            }
                        }
                        break;
                    case 'Alert':
                        if (log.hasOwnProperty('caller')) {
                            logRecord['caller'] = log.caller;
                        }
                        if (log.hasOwnProperty('properties')) {
                            if (log.properties.hasOwnProperty('ruleUri')) {
                                logRecord['caller'] = log.properties.ruleUri;
                            }
                        }
                        break;
                    case 'Autoscale':
                        if (log.hasOwnProperty('caller')) {
                            logRecord['caller'] = log.caller;
                        }
                        if (log.hasOwnProperty('properties')) {
                            if (log.properties.hasOwnProperty('description') && !log.description) {
                                logRecord['description'] = log.properties.description;
                            }
                        }
                        break;
                    case 'Security':
                        if (log.hasOwnProperty('properties')) {
                            if (log.properties.hasOwnProperty('severity')) {
                                logRecord['severity'] = log.properties.severity;
                            }
                            if (log.properties.hasOwnProperty('actionTaken')) {
                                logRecord['actionTaken'] = log.properties.actionTaken;
                            }
                            if (log.properties.hasOwnProperty('userName')) {
                                logRecord['caller'] = log.properties.userName;
                            }
                        }
                        break;
                    case 'Recommendation':
                        if (log.hasOwnProperty('properties')) {
                            if (log.properties.hasOwnProperty('recommendationCategory')) {
                                logRecord['recommendationCategory'] = log.properties.recommendationCategory;
                            }
                            if (log.properties.hasOwnProperty('recommendationImpact')) {
                                logRecord['severity'] = log.properties.recommendationImpact;
                            }
                        }
                        break;
                    default:
                        context.log.warn("Unexpected Activity Log category: " + logRecord.category)
                }

                activityLogRecords[log.eventDataId] = logRecord;
            });

            context.log.info("Found " + Object.keys(activityLogRecords).length + " activity log(s)");
            // After all Activity Logs have been parsed send records to ExtraHop over ODC
            if (Object.keys(activityLogRecords).length) {
                try {
                    // Configure ExtraHop ODC 
                    let memcacheServer = extrahopIp + ":" + extrahopPort;
                    let extrahopODC = new memcached(memcacheServer);
                    
                    // Go through and send each activity log record to ExtraHop
                    for (let logId in activityLogRecords) {
                        let logRecord = activityLogRecords[logId]
                        extrahopODC.set("azure-activity-log-" + logId, JSON.stringify(logRecord), 60, function (err) {
                            if (err) {
                                context.log.error("ExtraHop ODC set error: " + err);
                            } else {
                                context.log.verbose("Activity Log Record Sent: " + JSON.stringify(logRecord));
                            }
                        });
                    }
                } catch (err) {
                    // sendExtraHopODC error
                    context.log.error("sendExtraHopODC " + err);
                }
            }
        }).catch(err =>
            // getMetrics error
            context.log.error("getActivityLogs " + err)
        );
    }).catch(err =>
        // msiLogin error
        context.log.error("msiLogin " + err)
    );

    context.done();
};

