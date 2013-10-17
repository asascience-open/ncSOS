package com.asascience.ncsos.util;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import java.io.File;
import java.io.InputStream;
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
        Document doc = null;
        try {
            //File file = new File(templateFileLocation);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(templateFileLocation);
            doc.getDocumentElement().normalize();

            //setRouteElement(doc.getDocumentElement().getNodeName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }

    public static String getAttributeFromNode(Document doc, String routeSearch, 
    						String routeNamespace, String container, 
    						String containerNamespace, String attribute) {
        Element fstNmElmnt1 = getElementBaseOnContainer(doc, routeSearch, routeNamespace,
        												container, containerNamespace);
        String response = fstNmElmnt1.getAttribute(attribute);
        return response;
    }

    private static Element getElementBaseOnContainer(Document doc, String routeSearch,
    												String routeNamespace, 
    												String container, String containerNamespace) {
        NodeList serviceProviderNodeList = doc.getElementsByTagNameNS(routeNamespace, routeSearch);
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        NodeList fstNm1 = fstElmnt.getElementsByTagNameNS(containerNamespace, container);
        Element fstNmElmnt1 = (Element) fstNm1.item(0);
        return fstNmElmnt1;
    }

    public static void setAttributeFromNode(Document doc, String routeSearch, String routeNamespace,
    		String container, String containerNamespace, String attribute, String value) {
        Element fstNmElmnt1 = getElementBaseOnContainer(doc, routeSearch, routeNamespace,
        		container, containerNamespace);
        fstNmElmnt1.setAttribute((attribute), value);
    }

    public static void setNodeValue(Document doc, 
    								String container,
    								String containerNamespace,
    								String node, 
    								String nodeNamespace,
    								String value) {
        Element fstNmElmnt1 = getElementBaseOnContainerAndNode(doc, container, containerNamespace,
        														node, nodeNamespace);
        NodeList fstNm = fstNmElmnt1.getChildNodes();
        fstNm.item(0).setNodeValue(value);
    }
    
    
    /*
    public static void setNodeValue(Document doc, 
    								String container,
    								String containerNamespace,
    								String node, 
    								String nodeNamespace,
    								String value, 
    								int stationNumber) {
        Element fstNmElmnt1 = getElementBaseOnContainerAndNode(doc, container, containerNamespace,
        														node, nodeNamespace, stationNumber);
        NodeList fstNm = fstNmElmnt1.getChildNodes();
        fstNm.item(0).setNodeValue(value);
    }
    */

    private static Element getElementBaseOnContainerAndNode(Document doc, String container, 
    		String containerNamespace,
    		String node, 
    		String nodeNamespace,
    		int stationNumber) {
        NodeList serviceProviderNodeList = doc.getElementsByTagNameNS(containerNamespace, container);
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(stationNumber);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        NodeList fstNm1 = fstElmnt.getElementsByTagNameNS(nodeNamespace, node);
        Element fstNmElmnt1 = (Element) fstNm1.item(0);
        return fstNmElmnt1;
    }

    private static Element getElementBaseOnContainerAndNode(Document doc, 
    							String container,
    							String containerNamespace,
    							String node,
    							String nodeNamespace) {
    	 NodeList serviceProviderNodeList = null;
    	if(containerNamespace != null)
    		serviceProviderNodeList = doc.getElementsByTagNameNS(containerNamespace, container);
    	else 
    		serviceProviderNodeList = doc.getElementsByTagName( container);
    	
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        NodeList fstNm1 = null;
        if(nodeNamespace != null)
        	fstNm1 = fstElmnt.getElementsByTagNameNS(nodeNamespace, node);
        else
        	fstNm1 = fstElmnt.getElementsByTagName(node);

        Element fstNmElmnt1 = (Element) fstNm1.item(0);
        return fstNmElmnt1;
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
        NodeList fstNm = fstNmElmnt1.getChildNodes();
        return fstNm.item(0).getNodeValue();
    }


    public static String getNodeValue(Document doc, 
    		String container,
    		String node) {
    	return getNodeValue(doc, container, null, node, null);
    }

    public static void writeXMLDOMToFile(Document doc, String fileName) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // Prepare the output file
            File file = new File(fileName);
            Result result = new StreamResult(file);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static String getObsGMLIDAttributeFromNode(Document doc, 
    												  String container,
    												  String containerNamespace,
    												  String attribute) {
        NodeList serviceProviderNodeList = doc.getElementsByTagNameNS(containerNamespace, container);
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        String response = fstElmnt.getAttribute(attribute);
        return response;
    }

    public static void setObsGMLIDAttributeFromNode(Document doc, 
    												String container, 
    												String namespace, 
    												String attribute, 
    												String value) {
    	 setObsGMLIDAttributeFromNode(doc, container, namespace,
    			 					  attribute, null, value);
    }


    public static void setObsGMLIDAttributeFromNode(Document doc, 
    		String container, 
    		String namespace, 
    		String attribute, 
    		String attributeNamespace,
    		String value) {
    	NodeList serviceProviderNodeList = doc.getElementsByTagNameNS(namespace, container);
    	//get the first node in the the list matching the above name
    	Node fstNode = serviceProviderNodeList.item(0);
    	//create an element from the node
    	Element fstElmnt = (Element) fstNode;
    	if(attributeNamespace != null)
    		fstElmnt.setAttributeNS(attributeNamespace, (attribute), value);
    	else
    		fstElmnt.setAttribute((attribute), value);
    }
    public static Document getExceptionDom() {
        InputStream isTemplate = XMLDomUtils.class.getClassLoader().getResourceAsStream("templates/exception.xml");
        Document exceptionDom = getTemplateDom(isTemplate);
        return exceptionDom;
    }
    
    public static Document getExceptionDom(String exceptionMessage) {
        Document exceptionDom = getExceptionDom();
        setNodeValue(exceptionDom, "Exception", "", "ExceptionText", "", exceptionMessage);
        return exceptionDom;
    }

    public static void addObservation() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static Document addObservationElement(Document doc, 
    											 String obsListName, 
    											 String obsListNamespace,
    											 String obsName,
    											 String obsNamespace) {
        NodeList obsOfferingList = doc.getElementsByTagNameNS(obsListNamespace, obsListName);
        Element obsOfferEl = (Element) obsOfferingList.item(0);

        obsOfferEl.appendChild(doc.createElementNS(obsName, obsNamespace));
        return doc;
    }

    public static Element checkNodeExists(Document doc, String container, 
    									 String containerNamespace,
    									 String node, String nodeNamespace) {
        Element fstNmElmnt1 = getElementBaseOnContainerAndNode(doc, container, containerNamespace,
        														node, nodeNamespace);
        return fstNmElmnt1;
    }

    //METHOD OVERRIDE-------------------------------------------------------------
    public static Document addNode() {
        return null;
    }
    
    public static Document addNode(Document doc, 
    							   String parentName, 
    							   String parentNamespace,
    							   Node childNode) {
        NodeList parentList = doc.getElementsByTagNameNS(parentNamespace, parentName);
        Node parent = parentList.item(0);
        parent.appendChild(childNode);
        return doc;
    }
    
    public static Document addNode(Document doc, String Obs, 
    								String obsNamespace, 
    								String nodeName,
    								String nodeNamespace,
    								String value, 
    								int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagNameNS(obsNamespace, Obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);

        Element obsOfferingNode = doc.createElementNS(nodeNamespace, nodeName);
        obsOfferingNode.appendChild(doc.createTextNode(value));
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }

    public static Document addNode(Document doc, 
    							   String obs, 
    							   String obsNamespace,
    							   String nodeName, 
    							   String nodeNamespace,
    							   int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagNameNS(obsNamespace, obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);
        Element obsOfferingNode = doc.createElementNS(nodeNamespace, nodeName);
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }

    public static Document addNode(Document doc, 
    							   String obs, 
    							   String obsNamespace,
    							   String nodeaddingto, 
    							   String newNode, 
    							   String newNodeNamespace,
    							   String atrributeName, 
    							   String value, 
    							   int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagNameNS(obsNamespace, obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);
        NodeList nodes = obsOfferEl.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node nodeListNode = nodes.item(i);
            if (nodeListNode.getNodeName().equalsIgnoreCase(nodeaddingto)) {

                Element obsOfferEl1 = (Element) nodes.item(i);
                Element obsOfferingNode = doc.createElementNS(newNodeNamespace, newNode);
                obsOfferingNode.setAttribute(atrributeName, value);
                obsOfferEl1.appendChild(obsOfferingNode);
            }
        }
        return doc;
    }

    public static Document addNodeAtLocation(Document doc, 
    								String obsLocation, 
    								String obsLocationNamespace,
    								String obs, 
    								String obsNamespace,
    								String nodeName, 
    								String nodeNamespace,
    								String value, 
    								int stationNumber) {
        NodeList obsOfferingList1 = doc.getElementsByTagNameNS(obsLocationNamespace, obsLocation);
        Element obsOfferEl111 = (Element) obsOfferingList1.item(stationNumber);
        NodeList obsOfferingList = obsOfferEl111.getElementsByTagNameNS(obsNamespace, obs);
        Element obsOfferEl = (Element) obsOfferingList.item(0);
        Element obsOfferingNode = doc.createElementNS(nodeNamespace, nodeName);
        obsOfferingNode.appendChild(doc.createTextNode(value));
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }
    
    public static Document addNode(Document doc, 
    		String parentNodeName, String parentNs,
    		String nodeNameToInsert, String insertNodeNs,
    		String nodeNameToInsertBefore, String insertBeforeNodeNs) {
        NodeList nodeList = doc.getElementsByTagNameNS(parentNs, parentNodeName);
        Element parentEl = (Element)nodeList.item(0);
        nodeList = parentEl.getElementsByTagNameNS(insertBeforeNodeNs, nodeNameToInsertBefore);
        Element existingEl = (Element)nodeList.item(0);
        Element newNode = doc.createElementNS(insertNodeNs, nodeNameToInsert);
        parentEl.insertBefore(newNode, existingEl);
        return doc;
    }
    
    public static Document setNodeAttribute(Document doc, 
    										String nodeName,
    										String nodeNamespace,
    										String attributeName, 
    										String attributeValue) {
        NodeList nodeList = doc.getElementsByTagNameNS(nodeNamespace, nodeName);
        Element el = (Element)nodeList.item(0);
        el.setAttribute(attributeName, attributeValue);
        return doc;
    }

    public static Document setNodeAttribute(Document doc, 
    		String nodeName,
    		String nodeNamespace,
    		String attributeName,
    		String attributeNamespace,
    		String attributeValue) {
    	NodeList nodeList = doc.getElementsByTagNameNS(nodeNamespace, nodeName);
    	Element el = (Element)nodeList.item(0);
    	el.setAttributeNS(attributeNamespace, attributeName, attributeValue);
    	return doc;
    }

    //METHOD OVERRIDE-------------------------------------------------------------
    public static Document addNodeAndAttribute(Document doc, 
    										   String obs, 
    										   String obsNamespace,
    										   String nodeName, 
    										   String nodeNamespace,
    										   String attribute, 
    										   String value, 
    										   int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagNameNS(obsNamespace, obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);

        Element obsOfferingNode = doc.createElementNS(nodeNamespace, nodeName);
        obsOfferingNode.setAttribute(attribute, value);
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }

    /*
     * add node to attribute with allocation number
     */
    public static Document addNodeAndAttribute(Document doc, 
    										   String obs, 
    										   String obsNamespace,
    										   String nodeName, 
    										   String nodeNamespace, 
    										   int index, 
    										   String attribute, 
    										   String value, 
    										   int stationNumber,
    										   String dataRecordName,
    										   String dataRecordNamespace) {
        NodeList obsOfferingList1 = doc.getElementsByTagNameNS(dataRecordName, dataRecordNamespace);
        Element obsOfferEl1 = (Element) obsOfferingList1.item(stationNumber);

        NodeList obsOfferingList = obsOfferEl1.getElementsByTagNameNS(obsNamespace, obs);

        Element obsOfferEl = (Element) obsOfferingList.item(index);

        Element obsOfferingNode = doc.createElementNS(nodeNamespace, nodeName);
        obsOfferingNode.setAttribute(attribute, value);
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }
}
