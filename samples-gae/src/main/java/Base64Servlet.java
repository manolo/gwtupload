import com.google.appengine.repackaged.com.google.api.client.util.Base64;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gwtupload.server.gae.MemCacheFileItemFactory;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

public class Base64Servlet extends UploadAction {
  private static final long serialVersionUID = 1L;

  public Base64Servlet() {
    super();
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    super.service(request, response);
  }

  @Override
  protected FileItemFactory getFileItemFactory(long requestSize) {
    return new MemCacheFileItemFactory((int) requestSize);
  }

  @Override
  public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
    FileItem fi = sessionFiles.get(0);
    if (fi.isFormField()) {
      fi = sessionFiles.get(1);
    }
    byte[] bs = fi.get();
    String base64Picture = "data:" + fi.getContentType() + ";base64, ";
    base64Picture += new String(Base64.encodeBase64(bs));
    removeSessionFileItems(request, true);
    logger.info("Base64Servlet returns " + bs.length + " " + fi.getSize() + " " + base64Picture.length());
    return base64Picture;
  }
}