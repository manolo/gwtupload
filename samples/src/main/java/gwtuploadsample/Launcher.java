package gwtuploadsample;

import java.net.InetSocketAddress;
import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * When gwtupload samples are packaged with jetty this class is called to start jetty server.
 * 
 * @author manolo
 */
public final class Launcher {
  public static void main(String[] args) throws Exception {

    int port = Integer.parseInt(System.getProperty("port", "8080"));
    String bindAddress = System.getProperty("host", "0.0.0.0");

    InetSocketAddress a = new InetSocketAddress(bindAddress, port);
    Server server = new Server(a);

    ProtectionDomain domain = Launcher.class.getProtectionDomain();
    URL location = domain.getCodeSource().getLocation();
    WebAppContext webapp = new WebAppContext();
    webapp.setContextPath("/");
    webapp.setWar(location.toExternalForm());

    server.setHandler(webapp);
    server.start();
    server.join();
  }
}

