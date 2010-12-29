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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Set;

/**
 *<p>
 * Basic widget that implements the IUploadStatus interface.
 * It renders a simple progress bar.
 * This class that can be overwritten to create more complex widgets.  
 *</p>
 *
 * @author Manolo Carrasco Moñino
 * 
 */
public class BaseUploadStatus implements IUploadStatus {

  /**
   * A basic progress bar implementation used when the user doesn't provide any.
   */
  public class BasicProgressBar extends FlowPanel implements HasProgress {

    SimplePanel statusBar = new SimplePanel();
    Label statusMsg = new Label();

    public BasicProgressBar() {
      this.setWidth("100px");
      this.setStyleName("prgbar-back");
      this.add(statusBar);
      this.add(statusMsg);
      statusBar.setStyleName("prgbar-done");
      statusBar.setWidth("0px");
      statusMsg.setStyleName("prgbar-msg");
    }

    /* (non-Javadoc)
     * @see gwtupload.client.HasProgress#setProgress(int, int)
     */
    public void setProgress(int done, int total) {
      if (statusBar == null) {
        return;
      }
      int percent = IUploader.Utils.getPercent(done, total);
      statusBar.setWidth(percent + "px");
      statusMsg.setText(percent + "%");
    }
  }

  /**
   * Cancel button. 
   */
  protected Label cancelLabel = new Label(" ");
  
  /**
   * Label with the original name of the uploaded file.
   */
  protected Label fileNameLabel = new Label();
  
  /**
   * Main panel, attach it to the document using getWidget().
   */
  protected Panel panel = new HorizontalPanel();

  /**
   * Label with the progress status.
   */
  protected Label statusLabel = new Label();
  private Set<CancelBehavior> cancelCfg = DEFAULT_CANCEL_CFG;
  private boolean hasCancelActions = false;

  private UploadStatusConstants i18nStrs = GWT.create(UploadStatusConstants.class);
  private UploadStatusChangedHandler onUploadStatusChangedHandler = null;
  private Widget prg = null;
  private IUploadStatus.Status status = Status.UNINITIALIZED;

  /**
   * Default Constructor.
   */
  public BaseUploadStatus() {
    panel.add(cancelLabel);
    panel.add(fileNameLabel);
    panel.add(statusLabel);
    fileNameLabel.setStyleName("filename");
    statusLabel.setStyleName("status");
    cancelLabel.setStyleName("cancel");
    cancelLabel.setVisible(true);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#addCancelHandler(gwtupload.client.UploadCancelHandler)
   */
  public HandlerRegistration addCancelHandler(final UploadCancelHandler handler) {
    hasCancelActions = true;
    return cancelLabel.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        handler.onCancel();
      }
    });
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#getStatus()
   */
  public Status getStatus() {
    return status;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#getWidget()
   */
  public Widget getWidget() {
    return panel;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#newInstance()
   */
  public IUploadStatus newInstance() {
    IUploadStatus ret = new BaseUploadStatus();
    ret.setCancelConfiguration(cancelCfg);
    return ret;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#setCancelConfiguration(int)
   */
  public void setCancelConfiguration(Set<CancelBehavior> config) {
    cancelCfg = config;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#setError(java.lang.String)
   */
  public void setError(String msg) {
    setStatus(Status.ERROR);
    Window.alert(msg.replaceAll("\\\\n", "\\n"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see gwtupload.client.IUploadStatus#setFileName(java.lang.String)
   */
  public void setFileName(String name) {
    fileNameLabel.setText(name);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#setI18Constants(gwtupload.client.IUploadStatus.UploadConstants)
   */
  public void setI18Constants(UploadStatusConstants strs) {
    assert strs != null;
    i18nStrs = strs;
  }

  /**
   * Set the percent of the upload process.
   * Override this method to update your customized progress widget. 
   * 
   * @param percent
   */
  public void setPercent(int percent) {
    setStatus(status);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#setProgress(int, int)
   */
  public void setProgress(int done, int total) {
    int percent = total > 0 ? done * 100 / total : 0;
    setPercent(percent);
    if (prg != null) {
      if (prg instanceof HasProgress) {
        ((HasProgress) prg).setProgress(done, total);
      }
    }
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#setStatus(int)
   */
  public void setStatus(Status stat) {
    String statusName = stat.toString().toLowerCase();
    statusLabel.removeStyleDependentName(statusName);
    statusLabel.addStyleDependentName(statusName);
    switch (stat) {
      case CHANGED: case QUEUED:
        updateStatusPanel(false, i18nStrs.uploadStatusQueued());
        break;
      case SUBMITING:
        updateStatusPanel(false, i18nStrs.uploadStatusSubmitting());
        break;
      case INPROGRESS:
        updateStatusPanel(true, i18nStrs.uploadStatusInProgress());
        if (!cancelCfg.contains(CancelBehavior.STOP_CURRENT)) {
          cancelLabel.setVisible(false);
        }
        break;
      case SUCCESS: case REPEATED: 
        updateStatusPanel(false, i18nStrs.uploadStatusSuccess());
        if (!cancelCfg.contains(CancelBehavior.REMOVE_REMOTE)) {
          cancelLabel.setVisible(false);
        }
        break;
      case INVALID:
        getWidget().removeFromParent();
        break;
      case CANCELING:
        updateStatusPanel(false, i18nStrs.uploadStatusCanceling());
        break;
      case CANCELED:
        updateStatusPanel(false, i18nStrs.uploadStatusCanceled());
        if (cancelCfg.contains(CancelBehavior.REMOVE_CANCELLED_FROM_LIST)) {
          getWidget().removeFromParent();
        }
        break;
      case ERROR:
        updateStatusPanel(false, i18nStrs.uploadStatusError());
        break;
      case DELETED:
        updateStatusPanel(false, i18nStrs.uploadStatusDeleted());
        getWidget().removeFromParent();
        break;
    }
    if (status != stat && onUploadStatusChangedHandler != null) {
      status = stat;
      onUploadStatusChangedHandler.onStatusChanged(this);
    }
    status = stat;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#addStatusChangedHandler(gwtupload.client.IUploadStatus.UploadStatusChangedHandler)
   */
  public void setStatusChangedHandler(final UploadStatusChangedHandler handler) {
    onUploadStatusChangedHandler = handler;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploadStatus#setVisible(boolean)
   */
  public void setVisible(boolean b) {
    panel.setVisible(b);
  }

  /**
   * Override the default progress widget with a customizable one.
   * 
   * @param progress
   */
  protected void setProgressWidget(Widget progress) {
    if (prg != null) {
      panel.remove(prg);
    }
    prg = progress;
    panel.add(prg);
    prg.setVisible(false);
  }

  /**
   * Thought to be overridable by the user when extending this.
   * 
   * @param showProgress
   * @param message
   */
  protected void updateStatusPanel(boolean showProgress, String message) {
    if (showProgress && prg == null) {
      setProgressWidget(new BasicProgressBar());
    }
    if (prg != null) {
      prg.setVisible(showProgress);
    }

    fileNameLabel.setVisible(prg instanceof BasicProgressBar || !showProgress);
    statusLabel.setVisible(!showProgress);

    statusLabel.setText(message);
    cancelLabel.setVisible(hasCancelActions && !cancelCfg.contains(CancelBehavior.DISABLED));
  }

}
