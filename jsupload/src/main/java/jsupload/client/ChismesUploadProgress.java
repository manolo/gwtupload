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

import com.google.code.p.gwtchismes.client.GWTCAlert;
import com.google.code.p.gwtchismes.client.GWTCProgress;
import com.google.gwt.user.client.ui.Widget;

import gwtupload.client.BaseUploadStatus;
import gwtupload.client.IUploadStatus;

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
  GWTCAlert alert = new GWTCAlert();

  boolean asDialog = false;

  GWTCProgress prg;
  private String prgBarText = "{0}% {1}/{2} KB. ({3} KB/s)";

  public ChismesUploadProgress(boolean asDialog) {
    this.asDialog = asDialog;
    prg = new GWTCProgress(asDialog ? 60 : 20, asDialog ? GWTCProgress.SHOW_AS_DIALOG | GWTCProgress.SHOW_TIME_REMAINING | prgBarOption : prgBarOption);
    setProgressWidget(prg);
    prg.setVisible(true);
    setPercentMessage(prgBarText);
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
      alert.alert(error);
    }
  }
  
  @Override
  public void setFileName(String name) {
    if (!asDialog) {
      super.setFileName(name);
    }
    prg.setText(name);
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
  public void setProgress(int a, int b) {
    prg.setProgress(a, b);
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

}
