/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ucar.nc2.VariableSimpleIF;
import ucar.unidata.geoloc.LatLonRect;

/** "sensorML/1.0.1"
 * Class that formats the output for describe sensor requests. Holds an instance
 * of w3c document model for printing the xml response following the 
 * "sensorML/1.0.1" standard.
 * @author SCowan
 * @version 1.0.0
 */
public class DescribeSensorFormatter implements SOSOutputFormatter {
    
    private Document document;
    
    private final String TEMPLATE = "templates/sosDescribeSensor.xml";
    private final String uri;
    private final String query;
    
    /**
     * Creates a new formatter instance that uses the sosDescribeSensor.xml as a
     * template (found in the resources templates folder)
     */
    public DescribeSensorFormatter() {
        document = parseTemplateXML();
        this.uri = this.query = null;
    }

    /**
     * Creates a new formatter instance that uses the sosDescribeSensor.xml as a
     * template (found in the resources templates folder)
     * @param uri the uri of the request (used to construct hrefs for components
     * @param query the query of the request (used to construct hrefs for components)
     */
    public DescribeSensorFormatter(String uri, String query) {
        document = parseTemplateXML();
        this.uri = uri;
        this.query = query;
    }
    
    /**
     * The w3c DOM document that details the response for the request
     * @return w3c DOM document
     */
    public Document getDocument() {
        return document;
    }
    
    /*********************/
    /* Interface Methods */
    /**************************************************************************/

    public void AddDataFormattedStringToInfoList(String dataFormattedString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void EmtpyInfoList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setupExceptionOutput(String message) {
        document = XMLDomUtils.getExceptionDom(message);
    }

    public void writeOutput(Writer writer) {
        // output our document to the writer
        DOMSource domSource = new DOMSource(document);
        Result result = new StreamResult(writer);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(domSource, result);
        } catch (Exception e) {
            System.out.println("Error in writing GetCapsOutputter - " + e.getMessage());
        }
    }
    
    /**************************************************************************/
    
    /*******************
     * Private Methods *
     *******************/
    
    private Document parseTemplateXML() {
        InputStream templateInputStream = null;
        try {
            templateInputStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE);
            return XMLDomUtils.getTemplateDom(templateInputStream);
        } finally {
            if (templateInputStream != null) {
                try {
                    templateInputStream.close();
                } catch (IOException e) {
                    // ignore, closing..
                }
            }
        }
    }
    
    private Element getParentNode() {
        return (Element) document.getElementsByTagName("sml:System").item(0);
    }
    
    private String joinArray(String[] arrayToJoin, String adjoiningChar) {
        StringBuilder retval = new StringBuilder();
        for (String str : arrayToJoin) {
            retval.append(str).append(adjoiningChar);
        }
        // remove last adjoined char
        retval.delete(retval.length() - adjoiningChar.length(), retval.length());
        return retval.toString();
    }
    
    private Element AddNewNodeToParent(String nameOfNewNode, Element parentNode) {
        Element retval = document.createElement(nameOfNewNode);
        parentNode.appendChild(retval);
        return retval;
    }
    
    private Element AddNewNodeToParentWithAttribute(String nameOfNewNode, Element parentNode, String attributeName, String attributeValue) {
        Element retval = document.createElement(nameOfNewNode);
        retval.setAttribute(attributeName, attributeValue);
        parentNode.appendChild(retval);
        return retval;
    }
    
    private Element AddNewNodeToParentWithTextValue(String nameOfNewNode, Element parentNode, String textContentValue) {
        Element retval = document.createElement(nameOfNewNode);
        retval.setTextContent(textContentValue);
        parentNode.appendChild(retval);
        return retval;
    }
    
    private void addCoordinateInfoNode(HashMap<String,String> coordInfo, Element parentNode, String defName, String defAxis, String defUnit) {
        Element lat = AddNewNodeToParentWithAttribute("swe:coordinate", parentNode, "name", defName);
        if (coordInfo.containsKey("name"))
            lat.setAttribute("name", coordInfo.get("name").toString());
        lat = AddNewNodeToParentWithAttribute("swe:Quantity", lat, "axisID", defAxis);
        if (coordInfo.containsKey("axisID"))
            lat.setAttribute("axisID", coordInfo.get("axisID").toString());
        Element unit = AddNewNodeToParentWithAttribute("swe:uom", lat, "code", defUnit);
        if (coordInfo.containsKey("code"))
            unit.setAttribute("code", coordInfo.get("code").toString());
        if (coordInfo.containsKey("value"))
            AddNewNodeToParentWithTextValue("swe:value", lat, coordInfo.get("value").toString());
    }
    
    /****************************/
    /* Public Methods           */
    /* Setting the XML document */
    /**************************************************************************/
    
    /**
     * Accessor function for setting the system id in the sml:System node
     * @param id usually a string following the format "station-[station_name]" (or sensor)
     */    
    public void setSystemId(String id) {
        Element system = (Element) document.getElementsByTagName("sml:System").item(0);
        system.setAttribute("gml:id", id);
    }
    
    /**
     * Accessor function for setting the description text of the gml:description node
     * @param description usually the description attribute value of a netcdf dataset
     */
    public void setDescriptionNode(String description) {
        // get our description node and set its string content
        document.getElementsByTagName("gml:description").item(0).setTextContent(description);
    }
    
    /**
     * Removes the gml:description node from the xml document
     */
    public void deleteDescriptionNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("gml:description").item(0));
    }
    
    /**
     * Accessor for setting the sml:identification node with Attributes from the
     * station/sensor Variable. The parameters are expected to have the same lengths
     * as one another.
     * @param names collection of names from the Variable's Attributes
     * @param definitions collection of definitions for the name-value pairs (the
     * sml:Term of the Attribute)
     * @param values collections of the values of the Variable's Attributes
     */
    public void setIdentificationNode(String[] names, String[] definitions, String[] values) {
        if (names.length != definitions.length && names.length != values.length) {
            setupExceptionOutput("invalid formatting of station attributes");
            return;
        }
        // get our Identifier List and add nodes to it
        Element pList = (Element) document.getElementsByTagName("sml:IdentifierList").item(0);
        for (int i=0; i<names.length; i++) {
            Element parent = AddNewNodeToParentWithAttribute("sml:identifier", pList, "name", names[i]);
            parent = AddNewNodeToParentWithAttribute("sml:Term", parent, "definition", definitions[i]);
            AddNewNodeToParentWithTextValue("sml:value", parent, values[i]);
        }
    }
    
    /**
     * Removes the sml:identification node from the xml document
     */
    public void deleteIdentificationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:identification").item(0));
    }
    
    /**
     * Accessor for setting the sml:classification node in the xml document
     * @param classifierName name of the station classification (eg 'platformType')
     * @param definition sml:Term definition of the classification
     * @param classifierValue value of the classification (eg 'GLIDER')
     */
    public void addToClassificationNode(String classifierName, String definition, String classifierValue) {
        Element parent = (Element) getParentNode().getElementsByTagName("sml:ClassifierList").item(0);
        parent = AddNewNodeToParentWithAttribute("sml:classifier", parent, "name", classifierName);
        parent = AddNewNodeToParentWithAttribute("sml:Term", parent, "definition", definition);
        AddNewNodeToParentWithTextValue("sml:value", parent, classifierValue);
    }
    
    /**
     * Removes the sml:classification node from the xml document
     */
    public void deleteClassificationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:classification").item(0));
    }
    
    /**
     * Accessor method to add a contact node to the sml:System node structure. Most
     * information is gathered from global Attributes of the dataset
     * @param role the role of the contact (eg publisher)
     * @param organizationName the name of the orginization or individual
     * @param contactInfo info for contacting the...um...contact
     */
    public void addContactNode(String role, String organizationName, HashMap<String, HashMap<String, String>> contactInfo) {
        // setup and and insert a contact node (after history)
        document = XMLDomUtils.addNodeBeforeNode(document, "sml:System", "sml:contact", "sml:history");
        NodeList contacts = getParentNode().getElementsByTagName("sml:contact");
        Element contact = null;
        for (int i=0; i<contacts.getLength(); i++) {
            if (!contacts.item(i).hasAttributes()) {
                contact = (Element) contacts.item(i);
            }
        }
        contact.setAttribute("xlink:role", role);
        /* *** */
        Element parent = AddNewNodeToParent("sml:ResponseibleParty", contact);
        /* *** */
        AddNewNodeToParentWithTextValue("sml:organizationName", parent, organizationName);
        /* *** */
        parent = AddNewNodeToParent("sml:contactInfo", parent);
        /* *** */
        // super nesting for great justice
        if (contactInfo != null) {
            for (String key : contactInfo.keySet()) {
                // add key as node
                Element sparent = AddNewNodeToParent(key, parent);
                HashMap<String, String> vals = (HashMap<String, String>)contactInfo.get(key);
                for (String vKey : vals.keySet()) {
                    AddNewNodeToParentWithTextValue(vKey, sparent, vals.get(vKey).toString());
                }
            }
        }
    }
    
    /**
     * Removes the first contact node instance from the xml document
     */
    public void deleteContactNodeFirst() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:contact").item(0));
    }
    
    /**
     * Accessor method to set the sml:history node in the xml document
     * @param history the Attribute value of the 
     */
    public void setHistoryEvents(String history) {
        Element parent = (Element) document.getElementsByTagName("sml:history").item(0);
        parent.setTextContent(history);
    }
    
    /**
     * Removes the sml:history node from the xml document
     */
    public void deleteHistoryNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:history").item(0));
    }
    
    /**
     * Accessor method for adding a gml:Point node to the sml:location node in the
     * xml document
     * @param stationName name of the station
     * @param coords lat, lon of the station's location
     */
    public void setLocationNode(String stationName, double[] coords) {
        Element parent = (Element) getParentNode().getElementsByTagName("sml:location").item(0);
        
        parent = AddNewNodeToParentWithAttribute("gml:Point", parent, "gml:id", "STATION-LOCATION-" + stationName);
        AddNewNodeToParentWithTextValue("gml:coordinates", parent, coords[0] + " " + coords[1]);
    }
    
    /**
     * Accessor method for adding a gml:boundedBy node to the sml:location node
     * in the xml document
     * @param lowerPoint lat, lon of the lower corner of the bounding box
     * @param upperPoint lat, lon of the upper corner of the bounding box
     */
    public void setLocationNodeWithBoundingBox(String[] lowerPoint, String[] upperPoint) {
        if (lowerPoint.length != 2 || upperPoint.length != 2)
            throw new IllegalArgumentException("lowerPoint or upperPoint are not valid");
        
        Element parent = (Element) getParentNode().getElementsByTagName("sml:location").item(0);
        
        parent = AddNewNodeToParent("gml:boundedBy", parent);
        parent = AddNewNodeToParentWithAttribute("gml:Envelope", parent, "srsName", "");
        AddNewNodeToParentWithTextValue("gml:lowerCorner", parent, lowerPoint[0] + " " + lowerPoint[1]);
        AddNewNodeToParentWithTextValue("gml:upperCorner", parent, upperPoint[0] + " " + upperPoint[1]);
    }
    
    /**
     * Removes the sml:location node from the xml document
     */
    public void deleteLocationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:location").item(0));
    }
    
    /**
     * 
     * @param dataVariables
     * @param procedure
     */
    public void setComponentsNode(List<VariableSimpleIF> dataVariables, String procedure) {
        // iterate through our list and create the node
        Element parent = (Element) document.getElementsByTagName("sml:ComponentList").item(0);

        for (int i=0; i<dataVariables.size(); i++) {
            String fName = dataVariables.get(i).getFullName();
            // component node
            Element component = document.createElement("sml:component");
            component.setAttribute("name", "Sensor " + fName);
            // system node
            Element system = document.createElement("sml:System");
            system.setAttribute("gml:id", "sensor-" + fName);
            // identification node
            Element ident = document.createElement("sml:identification");
            ident.setAttribute("xlink:href", procedure + "::" + fName);
            // documentation (url) node
            Element doc = document.createElement("sml:documentation");
            // need to construct url for sensor request
            String url = this.uri;
            String[] reqParams = this.query.split("&");
            // look for procedure
            for (int j=0; j<reqParams.length; j++) {
                if (reqParams[j].contains("procedure"))
                    // add sensor
                    reqParams[j] += "::" + fName;
            }
            // rejoin
            url += "?" + joinArray(reqParams, "&");
            doc.setAttribute("xlink:href", url);
            // description
            Element desc = document.createElement("gml:description");
            desc.setTextContent(dataVariables.get(i).getDescription());
            // add all nodes
            system.appendChild(ident);
            system.appendChild(doc);
            system.appendChild(desc);
            component.appendChild(system);
            parent.appendChild(component);
        }
    }
    
    /**
     * 
     */
    public void deleteComponentsNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:components").item(0));
    }
    
    /**
     * 
     * @param name
     */
    public void setPositionName(String name) {
        Element position = (Element) getParentNode().getElementsByTagName("sml:position").item(0);
        position.setAttribute("name", name);
    }
    
    /**
     * 
     * @param fieldMap
     * @param decimalSeparator
     * @param blockSeparator
     * @param tokenSeparator
     */
    public void setPositionDataDefinition(HashMap<String, HashMap<String, String>> fieldMap, String decimalSeparator, String blockSeparator, String tokenSeparator) {
        Element dataDefinition = (Element) getParentNode().getElementsByTagName("sml:dataDefinition").item(0);
        // add data definition block
        Element parent = AddNewNodeToParent("swe:DataBlockDefinition", dataDefinition);
        // add components with "whenWhere"
        parent = AddNewNodeToParentWithAttribute("swe:components", parent, "name", "whenWhere");
        // add DataRecord for lat, lon, time
        parent = AddNewNodeToParent("swe:DataRecord", parent);
        // print each of our maps
        Element fieldNodeIter;
        for (String key : fieldMap.keySet()) {
            if (key.equalsIgnoreCase("time")) {
                HashMap<String, String> timeMap = (HashMap<String, String>)fieldMap.get(key);
                // add field node
                fieldNodeIter = AddNewNodeToParentWithAttribute("swe:field", parent, "name", key);
                // add time node
                fieldNodeIter = AddNewNodeToParentWithAttribute("swe:Time", fieldNodeIter, "definition", timeMap.get("definition"));
                // add uoms for every key that isn't 'definition'
                for (String sKey : timeMap.keySet()) {
                    if (!sKey.equalsIgnoreCase("definition")) {
                        AddNewNodeToParentWithAttribute("swe:uom", fieldNodeIter, sKey, timeMap.get(sKey).toString());
                    }
                }
            } else {
                HashMap<String, String> quantityMap = (HashMap<String, String>)fieldMap.get(key);
                // add field node
                fieldNodeIter = AddNewNodeToParentWithAttribute("swe:field", parent, "name", key);
                // add quantity node
                fieldNodeIter = AddNewNodeToParentWithAttribute("swe:Quantity", fieldNodeIter, "definition", quantityMap.get("definition"));
                // add uoms for every key !definition
                for (String sKey : quantityMap.keySet()) {
                    if (!sKey.equalsIgnoreCase("definition")) {
                        AddNewNodeToParentWithAttribute("swe:uom", fieldNodeIter, sKey, quantityMap.get(sKey).toString());
                    }
                }
            }
        }
        // lastly we need to add our encoding
        parent = (Element) getParentNode().getElementsByTagName("sml:dataDefinition").item(0);
        // add encoding node
        parent = AddNewNodeToParent("swe:encoding", parent);
        // add TextBlock node with above attributes
        parent = AddNewNodeToParentWithAttribute("swe:TextBlock", parent, "decimalSeparator", decimalSeparator);
        parent.setAttribute("blockSeparator", blockSeparator);
        parent.setAttribute("tokenSeparator", tokenSeparator);
    }
    
    /**
     * 
     * @param valueText
     */
    public void setPositionValue(String valueText) {
        // simple, just add values with text content of parameter
        Element parent = (Element) getParentNode().getElementsByTagName("sml:position").item(0);
        AddNewNodeToParentWithTextValue("sml:values", parent, valueText);
    }
    
    /**
     * 
     */
    public void deletePosition() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:position").item(0));
    }
    
    /**
     * 
     */
    public void deleteTimePosition() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:timePosition").item(0));
    }
    
    /**
     * 
     * @param latitudeInfo
     * @param longitudeInfo
     * @param depthInfo
     * @param definition
     */
    public void setStationPositionsNode(HashMap<String,String> latitudeInfo, HashMap<String,String> longitudeInfo, HashMap<String,String> depthInfo, String definition) {
        Element parent = (Element) getParentNode().getElementsByTagName("sml:PositionList").item(0);
        
        // add position w/ 'stationPosition' attribute
        parent = AddNewNodeToParentWithAttribute("sml:position", parent, "name", "stationPosition");
        // add Position, then location nodes
        parent = AddNewNodeToParent("swe:Position", parent);
        parent = AddNewNodeToParent("swe:location", parent);
        // add vector id="STATION_LOCATION" definition=definition
        parent = AddNewNodeToParentWithAttribute("swe:Vector", parent, "gml:id", "STATION_LOCATION");
        parent.setAttribute("definition", definition);
        // add a coordinate for each hashmap
        // latitude
        addCoordinateInfoNode(latitudeInfo, parent, "latitude", "Y", "deg");
        // longitude
        addCoordinateInfoNode(longitudeInfo, parent, "longitude", "X", "deg");
        // altitude/depth
        addCoordinateInfoNode(depthInfo, parent, "altitude", "Z", "m");
    }
    
    /**
     * 
     * @param latitudeInfo
     * @param longitudeInfo
     * @param depthInfo
     * @param definition
     * @param boundingBox
     */
    public void setStationPositionsNode(HashMap<String,String> latitudeInfo, HashMap<String,String> longitudeInfo, HashMap<String,String> depthInfo, String definition, LatLonRect boundingBox) {
        Element parent = (Element) getParentNode().getElementsByTagName("sml:PositionList").item(0);
        
        // add position w/ 'stationPosition' attribute
        parent = AddNewNodeToParentWithAttribute("sml:position", parent, "name", "stationPosition");
        // add Position, then location nodes
        parent = AddNewNodeToParent("swe:Position", parent);
        parent = AddNewNodeToParent("swe:location", parent);
        // add vector id="STATION_LOCATION" definition=definition
        parent = AddNewNodeToParentWithAttribute("swe:Vector", parent, "gml:id", "STATION_LOCATION");
        parent.setAttribute("definition", definition);
        // add a bounding box for lat/lon
        parent = AddNewNodeToParent("gml:boundedBy", parent);
        parent = AddNewNodeToParentWithAttribute("gml:Envelope", parent, "srsName", "");
        AddNewNodeToParentWithTextValue("gml:lowerCorener", parent, boundingBox.getLatMin() + " " + boundingBox.getLonMin());
        AddNewNodeToParentWithTextValue("gml:upperCorner", parent, boundingBox.getLatMax() + " " + boundingBox.getLonMax());
        // add depth/altitude
        addCoordinateInfoNode(depthInfo, parent, "altitude", "Z", "m");
    }
    
    /**
     * 
     * @param latitudeInfo
     * @param longitudeInfo
     * @param depthInfo
     * @param definition
     */
    public void setEndPointPositionsNode(HashMap<String,String> latitudeInfo, HashMap<String,String> longitudeInfo, HashMap<String,String> depthInfo, String definition) {
        Element parent = (Element) getParentNode().getElementsByTagName("sml:PositionList").item(0);
        
        // follow steps outlined above with slight alterations
        parent = AddNewNodeToParentWithAttribute("sml:position", parent, "name", "endPosition");
        parent = AddNewNodeToParent("swe:Position", parent);
        parent = AddNewNodeToParent("swe:location", parent);
        parent = AddNewNodeToParentWithAttribute("swe:Vector", parent, "gml:id", "END_LOCATION");
        parent.setAttribute("definition", definition);
        addCoordinateInfoNode(latitudeInfo, parent, "latitude", "Y", "deg");
        addCoordinateInfoNode(longitudeInfo, parent, "longitude", "X", "deg");
        addCoordinateInfoNode(depthInfo, parent, "altitude", "Z", "m");
    }
    
    /**
     * 
     * @param latitudeInfo
     * @param longitudeInfo
     * @param depthInfo
     * @param definition
     * @param boundingBox
     */
    public void setEndPointPositionsNode(HashMap<String,String> latitudeInfo, HashMap<String,String> longitudeInfo, HashMap<String,String> depthInfo, String definition, LatLonRect boundingBox) {
        Element parent = (Element) getParentNode().getElementsByTagName("sml:PositionList").item(0);
        
        // follow steps outlined above with slight alterations
        parent = AddNewNodeToParentWithAttribute("sml:position", parent, "name", "endPosition");
        parent = AddNewNodeToParent("swe:Position", parent);
        parent = AddNewNodeToParent("swe:location", parent);
        parent = AddNewNodeToParentWithAttribute("swe:Vector", parent, "gml:id", "END_LOCATION");
        parent.setAttribute("definition", definition);
        // add a bounding box for lat/lon
        parent = AddNewNodeToParent("gml:boundedBy", parent);
        parent = AddNewNodeToParentWithAttribute("gml:Envelope", parent, "srsName", "");
        AddNewNodeToParentWithTextValue("gml:lowerCorener", parent, boundingBox.getLatMin() + " " + boundingBox.getLonMin());
        AddNewNodeToParentWithTextValue("gml:upperCorner", parent, boundingBox.getLatMax() + " " + boundingBox.getLonMax());
        // add depth/altitude
        addCoordinateInfoNode(depthInfo, parent, "altitude", "Z", "m");
    }
    
    /**
     * 
     */
    public void deletePositions() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:positions").item(0));
    }
    
    public void setStationPositionsNode(HashMap<String,String> latitudeInfo, HashMap<String,String> longitudeInfo, HashMap<String,String> depthInfo, String definition) {
        Element parent = (Element) getParentNode().getElementsByTagName("sml:PositionList").item(0);
        
        // add position w/ 'stationPosition' attribute
        parent = AddNewNodeToParentWithAttribute("sml:position", parent, "name", "stationPosition");
        // add Position, then location nodes
        parent = AddNewNodeToParent("swe:Position", parent);
        parent = AddNewNodeToParent("swe:location", parent);
        // add vector id="STATION_LOCATION" definition=definition
        parent = AddNewNodeToParentWithAttribute("swe:Vector", parent, "gml:id", "STATION_LOCATION");
        parent.setAttribute("definition", definition);
        // add a coordinate for each hashmap
        // latitude
        addCoordinateInfoNode(latitudeInfo, parent, "latitude", "Y", "deg");
        // longitude
        addCoordinateInfoNode(longitudeInfo, parent, "longitude", "X", "deg");
        // altitude/depth
        addCoordinateInfoNode(depthInfo, parent, "altitude", "Z", "m");
    }
    
    public void setEndPointPositionsNode(HashMap<String,String> latitudeInfo, HashMap<String,String> longitudeInfo, HashMap<String,String> depthInfo, String definition) {
        Element parent = (Element) getParentNode().getElementsByTagName("sml:PositionList").item(0);
        
        // follow steps outlined above with slight alterations
        parent = AddNewNodeToParentWithAttribute("sml:position", parent, "name", "endPosition");
        parent = AddNewNodeToParent("swe:Position", parent);
        parent = AddNewNodeToParent("swe:location", parent);
        parent = AddNewNodeToParentWithAttribute("swe:Vector", parent, "gml:id", "END_LOCATION");
        parent.setAttribute("definition", definition);
        addCoordinateInfoNode(latitudeInfo, parent, "latitude", "Y", "deg");
        addCoordinateInfoNode(longitudeInfo, parent, "longitude", "X", "deg");
        addCoordinateInfoNode(depthInfo, parent, "altitude", "Z", "m");
    }
    
    public void deletePositions() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:positions").item(0));
    }
}
