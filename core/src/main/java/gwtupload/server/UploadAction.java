/*
 * Copyright 2010 Manuel Carrasco Moñino. (manolo at apache/org) 
 * http://code.google.com/p/gwtupload
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gwtupload.server;

import gwtupload.server.exceptions.UploadActionException;
import gwtupload.server.exceptions.UploadCanceledException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

/** 
 * <p>Class used to manipulate the data received in the server side.</p>
 * 
 * The user has to implement the method executeAction which receives the list of the FileItems
 * sent to the server. Each FileItem represents a file or a form field. 
 * 
 * <p>Note: Temporary files are not deleted until the user calls removeSessionFiles(request).</p>
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public class UploadAction extends UploadServlet {
  private static final long serialVersionUID = -6790246163691420791L;

  /**
   * Returns the content of a file as an InputStream if it is found in the 
   * FileItem vector.  
   * 
   * @param sessionFiles collection of files sent by the client 
   * @param parameter field name or file name of the desired file 
   * @return an ImputString 
   */
  public static InputStream getFileStream(Vector<FileItem> sessionFiles, String parameter) throws IOException {
    FileItem item = findFileItem(sessionFiles, parameter);
    return item == null ? null : item.getInputStream();
  }

  /**
   * Returns the value of a text field present in the FileItem collection. 
   * 
   * @param sessionFiles collection of fields sent by the client 
   * @param fieldName field name 
   * @return the string value 
   */
  public static String getFormField(Vector<FileItem> sessionFiles, String fieldName) {
    FileItem item = findItemByFieldName(sessionFiles, fieldName);
    return item == null || item.isFormField() == false ? null : item.getString();
  }

  /**
   * This method is called when all data is received in the server.
   * 
   * Temporary files are not deleted until the user calls removeSessionFiles(request)
   * 
   * Override this method to customize the behavior
   * 
   * @param request
   * @param sessionFiles
   * 
   * @return the text/html message to be sent to the client.
   *         In the case of null the standard response configured for this 
   *         action will be sent.
   *         
   * @throws UploadActionException
   *         In the case of error
   * 
   */
  public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
    return null;
  }

  /**
   * This method is called when a received file is requested to be removed and
   * is in the collection of items stored in session. 
   * If the item does't exist in session this method is not called
   * 
   * After it, the item is removed from the session items collection.
   * 
   * Override this method to customize the behavior
   * 
   * @param request
   * @param item    The item in session
   * 
   * @throws UploadActionException
   *         In the case of an error, the exception message is returned to 
   *         the client and the item is not deleted from session 
   *         
   */
  public void removeItem(HttpServletRequest request, FileItem item)  throws UploadActionException {
  }
  
  /**
   * This method is called when a received file is requested to be removed.
   * After it, the item is removed from the session items collection.
   * 
   * Override this method to customize the behavior
   * 
   * @param request
   * @param fieldName    The name of the filename input
   * 
   * @throws UploadActionException
   *         In the case of an error, the exception message is returned to 
   *         the client and the item is not deleted from session 
   *         
   */
  public void removeItem(HttpServletRequest request, String fieldName)  throws UploadActionException {
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException  {
    String parameter = request.getParameter(PARAM_REMOVE);
    if (parameter != null) {
      try {
        removeItem(request, parameter);
        FileItem item = super.findFileItem(getSessionFileItems(request), parameter);
        if (item != null) {
          removeItem(request, item);
        }
      } catch (Exception e) {
        renderXmlResponse(request, response, "<" + TAG_ERROR + ">" + e.getMessage() + "</" + TAG_ERROR + ">");
        return;
      }
      super.removeUploadedFile(request, response);
    } else {
      super.doGet(request, response);
    }
  }
  
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String error = null;
    String message = null;

    perThreadRequest.set(request);
    try {
      // Receive the files and form elements, updating the progress status
      error = super.parsePostRequest(request, response);
      if (error == null) {
        // Call to the user code 
        message = executeAction(request, getSessionFileItems(request));
      }
    } catch (UploadCanceledException e) {
      renderXmlResponse(request, response, "<cancelled>true</cancelled>");
      return;
    } catch (UploadActionException e) {
      logger.info("ExecuteUploadActionException: " + e);
      error =  e.getMessage();
    } catch (Exception e) {
      logger.info("Exception " + e);
      error = e.getMessage();
    } finally {
      perThreadRequest.set(null);
    }

    AbstractUploadListener listener = getCurrentListener(request);
    if (error != null) {
      renderXmlResponse(request, response, "<" + TAG_ERROR + ">" + error + "</" + TAG_ERROR + ">");
      if (listener != null) {
        listener.setException(new RuntimeException(error));
      }
      UploadServlet.removeSessionFileItems(request);
    } else {
      Map<String, String> stat = new HashMap<String, String>();
      getFileItemsSummary(request, stat);
      if (message != null) {
        stat.put("message", "\n<![CDATA[\n" + message + "\n]]>\n");
      }
      renderXmlResponse(request, response, statusToString(stat), true);
    }
    
    finish(request);
  }
  
}
