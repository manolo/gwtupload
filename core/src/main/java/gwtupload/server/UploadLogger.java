package gwtupload.server;

import java.util.Date;
import java.util.logging.Level;

public class UploadLogger {
    
    interface ILogger {
        void debug(Object o);
        void info(Object o);
        void error(Object o);
    }
    
    static class StderrLogger implements ILogger {
        String name = "";

        StderrLogger(String name) {
            this.name = name;
        }
        public void debug(Object string) {
            System.out.println(new Date() + " " + name + " DEBUG: " + string );
        }
        public void info(Object string) {
            System.out.println(new Date() + " " + name + " INFO: " + string );
        }
        public void error(Object string) {
            System.err.println(new Date() + " " + name + " ERROR: " + string );
        }
    }
    
    static class Log4jLogger implements ILogger {
        org.apache.log4j.Logger logger;
        
        protected Log4jLogger(String name) {
            logger = org.apache.log4j.Logger.getLogger(name);
        }
        public void debug(Object string) {
            logger.debug(string);
        }
        public void info(Object string) {
            logger.info(string);
        }
        public void error(Object string) {
            logger.error(string);
        }        
    }
    
    static class JavaLogger implements ILogger {
        java.util.logging.Logger logger;
        
        protected JavaLogger(String name) {
            logger = java.util.logging.Logger.getLogger(name);
        }
        public void debug(Object string) {
            logger.log(Level.FINE, string.toString());
        }
        public void info(Object string) {
            logger.log(Level.INFO, string.toString());
        }
        public void error(Object string) {
            logger.log(Level.SEVERE, string.toString());
        }        
    }
    
	ILogger logger;
	
	private UploadLogger(String name){
	    try { 
	        Class.forName("org.apache.log4j.Logger");
            logger = new Log4jLogger(name);
	    } catch(ClassNotFoundException e1) {
	        try { 
	            Class.forName("java.util.logging.Logger");
	            logger = new JavaLogger(name);
	        } catch(ClassNotFoundException e2) {
	            logger = new StderrLogger(name);
	        }
	    }
	}

	public static UploadLogger getLogger(Class<?> class1) {
		return new UploadLogger(class1.getName());
	}
	
    public void debug(String string) {
        logger.debug(string);
    }

    public void info(String string) {
        logger.info(string);
    }

    public void error(String string) {
        logger.error(string);
    }

}
