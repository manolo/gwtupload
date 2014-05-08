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
package jsupload.client;

import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.SingleUploader;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Manolo Carrasco Moñino
 * 
 * Exportable version of gwt Uploader.
 * 
 * <h3>Features</h3>
 * <ul>
 * <li>Three kind of progress bar, the most advanced one shows upload speed, time remaining, sizes, progress</li>
 * <li>Single upload form: while the file is being sent the modal dialog avoid the user to interact with the application, 
 * Then the form can be used again for uploading more files.</li>
 * <li>Multiple upload form: Each time the user selects a file it goes to the queue and the user can select more files.</li>
 * <li>It can call configurable functions on the events of onChange, onStart and onFinish</li>
 * <li>The user can cancel the current upload, can delete files in the queue or remove uploaded files</li>
 * </ul>
 *  
 */

@Export
@ExportPackage("jsu")
public class Upload implements Exportable {

  IUploader uploader = null;
  Panel panel;

  private JsProperties jsProp;

  public Upload(JavaScriptObject prop) {

    this.jsProp = new JsProperties(prop);

    boolean multipleUploader = jsProp.getBoolean(Const.MULTIPLE);
    ChismesUploadProgress status = null;
    FileInputType type = FileInputType.BROWSER_INPUT;
    
    String choose = jsProp.get(Const.CHOOSE_TYPE);
    if ("button".equals(choose)) {
      type = FileInputType.BUTTON;
    } else if ("label".equals(choose)) {
      type = FileInputType.LABEL;
    } else if ("anchor".equals(choose)) {
      type = FileInputType.ANCHOR;
    } else if ("drop".equals(choose)) {
      type = FileInputType.DROPZONE;
    }
    
    if ("incubator".equals(jsProp.get(Const.TYPE))) {
      if (multipleUploader) {
        uploader = new MultiUploader(type, new IncubatorUploadProgress());
      } else {
        uploader = new SingleUploader(type);
      }
    } else if ("basic".equals(jsProp.get(Const.TYPE))) {
      if (multipleUploader) {
        uploader = new MultiUploader(type);
      } else {
        uploader = new SingleUploader(type);
      }
    } else {
      status = new ChismesUploadProgress(!multipleUploader);
      uploader = multipleUploader ? new MultiUploader(type, status) : new SingleUploader(type, status); 
    }
    
    if (multipleUploader) {
      ((MultiUploader) uploader).setMaximumFiles(jsProp.getInt(Const.MAX_FILES));
    } else if (jsProp.getBoolean(Const.EMPTY)){
      ((SingleUploader) uploader).avoidEmptyFiles(false);
    }
    
    boolean auto = jsProp.defined(Const.AUTO) ? jsProp.getBoolean(Const.AUTO) : multipleUploader;
    uploader.setAutoSubmit(auto);

    boolean multipleSelection = jsProp.getBoolean(Const.MULTIPLE_SELECTION);
    uploader.setMultipleSelection(multipleSelection);
    
    uploader.addOnStartUploadHandler(JsUtils.getOnStartUploaderHandler(jsProp.getClosure(Const.ON_START)));
    uploader.addOnChangeUploadHandler(JsUtils.getOnChangeUploaderHandler(jsProp.getClosure(Const.ON_CHANGE)));
    uploader.addOnFinishUploadHandler(JsUtils.getOnFinishUploaderHandler(jsProp.getClosure(Const.ON_FINISH)));
    uploader.addOnCancelUploadHandler(JsUtils.getOnCancelUploaderHandler(jsProp.getClosure(Const.ON_CANCEL)));
    uploader.addOnStatusChangedHandler(JsUtils.getStatusChangedHandler(jsProp.getClosure(Const.ON_STATUS)));
    
    panel = RootPanel.get(jsProp.get(Const.CONT_ID, "NoId"));
    if (panel == null) {
      panel = RootPanel.get();
    }
    panel.add((Widget) uploader);

    if (jsProp.defined(Const.ACTION)) {
      uploader.setServletPath(jsProp.get(Const.ACTION));
    }

    if (jsProp.defined(Const.VALID_EXTENSIONS)) {
      String[] extensions = jsProp.get(Const.VALID_EXTENSIONS).split("[, ;:]+");
      uploader.setValidExtensions(extensions);
    }
    
    uploader.setI18Constants(new I18nConstants(jsProp, Const.REGIONAL));
    if (status != null) {
      if (jsProp.defined(Const.TXT_PERCENT)) {
        status.setPercentMessage(jsProp.get(Const.TXT_PERCENT));
      }
      if (jsProp.defined(Const.TXT_HOURS)) {
        status.setHoursMessage(jsProp.get(Const.TXT_HOURS));
      }
      if (jsProp.defined(Const.TXT_MINUTES)) {
        status.setMinutesMessage(jsProp.get(Const.TXT_MINUTES));
      }
      if (jsProp.defined(Const.TXT_SECONDS)) {
        status.setSecondsMessage(jsProp.get(Const.TXT_SECONDS));
      }
    }
  }
  
  /**
   * adds a javascript DOM element to the upload form.
   */
  public void addElement(Element e) {
    addElement(e, -1);
  }

  /**
   * adds a javascript DOM element to the upload form at the specified position
   */
  public void addElement(Element e, int index) {
    Widget w = null; 
    if (e.getTagName().toLowerCase().equals("input") && e.getAttribute("type").toLowerCase().equals("hidden")) {
      if (! Document.get().getBody().isOrHasChild(e)) {
        Document.get().getBody().appendChild(e);
      }
      w = Hidden.wrap(e);
    } else {
      w = new HTML();
      DOM.appendChild(w.getElement(), e);
    }
    uploader.add(w, index);
  }

  /**
   * Depending on the multiple feature configuration, it returns a javascript 
   * array of as many elements as images uploaded or one element.
   *  
   * The element with the uploaded info has this structure:
   *    upload.data().url      // The url to download the uploaded file from the server
   *    upload.data().name     // The name of the input form element
   *    upload.data().filename // The name of the file selected by the user as is reported by the browser
   *    upload.data().basename // The name of the file selected by the user without path
   *    upload.data().response // The raw server xml response
   *    upload.data().message  // The server text in the message tag
   *    upload.data().size     // The size of the file 
   *    upload.data().status   // The upload status (UNINITIALIZED, QUEUED, INPROGRESS, SUCCESS, ERROR, CANCELING, CANCELED, SUBMITING)
   */
  public JavaScriptObject data() {
    return uploader.getData();
  }

  /**
   * submit the upload form to the server.
   */
  public void submit() {
     uploader.submit();
  }
  
}
