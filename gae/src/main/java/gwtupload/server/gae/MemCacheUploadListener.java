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
package gwtupload.server.gae;

import gwtupload.server.AbstractUploadListener;

import java.util.Date;
import java.util.HashMap;

import javax.cache.Cache;
import javax.cache.CacheManager;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

/**
 * This is a File Upload Listener that can be used by Apache Commons File Upload to
 * monitor the progress of the uploaded file.
 * 
 * This listener is thought to be used in App-Engine because in this platform session's objects 
 * are not updated until the request has finished. 
 * 
 * This Listener saves itself in google's appengine-memcache.
 * 
 * Notes:
 * Although objects must be saved instantly in cache, sometimes the insertion of a new object
 * is done fast, but the modification of it is delayed. So in this implementation, we 
 * store a new object for each save action.
 * 
 * The cache instance is configured to store objects for a very short time. 
 * 
 * @author Manolo Carrasco Moñino
 * 
 */
public class MemCacheUploadListener extends AbstractUploadListener {

  private static final long serialVersionUID = -6431275569719042836L;

  public static final String KEY_LISTENER = "LISTENER-";
  
  private static Cache cache = null;

  public static MemCacheUploadListener current(String sessionId) {
    MemCacheUploadListener listener = null;
    for (int i=0; i<11; i++) { 
      Object o = getCacheInstance().get(KEY_LISTENER + sessionId + i);
      if (o != null) 
        listener = (MemCacheUploadListener) o;
    }
    logger.info(className + " " + sessionId + " get " +  listener);
    return listener;
  }
  
  @SuppressWarnings({ "serial", "unchecked" })
  public static Cache getCacheInstance() {
    if (cache == null) {
      try {
        cache = CacheManager.getInstance().getCacheFactory().createCache(new HashMap(){{
          put(GCacheFactory.EXPIRATION_DELTA, 60); 
        }});
      } catch (Exception e) {
        logger.error(className + " Unable to create Cache instance: " + e.getMessage());
        throw new RuntimeException(e);
      }
    }
    return cache;
  }

  private int counter = 0;
  
  public MemCacheUploadListener(int sleepMilliseconds, long requestSize) {
    super(sleepMilliseconds, requestSize);
  }

  /* (non-Javadoc)
   * @see gwtupload.server.AbstractUploadListener#remove()
   */
  public void remove() {
    for (int i=0; i<11; i++) 
      getCacheInstance().remove(KEY_LISTENER + sessionId + i);
    counter = 0;
    logger.info(className + " " + sessionId + " Remove " + this.toString());
  }

  @SuppressWarnings("unchecked")
  public void save() {
    int decena = (int)getPercent() / 10;
    if (decena >= counter) {
      counter = decena;

      MemCacheUploadListener listener = current(sessionId);
      if (listener != null && listener.isCanceled())
        exception = listener.getException();

      getCacheInstance().put(KEY_LISTENER + sessionId + counter, this);
      
      saved = new Date();
      logger.info(className + " " + sessionId + " Saved " + this.toString());
      counter ++;
    }
  }
  
  public String toString() {
    return "counter=" + counter + " " + super.toString();
  }
  
}
