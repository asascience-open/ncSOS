/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Dictionary;
import java.util.HashMap;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ucar.nc2.Attribute;

/**
 *
 * @author SCowan
 */
public class DescribeSensorFormatter implements SOSOutputFormatter {
    
    private Document document;
    
    private final String TEMPLATE = "templates/sosDescribeSensor.xml";
    
    public DescribeSensorFormatter() {
        document = parseTemplateXML();
    }
    
    public Document getDocument() {
        return document;
    }

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
    
    /****************************
     * Public Methods           *
     * Setting the XML document *
     ****************************/
    
    public void setDescriptionNode(String description) {
        // get our description node and set its string content
        document.getElementsByTagName("gml:description").item(0).setTextContent(description);
    }
    
    public void deleteDescriptionNode() {
        document.removeChild(document.getElementsByTagName("gml:description").item(0));
    }
    
    public void setIdentificationNode(String[] names, String[] definitions, String[] values) {
        if (names.length != definitions.length && names.length != values.length) {
            setupExceptionOutput("invalid formatting of station attributes");
            return;
        }
        // get our Identifier List and add nodes to it
        Element pList = (Element) document.getElementsByTagName("IdentifierList").item(0);
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
        document.removeChild(document.getElementsByTagName("identification").item(0));
    }
    
    public void addToClassificationNode(String classifierName, String definition, String classifierValue) {
        Element parent = (Element) document.getElementsByTagName("ClassifierList").item(0);
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
        document.removeChild(document.getElementsByTagName("classification").item(0));
    }
    
    public void addContactNode(String role, String organizationName, HashMap<String, HashMap<String, String>> contactInfo) {
        // setup and and insert a contact node (after classification)
        Element contact = document.createElement("contact");
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
        document.insertBefore(contact, document.getElementsByTagName("documentation").item(0));
    }
    
    public void deleteContactNodeFirst() {
        document.removeChild(document.getElementsByTagName("contact").item(0));
    }
    
    public void setDocumentationNode(String[] description, String[] format, String[] url) {
        if (description.length != format.length || description.length != url.length) {
            setupExceptionOutput("invalid format for documentation node; description, format, url counts are not the same");
            return;
        }
        
        Element parent = (Element) document.getElementsByTagName("documentation").item(0);
        
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
            if (url[i] != null) {
                Element u = document.createElement("onlineResource");
                u.setAttribute("xlink:href", url[i]);
                nDoc.appendChild(u);
            }
            parent.appendChild(nDoc);
        }
    }
    
    public void deleteDocumentationNode() {
        document.removeChild(document.getElementsByTagName("documentation").item(0));
    }
    
    public void setHistoryEvents(String[] name, String[] date, String[] description, String[] url) {
        // make sure all of the arrays line up
        if (name.length != date.length || name.length != description.length || name.length != url.length) {
            setupExceptionOutput("Invalid history values!");
            return;
        }
        
        Element parent = (Element) document.getElementsByTagName("history").item(0);
        
        for (int i=0; i<name.length; i++) {
            Element member = document.createElement("member");
            if (name[i] != null) {
                member.setAttribute("name", name[i]);
            }
            if (date[i] != null) {
                Element tEl = document.createElement("date");
                tEl.setTextContent(date[i]);
                member.appendChild(tEl);
            }
            if (description[i] != null) {
                Element tEl = document.createElement("description");
                tEl.setTextContent(description[i]);
                member.appendChild(tEl);
            }
            if (url[i] != null) {
                Element tEl = document.createElement("documentation");
                tEl.setAttribute("xlink:href", url[i]);
                member.appendChild(tEl);
            }
            parent.appendChild(member);
        }
    }
    
    public void deleteHistoryNode() {
        document.removeChild(document.getElementsByTagName("history").item(0));
    }
    
    public void setLocationNode(String stationName, double[] coords) {
        Element parent = (Element) document.getElementsByTagName("location").item(0);
        
        Element point = document.createElement("gml:Point");
        point.setAttribute("gml:id", "STATION-LOCATION-" + stationName);
        Element coordinates = document.createElement("gml:coordinates");
        coordinates.setTextContent(coords[0] + " " + coords[1]);
        point.appendChild(coordinates);
        
        parent.appendChild(point);
    }
    
    public void deleteLocationNode() {
        document.removeChild(document.getElementsByTagName("location").item(0));
    }
    
    public void setComponentsNode(HashMap<String, HashMap<String, String>> components) {
        // get our component list
        Element parent = (Element) document.getElementsByTagName("ComponentList").item(0);
        
        // iterate through our hash map and create our list
        for (String key : components.keySet()) {
            Element component = document.createElement("component");
            component.setAttribute("name", "Sensor " + key.toString());
            HashMap<String, String> cVals = (HashMap<String, String>)components.get(key);
            Element system = document.createElement("System");
            system.setAttribute("gml:id", "sensor-" + cVals.get("system").toString());
            for (String vKey : cVals.keySet()) {
                if (vKey.equalsIgnoreCase("name")) {
                    component.setAttribute("name", "Sensor " + cVals.get(vKey).toString());
                } else if (vKey.equalsIgnoreCase("system")) {
                    // ignore this
                } else if (vKey.equalsIgnoreCase("description")) {
                    Element tEl = document.createElement("gml:description");
                    tEl.setTextContent(cVals.get(vKey).toString());
                    system.appendChild(tEl);
                } else {
                    Element tEl = document.createElement(vKey);
                    tEl.setAttribute("xlink:href", cVals.get(vKey).toString());
                    system.appendChild(tEl);
                }
            }
            component.appendChild(system);
            parent.appendChild(component);
        }
    }
    
    public void deleteComponentsNode() {
        document.removeChild(document.getElementsByTagName("components").item(0));
    }
}
