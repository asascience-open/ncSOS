package thredds.server.sos.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
//TODO add in refactored code for utils here!

    public static Document getTemplateDom(InputStream templateFileLocation) {
        Document doc = null;
        try {
            //File file = new File(templateFileLocation);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(templateFileLocation);
            doc.getDocumentElement().normalize();
            //setRouteElement(doc.getDocumentElement().getNodeName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }

    public static String getAttributeFromNode(Document doc, String routeSearch, String container, String attribute) {
        Element fstNmElmnt1 = getElementBaseOnContainer(doc, routeSearch, container);
        String response = fstNmElmnt1.getAttribute(attribute);
        return response;
    }

    private static Element getElementBaseOnContainer(Document doc, String routeSearch, String container) {
        NodeList serviceProviderNodeList = doc.getElementsByTagName(routeSearch);
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        NodeList fstNm1 = fstElmnt.getElementsByTagName(container);
        Element fstNmElmnt1 = (Element) fstNm1.item(0);
        return fstNmElmnt1;
    }

    public static void setAttributeFromNode(Document doc, String routeSearch, String container, String attribute, String value) {
        Element fstNmElmnt1 = getElementBaseOnContainer(doc, routeSearch, container);
        fstNmElmnt1.setAttribute((attribute), value);
    }

    public static void setNodeValue(Document doc, String container, String node, String value) {
        Element fstNmElmnt1 = getElementBaseOnContainerAndNode(doc, container, node);
        NodeList fstNm = fstNmElmnt1.getChildNodes();
        fstNm.item(0).setNodeValue(value);
    }

    public static void setNodeValue(Document doc, String container, String node, String value, int stationNumber) {
        Element fstNmElmnt1 = getElementBaseOnContainerAndNode(doc, container, node, stationNumber);
        NodeList fstNm = fstNmElmnt1.getChildNodes();
        fstNm.item(0).setNodeValue(value);
    }

    private static Element getElementBaseOnContainerAndNode(Document doc, String container, String node, int stationNumber) {
        NodeList serviceProviderNodeList = doc.getElementsByTagName(container);
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(stationNumber);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        NodeList fstNm1 = fstElmnt.getElementsByTagName(node);
        Element fstNmElmnt1 = (Element) fstNm1.item(0);
        return fstNmElmnt1;
    }

    private static Element getElementBaseOnContainerAndNode(Document doc, String container, String node) {
        NodeList serviceProviderNodeList = doc.getElementsByTagName(container);
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        NodeList fstNm1 = fstElmnt.getElementsByTagName(node);
        Element fstNmElmnt1 = (Element) fstNm1.item(0);
        return fstNmElmnt1;
    }

    public static String getNodeValue(Document doc, String container, String node) {
        Element fstNmElmnt1 = getElementBaseOnContainerAndNode(doc, container, node);
        NodeList fstNm = fstNmElmnt1.getChildNodes();
        return fstNm.item(0).getNodeValue();
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

    public static String getObsGMLIDAttributeFromNode(Document doc, String container, String attribute) {
        NodeList serviceProviderNodeList = doc.getElementsByTagName(container);
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        String response = fstElmnt.getAttribute(attribute);
        return response;
    }

    public static void setObsGMLIDAttributeFromNode(Document doc, String container, String attribute, String value) {
        NodeList serviceProviderNodeList = doc.getElementsByTagName(container);
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        fstElmnt.setAttribute((attribute), value);
    }

    public static Document getExceptionDom() {
        InputStream isTemplate = XMLDomUtils.class.getClassLoader().getResourceAsStream("templates/exception.xml");
        Document exceptionDom = getTemplateDom(isTemplate);
        return exceptionDom;
    }

    public static void addObservation() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static Document addObservationElement(Document doc) {
        NodeList obsOfferingList = doc.getElementsByTagName("om:member");
        Element obsOfferEl = (Element) obsOfferingList.item(0);

        obsOfferEl.appendChild(doc.createElement("om:Observation"));
        return doc;
    }

    public static Element checkNodeExists(Document doc, String container, String node) {
        Element fstNmElmnt1 = getElementBaseOnContainerAndNode(doc, container, node);
        return fstNmElmnt1;
    }

    //METHOD OVERRIDE-------------------------------------------------------------
    public static Document addNodeAllOptions() {
        return null;
    }

    public static Document addNodeAllOptions(Document doc, String Obs, String nodeName, String value, int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagName(Obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);

        Element obsOfferingNode = doc.createElement(nodeName);
        obsOfferingNode.appendChild(doc.createTextNode(value));
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }

    public static Document addNodeAllOptions(Document doc, String obs, String nodeName, int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagName(obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);
        Element obsOfferingNode = doc.createElement(nodeName);
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }

    public static Document addNodeAllOptions(Document doc, String obs, String nodeaddingto, String newNode, String atrributeName, String value, int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagName(obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);
        NodeList nodes = obsOfferEl.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node nodeListNode = nodes.item(i);
            if (nodeListNode.getNodeName().equalsIgnoreCase(nodeaddingto)) {

                Element obsOfferEl1 = (Element) nodes.item(i);
                Element obsOfferingNode = doc.createElement(newNode);
                obsOfferingNode.setAttribute(atrributeName, value);
                obsOfferEl1.appendChild(obsOfferingNode);
            }
        }
        return doc;
    }

    public static Document addNodeAllOptions(Document doc, String obsLocation, String obs, String nodeName, String value, int stationNumber) {
        NodeList obsOfferingList1 = doc.getElementsByTagName(obsLocation);
        Element obsOfferEl111 = (Element) obsOfferingList1.item(stationNumber);
        NodeList obsOfferingList = obsOfferEl111.getElementsByTagName(obs);
        Element obsOfferEl = (Element) obsOfferingList.item(0);
        Element obsOfferingNode = doc.createElement(nodeName);
        obsOfferingNode.appendChild(doc.createTextNode(value));
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }

    //METHOD OVERRIDE-------------------------------------------------------------
    @Deprecated
    public static Document addNodeAndValue(Document doc, String Obs, String nodeName, String value, int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagName(Obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);

        Element obsOfferingNode = doc.createElement(nodeName);
        obsOfferingNode.appendChild(doc.createTextNode(value));
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }

    //adds a node with no value ie a container
    @Deprecated
    public static Document addNode(Document doc, String obs, String nodeName, int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagName(obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);
        Element obsOfferingNode = doc.createElement(nodeName);
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }

    public static Document addNodeAndAttribute(Document doc, String obs, String nodeName, String attribute, String value, int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagName(obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);

        Element obsOfferingNode = doc.createElement(nodeName);
        obsOfferingNode.setAttribute(attribute, value);
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }

    @Deprecated
    public static Document addNodeToNodeAndValue(Document doc, String obs, String nodeName, String value, int stationNumber) {
        NodeList obsOfferingList1 = doc.getElementsByTagName("om:Observation");
        Element obsOfferEl111 = (Element) obsOfferingList1.item(stationNumber);

        NodeList obsOfferingList = obsOfferEl111.getElementsByTagName(obs);
        Element obsOfferEl = (Element) obsOfferingList.item(0);

        Element obsOfferingNode = doc.createElement(nodeName);
        obsOfferingNode.appendChild(doc.createTextNode(value));

        obsOfferEl.appendChild(obsOfferingNode);

        return doc;
    }

    @Deprecated
    public static Document addNodeToNodeAndAttribute(Document doc, String obs, String nodeaddingto, String newNode, String atrributeName, String value, int stationNumber) {
        NodeList obsOfferingList = doc.getElementsByTagName(obs);
        Element obsOfferEl = (Element) obsOfferingList.item(stationNumber);

        NodeList nodes = obsOfferEl.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node nodeListNode = nodes.item(i);
            if (nodeListNode.getNodeName().equalsIgnoreCase(nodeaddingto)) {

                Element obsOfferEl1 = (Element) nodes.item(i);
                Element obsOfferingNode = doc.createElement(newNode);
                obsOfferingNode.setAttribute(atrributeName, value);
                obsOfferEl1.appendChild(obsOfferingNode);
            }
        }
        return doc;
    }

    /*
     * add node to attribute with allocation number
     */
    public static Document addNodeAndAttribute(Document doc, String obs, String nodeName, int index, String attribute, String value, int stationNumber) {
        NodeList obsOfferingList1 = doc.getElementsByTagName("swe:DataRecord");
        Element obsOfferEl1 = (Element) obsOfferingList1.item(stationNumber);

        NodeList obsOfferingList = obsOfferEl1.getElementsByTagName(obs);

        Element obsOfferEl = (Element) obsOfferingList.item(index);

        Element obsOfferingNode = doc.createElement(nodeName);
        obsOfferingNode.setAttribute(attribute, value);
        obsOfferEl.appendChild(obsOfferingNode);
        return doc;
    }
}
