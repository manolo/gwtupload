package jsupload.client;

import gwtupload.client.IUploader.UploaderConstants;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;

public class I18nConstants implements UploaderConstants{
  
  UploaderConstants defaultStrs = GWT.create(UploaderConstants.class);
  HashMap<String, String> strs = new HashMap<String, String>();
  
  public I18nConstants (JsProperties prop, String regional) {
    if (prop.defined(regional)) {
      JsProperties reg = prop.getJsProperties(regional);
      for (String key : reg.keys()) 
        strs.put(key, reg.get(key));
    }
  }
  
  public String firstValue (String...values) {
    for (String val : values) 
      if (val != null)
        return val;
    return null;
  }
  
  public String uploaderActiveUpload() {
    return firstValue(strs.get(Const.TXT_ACTIVE_UPLOAD), defaultStrs.uploaderActiveUpload());
  }
  public String uploaderAlreadyDone() {
    return firstValue(strs.get(Const.TXT_ALREADY_DONE), defaultStrs.uploaderAlreadyDone());
  }
  public String uploaderInvalidExtension() {
    return firstValue(strs.get(Const.TXT_INVALID_EXTENSION), defaultStrs.uploaderInvalidExtension());
  }
  public String uploaderSend() {
    return firstValue(strs.get(Const.TXT_SEND), defaultStrs.uploaderSend());
  }
  public String uploaderServerError() {
    return firstValue(strs.get(Const.TXT_SERVER_ERROR), defaultStrs.uploaderServerError());
  }
  public String uploaderServerUnavailable() {
    return firstValue(strs.get(Const.TXT_SERVER_UNAVAILABLE), defaultStrs.uploaderServerUnavailable());
  }
  public String uploaderTimeout() {
    return firstValue(strs.get(Const.TXT_TIMEOUT), defaultStrs.uploaderTimeout());
  }
  public String uploadLabelCancel() {
    return firstValue(strs.get(Const.TXT_CANCEL), defaultStrs.uploadLabelCancel());
  }
  public String uploadStatusCanceled() {
    return firstValue(strs.get(Const.TXT_CANCELED), defaultStrs.uploadStatusCanceled());
  }
  public String uploadStatusCanceling() {
    return firstValue(strs.get(Const.TXT_CANCELING), defaultStrs.uploadStatusCanceling());
  }
  public String uploadStatusError() {
    return firstValue(strs.get(Const.TXT_ERROR), defaultStrs.uploadStatusError());
  }
  public String uploadStatusInProgress() {
    return firstValue(strs.get(Const.TXT_INPROGRESS), defaultStrs.uploadStatusInProgress());
  }
  public String uploadStatusQueued() {
    return firstValue(strs.get(Const.TXT_QUEUED), defaultStrs.uploadStatusQueued());
  }
  public String uploadStatusSubmitting() {
    return firstValue(strs.get(Const.TXT_SUBMITING), defaultStrs.uploadStatusSubmitting());
  }
  public String uploadStatusSuccess() {
    return firstValue(strs.get(Const.TXT_SUCCESS), defaultStrs.uploadStatusSuccess());
  }

  public String uploadStatusDeleted() {
    return firstValue(strs.get(Const.TXT_DELETED), defaultStrs.uploadStatusDeleted());
  }
  
}