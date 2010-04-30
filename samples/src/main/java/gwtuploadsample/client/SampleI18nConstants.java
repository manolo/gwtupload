/*
 * Copyright 2010 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
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

import gwtupload.client.IUploader.UploaderConstants;

/**
 * Constants for examples.
 */
public interface SampleI18nConstants extends UploaderConstants {

  @DefaultStringValue("Close")
  String close();

  @DefaultStringValue("Select a file and add it to the upload queue, automatically the upload process will start, and a new input will be added to the panel.")
  String multiUploadBoxText();

  @DefaultStringValue("Multiple uploaders")
  String multiUploadTabText();

  @DefaultStringValue("Select a file to upload, push the send button, and a modal dialog, showing the progress, will appear.")
  String simpleUploadBoxText();

  @DefaultStringValue("Single uploader")
  String singleUploadTabText();
  
  @DefaultStringValue("Image thumbnails: Uploaded images will be shown here. Click on the images to view them in a popup window")
  String thumbNailsBoxText();
}
