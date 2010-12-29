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

import gwtupload.server.exceptions.UploadTimeoutException;

import java.io.Serializable;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * This is a File Upload Listener that is used by Apache Commons File Upload to
 * monitor the progress of the uploaded file.
 * 
 * This object and its attributes have to be serializable because
 * Google App-Engine uses dataStore and memCache to store session objects.
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class UploadListener extends AbstractUploadListener {

  /**
   * A class which is executed in a new thread, so its able to detect
   * when an upload process is frozen and sets an exception in order to
   * be canceled.
   * This doesn't work in Google application engine
   */
  public class TimeoutWatchDog extends Thread implements Serializable {
    private static final long serialVersionUID = -649803529271569237L;

    AbstractUploadListener listener;
    private long lastBytesRead = 0L;
    private long lastData = (new Date()).getTime();

    public TimeoutWatchDog(AbstractUploadListener l) {
      listener = l;
    }

    public void cancel() {
      listener = null;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(WATCHER_INTERVAL);
      } catch (InterruptedException e) {
        logger.error(className + " " + sessionId + " TimeoutWatchDog: sleep Exception: " + e.getMessage());
      }
      if (listener != null) {
        if (listener.getBytesRead() > 0 && listener.getPercent() >= 100 || listener.isCanceled()) {
          logger.debug(className + " " + sessionId + " TimeoutWatchDog: upload process has finished, stoping watcher");
          listener = null;
        } else {
          if (isFrozen()) {
            logger.info(className + " " + sessionId + " TimeoutWatchDog: the recepcion seems frozen: " + listener.getBytesRead() + "/" + listener.getContentLength() + " bytes ("
                  + listener.getPercent() + "%) ");
            exception = new UploadTimeoutException("No new data received after " + noDataTimeout / 1000 + " seconds");
          } else {
            run();
          }
        }
      }
    }

    private boolean isFrozen() {
      long now = (new Date()).getTime();
      if (bytesRead > lastBytesRead) {
        lastData = now;
        lastBytesRead = bytesRead;
      } else if (now - lastData > noDataTimeout) { return true; }
      return false;
    }
  }

  protected static final String ATTR_LISTENER = "LISTENER";

  private static int noDataTimeout = 20000;

  private static final long serialVersionUID = -6431275569719042836L;

  private static final int WATCHER_INTERVAL = 5000;
  
  public static void setNoDataTimeout(int i) {
    noDataTimeout = i;
  }

  public static AbstractUploadListener current(HttpServletRequest request) {
    return (AbstractUploadListener) request.getSession().getAttribute(ATTR_LISTENER);
  }

  public static AbstractUploadListener current(String sessionId) {
    return (AbstractUploadListener) session().getAttribute(ATTR_LISTENER);
  }

  /**
   *  Upload servlet saves the current request as a ThreadLocal,
   *  so it is accessible from any class.
   *  
   *  @return request of the current thread
   */
  private static HttpServletRequest request() {
    return UploadServlet.getThreadLocalRequest();
  }

  /**
   * @return current HttpSession
   */
  private static HttpSession session() {
    return request() != null ? request().getSession() : null;
  }

  private TimeoutWatchDog watcher = null;

  /**
   * Default constructor.
   * 
   */
  public UploadListener(int sleepMilliseconds, int requestSize) {
    super(sleepMilliseconds, requestSize);
    startWatcher();
  }

  /* (non-Javadoc)
   * @see gwtupload.server.AbstractUploadListener#remove()
   */
  public void remove() {
    logger.info(className + " " + sessionId + " remove: " + toString());
    if (session() != null) {
      session().removeAttribute(ATTR_LISTENER);
    }
    saved = new Date();
  }

  /* (non-Javadoc)
   * @see gwtupload.server.AbstractUploadListener#save()
   */
  public void save() {
    if (session() != null) {
      session().setAttribute(ATTR_LISTENER, this);
    }
    saved = new Date();
    logger.debug(className + " " + sessionId + " save " + toString());
  }

  /* (non-Javadoc)
   * @see gwtupload.server.AbstractUploadListener#update(long, long, int)
   */
  @Override
  public void update(long done, long total, int item) {
    super.update(done, total, item);
    if (getPercent() >= 100) {
      stopWatcher();
    }
  }

  private void startWatcher() {
    if (watcher == null) {
      try {
        watcher = new TimeoutWatchDog(this);
        watcher.start();
      } catch (Exception e) {
        logger.info(className + " " + sessionId + " unable to create watchdog: " + e.getMessage());
      }
    }
  }

  private void stopWatcher() {
    if (watcher != null) {
      watcher.cancel();
    }
  }
}
