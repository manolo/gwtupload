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
package gwtupload.server;

import java.io.Serializable;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.ProgressListener;

/**
 * 
 * Abstract class for file upload listeners used by apache-commons-fileupload to monitor
 * the progress of uploaded files.
 *  
 * It is useful to implement UploadListeners that can be saved in different
 * ways.
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public abstract class AbstractUploadListener implements ProgressListener, Serializable {

  protected static String className = AbstractUploadListener.class.getName().replaceAll("^.+\\.", "");

  protected static int DEFAULT_SAVE_INTERVAL = 3000;

  protected static UploadLogger logger = UploadLogger.getLogger(AbstractUploadListener.class);

  protected static final long serialVersionUID = -6431275569719042836L;

  public static AbstractUploadListener current(String sessionId) {
    throw new RuntimeException("Implement the static method 'current' in your customized class");
  }

  protected Long bytesRead = 0L, contentLength = 0L;

  protected RuntimeException exception = null;

  protected boolean exceptionTrhown = false;

  protected boolean finished = false;

  protected int frozenTimeout = 60000;

  protected Date saved = new Date();

  protected String sessionId = "";

  protected int slowUploads = 0;

  public AbstractUploadListener(int sleepMilliseconds, long requestSize) {
    this();
    slowUploads = sleepMilliseconds;
    contentLength = requestSize;
    logger.info(className + " " + sessionId + " created new instance. (slow=" + sleepMilliseconds + ", requestSize=" + requestSize + ")");
    HttpServletRequest request = UploadServlet.getThreadLocalRequest();
    if (request != null) {
      sessionId = request.getSession().getId();
    }
    save();
  }

  private AbstractUploadListener() {
    className = this.getClass().getName().replaceAll("^.+\\.", "");
    logger = UploadLogger.getLogger(this.getClass());
  }

  /**
   * Get the bytes transfered so far.
   * 
   * @return bytes
   */
  public long getBytesRead() {
    return bytesRead;
  }

  /**
   * Get the total bytes of the request.
   * 
   * @return bytes
   */
  public long getContentLength() {
    return contentLength;
  }

  /**
   * Get the exception.
   * 
   */
  public RuntimeException getException() {
    return exception;
  }

  /**
   * Return the percent done of the current upload. 
   * 
   * @return percent
   */
  public long getPercent() {
    return contentLength != 0 ? bytesRead * 100 / contentLength : 0;
  }

  /**
   * Return true if the process has been canceled due to an error or 
   * by the user.
   * 
   * @return boolean
   */
  public boolean isCanceled() {
    return exception != null;
  }

  public boolean isFinished() {
    return finished;
  }

  /**
   * Return true if has lasted a long since the last data received. 
   * by the user.
   * 
   * @return boolean
   */
  public boolean isFrozen() {
    return getPercent() > 0 && getPercent() < 100 && (new Date()).getTime() - saved.getTime() > frozenTimeout;
  }

  /**
   * Remove itself from session or cache.
   */
  public abstract void remove();

  /**
   * Save itself in session or cache.
   */
  public abstract void save();

  /**
   * Set the exception which cancels the upload.
   * 
   */
  public void setException(RuntimeException e) {
    exception = e;
    save();
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
    save();
  }

  public String toString() {
    return "total=" + getContentLength() + " done=" + getBytesRead() + " cancelled=" + isCanceled() + " finished=" + isFinished() + " saved=" + saved;
  }

  /**
   * This method is called each time the server receives a block of bytes.
   */
  public void update(long done, long total, int item) {
    if (exceptionTrhown) { return; }

    // To avoid cache overloading, this object is saved when the upload starts, 
    // when it has finished, or when the interval from the last save is significant. 
    boolean save = bytesRead == 0 && done > 0 || done >= total || (new Date()).getTime() - saved.getTime() > DEFAULT_SAVE_INTERVAL;
    bytesRead = done;
    contentLength = total;
    if (save) {
      save();
    }

    // If other request has set an exception, it is thrown so the commons-fileupload's 
    // parser stops and the connection is closed.
    if (isCanceled()) {
      String eName = exception.getClass().getName().replaceAll("^.+\\.", "");
      logger.info(className + " " + sessionId + " The upload has been canceled after " + bytesRead + " bytes received, raising an exception (" + eName + ") to close the socket");
      exceptionTrhown = true;
      throw exception;
    }

    // Just a way to slow down the upload process and see the progress bar in fast networks.
    if (slowUploads > 0 && done < total) {
      try {
        Thread.sleep(slowUploads);
      } catch (Exception e) {
        exception = new RuntimeException(e);
      }
    }
  }
}
