/*
 * Copyright 2010 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
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
package jsupload.client;

import com.google.gwt.core.client.GWT;

import gwtupload.client.IUploader.UploaderConstants;

import java.util.HashMap;

/**
 * Internationalizable constants.
 * 
 * @author Manuel Carrasco Moñino
 */
public class I18nConstants implements UploaderConstants {
  
  UploaderConstants defaultStrs = GWT.create(UploaderConstants.class);
  HashMap<String, String> strs = new HashMap<String, String>();
  
  public I18nConstants(JsProperties prop, String regional) {
    if (prop.defined(regional)) {
      JsProperties reg = prop.getJsProperties(regional);
      for (String key : reg.keys()) {
        strs.put(key, reg.get(key));
      }
    }
  }
  
  public String or(String...values) {
    for (String val : values) {
      if (val != null) {
        return val;
      }
    }
    return null;
  }
  
  public String uploaderActiveUpload() {
    return or(strs.get(Const.TXT_ACTIVE_UPLOAD), defaultStrs.uploaderActiveUpload());
  }
  public String uploaderAlreadyDone() {
    return or(strs.get(Const.TXT_ALREADY_DONE), defaultStrs.uploaderAlreadyDone());
  }
  public String uploaderBlobstoreError() {
    return defaultStrs.uploaderBlobstoreError();
  }
  public String uploaderBrowse() {
    return or(strs.get(Const.TXT_BROWSE), defaultStrs.uploaderBrowse());
  }
  public String uploaderInvalidExtension() {
    return or(strs.get(Const.TXT_INVALID_EXTENSION), defaultStrs.uploaderInvalidExtension());
  }
  public String uploaderSend() {
    return or(strs.get(Const.TXT_SEND), defaultStrs.uploaderSend());
  }
  public String uploaderServerError() {
    return or(strs.get(Const.TXT_SERVER_ERROR), defaultStrs.uploaderServerError());
  }
  public String uploaderServerUnavailable() {
    return or(strs.get(Const.TXT_SERVER_UNAVAILABLE), defaultStrs.uploaderServerUnavailable());
  }
  public String uploaderTimeout() {
    return or(strs.get(Const.TXT_TIMEOUT), defaultStrs.uploaderTimeout());
  }
  
  public String uploadLabelCancel() {
    return or(strs.get(Const.TXT_CANCEL), defaultStrs.uploadLabelCancel());
  }
  public String uploadStatusCanceled() {
    return or(strs.get(Const.TXT_CANCELED), defaultStrs.uploadStatusCanceled());
  }
  public String uploadStatusCanceling() {
    return or(strs.get(Const.TXT_CANCELING), defaultStrs.uploadStatusCanceling());
  }
  public String uploadStatusDeleted() {
    return or(strs.get(Const.TXT_DELETED), defaultStrs.uploadStatusDeleted());
  }
  public String uploadStatusError() {
    return or(strs.get(Const.TXT_ERROR), defaultStrs.uploadStatusError());
  }
  public String uploadStatusInProgress() {
    return or(strs.get(Const.TXT_INPROGRESS), defaultStrs.uploadStatusInProgress());
  }
  public String uploadStatusQueued() {
    return or(strs.get(Const.TXT_QUEUED), defaultStrs.uploadStatusQueued());
  }
  public String uploadStatusSubmitting() {
    return or(strs.get(Const.TXT_SUBMITING), defaultStrs.uploadStatusSubmitting());
  }
  public String uploadStatusSuccess() {
    return or(strs.get(Const.TXT_SUCCESS), defaultStrs.uploadStatusSuccess());
  }
}