package com.asascience.ncsos.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.InputStream;
import java.util.List;

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
            SAXBuilder builder = new SAXBuilder(false);
            // Create the XML document and return
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
                                                            String node) {
        Element fstNode = doc.getRootElement();
        if (!fstNode.getName().equals(container)) {
            fstNode = fstNode.getChild(container);
        }
        return fstNode.getChild(node);
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

    public static Element getNestedChild(Element base, String tagname) {
        if (base.getName().equals(tagname)) {
            return base;
        } else {
            for (Element e : (List<Element>)base.getChildren()) {
                Element x = XMLDomUtils.getNestedChild(e, tagname);
                if (x instanceof Element) {
                    return x;
                }
            }
        }
        return null;
    }

    public static Element getNestedChild(Element base, String tagname, Namespace namespace) {
        if (base.getName().equals(tagname) && base.getNamespace().equals(namespace)) {
            return base;
        } else {
            for (Element e : (List<Element>)base.getChildren()) {
                Element x = XMLDomUtils.getNestedChild(e, tagname, namespace);
                if (x instanceof Element) {
                    return x;
                }
            }
        }
        return null;
    }

}
