package gwtupload.server.gae;

import com.google.appengine.api.blobstore.BlobKey;

import gwtupload.server.HasKey;

public interface HasBlobKey extends HasKey {
  BlobKey getKey();
}
