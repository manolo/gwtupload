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

import gwtupload.client.BaseUploadStatus;
import gwtupload.client.IUploadStatus;
import gwtupload.client.Utils;

import java.util.List;

import com.google.code.p.gwtchismes.client.GWTCProgress;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>
 * Upload progress using Chismes's progress-bar and alert widgets.
 * </p>
 *  
 * @author Manolo Carrasco Moñino
 *
 */
public class ChismesUploadProgress extends BaseUploadStatus {

  public int prgBarElements = 40;
  public int prgBarOption = GWTCProgress.SHOW_NUMBERS | GWTCProgress.SHOW_TEXT;

  boolean asDialog = false;

  GWTCProgress prg;
  private String prgBarText = "{0}% {1}/{2} KB. ({3} KB/s)";
  
  public ChismesUploadProgress() {
    this(false);
  }

  public ChismesUploadProgress(boolean asDialog) {
    this.asDialog = asDialog;
    prg = new GWTCProgress(asDialog ? 60 : 20, asDialog ? GWTCProgress.SHOW_AS_DIALOG | GWTCProgress.SHOW_TIME_REMAINING | prgBarOption : prgBarOption);
    setProgressWidget(prg);
    panel.add(prg);
    prg.setVisible(true);
    setPercentMessage(prgBarText);
  }
  
  
  @Override
  protected Panel getPanel() {
    return new FlexTable() {
      public void add(Widget child) {
        if (child.equals(cancelLabel)) {
          setWidget(0, 0, child);
        } else if (child.equals(fileNameLabel)) {
          setWidget(0, 1, child);
        } else if (child.equals(statusLabel)) {
          setWidget(0, 2, child);
        } else {
          setWidget(1, 1, child);
        }
      }
    };
  }
  
  @Override
  public Widget getWidget() {
    return asDialog ? prg : super.getWidget();
  }
  
  @Override
  public IUploadStatus newInstance() {
    return new ChismesUploadProgress(asDialog);
  }
  
  @Override
  public void setError(String error) {
    setStatus(IUploadStatus.Status.ERROR);
    if (error != null && error.length() > 0) {
      Window.alert(error);
    }
  }
  
  public void setHoursMessage(String message) {
    if (message != null) {
      prg.setHoursMessage(message);
    }
  }

  public void setMinutesMessage(String message) {
    if (message != null) {
      prg.setMinutesMessage(message);
    }
  }

  public void setPercentMessage(String message) {
    if (message != null) {
      prg.setPercentMessage(message);
      prg.setTotalMessage(message);
    }
  }

  @Override
  public void setProgress(long a, long b) {
    prg.setProgress((int)a, (int)b);
  }

  public void setSecondsMessage(String message) {
    if (message != null) {
      prg.setSecondsMessage(message);
    }
  }

  @Override
  public void setVisible(boolean v) {
    if (asDialog) {
      if (v) {
        prg.show();
      } else {
        prg.hide();
      }
    } else {
      super.setVisible(v);
    }
  }
  
  @Override
  protected void updateStatusPanel(boolean showProgress, String message) {
    super.updateStatusPanel(showProgress, message);
    fileNameLabel.setVisible(true);
  }

}
