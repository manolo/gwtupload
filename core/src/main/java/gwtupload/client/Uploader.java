/*
 * Copyright 2010 Manuel Carrasco Moñino. (manolo at apache/org)
 * Copyright 2017 Sven Strickroth <email@cs-ware.de>
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
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import gwtupload.client.dnd.DragAndDropFormPanel;
import gwtupload.client.dnd.IDragAndDropFileInput;
import static gwtupload.shared.UConsts.ATTR_BLOBSTORE_PARAM_NAME;
import static gwtupload.shared.UConsts.MULTI_SUFFIX;
import static gwtupload.shared.UConsts.PARAM_BLOBKEY;
import static gwtupload.shared.UConsts.PARAM_BLOBSTORE;
import static gwtupload.shared.UConsts.PARAM_CANCEL;
import static gwtupload.shared.UConsts.PARAM_FILENAME;
import static gwtupload.shared.UConsts.PARAM_NAME;
import static gwtupload.shared.UConsts.PARAM_REMOVE;
import static gwtupload.shared.UConsts.PARAM_SHOW;
import static gwtupload.shared.UConsts.TAG_BLOBSTORE;
import static gwtupload.shared.UConsts.TAG_BLOBSTORE_NAME;
import static gwtupload.shared.UConsts.TAG_BLOBSTORE_PARAM;
import static gwtupload.shared.UConsts.TAG_BLOBSTORE_PATH;
import static gwtupload.shared.UConsts.TAG_CANCELED;
import static gwtupload.shared.UConsts.TAG_CTYPE;
import static gwtupload.shared.UConsts.TAG_CURRENT_BYTES;
import static gwtupload.shared.UConsts.TAG_FILE;
import static gwtupload.shared.UConsts.TAG_FINISHED;
import static gwtupload.shared.UConsts.TAG_KEY;
import static gwtupload.shared.UConsts.TAG_MESSAGE;
import static gwtupload.shared.UConsts.TAG_NAME;
import static gwtupload.shared.UConsts.TAG_PERCENT;
import static gwtupload.shared.UConsts.TAG_SIZE;
import static gwtupload.shared.UConsts.TAG_TOTAL_BYTES;
import static gwtupload.shared.UConsts.TAG_WAIT;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.ISession.Session;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.bundle.UploadCss;

/**
 * <p>
 * Uploader panel.
 * </p>
 *
 * @author Manolo Carrasco Moñino
 *
 *         <h3>Features</h3>
 *         <ul>
 *         <li>Renders a form with an input file for sending the file, and a hidden iframe where is received the server response</li>
 *         <li>The user can add more elements to the form</li>
 *         <li>It asks the server for the upload progress continuously until the submit process has finished.</li>
 *         <li>It expects xml responses instead of gwt-rpc, so the server part can be implemented in any language</li>
 *         <li>It uses a progress interface so it is easy to use customized progress bars</li>
 *         <li>By default it renders a basic progress bar</li>
 *         <li>It can be configured to automatic submit after the user has selected the file</li>
 *         <li>It uses a queue that avoid submit more than a file at the same time</li>
 *         </ul>
 *
 *         <h3>CSS Style Rules</h3>
 *         <ul>
 *         <li>.GWTUpld { Uploader container }</li>
 *         <li>.GWTUpld .upld-input { style for the FileInput element }</li>
 *         <li>.GWTUpld .upld-status { style for the IUploadStatus element }</li>
 *         <li>.GWTUpld .upld-button { style for submit button if present }</li>
 *         </ul>
 */
public class Uploader extends Composite implements IsUpdateable, IUploader, HasJsData {


  static {
    UploadCss.INSTANCE.css().ensureInjected();
  }


  public Widget getWidget(){
    return this;
  }

  /**
   * FormPanel add method only can be called once.
   * This class override the add method to allow multiple additions
   * to a flowPanel.
   */
//  public static class FormFlowPanel extends FormPanel {
  public static class FormFlowPanel extends DragAndDropFormPanel {
    FlowPanel formElements = new FlowPanel();
    public FormFlowPanel() {
      super.add(formElements);
      formElements.setStyleName("upld-form-elements");
    }
    public void add(Widget w) {
      if (w instanceof Hidden) {
        formElements.insert(w, 0);
      } else {
        formElements.add(w);
      }
    }
    public void add(Widget w, int index) {
      formElements.insert(w, Math.max(0, Math.min(index, formElements.getWidgetCount())));
    }
    public void clear() {
      formElements.clear();
    }
  }

  public static final int DEFAULT_FILEINPUT_SIZE = 40;

  public static final UploaderConstants I18N_CONSTANTS = GWT.create(UploaderConstants.class);

  protected static final String STYLE_BUTTON = "upld-button";
  protected static final String STYLE_INPUT = "upld-input";
  protected static final String STYLE_MAIN = "GWTUpld";
  protected static final String STYLE_STATUS = "upld-status";
  static HTML mlog;
  static Logger logger;
  private static final int DEFAULT_AUTOUPLOAD_DELAY = 600;

  private static final int DEFAULT_TIME_MAX_WITHOUT_RESPONSE = 60000;
  private static final int DEFAULT_UPDATE_INTERVAL = 500;

  private static HashSet<String> fileDone = new HashSet<String>();
  private static HashSet<String> fileUploading = new HashSet<String>();
  private static List<String> fileQueue = new ArrayList<String>();

  private static int statusInterval = DEFAULT_UPDATE_INTERVAL;

  private static int uploadTimeout = DEFAULT_TIME_MAX_WITHOUT_RESPONSE;
  public static void log(String msg, Throwable e) {
    if (mlog == null) {
      if (Window.Location.getParameter("log") != null) {
        mlog = new HTML();
        RootPanel.get().add(mlog);
        log(msg, e);
      } else {
        if (logger == null) {
          logger = Logger.getLogger("Gwt client Uploader");
        }
        logger.info(msg);
        GWT.log(msg, e);
      }
    } else {
      String html = SafeHtmlUtils.fromString(msg + "\n" + (e != null ? e.getMessage() :"")).asString().replaceAll("\n", "<br/>");
      mlog.setHTML(mlog.getHTML() + html);
    }
  }

  /**
   * Configure the frequency to send status requests to the server.
   */
  public static void setStatusInterval(int statusInterval) {
    Uploader.statusInterval = statusInterval;
  }

  /**
   * Configure the maximal time without a valid response from the server.
   * When this period is reached, the upload process is canceled.
   */
  public static void setUploadTimeout(int uploadTimeout) {
    Uploader.uploadTimeout = uploadTimeout;
  }

  private static long now() {
    return (new Date()).getTime();
  }
  protected Panel uploaderPanel;
  private final Timer automaticUploadTimer = new Timer() {
    private boolean firstTime = true;
    public void run() {
      if (autoSubmit && isTheFirstInQueue()) {
        this.cancel();
        firstTime = true;
        statusWidget.setStatus(IUploadStatus.Status.SUBMITING);
        statusWidget.setVisible(true);
        // See issue #134
        try {
          uploadForm.submit();
        } catch (Exception e) {
          cancel();
          cancelUpload(i18nStrs.uploaderInvalidPathError());
        }
      } else if (firstTime) {
        addToQueue();
        firstTime = false;
      }
    }
  };

  protected boolean autoSubmit = false;
  private boolean avoidRepeatedFiles = false;
  private boolean avoidEmptyFile = true;
  private List<String> basenames = new ArrayList<String>();
  private boolean blobstore = false;
  private IUploadStatus.UploadCancelHandler cancelHandler = new IUploadStatus.UploadCancelHandler() {
    public void onCancel() {
      cancel();
    }
  };
  private boolean canceled = false;
  private boolean enabled = true;
  private boolean multiple = true;
  private IFileInput fileInput;
  protected String fileInputPrefix = "GWTU";
  private String fileInputName = null;
  private FileInputType fileInputType;
  private boolean finished = false;
  private long lastData = now();
  private final RequestCallback onBlobstoreReceivedCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      String message = removeHtmlTags(exception.getMessage());
      cancelUpload(i18nStrs.uploaderServerUnavailable() + " (1) " + getServletPath() + "\n\n" + message);
    }
    public void onResponseReceived(Request request, Response response) {
      String text = response.getText();
      String url = null;
      Document document = null;

      String bpath = "<" + TAG_BLOBSTORE_PATH + ">";
      String sbpath = "</" + TAG_BLOBSTORE_PATH + ">";
      if (text.contains(bpath)) {
        try {
          document = XMLParser.parse(text);
          url = Utils.getXmlNodeValue(document, TAG_BLOBSTORE_PATH);
        } catch (Exception e) {
          cancelUpload(i18nStrs.uploaderBlobstoreError() + "\n>>>\n" + e.getMessage() + "\n>>>>\n" + e);
          return;
        }
        if (url == null) {
          url = text.replaceAll("[\r\n]+","").replaceAll("^.*" + bpath + "\\s*", "").replaceAll("\\s*" + sbpath + ".*$", "");
        }
      }
      if (url != null && url.length() > 0 && !"null".equalsIgnoreCase(url)) {
        if (session.getServletPath().startsWith("http")) {
          url = session.getServletPath().replaceFirst("(https?://[^/]+).*", "$1") + url;
        }
        uploadForm.setAction(url);
      } else {
        uploadForm.setAction(session.getServletPath());
      }
      removeHiddens();
      if (document != null) {
        String name = Utils.getXmlNodeValue(document, TAG_BLOBSTORE_NAME);
        if (name != null) {
          fileInput.setName(name);
        }
        NodeList list = document.getElementsByTagName(TAG_BLOBSTORE_PARAM);
        for (int i = 0; i < list.getLength(); i++) {
          Node node = list.item(i);
          String value = Utils.getXmlNodeValue(node);
          if (value != null) {
            Node attribute = node.getAttributes().getNamedItem(ATTR_BLOBSTORE_PARAM_NAME);
            if (attribute != null) {
              String paramName = attribute.getNodeValue();
              if (paramName != null) {
                addHidden(paramName, value);
              }
            }
          }
        }
      }
      receivedBlobPath = true;
      uploadForm.submit();
    }
  };

  private final RequestCallback onCancelReceivedCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      log("onCancelReceivedCallback onError: " , exception);
      statusWidget.setStatus(IUploadStatus.Status.CANCELED);
    }
    public void onResponseReceived(Request request, Response response) {
      if (getStatus() == Status.CANCELING) {
        updateStatusTimer.scheduleRepeating(3000);
      }
    }
  };

  private List<IUploader.OnChangeUploaderHandler> onChangeHandlers = new ArrayList<IUploader.OnChangeUploaderHandler>();
  private final RequestCallback onDeleteFileCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      statusWidget.setStatus(Status.DELETED);
      log("onCancelReceivedCallback onError: ", exception);
    }

    public void onResponseReceived(Request request, Response response) {
      statusWidget.setStatus(Status.DELETED);
      fileDone.removeAll(getFileNames());
    }
  };
  private final ChangeHandler onFileInputChanged = new ChangeHandler() {
    public void onChange(ChangeEvent event) {
      basenames.clear();
      for (String s: getFileNames()) {
        basenames.add(Utils.basename(s));
      }
      statusWidget.setFileNames(basenames);
      if (anyFileIsRepeated(false)) {
        statusWidget.setStatus(Status.REPEATED);
        return;
      }
      if (autoSubmit && !validateAll(basenames)) {
        return;
      }
      if (autoSubmit && fileSelected()) {
        automaticUploadTimer.scheduleRepeating(DEFAULT_AUTOUPLOAD_DELAY);
      }
      onChangeInput();
    }
  };

  private List<IUploader.OnFinishUploaderHandler> onFinishHandlers = new ArrayList<IUploader.OnFinishUploaderHandler>();

  private final RequestCallback onSessionReceivedCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      String message = removeHtmlTags(exception.getMessage());
      cancelUpload(i18nStrs.uploaderServerUnavailable() + " (2) " + getServletPath() + "\n\n" + message);
    }
    public void onResponseReceived(Request request, Response response) {
      try {
        String s = Utils.getXmlNodeValue(XMLParser.parse(response.getText()), TAG_BLOBSTORE);
        blobstore = "true".equalsIgnoreCase(s);
        // with blobstore status does not make sense
        if (blobstore) {
          updateStatusTimer.setInterval(5000);
        }
        uploadForm.setAction(session.getServletPath());
        uploadForm.submit();
      } catch (Exception e) {
        String message = e.getMessage().contains("error:")
            ? i18nStrs.uploaderServerUnavailable() + " (3) " + getServletPath() + "\n\n" + i18nStrs.uploaderServerError() + "\nAction: " + getServletPath() + "\nException: " + e.getMessage() + response.getText()
            : i18nStrs.submitError();
        cancelUpload( message);
      }
    }
  };

  private List<IUploader.OnStartUploaderHandler> onStartHandlers = new ArrayList<IUploader.OnStartUploaderHandler>();

  private List<IUploader.OnStatusChangedHandler> onStatusChangeHandlers = new ArrayList<IUploader.OnStatusChangedHandler>();

  /**
   * Handler called when the status request response comes back.
   *
   * In case of success it parses the xml document received and updates the progress widget
   * In case of a non timeout error, it stops the status repeater and notifies the user with the exception.
   */
  private final RequestCallback onStatusReceivedCallback = new RequestCallback() {
    public void onError(Request request, Throwable exception) {
      waitingForResponse = false;
      if (exception instanceof RequestTimeoutException) {
        log("GWTUpload: onStatusReceivedCallback timeout error, asking the server again.", null);
      } else {
        log("GWTUpload: onStatusReceivedCallback error: " + exception.getMessage(), exception);
        updateStatusTimer.cancel();
        String message = removeHtmlTags(exception.getMessage());
        message += "\n" + exception.getClass().getName();
        message += "\n" + exception.toString();
        statusWidget.setError(i18nStrs.uploaderServerUnavailable() + " (4) " + getServletPath() + "\n\n" + message);
      }
    }

    public void onResponseReceived(Request request, Response response) {
      waitingForResponse = false;
      if (finished == true && !uploading) {
        updateStatusTimer.cancel();
        return;
      }
      parseAjaxResponse(response.getText());
    }

  };

  private boolean onSubmitComplete;

  private SubmitCompleteHandler onSubmitCompleteHandler = new SubmitCompleteHandler() {
    public void onSubmitComplete(SubmitCompleteEvent event) {
      if (event.getResults() == null || event.getResults().isEmpty()) {
          // https://github.com/manolo/gwtupload/issues/11
          log("Ignoring empty message in onSubmitComplete", null);
          return;
      }
      updateStatusTimer.cancel();
      onSubmitComplete = true;
      serverRawResponse = event.getResults();
      try {
        // Parse the xml and extract UploadedInfos
        Document doc = XMLParser.parse(serverRawResponse);
        // for some reason the response is put inside a "pre" tag
        if (doc.getDocumentElement().getNodeName().equals("pre")) {
          serverRawResponse = doc.getFirstChild().getFirstChild().getNodeValue();
          doc = XMLParser.parse(serverRawResponse);
        }
        // If the server response is a valid xml
        parseAjaxResponse(serverRawResponse);
      } catch (Exception e) {
        log("onSubmitComplete exception parsing response (Check CORS and XML syntax): ", e);
        // Otherwise force an ajax request so as we have not to wait to the timer schedule
        updateStatusTimer.run();
      }
    }
  };

  /**
   *  Handler called when the file form is submitted
   *
   *  If any validation fails, the upload process is canceled.
   *
   *  If the client hasn't got the session, it asks for a new one
   *  and the submit process is delayed until the client has got it
   */
  private SubmitHandler onSubmitFormHandler = new SubmitHandler() {
    public void onSubmit(SubmitEvent event) {

      if (!finished && uploading) {
        uploading = false;
        statusWidget.setStatus(IUploadStatus.Status.CANCELED);
        return;
      }

      if (!autoSubmit && fileQueue.size() > 0) {
        statusWidget.setError(i18nStrs.uploaderActiveUpload());
        event.cancel();
        return;
      }

      if (anyFileIsRepeated(true)) {
        statusWidget.setStatus(IUploadStatus.Status.REPEATED);
        successful = true;
        event.cancel();
        uploadFinished();
        return;
      }

      if (getFileName().isEmpty() || !validateAll(basenames)) {
        event.cancel();
        return;
      }

      if (session == null) {
        event.cancel();
        // Sends a request to the server in order to get the session
        // When the response with the session comes, it re-submits the form.
        session = Session.createSession(servletPath, onSessionReceivedCallback);
        return;
      }

      if (blobstore && !receivedBlobPath) {
        event.cancel();
        // Sends a request to the server in order to get the blobstore path.
        // When the response with the blobstore path comes, it re-submits the form.
        session.sendRequest("blobstore", onBlobstoreReceivedCallback, PARAM_BLOBSTORE + "=true");
        return;
      }
      receivedBlobPath = false;

      addToQueue();
      uploading = true;
      finished = false;
      serverRawResponse = null;
      serverMessage = new ServerMessage();

      statusWidget.setVisible(true);
      updateStatusTimer.squeduleStart();
      statusWidget.setStatus(IUploadStatus.Status.INPROGRESS);
      lastData = now();
    }
  };

  private boolean receivedBlobPath = false;

  private int requestsCounter = 0;

  private String serverRawResponse = null;
  private ServerMessage serverMessage = new ServerMessage();

  private String servletPath = "servlet.gupld";
  private ISession session = null;

  private IUploadStatus.UploadStatusChangedHandler statusChangedHandler = new IUploadStatus.UploadStatusChangedHandler() {
    public void onStatusChanged(IUploadStatus statusWiget) {
      for (IUploader.OnStatusChangedHandler handler : onStatusChangeHandlers) {
        handler.onStatusChanged(thisInstance);
      }
    }
  };

  private IUploadStatus statusWidget = new BaseUploadStatus();

  protected UploaderConstants i18nStrs = I18N_CONSTANTS;

  private boolean successful = false;

  private Uploader thisInstance;

  private final UpdateTimer updateStatusTimer = new UpdateTimer(this, statusInterval);

  private FormPanel uploadForm;

  private List<Hidden> hiddens = null;

  private boolean uploading = false;

  private List<String> validExtensions;

  private String validExtensionsMsg = "";

  private boolean waitingForResponse = false;

  /**
   * Default constructor.
   * Initialize widget components and layout elements using the
   * standard file input.
   */
  public Uploader() {
    this(FileInputType.BROWSER_INPUT);
  }

  /**
   * Initialize widget components and layout elements using the
   * standard file input.
   *
   * @param automaticUpload
   *    when true the upload starts as soon as the user selects a file
   */
  public Uploader(boolean automaticUpload) {
    this(FileInputType.BROWSER_INPUT, automaticUpload);
  }

  /**
   * Initialize widget components and layout elements.
   *
   * @param type
   *   file input to use
   */
  public Uploader(FileInputType type) {
    this(type, null);
  }

  /**
   * Initialize widget components and layout elements.
   *
   * @param type
   *   file input to use
   * @param automaticUpload
   *   when true the upload starts as soon as the user selects a file
   */
  public Uploader(FileInputType type, boolean automaticUpload) {
    this(type);
    setAutoSubmit(automaticUpload);
  }

  /**
   * Initialize widget components and layout elements.
   *
   * @param type
   *   file input to use
   * @param form
   *   An existing form panel to use
   */
  public Uploader(FileInputType type, FormPanel form) {
    thisInstance = this;
    this.fileInputType = type;

    if (form == null) {
      form = new FormFlowPanel();
    }
    uploadForm = form;
    uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
    uploadForm.setMethod(FormPanel.METHOD_POST);
    uploadForm.addSubmitHandler(onSubmitFormHandler);
    uploadForm.addSubmitCompleteHandler(onSubmitCompleteHandler);
    // Issue #206
    FormElement.as(uploadForm.getElement()).setAcceptCharset("UTF-8");

    uploaderPanel = getUploaderPanel();
    uploaderPanel.add(uploadForm);
    uploaderPanel.setStyleName(STYLE_MAIN);

    setFileInput(fileInputType.getInstance());

    setStatusWidget(statusWidget);

    super.initWidget(uploaderPanel);
  }

  protected Panel getUploaderPanel() {
    return new HorizontalPanel();
  }

  /**
   * Adds a widget to formPanel.
   */
  public void add(Widget w) {
    uploadForm.add(w);
  }

  /**
   * Adds a widget to formPanel in a specified position.
   */
  public void add(Widget w, int index) {
    if (uploadForm instanceof FormFlowPanel) {
      ((FormFlowPanel)uploadForm).add(w, index);
    } else {
      add(w);
    }
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnCancelUploadHandler(gwtupload.client.IUploader.OnCancelUploaderHandler)
   */
  public HandlerRegistration addOnCancelUploadHandler(final OnCancelUploaderHandler handler) {
    assert handler != null;
    return statusWidget.addCancelHandler(new IUploadStatus.UploadCancelHandler() {
      public void onCancel() {
        handler.onCancel(thisInstance);
      }
    });
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnChangeUploadHandler(gwtupload.client.IUploader.OnChangeUploaderHandler)
   */
  public HandlerRegistration addOnChangeUploadHandler(final IUploader.OnChangeUploaderHandler handler) {
    assert handler != null;
    onChangeHandlers.add(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        onChangeHandlers.remove(handler);
      }
    };
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnFinishUploadHandler(gwtupload.client.IUploader.OnFinishUploaderHandler)
   */
  public HandlerRegistration addOnFinishUploadHandler(final IUploader.OnFinishUploaderHandler handler) {
    assert handler != null;
    onFinishHandlers.add(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        onFinishHandlers.remove(handler);
      }
    };
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnStartUploadHandler(gwtupload.client.IUploader.OnStartUploaderHandler)
   */
  public HandlerRegistration addOnStartUploadHandler(final IUploader.OnStartUploaderHandler handler) {
    assert handler != null;
    onStartHandlers.add(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        onStartHandlers.remove(handler);
      }
    };
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#addOnStatusChangedHandler(gwtupload.client.IUploader.OnStatusChangedHandler)
   */
  public HandlerRegistration addOnStatusChangedHandler(final OnStatusChangedHandler handler) {
    assert handler != null;
    onStatusChangeHandlers.add(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        onStatusChangeHandlers.remove(handler);
      }
    };
  }

  /**
   * Don't send files that have already been uploaded.
   * Note that this feature only works when multiple = false;
   */
  public void avoidRepeatFiles(boolean avoidRepeat) {
    if (avoidRepeat) multiple = false;
    this.avoidRepeatedFiles = avoidRepeat;
  }

  /**
   * Don't submit the form if the user has not selected any file.
   *
   * It is useful in forms where the developer whats the user to submit
   * information but the attachment is optional.
   *
   * By default avoidEmptyFile is true.
   */
  public void avoidEmptyFiles(boolean b) {
    this.avoidEmptyFile = b;
  }

  /**
   * Cancel the current upload process.
   */
  public void cancel() {
    if (getStatus() == Status.UNINITIALIZED) {
      return;
    }

    if (finished && !uploading) {
      if (successful) {
        try {
          sendAjaxRequestToDeleteUploadedFile();
        } catch (Exception e) {
        }
      } else {
        statusWidget.setStatus(Status.DELETED);
      }
      return;
    }

    if (canceled || getStatus() == Status.CANCELING) {
      return;
    }

    canceled = true;
    automaticUploadTimer.cancel();
    if (uploading) {
      updateStatusTimer.cancel();
      try {
        sendAjaxRequestToCancelCurrentUpload();
      } catch (Exception e) {
        log("Exception cancelling request " + e.getMessage(), e);
      }
      statusWidget.setStatus(IUploadStatus.Status.CANCELING);
    } else {
      uploadFinished();
      reuse();
    }
  }

  /**
   * Remove all widget from the form.
   */
  public void clear() {
    uploadForm.clear();
    uploadForm.add(fileInput.asWidget());
  }

  /**
   * Returns the link for get the last uploaded files from the server
   * It's useful to display uploaded images or generate links to uploaded files.
   */
  public String fileUrl() {
    return serverMessage.getUploadedInfos().get(0).getFileUrl();
  }

  /**
   * Returns a JavaScriptObject object with info of the uploaded files.
   * It's useful in the exported version of the library.
   */
  public JavaScriptObject getData() {
    if (multiple) {
      JsArray<JavaScriptObject> ret = JavaScriptObject.createArray().cast();
      for (UploadedInfo info: serverMessage.getUploadedInfos()) {
        ret.push(getDataInfo(info));
      }
      return ret;
    } else {
      return getDataInfo(getServerInfo());
    }
  }

  public IFileInput getFileInput() {
    return fileInput;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getFileName()
   */
  public String getFileName() {
    return fileInput.getFilename();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getFileNames()
   */
  public List<String> getFileNames() {
    return fileInput.getFilenames();
  }

  public FormPanel getForm() {
    return uploadForm;
  }

  public UploaderConstants getI18NConstants(){
    return i18nStrs;
  }

  public String getInputName() {
    return fileInput.getName().replace(MULTI_SUFFIX,"");
  }

  @Deprecated
  public String getServerResponse() {
    return getServerRawResponse();
  }

  public String getServerRawResponse() {
    return serverRawResponse;
  }

  public UploadedInfo getServerInfo() {
    return serverMessage.getUploadedInfos().size() > 0 ? serverMessage.getUploadedInfos().get(0) : null;
  }

  /**
   * return the configured server service in the form-panel.
   */
  public String getServletPath() {
    return servletPath;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#getStatus()
   */
  public Status getStatus() {
    return statusWidget.getStatus();
  }

  /**
   * Get the status progress used.
   */
  public IUploadStatus getStatusWidget() {
    return statusWidget;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  public boolean isFinished() {
    return finished;
  }

  /**
   * Returns a iterator of the widgets contained in the form panel.
   */
  public Iterator<Widget> iterator() {
    return uploadForm.iterator();
  }

  /**
   * remove a widget from the form panel.
   */
  public boolean remove(Widget w) {
    return uploadForm.remove(w);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#reset()
   */
  public void reset() {
    reuse();
    fileDone = new HashSet<String>();
    fileUploading = new HashSet<String>();
  }

  /**
   * Prepare the uploader for a new upload.
   */
  public void reuse() {
    this.uploadForm.reset();
    updateStatusTimer.cancel();
    onSubmitComplete = uploading = canceled = finished = successful = false;
    statusWidget.setStatus(Status.UNINITIALIZED);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setAutoSubmit(boolean)
   */
  public void setAutoSubmit(boolean b) {
    autoSubmit = b;
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setEnabled(boolean)
   */
  public void setEnabled(boolean b) {
    enabled = b;
    if (fileInput != null) {
      fileInput.setEnabled(b);
    }
  }

  public void setFileInput(IFileInput input) {
    if (fileInput != null) {
      fileInput.asWidget().removeFromParent();
    }
    fileInput = input;
    fileInput.addChangeHandler(onFileInputChanged);
    fileInput.setText(fileInput instanceof IDragAndDropFileInput ?
        i18nStrs.uploaderDrop() : i18nStrs.uploaderBrowse());
    fileInput.setEnabled(enabled);
    setFileInputSize(DEFAULT_FILEINPUT_SIZE);
    assignNewNameToFileInput();
    uploadForm.add(fileInput.asWidget());
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setFileInputPrefix(java.lang.String)
   */
  public void setFileInputPrefix(String prefix) {
    fileInputPrefix = prefix;
    assignNewNameToFileInput();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setFileInputSize(int)
   */
  public void setFileInputSize(int length) {
    fileInput.setLength(length);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setI18Constants(gwtupload.client.I18nUploadConstants)
   */
  public void setI18Constants(UploaderConstants strs) {
    this.i18nStrs = strs;
    fileInput.setText(fileInput instanceof IDragAndDropFileInput ?
        i18nStrs.uploaderDrop() : i18nStrs.uploaderBrowse());
    statusWidget.setI18Constants(strs);
  }

  /**
   * set the url of the server service that receives the files and informs
   * about the progress.
   */
  public void setServletPath(String path) {
    if (path != null) {
      session = null;
      servletPath = path;
    }
  }

  /**
   * set the status widget used to display the upload progress.
   */
  public void setStatusWidget(IUploadStatus stat) {
    if (stat == null) {
      return;
    }
    uploaderPanel.remove(statusWidget.asWidget());
    statusWidget = stat;
    if (!stat.asWidget().isAttached()) {
      uploaderPanel.add(statusWidget.asWidget());
    }
    statusWidget.asWidget().addStyleName(STYLE_STATUS);
    statusWidget.setVisible(false);
    statusWidget.addCancelHandler(cancelHandler);
    statusWidget.setStatusChangedHandler(statusChangedHandler);
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#setValidExtensions(java.lang.String[])
   */
  public void setValidExtensions(String... extensions) {
    this.validExtensions = new ArrayList<String>();
    validExtensionsMsg = "";
    if (extensions == null || extensions.length == 0) {
      return;
    }
    List<String> v = new ArrayList<String>();
    String accept = "";
    for (String ext : extensions) {
      if (ext == null) {
        continue;
      }
      if (ext.contains("/")) {
        accept += (accept.isEmpty() ? "" : ",") + ext;
        continue;
      }
      if (!ext.startsWith(".")) ext = "." + ext;
      accept += (accept.isEmpty() ? "" : ",") + ext;

      validExtensionsMsg += (validExtensionsMsg.isEmpty() ? "" : ",") + ext;
      ext = ext.replaceAll("\\.", "\\\\.");
      ext = ".+" + ext;
      validExtensions.add(ext);
    }
    fileInput.setAccept(accept);
  }

  public void setValidExtensions(String ext) {
    setValidExtensions(ext.split("[, ]+"));
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUploader#submit()
   */
  public void submit() {
    this.uploadForm.submit();
  }

  /* (non-Javadoc)
   * @see gwtupload.client.IUpdateable#update()
   */
  public void update() {
    if (waitingForResponse) {
      return;
    }
    waitingForResponse = true;
    session.sendRequest("get_status", onStatusReceivedCallback, "filename=" + fileInput.getName().replace(MULTI_SUFFIX, "") , "c=" + requestsCounter++);
  }

  /**
   * Method called when the file input has changed. This happens when the
   * user selects a file.
   *
   * Override this method if you want to add a customized behavior,
   * but remember to call this in your function
   */
  protected void onChangeInput() {
    statusWidget.setStatus(Status.CHANGED);
    for (IUploader.OnChangeUploaderHandler handler : onChangeHandlers) {
      handler.onChange(this);
    }
  }

  /**
   * Method called when the file upload process has finished,
   * or the file has been canceled or removed from the queue.
   * Override this method if you want to add a customized behavior,
   * but remember to call this in your function.
   */
  protected void onFinishUpload() {
    for (IUploader.OnFinishUploaderHandler handler : onFinishHandlers) {
      handler.onFinish(this);
    }
  }

  /**
   * Method called when the file is added to the upload queue.
   * Override this if you want to add a customized behavior,
   * but remember to call this from your method.
   */
  protected void onStartUpload() {
    for (IUploader.OnStartUploaderHandler handler : onStartHandlers) {
      handler.onStart(this);
    }
  }

  /**
   * Adds a file to the upload queue.
   */
  private void addToQueue() {
    statusWidget.setStatus(IUploadStatus.Status.QUEUED);
    statusWidget.setProgress(0, 0);
    if (!fileQueue.contains(getInputName())) {
      onStartUpload();
      fileQueue.add(getInputName());
      if (!multiple && avoidRepeatedFiles) {
        fileUploading.add(getFileName());
      }
    }
  }

  /**
   * Change the fileInput name, because the server uses it as an uniq identifier.
   */
  protected void assignNewNameToFileInput() {
    String fileInputName = (fileInputPrefix + "-" + Math.random()).replaceAll("\\.", "");
    if (multiple) {
      fileInputName += MULTI_SUFFIX;
    }
    fileInput.setName(fileInputName);
  }

  /**
   * Cancel upload process and show an error message to the user.
   */
  private void cancelUpload(String msg) {
    successful = false;
    uploadFinished();
    if (fileInput instanceof IDragAndDropFileInput) {
      ((IDragAndDropFileInput)fileInput).reset();
    }
    statusWidget.setStatus(IUploadStatus.Status.ERROR);
    statusWidget.setError(msg);
  }

  private JavaScriptObject getDataInfo(UploadedInfo info) {
    return info == null ? JavaScriptObject.createObject() :
       getDataImpl(info.fileUrl, info.field, info.name, Utils.basename(info.name), serverRawResponse, info.message, getStatus().toString(), info.size);
  }

  private native JavaScriptObject getDataImpl(String url, String inputName, String fileName, String baseName, String serverResponse, String serverMessage, String status, int size) /*-{
    return {
       url: url,
       name: inputName,
       filename: fileName,
       basename: baseName,
       response: serverResponse,
       message: serverMessage,
       status:  status,
       size: size
    };
  }-*/;

  private boolean isTheFirstInQueue() {
    return fileQueue.size() > 0 && fileQueue.get(0).equals(getInputName());
  }

  private void parseAjaxResponse(String responseTxt) {
    if (responseTxt == null) {
      return;
    }

    String error = null;
    Document doc = null;
    try {
      doc = XMLParser.parse(responseTxt);
      error = Utils.getXmlNodeValue(doc, "error");
      if (error == null) {
        // Response brings uploaded files info in either:
        // POST response or FINISHED status
        String msg = Utils.getXmlNodeValue(doc, TAG_MESSAGE);
        serverMessage.setMessage(msg);
        NodeList list = doc.getElementsByTagName(TAG_FILE);
        for (int i = 0, l = list.getLength(); i < l; i++) {
          UploadedInfo info = new UploadedInfo();
          info.setField(getInputName() + "-" + i);
          info.setName(Utils.getXmlNodeValue(doc, TAG_NAME, i));
          info.setCtype(Utils.getXmlNodeValue(doc, TAG_CTYPE, i));
          // TODO: test
          info.setKey (Utils.getXmlNodeValue(doc, TAG_KEY, i));
          // TODO: remove
          info.message = msg;
          String url = session.composeURL(PARAM_SHOW + "=" + info.getField());
          if (info.getKey() != null) {
            url += "&" + PARAM_BLOBKEY + "=" + info.getKey();
          }
          info.setFileUrl(url);

          String size = Utils.getXmlNodeValue(doc, TAG_SIZE, i);
          if (size != null) {
            info.setSize(Integer.parseInt(size));
          }
          serverMessage.getUploadedInfos().add(info);
        }
      }
    } catch (Exception e) {
      if (responseTxt.toLowerCase().matches("error")) {
        error = i18nStrs.uploaderServerError() + "\nAction: " + getServletPath() + "\nException: " + e.getMessage() + responseTxt;
      }
    }

    if (error != null) {
      successful = false;
      cancelUpload(error);
      return;
    } else if (Utils.getXmlNodeValue(doc, TAG_WAIT) != null) {
      if (serverRawResponse != null) {
        log("server response received, cancelling the upload " + getFileNames() + " " + serverRawResponse, null);
        successful = true;
        uploadFinished();
      }
    } else if (Utils.getXmlNodeValue(doc, TAG_CANCELED) != null) {
      log("server response is: canceled " + getFileNames(), null);
      successful = false;
      canceled = true;
      uploadFinished();
      return;
    } else if (Utils.getXmlNodeValue(doc, TAG_FINISHED) != null) {
      log("server response is: finished " + serverMessage.getUploadedFileNames(), null);
      successful = true;
      if (onSubmitComplete) {
        log("POST response from server has been received", null);
        uploadFinished();
      }
      return;
    } else if (Utils.getXmlNodeValue(doc, TAG_PERCENT) != null) {
      lastData = now();
      long transferredKB = Long.valueOf(Utils.getXmlNodeValue(doc, TAG_CURRENT_BYTES)) / 1024;
      long totalKB = Long.valueOf(Utils.getXmlNodeValue(doc, TAG_TOTAL_BYTES)) / 1024;
      statusWidget.setProgress(transferredKB, totalKB);
      log("server response transferred  " + transferredKB + "/" + totalKB + " " + getFileNames(), null);
      if (onSubmitComplete) {
        successful = false;
        String msg = i18nStrs.uploaderBadServerResponse() + "\n" + serverRawResponse;
        if (blobstore) {
          msg += "\n" + i18nStrs.uploaderBlobstoreBilling();
        } else {
          msg += "\n" + i18nStrs.uploaderBadParsing();
        }
        msg += "\n\n" + responseTxt;
        log(msg, null);
        statusWidget.setError(msg);
        uploadFinished();
      }
      return;
    } else {
      log("incorrect response: " + getFileNames() + " " + responseTxt, null);
    }

    if (uploadTimeout > 0 && now() - lastData >  uploadTimeout) {
      successful = false;
      cancelUpload(i18nStrs.uploaderTimeout());
      try {
        sendAjaxRequestToCancelCurrentUpload();
      } catch (Exception e) {
      }
    }
  }

  /**
   * remove a file from the upload queue.
   */
  private void removeFromQueue() {
    fileQueue.remove(getInputName());
    fileUploading.remove(getFileName());
  }

  private String removeHtmlTags(String message) {
    return message.replaceAll("<[^>]+>", "");
  }

  private void sendAjaxRequestToCancelCurrentUpload() {
    session.sendRequest("cancel_upload", onCancelReceivedCallback, PARAM_CANCEL + "=true");
  }

  private void sendAjaxRequestToDeleteUploadedFile() {
    for (String field: serverMessage.getUploadedFieldNames()) {
      session.sendRequest("remove_file", onDeleteFileCallback, PARAM_REMOVE + "=" + field);
    }
  }

  /**
   * Sends a request to the server in order to get the blobstore path.
   * When the response with the session comes, it submits the form.
   */
  private void sendAjaxRequestToGetBlobstorePath() throws RequestException {
    session.sendRequest("blobstore", onBlobstoreReceivedCallback, PARAM_BLOBSTORE + "=true&" + PARAM_NAME + "=" + getInputName() + "&" + PARAM_FILENAME + "=" + fileInput.getFilename());
  }

  /**
   * Called when the uploader detects that the upload process has finished:
   * - in the case of submit complete.
   * - in the case of error talking with the server.
   */
  private void uploadFinished() {
    removeFromQueue();
    finished = true;
    uploading = false;
    updateStatusTimer.cancel();
    statusWidget.setVisible(false);

    if (successful) {
      if (avoidRepeatedFiles) {
        fileDone.addAll(fileInput.getFilenames());
        statusWidget.setStatus(IUploadStatus.Status.SUCCESS);
      } else {
        statusWidget.setStatus(IUploadStatus.Status.SUCCESS);
      }
    } else if (canceled) {
      statusWidget.setStatus(IUploadStatus.Status.CANCELED);
    } else {
      statusWidget.setStatus(IUploadStatus.Status.ERROR);
    }
    onFinishUpload();
    //reatachIframe(uploadForm.getElement().getAttribute("target"));
  }

  // Fix for issue http://stackoverflow.com/questions/27711821
  private native static void reatachIframe(String name) /*-{
    if ($doc.querySelector) {
      var i = $doc.querySelector('iframe[name="' + name + '"]');
      if (i) {
        var o = i.onload;
        i.onload = undefined;
        var p = i.parentElement;
        p.removeChild(i);
        p.appendChild(i);
        i.onload = o;
      }
    }
  }-*/;

  private boolean fileSelected() {
    for (String s: basenames) {
      if (s.length() > 0) {
        return true;
      }
    }
    return false;
  }

  private boolean validateAll(Collection<String> coll) {
    for (String s: coll) {
      if (!validateExtension(s)) {
        return false;
      }
    }
    return true;
  }

  private boolean validateExtension(String filename) {
    if (filename == null || filename.length() == 0) {
      return !avoidEmptyFile;
    }
    boolean valid = Utils.validateExtension(validExtensions, filename);
    if (!valid) {
      finished = true;
      statusWidget.setError(i18nStrs.uploaderInvalidExtension() + validExtensionsMsg);
      statusWidget.setStatus(Status.INVALID);
    }
    return valid;
  }

  private void removeHiddens() {
    if (hiddens != null) {
      for (Hidden hidden : hiddens) {
        hidden.removeFromParent();
      }
      hiddens = null;
    }
  }

  private Hidden addHidden(String name, String value) {
    Hidden hidden = new Hidden(name, value);
    uploadForm.add(hidden);
    if (hiddens == null) {
      hiddens = new ArrayList<Hidden>();
    }
    hiddens.add(hidden);
    return hidden;
  }

  public List<String> getFileInputNames() {
    return fileInput.getFilenames();
  }

  public boolean anyFileIsRepeated(boolean checkOnlyUploadedFiles) {
    if (!multiple && avoidRepeatedFiles) {
      for (String s: fileInput.getFilenames()) {
        if (fileDone.contains(s) || (!checkOnlyUploadedFiles && fileUploading.contains(s)))
          return true;
      }
    }
    return false;
  }

  public void setMultipleSelection(boolean b) {
    multiple = b;
    fileInput.enableMultiple(b);
  }


  public void setServerMessage(ServerMessage msg) {
    serverMessage = msg;
    successful = true;
    statusWidget.setFileNames(msg.getUploadedFileNames());
    uploadFinished();
  }

  public ServerMessage getServerMessage() {
    return serverMessage;
  }
}
