package com.asascience.ncsos.util;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * this class hold a number of the XML DOM utils
 *
 * @author abird
 */
public class XMLDomUtils {


    public static Document loadFile(InputStream filestream) {
        Document doc = null;
        try {
            // Build the document with SAX and Xerces, with validation
            SAXBuilder builder = new SAXBuilder(true);
            // Create the JSON document and return
            doc = builder.build(filestream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public static Document loadFile(String filepath) {
        Document doc = null;
        try {
            // Build the document with SAX and Xerces, no validation
            SAXBuilder builder = new SAXBuilder();
            // Create the JSON document and return
            doc = builder.build(new File(filepath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public static Document getTemplateDom(InputStream templateFileLocation) {
        return XMLDomUtils.loadFile(templateFileLocation);
    }

    private static Element getElementBaseOnContainerAndNode(Document doc, 
    							String container,
    							String containerNamespace,
    							String node,
    							String nodeNamespace) {

        Element fstNode = (Element)doc.getRootElement().getChild(container, Namespace.getNamespace(containerNamespace));
        return fstNode.getChild(node, Namespace.getNamespace(nodeNamespace));
    }

    public static String getNodeValue(Document doc, 
    								String container,
    								String containerNamespace,
    								String node,
    								String nodeNamespace) {
        Element fstNmElmnt1 = getElementBaseOnContainerAndNode(doc, container,
        													containerNamespace, 
        													node,
        													nodeNamespace);
        Element fstNm = (Element)fstNmElmnt1.getContent(0);
        return fstNm.getValue();
    }


    public static String getNodeValue(Document doc, 
    		String container,
    		String node) {
    	return getNodeValue(doc, container, null, node, null);
    }

    public static Document getExceptionDom() {
        InputStream isTemplate = XMLDomUtils.class.getClassLoader().getResourceAsStream("templates/exception.xml");
        return XMLDomUtils.loadFile(isTemplate);
    }
    
    public static Document getExceptionDom(String exceptionMessage) {
        Document exceptionDom = getExceptionDom();
        exceptionDom.getRootElement().getChild("Exception").getChild("ExceptionText").setText(exceptionMessage);
        return exceptionDom;
    }

    public static Document addObservationElement(Document doc, 
    											 String obsListName,
                                                 Namespace obsListNamespace,
    											 String obsName,
                                                 Namespace obsNamespace) {
        Element obsOfferEl = doc.getRootElement().getChild(obsListName, obsListNamespace);
        obsOfferEl.addContent(new Element(obsName, obsNamespace));
        return doc;
    }

    public static Document addNode(Document doc, 
    							   String obs,
                                   Namespace obsNamespace,
    							   String nodeName,
                                   Namespace nodeNamespace,
    							   int stationNumber) {
        List<Element> obsOfferingList = doc.getRootElement().getChildren(obs, obsNamespace);
        Element obsOfferEl = obsOfferingList.get(stationNumber);
        Element obsOfferingNode = new Element(nodeName, nodeNamespace);
        obsOfferEl.addContent(obsOfferingNode);
        return doc;
    }

    
    public static Document addNode(Document doc,
    		String parentNodeName, Namespace parentNs,
    		String nodeNameToInsert, Namespace insertNodeNs,
    		String nodeNameToInsertBefore, Namespace insertBeforeNodeNs) {
        Element parentEl = doc.getRootElement().getChild(parentNodeName, parentNs);
        Element existingEl = parentEl.getChild(nodeNameToInsertBefore, insertBeforeNodeNs);
        Element newNode = new Element(nodeNameToInsert, insertNodeNs);
        parentEl.addContent(parentEl.indexOf(existingEl) - 1, newNode);
        return doc;
    }

    public static Document addNode(Document doc, String Obs,
                                   Namespace obsNamespace,
                                   String nodeName,
                                   Namespace nodeNamespace,
                                   String value,
                                   int stationNumber) {
        Element obsOfferEl = (Element)doc.getRootElement().getChildren(Obs, obsNamespace).get(stationNumber);
        Element obsOfferingNode = new Element(nodeName, nodeNamespace);
        obsOfferingNode.setText(value);
        obsOfferEl.addContent(obsOfferingNode);
        return doc;
    }

    public static Document setNodeAttribute(Document doc, 
    		String nodeName,
            Namespace nodeNamespace,
    		String attributeName,
            Namespace attributeNamespace,
    		String attributeValue) {
    	Element el = doc.getRootElement().getChild(nodeName, nodeNamespace);
    	el.setAttribute(attributeName, attributeValue, attributeNamespace);
    	return doc;
    }

}
