package gwtuploadsample.server;

import gwtupload.shared.UConsts;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class S3UploadServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(S3UploadServlet.class);

  private String awsBucketName;
  private String awsAccessKey;
  private byte[] awsSecretKey;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    awsBucketName = getInitParameter("awsBucketName");
    if (awsBucketName == null) {
      throw new ServletException(getClass().getSimpleName() + ": awsBucketName init-param is missing.");
    }

    awsAccessKey = getInitParameter("awsAccessKey");
    if (awsAccessKey == null) {
      throw new ServletException(getClass().getSimpleName() + ": awsAccessKey init-param is missing.");
    }

    String awsSecretKeyString = getInitParameter("awsSecretKey");
    if (awsSecretKeyString == null) {
      throw new ServletException(getClass().getSimpleName() + ": awsSecretKey init-param is missing.");
    }
    awsSecretKey = awsSecretKeyString.getBytes(Charsets.UTF_8);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if ("true".equals(request.getParameter(UConsts.PARAM_SESSION))) {
      doSession(request, response);
      return;
    }

    if ("true".equals(request.getParameter(UConsts.PARAM_BLOBSTORE))) {
      doBlobstore(request, response);
      return;
    }

    if ("true".equals(request.getParameter("done"))) {
      doDone(request, response);
      return;
    }

    if (request.getParameter("show") != null) {
      doShow(request, response);
      return;
    }

    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    logger.warn("Unsupported usage of " + getClass().getSimpleName() + " with queryString: " + request.getQueryString());
  }

  private void doSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/xml; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    out.println(
      "<response>" +
        "<" + UConsts.TAG_BLOBSTORE +">true</" + UConsts.TAG_BLOBSTORE +">" +
      "</response>"
    );
  }

  private void doBlobstore(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/xml; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

    String redirect = request.getRequestURL().toString() + "?done=true"; // redirect amazon to this servlet for doDone

    DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Calendar expiration = Calendar.getInstance();
    expiration.setTime(new Date());
    expiration.add(Calendar.HOUR, 24);
    String policyDocument = new ObjectMapper().writeValueAsString(ImmutableMap.of(
      "expiration", iso8601.format(expiration.getTime()),
      "conditions", ImmutableList.of(
        ImmutableMap.of("bucket", awsBucketName),
        ImmutableList.of("starts-with", "$key", ""),
        ImmutableMap.of("acl", "public-read"),
        ImmutableMap.of("redirect", redirect),
        ImmutableList.of("content-length-range", 0, 10*1024*1024) // maximum 10MB
      )
    ));

    String policy = new String(Base64.encodeBase64(policyDocument.getBytes(Charsets.UTF_8)));
    Mac hmac = null;
    try {
      hmac = Mac.getInstance("HmacSHA1");
      hmac.init(new SecretKeySpec(awsSecretKey, "HmacSHA1"));
    } catch (GeneralSecurityException e) {
      logger.error("Cannot sign policy in " + getClass().getSimpleName() + ".doBlobstore: " + e.getMessage(), e);
      out.println(
        "<response>" +
          "<" + UConsts.TAG_ERROR + ">" + StringEscapeUtils.escapeXml(e.getMessage()) + "</" + UConsts.TAG_ERROR + ">" +
        "</response>"
      );
      return;
    }

    String fileKey = request.getParameter(UConsts.PARAM_NAME);
    String signature = new String(Base64.encodeBase64(hmac.doFinal(policy.getBytes(Charsets.UTF_8))));
    String blobPath = "http://s3.amazonaws.com/" + awsBucketName;

    out.println(
      "<response>" +
        "<" + UConsts.TAG_BLOBSTORE_PATH + ">" + StringEscapeUtils.escapeXml(blobPath) + "</" + UConsts.TAG_BLOBSTORE_PATH + ">" +
        "<" + UConsts.TAG_BLOBSTORE_NAME + ">file</" + UConsts.TAG_BLOBSTORE_NAME + ">" +
        blobParam("key", fileKey) +
        blobParam("acl", "public-read") +
        blobParam("AWSAccessKeyId", awsAccessKey) +
        blobParam("policy", policy) +
        blobParam("signature", signature) +
        blobParam("redirect", redirect) +
      "</response>"
    );
  }

  private static String blobParam(String name, String value) {
    return "<" + UConsts.TAG_BLOBSTORE_PARAM + " " + UConsts.ATTR_BLOBSTORE_PARAM_NAME + "=\"" + StringEscapeUtils.escapeXml(name) + "\">" + StringEscapeUtils.escapeXml(value) + "</" + UConsts.TAG_BLOBSTORE_PARAM + ">";
  }

  private void doDone(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/plain; charset=UTF-8");
    PrintWriter out = response.getWriter();

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append(
      "<response>" +
        "<" + UConsts.TAG_FIELD + ">" + StringEscapeUtils.escapeXml(request.getParameter("key")) + "</" + UConsts.TAG_FIELD + ">" +
        "<" + UConsts.TAG_FILE + ">" +
          "<" + UConsts.TAG_CTYPE + "> </" + UConsts.TAG_CTYPE + ">" + // content-type is unknown
          "<" + UConsts.TAG_SIZE + ">0</" + UConsts.TAG_SIZE + ">" + // size is unknown
          "<" + UConsts.TAG_NAME + ">" + StringEscapeUtils.escapeXml(request.getParameter("key")) + "</" + UConsts.TAG_NAME + ">" +
        "</" + UConsts.TAG_FILE + ">" +
        "<" + UConsts.TAG_FINISHED + ">" + UConsts.TAG_OK + "</" + UConsts.TAG_FINISHED + ">" +
        "<" + UConsts.TAG_MESSAGE + "><![CDATA[ok]]></" + UConsts.TAG_MESSAGE + ">" +
      "</response>"
    );
    out.print(UConsts.TAG_MSG_START + xml.toString().replaceAll("<", UConsts.TAG_MSG_LT).replaceAll(">", UConsts.TAG_MSG_GT) + UConsts.TAG_MSG_END);
  }

  private void doShow(HttpServletRequest request, HttpServletResponse response) {
    response.setStatus(301);
    String name = request.getParameter(UConsts.PARAM_SHOW);
    name = name.replaceFirst("-[\\d]+$", "");
    response.setHeader("Location", "http://s3.amazonaws.com/" + awsBucketName + "/" + name);
  }

}
