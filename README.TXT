GWTUpload & JSUpload

GWTUpload is a library for uploading files to web servers, showing a progress bar with real information about the process (file size, bytes transferred, etc). It uses ajax requests to ask the web server for the upload progress. It has two components written in java, the server side with servlet and utility classes, and the client side that is compiled into javascript using gwt.

JSUpload is the same client library but compiled and exported into javascript, so non java developers can use it directly in web pages. I've written an article describing the technique used to do this. JSUpload provides a server program coded in perl that can be installed in any web server as a cgi-bin script.

GWTUpload-GAE is a library including a special servlet to handle uploads in Google Application Engine servers (GAE).


BUILDING:

- Checkout the code from either github or googlecode:
  git clone https://github.com/manolo/gwtupload.git
  git clone https://code.google.com/p/gwtupload/

- Assuming you have already installed maven in your system just run:
  mvn package

RUNNING EXAMPLES
- To compile the examples and run them with maven jetty plugin
  mvn jetty:run-exploded

- To run examples in GWT dev mode run:
  mvn gwt:run

- You can deploy the generated gwtupload-version.war in a servlet container or run it with:
  java -jar gwtupload-version.war


ECLIPSE:

- You can use m2eclipse (sonatype) to import the project
- Or you can generate an eclipse project running:
  mvn eclipse:eclipse

HELP:

  1.- Read the documentation: http://code.google.com/p/gwtupload/w/list
  2.- Check if your issue is listed in: http://code.google.com/p/gwtupload/issues/list?can=1
  3.- Read the mailing list: http://groups.google.com/group/gwtupload
  4.- Ask to the list: gwtupload@googlegroups.com
  5.- Open an issue


- Manuel Carrasco Moñino
