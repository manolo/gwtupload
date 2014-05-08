package gwtupload.client;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DragAndDropFormPanel.
 *
 * @author Sultan Tezadov
 * @since Jan 12, 2014
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
        final List<IFileInput> childFileInputs = getChildFileInputs();
        if (childFileInputs == null || childFileInputs.isEmpty()) {
            return;
        }
        final ArrayList<IDragAndDropFileInput> dndFileInputs = 
                new ArrayList<IDragAndDropFileInput>();
        boolean thereAreNonDragAndDropFileInputs = false;
        for (IFileInput fileInput : childFileInputs) {
            if (fileInput instanceof IDragAndDropFileInput) {
                final IDragAndDropFileInput dndFileInput = (IDragAndDropFileInput) fileInput;
                if (dndFileInput.thereAreDragAndDropedFiles()) {
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

    private void submitDragAndDropFileInputs(ArrayList<IDragAndDropFileInput> dndFileInputs) {
        // Fire the onSubmit event, because javascript's form.submit() does not
        // fire the built-in onsubmit event.
        if (!fireSubmitEvent()) {
            return;
        }

        for (IDragAndDropFileInput fileInput : dndFileInputs) {
            final FileList files = fileInput.getDragAndDropedFiles();
            if (files != null && files.getLength() > 0) {
                jsSubmit(getAction(), getMethod(), fileInput.getName(), files);
            }
        }
    }

    /**
     * Fire a {@link FormPanel.SubmitEvent}.
     *
     * @return true to continue, false if canceled
     */
    private boolean fireSubmitEvent() {
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

    private native void jsSubmit(String action, String method, String fieldName, FileList files) /*-{
        var formData = new FormData();
        for (var i = 0; i < files.length; i++) {
            formData.append(fieldName, files[i]);
        }

        var request = new XMLHttpRequest();
        var outerThis = this;
        request.onreadystatechange = function() {
            if (request.readyState == 4) { // the request has completed
                outerThis.@gwtupload.client.DragAndDropFormPanel::onSubmitComplete(Ljava/lang/String;)(request.responseText);
            }
        };
        request.open(method, action);
        request.send(formData);
    }-*/;
    
    // This is invoked from jsSubmit():
    private void onSubmitComplete(String resultsHtml) {
        fireEvent(new SubmitCompleteEvent(resultsHtml) {
        });
    }
}
