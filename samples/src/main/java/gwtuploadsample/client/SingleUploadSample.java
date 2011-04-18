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

import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.UploaderConstants;
import gwtupload.client.PreloadedImage;
import gwtupload.client.PreloadedImage.OnLoadPreloadedImageHandler;
import gwtupload.client.SingleUploader;
import gwtupload.client.SingleUploaderModal;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;

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
        String msg = uploader.getServerInfo().message;
        String url = msg != null && !msg.trim().isEmpty() ? uploader.getServletPath() + "?blob-key=" : uploader.fileUrl();
        new PreloadedImage(url, showImage);      
      }
    }
  };
  
  
  public void onModuleLoad() {
    SingleUploader single1 = new SingleUploaderModal();
    single1.addOnFinishUploadHandler(onFinishUploaderHandler);
    
    // This enables php apc progress mechanism
    single1.add(new Hidden("APC_UPLOAD_PROGRESS", single1.getInputName()), 0);
    
    RootPanel.get("single1").add(single1);
    
    SingleUploader single2 = new SingleUploader(FileInputType.LABEL);
    single2.addOnFinishUploadHandler(onFinishUploaderHandler);
    RootPanel.get("single2").add(single2);

    SingleUploader single3 = new SingleUploader(FileInputType.LABEL);
    single3.setAutoSubmit(true);
    single3.setValidExtensions("jpg", "gif", "png");
    single3.addOnFinishUploadHandler(onFinishUploaderHandler);
    single3.getFileInput().getWidget().setStyleName("customButton"); 
    single3.getFileInput().getWidget().setSize("159px", "27px");
    single3.avoidRepeatFiles(true);
    RootPanel.get("single3").add(single3);

    RootPanel.get("thumbnails").add(panelImages);
  }

}
