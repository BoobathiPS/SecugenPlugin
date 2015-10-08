var exec = require('cordova/exec');

var BiometricsExport = {};

BiometricsExport.capturePrint =  function(successCallBack, errorCallBack, args){
	exec(successCallBack, errorCallBack, "SecugenPlugin", "capturePrint",args);
};

BiometricsExport.verifyPrint = function(successCallBack,errorCallBack,args){
	exec(successCallBack,errorCallBack,"SecugenPlugin","verifyPrint",args);
}

module.exports = BiometricsExport;