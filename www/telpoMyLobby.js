var exec = require('cordova/exec');

var telpoMyLobby = function () {}
telpoMyLobby.prototype = {
    getTempData: function (success, error) {
   	 exec(success, error, 'telpoMyLobby', 'getTempData', []);
	},
}

var plugin = new telpoMyLobby()
module.exports = plugin;
