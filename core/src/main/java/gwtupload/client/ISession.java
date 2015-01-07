package gwtupload.client;

import static gwtupload.shared.UConsts.*;

import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.XMLParser;

public interface ISession {

  public static class CORSSession extends Session {
    @Override
    protected void setSessionId(String s) {
      super.setSessionId(s);
      s = s == null? "" : (";jsessionid=" + s);
      servletPath = servletPath.replaceFirst("^(.+)(/[^/\\?;]*)(;[^/\\?]*|)(\\?|/$|$)(.*)", "$1$2" + s + "$4$5");
      System.err.println("CORS Session: " + servletPath);
    }
  }

  public static class Session implements ISession {
    String sessionId;
    String servletPath = "servlet.gupld";

    public static ISession createSession(String path, RequestCallback callback) {
      Session ret = path.startsWith("http") ? new CORSSession() : new Session();
      ret.servletPath = path;
      ret.getSession(callback);
      return ret;
    }

    /**
     * Sends a request to the server in order to get the session cookie,
     * when the response with the session comes, it submits the form.
     *
     * This is needed because this client application usually is part of
     * static files, and the server doesn't set the session until dynamic pages
     * are requested.
     *
     * If we submit the form without a session, the server creates a new
     * one and send a cookie in the response, but the response with the
     * cookie comes to the client at the end of the request, and in the
     * meanwhile the client needs to know the session in order to ask
     * the server for the upload status.
     */
    public void getSession(final RequestCallback callback) {
      sendRequest("session", new RequestCallback() {
        public void onResponseReceived(Request request, Response response) {
          String s  = Cookies.getCookie("JSESSIONID");
          if (s == null) {
            s = Utils.getXmlNodeValue(XMLParser.parse(response.getText()), TAG_SESSION_ID);
          }
          setSessionId(s);
          callback.onResponseReceived(request, response);
        }
        public void onError(Request request, Throwable exception) {
          setSessionId(null);
          callback.onError(request, exception);
        }
      }, PARAM_SESSION + "=true");
    }

    protected void setSessionId(String s) {
      sessionId = s;
    }

    public String getServletPath() {
      return servletPath;
    }

    public void sendRequest(String payload, RequestCallback callback, String... params) {
      // Using a reusable builder makes IE fail
      RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, composeURL(params));
      reqBuilder.setTimeoutMillis(DEFAULT_AJAX_TIMEOUT);
      try {
        reqBuilder.sendRequest(payload, callback);
      } catch (RequestException e) {
        callback.onError(null, e);
      }
    }

    public String composeURL(String... params) {
      String ret = servletPath;
      ret = ret.replaceAll("[\\?&]+$", "");
      String sep = ret.contains("?") ? "&" : "?";
      for (String par : params) {
        ret += sep + par;
        sep = "&";
      }
      for (Entry<String, List<String>> e : Window.Location.getParameterMap().entrySet()) {
        ret += sep + e.getKey() + "=" + e.getValue().get(0);
      }
      ret += sep + "random=" + Math.random();
      return ret;
    }
  }

  static final int DEFAULT_AJAX_TIMEOUT = 10000;

  public void getSession(RequestCallback callback);

  public String composeURL(String... params);

  public void sendRequest(String name, RequestCallback callback, String... params);

  public String getServletPath();
}
