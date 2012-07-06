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
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ucar.nc2.VariableSimpleIF;

/**
 *
 * @author SCowan
 */
public class DescribeSensorFormatter implements SOSOutputFormatter {
    
    private Document document;
    
    private final String TEMPLATE = "templates/sosDescribeSensor.xml";
    private final String uri;
    private final String query;
    
    public DescribeSensorFormatter() {
        document = parseTemplateXML();
        this.uri = this.query = null;
    }

    public DescribeSensorFormatter(String uri, String query) {
        document = parseTemplateXML();
        this.uri = uri;
        this.query = query;
    }
    
    public Document getDocument() {
        return document;
    }
    
    /*********************
     * Interface Methods *
     *********************/

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
    
    private String joinArray(String[] arrayToJoin) {
        String retval = "";
        for (String str : arrayToJoin) {
            retval += str;
        }
        return retval;
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
    
    /****************************
     * Public Methods           *
     * Setting the XML document *
     ****************************/
    
    public void setSystemId(String id) {
        Element system = (Element) document.getElementsByTagName("sml:System").item(0);
        system.setAttribute("gml:id", id);
    }
    
    public void setDescriptionNode(String description) {
        // get our description node and set its string content
        document.getElementsByTagName("gml:description").item(0).setTextContent(description);
    }
    
    public void deleteDescriptionNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("gml:description").item(0));
    }
    
    public void setIdentificationNode(String[] names, String[] definitions, String[] values) {
        if (names.length != definitions.length && names.length != values.length) {
            setupExceptionOutput("invalid formatting of station attributes");
            return;
        }
        // get our Identifier List and add nodes to it
        Element pList = (Element) document.getElementsByTagName("sml:IdentifierList").item(0);
        for (int i=0; i<names.length; i++) {
            Element ident = document.createElement("identifier");
            ident.setAttribute("name", names[i]);
            Element term = document.createElement("Term");
            term.setAttribute("definition", definitions[i]);
            Element val = document.createElement("value");
            val.setTextContent(values[i]);
            term.appendChild(val);
            ident.appendChild(term);
            pList.appendChild(ident);
        }
    }
    
    public void deleteIdentificationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:identification").item(0));
    }
    
    public void addToClassificationNode(String classifierName, String definition, String classifierValue) {
        Element parent = (Element) document.getElementsByTagName("sml:ClassifierList").item(0);
        Element classifier = document.createElement("classifier");
        classifier.setAttribute("name", classifierName);
        Element term = document.createElement("Term");
        term.setAttribute("definition", definition);
        Element value = document.createElement("value");
        value.setTextContent(classifierValue);
        term.appendChild(value);
        classifier.appendChild(term);
        parent.appendChild(classifier);
    }
    
    public void deleteClassificationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:classification").item(0));
    }
    
    public void addContactNode(String role, String organizationName, HashMap<String, HashMap<String, String>> contactInfo) {
        // setup and and insert a contact node (after classification)
        Element contact = document.createElement("sml:contact");
        contact.setAttribute("xlink:role", role);
        /* *** */
        Element prevParent = contact;
        Element curParent = document.createElement("ResponsibleParty");
        prevParent.appendChild(curParent);
        /* *** */
        prevParent = curParent;
        curParent = document.createElement("oranizationName");
        curParent.setTextContent(organizationName);
        prevParent.appendChild(curParent);
        /* *** */
        curParent = document.createElement("contactInfo");
        prevParent.appendChild(curParent);
        /* *** */
        // super nesting for great justice
        prevParent = curParent;
        for (String key : contactInfo.keySet()) {
            // add key as node
            curParent = document.createElement(key);
            HashMap<String, String> vals = (HashMap<String, String>)contactInfo.get(key);
            for (String vKey : vals.keySet()) {
                Element tEl = document.createElement(vKey);
                tEl.setTextContent(vals.get(vKey).toString());
                curParent.appendChild(tEl);
            }
            prevParent.appendChild(curParent);
        }
        // insert before documentation
        document.insertBefore(contact, document.getElementsByTagName("sml:documentation").item(0));
    }
    
    public void deleteContactNodeFirst() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("sml:contact").item(0));
    }
    
    public void setDocumentationNode(String[] description, String[] format, String[] documentation) {
        if (description.length != format.length || description.length != documentation.length) {
            setupExceptionOutput("invalid format for documentation node; description, format, url counts are not the same");
            return;
        }
        
        Element parent = (Element) document.getElementsByTagName("sml:documentation").item(0);
        
        for (int i=0; i<description.length; i++) {
            Element nDoc = document.createElement("Document");
            if (description[i] != null) {
                Element desc = document.createElement("gml:description");
                desc.setTextContent(description[i]);
                nDoc.appendChild(desc);
            }
            if (format[i] != null) {
                Element form = document.createElement("format");
                form.setTextContent(format[i]);
                nDoc.appendChild(form);
            }
            if (documentation[i] != null) {
                Element u = document.createElement("onlineResource");
                u.setAttribute("xlink:href", documentation[i]);
                nDoc.appendChild(u);
            }
            parent.appendChild(nDoc);
        }
    }
    
    public void deleteDocumentationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("documentation").item(0));
    }
    
    public void setHistoryEvents(String[] name, String[] date, String[] description, String[] url) {
        // make sure all of the arrays line up
        if (name.length != date.length || name.length != description.length || name.length != url.length) {
            setupExceptionOutput("Invalid history values!");
            return;
        }
        
        Element parent = (Element) document.getElementsByTagName("sml:history").item(0);
        parent = (Element) parent.getElementsByTagName("EventList").item(0);
        
        for (int i=0; i<name.length; i++) {
            Element member = document.createElement("member");
            if (name[i] != null) {
                member.setAttribute("name", name[i]);
            }
            Element event = document.createElement("Event");
            member.appendChild(event);
            if (date[i] != null) {
                Element tEl = document.createElement("date");
                tEl.setTextContent(date[i]);
                event.appendChild(tEl);
            }
            if (description[i] != null) {
                Element tEl = document.createElement("description");
                tEl.setTextContent(description[i]);
                event.appendChild(tEl);
            }
            if (url[i] != null) {
                Element tEl = document.createElement("documentation");
                tEl.setAttribute("xlink:href", url[i]);
                event.appendChild(tEl);
            }
            parent.appendChild(member);
        }
    }
    
    public void deleteHistoryNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("history").item(0));
    }
    
    public void setLocationNode(String stationName, double[] coords) {
        Element parent = (Element) document.getElementsByTagName("sml:location").item(0);
        
        Element point = document.createElement("gml:Point");
        point.setAttribute("gml:id", "STATION-LOCATION-" + stationName);
        Element coordinates = document.createElement("gml:coordinates");
        coordinates.setTextContent(coords[0] + " " + coords[1]);
        point.appendChild(coordinates);
        
        parent.appendChild(point);
    }
    
    public void deleteLocationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("location").item(0));
    }
    
    public void setComponentsNode(List<VariableSimpleIF> dataVariables, String procedure) {
        // iterate through our list and create the node
        Element parent = (Element) document.getElementsByTagName("sml:ComponentList").item(0);

        for (int i=0; i<dataVariables.size(); i++) {
            String fName = dataVariables.get(i).getFullName();
            // component node
            Element component = document.createElement("component");
            component.setAttribute("name", "Sensor " + fName);
            // system node
            Element system = document.createElement("System");
            system.setAttribute("gml:id", "sensor-" + fName);
            // identification node
            Element ident = document.createElement("identification");
            ident.setAttribute("xlink:href", procedure + "::" + fName);
            // documentation (url) node
            Element doc = document.createElement("documentation");
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
            url += "?" + joinArray(reqParams);
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
    
    public void deleteComponentsNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("components").item(0));
    }
    
    public void setPositionName(String name) {
        Element position = (Element) getParentNode().getElementsByTagName("position").item(0);
        position.setAttribute("name", name);
    }
    
    public void setPositionDataDefinition(HashMap<String, HashMap<String, String>> fieldMap, String decimalSeparator, String blockSeparator, String tokenSeparator) {
        Element dataDefinition = (Element) getParentNode().getElementsByTagName("dataDefinition").item(0);
        // add data definition block
        Element parent = AddNewNodeToParent("DataBlockDefinition", dataDefinition);
        // add components with "whenWhere"
        parent = AddNewNodeToParentWithAttribute("components", parent, "name", "whenWhere");
        // add DataRecord for lat, lon, time
        parent = AddNewNodeToParent("DataRecord", parent);
        // print each of our maps
        Element fieldNodeIter;
        for (String key : fieldMap.keySet()) {
            if (key.equalsIgnoreCase("time")) {
                HashMap<String, String> timeMap = (HashMap<String, String>)fieldMap.get(key);
                // add field node
                fieldNodeIter = AddNewNodeToParentWithAttribute("field", parent, "name", key);
                // add time node
                fieldNodeIter = AddNewNodeToParentWithAttribute("Time", fieldNodeIter, "definition", timeMap.get("definition"));
                // add uoms for every key that isn't 'definition'
                for (String sKey : timeMap.keySet()) {
                    if (!sKey.equalsIgnoreCase("definition")) {
                        AddNewNodeToParentWithAttribute("uom", fieldNodeIter, sKey, timeMap.get(sKey).toString());
                    }
                }
            } else {
                HashMap<String, String> quantityMap = (HashMap<String, String>)fieldMap.get(key);
                // add field node
                fieldNodeIter = AddNewNodeToParentWithAttribute("field", parent, "name", key);
                // add quantity node
                fieldNodeIter = AddNewNodeToParentWithAttribute("Quantity", fieldNodeIter, "definition", quantityMap.get("definition"));
                // add uoms for every key !definition
                for (String sKey : quantityMap.keySet()) {
                    if (!sKey.equalsIgnoreCase("definition")) {
                        AddNewNodeToParentWithAttribute("uom", fieldNodeIter, sKey, quantityMap.get(sKey).toString());
                    }
                }
            }
        }
        // lastly we need to add our encoding
        parent = (Element) getParentNode().getElementsByTagName("dataDefinition").item(0);
        // add encoding node
        parent = AddNewNodeToParent("encoding", parent);
        // add TextBlock node with above attributes
        parent = AddNewNodeToParentWithAttribute("TextBlock", parent, "decimalSeparator", decimalSeparator);
        parent.setAttribute("blockSeparator", blockSeparator);
        parent.setAttribute("tokenSeparator", tokenSeparator);
    }
    
    public void setPositionValue(String valueText) {
        // simple, just add values with text content of parameter
        Element parent = (Element) getParentNode().getElementsByTagName("position").item(0);
        AddNewNodeToParentWithTextValue("values", parent, valueText);
    }
    
    public void deletePosition() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("position").item(0));
    }
    
    public void deleteTimePosition() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("timePosition").item(0));
    }
    
    public void deletePositions() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("positions").item(0));
    }
    
    public void deleteValidTime() {
        getParentNode().removeChild(getParentNode().getElementsByTagName("validTime").item(0));
    }
}
