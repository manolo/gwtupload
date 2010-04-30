/*
 * Copyright 2009 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

import gwtupload.client.BaseUploadStatus;
import gwtupload.client.IUploader;
import gwtupload.client.PreloadedImage;
import gwtupload.client.SingleUploader;
import gwtupload.client.PreloadedImage.OnLoadPreloadedImageHandler;

/**
 * <p>
 * An experimental widget.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 *
 */
public class SingleUploadSampleUsingIFrames implements EntryPoint {

  FlowPanel panelImages = new FlowPanel();

  OnLoadPreloadedImageHandler showImage = new OnLoadPreloadedImageHandler() {
    public void onLoad(PreloadedImage img) {
      img.setWidth("75px");
      panelImages.add(img);
    }
  };

  private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler() {
    public void onFinish(IUploader uploader) {
      new PreloadedImage(uploader.fileUrl(), showImage);
    }
  };

  public void onModuleLoad() {
    Button b = new Button("Send the file !");
    final Frame f = new Frame();
    RootPanel.get().add(f);
    AbsolutePanel fp = new AbsolutePanel(getIframePanel(f.getElement())) { };

    BaseUploadStatus s = new BaseUploadStatus();
    RootPanel.get().add(s.getWidget());

    final SingleUploader uploader = new SingleUploader(s, b);
    uploader.addOnFinishUploadHandler(onFinishUploaderHandler);
    fp.add(uploader);
    f.setWidth(uploader.getOffsetWidth() + 20 + "px");
    f.setHeight(uploader.getOffsetHeight() + 15 + "px");
    RootPanel.get().add(b);
    RootPanel.get().add(panelImages);

    Button c = new Button("Cancel the file !", new ClickHandler() {
      public void onClick(ClickEvent event) {
        cancelIframe(f.getElement());
        uploader.cancel();
      }
    });
    RootPanel.get().add(c);
  }
  
  private native void cancelIframe(Element iframe) /*-{
    var win = iframe.contentWindow.location = "empty.html";
    var win = iframe.contentWindow.location.reload();
  }-*/;

  private native Element getIframePanel(Element iframe) /*-{
    var win = iframe.contentWindow;
    var doc = iframe.contentDocument || iframe.contentWindow.document;
    win.$win = win;
    win.$doc = doc;
    doc.open();
    doc.write("<body></body>");
    doc.close();
    var body = doc.body;
    var div = doc.createElement("div");
    body.appendChild(div);    
    return div;
  }-*/;

}
