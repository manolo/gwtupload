/*
 * Copyright 2010 Manuel Carrasco Moñino. (manuel_carrasco at
 * users.sourceforge.net) http://code.google.com/p/gwtupload
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
package gwtupload.server.gae;

import static gwtupload.shared.UConsts.*;

import gwtupload.server.AbstractUploadListener;
import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import gwtupload.server.exceptions.UploadCanceledException;
import gwtupload.server.gae.BlobstoreFileItemFactory.BlobstoreFileItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * <p>
 * Upload servlet for the GwtUpload library's deployed in Google App-engine using blobstore.
 * </p>
 * 
 * <p>
 * Constrains:
 * <ul>
 *   <li>It seems that the redirected servlet path must be /upload because wildcards don't work.</li>
 *   <li>After of blobstoring, our doPost receives a request size = -1.</li>
 *   <li>If the upload fails, our doPost is never called.</li>
 * </ul>
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class BlobstoreUploadAction extends UploadAction {

  protected static BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  protected static DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
  protected static BlobInfoFactory  blobInfoFactory = new BlobInfoFactory(datastoreService);
  
  private static final long serialVersionUID = -2569300604226532811L;
  
  // See constrain 1
  private String servletPath = "/upload";
  
  @Override
  public void checkRequest(HttpServletRequest request) {
    logger.debug("BLOB-STORE-SERVLET: (" + request.getSession().getId() + ") procesing a request with size: " + request.getContentLength() + " bytes.");
  }

  @Override
  public void getUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String parameter = request.getParameter(PARAM_SHOW);
    FileItem item = findFileItem(getSessionFileItems(request), parameter);
    if (item != null) {
      BlobInfo i = blobInfoFactory.loadBlobInfo(((BlobstoreFileItem) item).getKey());
      if (i != null) {
        logger.debug("BLOB-STORE-SERVLET: (" + request.getSession().getId() + ") getUploadedFile: " + parameter + " serving blobstore: " + i);
        blobstoreService.serve(((BlobstoreFileItem) item).getKey(), response);
      } else {
        logger.error("BLOB-STORE-SERVLET: (" + request.getSession().getId() + ") getUploadedFile: " + parameter + " file isn't in blobstore.");
      }
    } else {
      logger.info("BLOB-STORE-SERVLET: (" + request.getSession().getId() + ") getUploadedFile: " + parameter + " file isn't in session.");
      renderXmlResponse(request, response, XML_ERROR_ITEM_NOT_FOUND);
    }
  }
  
  @Override
  public boolean isAppEngine() {
    return true;
  }
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    uploadDelay = 0;
    useBlobstore = true;
    logger.info("BLOB-STORE-SERVLET: init: maxSize=" + maxSize
        + ", slowUploads=" + uploadDelay + ", isAppEngine=" + isAppEngine()
        + ", useBlobstore=" + useBlobstore); 
  }
  
  @Override
  protected final AbstractUploadListener createNewListener(
      HttpServletRequest request) {
    return new MemCacheUploadListener(uploadDelay, request.getContentLength());
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    if (request.getParameter(PARAM_BLOBKEY) != null) {
      blobstoreService.serve(new BlobKey(request.getParameter(PARAM_BLOBKEY)), response);
    } else if (request.getParameter(PARAM_REDIRECT) != null) {
      perThreadRequest.set(request);
      Map<String, String> stat = new HashMap<String, String>();
      if (request.getParameter(PARAM_MESSAGE) != null) {
        stat.put(TAG_MESSAGE, request.getParameter(PARAM_MESSAGE));
      }
      if (request.getParameter(PARAM_ERROR) != null) {
        stat.put(TAG_ERROR, request.getParameter(PARAM_ERROR));
      } else if (request.getParameter(PARAM_CANCEL) != null) {
        stat.put(TAG_CANCELED, request.getParameter(PARAM_CANCEL));
      } else  {
        getFileItemsSummary(request, stat);
      }
      stat.put(TAG_FINISHED, RESP_OK);

      String ret = statusToString(stat);
      finish(request);
      logger.debug("BLOB-STORE-SERVLET: (" + request.getSession().getId() + ") redirect \n" + ret);
      renderXmlResponse(request, response, ret, true);
      perThreadRequest.set(null);
    } else {
      super.doGet(request, response);
    }
  }
  
  @Override
  protected Map<String, String> getUploadStatus(HttpServletRequest request,
      String fieldname, Map<String, String> ret) {
    ret.put(TAG_PERCENT, "50");
    ret.put(TAG_CURRENT_BYTES, "0");
    ret.put(TAG_TOTAL_BYTES, "0" );    
    return ret;
  }
  
  @Override
  protected Map<String, String> getFileItemsSummary(HttpServletRequest request, Map<String, String> ret) {
    ret = super.getFileItemsSummary(request, ret);
    for (FileItem item : getLastReceivedFileItems(request)) {
      if (!item.isFormField()) {
        BlobKey k = ((BlobstoreFileItem) item).getKey();
        if (k != null && k.getKeyString() != null) {
          ret.put(TAG_KEY, k.getKeyString());
          
          // legacy
          ret.put("blobkey", k.getKeyString());
        }
      }
    }
    return ret;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String error = null;
    String message = null;
    
    if (request.getContentLength() > 0) {
      perThreadRequest.set(request);
      try {
        error = super.parsePostRequest(request, response);
      } catch (UploadCanceledException e) {
        finish(request);
        redirect(response, PARAM_CANCEL + "=true");
        return;
      } catch (Exception e) {
        logger.info("BLOB-STORE-SERVLET: Exception " + e);
        error = e.getMessage();
      } finally {
        perThreadRequest.set(null);
      }
    }

    if (error != null) {
      removeSessionFileItems(request);
      removeCurrentListener(request);
      redirect(response, PARAM_ERROR + "=" + error);
      return;
    } 

    @SuppressWarnings("deprecation")
    Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(request);
    if (blobs != null && blobs.size() > 0) {
      List<FileItem> receivedFiles = new Vector<FileItem>();
      for (Entry<String, BlobKey> e: blobs.entrySet()) {
        BlobstoreFileItem i = new BlobstoreFileItem(e.getKey(), "unknown", false, "");
        logger.info("BLOB-STORE-SERVLET: received file: " + e.getKey() + " " + e.getValue().getKeyString());
        i.setKey(e.getValue());
        receivedFiles.add(i);
      }
      
      logger.info("BLOB-STORE-SERVLET: putting in sesssion elements -> " + receivedFiles.size());
      
      Vector<FileItem> sessionFiles = (Vector<FileItem>) getSessionFileItems(request);
      if (sessionFiles == null) {
        sessionFiles = new Vector<FileItem>();
      }
      sessionFiles.addAll(receivedFiles);
      
      request.getSession().setAttribute(SESSION_FILES, receivedFiles);
      request.getSession().setAttribute(SESSION_LAST_FILES, sessionFiles);
    } else {
      error = getMessage("no_data");
    }
      
    try {
      message = executeAction(request, getSessionFileItems(request));
    } catch (UploadActionException e) {
      logger.info("ExecuteUploadActionException: " + e);
      error =  e.getMessage();
    }
    
    removeCurrentListener(request);
      
    redirect(response, message != null ? PARAM_MESSAGE + "=" + message : null);
  }
  
  protected void redirect(HttpServletResponse response, String params) throws IOException {
    String url = servletPath + "?" + PARAM_REDIRECT + "=true" + (params != null ? "&" + params.replaceAll("[\n\r]+", " ") : "");
    logger.info("BLOB-STORE-SERVLET: redirecting to -> : " + url);
    response.sendRedirect(url);
  }

  protected String getBlobstorePath(HttpServletRequest request) {
    String ret = blobstoreService.createUploadUrl(servletPath);
    ret = ret.replaceAll("^https*://[^/]+", "");
    logger.info("BLOB-STORE-SERVLET: generated new upload-url -> " + servletPath + " : " + ret);
    return ret;
  }

  @Override
  protected final AbstractUploadListener getCurrentListener(
      HttpServletRequest request) {
    return MemCacheUploadListener.current(request.getSession().getId());
  }

  @Override
  protected final FileItemFactory getFileItemFactory(int requestSize) {
    return new BlobstoreFileItemFactory();
  }
}
