/*
 * Copyright 2017 Sven Strickroth <email@cs-ware.de>
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
package gwtupload.server;

import static gwtupload.shared.UConsts.TAG_CTYPE;
import static gwtupload.shared.UConsts.TAG_FIELD;
import static gwtupload.shared.UConsts.TAG_FILE;
import static gwtupload.shared.UConsts.TAG_FILES;
import static gwtupload.shared.UConsts.TAG_KEY;
import static gwtupload.shared.UConsts.TAG_NAME;
import static gwtupload.shared.UConsts.TAG_PARAM;
import static gwtupload.shared.UConsts.TAG_PARAMS;
import static gwtupload.shared.UConsts.TAG_RESPONSE;
import static gwtupload.shared.UConsts.TAG_SIZE;
import static gwtupload.shared.UConsts.TAG_VALUE;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLResponse {
  private Document responseDocument;
  private Node responseNode;
  private Node filesNode;
  private Node paramsNode;

  public XMLResponse() {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", null);
    DocumentBuilder builder;
    try {
      builder = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      return;
    }
    responseDocument = builder.newDocument();
    responseNode = responseDocument.createElement(TAG_RESPONSE);
    responseDocument.appendChild(responseNode);
  }

  private Node addResponseNode(String tagName, String value) {
    Element el = responseDocument.createElement(tagName);
    addTextToNode(el, value);
    return responseNode.appendChild(el);
  }

  public void addResponseTag(String tagName, String value) {
    addResponseNode(tagName, value);
  }

  public void addResponseTags(Map<String, String> uploadStatus) {
    for (Entry<String, String> e : uploadStatus.entrySet()) {
      if (e.getValue() == null) {
         continue;
      }
      addResponseTag(e.getKey(), e.getValue().replaceAll("</*pre>", "")); // TODO: is removing pre really needed?
    }
  }

  public void prepareFilesParams() {
    filesNode = addResponseNode(TAG_FILES, null);
    paramsNode = addResponseNode(TAG_PARAMS, null);
  }

  public void addParam(String key, String value) {
    Node paramNode = responseDocument.createElement(TAG_PARAM);

    Node fieldNode = responseDocument.createElement(TAG_FIELD);
    addTextToNode(fieldNode, key);
    paramNode.appendChild(fieldNode);
    Node valueNode = responseDocument.createElement(TAG_VALUE);
    addTextToNode(valueNode, value);
    paramNode.appendChild(valueNode);

    paramsNode.appendChild(paramNode);
  }

  private void addTextToNode(Node node, String value) {
    if (value == null) {
      return;
    }

    node.appendChild(responseDocument.createTextNode(value));
  }

  public String getXML() {
    Transformer tf;
    try {
       tf = TransformerFactory.newInstance().newTransformer();
    } catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
      e.printStackTrace();
      return "";
    }
    tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    tf.setOutputProperty(OutputKeys.INDENT, "yes");
    Writer out = new StringWriter();
    try {
      tf.transform(new DOMSource(responseDocument), new StreamResult(out));
    } catch (TransformerException e) {
      e.printStackTrace();
    }
    return out.toString();
  }

  public void addFile(String fieldName, String fileName, long size, String contentType, String key) {
    Node fileNode = responseDocument.createElement(TAG_FILE);

    Node fieldNode = responseDocument.createElement(TAG_FIELD);
    addTextToNode(fieldNode, fieldName);
    fileNode.appendChild(fieldNode);
    Node fileNameNode = responseDocument.createElement(TAG_NAME);
    addTextToNode(fileNameNode, fileName);
    fileNode.appendChild(fileNameNode);
    Node fileSizeNode = responseDocument.createElement(TAG_SIZE);
    addTextToNode(fileSizeNode, new Long(size).toString());
    fileNode.appendChild(fileSizeNode);
    Node contentTypeNode = responseDocument.createElement(TAG_CTYPE);
    addTextToNode(contentTypeNode, contentType);
    fileNode.appendChild(contentTypeNode);
    if (key != null) {
      Node keyNode = responseDocument.createElement(TAG_KEY);
      addTextToNode(keyNode, key);
      fileNode.appendChild(keyNode);
    }

    filesNode.appendChild(fileNode);
  }
}
