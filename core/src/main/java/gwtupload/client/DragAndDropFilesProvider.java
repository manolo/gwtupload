package gwtupload.client;

import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DragAndDropFilesProvider.
 *
 * @author Sultan Tezadov
 * @since Jan 20, 2014
 */
public class DragAndDropFilesProvider implements HasValueChangeHandlers<FileList> {

    private static Map<IDropZone, List<HandlerRegistration>> handlerRegistrationsMap =
            new HashMap<IDropZone, List<HandlerRegistration>>();

    private static void rememberHandlerRegistration(
            IDropZone dropZoneWidget, HandlerRegistration handler) {
        List<HandlerRegistration> handlerRegistrationsList =
                handlerRegistrationsMap.get(dropZoneWidget);
        if (handlerRegistrationsList == null) {
            handlerRegistrationsList = new ArrayList<HandlerRegistration>();
            handlerRegistrationsMap.put(dropZoneWidget, handlerRegistrationsList);
        }
        handlerRegistrationsList.add(handler);
    }

    private static void removePreviousHandlers(IDropZone dropZoneWidget) {
        List<HandlerRegistration> handlerRegistrationsList =
                handlerRegistrationsMap.get(dropZoneWidget);
        if (handlerRegistrationsList != null) {
            handlerRegistrationsMap.remove(dropZoneWidget);
            for (HandlerRegistration handlerRegistration : handlerRegistrationsList) {
                handlerRegistration.removeHandler();
            }
        }
    }
    //
    private FileList files;
    private String fieldName;
    private boolean enabled = true;
    private HandlerManager handlerManager;

    private final class Handlers implements DropHandler, DragOverHandler,
            DragHandler, DragEndHandler, DragEnterHandler, DragLeaveHandler,
            DragStartHandler, AttachEvent.Handler {

        private void preventDefault(DragDropEventBase event) {
            event.preventDefault();
            event.stopPropagation();
        }

        public void onDrop(DropEvent event) {
            preventDefault(event);
            onDragDrop(event.getDataTransfer());
        }

        public void onDragOver(DragOverEvent event) {
            preventDefault(event);
        }

        public void onDrag(DragEvent event) {
            preventDefault(event);
        }

        public void onDragEnd(DragEndEvent event) {
            preventDefault(event);
        }

        public void onDragEnter(DragEnterEvent event) {
            preventDefault(event);
        }

        public void onDragLeave(DragLeaveEvent event) {
            preventDefault(event);
        }

        public void onDragStart(DragStartEvent event) {
            preventDefault(event);
        }

        public void onAttachOrDetach(AttachEvent event) {
            if (!event.isAttached()) { // on detach
                if (event.getSource() instanceof IDropZone) {
                    // clean up from handlerRegistrationsMap to avoid
                    // potential memory leak:
                    removePreviousHandlers((IDropZone) event.getSource());
                }
            }
        }
    }

    public DragAndDropFilesProvider(IDropZone dropZoneWidget) {
        initListeners(dropZoneWidget);
    }

    private void initListeners(IDropZone dropZoneWidget) {
        removePreviousHandlers(dropZoneWidget);
        //
        final Handlers handlers = new Handlers();
        rememberHandlerRegistration(dropZoneWidget, dropZoneWidget.addDropHandler(handlers));
//        if (dropZoneWidget instanceof HasDragOverHandlers) {
//            handler = ((HasDragOverHandlers) dropZoneWidget).addDragOverHandler(handlers);
//            rememberHandlerRegistration(dropZoneWidget, handler);
//        }
//        if (dropZoneWidget instanceof HasDragHandlers) {
//            handler = ((HasDragHandlers) dropZoneWidget).addDragHandler(handlers);
//            rememberHandlerRegistration(dropZoneWidget, handler);
//        }
//        if (dropZoneWidget instanceof HasDragEndHandlers) {
//            handler = ((HasDragEndHandlers) dropZoneWidget).addDragEndHandler(handlers);
//            rememberHandlerRegistration(dropZoneWidget, handler);
//        }
//        if (dropZoneWidget instanceof HasDragEnterHandlers) {
//            handler = ((HasDragEnterHandlers) dropZoneWidget).addDragEnterHandler(handlers);
//            rememberHandlerRegistration(dropZoneWidget, handler);
//        }
//        if (dropZoneWidget instanceof HasDragLeaveHandlers) {
//            handler = ((HasDragLeaveHandlers) dropZoneWidget).addDragLeaveHandler(handlers);
//            rememberHandlerRegistration(dropZoneWidget, handler);
//        }
//        if (dropZoneWidget instanceof HasDragStartHandlers) {
//            handler = ((HasDragStartHandlers) dropZoneWidget).addDragStartHandler(handlers);
//            rememberHandlerRegistration(dropZoneWidget, handler);
//        }
        rememberHandlerRegistration(dropZoneWidget, dropZoneWidget.addDragOverHandler(handlers));
        rememberHandlerRegistration(dropZoneWidget, dropZoneWidget.addDragHandler(handlers));
        rememberHandlerRegistration(dropZoneWidget, dropZoneWidget.addDragEndHandler(handlers));
        rememberHandlerRegistration(dropZoneWidget, dropZoneWidget.addDragEnterHandler(handlers));
        rememberHandlerRegistration(dropZoneWidget, dropZoneWidget.addDragLeaveHandler(handlers));
        rememberHandlerRegistration(dropZoneWidget, dropZoneWidget.addDragStartHandler(handlers));
        // to clean up on drop zone detach:
        if (dropZoneWidget instanceof HasAttachHandlers) {
            ((HasAttachHandlers) dropZoneWidget).addAttachHandler(handlers);
        }
    }

    private void onDragDrop(DataTransfer dataTransfer) {
        if (!enabled) {
            return;
        }
        files = getDataTransferFiles(dataTransfer);
        fireChangeEvent();
    }

    public FileList getDragAndDropedFiles() {
        return files;
    }

    private native FileList getDataTransferFiles(DataTransfer dataTransfer) /*-{
     return dataTransfer.files;
     }-*/;

    public static boolean thereAreDragAndDropedFiles(FileList fileList) {
        return fileList != null && fileList.getLength() > 0;
    }
    
    public boolean thereAreDragAndDropedFiles() {
        return thereAreDragAndDropedFiles(files);
    }

    public static String getFilename(FileList fileList) {
        if (thereAreDragAndDropedFiles(fileList)) {
            return fileList.item(0).getName();
        } else {
            return null;
        }
    }
    
    public String getFilename() {
        return getFilename(files);
    }

    public static List<String> getFilenames(FileList fileList) {
        ArrayList<String> result = new ArrayList<String>();
        if (fileList != null) {
            for (int i = 0; i < fileList.getLength(); i++) {
                result.add(fileList.item(i).getName());
            }
        }
        return result;
    }

    public List<String> getFilenames() {
        return getFilenames(files);
    }
    
    public String getName() {
        return fieldName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void enableMultiple(boolean b) {
    }

    public void setAccept(String accept) {
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<FileList> handler) {
        if (handlerManager == null) {
            handlerManager = new HandlerManager(this);
        }
        return handlerManager.addHandler(ValueChangeEvent.getType(), handler);
    }

    private void fireChangeEvent() {
        ValueChangeEvent.fire(this, files);
    }

    public void fireEvent(GwtEvent<?> event) {
        if (handlerManager != null) {
            handlerManager.fireEvent(event);
        }
    }
}
