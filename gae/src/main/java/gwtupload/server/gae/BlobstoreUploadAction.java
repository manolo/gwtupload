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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static gwtupload.shared.UConsts.PARAM_BLOBKEY;
import static gwtupload.shared.UConsts.PARAM_CANCEL;
import static gwtupload.shared.UConsts.PARAM_ERROR;
import static gwtupload.shared.UConsts.PARAM_MESSAGE;
import static gwtupload.shared.UConsts.PARAM_REDIRECT;
import static gwtupload.shared.UConsts.RESP_OK;
import static gwtupload.shared.UConsts.TAG_CANCELED;
import static gwtupload.shared.UConsts.TAG_ERROR;
import static gwtupload.shared.UConsts.TAG_FINISHED;
import static gwtupload.shared.UConsts.TAG_MESSAGE;
import gwtupload.server.AbstractUploadListener;
import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import gwtupload.server.exceptions.UploadCanceledException;
import gwtupload.server.gae.BlobstoreFileItemFactory.BlobstoreFileItem;
import gwtupload.shared.UConsts;

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
 *
 * @author Manolo Carrasco Moñino
 *
 */
public class BlobstoreUploadAction extends UploadAction {

  private static final long serialVersionUID = -2569300604226532811L;

  protected static BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

  // See constrain 1
  private String servletPath = "/upload";

  @Override
  public void checkRequest(HttpServletRequest request) {
    logger.debug("BLOB-STORE-SERVLET: (" + request.getSession().getId() + ") procesing a request with size: " + request.getContentLength() + " bytes.");
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

  @SuppressWarnings("serial")
  @Override
  protected final AbstractUploadListener createNewListener(
      HttpServletRequest request) {
    return new MemCacheUploadListener(uploadDelay, request.getContentLength()) {
      int cont = 1;
      // In blobStore we cannot know the fileSize emulating progress
      public long getBytesRead() {
        return getContentLength() / (isFinished() ? 1 : 10 * (1+ (cont++ % 10)));
      }
      public long getContentLength() {
        return 100;
      }
    };
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    if (request.getParameter(PARAM_BLOBKEY) != null) {
      BlobKey blobKey = new BlobKey(request.getParameter("blob-key"));
      blobstoreService.serve(blobKey, response);
      try {
        logger.info("BLOB-STORE-SERVLET: Serving blobstore file. " + request.getParameter(PARAM_BLOBKEY));
        blobstoreService.serve(new BlobKey(request.getParameter(PARAM_BLOBKEY)), response);
      } catch (Throwable e) {
        logger.info("BLOB-STORE-SERVLET: Exception accessing blobStoreService:" + e.getMessage());
        e.printStackTrace();
        renderXmlResponse(request, response, "Error getting blob: " + e.getMessage() + " " + request.getParameter(PARAM_BLOBKEY));
      }
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
      finish(request, ret);

      logger.debug("BLOB-STORE-SERVLET: (" + request.getSession().getId() + ") redirect \n" + ret);
      renderXmlResponse(request, response, ret, true);
      perThreadRequest.set(null);
    } else {
      super.doGet(request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String error = null;
    String message = null;
    perThreadRequest.set(request);
    try {
      error = super.parsePostRequest(request, response);
    } catch (UploadCanceledException e) {
      redirect(response, PARAM_CANCEL + "=true");
      return;
    } catch (Exception e) {
      logger.info("BLOB-STORE-SERVLET: Exception " + e);
      error = e.getMessage();
    } finally {
      perThreadRequest.set(null);
    }

    if (error != null) {
      removeSessionFileItems(request);
      redirect(response, PARAM_ERROR + "=" + error);
      perThreadRequest.set(null);
      return;
    }

    List<FileItem> sessionFiles = getMySessionFileItems(request);
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    if (blobs != null && blobs.size() > 0) {
      List<FileItem> receivedFiles = new ArrayList<FileItem>();
      for (Entry<String, List<BlobKey>> e: blobs.entrySet()) {
        for (BlobKey blob: e.getValue()) {
          logger.error("Entry .... " + e.getKey() + " " + blob.getKeyString() + " " + blob);
          String type = "unknown";
          String name = e.getKey();
          if (sessionFiles != null) {
            for (FileItem i : sessionFiles) {
              if (i.getFieldName().replaceFirst("-\\d+$", "").equals(e.getKey().replace(UConsts.MULTI_SUFFIX, ""))) {
                type = i.getContentType().replaceAll(";.*$", "");
                name = i.getFieldName();
                break;
              }
            }
          }
          BlobstoreFileItem i = new BlobstoreFileItem(name, type, false, "");
          i.setKey(blob);
          receivedFiles.add(i);
          logger.info("BLOB-STORE-SERVLET: received file: " + e.getKey() + " " + i);
        }
      }
      logger.error("BLOB-STORE-SERVLET: putting in sesssion elements -> " + receivedFiles.size());
      sessionFiles = new ArrayList<FileItem>(receivedFiles);

      request.getSession().setAttribute(getSessionFilesKey(request), receivedFiles);
      request.getSession().setAttribute(getSessionLastFilesKey(request), sessionFiles);
    } else {
      error = getMessage("no_data");
    }

    try {
      message = executeAction(request, getMySessionFileItems(request));
    } catch (UploadActionException e) {
      logger.error("ExecuteUploadActionException: " + e);
      error =  e.getMessage();
    }

    redirect(response, message != null ? PARAM_MESSAGE + "=" + message : null);
    perThreadRequest.set(null);
  }

  /**
   * User can override this for customization.
   */
  public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles)
      throws UploadActionException {
    return null;
  }

  protected void redirect(HttpServletResponse response, String params) throws IOException {
    String url = servletPath + "?" + PARAM_REDIRECT + "=true" + (params != null ? "&" + params.replaceAll("[\n\r]+", " ") : "");
    logger.info("BLOB-STORE-SERVLET: redirecting to -> : " + url);
    response.sendRedirect(url);
  }

  protected String getBlobstorePath(HttpServletRequest request) {
    String ret = blobstoreService.createUploadUrl(servletPath);
    logger.info("BLOB-STORE-SERVLET: generated new upload-url -> " + servletPath + " : " + ret);
    ret = ret.replaceFirst("^https?://[^/]+", "");
    return ret;
  }

  @Override
  protected final AbstractUploadListener getCurrentListener(
      HttpServletRequest request) {
    return MemCacheUploadListener.current(request.getSession().getId());
  }

  @Override
  protected final FileItemFactory getFileItemFactory(long requestSize) {
    return new BlobstoreFileItemFactory();
  }
}
