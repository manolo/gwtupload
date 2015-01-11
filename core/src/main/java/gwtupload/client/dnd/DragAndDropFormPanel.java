package gwtupload.client.dnd;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xhr.client.XMLHttpRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gwtupload.client.Uploader;

import gwtupload.client.FileList;
import gwtupload.client.IFileInput;

/**
 * DragAndDropFormPanel.
 *
 * @author Sultan Tezadov
 * @author Manolo Carrasco Moñino
 */
public class DragAndDropFormPanel extends FormPanel {

  @Override
  public void setAction(String url) {
    super.setAction(url);
  }

  @Override
  public void setEncoding(String encodingType) {
    super.setEncoding(encodingType);
  }

  @Override
  public void setMethod(String method) {
    super.setMethod(method);
  }

  @Override
  public HandlerRegistration addSubmitHandler(SubmitHandler handler) {
    return super.addSubmitHandler(handler);
  }

  @Override
  public HandlerRegistration addSubmitCompleteHandler(SubmitCompleteHandler handler) {
    return super.addSubmitCompleteHandler(handler);
  }

  @Override
  public void add(Widget w) {
    super.add(w);
  }

  @Override
  public void clear() {
    super.clear();
  }

  @Override
  public Iterator<Widget> iterator() {
    return super.iterator();
  }

  @Override
  public boolean remove(Widget w) {
    return super.remove(w);
  }

  @Override
  public void reset() {
    super.reset();
  }

  @Override
  public void submit() {
    Uploader.log("panel submit", null);
    final List<IFileInput> childFileInputs = getChildFileInputs();
    if (childFileInputs == null || childFileInputs.isEmpty()) {
      return;
    }
    final ArrayList<IDragAndDropFileInput> dndFileInputs = new ArrayList<IDragAndDropFileInput>();
    boolean thereAreNonDragAndDropFileInputs = false;
    for (IFileInput fileInput : childFileInputs) {
      if (fileInput instanceof IDragAndDropFileInput) {
        final IDragAndDropFileInput dndFileInput = (IDragAndDropFileInput) fileInput;
        if (dndFileInput.hasFiles()) {
          dndFileInputs.add(dndFileInput);
        } else {
          // files might be selected via file dialog
          thereAreNonDragAndDropFileInputs = true;
        }
      } else {
        // files are selected via file dialog
        thereAreNonDragAndDropFileInputs = true;
      }
    }
    if (thereAreNonDragAndDropFileInputs) {
      super.submit(); // submit non-dnd file inputs
    }
    if (!dndFileInputs.isEmpty()) {
      submitDragAndDropFileInputs(dndFileInputs); // submit dnd file inputs
    }
  }
  private static DragAndDropFormPanel currentInstance = null;
  private XMLHttpRequest request = null;

  private void submitDragAndDropFileInputs(ArrayList<IDragAndDropFileInput> dndFileInputs) {
    Uploader.log("panel submitDragAndDropFileInputs", null);

    // Fire the onSubmit event, because javascript's form.submit() does not
    // fire the built-in onsubmit event.
    if (!fireSubmitEvent()) {
      return;
    }

    if (currentInstance != null) {
      Uploader.log("DnD panel already sending files. ", null);
      return;
    }

    for (IDragAndDropFileInput fileInput : dndFileInputs) {
      final FileList files = fileInput.getFiles();
      if (files != null && files.getLength() > 0) {
        fileInput.lock();
        currentInstance = this;
        request = jsSubmit(getAction(), getMethod(), fileInput.getName(), files, dndFileInputs);
      }
    }
  }

  /**
   * Fire a {@link FormPanel.SubmitEvent}.
   *
   * @return true to continue, false if canceled
   */
  private boolean fireSubmitEvent() {
    Uploader.log("fireSubmitEvent ", null);
    FormPanel.SubmitEvent event = new FormPanel.SubmitEvent();
    fireEvent(event);
    return !event.isCanceled();
  }

  private List<IFileInput> getChildFileInputs() {
    final List<IFileInput> res = new ArrayList<IFileInput>();
    findChildFileInputs(res, iterator());
    return res;
  }

  private void findChildFileInputs(List<IFileInput> result, Iterator<Widget> iterator) {
    if (result == null || iterator == null) {
      return;
    }
    while (iterator.hasNext()) {
      final Widget next = iterator.next();
      if (next instanceof IFileInput) {
        result.add((IFileInput) next);
      } else if (next instanceof HasWidgets) {
        // recursive call:
        findChildFileInputs(result, ((HasWidgets) next).iterator());
      }
    }
  }

  // FIXME(manolo): Use session and reuse gwt form panel
  private native XMLHttpRequest jsSubmit(String action, String method, String fieldName, FileList files, ArrayList<IDragAndDropFileInput> inputs) /*-{
    var formData = new FormData();
    for (var i = 0; i < files.length; i++) {
      formData.append(fieldName, files[i]);
    }
    var request = new XMLHttpRequest();
    var outerThis = this;
    request.onreadystatechange = function() {
      if (request.readyState == 4) { // the request has completed
        outerThis.@gwtupload.client.dnd.DragAndDropFormPanel::onSubmitComplete(*)(request.responseText, request.status, inputs);
      }
    };
    request.open(method, action);
    request.send(formData);
    return request;
  }-*/;

  // This is invoked from jsSubmit():
  private void onSubmitComplete(String resultsHtml, int status, ArrayList<IDragAndDropFileInput> dndFileInputs) {
    Uploader.log("DnD panel complete. " + dndFileInputs.size() + " " + status, null);
    currentInstance = null;
    if (dndFileInputs != null) {
      for (IDragAndDropFileInput fileInput : dndFileInputs) {
        fileInput.reset();
      }
    }
    fireEvent(new SubmitCompleteEvent(resultsHtml) {
    });
  }

  protected static void abortIfRunning() {
    currentInstance = null;
//     if (currentInstance != null) {
//      currentInstance.request.abort();
//      currentInstance.onSubmitComplete(null, 0, null);
//    }
  }
}
