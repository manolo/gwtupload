/*
 * Copyright 2007 Manuel Carrasco Moñino. (manolo at apache/org)
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

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import gwtupload.client.DecoratedFileUpload.FileUploadWithMouseEvents;

/**
 * Interface used by Uploaders to use and configure a customized file fileUplad.
 *
 * Widgets implementing this interface have to render a file fileUplad tag because
 * it will be added to the form which is sent to the server.
 *
 * This interface has thought to let the user the option to create customizable
 * panels for file inputs.
 *
 * @author Manolo Carrasco Moñino
 *
 */
public interface IFileInput extends HasChangeHandlers, IsWidget {

  /**
   * A HyperLinkFileInput implementing the IFileInput interface
   *
   */
  public class AnchorFileInput extends ButtonFileInput {
    public AnchorFileInput() {
      super(new Anchor());
    }
  }

  /**
   * Just a FileUpload which implements the interface IFileInput
   */
  public class BrowserFileInput extends FileUploadWithMouseEvents implements
      IFileInput {

    @Deprecated
    public Widget getWidget() {
      return asWidget();
    }

    public IFileInput newInstance() {
      return new BrowserFileInput();
    }

    public void setLength(int length) {
      DOM.setElementAttribute(getElement(), "size", "" + length);
    }

    /**
     * It is not possible to change the button text in a fileUplad type=file
     */
    public void setText(String text) {
    }

    public void updateSize() {
    }
  }

  /**
   * A DecoratedFileInput implementing the IFileInput interface
   *
   */
  public class ButtonFileInput extends DecoratedFileUpload implements IFileInput {
    boolean i18n = true;

    public ButtonFileInput() {
      this(new Button());
    }

    public ButtonFileInput(Widget w) {
      this(w, true);
    }

    public ButtonFileInput(Widget w, boolean i18n) {
      super(w);
      this.i18n = i18n;
      if (i18n) {
        super.setText(Uploader.I18N_CONSTANTS.uploaderBrowse());
      }
    }

    public IFileInput newInstance() {
      Widget widget = button != null ? button : new Button(this.getText());
      return new ButtonFileInput(widget, i18n);
    }

    public void setLength(int length) {
    }

    public void setText(String text) {
      if (i18n) {
        super.setText(text);
      }
    }
  }

  /**
   * Enum for different IFileInput implementations
   */
  public enum FileInputType implements HasFileInputType {
    ANCHOR {
      public IFileInput getInstance() {
        return GWT.create(AnchorFileInput.class);
      }
      public FileInputType with(Widget w, boolean hasText) {
        return this;
      }
      public FileInputType with(Widget w) {
        return this;
      }
      public FileInputType with(IFileInput i) {
        return this;
      }
      public FileInputType with(IDropZone dropZone) {
        return this;
      }
    },
    BROWSER_INPUT {
      public IFileInput getInstance() {
        return GWT.create(BrowserFileInput.class);
      }
      public FileInputType with(Widget w, boolean hasText) {
        return this;
      }
      public FileInputType with(Widget w) {
        return this;
      }
      public FileInputType with(IFileInput i) {
        return this;
      }
      public FileInputType with(IDropZone dropZone) {
        return this;
      }
    },
    BUTTON {
      public IFileInput getInstance() {
        return GWT.create(ButtonFileInput.class);
      }
      public FileInputType with(Widget w, boolean hasText) {
        return this;
      }
      public FileInputType with(Widget w) {
        return this;
      }
      public FileInputType with(IFileInput i) {
        return this;
      }
      public FileInputType with(IDropZone dropZone) {
        return this;
      }
    },
    LABEL {
      public IFileInput getInstance() {
        return GWT.create(LabelFileInput.class);
      }
      public FileInputType with(Widget w, boolean hasText) {
        return this;
      }
      public FileInputType with(Widget w) {
        return this;
      }
      public FileInputType with(IFileInput i) {
        return this;
      }
      public FileInputType with(IDropZone dropZone) {
        return this;
      }
    },
    CUSTOM {
      Widget widget;
      boolean hasText = false;
      IFileInput i = null;
      private IDropZone dropZone;
      private boolean withDropZone;

      public IFileInput getInstance() {
        if (i == null) {
          if (withDropZone) {
            if (widget == null) {
              i = GWT.create(DropZoneButtonFileInput.class);
            } else {
              i = new DropZoneButtonFileInput(widget, hasText, dropZone);
            }
          } else {
            if (widget == null) {
              i = GWT.create(ButtonFileInput.class);
            } else {
              i = new ButtonFileInput(widget, hasText);
            }
          }
        }
        return i.newInstance();
      }

      public FileInputType with(Widget widget, boolean hasText) {
        i = null;
        this.widget = widget;
        this.hasText = hasText;
        this.withDropZone = false;
        this.dropZone = null;
        return this;
      }
      public FileInputType with(Widget w) {
        return with(w, false);
      }
      public FileInputType with(IFileInput i) {
        this.i = i;
        return this;
      }
      public FileInputType with(IDropZone dropZone) {
        this.withDropZone = true; // dropZone can be null
        this.dropZone = dropZone;
        return this;
      }
    },
    DROPZONE {
      private IDropZone dropZone;

      public IFileInput getInstance() {
        return new DropZoneFileInput(dropZone);
      }
      public FileInputType with(Widget widget, boolean hasText) {
        return this;
      }
      public FileInputType with(Widget w) {
        return with(w, false);
      }
      public FileInputType with(IFileInput i) {
        return this;
      }
      public FileInputType with(IDropZone dropZone) {
        this.dropZone = dropZone;
        return this;
      }
    }
  }

  /**
   * interface for FileInputType enum
   */
  interface HasFileInputType {
    IFileInput getInstance();
    FileInputType with(Widget w, boolean hasText);
    FileInputType with(Widget w);
    FileInputType with(IFileInput w);
    FileInputType with(IDropZone dropZone);
  }

  /**
   * A LabelFileInput implementing the IFileInput interface
   *
   */
  public class LabelFileInput extends ButtonFileInput {
    public LabelFileInput() {
      super(new Label());
      addChangeHandler(new ChangeHandler() {
        public void onChange(ChangeEvent event) {
          setText(getFilename());
        }
      });
    }
  }

  /**
   * Gets the filename selected by the user. This property has no mutator, as
   * browser security restrictions preclude setting it.
   *
   * @return the widget's filename
   */
  String getFilename();

  /**
   * Gets the filenames selected by the user.
   */
  List<String> getFilenames();

  /**
   * Gets the name of this fileUplad element.
   *
   * @return fieldName
   */
  String getName();

  /**
   * Returns the widget which will be inserted in the document.
   * @deprecated use asWidget instead
   */
  @Deprecated
  Widget getWidget();

  /**
   * return whether the fileUplad is or not enabled.
   */
  boolean isEnabled();

  /**
   * Creates a new instance of the current object type.
   *
   * @return a new instance
   */
  IFileInput newInstance();

  /**
   * Enable the file fileUplad.
   */
  void setEnabled(boolean b);

  /**
   * Set the length in characters of the fileinput which are shown.
   *
   * @param length
   */
  void setLength(int length);

  /**
   * Sets the html name for this fileUplad element. It is the name of the form
   * parameter sent to the server.
   *
   * @param fieldName
   */
  void setName(String fieldName);

  /**
   * Set the size of the widget.
   *
   * @param width
   * @param height
   */
  void setSize(String width, String height);

  /**
   * Set the text for the link which opens the browse file dialog.
   *
   * @param text
   */
  void setText(String text);

  void setVisible(boolean b);

  void updateSize();

  void enableMultiple(boolean b);

  void setAccept(String accept);

}
