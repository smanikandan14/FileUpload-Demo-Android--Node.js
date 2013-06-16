var formidable = require('formidable'),
    http = require('http'),
    https = require('https'),
    util = require('util'),
    express = require('express'),
    app = express(),
    fs = require('fs');


var options = {
  key: fs.readFileSync(__dirname +'/keys1/server.key'),
  cert: fs.readFileSync(__dirname +'/keys1/server.crt'),
  
  //passphrase: "myserver",
  // This is necessary only if using the client certificate authentication.
  requestCert: true,

  // This is necessary only if the client uses the self-signed certificate.
  ca: [ fs.readFileSync(__dirname +'/keys1/ca.crt') ],

  rejectUnauthorized: false

};

//var  server = http.createServer(app);
var  server = https.createServer(options,app);
server.listen(4000);

var    uploadDB = require('./fileDB'),
    thumbnail = require('node-thumbnail').thumb,
    easyimg = require('easyimage'),
    path = require('path'),
    vidthumbnail = require('video-thumb'),
     _ = require('underscore'),
    net = require('net'),
    os = require("os"),
    ffmpeg = require('fluent-ffmpeg');

var io = require('socket.io').listen(server);
var new_upload_socket = io.of("/new_upload");

var picextensions = [
  '.jpg',
  '.jpeg',
  '.JPG',
  '.JPEG',
  '.png',
  '.PNG'
];

var videoextensions = [
  '.mp4',
  '.avi',
  '.mov'
];

var audioxtensions = [
  '.mp3',
  '.wav',
  '.3gp'  
];

//
//var server = https.createServer(options, app); 

//var server = http.createServer(app);

//server.listen(4000);

app.configure(function(){
  app.set('views', __dirname + '/views'); //specifying template path
  app.set('view engine', 'ejs'); //specifying template engine
  app.engine('ejs', require('ejs-locals'));
  app.use(express.favicon());
  //app.use(connectTimeout({ time: 20000 }));
  app.use(express.methodOverride());
  app.use(express.static(__dirname + '/'));
});


function getNetworkIP(callback) {
  var socket = net.createConnection(80, 'www.google.com');
  socket.on('connect', function() {
    callback(undefined, socket.address().address);
    socket.end();
  });
  socket.on('error', function(e) {
    callback(e, 'error');
  });
}

app.get('/',function(req, res) {
    console.log(" ######### GET / ############ ");
    res.render('index');
});

app.post('/upload', function(req,res) {

    console.log(" ########## POST /uplaod ####### ");

    var form = new formidable.IncomingForm(),
        files = [],
        fields = [];

    var account;
    var type;
    var device;
    var location;
    var fileCreated;

    form.encoding = 'utf-8';
    form.uploadDir = __dirname+"/files";
    form.keepExtensions = true;

    console.log(" ########## POST /uplaod form ####### "+ form.uploadDir);
    form
      .on('fileBegin', function(field,file) {
            file.path = form.uploadDir +"/"+ file.name;
            console.log("###### file begin ::: file Path"+file.path);
      })    
      .on('progress', function(bytesReceived, bytesExpected) {
            console.log("###### BYTES RECEIVED EXCEPTED "+bytesReceived+" : "+bytesExpected);
      })
      .on('field', function(field, value) {
        console.log('############ on Field ######### '+field+" : "+value);
        if(field == 'name') {
            account = value;
        }

        if(field == 'type') {
            type = value;
        }

        if(field == 'device') {
            device = value;
        }

        if(field == 'location') {
            location = value;
        }

        fields.push([field, value]);
      })
      .on('file', function(field, file) {
        console.log('############ on File ######### '+field+" : "+file.path);
        files.push([field, file]);
        fileCreated = file;
      })
      .on('end', function() {
        console.log('-> upload done');
        console.log('############ on End ######### '+account+" : "+type);
        console.log('############ fileCreated ######### '+fileCreated.path);

        var ext = path.extname(fileCreated.path);   
        var base = path.basename(fileCreated.path, ext);

        console.log('############ ext base ######### '+ext+" : "+base);

        var srcPath = __dirname+'/files/'+fileCreated.name;
        var dstPath = __dirname+'/files/'+base+'_thumb'+ext;

        var fileUrl = '/files/'+fileCreated.name;
        var thumbfileUrl = '/files/'+base+'_thumb'+ext;

        if( _.indexOf(picextensions, ext) != -1 ) {
            console.log('############ Image found ######### ');
            // Create thumbnail
            easyimg.resize({src:srcPath, dst:dstPath, width:150, height:150}, function(err, image) {
                if (err) { 
                    throw err;
                }
                console.log('Resized');
                console.log(image);
                uploadDB.addFile(account,type,device,location,thumbfileUrl,fileUrl);
                console.log('############ Emitting on new socket ######### ');
                new_upload_socket.emit('update', { type:"image", path:fileUrl, name:account, location:location});
                console.log('All done!');

            });
        } else if( _.indexOf(videoextensions, ext) != -1 ) {
            dstPath = "";
            console.log('############ Video found ######### ');
            uploadDB.addFile(account,type,device,location,thumbfileUrl,fileUrl);
            new_upload_socket.emit('update', { type:"video", path:fileUrl});
        } else if( _.indexOf(audioxtensions, ext) != -1 ) {
            dstPath = "";            
            
            var dstAudioMp3Path = __dirname+'/files/'+base+'.mp3';
            var dbAudioMp3Path = '/files/'+base+'.mp3';
            console.log('############ Audio found ######### '+fileUrl+" : "+dstAudioMp3Path);
            var proc = new ffmpeg({ source: srcPath})
              .saveToFile(dstAudioMp3Path, function(stdout, stderr) {
                console.log('######## file has been converted succesfully #########'+stdout+" : "+stderr);
                uploadDB.addFile(account,type,device,location,thumbfileUrl,dbAudioMp3Path);
                new_upload_socket.emit('update', { type:"audio", path:dbAudioMp3Path});
              });
            
        }

        res.writeHead(200, {'content-type': 'text/plain'});
        res.write('received fields:\n\n '+util.inspect(fields));
        res.write('\n\n');
        res.end('received files:\n\n '+util.inspect(files));
      });
    form.parse(req);
});


app.get('/all', function(req,res){
    uploadDB.getAll(function(err,result) {
        if(err) {
          res.send([]);      
        } else {
          console.log('###### SENDING ALL RESULT ######## ');
          res.send(result);
        }
    });
});


app.get('/images', function(req,res){
    uploadDB.getImages(function(err,result) {
        if(err) {
          res.send([]);      
        } else {
          console.log('###### SENDING IMAGES RESULT ######## '+result);
          res.send(result);
        }
    });
});


app.get('/videos', function(req,res){
    uploadDB.getVideos(function(err,result) {
        if(err) {
          res.send([]);      
        } else {
          console.log('###### SENDING VIDEOS RESULT ######## '+result);
          res.send(result);
        }
    });
});

app.get('/audios', function(req,res){
    uploadDB.getAudios(function(err,result) {
        if(err) {
          res.send([]);      
        } else {
          console.log('###### SENDING AUDIO RESULT ######## '+result);
          res.send(result);
        }
    });
});

app.get('/latest_uploads', function(req,res){
    uploadDB.getLatest(function(err,result) {
        if(err) {
          res.send([]);      
        } else {
          console.log('###### SENDING Latest uploads ######## ');
          res.send(result);
        }
    });
});
new_upload_socket.on('connection', function(socket) {

  var hs = socket.handshake;
  console.log('########## A client connected ####### socketID ' +socket.id+' #### '+JSON.stringify(socket.handshake));
  socket.emit('populate', "new connection "+socket.id);    
}); // End of push_messages socket.io
