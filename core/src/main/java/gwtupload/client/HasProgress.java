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

/**
 * <p>
 * Interface for Classes having a method to update progress 
 * </p>
 * 
 *  @author Manolo Carrasco Moñino
 *
 */
public interface HasProgress {


  /**
   * Called whenever the uploader gets new progress information from server
   * @param done bytes uploaded
   * @param total size of the request
   */
  void setProgress(int done, int total);

}
