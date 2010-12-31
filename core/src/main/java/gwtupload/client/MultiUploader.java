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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * <p>
 * Implementation of an uploader panel that is able to handle several uploads.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * <p>
 * Each time the user selects a file, this is queued and a new upload form is created,
 * so the user can add new files to the queue while they are being uploaded
 * </p>
 */
public class MultiUploader extends Composite implements IUploader {

  IUploader.OnStatusChangedHandler statusChangeHandler = new IUploader.OnStatusChangedHandler() {
    public void onStatusChanged(IUploader uploader) {
      Uploader u = (Uploader) uploader;
      if (u.getStatus() == Status.CHANGED) {
        u.getFileInput().setVisible(false);
        u.getStatusWidget().setVisible(true);
      } else if (u.getStatus() == Status.SUBMITING) {
        // For security reasons, most browsers don't submit files if fileInput is hidden or has a size of 0,
        // so, before sending the form, it is necessary to show the fileInput, we put it out of the viewable
        // area.
        Widget w = u.getFileInput().getWidget();
        DOM.setStyleAttribute(w.getElement(), "position", "absolute");
        DOM.setStyleAttribute(w.getElement(), "left", "-4000px");
        u.getFileInput().setVisible(true);
      } else if (u.getStatus() == Status.REPEATED) {
        u.getFileInput().setVisible(true);
        u.getStatusWidget().setVisible(false);
      } else if (u.getStatus() == Status.INPROGRESS) {
        u.getFileInput().setVisible(false); 
      } else {
        // We don't need any more all the stuff related with the FormPanel when the upload has finished
        if (u.isFinished() && u.getForm().isAttached()) {
          u.getForm().removeFromParent();
        }
        u.getStatusWidget().setVisible(true);
        newUploaderInstance();
      }
    }
  };
  private boolean avoidRepeat = true;
  private IUploader currentUploader = null;
  private boolean enabled = true;
  private String fileInputPrefix = "GWTMU";
  private int fileInputSize = Uploader.DEFAULT_FILEINPUT_SIZE;

  private FileInputType fileInputType;
  private UploaderConstants i18nStrs = Uploader.I18N_CONSTANTS;
  private IUploader lastUploader = null;
  private int maximumFiles = 0;
  private FlowPanel multiUploaderPanel = new FlowPanel();
  private IUploader.OnCancelUploaderHandler onCancelHandler = null;
  
  private IUploader.OnChangeUploaderHandler onChangeHandler = null;
  private IUploader.OnFinishUploaderHandler onFinishHandler = null;

  private IUploader.OnStartUploaderHandler onStartHandler = null;
  
  private IUploader.OnStatusChangedHandler onStatusChangedHandler = null;

  private String servletPath = null;

  private IUploadStatus statusWidget = null;
  
  private List<IUploader> uploaders = new ArrayList<IUploader>();
  
  public List<IUploader> getUploaders() {
    return uploaders;
  }

  private String[] validExtensions = null;

  /**
   * Initialize widget components and layout elements.
   * Uses the default status widget and the standard input file.
   * 
   */
  public MultiUploader() {
    this(FileInputType.BROWSER_INPUT, new BaseUploadStatus());
  }
  
  /**
   * Initialize widget components and layout elements.
   * Uses the default status widget. 
   *  
   * @param type
   *   file input to use
   */
  @UiConstructor
  public MultiUploader(FileInputType type) {
    this(type, new BaseUploadStatus());
  }
  
  /**
   * Initialize widget components and layout elements.
   * 
   * @param type
   *   file input to use
   * @param status
   *   Customized status widget to use
   */
  public MultiUploader(FileInputType type, IUploadStatus status) {
    fileInputType = type;
    statusWidget = status;
    initWidget(multiUploaderPanel);
    setStyleName("upld-multiple");
    newUploaderInstance();
  }

  /**
   * Initialize widget components and layout elements.
   * 
   * @param status
   *   Customized status widget to use
   */
  public MultiUploader(IUploadStatus status) {
    this(FileInputType.BROWSER_INPUT, status);
  }

  /**
   * This is the constructor for customized multiuploaders.
   * 
   * @param status
   *   Customized status widget to use
   * @param fileInput
   *   Customized file input
   */
  public MultiUploader(IUploadStatus status, IFileInput fileInput) {
    this(status);
    setFileInput(fileInput);
  }

  /* (non-Javadoc)
  * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
  */
  public void add(Widget w) {
    currentUploader.add(w);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnCancelUploadHandler(gwtupload.client.IUploader.OnCancelUploaderHandler)
   */
  public HandlerRegistration addOnCancelUploadHandler(OnCancelUploaderHandler handler) {
    onCancelHandler = handler;
    return currentUploader.addOnCancelUploadHandler(handler);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnChangeUploadHandler(gwtupload.client.Uploader.OnChangeUploaderHandler)
   */
  public HandlerRegistration addOnChangeUploadHandler(IUploader.OnChangeUploaderHandler handler) {
    onChangeHandler = handler;
    return currentUploader.addOnChangeUploadHandler(handler);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnFinishUploadHandler(gwtupload.client.Uploader.OnFinishUploaderHandler)
   */
  public HandlerRegistration addOnFinishUploadHandler(IUploader.OnFinishUploaderHandler handler) {
    onFinishHandler = handler;
    return currentUploader.addOnFinishUploadHandler(handler);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnStartUploadHandler(gwtupload.client.Uploader.OnStartUploaderHandler)
   */
  public HandlerRegistration addOnStartUploadHandler(IUploader.OnStartUploaderHandler handler) {
    onStartHandler = handler;
    return new HandlerRegistration() {
      public void removeHandler() {
        onStartHandler = null;
      }
    };
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnStatusChangedHandler(gwtupload.client.IUploader.OnStatusChangedHandler)
   */
  public HandlerRegistration addOnStatusChangedHandler(OnStatusChangedHandler handler) {
    onStatusChangedHandler = handler;
    for (IUploader uploader : uploaders) {
      uploader.addOnStatusChangedHandler(handler);
    }
    return new HandlerRegistration() {
      public void removeHandler() {
        onStatusChangedHandler = null;
      }
    };
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#avoidRepeatFiles(boolean)
   */
  public void avoidRepeatFiles(boolean avoidRepeatFiles) {
    avoidRepeat = avoidRepeatFiles;
    currentUploader.avoidRepeatFiles(avoidRepeat);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#cancel()
   */
  public void cancel() {
    currentUploader.cancel();
  }

  /* (non-Javadoc)
    * @see com.google.gwt.user.client.ui.HasWidgets#clear()
    */
  public void clear() {
    currentUploader.clear();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#fileUrl()
   */
  public String fileUrl() {
    return lastUploader.fileUrl();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getBasename()
   */
  public String getBasename() {
    return Utils.basename(getFileName());
  }

  /* (non-Javadoc)
   * @see gwtupload.client.HasJsData#getData()
   */
  public JavaScriptObject getData() {
    return lastUploader.getData();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getFileInput()
   */
  public IFileInput getFileInput() {
    return currentUploader.getFileInput();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getFileName()
   */
  public String getFileName() {
    return lastUploader.getFileName();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getInputName()
   */
  public String getInputName() {
    return lastUploader.getInputName();
  }

  /**
   * Return the maximum files that can be uploaded to the server.
   * 
   */
  public int getMaximumFiles() {
    return maximumFiles;
  }

  /**
   * Return the number of uploads that have a non erroneous status.
   * It includes files which are queued or uploading. 
   * 
   */
  public int getNonErroneousUploads() {
    int ret = 0;
    for (IUploader u : uploaders) {
      if (u.getStatus() == Status.SUCCESS || u.getStatus() == Status.INPROGRESS || u.getStatus() == Status.QUEUED || u.getStatus() == Status.SUBMITING) {
        ret++;
      }
    }
    return ret;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getServerResponse()
   */
  public String getServerResponse() {
    return lastUploader.getServerResponse();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getServerInfo()
   */
  public UploadedInfo getServerInfo() {
    return lastUploader.getServerInfo();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getServletPath()
   */
  public String getServletPath() {
    return currentUploader.getServletPath();
  }

  /**
   *  Return the status of the multiuploader.
   *  
   *   @return
   *             Status.INPROGRESS    if there are items being sent or queued.
   *             Status.UNINITIALIZED if the user has not selected any file
   *             Status.DONE          if all items has been processed (SUCCESS or ERROR)
   */
  public Status getStatus() {
    for (IUploader uploader : uploaders) {
      Status stat = uploader.getStatus();
      if (stat == Status.INPROGRESS || stat == Status.QUEUED || stat == Status.SUBMITING) {
        return Status.INPROGRESS;
      }
    }
    if (uploaders.size() <= 1) {
      return Status.UNINITIALIZED;
    } else {
      return Status.DONE;
    }
  }

  /**
   * Return the status of the uploader whose fieldName or fileName is equal to 
   * the name passed as argument.
   * 
   * @param name
   * @return the status of the uploader in the case of found or UNINITIALIZED 
   */
  public Status getStatus(String name) {
    for (IUploader u : uploaders) {
      if (u.getInputName().equals(name) || u.getFileName().equals(name)) {
        return u.getStatus();
      }
    }
    return Status.UNINITIALIZED;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getStatusWidget()
   */
  public IUploadStatus getStatusWidget() {
    return currentUploader.getStatusWidget();
  }

  /**
   * Return the number of finished uploads with status success.
   * 
   */
  public int getSuccessUploads() {
    int ret = 0;
    for (IUploader u : uploaders) {
      if (u.getStatus() == Status.SUCCESS) {
        ret++;
      }
    }
    return ret;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
   */
  public Iterator<Widget> iterator() {
    return currentUploader.iterator();
  }
  
  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
   */
  public boolean remove(Widget w) {
    return currentUploader.remove(w);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#reset()
   */
  public void reset() {
    currentUploader.reset();
    currentUploader = null;
    uploaders = new Vector<IUploader>();
    multiUploaderPanel.clear();
    newUploaderInstance();
  }
  
  public void setAvoidRepeatFiles(boolean b){
    this.avoidRepeatFiles(b);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setEnabled(boolean)
   */
  public void setEnabled(boolean b) {
    enabled = b;
    currentUploader.setEnabled(b);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setFileInput(gwtupload.client.IFileInput)
   */
  public void setFileInput(IFileInput fileInput) {
    currentUploader.setFileInput(fileInput);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setFileInputPrefix(java.lang.String)
   */
  public void setFileInputPrefix(String prefix) {
    fileInputPrefix = prefix;
    currentUploader.setFileInputPrefix(prefix);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setFileInputSize(int)
   */
  public void setFileInputSize(int length) {
    fileInputSize = length;
    currentUploader.setFileInputSize(length);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setI18Constants(gwtupload.client.I18nUploadConstants)
   */
  public void setI18Constants(UploaderConstants strs) {
    i18nStrs = strs;
    currentUploader.setI18Constants(i18nStrs);
  }

  /**
   * Set the maximum number of files that can be uploaded to the server.
   * Only success uploads are counted.
   * 
   * If you decrease this parameter, files already uploaded or in queue are
   * not removed.
   * 
   * @param max
   */
  public void setMaximumFiles(int max) {
    maximumFiles = max;
  }

  /* (non-Javadoc)
  * @see gwtupload.client.IUploader#setServletPath(java.lang.String)
  */
  public void setServletPath(String path) {
    servletPath = path;
    currentUploader.setServletPath(path);
  }

  /* (non-Javadoc)
  * @see gwtupload.client.IUploader#setStatusWidget(gwtupload.client.IUploadStatus)
  */
  public void setStatusWidget(IUploadStatus status) {
    currentUploader.setStatusWidget(status);
  }
  
  /* (non-Javadoc)
  * @see gwtupload.client.IUploader#setValidExtensions(java.lang.String[])
  */
  public void setValidExtensions(String... ext) {
    validExtensions = ext;
    currentUploader.setValidExtensions(ext);
  }

  /* (non-Javadoc)
  * @see gwtupload.client.IUploader#submit()
  */
  public void submit() {
    currentUploader.submit();
  }

  protected IUploader getUploaderInstance() {
    return new Uploader(fileInputType, true);
  }

  private void newUploaderInstance() {

    if (maximumFiles > 0 && getNonErroneousUploads() >= maximumFiles) {
      GWT.log("Reached maximum number of files in MultiUploader widget: " + maximumFiles, null);
      return;
    }

    if (currentUploader != null) {
      Status status = currentUploader.getStatus();
      if (status == Status.UNINITIALIZED) {
        return;
      }
      // Save the last uploader, create a new statusWidget and fire onStart events
      lastUploader = currentUploader;
      statusWidget = lastUploader.getStatusWidget().newInstance();
      if (onStartHandler != null) {
        onStartHandler.onStart(lastUploader);
      }
    }

    // Create a new uploader
    currentUploader = getUploaderInstance();
    uploaders.add(currentUploader);
    currentUploader.setStatusWidget(statusWidget);
    if (lastUploader != null) {
      currentUploader.setFileInput(lastUploader.getFileInput().newInstance()); 
    }
    currentUploader.setValidExtensions(validExtensions);
    currentUploader.setServletPath(servletPath);
    currentUploader.avoidRepeatFiles(avoidRepeat);
    currentUploader.setI18Constants(i18nStrs);
    // Set the handlers
    currentUploader.addOnStatusChangedHandler(statusChangeHandler);
    
    if (onChangeHandler != null) {
      currentUploader.addOnChangeUploadHandler(onChangeHandler);
    }
    if (onFinishHandler != null) {
      currentUploader.addOnFinishUploadHandler(onFinishHandler);
    }
    if (onStatusChangedHandler != null) {
      currentUploader.addOnStatusChangedHandler(onStatusChangedHandler);
    }
    if (onCancelHandler != null) {
      currentUploader.addOnCancelUploadHandler(onCancelHandler);
    }
    
    currentUploader.setFileInputPrefix(fileInputPrefix);
    currentUploader.setFileInputSize(fileInputSize);
    currentUploader.setEnabled(enabled);
    // add the new uploader to the panel
    multiUploaderPanel.add((Widget) currentUploader);

    if (lastUploader == null) {
      lastUploader = currentUploader;
    }
  }

}
