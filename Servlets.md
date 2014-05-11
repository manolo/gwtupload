
<wiki:toc max_depth="3" />

# Sending parameters to the servlet

Gwtupload allows different mechanisms to send info when uploading a file.
1. Adding **parameters** to the query-string. It is the simplier way
- **Client**
      MultiUploader u = new MultiUploader();
      u.setServletPath(u.getServletPath() + "?myinfo=whatever");
- **Server**
     String myInfo = request.getParameter("myinfo");
1. Adding **widgets** to the !FormPanel. It works with either single or multiple uploaders, and you can set the order in the formpanel. Note: to use PHP-APC you have to add hidden fields in the first position.
- **Client**
      SingleUploader u = new SingleUploader();
      u.add(new Hidden("myinfo", "whatever"), 0);
- **Server**
      for (FileItem item : getSessionFileItems(request)) {
        if (item.isFormField() && "myinfo".equals(item.getFieldName())) {
          String myInfo = item.getString();
        }
      }

1. Changing the prefix of the input file name, you can not use spaces though.
- **Client**
      SingleUploader u = new SingleUploader();
      u.setFileInputPrefix("Whatever-"); 
- **Server**
        for (FileItem item : getSessionFileItems(request)) {
          if (!item.isFormField()) {
            String myInfo = item.getFieldName().split("-")[0];
          }
        }

# Getting additional server information

**Note**: This information is for gwtupload 0.6.3, in older versions you have to deal with getServerResponse.

When a file is uploaded, gwtupload servlet returns a message which is parsed in client side being available as a java object.

The info not only includes important info only available at server side (size, content-type, etc), but a customized message which you cand send if you are extending !UploadAction.

The message can be in any format (text, xml, json).

- **Server**
    public class MyServlet extends UploadAction {
      public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
        String ret = "";
        for (FileItem item : getSessionItems(request)) {
          if (!item.isFormField()) {
            // Do anything with the file.
            
            // Update the string to return;
            ret += "server message";
          }
        }
        super.removeSessionFileItems(request);
        return ret;
      }
    }
- **Client**
      MultiUploader u = new MultiUploader();
      u.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
        public void onFinish(IUploader uploader) {
          if (uploader.getStatus() == Status.SUCCESS) {
            UploadedInfo info = uploader.getServerInfo();
            System.out.println("File name " + info.name);
            System.out.println("File content-type " + info.ctype);
            System.out.println("File size " + info.size);
    
            // Here is the string returned in your servlet
            System.out.println("Server message " + info.message);
          }
        }
      });

# Server Configuration (troubleshooting)

A high number of reported errors are related with the server configuration.
- Verify that the `servlet-class` tag matches your servlet class.
- Be sure that your customized servlet is in your classpath and extends [UploadAction](http://gwtupload.googlecode.com/svn/site/apidocs/core/gwtupload/server/UploadAction.html)
- The url-pattern should be accessible. I hereby recommend that you use wild-cards, just the configuration listed below works out-of-the-box.

If you are using GAE, please be aware that you will have many problems, so read the specific wiki page for it (not written yet).
 
- **web.xml**:
      <servlet>
        <servlet-name>uploadServlet</servlet-name>
        <servlet-class>my.package.MyServlet</servlet-class>
      </servlet>
      <servlet-mapping>
        <servlet-name>uploadServlet</servlet-name>
        <url-pattern>*.gupld</url-pattern>
      </servlet-mapping>
    
      <!-- Optional -->
      <context-param>
        <!-- default is 5Mb -->
        <param-name>maxSize</param-name>
        <param-value>3145728</param-value>
      </context-param>
      <context-param>
        <!-- just for testing, set it to false in production -->
        <param-name>slowUploads</param-name>
        <param-value>200</param-value>
      </context-param>
      <context-param>
        <!-- You should not change it unless your app is in a very slow net -->
        <param-name>noDataTimeout</param-name>
        <param-value>20000</param-value>
      </context-param>


# Questions

Send emails to the group [gwtupload@googlegroups.com](http://groups.google.com/group/gwtupload) to ask for help.

The group is public and it is indexed, so anyone should find and read your questions/answers.

**IMPORTANT!** Comments in this page will be ignored, please use the mailing list.

Thank you.

----
*©2011 [Manuel Carrasco Moñino](http://manolocarrasco.blogspot.com)* 