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
package gwtupload.server.gae;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;

/**
 * Upload factory based in the GAE File API.
 * 
 * @author Vyacheslav Sokolov
 * @author Manuel Carrasco
 */
public class FilesApiFileItemFactory implements FileItemFactory, Serializable {

  private static final long serialVersionUID = 3683112300714613746L;

  public static class FilesAPIOutputStream extends OutputStream {
    private FileWriteChannel channel;
    private OutputStream stream;

    public FilesAPIOutputStream(FileService service, AppEngineFile file)
        throws IOException {
      channel = service.openWriteChannel(file, true);
      stream = Channels.newOutputStream(channel);
    }
    public void close() throws IOException {
      stream.close();
      channel.closeFinally();
    }
    public void flush() throws IOException {
      stream.flush();
    }
    public void write(byte[] b, int off, int len) throws IOException {
      stream.write(b, off, len);
    }
    public void write(byte[] b) throws IOException {
      stream.write(b);
    }
    public void write(int b) throws IOException {
      stream.write(b);
    }
  }

  public static class FilesAPIFileItem implements FileItem, HasBlobKey {
    private static final long serialVersionUID = 3683112300714613746L;
    private String field;
    private String type;
    private boolean formField;
    private String name;

    static private FileService fileService = FileServiceFactory.getFileService();
    static private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    
    public static BlobstoreService getBlobstoreService() {
      return blobstoreService;
    }

    private AppEngineFile file = null;

    public FilesAPIFileItem(String fieldName, String contentType,
        boolean isFormField, String fileName) throws IOException {
      field = fieldName;
      type = contentType;
      formField = isFormField;
      name = fileName;
      file = fileService.createNewBlobFile(contentType, fileName);
    }

    public void delete() {
      BlobKey key = getKey();
      if (key != null) {
        blobstoreService.delete(key);
        file = null;
      }
    }

    public byte[] get() {
      BlobKey key = getKey();
      if (key == null)
        return null;
      return blobstoreService.fetchData(key, 0, getSize() - 1);
    }

    public String getContentType() {
      return type;
    }

    public String getFieldName() {
      return field;
    }

    public InputStream getInputStream() throws IOException {
      if (file == null)
        return null;
      FileReadChannel channel = fileService.openReadChannel(file, false);
      return Channels.newInputStream(channel);
    }

    public String getName() {
      return name;
    }

    public OutputStream getOutputStream() throws IOException {
      if (file == null)
        return null;
      return new FilesAPIOutputStream(fileService, file);
    }

    public long getSize() {
      BlobKey key = getKey();
      if (key == null)
        return 0;
      BlobInfo info = new BlobInfoFactory().loadBlobInfo(key);
      if (info == null)
        return 0;
      return info.getSize();
    }

    public String getString() {
      return get().toString();
    }

    public String getString(String encoding)
        throws UnsupportedEncodingException {
      return new String(get(), encoding);
    }

    public boolean isFormField() {
      return formField;
    }

    public boolean isInMemory() {
      return false;
    }

    public void setFieldName(String arg0) {
      field = arg0;
    }

    public void setFormField(boolean arg0) {
      formField = arg0;
    }

    public void write(File arg0) throws Exception {
      throw new UnsupportedOperationException("Writing to file is not allowed");
    }

    public BlobKey getKey() {
      if (file == null)
        return null;
      return fileService.getBlobKey(file);
    }
    
    public String getKeyString() {
      BlobKey k = getKey();
      return k == null ? null : k.getKeyString();
    }
  }

  public FileItem createItem(String fieldName, String contentType,
      boolean isFormField, String fileName) {
    try {
      return new FilesAPIFileItem(fieldName, contentType, isFormField, fileName);
    } catch (IOException x) {
      x.printStackTrace();
      return null;
    }
  }
}