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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import gwtupload.client.IUploader.UploaderConstants;

import java.util.HashMap;

/**
 * A decorated file upload is a widget which hides a FileUpload showing
 * a clickable and customizable Widget, normally a button.
 * 
 * @author Manuel Carrasco Moñino
 *
 */
public class DecoratedFileUpload extends Composite implements HasText, HasName, HasChangeHandlers {

  /**
   * A FileUpload which implements onChange, onMouseOver and onMouseOut events.
   * 
   * Note FileUpload in version 2.0.x implements onChange event, but we put it here 
   * in order to be compatible with 1.6.x
   *
   */
  public static class FileUploadWithMouseEvents extends FileUpload implements HasMouseOverHandlers, HasMouseOutHandlers, HasChangeHandlers {

    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
      return addDomHandler(handler, ChangeEvent.getType());
    }

    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
      return addDomHandler(handler, MouseOutEvent.getType());
    }

    public HandlerRegistration addMouseOverHandler(final MouseOverHandler handler) {
      return addDomHandler(handler, MouseOverEvent.getType());
    }
  }

  /**
   * An abstract class which is the base for specific browser implementations.
   */
  private abstract static class DecoratedFileUploadImpl {

    protected Widget button;
    protected AbsolutePanel container;
    protected FileUploadWithMouseEvents input;

    public void init(AbsolutePanel container, FileUploadWithMouseEvents input) {
      this.container = container;
      this.input = input;
    }

    public void resize() {
      if (button != null) {
        container.setWidth(button.getOffsetWidth() + "px");
        container.setHeight(button.getOffsetHeight() + "px");
      }
    }

    public void setButton(Widget widget) {
      this.button = widget;
      if (button instanceof HasMouseOverHandlers) {
        ((HasMouseOverHandlers) button).addMouseOverHandler(new MouseOverHandler() {
          public void onMouseOver(MouseOverEvent event) {
            button.addStyleDependentName(STYLE_BUTTON_OVER_SUFFIX);
          }
        });
      }
      if (button instanceof HasMouseOutHandlers) {
        ((HasMouseOutHandlers) button).addMouseOutHandler(new MouseOutHandler() {
          public void onMouseOut(MouseOutEvent event) {
            button.removeStyleDependentName(STYLE_BUTTON_OVER_SUFFIX);
          }
        });
      }
    }
  }

  /**
   * Implementation for browsers which support the click() method:
   * IE, Chrome, Safari
   * 
   * The hack here is to put the customized button
   * and the file input statically positioned in an absolute panel. 
   * This panel has the size of the button, and the input is not shown 
   * because it is placed out of the width and height panel limits.
   * 
   */
  @SuppressWarnings("unused")
  private static class DecoratedFileUploadImplClick extends DecoratedFileUploadImpl {

    private static HashMap<Widget, HandlerRegistration> clickHandlerCache = new HashMap<Widget, HandlerRegistration>();

    private static native void clickOnInputFile(Element elem) /*-{
      elem.click();
    }-*/;

    public void init(AbsolutePanel container, FileUploadWithMouseEvents input) {
      super.init(container, input);
      container.add(input, 500, 500);
      DOM.setStyleAttribute(container.getElement(), "cssFloat", "left");
      DOM.setStyleAttribute(container.getElement(), "display", "inline");
    }

    public void setButton(Widget widget) {
      super.setButton(widget);
      HandlerRegistration clickRegistration = clickHandlerCache.get(widget);
      if (clickRegistration != null) {
        clickRegistration.removeHandler();
      }
      if (button != null) {
        if (button instanceof HasClickHandlers) {
          clickRegistration = ((HasClickHandlers) button).addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              clickOnInputFile(input.getElement());
            }
          });
          clickHandlerCache.put(widget, clickRegistration);
        }
      }
    }
  }

  /**
   * Implementation for browsers which do not support the click() method:
   * FF, Opera
   * 
   * The hack here is to place the customized button and the file input positioned
   * statically in an absolute panel which has size of the button. 
   * The file input is wrapped into a transparent panel, which also has the button
   * size and is placed covering the customizable button.
   * 
   * When the user puts his mouse over the button and clicks on it, what really 
   * happens is that the user clicks on the transparent file input showing
   * the choose file dialog.
   * 
   */  
  @SuppressWarnings("unused")
  private static class DecoratedFileUploadImplNoClick extends DecoratedFileUploadImpl {

    private SimplePanel wrapper;

    public void init(AbsolutePanel container, FileUploadWithMouseEvents input) {
      super.init(container, input);
      wrapper = new SimplePanel();
      wrapper.add(input);
      container.add(wrapper, 0, 0);
      wrapper.setStyleName("wrapper");
      
      // Not using GWT 2.0.x way to set Style attributes in order to be
      // compatible with old GWT releases
      DOM.setStyleAttribute(container.getElement(), "cssFloat", "left");
      DOM.setStyleAttribute(wrapper.getElement(), "textAlign", "left");
      DOM.setStyleAttribute(wrapper.getElement(), "zIndex", "1");
      DOM.setStyleAttribute(input.getElement(), "marginLeft", "-1500px");
      DOM.setStyleAttribute(input.getElement(), "fontSize", "500px");
      DOM.setStyleAttribute(input.getElement(), "borderWidth", "0px");
      DOM.setStyleAttribute(input.getElement(), "opacity", "0");
      DOM.setElementAttribute(input.getElement(), "size", "1");
      
      // Trigger over and out handlers which already exist in the covered button.
      input.addMouseOverHandler(new MouseOverHandler() {
        public void onMouseOver(MouseOverEvent event) {
          if (button != null) {
            button.fireEvent(event);
          }
        }
      });
      input.addMouseOutHandler(new MouseOutHandler() {
        public void onMouseOut(MouseOutEvent event) {
          if (button != null) {
            button.fireEvent(event);
          }
        }
      });
    }

    public void resize() {
      super.resize();
      if (button != null) {
        wrapper.setWidth(button.getOffsetWidth() + "px");
        wrapper.setHeight(button.getOffsetHeight() + "px");
      }
    }
  }

  private static final String STYLE_BUTTON_OVER_SUFFIX = "over";
  private static final String STYLE_CONTAINER = "DecoratedFileUpload";
  private static final String STYLE_DISABLED = "disabled";
  protected Widget button;
  protected AbsolutePanel container;
  protected FileUploadWithMouseEvents input = new FileUploadWithMouseEvents();
  protected boolean reuseButton = false;
  private DecoratedFileUploadImpl impl;
  private UploaderConstants strs = GWT.create(UploaderConstants.class);

  public DecoratedFileUpload() {
    impl = GWT.create(DecoratedFileUploadImpl.class);
    container = new AbsolutePanel();
    container.addStyleName(STYLE_CONTAINER);
    initWidget(container);
    impl.init(container, input);
  }

  public DecoratedFileUpload(Widget button) {
    this();
    setButton(button);
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return input.addChangeHandler(handler);
  }

  public String getFilename() {
    return input.getFilename();
  }

  public FileUpload getFileUpload() {
    return input;
  }

  public String getName() {
    return input.getName();
  }

  public String getText() {
    if (button == null) {
      return "";
    }
    if (button instanceof HasText) {
      return ((HasText) button).getText();
    } else {
      return button.toString();
    }
  }

  public Widget getWidget() {
    return this;
  }

  public boolean isEnabled() {
    return input.isEnabled();
  }

  @Override
  public void onAttach() {
    super.onAttach();
    if (button == null) {
      button = new Button(strs.uploaderBrowse());
      setButton(button);
    } else {
      impl.resize();
    }
  }

  public void setButton(Widget button) {
    assert button instanceof HasClickHandlers : "Button should extend HasClickHandlers";
    if (this.button != null) {
      container.remove(this.button);
    }
    this.button = button;
    container.add(button, 0, 0);
    impl.setButton(button);
    impl.resize();
  }

  public void setButtonSize(String width, String height) {
    button.setSize(width, height);
    impl.resize();
  }

  public void setEnabled(boolean b) {
    input.setEnabled(b);
    if (b) {
      container.removeStyleDependentName(STYLE_DISABLED);
    } else {
      container.addStyleDependentName(STYLE_DISABLED);
    }
  }
  
  public void setName(String fieldName) {
    input.setName(fieldName);
  }

  public void setText(String text) {
    if (button instanceof HasText) {
      ((HasText) button).setText(text);
      impl.resize();
    }
  }  

}
