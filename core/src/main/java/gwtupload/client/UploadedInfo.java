package gwtupload.client;

public class UploadedInfo {
  
  /**
   * Field name sent to the server
   */
  private String field;

  /**
   * File name sent by the client
   */
  private String name;

  /**
   * Content-type sent by the client
   */
  private String ctype;

  /**
   * Size in bytes calculated in the server
   */
  private int size = 0;

  /**
   * Used when the server sends a special key to identify the file.
   * Blobstore uses it.
   */
  private String key;

  /**
   * Returns the name of the file selected by the user reported by the browser
   * or an empty string when the user has not selected any one.
   */
  public String getFileName() {
    return name;
  }

  public String getCtype() {
    return ctype;
  }

  public int getSize() {
    return size;
  }

  public String getKey() {
    return key;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCtype(String ctype) {
    this.ctype = ctype;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }  
}