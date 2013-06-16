var mongoose = require('mongoose');
mongoose.connect('mongodb://localhost/test');

var schema = new mongoose.Schema({ 
			name : String,
			type : String,
			thumb : String,
			path : String,
			device : String,
			location : String,
			time: { type: Date, default: Date.now }
});

var filesDB = mongoose.model('files',schema);

exports.addFile = function(name,type,device,location,thumb,path) {
    console.log('Adding file: ' + name+" : "+type+" : "+device+" : "+location+" :"+thumb+" : "+path);
    new filesDB(
				{ 	
					name:name,
					type:type,
					device:device,
					location:location,
					thumb:thumb,
					path:path
				}).save(function (err, file) {
			if (err) { // TODO handle the error
				console.log("####### Add file failed #######");
			} else {
				console.log("####### Add file SUCCESS ####### " +file);
			}
	});
}

exports.getAll = function(callback) {
    filesDB.find().exec(function(err, result) { 
	  if (!err) { 
	    // handle result
	    console.log("####### GET All SUCCESS #######"+JSON.stringify(result));
	    callback(null,JSON.stringify(result));
	    
	  } else {
	    // error handling
	    console.log("####### GET All ERROR #######");
	    callback(err,null);
	  };
	});
}


exports.getImages = function(callback) {
    filesDB.find({type:'image'}).exec(function(err, result) { 
	  if (!err) { 
	    // handle result
	    console.log("####### GET Images SUCCESS #######"+JSON.stringify(result));
	    callback(null,JSON.stringify(result));
	  } else {
	    // error handling
	    console.log("####### GET Images ERROR #######");
	    callback(err,null);
	  };
	});
}

exports.getVideos = function(callback) {
    filesDB.find({type:'video'}).exec(function(err, result) { 
	  if (!err) { 
	    // handle result
	    console.log("####### GET Videos SUCCESS #######"+JSON.stringify(result));
	    callback(null,JSON.stringify(result));
	  } else {
	    // error handling
	    console.log("####### GET Videos ERROR #######");
	    callback(err,null);
	  };
	});

}

exports.getAudios = function(callback) {
	filesDB.find({type:'audio'}).exec(function(err, result) { 
	  if (!err) { 
	    // handle result
	    console.log("####### GET Audio SUCCESS #######"+JSON.stringify(result));
	    callback(null,JSON.stringify(result));
	  } else {
	    // error handling
	    console.log("####### GET Audio ERROR #######");
	    callback(err,null);
	  };
	});
}


exports.getLatest = function(callback) {
	var options = {
    	"limit": 10
	}

	filesDB.find().limit(10).exec(function(err, result) { 
	  if (!err) { 
	    // handle result
	    console.log("####### GET getLatest SUCCESS #######"+JSON.stringify(result));
	    callback(null,JSON.stringify(result));
	  } else {
	    // error handling
	    console.log("####### GET getLatest ERROR #######");
	    callback(err,null);
	  };
	});

}