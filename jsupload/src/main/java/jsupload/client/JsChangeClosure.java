/*
 * Copyright 2009 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
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
package jsupload.client;

import gwtupload.client.HasJsData;
import gwtupload.client.IUploader;
import gwtupload.client.PreloadedImage;
import gwtupload.client.PreloadedImage.OnLoadPreloadedImageHandler;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * @author Manolo Carrasco Moñino
 * 
 * An exportable closure
 *
 */
@Export
@ExportPackage("jsu")
@ExportClosure
public interface JsChangeClosure extends Exportable {
  @Export void onChange(Object object);

}

/**
 * @author Manolo Carrasco Moñino
 * 
 * An utility class for storing static methods
 *
 */
class JsUtils {

  /**
   * Creates a valueChangeHandler that executes a ChangeClosure.
   * 
   * @param <T>
   * @param clazz
   * @param jsChange
   * @return
   */
  public static final <T extends HasJsData> ValueChangeHandler<T> getClosureHandler(final T clazz, final JsChangeClosure jsChange) {
    return new ValueChangeHandler<T>() {
      public void onValueChange(ValueChangeEvent<T> event) {
        Object data = null;
        if (jsChange != null) {
          if (event != null && event.getValue() != null) {
            data = event.getValue().getData();
          }
          jsChange.onChange(data);
        }
      }
    };
  }
  
  public static final ValueChangeHandler<HasJsData> getDataHandler(final JsChangeClosure jsChange) {
    return new ValueChangeHandler<HasJsData>() {
      public void onValueChange(ValueChangeEvent<HasJsData> event) {
        Object data = null;
        if (jsChange != null) {
          if (event != null && event.getValue() != null) {
            data = event.getValue().getData();
          }
          jsChange.onChange(data);
        }
      }
    };
  }
  
  public static final IUploader.OnCancelUploaderHandler getOnCancelUploaderHandler(final JsChangeClosure jsChange) {
    return new IUploader.OnCancelUploaderHandler() {
      public void onCancel(IUploader u) {
        if (jsChange != null) {
        }
        jsChange.onChange(u.getData());
      }
    };
  }
  
  public static final IUploader.OnChangeUploaderHandler getOnChangeUploaderHandler(final JsChangeClosure jsChange) {
    return new IUploader.OnChangeUploaderHandler() {
      public void onChange(IUploader u) {
        if (jsChange != null) {
        }
        jsChange.onChange(u.getData());
      }
    };
  }
  
  public static final IUploader.OnFinishUploaderHandler getOnFinishUploaderHandler(final JsChangeClosure jsChange) {
    return new IUploader.OnFinishUploaderHandler() {
      public void onFinish(IUploader u) {
        if (jsChange != null) {
        }
        jsChange.onChange(u.getData());
      }
    };
  }
  
  public static final OnLoadPreloadedImageHandler getOnLoadPreloadedImageHandler(final JsChangeClosure jsChange) {
    return new OnLoadPreloadedImageHandler() {
      public void onLoad(PreloadedImage image) {
        if (jsChange != null) {
        }
        jsChange.onChange(image.getData());
      }
    };
  }

  public static final IUploader.OnStartUploaderHandler getOnStartUploaderHandler(final JsChangeClosure jsChange) {
    return new IUploader.OnStartUploaderHandler() {
      public void onStart(IUploader u) {
        if (jsChange != null) {
        }
        jsChange.onChange(u.getData());
      }
    };
  }

  public static final IUploader.OnStatusChangedHandler getStatusChangedHandler(final JsChangeClosure jsChange) {
    return new IUploader.OnStatusChangedHandler() {
      public void onStatusChanged(IUploader u) {
        if (jsChange != null) {
        }
        jsChange.onChange(u.getData());
      }
    };
  }
}
