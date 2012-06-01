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
package gwtupload.server.gae;

import static gwtupload.shared.UConsts.*;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import gwtupload.server.gae.FilesApiFileItemFactory.FilesAPIFileItem;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * Upload servlet which uses the FilesApiFileItemFactory using the GAE File API.
 * 
 * To use this servlet you need:
 * 
 * Add these lines to your web.xml
 * 
 * <pre>
  &lt;servlet>
    &lt;servlet-class>gwtupload.server.gae.FilesApiUploadAction&lt;/servlet-class>
  &lt;/servlet>
  &lt;servlet-mapping>
    &lt;servlet-name>uploadServlet&lt;/servlet-name>
    &lt;url-pattern>*.gupld&lt;/url-pattern>
  &lt;/servlet-mapping>
  &lt;servlet-mapping>
    &lt;servlet-name>uploadServlet&lt;/servlet-name>
    &lt;url-pattern>/upload&lt;/url-pattern>
  &lt;/servlet-mapping> 
 </pre>
 * 
 * Enable Session in your appengine-web.xml
 * 
 * <pre>
  &lt;sessions-enabled>true&lt;/sessions-enabled>
</pre>
 * 
 * You can get the blob key in server client side using this code
 * 
 * <pre>
  uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
    public void onFinish(IUploader uploader) {
      if (uploader.getStatus() == Status.SUCCESS) {
        String url = uploader.getServletPath() + "?blob-key=" + uploader.getServerInfo().message;
      }
    }
  });
</pre>
 * 
 * @author Vyacheslav Sokolov
 * @author Manuel Carrasco
 */
public class FilesApiUploadAction extends UploadAction {
  private static final long serialVersionUID = 3683112300714613746L;
  
  
  @Override
  public boolean isAppEngine() {
    return true;
  }
  
  @Override
  public String executeAction(HttpServletRequest request,
      List<FileItem> sessionFiles) throws UploadActionException {
    String ret = "";
    for (FileItem i : sessionFiles) {
      if (!i.isFormField()) {
        ret += (ret.isEmpty() ? "" : " ") + ((FilesAPIFileItem) i).getKey().getKeyString();
        logger.info("Received new file, stored in blobstore with the key: " + ret);
      }
    }
    return ret;
  }

  @Override
  protected FileItemFactory getFileItemFactory(int requestSize) {
    return new FilesApiFileItemFactory();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String bkey = request.getParameter(PARAM_BLOBKEY);
    if (bkey != null) {
      logger.info("Serving a blobstore file with the key:" + bkey);
      FilesAPIFileItem.getBlobstoreService()
        .serve(new BlobKey(bkey), response);
    } else {
      super.doGet(request, response);
    }
  }
}