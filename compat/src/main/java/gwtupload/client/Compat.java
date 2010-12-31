package gwtupload.client;

import gwtupload.client.IFileInput.FileInputType;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class Compat implements EntryPoint {
  
  final HTML htmlButton = new HTML(){{
    setSize("159px", "27px");
    DOM.setStyleAttribute(getElement(), "backgroundImage", "url(chooseFile.png)");
    addMouseOverHandler(new MouseOverHandler() {
      public void onMouseOver(MouseOverEvent arg0) {
        DOM.setStyleAttribute(getElement(), "backgroundImage", "url(chooseFileO.png)");
      }
    });
    addMouseOutHandler(new MouseOutHandler() {
      public void onMouseOut(MouseOutEvent arg0) {
        DOM.setStyleAttribute(getElement(), "backgroundImage", "url(chooseFile.png)");
      }
    });
  }};
  
  final Image imgButton = new Image("chooseFile.png") {{
    setSize("159px", "27px");
    addMouseOverHandler(new MouseOverHandler() {
      public void onMouseOver(MouseOverEvent arg0) {
        imgButton.setUrl("chooseFileO.png");
      }
    });
    addMouseOutHandler(new MouseOutHandler() {
      public void onMouseOut(MouseOutEvent arg0) {
        imgButton.setUrl("chooseFile.png");
      }
    });
  }};
  
  MultiUploader multi = new MultiUploader(FileInputType.CUSTOM.with(imgButton));
  SingleUploader single = new SingleUploader(FileInputType.ANCHOR);
  
  public void onModuleLoad() {
    RootPanel.get().add(single);
    RootPanel.get().add(multi);
  }
}
