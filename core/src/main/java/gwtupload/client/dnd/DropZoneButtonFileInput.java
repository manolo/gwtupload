package gwtupload.client.dnd;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

import static gwtupload.client.dnd.DropZoneFileInput.STYLE_DROP_ZONE;
import static gwtupload.client.dnd.DropZoneFileInput.STYLE_DROP_ZONE_SENDING;

import gwtupload.client.FileList;
import gwtupload.client.IFileInput;

import gwtupload.client.IFileInput.ButtonFileInput;
/**
 * DropZoneButtonFileInput.
 *
 * @author Sultan Tezadov
 */
// FIXME(manolo): This file is pretty equal to DropZoneFileInput, why not unify them?
public class DropZoneButtonFileInput extends ButtonFileInput implements HasAllDragAndDropHandlers,
    IDragAndDropFileInput {

  private HasAllDragAndDropHandlers externalDropZoneWidget;
  private DragAndDropFilesProvider dragAndDropFilesProvider;
  private Widget dropZone;

  public DropZoneButtonFileInput() {
    init(this, null);
  }

  public DropZoneButtonFileInput(Widget w) {
    super(w);
    init(this, null);
  }

  public DropZoneButtonFileInput(Widget w, boolean i18n) {
    super(w, i18n);
    init(this, null);
  }

  public DropZoneButtonFileInput(Widget w, boolean i18n, HasAllDragAndDropHandlers dropZoneWidget) {
    super(w, i18n);
    init(dropZoneWidget, dropZoneWidget);
  }


  private void init(HasAllDragAndDropHandlers dropZoneWidget,
      HasAllDragAndDropHandlers externalDropZoneWidget) {
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
    dropZone = (Widget) dropZoneWidget;
    dropZone.addStyleName(STYLE_DROP_ZONE);    
  }

  public boolean hasFiles() {
    return dragAndDropFilesProvider.thereAreDragAndDropedFiles();
  }

  @Override
  public void reset() {
    dragAndDropFilesProvider.reset();
    dropZone.removeStyleName(STYLE_DROP_ZONE_SENDING);
  }

  public FileList getFiles() {
    return dragAndDropFilesProvider.getDragAndDropedFiles();
  }

  @Override
  public void lock() {
    dragAndDropFilesProvider.lock();
    dropZone.addStyleName(STYLE_DROP_ZONE_SENDING);
  }

  @Override
  public String getFilename() {
    return hasFiles() ? dragAndDropFilesProvider.getFilename()
        : super.getFilename();
  }

  @Override
  public List<String> getFilenames() {
    return hasFiles() ? dragAndDropFilesProvider.getFilenames()
        : super.getFilenames();
  }

  @Override
  public IFileInput newInstance() {
    Widget widget = button != null ? button : new Button(this.getText());
    return new DropZoneButtonFileInput(widget, i18n, externalDropZoneWidget);
  }

  @Override
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    dragAndDropFilesProvider.setEnabled(b);
  }

  @Override
  public void setName(String fieldName) {
    super.setName(fieldName);
    dragAndDropFilesProvider.setName(fieldName);
  }

  @Override
  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    super.addChangeHandler(handler);
    return addDomHandler(handler, ChangeEvent.getType());
  }

  private void fireChangeEvent() {
    ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
  }

  public HandlerRegistration addDragEndHandler(DragEndHandler handler) {
    return addBitlessDomHandler(handler, DragEndEvent.getType());
  }

  public HandlerRegistration addDragEnterHandler(DragEnterHandler handler) {
    return addBitlessDomHandler(handler, DragEnterEvent.getType());
  }

  public HandlerRegistration addDragLeaveHandler(DragLeaveHandler handler) {
    return addBitlessDomHandler(handler, DragLeaveEvent.getType());
  }

  public HandlerRegistration addDragHandler(DragHandler handler) {
    return addBitlessDomHandler(handler, DragEvent.getType());
  }

  public HandlerRegistration addDragOverHandler(DragOverHandler handler) {
    return addBitlessDomHandler(handler, DragOverEvent.getType());
  }

  public HandlerRegistration addDragStartHandler(DragStartHandler handler) {
    return addBitlessDomHandler(handler, DragStartEvent.getType());
  }

  public HandlerRegistration addDropHandler(DropHandler handler) {
    return addBitlessDomHandler(handler, DropEvent.getType());
  }
}
