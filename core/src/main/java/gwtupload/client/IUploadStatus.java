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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>
 * Interface used by uploaders to notify the progress status.
 * </p>
 *
 * @author Manolo Carrasco Moñino
 *
 */
public interface IUploadStatus extends HasProgress, IsWidget {

  /**
   * Enumeration of possible cancel options
   */
  public static enum CancelBehavior {
    DISABLED, REMOVE_CANCELLED_FROM_LIST, REMOVE_REMOTE, STOP_CURRENT, REMOVE_INVALID
  }

  /**
   * Enumeration of possible status values
   */
  public static enum Status {
    CANCELED, CANCELING, DELETED, DONE, ERROR, INPROGRESS, QUEUED, REPEATED, INVALID, SUBMITING, SUCCESS, UNINITIALIZED, CHANGED
  }

  /**
   * Handler called when the user clicks on the cancel button.
   */
  public interface UploadCancelHandler extends EventHandler {
    void onCancel();
  }

  /**
   * Handler called when the status changes.
   */
  public interface UploadStatusChangedHandler extends EventHandler {
    void onStatusChanged(IUploadStatus statusWiget);
  }

  /**
   * Interface for internationalizable elements.
   */
  public interface UploadStatusConstants extends Constants {

    @DefaultStringValue(" ")
    String uploadLabelCancel();
    @DefaultStringValue("Canceled")
    String uploadStatusCanceled();
    @DefaultStringValue("Canceling ...")
    String uploadStatusCanceling();
    @DefaultStringValue("Deleted")
    String uploadStatusDeleted();
    @DefaultStringValue("Error")
    String uploadStatusError();
    @DefaultStringValue("In progress")
    String uploadStatusInProgress();
    @DefaultStringValue("Queued")
    String uploadStatusQueued();
    @DefaultStringValue("Submitting form ...")
    String uploadStatusSubmitting();
    @DefaultStringValue("Done")
    String uploadStatusSuccess();
  }

  public Set<CancelBehavior> DEFAULT_CANCEL_CFG = EnumSet.of(CancelBehavior.REMOVE_REMOTE, CancelBehavior.STOP_CURRENT);
  public Set<CancelBehavior> DEFAULT_MULTI_CFG = EnumSet.of(CancelBehavior.STOP_CURRENT, CancelBehavior.REMOVE_REMOTE, CancelBehavior.REMOVE_INVALID, CancelBehavior.REMOVE_CANCELLED_FROM_LIST);
  public Set<CancelBehavior> GMAIL_MULTI_CFG = EnumSet.of(CancelBehavior.STOP_CURRENT, CancelBehavior.REMOVE_REMOTE, CancelBehavior.REMOVE_INVALID, CancelBehavior.REMOVE_CANCELLED_FROM_LIST);

  /**
   * Add a new  handler which will be fired when the user clicks on the cancel button.
   */
  HandlerRegistration addCancelHandler(UploadCancelHandler handler);

  /**
   * Return the status of the upload process.
   */
  Status getStatus();

  /**
   * Called for getting the container widget.
   * @return The container widget
   */
  @Deprecated
  Widget getWidget();

  /**
   * Creates a new instance of the current object type.
   */
  IUploadStatus newInstance();

  /**
   * Set the configuration for the cancel action.
   *
   * @param config
   *   Set of configuration parameters.
   *   TIP: Use EnumSet.of() to fill them.
   *   You have a set of predefined configurations predefined:
   *   IUploadStatus.DEFAULT_CANCEL_CFG IUploadStatus.DEFAULT_MULTI_CFG IUploadStatus.GMAIL_MULTI_CFG
   */
  void setCancelConfiguration(Set<IUploadStatus.CancelBehavior> config);

  /**
   * Called when an error is detected.
   */
  void setError(String error);

  /**
   * Called when the uploader knows the filenames selected by the user.
   */
  void setFileNames(List<String> names);

  /**
   * Internationalize the UploadStatus widget.
   */
  void setI18Constants(UploadStatusConstants strs);

  /**
   * Set the process status.
   */
  void setStatus(IUploadStatus.Status status);

  /**
   * Set the handler which will be fired when the status changes.
   */
  void setStatusChangedHandler(UploadStatusChangedHandler handler);

  /**
   * show/hide the widget.
   */
  void setVisible(boolean b);

  Set<CancelBehavior> getCancelConfiguration();
}