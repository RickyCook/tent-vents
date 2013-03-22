var config = require('./config'),
    resource = require('express-resource'),
    passport = require('passport'),
    LocalStrategy = require('passport-local').Strategy;



/*****************
 * Help
 */
function ensureAuthenticated(req, res, next) {
  if (req.isAuthenticated()) { return next(); }
  res.redirect('/');
}

/*****************
 * Setup
 */

passport.use(new LocalStrategy({
    usernameField: 'email',
  },
  function(email, password, done) {
    return done(null, { email: email });
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
    return res.send(200, req.user)
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