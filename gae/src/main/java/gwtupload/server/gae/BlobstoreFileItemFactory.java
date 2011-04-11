/*
 * Copyright 2010 Manuel Carrasco Moñino. (manuel_carrasco at
 * users.sourceforge.net) http://code.google.com/p/gwtupload
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

import com.google.appengine.api.blobstore.BlobKey;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * @author Manolo Carrasco Moñino
 */
public class BlobstoreFileItemFactory implements FileItemFactory, Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * FileItem class which stores file data in cache.
   * 
   */
  public static class BlobstoreFileItem implements FileItem, Serializable {
    private static final long serialVersionUID = 1L;
    String ctype;
    OutputStream data = null;
    String fname;
    boolean formfield;
    BlobKey key;
    String name;

    int size = 1;

    public BlobstoreFileItem(String fieldName, String contentType,
        boolean isFormField, String fileName) {
      ctype = contentType;
      fname = fieldName;
      name = fileName;
      formfield = isFormField;
      data = new DumbOutputStream();
    }

    public void delete() {
      if (key != null) {
        BlobstoreUploadAction.blobstoreService.delete(key);
      }
    }

    public byte[] get() {
      return getData();
    }

    public String getContentType() {
      return ctype;
    }

    public String getFieldName() {
      return fname;
    }

    public InputStream getInputStream() throws IOException {
      return new DumbInputStream();
    }

    public BlobKey getKey() {
      return key;
    }

    public String getName() {
      return name;
    }

    public OutputStream getOutputStream() throws IOException {
      return data;
    }

    public long getSize() {
      return size;
    }

    public String getString() {
      return getData().toString();
    }

    public String getString(String arg0) throws UnsupportedEncodingException {
      return new String(get(), arg0);
    }

    public boolean isFormField() {
      return formfield;
    }

    public boolean isInMemory() {
      return data != null;
    }

    public void save(OutputStream o) {
    }

    public void setFieldName(String arg0) {
      fname = arg0;
    }

    public void setFormField(boolean arg0) {
      formfield = arg0;
    }

    public void setKey(BlobKey key) {
      this.key = key;
    }

    public void write(File arg0) throws Exception {
      throw new UnsupportedOperationException(this.getClass().getName()
          + " doesn't support write to files");
    }

    private byte[] getData() {
      return new byte[] {};
    }
  }

  /**
   */
  public static class DumbInputStream extends InputStream implements Serializable {
    private static final long serialVersionUID = 1L;
    public int read() throws IOException {
      return -1;
    }
  }

  /**
   */
  public static class DumbOutputStream extends OutputStream implements Serializable {
    private static final long serialVersionUID = 1L;
    public void write(int b) throws IOException {
    }
  }

  public BlobstoreFileItemFactory() {
  }

  public BlobstoreFileItemFactory(int requestSize) {
  }

  public FileItem createItem(String fieldName, String contentType,
      boolean isFormField, String fileName) {
    return new BlobstoreFileItem(fieldName, contentType, isFormField, fileName);
  }

}
