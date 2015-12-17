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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static gwtupload.shared.UConsts.MULTI_SUFFIX;
import static gwtupload.shared.UConsts.PARAM_DELAY;
import static gwtupload.shared.UConsts.PARAM_MAX_FILE_SIZE;
import static gwtupload.shared.UConsts.TAG_BLOBSTORE;
import static gwtupload.shared.UConsts.TAG_BLOBSTORE_PATH;
import static gwtupload.shared.UConsts.TAG_CANCELED;
import static gwtupload.shared.UConsts.TAG_CTYPE;
import static gwtupload.shared.UConsts.TAG_CURRENT_BYTES;
import static gwtupload.shared.UConsts.TAG_DELETED;
import static gwtupload.shared.UConsts.TAG_ERROR;
import static gwtupload.shared.UConsts.TAG_FIELD;
import static gwtupload.shared.UConsts.TAG_FILE;
import static gwtupload.shared.UConsts.TAG_FILES;
import static gwtupload.shared.UConsts.TAG_FINISHED;
import static gwtupload.shared.UConsts.TAG_KEY;
import static gwtupload.shared.UConsts.TAG_MSG_END;
import static gwtupload.shared.UConsts.TAG_MSG_GT;
import static gwtupload.shared.UConsts.TAG_MSG_LT;
import static gwtupload.shared.UConsts.TAG_MSG_START;
import static gwtupload.shared.UConsts.TAG_NAME;
import static gwtupload.shared.UConsts.TAG_PARAM;
import static gwtupload.shared.UConsts.TAG_PARAMS;
import static gwtupload.shared.UConsts.TAG_PERCENT;
import static gwtupload.shared.UConsts.TAG_SESSION_ID;
import static gwtupload.shared.UConsts.TAG_SIZE;
import static gwtupload.shared.UConsts.TAG_TOTAL_BYTES;
import static gwtupload.shared.UConsts.TAG_VALUE;

import gwtupload.server.exceptions.UploadActionException;
import gwtupload.server.exceptions.UploadCanceledException;
import gwtupload.server.exceptions.UploadException;
import gwtupload.server.exceptions.UploadSizeLimitException;
import gwtupload.server.exceptions.UploadTimeoutException;
import gwtupload.shared.UConsts;

/**
 * <p>
 * Upload servlet for the GwtUpload library.
 * </p>
 *
 * <ul>
 * <li>For customizable application actions, it's better to extend the UloadAction
 * class instead of this.</li>
 *
 * <li>
 * This servlet supports to be deployed in google application engine. It is able to
 * detect this environment and in this case it does:
 * <ul>
 * <li>Set the request size to 512 KB which is the maximal size allowed</li>
 * <li>Store received data in memory and cache instead of file system</li>
 * <li>Uses memcache for session tracking instead of normal session objects,
 * because objects stored in session seem not to be available until the thread finishes</li>
 * </ul>
 * </li>
 * </ul>
 *
 *
 * <p>
 * <b>Example of web.xml</b>
 * </p>
 *
 * <pre>
 * &lt;context-param&gt;
 *     &lt;!-- max size of the upload request --&gt;
 *     &lt;param-name&gt;maxSize&lt;/param-name&gt;
 *     &lt;param-value&gt;3145728&lt;/param-value&gt;
 *   &lt;/context-param&gt;
 *
 *   &lt;context-param&gt;
 *     &lt;!-- useful in development mode to see the upload progress bar in fast networks. (sleep time in milliseconds) --&gt;
 *     &lt;param-name&gt;slowUploads&lt;/param-name&gt;
 *     &lt;param-value&gt;200&lt;/param-value&gt;
 *   &lt;/context-param&gt;
 *
 *   &lt;context-param&gt;
 *     &lt;!-- max file size of the upload request --&gt;
 *     &lt;param-name&gt;maxFileSize&lt;/param-name&gt;
 *     &lt;param-value&gt;3145728&lt;/param-value&gt;
 *   &lt;/context-param&gt;
 *
 *   &lt;servlet&gt;
 *     &lt;servlet-name&gt;uploadServlet&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;gwtupload.server.UploadServlet&lt;/servlet-class&gt;
 *   &lt;/servlet&gt;
 *
 *   &lt;servlet-mapping&gt;
 *     &lt;servlet-name&gt;uploadServlet&lt;/servlet-name&gt;
 *     &lt;url-pattern&gt;*.gupld&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 *
 *
 * </pre>
 *
 * @author Manolo Carrasco Moñino
 *
 */
public class UploadServlet extends HttpServlet implements Servlet {

  private static final String SESSION_FILES = "FILES";
  private static final String SESSION_LAST_FILES = "LAST_FILES";

  protected static final int DEFAULT_REQUEST_LIMIT_KB = 5 * 1024 * 1024;
  protected static final int DEFAULT_SLOW_DELAY_MILLIS = 300;

  protected static final String XML_CANCELED_TRUE = "<" + TAG_CANCELED + ">true</" + TAG_CANCELED + ">";
  protected static final String XML_DELETED_TRUE = "<" + TAG_DELETED + ">true</" + TAG_DELETED + ">";
  protected static final String XML_ERROR_ITEM_NOT_FOUND = "<" + TAG_ERROR + ">item not found</" + TAG_ERROR + ">";
  protected static final String XML_ERROR_TIMEOUT = "<" + TAG_ERROR + ">timeout receiving file</" + TAG_ERROR + ">";
  protected static final String XML_FINISHED_OK = "<" + TAG_FINISHED + ">OK</" + TAG_FINISHED + ">";

  protected static UploadLogger logger = UploadLogger.getLogger(UploadServlet.class);

  protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

  private static boolean appEngine = false;

  private static final long serialVersionUID = 2740693677625051632L;

  private static String XML_TPL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>%%MESSAGE%%</response>\n";

  private String corsDomainsRegex = "^$";

  /**
   * Copy the content of an input stream to an output one.
   *
   * @param in
   * @param out
   * @throws IOException
   */
  public static void copyFromInputStreamToOutputStream(InputStream in, OutputStream out) throws IOException {
    IOUtils.copy(in, out);
  }

  /**
   * Utility method to get a fileItem of type file from a vector using either
   * the file name or the attribute name.
   *
   * @param sessionFiles
   * @param parameter
   * @return fileItem of the file found or null
   */
  public static FileItem findFileItem(List<FileItem> sessionFiles, String parameter) {
    if (sessionFiles == null || parameter == null) {
      return null;
    }

    FileItem item = findItemByFieldName(sessionFiles, parameter);
    if (item == null) {
      item = findItemByFileName(sessionFiles, parameter);
    }
    if (item != null && !item.isFormField()) {
      return item;
    }

    return null;
  }

  /**
   * Utility method to get a fileItem from a vector using the attribute name.
   *
   * @param sessionFiles
   * @param attrName
   * @return fileItem found or null
   */
  public static FileItem findItemByFieldName(List<FileItem> sessionFiles, String attrName) {
    if (sessionFiles != null) {
      for (FileItem fileItem : sessionFiles) {
        if (fileItem.getFieldName().equalsIgnoreCase(attrName)) {
          return fileItem;
        }
      }
    }
    return null;
  }

  /**
   * Utility method to get a fileItem from a vector using the file name It
   * only returns items of type file.
   *
   * @param sessionFiles
   * @param fileName
   * @return fileItem of the file found or null
   */
  public static FileItem findItemByFileName(List<FileItem> sessionFiles, String fileName) {
    if (sessionFiles != null) {
      for (FileItem fileItem : sessionFiles) {
        if (fileItem.isFormField() == false && fileItem.getName().equalsIgnoreCase(fileName)) {
          return fileItem;
        }
      }
    }
    return null;
  }

  /**
   * Return the list of FileItems stored in session under the provided session key.
   */
  @SuppressWarnings("unchecked")
  public static List<FileItem> getSessionFileItems(HttpServletRequest request, String sessionFilesKey) {
    return (List<FileItem>) request.getSession().getAttribute(sessionFilesKey);
  }

  /**
   * Return the list of FileItems stored in session under the default name.
   */
  public static List<FileItem> getSessionFileItems(HttpServletRequest request) {
    return getSessionFileItems(request, SESSION_FILES);
  }

  /**
   * Return the list of FileItems stored in session under the session key.
   */
  // FIXME(manolo): Not sure about the convenience of this and sessionFilesKey.
  public List<FileItem> getMySessionFileItems(HttpServletRequest request) {
    return getSessionFileItems(request, getSessionFilesKey(request));
  }

  /**
   * Return the most recent list of FileItems received
   */
  @SuppressWarnings("unchecked")
  public static List<FileItem> getLastReceivedFileItems(HttpServletRequest request, String sessionLastFilesKey) {
    return (List<FileItem>) request.getSession().getAttribute(sessionLastFilesKey);
  }

  /**
   * Return the most recent list of FileItems received under the default key
   */
  public static List<FileItem> getLastReceivedFileItems(HttpServletRequest request) {
    return getLastReceivedFileItems(request, SESSION_LAST_FILES);
  }

  /**
   * Return the most recent list of FileItems received under the session key
   */
  public List<FileItem> getMyLastReceivedFileItems(HttpServletRequest request) {
    return getLastReceivedFileItems(request, getSessionLastFilesKey(request));
  }

  /**
   * @deprecated use getSessionFileItems
   */
  public static List<FileItem> getSessionItems(HttpServletRequest request) {
    return getSessionFileItems(request);
  }

  /**
   * Returns the localized text of a key.
   */
  public static String getMessage(String key, Object... pars) {
    Locale loc =
      getThreadLocalRequest() == null || getThreadLocalRequest().getLocale() == null
       ? new Locale("en")
       : getThreadLocalRequest().getLocale();

    ResourceBundle res =
      ResourceBundle.getBundle(UploadServlet.class.getName(), loc);

    String msg = res.getString(key);
    return new MessageFormat(msg, loc).format(pars);
  }

  public static final HttpServletRequest getThreadLocalRequest() {
    return perThreadRequest.get();
  }

  /**
   * Just a method to detect whether the web container is running with appengine
   * restrictions.
   *
   * @return true if the case of the application is running in appengine
   */
  public boolean isAppEngine() {
    return appEngine;
  }

  /**
   * Removes all FileItems stored in session under the session key and the temporary data.
   *
   * @param request
   */
  public static void removeSessionFileItems(HttpServletRequest request, String sessionFilesKey) {
    removeSessionFileItems(request, sessionFilesKey, true);
  }

  /**
   * Removes all FileItems stored in session under the default key and the temporary data.
   */
  public static void removeSessionFileItems(HttpServletRequest request) {
    removeSessionFileItems(request, SESSION_FILES, true);
    removeSessionFileItems(request, SESSION_LAST_FILES, true);
  }

  /**
   * Removes all FileItems stored in session under the session key, but in this case
   * the user can specify whether the temporary data is removed from disk.
   *
   * @param request
   * @param removeData
   *                    true: the file data is deleted.
   *                    false: use it when you are referencing file items
   *                    instead of copying them.
   */
  public static void removeSessionFileItems(HttpServletRequest request, String sessionFilesKey, boolean removeData) {
    logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") removeSessionFileItems: removeData=" + removeData);
    List<FileItem> sessionFiles = getSessionFileItems(request, sessionFilesKey);
    if (removeData && sessionFiles != null) {
      for (FileItem fileItem : sessionFiles) {
        if (fileItem != null && !fileItem.isFormField()) {
          fileItem.delete();
        }
      }
    }
    request.getSession().removeAttribute(sessionFilesKey);
  }

  /**
   * Removes all FileItems stored in session under the default key, but in this case
   * the user can specify whether the temporary data is removed from disk.
   */
  public static void removeSessionFileItems(HttpServletRequest request, boolean removeData) {
    removeSessionFileItems(request, SESSION_FILES, removeData);
    removeSessionFileItems(request, SESSION_LAST_FILES, removeData);
  }

  /**
   * Delete an uploaded file.
   *
   * @param request
   * @param response
   * @return FileItem
   * @throws IOException
   */
  protected static FileItem removeUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String parameter = request.getParameter(UConsts.PARAM_REMOVE);

    FileItem item = findFileItem(getSessionFileItems(request), parameter);
    if (item != null) {
      getSessionFileItems(request).remove(item);
      logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") removeUploadedFile: " + parameter + " " + item.getName() + " " + item.getSize());
    } else {
      logger.info("UPLOAD-SERVLET (" + request.getSession().getId() + ") removeUploadedFile: " + parameter + " not in session.");
    }

    renderXmlResponse(request, response, XML_DELETED_TRUE);
    return item;
  }

  /**
   * Writes a response to the client.
   */
  protected static void renderMessage(HttpServletResponse response, String message, String contentType) throws IOException {
    response.addHeader("Cache-Control", "no-cache");
    response.setContentType(contentType + "; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();
    out.print(message);
    out.flush();
    out.close();
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (checkCORS(request, response) && request.getMethod().equals("OPTIONS")) {
      String method = request.getHeader("Access-Control-Request-Method");
      if (method != null) {
        response.addHeader("Access-Control-Allow-Methods", method);
        response.setHeader("Allow", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS");
      }
      String headers = request.getHeader("Access-Control-Request-Headers");
      if (headers != null) {
        response.addHeader("Access-Control-Allow-Headers", headers);
      }
      response.setContentType("text/plain");
    }
    super.service(request, response);
  }

  private boolean checkCORS(HttpServletRequest request, HttpServletResponse response) {
    String origin = request.getHeader("Origin");
    if (origin != null && origin.matches(corsDomainsRegex)) {
      // Maybe the user has used this domain before and has a session-cookie, we delete it
      //   Cookie c  = new Cookie("JSESSIONID", "");
      //   c.setMaxAge(0);
      //   response.addCookie(c);
      // All doXX methods should set these header
      response.addHeader("Access-Control-Allow-Origin", origin);
      response.addHeader("Access-Control-Allow-Credentials", "true");
      return true;
    } else if (origin != null) {
      logger.error("checkCORS error Origin: " + origin + " does not match:" + corsDomainsRegex);
    }
    return false;
  }

  /**
   * Writes an XML response to the client.
   */
  protected void renderHtmlMessage(HttpServletResponse response, String message) throws IOException {
    renderMessage(response, message, "text/html");
  }

  /**
   * Writes a XML response to the client.
   * The message must be a text which will be wrapped in an XML structure.
   *
   * Note: if the request is a POST, the response should set the content type
   *  to text/html or text/plain in order to be able in the client side to
   *  read the iframe body (submitCompletEvent.getResults()), otherwise the
   *  method returns null
   *
   * @param request
   * @param response
   * @param message
   * @param post
   *        specify whether the request is post or not.
   * @throws IOException
   */
  protected static void renderXmlResponse(HttpServletRequest request, HttpServletResponse response, String message, boolean post) throws IOException {
    String contentType = post ? "text/plain" : "text/html";

    String xml = XML_TPL.replace("%%MESSAGE%%", message != null ? message : "");
    if (post) {
      xml = TAG_MSG_START + xml.replaceAll("<", TAG_MSG_LT).replaceAll(">", TAG_MSG_GT) + TAG_MSG_END;
    }

    renderMessage(response, xml, contentType);
  }

  protected static void renderXmlResponse(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
    renderXmlResponse(request, response, message, false);
  }

  protected static void setThreadLocalRequest(HttpServletRequest request) {
    perThreadRequest.set(request);
  }

  /**
   * Simple method to get a string from the exception stack.
   *
   * @param e
   * @return string
   */
  protected static String stackTraceToString(Throwable e) {
    StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    return writer.getBuffer().toString();
  }

  protected long maxSize = DEFAULT_REQUEST_LIMIT_KB;

  protected  long maxFileSize = DEFAULT_REQUEST_LIMIT_KB;

  protected int uploadDelay = 0;

  protected boolean useBlobstore = false;

  /**
   * Mark the current upload process to be canceled.
   *
   * @param request
   */
  public void cancelUpload(HttpServletRequest request) {
    logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") cancelling Upload");
    AbstractUploadListener listener = getCurrentListener(request);
    if (listener != null && !listener.isCanceled()) {
      listener.setException(new UploadCanceledException());
    }
  }

  /**
   * Override this method if you want to check the request before it is passed
   * to commons-fileupload parser.
   *
   * @param request
   * @throws RuntimeException
   */
  public void checkRequest(HttpServletRequest request) {
    logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") procesing a request with size: " + getContentLength(request) + " bytes.");
    if (getContentLength(request) > maxSize) {
      throw new UploadSizeLimitException(maxSize, getContentLength(request));
    }
  }

  /**
   * Get an uploaded file item.
   *
   * @param request
   * @param response
   * @throws IOException
   */
  public void getUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String parameter = request.getParameter(UConsts.PARAM_SHOW);
    FileItem item = findFileItem(getMySessionFileItems(request), parameter);
    if (item != null) {
      logger.error("UPLOAD-SERVLET (" + request.getSession().getId() + ") getUploadedFile: " + parameter + " returning: " + item.getContentType() + ", " + item.getName() + ", " + item.getSize()
            + " bytes");
      response.setContentType(item.getContentType());
      copyFromInputStreamToOutputStream(item.getInputStream(), response.getOutputStream());
    } else {
      logger.error("UPLOAD-SERVLET (" + request.getSession().getId() + ") getUploadedFile: " + parameter + " file isn't in session.");
      renderXmlResponse(request, response, XML_ERROR_ITEM_NOT_FOUND);
    }
  }

  @Override
  public String getInitParameter(String name) {
    String value = getServletContext().getInitParameter(name);
    if (value == null) {
      value = super.getInitParameter(name);
    }
    return value;
  }

  /**
   * Read configurable parameters during the servlet initialization.
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    String size = getInitParameter("maxSize");
    if (size != null) {
      try {
        maxSize = Long.parseLong(size);
      } catch (NumberFormatException e) {
      }
    }

    String fileSize = getInitParameter("maxFileSize");
    if (null != fileSize){
      try {
        maxFileSize = Long.parseLong(fileSize);
      } catch (NumberFormatException e){
      }
    }

    String slow = getInitParameter("slowUploads");
    if (slow != null) {
      if ("true".equalsIgnoreCase(slow)) {
        uploadDelay = DEFAULT_SLOW_DELAY_MILLIS;
      } else {
        try {
          uploadDelay = Integer.valueOf(slow);
        } catch (NumberFormatException e) {
        }
      }
    }

    String timeout = getInitParameter("noDataTimeout");
    if (timeout != null){
      try {
        UploadListener.setNoDataTimeout(Integer.parseInt(timeout));
      } catch (NumberFormatException e) {
      }
    }

    String appe = getInitParameter("appEngine");
    if (appe != null) {
      appEngine = "true".equalsIgnoreCase(appe);
    } else {
      appEngine = isAppEngine();
    }

    String cors = getInitParameter("corsDomainsRegex");
    if (cors != null) {
      corsDomainsRegex = cors;
    }

    logger.info("UPLOAD-SERVLET init: maxSize=" + maxSize + ", slowUploads=" + slow + ", isAppEngine=" + isAppEngine() + ", corsRegex=" + corsDomainsRegex);
  }

  /**
   * Create a new listener for this session.
   *
   * @param request
   * @return the appropriate listener
   */
  protected AbstractUploadListener createNewListener(HttpServletRequest request) {
    int delay = request.getParameter("nodelay") != null ? 0 : uploadDelay;
    if (isAppEngine()) {
      return new MemoryUploadListener(delay, getContentLength(request));
    } else {
      return new UploadListener(delay, getContentLength(request));
    }
  }

  private long getContentLength(HttpServletRequest request) {
    long size = -1;
    try {
      size = Long.parseLong(request.getHeader(FileUploadBase.CONTENT_LENGTH));
    } catch (NumberFormatException e) {
    }
    return size;
  }


  /**
   * The get method is used to monitor the uploading process or to get the
   * content of the uploaded files.
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    perThreadRequest.set(request);
    try {
      AbstractUploadListener listener = getCurrentListener(request);
      if (request.getParameter(UConsts.PARAM_SESSION) != null) {
        logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") new session, blobstore=" + (isAppEngine() && useBlobstore));
        String sessionId = request.getSession().getId();
        renderXmlResponse(request, response,
            "<" + TAG_BLOBSTORE + ">" + (isAppEngine() && useBlobstore) + "</" + TAG_BLOBSTORE + ">" +
            "<" + TAG_SESSION_ID + ">" + sessionId + "</" + TAG_SESSION_ID + ">");
      } else if (isAppEngine() && (request.getParameter(UConsts.PARAM_BLOBSTORE) != null || request.getParameterMap().size() == 0)) {
        String blobStorePath = getBlobstorePath(request);
        logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") getBlobstorePath=" + blobStorePath);
        renderXmlResponse(request, response, "<" + TAG_BLOBSTORE_PATH + ">" + blobStorePath + "</" + TAG_BLOBSTORE_PATH + ">");
      } else if (request.getParameter(UConsts.PARAM_SHOW) != null) {
        getUploadedFile(request, response);
      } else if (request.getParameter(UConsts.PARAM_CANCEL) != null) {
        cancelUpload(request);
        renderXmlResponse(request, response, XML_CANCELED_TRUE);
      } else if (request.getParameter(UConsts.PARAM_REMOVE) != null) {
        removeUploadedFile(request, response);
      } else if (request.getParameter(UConsts.PARAM_CLEAN) != null) {
        logger.debug("UPLOAD-SERVLET (" + request.getSession().getId() + ") cleanListener");
        if (listener != null) {
          listener.remove();
        }
        renderXmlResponse(request, response, XML_FINISHED_OK);
      } else if (listener != null && listener.isFinished()) {
        removeCurrentListener(request);
        renderXmlResponse(request, response, listener.getPostResponse());
      } else {
        String message = statusToString(getUploadStatus(request, request.getParameter(UConsts.PARAM_FILENAME), null));
        renderXmlResponse(request, response, message);
      }
    } finally {
      perThreadRequest.set(null);
    }
  }

  protected String statusToString(Map<String, String> stat) {
    String message = "";
    for (Entry<String, String> e : stat.entrySet()) {
      if (e.getValue() != null) {
        String k = e.getKey();
        String v = e.getValue().replaceAll("</*pre>", "").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
        message += "<" + k + ">" + v + "</" + k + ">\n";
      }
    }
    return message;
  }

  /**
   * The post method is used to receive the file and save it in the user
   * session. It returns a very XML page that the client receives in an
   * iframe.
   *
   * The content of this xml document has a tag error in the case of error in
   * the upload process or the string OK in the case of success.
   *
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    perThreadRequest.set(request);
    String error;
    try {
      error = parsePostRequest(request, response);
      Map<String, String> stat = new HashMap<String, String>();
      if (error != null && error.length() > 0 ) {
        stat.put(TAG_ERROR, error);
      } else {
        getFileItemsSummary(request, stat);
      }
      String postResponse = statusToString(stat);
      finish(request, postResponse);
      renderXmlResponse(request, response, postResponse, true);
    } catch (UploadCanceledException e) {
      renderXmlResponse(request, response, XML_CANCELED_TRUE, true);
    } catch (UploadTimeoutException e) {
      renderXmlResponse(request, response, XML_ERROR_TIMEOUT, true);
    } catch (UploadSizeLimitException e) {
      renderXmlResponse(request, response, "<" + TAG_ERROR + ">" + e.getMessage() + "</" + TAG_ERROR + ">", true);
    } catch (Exception e) {
      logger.error("UPLOAD-SERVLET (" + request.getSession().getId() + ") Exception -> " + e.getMessage() + "\n" + stackTraceToString(e));
      error = e.getMessage();
      renderXmlResponse(request, response, "<" + TAG_ERROR + ">" + error + "</" + TAG_ERROR + ">", true);
    } finally {
      perThreadRequest.set(null);
    }
  }

  protected Map<String, String> getFileItemsSummary(HttpServletRequest request, Map<String, String> stat) {
    if (stat == null) {
      stat = new HashMap<String, String>();
    }
    List<FileItem> s = getMyLastReceivedFileItems(request);
    if (s != null) {
      String files = "";
      String params = "";
      for (FileItem i : s) {
        if (i.isFormField()) {
          params += formFieldToXml(i);
        } else {
          files += fileFieldToXml(i);
        }
      }
      stat.put(TAG_FILES, files);
      stat.put(TAG_PARAMS, params);
      stat.put(TAG_FINISHED, "ok");
    }
    return stat;
  }

  private String formFieldToXml(FileItem i) {
    Map<String, String> item = new HashMap<String, String>();
    item.put(TAG_VALUE, "" + i.getString());
    item.put(TAG_FIELD, "" + i.getFieldName());

    Map<String, String> param = new HashMap<String, String>();
    param.put(TAG_PARAM, statusToString(item));
    return statusToString(param);
  }

  private String fileFieldToXml(FileItem i) {
    Map<String, String> item = new HashMap<String, String>();
    item.put(TAG_CTYPE, i.getContentType() !=null ? i.getContentType() : "unknown");
    item.put(TAG_SIZE, "" + i.getSize());
    item.put(TAG_NAME, "" + i.getName());
    item.put(TAG_FIELD, "" + i.getFieldName());
    if (i instanceof HasKey) {
      String k = ((HasKey)i).getKeyString();
      item.put(TAG_KEY, k);
    }

    Map<String, String> file = new HashMap<String, String>();
    file.put(TAG_FILE, statusToString(item));
    return statusToString(file);
  }

/**
   * Notify to the listener that the upload has finished.
   *
   * @param request
 * @param postResponse
   */
  protected void finish(HttpServletRequest request, String postResponse) {
    AbstractUploadListener listener = getCurrentListener(request);
    if (listener != null) {
      listener.setFinished(postResponse);
    }
  }

  protected String getBlobstorePath(HttpServletRequest request) {
    return null;
  }

  /**
   * Get the listener active in this session.
   *
   * @param request
   * @return the listener active
   */
  protected AbstractUploadListener getCurrentListener(HttpServletRequest request) {
    if (isAppEngine()) {
      return MemoryUploadListener.current(request.getSession().getId());
    } else {
      return UploadListener.current(request);
    }
  }

  /**
   * Override this method if you want to implement a different ItemFactory.
   *
   * @return FileItemFactory
   */
  protected FileItemFactory getFileItemFactory(long requestSize) {
    return new DefaultFileItemFactory();
  }

  /**
   * Method executed each time the client asks the server for the progress status.
   * It uses the listener to generate the adequate response
   *
   * @param request
   * @param fieldname
   * @return a map of tag/values to be rendered
   */
  protected Map<String, String> getUploadStatus(HttpServletRequest request, String fieldname, Map<String, String> ret) {
    perThreadRequest.set(request);
    HttpSession session = request.getSession();

    if (ret == null) {
      ret = new HashMap<String, String>();
    }

    long currentBytes = 0;
    long totalBytes = 0;
    long percent = 0;
    AbstractUploadListener listener = getCurrentListener(request);
    if (listener != null) {
      if (listener.isFinished()) {

      } else if (listener.getException() != null) {
        if (listener.getException() instanceof UploadCanceledException) {
          ret.put(TAG_CANCELED, "true");
          ret.put(TAG_FINISHED, TAG_CANCELED);
          logger.error("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + fieldname + " canceled by the user after " + listener.getBytesRead() + " Bytes");
        } else {
          String errorMsg = getMessage("server_error", listener.getException().getMessage());
          ret.put(TAG_ERROR, errorMsg);
          ret.put(TAG_FINISHED, TAG_ERROR);
          logger.error("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + fieldname + " finished with error: " + listener.getException().getMessage());
        }
      } else {
        currentBytes = listener.getBytesRead();
        totalBytes = listener.getContentLength();
        percent = totalBytes != 0 ? currentBytes * 100 / totalBytes : 0;
        // logger.debug("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + fieldname + " " + currentBytes + "/" + totalBytes + " " + percent + "%");
        ret.put(TAG_PERCENT, "" + percent);
        ret.put(TAG_CURRENT_BYTES, "" + currentBytes);
        ret.put(TAG_TOTAL_BYTES, "" + totalBytes);
      }
    } else if (getMySessionFileItems(request) != null) {
      if (fieldname == null) {
        ret.put(TAG_FINISHED, "ok");
        logger.debug("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + request.getQueryString() +
            " finished with files: " + session.getAttribute(getSessionFilesKey(request)));
      } else {
        List<FileItem> sessionFiles = getMySessionFileItems(request);
        for (FileItem file : sessionFiles) {
          if (file.isFormField() == false && file.getFieldName().equals(fieldname)) {
            ret.put(TAG_FINISHED, "ok");
            ret.put(UConsts.PARAM_FILENAME, fieldname);
            logger.debug("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: " + fieldname +
                " finished with files: " + session.getAttribute(getSessionFilesKey(request)));
          }
        }
      }
    } else {
      logger.debug("UPLOAD-SERVLET (" + session.getId() + ") getUploadStatus: no listener in session");
      ret.put("wait", "listener is null");
    }
    if (ret.containsKey(TAG_FINISHED)) {
      removeCurrentListener(request);
    }
    perThreadRequest.set(null);
    return ret;
  }

  /**
   * This method parses the submit action, puts in session a listener where the
   * progress status is updated, and eventually stores the received data in
   * the user session.
   *
   * returns null in the case of success or a string with the error
   *
   */
  protected String parsePostRequest(HttpServletRequest request, HttpServletResponse response) {

    try {
      String delay = request.getParameter(PARAM_DELAY);
      String maxFilesize = request.getParameter(PARAM_MAX_FILE_SIZE);
      maxSize = maxFilesize != null && maxFilesize.matches("[0-9]*") ? Long.parseLong(maxFilesize) : maxSize;
      uploadDelay = Integer.parseInt(delay);
    } catch (Exception e) { }

    HttpSession session = request.getSession();

    logger.debug("UPLOAD-SERVLET (" + session.getId() + ") new upload request received.");

    AbstractUploadListener listener = getCurrentListener(request);
    if (listener != null) {
      if (listener.isFrozen() || listener.isCanceled() || listener.getPercent() >= 100) {
        removeCurrentListener(request);
      } else {
        String error = getMessage("busy");
        logger.error("UPLOAD-SERVLET (" + session.getId() + ") " + error);
        return error;
      }
    }

    // Create a file upload progress listener, and put it in the user session,
    // so the browser can use ajax to query status of the upload process
    listener = createNewListener(request);

    List<FileItem> uploadedItems;
    try {

      // Call to a method which the user can override
      checkRequest(request);

      // Create the factory used for uploading files,
      FileItemFactory factory = getFileItemFactory(getContentLength(request));
      ServletFileUpload uploader = new ServletFileUpload(factory);
      uploader.setSizeMax(maxSize);
      uploader.setFileSizeMax(maxFileSize);
      uploader.setProgressListener(listener);

      // Receive the files
      logger.error("UPLOAD-SERVLET (" + session.getId() + ") parsing HTTP POST request ");
      uploadedItems = uploader.parseRequest(request);
      session.removeAttribute(getSessionLastFilesKey(request));
      logger.error("UPLOAD-SERVLET (" + session.getId() + ") parsed request, " + uploadedItems.size() + " items received.");

      // Received files are put in session
      List<FileItem> sessionFiles = getMySessionFileItems(request);
      if (sessionFiles == null) {
        sessionFiles = new ArrayList<FileItem>();
      }

      String error = "";
      if (uploadedItems.size() > 0) {
        sessionFiles.addAll(uploadedItems);
        String msg = "";
        for (FileItem i : sessionFiles) {
          msg += i.getFieldName() + " => " + i.getName() + "(" + i.getSize() + " bytes),";
        }
        logger.debug("UPLOAD-SERVLET (" + session.getId() + ") puting items in session: " + msg);
        session.setAttribute(getSessionFilesKey(request), sessionFiles);
        session.setAttribute(getSessionLastFilesKey(request), uploadedItems);
      } else if (!isAppEngine()){
        logger.error("UPLOAD-SERVLET (" + session.getId() + ") error NO DATA received ");
        error += getMessage("no_data");
      }
      return error.length() > 0 ? error : null;

    // So much silly questions in the list about this issue.
    } catch(LinkageError e) {
      logger.error("UPLOAD-SERVLET (" + request.getSession().getId() + ") Exception: " + e.getMessage() + "\n" + stackTraceToString(e));
      RuntimeException ex = new UploadActionException(getMessage("restricted", e.getMessage()), e);
      listener.setException(ex);
      throw ex;
    } catch (SizeLimitExceededException e) {
      RuntimeException ex = new UploadSizeLimitException(e.getPermittedSize(), e.getActualSize());
      listener.setException(ex);
      throw ex;
    } catch (UploadSizeLimitException e) {
      listener.setException(e);
      throw e;
    } catch (UploadCanceledException e) {
      listener.setException(e);
      throw e;
    } catch (UploadTimeoutException e) {
      listener.setException(e);
      throw e;
    } catch (Throwable e) {
      logger.error("UPLOAD-SERVLET (" + request.getSession().getId() + ") Unexpected Exception -> " + e.getMessage() + "\n" + stackTraceToString(e));
      e.printStackTrace();
      RuntimeException ex = new UploadException(e);
      listener.setException(ex);
      throw ex;
    }
  }

  /**
   * Remove the listener active in this session.
   *
   * @param request
   */
  protected void removeCurrentListener(HttpServletRequest request) {
    AbstractUploadListener listener = getCurrentListener(request);
    if (listener != null) {
      listener.remove();
    }
  }

  /**
   * Override this to provide a session key which allow to differentiate between
   * multiple instances of uploaders in an application with the same session but
   * who do not wish to share the uploaded files.
   * Example:
   * protected String getSessionFilesKey(HttpServletRequest request) {
   *  return getSessionFilesKey(request.getParameter("randomNumber"));
   * }
   *
   * public static String getSessionFilesKey(String parameter) {
   *  return "SESSION_FILES_" + parameter;
   * }
   *
   */
  protected String getSessionFilesKey(HttpServletRequest request) {
    return SESSION_FILES;
  }

  /**
   * Override this to provide a session key which allow to differentiate between
   * multiple instances of uploaders in an application with the same session but
   * who do not wish to share the uploaded files.
   * See getSessionFilesKey() for an example.
   */
  protected String getSessionLastFilesKey(HttpServletRequest request) {
    return SESSION_LAST_FILES;
  }

  /**
   * DiskFileItemFactory for Multiple file selection.
   */
  public static class DefaultFileItemFactory extends DiskFileItemFactory {
    private HashMap<String, Integer> map = new HashMap<String, Integer>();

    @Override
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
      Integer cont = map.get(fieldName) != null ? (map.get(fieldName) + 1): 0;
      map.put(fieldName, cont);
      fieldName = fieldName.replace(MULTI_SUFFIX, "") + "-" + cont;
      return super.createItem(fieldName, contentType, isFormField, fileName);
    }
  }
}
