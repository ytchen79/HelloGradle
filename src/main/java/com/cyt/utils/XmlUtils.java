package com.cyt.utils;

import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings(value = "unchecked")
public class XmlUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);

    private static final String DEFAULT_CHARSET = "UTF-8";

    public static boolean isFormatXML(String xml) {
        try {
            Document doc = DocumentHelper.parseText(xml);
            return true;
        } catch (DocumentException e) {
            return false;
        }
    }

    public static boolean isFormatXML(File xmlFile) {
        try {
            new SAXReader().read(xmlFile);
            return true;
        } catch (DocumentException e) {
            return false;
        }
    }

    public static Element getSingleElementByName(String xml, String name) throws DocumentException {
        Document doc = DocumentHelper.parseText(xml);

        List<Element> e = doc.selectNodes(name);
        if (e.isEmpty()) {
            return null;
        }
        return e.get(0);
    }

    public static List<Element> getElementsByName(String xml, String name) throws DocumentException {
        Document doc = DocumentHelper.parseText(xml);
        List<Element> e = doc.selectNodes(name);
        if (e.isEmpty()) {
            return null;
        }
        return e;
    }

    public static String getTextByName(String xml, String name) throws DocumentException {
        Element e = getSingleElementByName(xml, name);
        if (e != null) {
            return e.getText();
        }
        return null;
    }

    public static void createXmlFile(Element root, String path, String charsetName) throws IOException {
        Document document = DocumentHelper.createDocument(root);
        createXmlFile(document, path, charsetName);
    }

    public static void createXmlFile(String xml, String path, String charsetName) throws IOException, DocumentException {
        Document document = DocumentHelper.parseText(xml);
        createXmlFile(document, path, charsetName);
    }

    public static void createXmlFile(Document document, String path, String charsetName) throws IOException {
        OutputFormat format = new OutputFormat("    ", true);
        format.setEncoding(charsetName);
        File xmlFile = new File(path);
        if (!xmlFile.getParentFile().exists() && !xmlFile.getParentFile().mkdirs()) {
            throw new IOException(xmlFile.getParentFile().getAbsoluteFile() + "创建失败！");
        }
        XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(xmlFile), format);
        xmlWriter.write(document);
        xmlWriter.close();
    }

    public static Element getElementByNodeName(Element root, String nodeName) {
        return root.element(nodeName);
    }

    public static List<Element> getElementsByNodeName(Element root, String nodeName) {
        return root.elements(nodeName);
    }

    public static Map<String, Object> xmlToMap2(String xmlStr) throws Exception {
        if (!isFormatXML(xmlStr)) {
            throw new IllegalArgumentException(StringUtils.formatString("%s为非XML格式报文！", xmlStr));
        }
        Map<String, Object> xmlMap = new HashMap<>();
        Document doc = DocumentHelper.parseText(xmlStr);
        Element root = doc.getRootElement();
        List<Element> elementList = root.elements();
        for (Element e : elementList) {
            List<Object> tmpList = null;
            if (xmlMap.get(e.getName()) != null) {
                tmpList = (List<Object>) xmlMap.get(e.getName());
            } else {
                tmpList = new ArrayList<>();
                xmlMap.put(e.getName(), tmpList);
            }
            if (e.isTextOnly()) {
                tmpList.add(StringUtils.trim(e.getText()));
            } else {
                tmpList.add(xmlToMap2(e.asXML()));
            }
        }
        return xmlMap;
    }

    public static String getElementText(String xmlStr, String elementName) throws Exception {
        if (!isFormatXML(xmlStr)) {
            throw new IllegalArgumentException(StringUtils.formatString("%s为非XML格式报文！", xmlStr));
        }
        Element e = getSingleElementByName(xmlStr, elementName);
        return e == null ? null : e.asXML();
    }

    public static Map<String, Object> xmlToMap(String xmlStr) throws Exception {
        if (!isFormatXML(xmlStr)) {
            throw new IllegalArgumentException(StringUtils.formatString("%s为非XML格式报文！", xmlStr));
        }
        Map<String, Object> xmlMap = new HashMap<>();
        Document doc = DocumentHelper.parseText(xmlStr);
        Element root = doc.getRootElement();
        List<Element> elementList = root.elements();
        for (Element e : elementList) {
            if (root.elements(e.getName()).size() > 1) {
                List<Object> tmpList = null;
                if (xmlMap.get(e.getName()) != null) {
                    tmpList = (List<Object>) xmlMap.get(e.getName());
                } else {
                    tmpList = new ArrayList<>();
                    xmlMap.put(e.getName(), tmpList);
                }
                if (e.isTextOnly()) {
                    tmpList.add(StringUtils.trim(e.getText()));
                } else {
                    tmpList.add(xmlToMap(e.asXML()));
                }
            } else {
                if (e.isTextOnly()) {
                    xmlMap.put(e.getName(), StringUtils.trim(e.getText()));
                } else {
                    xmlMap.put(e.getName(), xmlToMap(e.asXML()));
                }
            }
        }
        return xmlMap;
    }

    public static String mapToXml(String rootElementName, Map<String, Object> xmlMap) {
        if (!StringUtils.hasText(rootElementName)) {
            return mapToXml(xmlMap);
        } else {
            Document doc = DocumentHelper.createDocument();
            Element root = doc.addElement(rootElementName);
            mapToXml(root, xmlMap);
            return root.asXML();
        }
    }

    public static void mapToXml(Element root, Map<String, Object> xmlMap) {
        for (Map.Entry<String, Object> entry : xmlMap.entrySet()) {
            String key = null;
            Element subElement;
            if (entry.getValue() instanceof List) {
//				List<Map<String, Object>> tmpList = (List<Map<String, Object>>) entry.getValue();
                List tmpList = (List) entry.getValue();
                if (tmpList.get(0) instanceof Map) {
                    for (int i = 0; i < tmpList.size(); i++) {
                        Map<String, Object> tmp = (Map<String, Object>) tmpList.get(i);
                        key = entry.getKey();
                        if (entry.getKey().contains("$$")) {
                            key = entry.getKey().split("\\$\\$")[0];
                            subElement = root.addElement(key);
                            String attrStr = entry.getKey().split("\\$\\$")[1];
                            if (!!StringUtils.hasText(attrStr)) {
                                String[] attrs = attrStr.split(",");
                                for (String attr : attrs) {
                                    String[] keyVal = attr.split("=");
                                    subElement.addAttribute(StringUtils.trim(keyVal[0]), StringUtils.trim(keyVal[1]));
                                }
                            }
                        } else {
                            subElement = root.addElement(entry.getKey());
                        }
                        mapToXml(subElement, (Map<String, Object>) tmp);
                    }
                } else {
                    key = entry.getKey();
                    for (int i = 0; i < tmpList.size(); i++) {
                        if (entry.getKey().contains("$$")) {
                            key = entry.getKey().split("\\$\\$")[0];
                            subElement = root.addElement(key);

                            String attrStr = entry.getKey().split("\\$\\$")[1];
                            if (!!StringUtils.hasText(attrStr)) {
                                String[] attrs = attrStr.split(",");
                                for (String attr : attrs) {
                                    String[] keyVal = attr.split("=");
                                    subElement.addAttribute(StringUtils.trim(keyVal[0]), StringUtils.trim(keyVal[1]));
                                }
                            }
                        } else {
                            subElement = root.addElement(entry.getKey());
                        }

                        subElement.addText((String) tmpList.get(i));
                    }

                }


            } else if (entry.getValue() instanceof String) {
                key = entry.getKey();
                if (entry.getKey().contains("$$")) {
                    key = entry.getKey().split("\\$\\$")[0];
                    subElement = root.addElement(key);

                    String attrStr = entry.getKey().split("\\$\\$")[1];
                    if (!!StringUtils.hasText(attrStr)) {
                        String[] attrs = attrStr.split(",");
                        for (String attr : attrs) {
                            String[] keyVal = attr.split("=");
                            subElement.addAttribute(StringUtils.trim(keyVal[0]), StringUtils.trim(keyVal[1]));
                        }
                    }
                } else {
                    subElement = root.addElement(entry.getKey());
                }

                subElement.addText((String) entry.getValue());
            } else if (entry.getValue() instanceof Map) {
                key = entry.getKey();
                if (entry.getKey().contains("$$")) {
                    key = entry.getKey().split("\\$\\$")[0];
                    subElement = root.addElement(key);
                    String attrStr = entry.getKey().split("\\$\\$")[1];
                    if (!!StringUtils.hasText(attrStr)) {
                        String[] attrs = attrStr.split(",");
                        for (String attr : attrs) {
                            String[] keyVal = attr.split("=");
                            subElement.addAttribute(StringUtils.trim(keyVal[0]), StringUtils.trim(keyVal[1]));
                        }
                    }
                } else {
                    subElement = root.addElement(entry.getKey());
                }
                mapToXml(subElement, (Map<String, Object>) entry.getValue());
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    public static String mapToXml(Map<String, Object> xmlMap) {
        StringBuilder xmlStr = new StringBuilder(1024);
        for (Map.Entry<String, Object> entry : xmlMap.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            String key = null;
            if (entry.getValue() instanceof List) {
//				List<Map<String, Object>> tmpList = (List<Map<String, Object>>) entry.getValue();
                List tmpList = (List) entry.getValue();
                if (tmpList.get(0) instanceof Map) {
                    for (int i = 0; i < tmpList.size(); i++) {
                        Map<String, Object> tmp = (Map<String, Object>) tmpList.get(i);
                        key = entry.getKey();
                        xmlStr.append("<");
                        if (entry.getKey().contains("$$")) {
                            key = entry.getKey().split("\\$\\$")[0];
                            xmlStr.append(key);
                            String attrStr = entry.getKey().split("\\$\\$")[1];
                            if (!!StringUtils.hasText(attrStr)) {
                                String[] attrs = attrStr.split(",");
                                for (String attr : attrs) {
                                    String[] keyVal = attr.split("=");
                                    xmlStr.append(" ").append(StringUtils.trim(keyVal[0]));
                                    xmlStr.append("=").append("\"").append(StringUtils.trim(keyVal[1])).append("\"");
                                }
                            }
                        } else {
                            xmlStr.append(entry.getKey());
                        }
                        xmlStr.append(">");
                        xmlStr.append(mapToXml((Map<String, Object>) tmp));
                        xmlStr.append("</").append(key).append(">");
                    }
                } else {
                    key = entry.getKey();
                    for (int i = 0; i < tmpList.size(); i++) {
                        xmlStr.append("<");
                        if (entry.getKey().contains("$$")) {
                            key = entry.getKey().split("\\$\\$")[0];
                            xmlStr.append(key);
                            String attrStr = entry.getKey().split("\\$\\$")[1];
                            if (!!StringUtils.hasText(attrStr)) {
                                String[] attrs = attrStr.split(",");
                                for (String attr : attrs) {
                                    String[] keyVal = attr.split("=");
                                    xmlStr.append(" ").append(StringUtils.trim(keyVal[0]));
                                    xmlStr.append("=").append("\"").append(StringUtils.trim(keyVal[1])).append("\"");
                                }
                            }
                        } else {
                            xmlStr.append(entry.getKey());
                        }
                        xmlStr.append(">");
                        xmlStr.append("<![CDATA[").append((String) tmpList.get(i)).append("]]>");
                        xmlStr.append("</").append(key).append(">");
                    }
                }
            } else if (entry.getValue() instanceof String) {
                key = entry.getKey();
                xmlStr.append("<");
                if (entry.getKey().contains("$$")) {
                    key = entry.getKey().split("\\$\\$")[0];
                    xmlStr.append(key);
                    String attrStr = entry.getKey().split("\\$\\$")[1];
                    if (!!StringUtils.hasText(attrStr)) {
                        String[] attrs = attrStr.split(",");
                        for (String attr : attrs) {
                            String[] keyVal = attr.split("=");
                            xmlStr.append(" ").append(StringUtils.trim(keyVal[0]));
                            xmlStr.append("=").append("\"").append(StringUtils.trim(keyVal[1])).append("\"");
                        }
                    }
                } else {
                    xmlStr.append(entry.getKey());
                }
                xmlStr.append(">");
                xmlStr.append("<![CDATA[").append((String) entry.getValue()).append("]]>");
                xmlStr.append("</").append(key).append(">");
            } else if (entry.getValue() instanceof Map) {
                key = entry.getKey();
                xmlStr.append("<");
                if (entry.getKey().contains("$$")) {
                    key = entry.getKey().split("\\$\\$")[0];
                    xmlStr.append(key);
                    String attrStr = entry.getKey().split("\\$\\$")[1];
                    if (!!StringUtils.hasText(attrStr)) {
                        String[] attrs = attrStr.split(",");
                        for (String attr : attrs) {
                            String[] keyVal = attr.split("=");
                            xmlStr.append(" ").append(StringUtils.trim(keyVal[0]));
                            xmlStr.append("=").append("\"").append(StringUtils.trim(keyVal[1])).append("\"");
                        }
                    }
                } else {
                    xmlStr.append(entry.getKey());
                }
                xmlStr.append(">");
                xmlStr.append(mapToXml((Map<String, Object>) entry.getValue()));
                xmlStr.append("</").append(key).append(">");
            }
        }
        return xmlStr.toString();
    }

    public static String readXmlFileAsString(String xmlPath) throws Exception {
        if (!FileUtils.isExist(xmlPath)) {
            throw new FileNotFoundException(StringUtils.formatString("xml文件%s不存在！", xmlPath));
        }
        Document doc = new SAXReader().read(new File(xmlPath));
        return doc.asXML();
    }

    public static boolean validate(String xmlStr, String schemaPath) {
        return validate(xmlStr, DEFAULT_CHARSET, schemaPath);
    }

    public static boolean validate(String xmlStr, String charsetName, String schemaPath) {
        try {
            return validate(xmlStr.getBytes(charsetName), schemaPath);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("ERROR:", e);
            return false;
        }
    }

    public static boolean validate(byte[] xmlStrBytes, String schemaPath) {
        try {
            SchemaFactory factory = new XMLSchemaFactory();
            Source xsdSource = new StreamSource(XmlUtils.class.getResourceAsStream(schemaPath));
            Schema schema = factory.newSchema(xsdSource);
            Validator validator = schema.newValidator();
            Source xmlSource = new StreamSource(new ByteArrayInputStream(xmlStrBytes));
            validator.validate(xmlSource);
            return true;
        } catch (Exception e) {
            LOGGER.error(StringUtils.formatString("schema 校验错误，错误为：%s", e.getMessage()));
            return false;
        }
    }

}
