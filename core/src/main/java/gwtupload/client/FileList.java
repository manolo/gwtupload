package gwtupload.client;

import com.google.gwt.core.client.JavaScriptObject;

public class FileList extends JavaScriptObject {
  protected FileList() {}

  public final native File item(int index) /*-{
		return this.item(index);
	}-*/;

  public final native int getLength() /*-{
		return this.length;
	}-*/;
}
