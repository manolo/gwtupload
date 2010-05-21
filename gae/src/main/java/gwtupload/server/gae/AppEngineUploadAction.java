/*
 * Copyright 2009 Manuel Carrasco Moñino. (manuel_carrasco at
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

import gwtupload.server.AbstractUploadListener;
import gwtupload.server.UploadAction;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemFactory;

/**
 * <p>
 * Upload servlet for the GwtUpload library's deployed in Google App-engine.
 * </p>
 * 
 * <h4>Limitations in Google Application Engine:</h4>
 * <ul>
 * <li>It doesn't support writing to file-system, so this servlet stores
 * fileItems in memcache using CacheableFileItem</li>
 * <li>The request size is limited to 512 KB, so this servlet hardcodes the
 * maxSize to 512</li>
 * <li>The limit for session and cache objects is 1024 KB</li>
 * <li>The time spent to process a request is limited, so this servlet limits
 * the sleep delay to a maximum of 50ms</li>
 * </ul>
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class AppEngineUploadAction extends UploadAction {

  private static final long serialVersionUID = -2569300604226532811L;

  @Override
  public void checkRequest(HttpServletRequest request) {
    super.checkRequest(request);
    if (request.getContentLength() > MemCacheFileItemFactory.DEFAULT_REQUEST_SIZE + 1024) {
      throw new RuntimeException(
          "Google appengine doesn't allow requests with a size greater than "
              + MemCacheFileItemFactory.DEFAULT_REQUEST_SIZE + " Bytes");
    }
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    if (isAppEngine()) {
      useBlobstore = false;
      if (uploadDelay > 0) {
        uploadDelay = Math.max(50, uploadDelay);
      }
      if (maxSize > MemCacheFileItemFactory.DEFAULT_REQUEST_SIZE) {
        maxSize = MemCacheFileItemFactory.DEFAULT_REQUEST_SIZE;
        logger.info("GAEE-UPLOAD-SERVLET init: maxSize=" + maxSize
            + ", slowUploads=" + uploadDelay + ", isAppEngine=" + isAppEngine()
            + ", useBlobstore=" + useBlobstore);
      }
    }
  }
  
  @Override
  protected final AbstractUploadListener createNewListener(
      HttpServletRequest request) {
    return new MemCacheUploadListener(uploadDelay, request.getContentLength());
  }

  @Override
  protected final AbstractUploadListener getCurrentListener(
      HttpServletRequest request) {
    return MemCacheUploadListener.current(request.getSession().getId());
  }

  @Override
  protected final FileItemFactory getFileItemFactory(int requestSize) {
    return new MemCacheFileItemFactory();
  }

}
