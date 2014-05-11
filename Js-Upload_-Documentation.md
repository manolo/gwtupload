
*The library has been developed using java and exported to javascript using gwt-compiler and [gwt-exporter](http://code.google.com/p/gwt-exporter/) library.*

<wiki:toc max_depth="3" />

# Goals

- The library can be used directly, without the need of knowing anything about gwt and java.
- The generated javascript code is minimized, optimized, obfuscated, and doesn't depend on any other library.
- The library works fine in all browsers supported by gwt.
- The library provides a simple cgi-bin written in perl that can be used in most web servers.

# Setup instructions

1. Download last version of the library: jsupload-x.x.x.zip and uncompress it in a folder.
1. In the server side you have to install one of the provided server programs to receive the files:
- CGI: put the jsupload.cgi.pl (included in jsupload-x.x.x.zip) into the cgi-bin folder of your web server, and set the action parameter in your javascript code pointing to it.
   {{{
   uploader = new jsu.Upload({
      action: "/cgi-bin/jsupload.cgi.pl" 
      [});
   }}}
- PHP: Install PHP-APC and put the lines below in your php.ini file. Then install the provided script in any php folder enable in your web server. You have to add
   {{{
    [APC](...])
    apc.enabled=1
    apc.shm_segments=1
    apc.shm_size=64
    apc.max_file_size=10M
    apc.stat=1
    apc.rfc1867="1"
    apc.rfc1867_freq="0"
   }}}
   {{{
   uploader = new jsu.Upload({
      action: "[[...](...]/jsupload.php")
   });
   var e = document.createElement("INPUT")
   e.type = "hidden";
   e.name = "APC_UPLOAD_PROGRESS";
   e.value = u.data().name;
   uploader.addElement(e, 0);
   }}}
- Java: You can use the servlet provided with gwtupload.jar and setup it in WEB-INF/web.xml file.
- Rails: You can use the cgi script provided. If you wanted to collaborate in a port for rails, please let me know.
- Others: Install the provided cgi script which leaves the files and information in a temporary folder. Them, use an ajax request to inform your application that the upload process has finished.
   {{{
   uploader = new jsu.Upload({
      onFinish: function(data) {
        [to your application, read the cookie set by the cgi uploader to know where are the uploaded files](call)
      } 
   });
   }}}
1. Include javascript library in your html file.  You can use relative paths or full qualified ones because the library has been compiled using cross-site linker. The library takes care loading dynamically the needed images and css.
 {{{
     <script language="javascript" src="[}}}
1. In your html code, define the jsuOnLoad() function wich will be called once the library is loaded.
 {{{
     <script language='javascript'>
      function jsuOnLoad() {
        [...](...]/jsupload.JsUpload.nocache.js"></script>)
      }
     </script>
 }}}


# Library API

## Upload

*Exportable version of gwt Uploader.
    <h3>Features</h3>
    <ul>
    <li>Three kind of progress bar, the most advanced one shows upload speed, time remaining, sizes, progress</li>
    <li>Single upload form: while the file is being sent the modal dialog avoid the user to interact with the application, 
    Then the form can be used again for uploading more files.</li>
    <li>Multiple upload form: Each time the user selects a file it goes to the queue and the user can select more files.</li>
    <li>It can call configurable functions on the events of on Change, on Start and on Finish</li>
    <li>The user can cancel the current upload, can delete files in the queue or remove uploaded files</li>
    </ul>
    *
- Constructor
    var upload = new jsc.Upload ({
       multiple: false,  // specify whether the uploader has a multiple behavior
       chooser: "browser",  // Choose file button type, options are: "browser", "button", "label", "anchor".
       type: "chismes",  // Type of progress bar, valid options are "basic", "chismes" or "incubator"
       maxFiles: 0,  // Only used if multiple=true. The maximum number of files which the user can send to the server. 0 means unlimited. Only successful uploads are counted.
       empty: false,  // specify whether single uploader can submit the form is the file input is empty
       auto: ,  // set autosubmit flag, by default is true for multiuploader and false for singleuploader.
       multiSelection: false,  // specify whether the user can select multiple files in the browser dialog (only in browsers supporting the multiple attribute).
       onStart: null,  // Javascript method called when the upload process starts
       onChange: null,  // Javascript method called when the user selects a file
       onFinish: null,  // Javascript method called when the upload process finishes
       onCancel: null,  // Javascript method called when the upload file is canceled, removed from the queue or from the server
       onStatus: null,  // Javascript method called when the upload file's status changes
       containerId: null,  // Id of the element where the widget will be inserted
       action: "servlet.gupld",  // Servlet path, it has to be in the same domain, because cross-domain is not supported
       validExtensions: null,  // List of valid extensions, the extensions has to be separated by comma or spaces
       regional: {     // hash with the set of key/values to internationalize the widget
            uploaderActiveUpload: "There is already an active upload, try later.", 
            uploaderAlreadyDone: "This file was already uploaded.", 
            uploaderInvalidExtension: "Invalid file.\nOnly these types are allowed:\n", 
            uploaderTimeout: "Timeout sending the file:\n perhups your browser does not send files correctly,\n your session has expired,\n or there was a server error.\nPlease try again.", 
            uploaderServerError: "Invalid server response. Have you configured correctly your application in the server side?", 
            uploaderServerUnavailable: "Unable to contact with the server: ", 
            uploaderBadServerResponse: "Unable to upload the file:\nServer response has a format which can not be parsed by the application.\n\n Server Response is:\n.", 
            uploaderBlobstoreBilling: "It seems that you are using blobstore, check that your application is billing enabled to use it.", 
            uploaderSend: "Send", 
            uploadLabelCancel: null, 
            uploadStatusCanceling: "Canceling", 
            uploadStatusCanceled: "Canceled", 
            uploadStatusError: "Error", 
            uploadStatusInProgress: "Sending...", 
            uploadStatusQueued: "Queued", 
            uploadStatusSubmitting: "Submiting form...", 
            uploadStatusSuccess: "Done", 
            uploadStatusDeleted: "Deleted", 
            uploadBrowse: "Select a file ...", 
            submitError: Unable to auto submit the form, it seems your browser has security issues with this feature.\n Developer Info: If you are using jsupload and you do not need cross-domain, try a version compiled with the standard linker?, 
            invalidPathError: Error you have typed an invalid file name, please select a valid one., 
            progressPercentMsg: "{0}%", // Set the message used to format the progress in percent units. 
            progressSecondsMsg: "Time remaining: {0} Seconds", // Set the message used to format the time remaining text below the progress bar in seconds.
            progressMinutesMsg: "Time remaining: {0} Minutes", // Set the message used to format the time remaining text below the progress bar in minutes
            progressHoursMsg: "Time remaining: {0} Hours" // Set the message used to format the time remaining text below the progress bar in hours
       }
    });
- Instance methods
    
    /* adds a javascript DOM element to the upload form at the specified position */
    upload.addElement(e, index);
    
    /* Depending on the multiple feature configuration, it returns a javascript 
    array of as many elements as images uploaded or one element.
    
    The element with the uploaded info has this structure:
    upload.data().url      // The url to download the uploaded file from the server
    upload.data().name     // The name of the input form element
    upload.data().filename // The name of the file selected by the user as is reported by the browser
    upload.data().basename // The name of the file selected by the user without path
    upload.data().response // The raw server xml response
    upload.data().message  // The server text in the message tag
    upload.data().size     // The size of the file 
    upload.data().status   // The upload status (UNINITIALIZED, QUEUED, INPROGRESS, SUCCESS, ERROR, CANCELING, CANCELED, SUBMITING) */
    var a_javascriptobject = upload.data();
    
    /* submit the upload form to the server. */
    upload.submit();
    
    /* adds a javascript DOM element to the upload form. */
    upload.addElement(e);

## Preload Image

*This class preloads an image in the browser, and in the case of
    success executes a user defined function.
    It stores the original size of the image.
    *
- Constructor
    var preloadimage = new jsc.PreloadImage ({
       url: null,  // web address for the image
       containerId: null,  // Id of the element where the widget will be inserted
       onLoad: null  // Javascript method called after the browser has loaded the image
    });
- Instance methods
    
    /* Returns the original height of the image. */
    var a_int = preloadimage.realHeight();
    
    /* Returns a properties javascript hash. 
    This hash has the info:
    - url
    - realwidth
    - realheight */
    var a_javascriptobject = preloadimage.getData();
    
    /* Returns the original width of the image. */
    var a_int = preloadimage.realWidth();
    
    /* Change the size of the image in the document. */
    preloadimage.setSize(width, height);
    
    /* Sets the alt attribute of the image. */
    preloadimage.setAlt(alt);
    
    /* Returns the DOM element of the image. */
    var a_element = preloadimage.getElement();
    
    /* Adds a classname to the image. */
    preloadimage.addStyleName(style);
# Server script (jsupload.cgi.pl)

- The provided server script has been written in perl for the GWTUpload/JSUpload library.
- This script requires perl, CGI and Digest::MD5, which are installed by default in most linux/unix distributions.
- You have to put this file in a script/cgi-bin enabled folder of your web server, and you have to set the rigth execution permissions.
1. **Operation**:
- When it receives a POST request, it updates periodically a status file with the progress,       and stores all received form elements in a temporary folder for the user session.
- When it receives GET requests, returns a xml response with the progess information.
- When the GET request, has the parameter show=xxxx, it returns the content of the form      element whose name is xxxx. If the element is a file it sends a response with the adequate      content-type, otherwise it returns a xml response with the element's value.
1. **Configuration**: At the top of the script you have some customizable variables.
- $idname: is the name for the sessionid cookie name, you could redefine it to match              the name of the cookie set by your application. By default the name is CGISESSID.
- $tmp_dir: is the prefix used in the path location to store the data received. By default              it is set to "/tmp/uploader", normally you have to change it to match the path              configured in your application. It is strongly recommended that this directory               should be created apart from "/tmp", in a non-httpd accessible path.
- $max_size: is the maximum size in bytes allowed for the entire request size, note that it is not              the real file size because the request includes the headers and could have more              files or parameters. Normally only one file is transfered, and the request size              is very closed to the file size.
- $mkpath: if true jsupload will create the folder to store the received data, otherwise              it is the application the responsible of creating it. Note that setting it to false               your system is protected to be used as a surreptitious storage area. The files               in this folder will be created with the default perl permissions (0666 or the user umask),               so in the case you leave the application to create the folder be aware to set the              appropriate permissions.
- $slow:   if it has a value greater than zero, after each chunk received, jsupload will sleep              for this value in seconds (it may be fractional). It is used to see the progress bar               in fast networks when you are testing or developing your app.
1. **Integration**:
- The files are stored in the folder $tmp_dir/xxxx where xxxx is the session id (cookie CGISESSID).
- For each form element received, it stores a file /tmp/uploader/xxxx/yyyy.info      where yyyy is the name of the element.
- In the case of file elements, it puts the content of the file in the file     /tmp/uploader/xxxx/yyyy.bin.
- The application must create, handle, and clean $tmp_dir files.
# Server script (jsupload.php)

- Server script written in php for the GWTUpload/JSUpload library.
- This script requires PHP with PHP-APC.
- You have to put this file in a php enabled folder of your web server.
1. **Server Configuration**:
   In order to enable the PHP APC extension to support file upload progress indication   you have to add the following section into php.ini:
   {{{
    [apc.enabled=1
    apc.shm_segments=1
    apc.shm_size=64
    apc.max_file_size=10M
    apc.stat=1
    apc.rfc1867="1"
    apc.rfc1867_freq="0"
   }}}
1. *Client side*:
   In the client you have to add a hidden widget to the uploader in the first position.
- Gwt-upload
     {{{
       uploader.add(new Hidden("APC_UPLOAD_PROGRESS", uploader.getInputName()), 0);
     }}}
- Js-upload
     {{{
       var e = document.createElement("INPUT")
       e.type = "hidden";
       e.name = "APC_UPLOAD_PROGRESS";
       e.value = u.data().name;
       u.addElement(e, 0);
     }}}
1. *Configuration*: At the top of the script there is a customizable variable.
- $uploaddir: is the prefix used in the path location to store the data received. By default                 it is set to "/tmp/php_upload/", normally you have to change it to match the path                 configured in your application. It is strongly recommended that this directory                  should be created apart from "/tmp", in a non-httpd accessible path.
1. *Integration*:
- The files are stored in the folder $tmp_dir/xxxx where xxxx is the session id (cookie PHPSESSID).
- For each file received, it stores a file /tmp/uploader/xxxx/yyyy.info,      where yyyy is the name of the element, with the original filename and its content-type.
- The content of the file in the file will be in the /tmp/uploader/xxxx/yyyy.bin file.
- The application must create, handle, and clean $tmp_dir files.
- You should check that the user session is valid in order to avoid that your server could     be used as a used as a surreptitious storage area.
# Sample Code

You can view this example  [http://gwtupload.alcala.org/gupld/jsupload/JsUpload.html here](APC])
          // Method called after the JsUpload library has been loaded
          function jsuOnLoad() {
            new jsu.Upload({
              type: "incubator",
              containerId: "uploader1",
              multiple: true, 
              multiSelection: true,
              auto: true,
              /* 
               * Uncomment the next line to handle actions when 
               *  the upload status changes
               */ 
              // onStatus: function(data) { alert(data.status); },
              onFinish: loadImage
            });
            
            new jsu.Upload({
               containerId: "uploader2",
               multiple: true, 
               multiSelection: false,
               auto: true,
               chooser: "anchor",    
               onFinish: loadImage
            });
            
            new jsu.Upload({
              type: "basic",
              containerId: "uploader3",
              multiple: true, 
              multiSelection: true,
              auto: true,
              onChange: null,
              onStart: null,
              onFinish: loadImage,
              chooser: "button",    
              maxFiles: 4,
              regional: {
                  uploadStatusSuccess: "File saved in the server",
                  uploadStatusError:   "Unable to save the file.",
               	  uploadBrowse: "Select a file to save ..."    
              }
            });
    
            var u = new jsu.Upload({
              type: "basic",
              containerId: "uploader4",
              onFinish: loadImage,
              multiSelection: true,
              regional: {
            	uploaderSend: "Send File."    
              }
            });
            
            // This enables php apc progress mechanism
            var e = document.createElement("INPUT")
            e.type = "hidden";
            e.name = "APC_UPLOAD_PROGRESS";
            e.value = u.data().name;
            u.addElement(e, 0);
    
            new jsu.Upload({
              containerId: "uploader5",
              onFinish: loadImage,
              chooser: "label",    
              uploadBrowse: "",
              validExtensions: ".jpg, .gif, .png",
              regional: {
            	uploaderSend: "Upload it.",
            	uploadBrowse: "Select an image from your computer..."    
              }
            });
            
            // Method to show a picture using the class PreloadImage
            // The image is not shown until it has been sucessfully downloaded
            function loadImage(upl_data) {
               if (upl_data && upl_data[0] && upl_data[0].url) {
            	 for (i = 0; i < upl_data.length; i++) {
            		loadImage(upl_data[i]);
            	 }
               } else if (upl_data && upl_data.url) {
                  var image = new jsu.PreloadImage({
                    url: upl_data.url,
                    containerId: "photos",
                    onLoad: function(img_data) {
                      image.setSize(100, -1);
                    }
                  });
               }
            }
          }
    
**Author:** *[Manuel Carrasco Moñino](http://manolocarrasco.blogspot.com/)*

**Date:** *Sun Apr 27 11:14:22 CEST 2014*

This documentation has been generated automatically parsing comments in java files, if you realise any error, please report it