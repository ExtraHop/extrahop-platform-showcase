var express = require('express'),
    bodyParser = require('body-parser'),
    request = require('request');

// API access variables
// If using backend instead of accessing ExtraHop directly from client-side,
// populate the following fields:
var EXTRAHOP_HOST = '',
    EXTRAHOP_API_KEY = '',

// Create express app to serve example
app = express();
// and expose public directory
app.use(express.static(__dirname + '/public'));
// parse application/json
app.use(bodyParser.json());

// Serve index.html if accessing root
app.get('/', function(req, res) {
    res.sendFile('index.html');
});

// Create a proxy for the ExtraHop's metrics/total v1 API endpoint
app.post('/api/v1/metrics/total', function(req, res, next) {
    // Ensure that authorization key either exists on client-side or specified here,
    // otherwise abort request
    var authKey = '';
    if (EXTRAHOP_API_KEY) {
        authKey = 'ExtraHop apikey=' + EXTRAHOP_API_KEY;
    } else if (req.headers.authorization) {
        authKey = req.headers.authorization;
    } else {
        res.status(400).send('Error, no authorization header found!');
        return next();
    }

    // Configure base options when performing an ExtraHop API POST request via Node's 'request' module
    var options = {
        method: 'POST',
        url: 'https://' + EXTRAHOP_HOST + '/api/v1/metrics/total',
        rejectUnauthorized: false, // Only needed if the extrahop does not have a Valid SSL cert
        headers: {
            accept: 'application/json',
            authorization: authKey
        },
        body: {}, // To be populated by individual requests
        json: true
    };

    // Proxy the body through from the original POST
    options.body = req.body;

    request(options, function(error, response, body) {
        if (error) {
            res.status(500).send(error);
        } else {
            res.json(body);
        }
    });
});


// Finally, start the express server
var PORT = 3030;
app.listen(PORT, function() {
    console.log('Running simple web server at: http://localhost:' + PORT);
});
