/*
 * Copyright 2014 Manuel Carrasco Moñino. (manolo at apache/org)
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
 * @author Sultan Tezadov
 */
public class DropZoneFileInput extends Label implements IDropZone, IFileInput,
    IDragAndDropFileInput {

  boolean i18n = true;

  private IDropZone externalDropZoneWidget;
  private DragAndDropFilesProvider dragAndDropFilesProvider;

  public DropZoneFileInput() {
    this(null, true);
  }

  public DropZoneFileInput(IDropZone dropZoneWidget) {
    this(dropZoneWidget, true);
  }

  public DropZoneFileInput(IDropZone dropZoneWidget, boolean i18n) {
    super(Document.get().createSpanElement());
    this.i18n = i18n;
    if (dropZoneWidget == null) {
      if (i18n) {
        setText(Uploader.I18N_CONSTANTS.uploaderDrop());
      }
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
    return new DropZoneFileInput(externalDropZoneWidget, true);
  }

  public void setEnabled(boolean b) {
    dragAndDropFilesProvider.setEnabled(b);
  }

  public void setLength(int length) {
  }

  public void setName(String fieldName) {
    dragAndDropFilesProvider.setName(fieldName);
  }

  public void setText(String text) {
    if (i18n) {
      super.setText(text);
    }
  }

  public void updateSize() {
  }

  public void enableMultiple(boolean b) {
  }

  public void setAccept(String accept) {
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

  private void fireChangeEvent() {
    ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
  }
}