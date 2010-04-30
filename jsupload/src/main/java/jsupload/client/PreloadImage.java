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
package jsupload.client;

import gwtupload.client.HasJsData;
import gwtupload.client.PreloadedImage;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * @author Manolo Carrasco Moñino
 * 
 * This class preloads an image in the browser, and in the case of
 * success executes a user defined function.
 * 
 * It stores the original size of the image.
 *
 */
@Export
@ExportPackage("jsu")
public class PreloadImage extends PreloadedImage implements Exportable, HasJsData {

  private JsProperties jsProp;

  public PreloadImage(JavaScriptObject prop) {
    this.jsProp = new JsProperties(prop);
    super.setUrl(jsProp.get(Const.URL));
    super.setContainerId(jsProp.get(Const.CONT_ID));
    super.setOnloadHandler(JsUtils.getOnLoadPreloadedImageHandler(this.jsProp.getClosure(Const.ON_LOAD)));
  }

  /**
   * Adds a classname to the image.
   */
  @Override
  public void addStyleName(String style) {
    super.addStyleName(style);
  }

  /**
   * Returns a properties javascript hash. 
   * This hash has the info:
   *  - url
   *  - realwidth
   *  - realheight
   */
  public JavaScriptObject getData() {
    return getDataImpl(getUrl(), getRealHeight(), getRealWidth());
  }

  /**
   * Returns the DOM element of the image.
   */
  public Element getElement() {
    return super.getElement();
  }
  
  /**
   * Returns the original height of the image.
   */
  public int realHeight() {
    return super.getRealHeight();
  }

  /**
   * Returns the original width of the image.
   */
  public int realWidth() {
    return super.getRealWidth();
  }
  
  /**
   * Sets the alt attribute of the image.
   */
  public void setAlt(String alt) {
    DOM.setElementAttribute(getElement(), "alt", alt);
  }
  
  /**
   *  Change the size of the image in the document.
   */
  public void setSize(int width, int height) {
    if (width > 0) {
      setWidth(width + "px");
    }
    if (height > 0) {
      setHeight(height + "px");
    }
  }
  
  private native JavaScriptObject getDataImpl(String url, int height, int width) /*-{
    return {
       url: url,
       realwidth: width,
       realheight: height
    };
  }-*/;

}
