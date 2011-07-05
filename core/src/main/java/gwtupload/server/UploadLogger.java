package gwtupload.server;

import java.util.Date;
import java.util.logging.Level;

public class UploadLogger {

  public static interface ILogger {
    void debug(Object o);

    void info(Object o);

    void error(Object o);

    void debug(Object o, Throwable t);

    void info(Object o, Throwable t);

    void error(Object o, Throwable t);
  }

  static class StderrLogger implements ILogger {
    String name = "";

    StderrLogger(String name) {
      this.name = name;
    }

    public void debug(Object o) {
      System.out.println(new Date() + " " + name + " DEBUG: " + o);
    }

    public void info(Object o) {
      System.out.println(new Date() + " " + name + " INFO: " + o);
    }

    public void error(Object o) {
      System.err.println(new Date() + " " + name + " ERROR: " + o);
    }

    public void debug(Object o, Throwable t) {
      debug(o);
    }

    public void info(Object o, Throwable t) {
      info(o);
    }

    public void error(Object o, Throwable t) {
      error(o);
    }
  }

  static class Log4jLogger implements ILogger {
    org.apache.log4j.Logger logger;

    protected Log4jLogger(String name) {
      logger = org.apache.log4j.Logger.getLogger(name);
    }

    public void debug(Object o) {
      logger.debug(o);
    }

    public void info(Object o) {
      logger.info(o);
    }

    public void error(Object o) {
      logger.error(o);
    }

    public void debug(Object o, Throwable t) {
      logger.debug(o, t);
    }

    public void error(Object o, Throwable t) {
      logger.error(o, t);
    }

    public void info(Object o, Throwable t) {
      logger.info(o, t);
    }
  }

  static class JavaLogger implements ILogger {
    java.util.logging.Logger logger;

    protected JavaLogger(String name) {
      logger = java.util.logging.Logger.getLogger(name);
    }

    public void debug(Object o) {
      logger.log(Level.FINE, o.toString());
    }

    public void info(Object o) {
      logger.log(Level.INFO, o.toString());
    }

    public void error(Object o) {
      logger.log(Level.SEVERE, o.toString());
    }

    public void debug(Object o, Throwable t) {
      logger.log(Level.FINE, o.toString(), t);
    }

    public void info(Object o, Throwable t) {
      logger.log(Level.INFO, o.toString(), t);
    }

    public void error(Object o, Throwable t) {
      logger.log(Level.SEVERE, o.toString(), t);
    }
  }

  ILogger logger;

  private UploadLogger(String name) {
    try {
      Class.forName("org.apache.log4j.Logger");
      logger = new Log4jLogger(name);
    } catch (ClassNotFoundException e1) {
      try {
        Class.forName("java.util.logging.Logger");
        logger = new JavaLogger(name);
      } catch (ClassNotFoundException e2) {
        logger = new StderrLogger(name);
      }
    }
  }

  public static UploadLogger getLogger(Class<?> class1) {
    return new UploadLogger(class1.getName());
  }
  
  public void debug(Object o) {
    logger.debug(o);
  }

  public void info(Object o) {
    logger.info(o);
  }

  public void error(Object o) {
    logger.error(o);
  }

  public void debug(Object o, Throwable t) {
    logger.debug(o, t);
  }

  public void error(Object o, Throwable t) {
    logger.error(o, t);
  }

  public void info(Object o, Throwable t) {
    logger.info(o, t);
  }

}
