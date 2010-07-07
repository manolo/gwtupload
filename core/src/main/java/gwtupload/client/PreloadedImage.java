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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Manolo Carrasco Moñino
 *  
 * <p> An Image widget that preloads the picture, and in the case of success executes a user action. 
 * It stores the original width and height of the image that can be used for calculations. </p>
 */
public class PreloadedImage extends Image implements HasJsData {

  /**
   * Handler called when the image load raises an error.
   */
  public interface OnErrorPreloadedImageHandler extends EventHandler {
    void onError(PreloadedImage image);
  }
  
  /**
   * Handler called when the image is loaded successfully.
   */
  public interface OnLoadPreloadedImageHandler extends EventHandler {
    void onLoad(PreloadedImage image);
  }
  
  private PreloadedImage thisInstance;
  private String containerId;
  private HandlerRegistration errHandler = null;
  private ErrorHandler imgErrorListener = new ErrorHandler() {
    public void onError(ErrorEvent event) {
      loadHandler.removeHandler();
      errHandler.removeHandler();
      Image img = (Image) event.getSource();
      if (img != null) {
        img.removeFromParent();
      }
      if (onError != null) {
        onError.onError(thisInstance);
      }
    }
  };
  private LoadHandler imgLoadListener = new LoadHandler() {
    public void onLoad(LoadEvent event) {
      loadHandler.removeHandler();
      errHandler.removeHandler();
      Image img = (Image) event.getSource();
      if (img != null) {
        img.setVisible(true);
        realWidth = img.getWidth();
        realHeight = img.getHeight();
      }
      if (containerId != null && RootPanel.get(containerId) != null) {
        RootPanel.get(containerId).add(thisInstance);
      }
      if (onLoad != null) {
        onLoad.onLoad(thisInstance);
      }
    }
  };
  private HandlerRegistration loadHandler = null;
  private String name = null;
  private OnErrorPreloadedImageHandler onError = null;
  private OnLoadPreloadedImageHandler onLoad = null;
  
  private int realWidth = 0, realHeight = 0;
  
  private String uniqId = null;
  
  /**
   * Constructor.
   */
  public PreloadedImage() {
    thisInstance = this;
    loadHandler = super.addLoadHandler(imgLoadListener);
    errHandler = super.addErrorHandler(imgErrorListener);
  }
  
  /**
   * Constructor.
   * 
   * @param url
   *               The image url
   * @param onLoad 
   *               handler to be executed in the case of success loading
   */
  public PreloadedImage(String url, OnLoadPreloadedImageHandler onLoad) {
    this();
    setOnloadHandler(onLoad);
    setUrl(url);
  }
  
  /**
   * Constructor.
   * 
   * @param url
   *               The image url
   * @param uniqId
   *               A unique id that identifies this image
   * @param name
   *               The name for this image
   * @param onLoad
   *               handler to be executed in the case of success loading
   */
  public PreloadedImage(String url, String uniqId, String name, OnLoadPreloadedImageHandler onLoad) {
    this(url, onLoad);
    this.uniqId = uniqId;
    this.name = name;
  }
  
  /* (non-Javadoc)
   * @see gwtupload.client.HasJsData#getData()
   */
  public JavaScriptObject getData() {
    return null;
  }

  /**
   * get the name of this image.
   */
  public String getName() {
        return name;
      }

  /**
   * Get the real size of the image. 
   * It is calculated when the image loads
   * 
   */
  public int getRealHeight() {
    return realHeight;
  }

  /**
   * Get the real size of the image. 
   * It is calculated when the image loads
   * 
   */
  public int getRealWidth() {
    return realWidth;
  }

  /**
   * Get the uniqId assigned to this image.
   */
  public String getUniqId() {
    return uniqId;
  }

  /**
   * Set the element's id where the image will be attached.
   * 
   * @param id of the DOM element
   */
  public void setContainerId(String id) {
    containerId = id;
  }

  public void setName(String name) {
        this.name = name;
  }
  
  /**
   * Set the handler to be called in the case of error.
   * 
   * @param onError
   */
  public void setOnErrorHandler(OnErrorPreloadedImageHandler onError) {
    this.onError = onError;
  }

  /**
   * Set the handler to be called when the image has been sucessfuly loaded.
   * @param onLoad
   */
  public void setOnloadHandler(OnLoadPreloadedImageHandler onLoad) {
    this.onLoad = onLoad;
  }

  public void setUniqId(String uniqId) {
        this.uniqId = uniqId;
      }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.Image#setUrl(java.lang.String)
   */
  public void setUrl(String url) {
    super.setUrl(url);
    RootPanel.get().add(this);
    setVisible(false);
  }

}
