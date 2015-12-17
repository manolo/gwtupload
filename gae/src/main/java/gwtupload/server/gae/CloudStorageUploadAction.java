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
import gwtupload.server.gae.CloudStorageFileItemFactory.CloudStorageFileItem;

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
  &lt;servlet&gt;
    &lt;servlet-class&gt;gwtupload.server.gae.FilesApiUploadAction&lt;/servlet-class&gt;
  &lt;/servlet&gt;
  &lt;servlet-mapping&gt;
    &lt;servlet-name&gt;uploadServlet&lt;/servlet-name&gt;
    &lt;url-pattern&gt;gupld&lt;/url-pattern&gt;
  &lt;/servlet-mapping&gt;
  &lt;servlet-mapping&gt;
    &lt;servlet-name&gt;uploadServlet&lt;/servlet-name&gt;
    &lt;url-pattern&gt;upload&lt;/url-pattern&gt;
  &lt;/servlet-mapping&gt;
 </pre>
 *
 * Enable Session in your appengine-web.xml
 *
 * <pre>
  &lt;sessions-enabled&gt;true&lt;/sessions-enabled&gt;
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
 * @author Manolo Carrasco Moñino
 */
public class CloudStorageUploadAction extends UploadAction {
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
        ret += (ret.isEmpty() ? "" : " ") + ((CloudStorageFileItem) i).getKey().getKeyString();
        logger.info("Received new file, stored in google cloud storage with the key: " + ret);
      }
    }
    return ret;
  }

  @Override
  protected FileItemFactory getFileItemFactory(long requestSize) {
    return new CloudStorageFileItemFactory();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String bkey = request.getParameter(PARAM_BLOBKEY);
    logger.info("Files doGet " + bkey);
    if (bkey != null) {
      logger.info("Serving a google cloud storage file with the key:" + bkey);
      CloudStorageFileItem.getBlobstoreService().serve(new BlobKey(bkey), response);
    } else {
      super.doGet(request, response);
    }
  }
}
