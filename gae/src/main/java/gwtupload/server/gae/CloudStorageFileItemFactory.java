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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.apphosting.api.ApiProxy;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemHeaders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.util.HashMap;

import static gwtupload.shared.UConsts.MULTI_SUFFIX;

/**
 * Upload factory based in the GAE Cloud Storage API.
 *
 * @author Vyacheslav Sokolov
 * @author Manolo Carrasco Moñino
 */
public class CloudStorageFileItemFactory implements FileItemFactory, Serializable {

  private static final long serialVersionUID = 3683112300714613746L;

  public static class CloudStorageOutputStream extends OutputStream {
    private GcsOutputChannel channel;
    private OutputStream stream;    

    public CloudStorageOutputStream(GcsService service, GcsFilename file, GcsFileOptions options)
        throws IOException {
      channel = service.createOrReplace(file, options);
      stream = Channels.newOutputStream(channel);
    }
    public void close() throws IOException {
      stream.close();
      channel.close();
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

  public static class CloudStorageFileItem implements FileItem, HasBlobKey {
    private static final long serialVersionUID = 3683112300714613746L;
    private String field;
    private GcsFileOptions options;
    private String bucket;
    private boolean formField;
    private String name;

    static private GcsService gcsService = GcsServiceFactory.createGcsService();
    static private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    public static BlobstoreService getBlobstoreService() {
      return blobstoreService;
    }

    private GcsFilename file = null;

    public CloudStorageFileItem(String fieldName, String contentType,
        boolean isFormField, String fileName) throws IOException {
      field = fieldName;
      options = new GcsFileOptions.Builder().mimeType(contentType).build();
      formField = isFormField;
      name = fileName;
      
      String bucketNameProperty = System.getProperty("upload.gcs.bucket.name");
      boolean useDefaultBucket = bucketNameProperty == null || bucketNameProperty.length() == 0;
      
      if (useDefaultBucket) {
        String appId = ApiProxy.getCurrentEnvironment().getAppId();

        if (appId.startsWith("s~")) {
          appId = appId.substring(2);
        }

        bucket = appId + ".appspot.com";
      } else {
        bucket = bucketNameProperty;
      }
      file = new GcsFilename(bucket, fileName);
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
      return options.getMimeType();
    }

    public String getFieldName() {
      return field;
    }

    public InputStream getInputStream() throws IOException {
      if (file == null)
        return null;
      GcsInputChannel channel = gcsService.openReadChannel(file, 0);
      return Channels.newInputStream(channel);
    }

    public String getName() {
      return name;
    }

    public OutputStream getOutputStream() throws IOException {
      if (file == null)
        return null;
      return new CloudStorageOutputStream(gcsService, file, options);
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
      return BlobstoreServiceFactory.getBlobstoreService().createGsBlobKey(
          "/gs/" + file.getBucketName() + "/" + file.getObjectName());
    }

    public String getKeyString() {
      BlobKey k = getKey();
      return k == null ? null : k.getKeyString();
    }

    public FileItemHeaders getHeaders() {
      return null;
    }

    public void setHeaders(FileItemHeaders headers) {
    }
  }

  private HashMap<String, Integer> map = new HashMap<String, Integer>();

  public FileItem createItem(String fieldName, String contentType,
      boolean isFormField, String fileName) {

    if (fieldName.contains(MULTI_SUFFIX)) {
      Integer cont = map.get(fieldName) != null ? (map.get(fieldName) + 1): 0;
      map.put(fieldName, cont);
      fieldName = fieldName.replace(MULTI_SUFFIX, "") + "-" + cont;
    }
    try {
      return new CloudStorageFileItem(fieldName, contentType, isFormField, fileName);
    } catch (IOException x) {
      x.printStackTrace();
      return null;
    }
  }
}