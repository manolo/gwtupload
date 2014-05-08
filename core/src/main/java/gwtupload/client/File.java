package gwtupload.client;

import com.google.gwt.core.client.JavaScriptObject;

public class File extends JavaScriptObject {
  protected File() {}

  public final native String getName() /*-{
		return this.name;
	}-*/;
}
