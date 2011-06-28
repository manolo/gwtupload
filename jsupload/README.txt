

- This project produces a set of files which can be used in projects not using gwt but javascript writen by hand.

- To produce the files you need maven, java and perl.

To produce the default obfuscated library for using in normal sites run:
$ mvn clean package 

To produce the library with non ofuscated javascript run:
$ mvn clean package -Ppretty

If you wanted to deploy the library in a server and use it from another site run:
$ mvn clean package -Pxs
NOTE: xs linker has security issues with IE7 

- After running any of these commands you should have a .zip file in the target folder
ready to deploy in your webserver.

- The distribution file includes an example application which you can run at:
http://yoursite/jsupload/JsUpload.html

- Before running this example you should put jsupload.cgi.pl in your cgi-bin folder 
so be sure that this url does return an OK
http://yoursite/cgi-bin/jsupload.cgi.pl


Please report your issues/suggestion to http://code.google.com/p/gwtupload/issues

Manuel Carrasco Moñino
