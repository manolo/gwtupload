package gwtupload.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.FileUpload;

/**
 * Extends GWT's Fileupload class as that one does not support the HTML5 feature of a
 * multiple-select uploader.
 *
 * @author ebluemelhuber
 *
 */
public class MultipleFileUpload extends FileUpload {

  public MultipleFileUpload() {
    super();
    // By default multiple is enabled;
    enableMultiple(true);
  }

  public List<String> getFilenames() {
    ArrayList<String> result = new ArrayList<String>();

    JavaScriptObject rawFileList = getElement().getPropertyJSO("files");
    if (rawFileList == null) {
      result.add(InputElement.as(getElement()).getValue()); // IE does not support multiple-select
    } else {
      FileList fileList = rawFileList.cast();
      for (int i = 0; i < fileList.getLength(); ++i) {
        result.add(fileList.item(i).getName());
      }
    }

    return result;
  }

  public void enableMultiple(boolean b) {
    String attr = "multiple";
    if (b) {
      getElement().setAttribute(attr, attr);
    } else {
      getElement().removeAttribute(attr);
    }
  }

  public void setAccept(String accept) {
    getElement().setAttribute("accept", accept);
  }
}

