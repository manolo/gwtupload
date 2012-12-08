package gwtuploadsample.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MUpld extends Composite {

  private static MUpldUiBinder uiBinder = GWT.create(MUpldUiBinder.class);

  interface MUpldUiBinder extends UiBinder<Widget, MUpld> {
  }

  public MUpld() {
    initWidget(uiBinder.createAndBindUi(this));
  }

}
