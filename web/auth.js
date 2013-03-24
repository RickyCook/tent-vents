var config = require('./config'),
    resource = require('express-resource'),
    nstore = require('nstore'),
    passport = require('passport'),
    LocalStrategy = require('passport-local').Strategy,
    uuid = require('node-uuid'),

    $ = require('jquery'),

    dbUsers = nstore.new(config.db.users),
    dbKeys = nstore.new(config.db.keys);

/*****************
* Setup
*/

passport.use(new LocalStrategy({
		usernameField: 'email',
	},
	function(email, password, done) {
		var user, currentKey
		    cryptPassword = encryptPassword(password);
		dbUsers.get(email, function(err, doc, key) {
			if (err) {
				if (err.message.match(/^Document does not exist for/)) {
					if (config.registration) {
						user = { cryptPassword: cryptPassword, devices: [] };
						dbUsers.save(email, user, function(err) {
							if (err) return done(err);
							return doLogin(email, user, done);
						});
					} else {
						return done(null, false, {
							message: 'User ' + email +
								' does not exist and registration is disabled'
						});
					}
				} else {
					return done(err);
				}
			}

			if ($.type(doc.cryptPassword) != 'string' && $.type(doc.password) == 'string') {
				doc.cryptPassword = encryptPassword(doc.password);
				delete doc.password;
				dbUsers.save(email, doc, function (err) {
					if (err) return console.log(err);
					console.log('Updated to cryptPassword for email ' + email);
				});
			}
			if (cryptPassword != doc.cryptPassword)
				return done(null, false, { message: 'Password was incorrect' });

			return doLogin(email, doc, done);
		});
	}
));
function doLogin(email, user, done) {
	var currentKey = uuid.v4();
	dbKeys.save(currentKey, email, function(err) {
		if (err) return done(err);
		user.currentKey = currentKey;
		return done(null, {
			email: email,
			key: currentKey,
		});
	});
}

passport.serializeUser(function(user, done) {
	done(null, user);
});

passport.deserializeUser(function(obj, done) {
	done(null, obj);
});

function setupRoutes(app) {
	/*****************
	* All
	*/
	authOkay = function(req, res) {
		return res.send(200, req.user);
	}
	app.post('/auth/local.json',
		passport.authenticate('local', { session:false }),
		authOkay
	);
	app.post('/logout.json',
		function(req, res) {
			req.logout()
			return res.send(200, { okay: true });
		}
	);

}
function getPasswordCrypt() {
	return config.passwords.cryptoFunc
		.apply(null, config.passwords.cryptoArgs);
}
function encryptPassword(password) {
	var i;
	for (i = 0; i < config.passwords.passes; i++) {
		password = getPasswordCrypt().update(password).digest('hex');
	}
	return password;
}
/*****************
* Exports
*/
module.exports = {
	setupRoutes: setupRoutes,
	getPasswordCrypt: getPasswordCrypt,
	encryptPassword: encryptPassword
}