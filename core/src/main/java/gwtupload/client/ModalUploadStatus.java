/*
 * Copyright 2007 Manuel Carrasco Moñino. (manolo at apache/org) 
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

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *<p>
 * Modal progress widget that implements the IUploadStatus interface.
 *</p>
 *
 * @author Manolo Carrasco Moñino
 * 
 * 
 */
public class ModalUploadStatus extends BaseUploadStatus {
  protected PopupPanel box = new PopupPanel(false, true);
  
  public ModalUploadStatus() {
    super();
    super.getWidget().addStyleName("upld-status");
    box.add(super.getWidget());
    ((Element) box.getElement().getFirstChild()).setClassName("GWTUpld");
  }
  
  /**
   * Returns an empty html widget, 
   * so, PopupPanel will never attached to the document by the user
   * and it will be attached when show() is called.
   */
  @Override public Widget getWidget() {
    return new HTML();
  };
  
  /**
   * show/hide the modal dialog.
   */
  @Override
  public void setVisible(boolean b) {
    if (b) {
      box.center();
    } else {
      box.hide();
    }
  }
}