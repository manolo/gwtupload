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

import com.google.gwt.widgetideas.client.ProgressBar;
import com.google.gwt.widgetideas.client.ProgressBar.TextFormatter;

import gwtupload.client.BaseUploadStatus;
import gwtupload.client.IUploadStatus;

/**
 *<p>
 * Upload progress using Incubator progress-bar widget.
 * </p>
 *  
 * @author Manolo Carrasco Moñino
 */
public class IncubatorUploadProgress extends BaseUploadStatus {

  TextFormatter formater = new TextFormatter() {
    protected String getText(ProgressBar bar, double curProgress) {
      return fileNameLabel.getText() + "  (" + (int) curProgress + " %)";
    }
  };
  ProgressBar prg = new ProgressBar();

  public IncubatorUploadProgress() {
    setProgressWidget(prg);
    prg.setTextFormatter(formater);
  }

  @Override
  public IUploadStatus newInstance() {
    return new IncubatorUploadProgress();
  }

  @Override
  public void setPercent(int percent) {
    super.setPercent(percent);
    prg.setProgress(percent);
  }

}
