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
import gwtupload.client.MultiUploader;
import gwtupload.client.PreloadedImage;
import gwtupload.client.PreloadedImage.OnLoadPreloadedImageHandler;
import jsupload.client.ChismesUploadProgress;
import jsupload.client.IncubatorUploadProgress;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * <p>
 * An example of a MultiUploader panel using incubator's progress bar widget.
 * The example also uses PreloadedImage to display uploaded images
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public class MultipleUploadSample implements EntryPoint {

  // Load the image in the document and in the case of success attach it to the viewer
  private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler() {
    public void onFinish(IUploader uploader) {
      if (uploader.getStatus() == Status.SUCCESS) {
        for (int i = 0; i < uploader.fileUrls().size(); i++) {
          new PreloadedImage(uploader.fileUrls().get(i), showImage);
        }
      }
    }
  };

  // A panel where the thumbnails of uploaded images will be shown
  private FlowPanel panelImages = new FlowPanel();

  // Attach an image to the pictures viewer
  private OnLoadPreloadedImageHandler showImage = new OnLoadPreloadedImageHandler() {
    public void onLoad(PreloadedImage image) {
      image.setWidth("75px");
      panelImages.add(image);
    }
  };

  public void onModuleLoad() {
    // Attach the image viewer to the document
    RootPanel.get("thumbnails").add(panelImages);
    
    // Create a new uploader panel and attach it to the document
    MultiUploader defaultUploader = new MultiUploader();
    RootPanel.get("default").add(defaultUploader);
    
    
    // Add a finish handler which will load the image once the upload finishes
    defaultUploader.addOnFinishUploadHandler(onFinishUploaderHandler);
    defaultUploader.setMaximumFiles(3);
    defaultUploader.setFileInputPrefix("default");
    // You can add customized parameters to servlet call 
    defaultUploader.setServletPath(defaultUploader.getServletPath() + "?foo=bar");
    defaultUploader.avoidRepeatFiles(true);
    // This enables php apc progress mechanism
    defaultUploader.add(new Hidden("APC_UPLOAD_PROGRESS", defaultUploader.getInputName()));

    MultiUploader incubatorUploader = new MultiUploader(FileInputType.ANCHOR, new IncubatorUploadProgress());
    incubatorUploader.addOnFinishUploadHandler(onFinishUploaderHandler);
    incubatorUploader.setValidExtensions("jpg", "jpeg", "png", "gif");
    RootPanel.get("incubator").add(incubatorUploader);


    MultiUploader chismesUploader = new MultiUploader(FileInputType.BUTTON, new ChismesUploadProgress(false));//, new DecoratedFileInput(new GWTCButton()));
    chismesUploader.addOnFinishUploadHandler(onFinishUploaderHandler);
    RootPanel.get("chismes").add(chismesUploader);
    
    MUpld m = new MUpld();
    RootPanel.get("uibinder").add(m);
  }

}
