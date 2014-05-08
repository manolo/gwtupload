package gwtupload.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;

/**
 * DropZoneFileInput.
 *
 * @author Sultan Tezadov
 * @since Jan 12, 2014
 */
public class DropZoneFileInput extends Label implements IDropZone, IFileInput,
        IDragAndDropFileInput {

    private IDropZone externalDropZoneWidget;
    private DragAndDropFilesProvider dragAndDropFilesProvider;

    public DropZoneFileInput() {
        this(null);
    }

    public DropZoneFileInput(IDropZone dropZoneWidget) {
        super(Document.get().createSpanElement());
        if (dropZoneWidget == null) {
            //setText("Drag files here to upload");
            getElement().getStyle().setBorderStyle(Style.BorderStyle.DASHED);
            getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
        }
        init(dropZoneWidget, dropZoneWidget);
    }

    private void init(IDropZone dropZoneWidget, IDropZone externalDropZoneWidget) {
        if (dropZoneWidget == null) {
            dropZoneWidget = this;
        }
        this.externalDropZoneWidget = externalDropZoneWidget;
        dragAndDropFilesProvider = new DragAndDropFilesProvider(dropZoneWidget);
        dragAndDropFilesProvider.addValueChangeHandler(new ValueChangeHandler<FileList>() {
            public void onValueChange(ValueChangeEvent<FileList> event) {
                fireChangeEvent();
            }
        });
    }

    public boolean thereAreDragAndDropedFiles() {
        return dragAndDropFilesProvider.thereAreDragAndDropedFiles();
    }

    public FileList getDragAndDropedFiles() {
        return dragAndDropFilesProvider.getDragAndDropedFiles();
    }

    public String getFilename() {
        return dragAndDropFilesProvider.getFilename();
    }

    public List<String> getFilenames() {
        return dragAndDropFilesProvider.getFilenames();
    }

    public String getName() {
        return dragAndDropFilesProvider.getName();
    }

    public Widget getWidget() {
        return asWidget();
    }

    public boolean isEnabled() {
        return dragAndDropFilesProvider.isEnabled();
    }

    public IFileInput newInstance() {
        return new DropZoneFileInput(externalDropZoneWidget);
    }

    public void setEnabled(boolean b) {
        dragAndDropFilesProvider.setEnabled(b);
    }

    public void setLength(int length) {
    }

    public void setName(String fieldName) {
        dragAndDropFilesProvider.setName(fieldName);
    }
//
//    public void setSize(String width, String height) {
//    }
//
//    public void setText(String text) {
//    }
//
//    public void setVisible(boolean b) {
//    }

    public void updateSize() {
    }

    public void enableMultiple(boolean b) {
    }

    public void setAccept(String accept) {
    }

    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return addDomHandler(handler, ChangeEvent.getType());
    }
//
//    public void fireEvent(GwtEvent<?> event) {
//    }
//
//    public Widget asWidget() {
//        return null;
//    }

    private void fireChangeEvent() {
        ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
    }
}