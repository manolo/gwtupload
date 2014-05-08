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
package gwtuploadsample.client;

import jsupload.client.ChismesUploadProgress;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.UploaderConstants;
import gwtupload.client.PreloadedImage;
import gwtupload.client.PreloadedImage.OnLoadPreloadedImageHandler;
import gwtupload.client.SingleUploader;
import gwtupload.client.SingleUploaderModal;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import gwtupload.client.IDropZone;

/**
 *  * <p>
 * An example of a single uploader form, using a simple and modal upload progress widget
 * The example also uses PreloadedImage to display uploaded images.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public class SingleUploadSample implements EntryPoint {

  FlowPanel panelImages = new FlowPanel();
  OnLoadPreloadedImageHandler showImage = new OnLoadPreloadedImageHandler() {
    public void onLoad(PreloadedImage img) {
      img.setWidth("75px");
      panelImages.add(img);
    }
  };

  protected UploaderConstants i18nStrs;;

  private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler() {
    public void onFinish(IUploader uploader) {
      if (uploader.getStatus() == Status.SUCCESS) {
        for (String url : uploader.getServerMessage().getUploadedFileUrls()) {
          new PreloadedImage(url, showImage);
        }
      }
    }
  };
  
  public void onModuleLoad() {
    SingleUploader single1 = new SingleUploaderModal();
    single1.addOnFinishUploadHandler(onFinishUploaderHandler);
    
    // This enables php apc progress mechanism
    single1.add(new Hidden("APC_UPLOAD_PROGRESS", single1.getInputName()), 0);
    single1.avoidEmptyFiles(false);
    RootPanel.get("single1").add(single1);
    
    SingleUploader single2 = new SingleUploaderModal(FileInputType.ANCHOR, new ChismesUploadProgress(true));
    single2.addOnFinishUploadHandler(onFinishUploaderHandler);
    RootPanel.get("single2").add(single2);
    
    SingleUploader single3 = new SingleUploader(FileInputType.BUTTON);
    single3.addOnFinishUploadHandler(onFinishUploaderHandler);
    RootPanel.get("single3").add(single3);
    
    SingleUploader single4 = new SingleUploader(FileInputType.LABEL);
    single4.setAutoSubmit(true);
    single4.setValidExtensions("jpg", "gif", "png");
    single4.addOnFinishUploadHandler(onFinishUploaderHandler);
    single4.getFileInput().asWidget().setStyleName("customButton"); 
    single4.getFileInput().asWidget().setSize("159px", "27px");
    single4.avoidRepeatFiles(true);
    RootPanel.get("single4").add(single4);
    
    SingleUploader single5 = new SingleUploader(FileInputType.DROPZONE);
    single5.setAutoSubmit(true);
    single5.setValidExtensions("jpg", "gif", "png");
    single5.addOnFinishUploadHandler(onFinishUploaderHandler);
    single5.avoidRepeatFiles(true);
    RootPanel.get("single5").add(single5);

    class IDropZoneLabel extends Label implements IDropZone {}
    
    IDropZoneLabel externalDropZone = new IDropZoneLabel();
    externalDropZone.setText("Drop files here");
    externalDropZone.setSize("160px", "30px");
    externalDropZone.getElement().getStyle().setBorderStyle(Style.BorderStyle.DASHED);
    externalDropZone.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
    SingleUploader single6 = new SingleUploader(FileInputType.DROPZONE.with((IDropZone) externalDropZone));
    single6.setAutoSubmit(true);
    single6.setValidExtensions("jpg", "gif", "png");
    single6.addOnFinishUploadHandler(onFinishUploaderHandler);
    single6.avoidRepeatFiles(true);
    RootPanel.get("single6").add(single6);
    RootPanel.get("single6").add(externalDropZone);

    RootPanel.get("thumbnails").add(panelImages);
  }

}
