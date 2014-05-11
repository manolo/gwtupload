1. Download last version of the library: gwtupload-x.x.x.jar and include it in your classpath.
1. Also, add these libraries to your application: commons-fileupload-1.2.1.jar, commons-io-2.3.jar and log4j.jar
1. Edit your module file: Xxx.gwt.xml.
    <module>
    
      <!-- Include GWTUpload library -->
      <inherits name="gwtupload.GWTUpload"/>
      <!-- Load dinamically predefined styles in the library when the application starts -->
      <stylesheet src="Upload.css"/>
       
      <!-- Change this line with your project's entry-point -->
      <entry-point class="package.Xxx"/>
    </module>
1. Edit your web.xml and include the default servlet.
      <context-param>
        <!-- max size of the upload request -->
        <param-name>maxSize</param-name>
        <param-value>3145728</param-value>
      </context-param>
      <context-param>
        <!-- Useful in development mode to slow down the uploads in fast networks.
             Put the number of milliseconds to sleep in each block received in the server.
             false or 0, means don't use slow uploads  -->
        <param-name>slowUploads</param-name>
        <param-value>200</param-value>
      </context-param>
    
      <servlet>
        <servlet-name>uploadServlet</servlet-name>
        <!-- This is the default servlet, it puts files in session -->
        <servlet-class>gwtupload.server.UploadServlet</servlet-class>
      </servlet>
      <servlet-mapping>
        <servlet-name>uploadServlet</servlet-name>
        <url-pattern>*.gupld</url-pattern>
      </servlet-mapping>
1. Or use your customized servlet (extending ActionUpload and overriding executeAction).
      <servlet>
        <servlet-name>uploadServlet</servlet-name>
        <servlet-class>my.package.MyCustomizedUploadServlet</servlet-class>
      </servlet>
      <servlet-mapping>
        <servlet-name>uploadServlet</servlet-name>
        <url-pattern>*.gupld</url-pattern>
      </servlet-mapping>
1. Create your client application
     
    /**
     * An example of a MultiUploader panel using a very simple upload progress widget
     * The example also uses PreloadedImage to display uploaded images.
     * 
     * @author Manolo Carrasco Moñino
     */
    public class MultipleUploadSample implements EntryPoint {
    
      // A panel where the thumbnails of uploaded images will be shown
      private FlowPanel panelImages = new FlowPanel();
    
      public void onModuleLoad() {
        // Attach the image viewer to the document
        RootPanel.get("thumbnails").add(panelImages);
        
        // Create a new uploader panel and attach it to the document
        MultiUploader defaultUploader = new MultiUploader();
        RootPanel.get("default").add(defaultUploader);
    
        // Add a finish handler which will load the image once the upload finishes
        defaultUploader.addOnFinishUploadHandler(onFinishUploaderHandler);
      }
    
      // Load the image in the document and in the case of success attach it to the viewer
      private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler() {
        public void onFinish(IUploader uploader) {
          if (uploader.getStatus() == Status.SUCCESS) {
    
            new PreloadedImage(uploader.fileUrl(), showImage);
            
            // The server sends useful information to the client by default
            UploadedInfo info = uploader.getServerInfo();
            System.out.println("File name " + info.name);
            System.out.println("File content-type " + info.ctype);
            System.out.println("File size " + info.size);
    
            // You can send any customized message and parse it 
            System.out.println("Server message " + info.message);
          }
        }
      };
    
      // Attach an image to the pictures viewer
      private OnLoadPreloadedImageHandler showImage = new OnLoadPreloadedImageHandler() {
        public void onLoad(PreloadedImage image) {
          image.setWidth("75px");
          panelImages.add(image);
        }
      };
    }
    
1. Create your customize servlet. This is an example of how to save the received files in a temporary folder. Don't override neither doPost or doGet unless you know what you are doing:
    /**
     * This is an example of how to use UploadAction class.
     *  
     * This servlet saves all received files in a temporary folder, 
     * and deletes them when the user sends a remove request.
     * 
     * @author Manolo Carrasco Moñino
     *
     */
    public class SampleUploadServlet extends UploadAction {
    
      private static final long serialVersionUID = 1L;
      
      Hashtable<String, String> receivedContentTypes = new Hashtable<String, String>();
      /**
       * Maintain a list with received files and their content types. 
       */
      Hashtable<String, File> receivedFiles = new Hashtable<String, File>();
    
      /**
       * Override executeAction to save the received files in a custom place
       * and delete this items from session.  
       */
      @Override
      public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
        String response = "";
        for (FileItem item : sessionFiles) {
          if (false == item.isFormField()) {
            try {
              /// Create a new file based on the remote file name in the client
              // String saveName = item.getName().replaceAll("[\\\\/><\\|\\s\"'{}()\\[\\]]+", "_");
              // File file =new File("/tmp/" + saveName);
              
              /// Create a temporary file placed in /tmp (only works in unix)
              // File file = File.createTempFile("upload-", ".bin", new File("/tmp"));
              
              /// Create a temporary file placed in the default system temp folder
              File file = File.createTempFile("upload-", ".bin");
              item.write(file);
              
              /// Save a list with the received files
              receivedFiles.put(item.getFieldName(), file);
              receivedContentTypes.put(item.getFieldName(), item.getContentType());
              
              /// Send a customized message to the client.
              response += "File saved as " + file.getAbsolutePath();
    
            } catch (Exception e) {
              throw new UploadActionException(e);
            }
          }
        }
        
        /// Remove files from session because we have a copy of them
        removeSessionFileItems(request);
        
        /// Send your customized message to the client.
        return response;
      }
      
      /**
       * Get the content of an uploaded file.
       */
      @Override
      public void getUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fieldName = request.getParameter(UConsts.PARAM_SHOW);
        File f = receivedFiles.get(fieldName);
        if (f != null) {
          response.setContentType(receivedContentTypes.get(fieldName));
          FileInputStream is = new FileInputStream(f);
          copyFromInputStreamToOutputStream(is, response.getOutputStream());
        } else {
          renderXmlResponse(request, response, XML_ERROR_ITEM_NOT_FOUND);
       }
      }
      
      /**
       * Remove a file when the user sends a delete request.
       */
      @Override
      public void removeItem(HttpServletRequest request, String fieldName)  throws UploadActionException {
        File file = receivedFiles.get(fieldName);
        receivedFiles.remove(fieldName);
        receivedContentTypes.remove(fieldName);
        if (file != null) {
          file.delete();
        }
      }
    }
1. This code is available as an Eclipse project with maven and ant scripts
- Check out the project, import it in Eclipse an take a look to the code
      svn checkout http://gwtupload.googlecode.com/svn/trunk/GettingStarted
1. There is a specific page for servlet stuff: [Servlets](http://code.google.com/p/gwtupload/wiki/Servlets)
1. Also you could use gwtupload's Sendmail example as a reference application:
- Taking a look to the sources: [web.xml](http://gwtupload.googlecode.com/svn/trunk/SendMailSample/src/main/webapp/WEB-INF/web.xml), [SendMailSample.gwt.xml](http://gwtupload.googlecode.com/svn/trunk/SendMailSample/src/main/java/gwtupload/sendmailsample/SendMailSample.gwt.xml), [SendMailSample.java](http://gwtupload.googlecode.com/svn/trunk/SendMailSample/src/main/java/gwtupload/sendmailsample/client/SendMailSample.java) and [SendMailSampleServlet.java](http://gwtupload.googlecode.com/svn/trunk/SendMailSample/src/main/java/gwtupload/sendmailsample/server/SendMailSampleServlet.java)
- Checking out the project and importing it in eclipse:
      svn checkout http://gwtupload.googlecode.com/svn/trunk/SendMailSample
- Or downloading the aplication ([SendMailSample.war](http://gwtupload.googlecode.com/svn/trunk/SendMailSample/SendMailSample.war)) and deploying it in any servlet container.

## IMPORTANT!!: How to ask questions

Please, send emails to the group [gwtupload@googlegroups.com](http://groups.google.com/group/gwtupload) to ask for help.

The group is public and it is indexed, so anyone should find and read your questions/answers.

**Note** that Comments here will be ignored !!!.

Thank you.

----
*©2011 [Manuel Carrasco Moñino](http://manolocarrasco.blogspot.com)* 