

<wiki:toc max_depth="3" />

# Customizing the choose button

You can use any clickable widget to open the choose file dialog, [DecoratedFileUpload](http://gwtupload.googlecode.com/svn/site/apidocs/core/gwtupload/client/DecoratedFileUpload.html) does the work of hiding the default browser button.
### Using css

!GwtUpload comes with 3 decorated widgets: Button, Label and Anchor.
      SingleUploader u = new SingleUploader(FileInputType.LABEL);
      // Set the classname you want to use in the css file
      u.getWidget().setStyleName("customButton"); 
      // Set the size of the bg image you want to use
      u.getWidget().setSize("159px", "27px");
      RootPanel.get().add(u);
     .customButton {
       background: url(images/chooseFile.png);
       font-size: 0px;
       color: white;
     }
     .customButton-over {
       background: url(images/chooseFileO.png);
     }
    

### Implementing your own widget

You can provide your own widget, but be sure that:
1. Your widget implements `HasClickHandlers`
1. Optionally you can implement `HasMouseOverHandlers` and `HasMouseOutHandlers`

      public class MyFancyLookingButton extends Composite implements HasClickHandlers {
        DecoratorPanel widget = new DecoratorPanel();
        
        public MyFancyLookingButton() {
          DecoratorPanel widget = new DecoratorPanel();
          initWidget(widget);
          widget.setWidget(new HTML("Choose ..."));
        }
    
        public HandlerRegistration addClickHandler(ClickHandler handler) {
          return addDomHandler(handler, ClickEvent.getType());
        }
      }
    
      MyFancyLookingButton button = new MyFancyLookingButton();
      SingleUploader uploader = new SingleUploader(FileInputType.CUSTOM.with(button));
      RootPanel.get().add(uploader);

# Customizing progress bar

### Using css

1. There are set of classes you can use in your css files to change default progress bar appearance

    
    /* Main container */
    .GWTUpld .prgbar {
      height: 12px;
      width: 100px;
    }
    
    /* background */
    .GWTUpld .prgbar-back {
      background: #ffffff;
      border: 1px solid #999999;
      overflow: hidden;
      padding: 1px;
    }
    
    /* done */
    .GWTUpld .prgbar-done {
      background: #d4e4ff;
      font-size: 0;
      height: 100%;
      float: left;
    }
     

### Implementing your own widget

1. Create your customized progress bar implementing `HasProgress`.
1. Create your own version of `IUploadStatus` extending `BaseUploadStatus` and set your progress bar in the constructor.

      // progress bar using HTML5 <progress> tag
      // Note that it does not works in earlier versions to IE10.
      public static class MyProgressBar extends Widget implements HasProgress {
        public MyProgressBar() {
          setElement(Document.get().createElement("progress"));
        }
        public void setProgress(long done, long total) {
          getElement().setAttribute("max", "" + total);
          getElement().setAttribute("value", "" + done);
        }
      }
    
      public static class MyUploadStatus extends BaseUploadStatus {
        public MyStatusBar() {
          setProgressWidget(new MyProgressBar());
        }
      }
    
      SingleUploader uploader = new SingleUploader(FileInputType.BUTTON, new MyStatusBar());
      RootPanel.get().add(uploader);
    

# Questions

Send emails to the group [gwtupload@googlegroups.com](http://groups.google.com/group/gwtupload) to ask for help.

The group is public and it is indexed, so anyone should find and read your questions/answers.

**IMPORTANT!** Comments in this page will be ignored, please use the mailing list.

Thank you.

----
*©2011 [Manuel Carrasco Moñino](http://manolocarrasco.blogspot.com)* 