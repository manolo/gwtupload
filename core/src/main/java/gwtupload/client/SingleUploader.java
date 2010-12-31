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
package gwtupload.client;

import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>
 * Implementation of a single uploader panel with a submit button.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 * <p>
 * When the user selects a file, the button changes its style
 * so the she could realize that she has to push the button.
 * </p>
 *
 */
public class SingleUploader extends Uploader {

  private Widget button;
  
  private void setEnabledButton(boolean b) {
    if (button != null) {
      // HasEnabled is only available after gwt-2.1.x
      if (button instanceof HasEnabled) {
        ((HasEnabled)button).setEnabled(b);
      } else if (button instanceof Button) {
        ((Button)button).setEnabled(b);
      }
    }
  }

  /**
   * Default constructor.
   * Uses the standard browser input, a basic status widget, and
   * creates a standard button to send the file
   * 
   */
  public SingleUploader() {
    this(FileInputType.BROWSER_INPUT);
  }

  /**
   * Use a basic status widget, and creates 
   * a standard button to send the file
   * 
   * @param type
   *        file input to use
   */
  @UiConstructor
  public SingleUploader(FileInputType type) {
    this(type, null);
  }

  /**
   * Creates a standard button to send the file
   * 
   * @param type
   *        file input to use
   * @param status
   *        Customized status widget to use
   */
  public SingleUploader(FileInputType type, IUploadStatus status) {
    this(type, status, new Button());
  }

  /**
   * Constructor
   * 
   * @param type
   *        file input to use
   * @param status
   *        Customized status widget to use
   * @param submitButton
   *        Customized button which submits the form
   */
  public SingleUploader(FileInputType type, IUploadStatus status, Widget submitButton) {
    this(type, status, submitButton, null);
  }

  /**
   * This constructor allows to use an existing form panel.
   * 
   * @param type
   *        file input to use
   * @param status
   *        Customized status widget to use
   * @param submitButton
   *        Customized button which submits the form
   * @param form
   *        Customized form panel
   */
  public SingleUploader(FileInputType type, IUploadStatus status, Widget submitButton, FormPanel form) {
    super(type, form);

    final Uploader thisInstance = this;

    if (status == null) {
      status = new ModalUploadStatus();
    }
    super.setStatusWidget(status);
    
    this.button = submitButton;
    if (submitButton != null) {
      submitButton.addStyleName("submit");
      if (submitButton instanceof HasClickHandlers) {
        ((HasClickHandlers)submitButton).addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            thisInstance.submit();
          }
        });
      }
      if (submitButton instanceof HasText) {
        ((HasText)submitButton).setText(I18N_CONSTANTS.uploaderSend());
      }
      // The user could have attached the button anywhere in the page.
      if (!submitButton.isAttached()) {
        super.add(submitButton);
      }
    }
  }

  /**
   * Uses the standard browser input, customized status, and creates a 
   * standard button to send the file
   * 
   * @param status
   *        Customized status widget to use
   */
  public SingleUploader(IUploadStatus status) {
    this(FileInputType.BROWSER_INPUT, status);
  }

  /**
   * 
   * @param status
   *        Customized status widget to use
   * @param submitButton
   *        Customized button which submits the form
   */
  public SingleUploader(IUploadStatus status, Widget submitButton) {
    this(FileInputType.BROWSER_INPUT, status, submitButton, null);
  }

  @Override
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    setEnabledButton(b);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#setI18Constants(gwtupload.client.IUploader.UploaderConstants)
   */
  @Override
  public void setI18Constants(UploaderConstants strs) {
    super.setI18Constants(strs);
    if (button != null && button instanceof HasText) {
      ((HasText)button).setText(strs.uploaderSend());
    }
  }

  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#onChangeInput()
   */
  @Override
  protected void onChangeInput() {
    super.onChangeInput();
    if (button != null) {
      button.addStyleName("changed");
      if (button instanceof Focusable) {
        ((Focusable)button).setFocus(true);
      }
    }
  }

  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#onFinishUpload()
   */
  @Override
  protected void onFinishUpload() {
    super.onFinishUpload();
    if (getStatus() == Status.REPEATED) {
      getStatusWidget().setError(getI18NConstants().uploaderAlreadyDone());
    }
    getStatusWidget().setStatus(Status.UNINITIALIZED);
    reuse();
    assignNewNameToFileInput();
    if (button != null) {
      setEnabledButton(true);
      button.removeStyleName("changed");
    }
    if (autoSubmit) {
      getFileInput().setText(i18nStrs.uploaderBrowse());
    }
  }
  
  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#onStartUpload()
   */
  @Override
  protected void onStartUpload() {
    super.onStartUpload();
    if (button != null) {
      setEnabledButton(false);
      button.removeStyleName("changed");
    }
  }
  
  public void setAvoidRepeatFiles(boolean b){
    this.avoidRepeatFiles(b);
  }
  
  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#setAutoSubmit(boolean)
   */
  @Override
  public void setAutoSubmit(boolean b) {
    if (button != null) {
      button.setVisible(!b);
    }
    super.setAutoSubmit(b);
  }

}
