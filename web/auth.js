var config = require('./config'),
    resource = require('express-resource'),
    nstore = require('nstore'),
    passport = require('passport'),
    LocalStrategy = require('passport-local').Strategy

    dbUsers = nstore.new(config.db.users);

/*****************
* Setup
*/

passport.use(new LocalStrategy({
		usernameField: 'email',
	},
	function(email, password, done) {
		var user;
		dbUsers.get(email, function(err, doc, key) {
			if (err) {
				if (err.message.match(/^Document does not exist for/)) {
					if (config.registration) {
						user = { password: password, devices: [] };
						dbUsers.save(email, user, function(err) {
							if (err) return done(err);
							return done(null, user)
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

			if (password != doc.password)
				return done(null, false, { message: 'Password was incorrect' });;
			return done(null, doc);
		});
	}
));

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

/*****************
* Exports
*/
module.exports = {
	ensureAuthenticated: ensureAuthenticated,
	setupRoutes: setupRoutes
}