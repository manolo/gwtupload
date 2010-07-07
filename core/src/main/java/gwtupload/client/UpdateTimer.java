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

import com.google.gwt.user.client.Timer;

/**
 * <p>
 * A timer that notifies periodically to IUpdateable classes.
 * </p>
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class UpdateTimer extends Timer {

  private Timer delayedStarter = new Timer() {
    public void run() {
      thisInstance.start();
    }
  };

  private int interval = 1500;

  private boolean isRunning = true;

  private UpdateTimer thisInstance;

  private IsUpdateable updateable;

  public UpdateTimer(IsUpdateable updateable, int periodMillis) {
    this.updateable = updateable;
    this.interval = periodMillis;
    thisInstance = this;
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.Timer#cancel()
   */
  @Override
  public void cancel() {
    isRunning = false;
    super.cancel();
  }

  /**
   * stop the timer
   */
  public void finish() {
    cancel();
  }

  /**
   * @return 
   *     interval
   */
  public int getInterval() {
    return interval;
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.Timer#run()
   */
  public void run() {
    updateable.update();
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.Timer#scheduleRepeating(int)
   */
  @Override
  public void scheduleRepeating(int periodMillis) {
    isRunning = true;
    super.scheduleRepeating(periodMillis);
  }

  /**
   * @param periodMillis
   */
  public void setInterval(int periodMillis) {
    if (this.interval != periodMillis) {
      this.interval = periodMillis;
      if (isRunning) {
        finish();
        start();
      }
    }
  }

  /**
   * Schedules the timer's start to elapse in the future.
   * The time to wait is the default configured period.
   */
  public void squeduleStart() {
    squeduleStart(interval);
  }

  /**
   * Schedules the timer's start to elapse in the future.
   * 
   * @param delayMillis time in milliseconds to wait before start the timer
   */
  public void squeduleStart(int delayMillis) {
    delayedStarter.schedule(delayMillis);
  }

  /**
   * start the timer
   */
  public void start() {
    scheduleRepeating(interval);
  }
}
