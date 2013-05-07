/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 *
 * @author SCowan
 */
public class BaseOutputFormatter implements SOSOutputFormatter {
    protected class SubElement {
        public HashMap<String,String> attributes;
        public String tag;
        public String textContent;
        
        public SubElement(String tag) {
            this.attributes = new HashMap<String, String>();
            this.textContent = null;
            this.tag = tag;
        }
    }
    
    protected String DEFAULT_VALUE = "UNKNOWN";
    protected Document document;
    
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DescribeSensorFormatter.class);
    
    /** Public Methods **/

    //<editor-fold defaultstate="collapsed" desc="interface methods">
    public void addDataFormattedStringToInfoList(String dataFormattedString) {}

    public void emtpyInfoList() {}

    public void setupExceptionOutput(String message) {
        this.document = XMLDomUtils.getExceptionDom(message);
    }

    public void writeOutput(Writer writer) {
//        try {
//            Transformer tf = TransformerFactory.newInstance().newTransformer();
//            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//            tf.setOutputProperty(OutputKeys.INDENT, "yes");
//            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//            tf.transform(new DOMSource(this.document), new StreamResult(writer));
//        } catch (Exception ex) {
//            logger.error(ex.toString());
//            logger.error("Using DOMImplementation for transformer");
            try {
                DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
                DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
                // output our document to the writer
                LSSerializer xmlSerializer = impl.createLSSerializer();
                LSOutput xmlOut = impl.createLSOutput();
                xmlSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
                xmlOut.setCharacterStream(writer);
                xmlSerializer.write(this.document, xmlOut);
            } catch (Exception ex2) {
                logger.error(ex2.getMessage());
            }
//        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Protected Methods">
    protected void loadTemplateXML(String templateLocation) {
        InputStream templateInputStream = null;
        try {
            templateInputStream = getClass().getClassLoader().getResourceAsStream(templateLocation);
            this.document = XMLDomUtils.getTemplateDom(templateInputStream);
        } catch (Exception ex) {
            logger.error(ex.toString());
        } finally {
            if (templateInputStream != null) {
                try {
                    templateInputStream.close();
                } catch (IOException e) {
                    // ignore, closing..
                    logger.error(e.toString());
                }
            }
        }
    }
    
    protected Element createSubElement(SubElement element) {
        Element retval = this.document.createElement(element.tag);
        if (element.textContent != null) {
            retval.setTextContent(element.textContent);
        }
        for (Map.Entry<String,String> entry : element.attributes.entrySet()) {
            retval.setAttribute(entry.getKey(), entry.getValue());
        }
        return retval;
    }
    
    protected Element getParentNode() {
        return (Element) document.getFirstChild();
    }
    
    protected Element addNewNode(String parentName, String nodeName) {
        Element parent = (Element) this.document.getElementsByTagName(parentName).item(0);
        return addNewNode(parent, nodeName);
    }
    
    protected Element addNewNode(Node parent, String nodeName) {
        Element child = this.document.createElement(nodeName);
        parent.appendChild(child);
        return child;
    }
    
    protected Element addNewNode(String parentName, String nodeName, String textValue) {
        Element parent = (Element) this.document.getElementsByTagName(parentName).item(0);
        return addNewNode(parent, nodeName, textValue);
    }
    
    protected Element addNewNode(Node parent, String nodeName, String textValue) {
        Element child = this.document.createElement(nodeName);
        child.setTextContent(textValue);
        parent.appendChild(child);
        return child;
    }
    
    protected Element addNewNode(String parentName, String nodeName, String attrName, String attrValue) {
        Element parent = (Element) this.document.getElementsByTagName(parentName).item(0);
        return addNewNode(parent, nodeName, attrName, attrValue);
    }
    
    protected Element addNewNode(Node parent, String nodeName, String attrName, String attrValue) {
        Element child = this.document.createElement(nodeName);
        child.setAttribute(attrName, attrValue);
        parent.appendChild(child);
        return child;
    }
    //</editor-fold>
    
    
    /** Private Methods **/
}
