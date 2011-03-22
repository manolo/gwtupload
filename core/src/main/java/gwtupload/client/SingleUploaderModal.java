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

import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>
 * Implementation of a single uploader with a submit button and a modal status widget.
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
public class SingleUploaderModal extends SingleUploader {


  /**
   * Default constructor.
   * Uses the standard browser input, a basic status widget, and
   * creates a standard button to send the file
   * 
   */
  public SingleUploaderModal() {
    this(FileInputType.BROWSER_INPUT);
  }

  @UiConstructor
  public SingleUploaderModal(FileInputType type) {
    this(type, new ModalUploadStatus());
  }

  /**
   * Creates a standard button to send the file
   * 
   * @param type
   *        file input to use
   * @param status
   *        Customized status widget to use
   */
  public SingleUploaderModal(FileInputType type, IUploadStatus status) {
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
  public SingleUploaderModal(FileInputType type, IUploadStatus status, Widget submitButton) {
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
  public SingleUploaderModal(FileInputType type, IUploadStatus status, Widget submitButton, FormPanel form) {
    super(type, status, submitButton, form);
  }

  /**
   * Uses the standard browser input, customized status, and creates a 
   * standard button to send the file
   * 
   * @param status
   *        Customized status widget to use
   */
  public SingleUploaderModal(IUploadStatus status) {
    this(FileInputType.BROWSER_INPUT, status);
  }

  /**
   * 
   * @param status
   *        Customized status widget to use
   * @param submitButton
   *        Customized button which submits the form
   */
  public SingleUploaderModal(IUploadStatus status, Widget submitButton) {
    this(FileInputType.BROWSER_INPUT, status, submitButton, null);
  }


  /* (non-Javadoc)
   * @see gwtupload.client.Uploader#onStartUpload()
   */
  @Override
  protected void onStartUpload() {
    super.onStartUpload();
    getFileInput().getWidget().setVisible(true);
    if (button != null) {
      button.setVisible(true);
    }
  }
  
}
