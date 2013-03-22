module.exports = {
	port: 80,
	baseURL: 'http://localhost',
	db: {
		users: 'users.db',
		keys: 'keys.db'
	},
	registration: true,
	passwords: {
		cryptoFunc: require('crypto').createHmac,
		cryptoArgs: ['sha1', 'changeme'],
		passes: 1000
	}
}