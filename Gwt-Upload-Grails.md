
In the server side you have to write your own !MultipartResolver, take a look to this message in the mailing list:
https://groups.google.com/forum/#!searchin/gwtupload/grails/gwtupload/dGe-TGMmL1c/6qu2-0v3j-4J

    package my.package
    
    import javax.servlet.http.HttpServletRequest
    
    import org.apache.commons.logging.Log;
    import org.springframework.web.context.WebApplicationContext
    import org.springframework.web.context.support.WebApplicationContextUtils
    import org.springframework.web.multipart.MaxUploadSizeExceededException
    import org.springframework.web.multipart.MultipartHttpServletRequest;
    import org.springframework.web.multipart.commons.CommonsMultipartResolver;
    import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
    import org.apache.commons.logging.LogFactory
    
    class MyMultipartResolver extends CommonsMultipartResolver {
      private Log log = LogFactory.getLog(getClass())
    
      @Override
      public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) {
        log.debug 'Multipart Filter URI ' + request.requestURI
        MultipartHttpServletRequest lMultipartHttpServletRequest = null
        if (!request.requestURI.endsWith("gupld")) {
           // request is not for gwtupload. Default processing
           try {
             lMultipartHttpServletRequest = super.resolveMultipart(request)
           } catch (MaxUploadSizeExceededException maxUploadSizeExceededException) {
             request.exception = maxUploadSizeExceededException
             return new DefaultMultipartHttpServletRequest(
                (HttpServletRequest) request, 
                 new org.springframework.util.LinkedMultiValueMap(), 
                 new HashMap())
           }
        } else {
          // Create an "empty" return value without extracting the files from the request
          lMultipartHttpServletRequest = new DefaultMultipartHttpServletRequest(
            (HttpServletRequest) request, 
            new org.springframework.util.LinkedMultiValueMap(), 
            new HashMap())
        }
        return lMultipartHttpServletRequest
      }
    }