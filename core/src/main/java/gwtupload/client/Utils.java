package gwtupload.client;

import java.util.Collection;
import java.util.List;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

/**
 * Utility class.
 */
public final class Utils {
  /**
   * return the name of a file without path.
   */
  public static String basename(String name) {
    return name.replaceAll("^.*[/\\\\]", "");
  }

  public static int getPercent(long done, long total) {
    return (int) (total > 0 ? done * 100 / total : 0);
  }

  /**
   * return the text content of a first tag in a xml document
   */
  public static String getXmlNodeValue(Document doc, String tag) {
    return getXmlNodeValue(doc, tag, 0);
  }
  
  /**
   * return the text content of a tag in the position idx inside a xml document
   */
  public static String getXmlNodeValue(Document doc, String tag, int idx) {
    if (doc == null) {
      return null;
    }
    return getXmlNodeValue(doc.getElementsByTagName(tag), tag, idx);
  }

  public static String getXmlNodeValue(NodeList list, String tagName, int idx) {
    if (list == null || list.getLength() <= idx) {
      return null;
    }
    Node node = list.item(idx);
    if (node.getNodeType() != Node.ELEMENT_NODE) {
      return null;
    }
    String ret = "";
    NodeList textNodes = node.getChildNodes();
    for (int i = 0; i < textNodes.getLength(); i++) {
      Node n = textNodes.item(i);
      if (n.getNodeType() == Node.TEXT_NODE && n.getNodeValue().replaceAll("[ \\n\\t\\r]", "").length() > 0) {
        ret += n.getNodeValue();
      } else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
        ret += n.getNodeValue();
      }
    }
    return ret.length() == 0 ? null : ret.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
  }

  /**
   * Return true in the case of the filename has an extension included in the
   * validExtensions array. It isn't case sensitive.
   * 
   * @param validExtensions an array with allowed extensions. ie: .jpg, .mpg ..
   * @param fileName
   * @return true in the case of valid filename
   */
  public static boolean validateExtension(List<String> validExtensions, String fileName) {
    if (fileName == null || fileName.length() == 0) {
      return false;
    }

    boolean valid = validExtensions == null || validExtensions.isEmpty() ? true : false;
    if (!valid) for (String regx : validExtensions) {
      if (fileName.toLowerCase().matches(regx)) {
        valid = true; 
        break;
      }
    }
    return valid;
  }

  public static String convertCollectionToString(Collection<String> strings, String separator) {
    String result = "";
    boolean first = true;
    for (String s : strings) {
      if (first) {
        result += s;
        first = false;
      } else {
        result += separator + s;
      }
    }
    return result;
  }
}