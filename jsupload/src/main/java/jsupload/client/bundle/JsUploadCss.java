package jsupload.client.bundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface JsUploadCss extends ClientBundle {
    public static final JsUploadCss INSTANCE =  GWT.create(JsUploadCss.class);

    @Source("JsUpload.css")
    public CssResource css();
}