# ExtraHop REST API and Highcharts POC

## Overview
This project aims to give a simple example of how the [Extrahop REST API](https://docs.extrahop.com/current/rest-api-guide/) can be used with a charting library like [Highcharts](http://www.highcharts.com/) to generate graphs similar to those displayed in the ExtraHop UI.  For this example, two horizontal bar charts are generated:

* HTTP Responses by Status Code
* HTTP Requests by Server (Host if available)

The project uses a lightweight [Express](http://expressjs.com/) backend to proxy the API requests between the frontend and the ExtraHop REST API.  Alternatively, the frontend could query the REST API directly as long as whatever site was hosting the frontend was added as a 'trusted origin' on the ExtraHop (**Note**: the 'setup' account is required to add a new 'trusted origin'). To do this, populate the `EXTRAHOP_HOST` field inside the main.js (see Getting started below).

## Requirements
Project requires [Node.js](https://nodejs.org) (>=0.10.30) to be installed on the host system.  If running purely client-side, no system requirements.

## Getting started
To get started, you'll need to populate **3** fields before you can run this example.
> If you've added your host as a 'trusted origin' and want to run this example entirely on the client-side, simply edit the main.js file below and run the public folder from your host.  Otherwise, edit the app.js file below and then add your application OID to main.js.

***/app.js***  
Fields:
* `EXTRAHOP_HOST` - Hostname or IP of the ExtraHop you wish to connect to (**Populate if using Express backend**)
* `EXTRAHOP_API_KEY` - API Key used to access the REST API (**Populate if using Express backend**)

***/public/js/main.js***  
Fields:
* `APPLICATION_OID` - Application or device id metric source to be used with generated charts
* `EXTRAHOP_API_KEY` - API Key used to access the REST API (**Populate if using client-side only**)
* `EXTRAHOP_HOST` - Hostname or IP of the ExtraHop you wish to connect to (**Populate if using client-side only**)

Install node modules and run Express app (if want to use the express backend):

```
> npm install
> node app.js
```
and navigate to localhost at the port specified in the output. (example: http://localhost:3030)

## Project Layout
```
app.js - Express app used to proxy requests to the ExtraHop REST API
package.json - Node required npm packages for minimal backend
/public
  index.html - Single page html used for charts
  /js
    main.js - Client-side javascript which handles ajax requests and chart generation
    /vendor - Vendor required javascript libraries
```
