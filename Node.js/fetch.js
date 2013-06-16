$(document).ready(function () {

    var progress = $('#progress'),
        gallery = $('#gallery'),
        gallery_ul = $('#gallery_ul');
        latest_upload_ul = $('#latest_uploads');
        myModal = $('#myModal');

    //alert('fetch.js');

    $(document).ajaxStart(function() {
        gallery.hide();
        progress.show();
        gallery_ul.html("");
    });


    $('ul.nav > li').click(function (e) {
            e.preventDefault();
            $('ul.nav > li').removeClass('active');
            //alert($(this).attr('id'));

            $(this).addClass('active');                
            var id = $(this).attr('id');
            if( id == 1) {
                showAll();                
            } else if (id == 2) {
                showImages();
            } else if (id == 3) {
                showVideos();
            } else if (id == 4) {
                showAudios();
            }
    });      

    showAll();
    showlatest();

    //var newUploadSocket  = io.connect('http://ec2-54-242-153-20.compute-1.amazonaws.com:4000/new_upload');
    var newUploadSocket  = io.connect('http://localhost:4000/new_upload');

    newUploadSocket.on('error', function (reason){
        alert('Unable to connect Socket.IO'+reason);
    });

    newUploadSocket.on('connect', function (){      
        //alert(' ######### Connected ########## ');
    });

    newUploadSocket.on('update', function (data) {        
        alert( ' ######### pushMessageSocket new update ############## '+JSON.stringify(data));
        var type = data.type;
        var path = data.path;
        var name = data.name+","+data.location;
        if( type == 'image') {
            name += ' has uploaded a picture';
        } else if ( type == 'video') {
            name += ' has uploaded a video';
        } else if ( type == 'audio') {
            name += ' has uploaded a audio';
        }
        
        var li = $('<li/>');
        $('<a/>')
        .prop('href', data.path)
        .html(name)
        .appendTo(li);
        latest_upload_ul.prepend(li);
        alert(path+" : "+name);
    }); 

    // Load images via flickr for demonstration purposes:
/*    $.ajax({
        url: 'http://api.flickr.com/services/rest/',
        data: {
            format: 'json',
            method: 'flickr.interestingness.getList',
            api_key: '7617adae70159d09ba78cfec73c13be3'
        },
	    dataType: 'jsonp',
        jsonp: 'jsoncallback'
    }).done(function (data) {
        alert(' ajax done');
        progress.hide();
        gallery.show();
        //var gallery = $('#gallery_ul');
        var url;

        $.each(data.photos.photo, function (index, photo) {
            url = 'http://farm' + photo.farm + '.static.flickr.com/' +
                photo.server + '/' + photo.id + '_' + photo.secret;
            var li = $('<li/>').appendTo(gallery_ul);
            $('<a data-gallery="gallery" class="thumbnail"/>')
                .append($('<img style="width:120px; height:120px">').prop('src', url + '_s.jpg'))
                .prop('href', url + '_b.jpg')
                .prop('title', photo.title)
                .appendTo(li);
        });
    });  */


    function showAll() {
        $.ajax({
            url: "/all",
            type: "get",
            success: function(response, textStatus, jqXHR){
                var result = JSON.parse(response);
                for( var i=0; i< result.length; i++) {
                    var data = result[i];
                    var li = $('<li/>').appendTo(gallery_ul);
                    if(data.type == 'image') {
                        $('<a data-gallery="gallery" class="thumbnail"/>')
                        .append($('<img style="width:220px; height:220px">').prop('src', data.path))
                        .prop('href', data.path)
                        .prop('title', data.name+" "+data.location+" "+data.device)
                        .appendTo(li);
                    } else if (data.type == 'video') {
                        $('<video controls width="220" height="220" />')
                        .prop('src', data.path)
                        .prop('title', data.name+" "+data.location+" "+data.device)
                        .appendTo(li);
                    } else if (data.type == 'audio') {
                        $('<audio controls width="220" height="220" />')
                        .prop('src', data.path)
                        .prop('title', data.name+" "+data.location+" "+data.device)
                        .appendTo(li);
                    }
                }

                hideProgress();
            },
            error: function(jqXHR, textStatus, errorThrown){
                showAlert(" Error while sending retreiving images "+testStatus+"  : "+errorThrown);
                hideProgress();
            }            
        }); // End of ajax    
    }

    function showlatest() {
        $.ajax({
            url: "/latest_uploads",
            type: "get",
            success: function(response, textStatus, jqXHR){
                var result = JSON.parse(response);
                for( var i=0; i< result.length; i++) {
                    var data = result[i];
                    var type = data.type;
                    var path = data.path;
                    var name = data.name+","+data.location;
                    if( type == 'image') {
                        name += ' has uploaded a picture';
                    } else if ( type == 'video') {
                        name += ' has uploaded a video';
                    } else if ( type == 'audio') {
                        name += ' has uploaded a audio';
                    }
                    
                    var li = $('<li/>'); 
                    $('<a/>')
                    .click({path:data.path,name:data.name+","+data.location,type:data.type},showContent)
                    .html(name)
                    .appendTo(li); 
                    latest_upload_ul.prepend(li);
                }

            },
            error: function(jqXHR, textStatus, errorThrown){
                showAlert(" Error while sending retreiving images "+testStatus+"  : "+errorThrown);
            }            
        }); // End of ajax    
    }

    function showContent(event) {

        var type = event.data.type;
        var name = event.data.name;
        var path = event.data.path;

        myModal.modal('show');
        $('#myModalLabel').html(name);
        
        if( type == 'image') {
            var img = $('<img/>');
            img.prop('src', path);
            $('#modalContent').show().html(img);
        } else if ( type == 'video') {
            var video = $('<video controls width="320" height="240" />')
            .prop('src', path)
            .prop('title', name);
            $('#modalContent').show().html(video);
        } else if ( type == 'audio') {
            $('#modalContent').show().html('<img src='+event.data.path+'/>');
        }
    }

    function showImages() {

        $.ajax({
            url: "/images",
            type: "get",
            success: function(response, textStatus, jqXHR){
                var result = JSON.parse(response);
                for( var i=0; i< result.length; i++) {
                    var data = result[i];
                    var li = $('<li/>').appendTo(gallery_ul);
                    $('<a data-gallery="gallery" class="thumbnail"/>')
                    .append($('<img style="width:120px; height:120px">').prop('src', data.path))
                    .prop('href', data.path)
                    .prop('title', data.name+" "+data.location+" "+data.device)
                    .appendTo(li);
                }

                hideProgress();
            },
            error: function(jqXHR, textStatus, errorThrown){
                showAlert(" Error while sending retreiving images "+testStatus+"  : "+errorThrown);
                hideProgress();
            }
        }); // End of ajax
    }

    function showVideos() {
        $.ajax({
            url: "/videos",
            type: "get",
            success: function(response, textStatus, jqXHR){
                var result = JSON.parse(response);
                for( var i=0; i< result.length; i++) {
                    var data = result[i];
                    var li = $('<li/>').appendTo(gallery_ul);
//                    <video src="files/test.mp4" controls width="320" height="240"></video>
                    $('<video controls width="320" height="240" />')
                    .prop('src', data.path)
                    .prop('title', data.name+" "+data.location+" "+data.device)
                    .appendTo(li);
                }

                hideProgress();
            },
            error: function(jqXHR, textStatus, errorThrown){
                showAlert(" Error while sending retreiving images "+testStatus+"  : "+errorThrown);
                hideProgress();
            }
        }); // End of ajax    
    }

    function showAudios() {
        $.ajax({
            url: "/audios",
            type: "get",
            success: function(response, textStatus, jqXHR){
                var result = JSON.parse(response);
                for( var i = 0; i< result.length; i++) {
                    var data = result[i];
                    var li = $('<li/>').appendTo(gallery_ul);
//                  <audio src="/files/test.mp3" controls="controls">
                    $('<audio controls />')
                    .prop('src', data.path)
                    .appendTo(li);
                    $('<h4/>')
                    .html(data.name+" "+data.location+"\n"+data.device)
                    .appendTo(li);
                }
                hideProgress();
            },
            error: function(jqXHR, textStatus, errorThrown){
                showAlert(" Error while sending retreiving images "+testStatus+"  : "+errorThrown);
                hideProgress();
            }
        }); // End of ajax
    }

    function hideProgress() {
        progress.hide();
        gallery.show();
    }

});
