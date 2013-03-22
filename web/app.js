var config = require('./config'),
    auth = require('./auth'),
    express = require('express'),
    passport = require('passport'),

    app = express();

app.use(passport.initialize());
app.use(express.static(__dirname + '/public'));
app.use(express.bodyParser());
app.use(app.router);

auth.setupRoutes(app);

app.listen(config.port);
console.log('Listening on port ' + config.port);

app.use(function(err, req, res, next) {
	console.error(err.stack);
	res.send(500, 'Aww nuuuu! Something went wrong!');
});