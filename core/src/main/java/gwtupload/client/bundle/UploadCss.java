package gwtupload.client.bundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface UploadCss extends ClientBundle {
    public static final UploadCss INSTANCE = GWT.create(UploadCss.class);

    @Source("Upload.css")
    public CssResource css();

    @Source("cancel-upld.gif")
    ImageResource imgCancelUpload();

    @Source("cancel-upld-hover.gif")
    ImageResource imgCancelUploadHover();
}