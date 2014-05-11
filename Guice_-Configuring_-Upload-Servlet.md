
** Extend `UploadAction` and inject your configuration in the constructor 
    @Singleton
    public class MetUpload extends UploadAction {
      @Inject
      public MetUpload(Settings settings) {
        this.maxSize = settings.getIntegerSetting("upload.maxupload", DEFAULT_REQUEST_LIMIT_KB);
        this.uploadDelay = settings.getIntegerSetting("upload.uploadDelay", DEFAULT_SLOW_DELAY_MILLIS);
      }
    
      @Override
      public String executeAction(HttpServletRequest request,
        List<FileItem> sessionFiles) throws UploadActionException {
          for (FileItem fileItem : sessionFiles) {
           // store them somewhere
          }
          return null;
        }
      }

** Or if you prefer to use `Named` parameters, create the appropriate methods for injecting the values.
       @Inject(optional=true) 
       void setMaxSize(@Named("upload.maxupload") size) {
         super.maxSize = size;
       }

* Finally put this in your guice servlet module.
       bind(MetUpload.class).in(Singleton.class);
       serve("*.gupld").with(MetUpload.class);