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

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;


/**
 * These are the constants used in the package.
 */
@Export
@ExportPackage("jsu")
public class Const implements Exportable {
    
    protected static final String DIALOG = "dialog"; // [true] the widget is shown in a popup dialog
    protected static final String CONT_ID = "containerId"; // [null] Id of the element where the widget will be inserted
    protected static final String RND_BOX = "roundedBox"; // [false] show the element inside a decorated rounded container
    protected static final String BUTTONS = "buttons"; // ["rounded"] Buttons style, available options are: rounded, flat, standard
    protected static final String CLASS_NAME = "className"; // [""] Add an optional classname to the container
    
    protected static final String TOTAL_MSG = "totalMsg"; // ["{0}% {1}/{2} "] Set the message to show when the process has finished
    protected static final String ELEMENTS = "elements"; // [20] number of bars to show in the progress bar
    protected static final String NUMBERS = "numbers"; // [true] show numeric information of the progress
    protected static final String TIME_REMAINING = "timeRemaining"; // [false] show time remaining
    
    protected static final String REGIONAL = "regional";  // [null] hash with the set of key/values to internationalize the widget
    protected static final String VALID_EXTENSIONS = "validExtensions"; // [null] List of valid extensions, the extensions has to be separated by comma or spaces
    protected static final String ACTION = "action"; // ["servlet.gupld"] Servlet path, it has to be in the same domain, because cross-domain is not supported
    protected static final String ON_FINISH = "onFinish"; // [null] Javascript method called when the upload process finishes
    protected static final String ON_START = "onStart"; // [null] Javascript method called when the upload process starts
    protected static final String ON_CHANGE = "onChange"; // [null] Javascript method called when the user selects a file
    protected static final String ON_CANCEL = "onCancel"; // [null] Javascript method called when the upload file is canceled, removed from the queue or from the server
    protected static final String ON_STATUS = "onStatus"; // [null] Javascript method called when the upload file's status changes 
    protected static final String MULTIPLE = "multiple"; // [false] specify whether the uploader has a multiple behavior
    protected static final String EMPTY = "empty"; // [false] specify whether single uploader can submit the form is the file input is empty
    protected static final String TYPE = "type"; // ["chismes"] Type of progress bar, valid options are "basic", "chismes" or "incubator"
    protected static final String ON_LOAD = "onLoad"; // [null] Javascript method called after the browser has loaded the image
    protected static final String URL = "url"; // [null] web address for the image
    protected static final String MAX_FILES = "maxFiles"; // [0] Only used if multiple=true. The maximum number of files which the user can send to the server. 0 means unlimited. Only successful uploads are counted.
    protected static final String CHOOSE_TYPE = "chooser"; // ["browser"] Choose file button type, options are: "browser", "button", "label", "anchor".
    
    protected static final String TXT_ACTIVE_UPLOAD = "uploaderActiveUpload"; // (Upload)["There is already an active upload, try later."]  
    protected static final String TXT_ALREADY_DONE = "uploaderAlreadyDone"; // (Upload)["This file was already uploaded."]  
    protected static final String TXT_INVALID_EXTENSION = "uploaderInvalidExtension"; // (Upload)["Invalid file.\nOnly these types are allowed:\n"] 
    protected static final String TXT_TIMEOUT = "uploaderTimeout"; // (Upload)["Timeout sending the file:\n perhups your browser does not send files correctly,\n your session has expired,\n or there was a server error.\nPlease try again."] 
    protected static final String TXT_SERVER_ERROR = "uploaderServerError"; // (Upload)["Invalid server response. Have you configured correctly your application in the server side?"] 
    protected static final String TXT_SERVER_UNAVAILABLE = "uploaderServerUnavailable"; // (Upload)["Unable to contact with the server: "] 
    protected static final String TXT_SEND = "uploaderSend"; // (Upload)["Send"] 
    protected static final String TXT_CANCEL = "uploadLabelCancel"; // (Upload)[null] 
    protected static final String TXT_CANCELING = "uploadStatusCanceling"; // (Upload)["Canceling"] 
    protected static final String TXT_CANCELED = "uploadStatusCanceled"; // (Upload)["Canceled"] 
    protected static final String TXT_ERROR = "uploadStatusError"; // (Upload)["Error"] 
    protected static final String TXT_INPROGRESS = "uploadStatusInProgress"; // (Upload)["Sending..."] 
    protected static final String TXT_QUEUED = "uploadStatusQueued"; // (Upload)["Queued"] 
    protected static final String TXT_SUBMITING = "uploadStatusSubmitting"; // (Upload)["Submiting form..."] 
    protected static final String TXT_SUCCESS = "uploadStatusSuccess"; // (Upload)["Done"]
    protected static final String TXT_DELETED = "uploadStatusDeleted"; // (Upload)["Deleted"]
    protected static final String TXT_BROWSE = "uploadBrowse"; // (Upload)["Select a file ..."]
    protected static final String TXT_SUBMIT_ERROR = "submitError"; // (Upload)[Unable to auto submit the form, it seems your browser has security issues with this feature.\n Developer Info: If you are using jsupload and you do not need cross-domain, try a version compiled with the standard linker?]
    
    protected static final String TXT_PERCENT = "progressPercentMsg"; // (Upload)["{0}%"] Set the message used to format the progress in percent units. 
    protected static final String TXT_SECONDS = "progressSecondsMsg"; // (Upload)["Time remaining: {0} Seconds"] Set the message used to format the time remaining text below the progress bar in seconds.
    protected static final String TXT_MINUTES = "progressMinutesMsg"; // (Upload)["Time remaining: {0} Minutes"] Set the message used to format the time remaining text below the progress bar in minutes
    protected static final String TXT_HOURS = "progressHoursMsg"; // (Upload)["Time remaining: {0} Hours"] Set the message used to format the time remaining text below the progress bar in hours
    
}
