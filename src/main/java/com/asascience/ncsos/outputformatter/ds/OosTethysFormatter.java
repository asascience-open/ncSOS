/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter.ds;

import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import ucar.nc2.VariableSimpleIF;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Class that formats the output for describe sensor requests. Holds an instance
 * of w3c document model for printing the xml response following the 
 * "sensorML/1.0.1" standard.
 * @author SCowan
 * @version 1.0.0
 */
public class OosTethysFormatter extends OutputFormatter {
    public static final String CLASSIFIER = "classifier";
    public static final String CLASSIFIERLIST = "ClassifierList";

    public static final String CODE = "code";
    public static final String CONTACT = "contact";
    public static final String COORDINATE = "coordinate";
    public static final String DOCUMENTATION = "documentation";
    public static final String NAME = "name";
    public static final String PROCEDURE = "procedure";
    public static final String SENSOR_ = "Sensor ";
    public static final String SENSOR_WITH_SPACER = "sensor-";
    public static final String UOM = "uom";
    private final String TEMPLATE = "templates/DS_oostethys.xml";
    private final String uri;
    private final String query;
    private DOMImplementationLS impl;
    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(OosTethysFormatter.class);

    /**
     * Creates a new formatter instance that uses the sosDescribeSensor.xml as a
     * template (found in the resources templates folder)
     */
    public OosTethysFormatter() {
        this.document = parseTemplateXML();
        initNamespaces();

        this.uri = this.query = null;
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (InstantiationException ex) {
            System.out.println(ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println(ex.getMessage());
        } catch (ClassCastException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Creates a new formatter instance that uses the sosDescribeSensor.xml as a
     * template (found in the resources templates folder)
     * @param uri the uri of the request (used to construct hrefs for components
     * @param query the query of the request (used to construct hrefs for components)
     */
    public OosTethysFormatter(String uri, String query) {
        document = parseTemplateXML();
        this.uri = uri;
        this.query = query;
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (InstantiationException ex) {
            System.out.println(ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println(ex.getMessage());
        } catch (ClassCastException ex) {
            System.out.println(ex.getMessage());
        }
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
    public void addDataFormattedStringToInfoList(String dataFormattedString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void emtpyInfoList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setupExceptionOutput(String message) {
        document = XMLDomUtils.getExceptionDom(message);
    }

    public void writeOutput(Writer writer) {
        // output our document to the writer
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
            _log.error(ex2.getMessage());
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
        return (Element) document.getElementsByTagNameNS(SML_NS, SYSTEM).item(0);
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

    private Element addNewNodeToParent(String nameSpace,
            String nameOfNewNode,
            Element parentNode) {
        Element retval = createElementNS(nameSpace, nameOfNewNode);

        parentNode.appendChild(retval);
        return retval;
    }

    private Element addNewNodeToParentWithAttribute(
            String nameSpace,
            String nameOfNewNode, Element parentNode,
            String attributeName, String attributeValue) {

        return addNewNodeToParentWithAttribute(nameSpace,
                nameOfNewNode, parentNode, null,
                attributeName, attributeValue);
    }

    private Element addNewNodeToParentWithAttribute(
            String nameSpace,
            String nameOfNewNode, Element parentNode,
            String attributeNS,
            String attributeName, String attributeValue) {
        Element retval = createElementNS(nameSpace, nameOfNewNode);

        if (attributeNS != null) {
            retval.setAttributeNS(attributeNS, attributeName, attributeValue);
        } else {
            retval.setAttribute(attributeName, attributeValue);
        }
        parentNode.appendChild(retval);
        return retval;
    }

    private Element addNewNodeToParentWithTextValue(
            String nameSpace,
            String nameOfNewNode, Element parentNode,
            String textContentValue) {
        Element retval = createElementNS(nameSpace, nameOfNewNode);

        retval.setTextContent(textContentValue);
        parentNode.appendChild(retval);
        return retval;
    }

    private void addCoordinateInfoNode(HashMap<String, String> coordInfo, Element parentNode, String defName, String defAxis, String defUnit) {
        Element lat = addNewNodeToParentWithAttribute(SWE_NS, COORDINATE, parentNode, NAME, defName);
        if (coordInfo.containsKey(NAME)) {
            lat.setAttribute(NAME, coordInfo.get(NAME).toString());
        }
        lat = addNewNodeToParentWithAttribute(SWE_NS, QUANTITY, lat, AXIS_ID, defAxis);
        if (coordInfo.containsKey(AXIS_ID)) {
            lat.setAttribute(AXIS_ID, coordInfo.get(AXIS_ID).toString());
        }
        Element unit = addNewNodeToParentWithAttribute(SWE_NS, UOM, lat, CODE, defUnit);
        if (coordInfo.containsKey(CODE)) {
            unit.setAttribute(CODE, coordInfo.get(CODE).toString());
        }
        if (coordInfo.containsKey(SML_VALUE)) {
            addNewNodeToParentWithTextValue(SWE_NS, SML_VALUE, lat, coordInfo.get(SML_VALUE).toString());
        }
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
        Element system = (Element) document.getElementsByTagNameNS(SML_NS, "System").item(0);
        system.setAttributeNS(GML_NS, ID, id);
    }

    /**
     * Accessor function for setting the description text of the gml:description node
     * @param description usually the description attribute value of a netcdf dataset
     */
    public void setDescriptionNode(String description) {
        // get our description node and set its string content
        document.getElementsByTagNameNS(GML_NS, DESCRIPTION).item(0).setTextContent(DESCRIPTION);
    }

    /**
     * Sets the text content of the gml:name node
     * @param name name (urn) of the station
     */
    public void setName(String name) {
        document.getElementsByTagNameNS(GML_NS, NAME).item(0).setTextContent(NAME);
    }

    /**
     * Removes the gml:description node from the xml document
     */
    public void deleteDescriptionNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(GML_NS, DESCRIPTION).item(0));
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
        Element pList = (Element) document.getElementsByTagNameNS(SML_NS, IDENTIFIER_LIST).item(0);
        for (int i = 0; i < names.length; i++) {
            Element parent = addNewNodeToParentWithAttribute(SML_NS, IDENTIFIER, pList, NAME, names[i]);
            parent = addNewNodeToParentWithAttribute(SML_NS, TERM, parent, DEFINITION, definitions[i]);
            addNewNodeToParentWithTextValue(SML_NS, SML_VALUE, parent, values[i]);
        }
    }

    /**
     * adds nodes to the sml:IdentifierList element
     * @param name name of the identifier
     * @param definition definition of the identifier
     * @param value  value of the sml:value node in the identifier
     */
    public void addSmlIdentifier(String name, String definition, String value) {
        /* Adds the following to the sml:IdentifierList
         * <sml:identifier name='name'>
         *   <sml:Term definition='definition'>
         *     <sml:value>value</sml:value>
         *   </sml:Term>
         * </sml:identifier>
         */
        Element parent = (Element) document.getElementsByTagNameNS(SML_NS, IDENTIFIER_LIST).item(0);

        Element ident = addNewNodeToParentWithAttribute(SML_NS, IDENTIFIER, parent, NAME, name);
        ident = addNewNodeToParentWithAttribute(SML_NS, TERM, ident, DEFINITION, definition);
        addNewNodeToParentWithTextValue(SML_NS, SML_VALUE, ident, value);
    }

    /**
     * Removes the sml:identification node from the xml document
     */
    public void deleteIdentificationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(SML_NS, IDENTIFICATION).item(0));
    }

    /**
     * Accessor for setting the sml:classification node in the xml document
     * @param classifierName name of the station classification (eg 'platformType')
     * @param definition sml:Term definition of the classification
     * @param classifierValue value of the classification (eg 'GLIDER')
     */
    public void addToClassificationNode(String classifierName, String definition, String classifierValue) {
        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, CLASSIFIERLIST).item(0);
        parent = addNewNodeToParentWithAttribute(SML_NS, CLASSIFIER, parent, NAME, classifierName);
        parent = addNewNodeToParentWithAttribute(SML_NS, TERM, parent, DEFINITION, definition);
        addNewNodeToParentWithTextValue(SML_NS, SML_VALUE, parent, classifierValue);
    }

    /**
     * Adds a sml:classifier to sml:ClassifierList
     * @param name name of the classifier
     * @param definition definition of the term
     * @param codeSpace codeSpace of the classifier
     * @param value value of the classifier
     */
    public void addSmlClassifier(String name, String definition, String codeSpace, String value) {
        /*
         * <sml:classifier name='name'>
         *   <sml:Term definition='definition'>
         *     <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/" + 'codeSpace'>
         *     <sml:value>'value'</sml:value>
         *   </sml:Term>
         * </sml:classifier>
         */
        Element parent = (Element) document.getElementsByTagNameNS(SML_NS, CLASSIFIERLIST).item(0);
        parent = addNewNodeToParentWithAttribute(SML_NS, CLASSIFIER, parent, NAME, name);
        parent = addNewNodeToParentWithAttribute(SML_NS, "Term", parent, DEFINITION, definition);
        addNewNodeToParentWithAttribute(SML_NS, CODE_SPACE, parent,
                XLINK_NS, HREF, "http://mmisw.org/ont/ioos/" + codeSpace);
        addNewNodeToParentWithTextValue(SML_NS, SML_VALUE, parent, value);
    }

    /**
     * Removes the sml:classification node from the xml document
     */
    public void deleteClassificationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(SML_NS, "classification").item(0));
    }

    /**
     * Set the sml:validTime node
     * @param timeBegin the beginPosition value
     * @param timeEnd the endPosition value
     */
    public void setValidTime(String timeBegin, String timeEnd) {
        /*
         * <gml:TimePeriod>
         *   <gml:beginPosition>'timeBegin'</gml:beginPosition>
         *   <gml:endPosition>'timeEnd'</gml:endPosition>
         * </gml:TimePeriod>
         */
        Element parent = (Element) document.getElementsByTagNameNS(SML_NS, VALID_TIME).item(0);
        parent = addNewNodeToParent(GML_NS, TIME_PERIOD, parent);
        addNewNodeToParentWithTextValue(GML_NS, BEGIN_POSITION, parent, timeBegin);
        addNewNodeToParentWithTextValue(GML_NS, END_POSITION, parent, timeEnd);
    }

    /**
     * Adds a sml:capabilities node that defines a metadata property for the sensor
     * @param name name of the capability
     * @param title name of the metadata
     * @param href reference to the metadata
     */
    public void addSmlCapabilitiesGmlMetadata(String name, String title, String href) {
        /*
         * <sml:capabilities name='name'>
         *   <swe:SimpleDataRecord>
         *     <gml:metaDataProperty xlink:title='title' xlink:href='href' />
         *   </swe:SimpleDataRecord>
         * </sml:capabilities>
         */
        Element parent = getParentNode();
        parent = addNewNodeToParentWithAttribute(SML_NS, "capabilities", parent, NAME, name);
        parent = addNewNodeToParent(SWE_NS, "SimpleDataRecord", parent);
        parent = addNewNodeToParentWithAttribute(GML_NS, META_DATA_PROP, parent, XLINK_NS, TITLE, title);
        parent.setAttributeNS(XLINK_NS, HREF, href);
    }

    /**
     * Accessor method to add a contact node to the sml:System node structure. Most
     * information is gathered from global Attributes of the dataset
     * @param role the role of the contact (eg publisher)
     * @param organizationName the name of the orginization or individual
     * @param contactInfo info for contacting the...um...contact
     */
    public void addContactNode(String role, String organizationName, HashMap<String, HashMap<String, String>> contactInfo, String onlineResource) {
        // setup and and insert a contact node (after history)
        document = XMLDomUtils.addNode(document, SYSTEM, SML_NS, CONTACT, SML_NS, HISTORY, SML_NS);
        NodeList contacts = getParentNode().getElementsByTagNameNS(SML_NS, CONTACT);
        Element contact = null;
        for (int i = 0; i < contacts.getLength(); i++) {
            if (!contacts.item(i).hasAttributes()) {
                contact = (Element) contacts.item(i);
            }
        }
        contact.setAttributeNS(XLINK_NS, "role", role);
        /* *** */
        Element parent = addNewNodeToParent(SML_NS, "ResponseibleParty", contact);
        /* *** */
        addNewNodeToParentWithTextValue(SML_NS, "organizationName", parent, organizationName);
        /* *** */
        parent = addNewNodeToParent(SML_NS, "contactInfo", parent);
        /* *** */
        // super nesting for great justice
        if (contactInfo != null) {
            for (String key : contactInfo.keySet()) {
                // add key as node
                Element sparent = addNewNodeToParent(SML_NS, key, parent);
                HashMap<String, String> vals = (HashMap<String, String>) contactInfo.get(key);
                for (String vKey : vals.keySet()) {
                    if (vals.get(vKey) != null) {
                        addNewNodeToParentWithTextValue(SML_NS, vKey, sparent, vals.get(vKey).toString());
                    }
                }
            }
        }
        // add online resource if it exists
        if (onlineResource != null) {
            addNewNodeToParentWithAttribute(SML_NS, "onlineResource", parent, XLINK_NS, HREF, onlineResource);
        }
    }

    public void addContactNode(String role, String orgName, HashMap<String, HashMap<String, String>> contactInfo) {
        addContactNode(role, orgName, contactInfo, null);
    }

    /**
     * Removes the first contact node instance from the xml document
     */
    public void deleteContactNodeFirst() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(SML_NS, CONTACT).item(0));
    }

    /**
     * Accessor method to set the sml:history node in the xml document
     * @param history the Attribute value of the 
     */
    public void setHistoryEvents(String history) {
        Element parent = (Element) document.getElementsByTagNameNS(SML_NS, HISTORY).item(0);
        parent.setTextContent(history);
    }

    /**
     * Removes the sml:history node from the xml document
     */
    public void deleteHistoryNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(SML_NS, HISTORY).item(0));
    }

    public void setSmlLocation(String srsName, String pos) {
        /*
         * <sml:location>
         *   <gml:Point srsName='srsName'>
         *     <gml:pos>'pos'</gml:pos>
         *   </gml:Point>
         * </sml:location
         */
        Element parent = (Element) this.document.getElementsByTagNameNS(SML_NS, LOCATION).item(0);
        parent = addNewNodeToParentWithAttribute(GML_NS, "Point", parent, SRS_NAME, srsName);
        addNewNodeToParentWithTextValue(GML_NS, POS, parent, pos);
    }

    public void setLocationNode2Dimension(String stationName, double[][] coords) {
        setLocationNode2Dimension(stationName, coords, "http://www.opengis.net/def/crs/EPSG/0/4326");
    }

    /**
     * 
     * @param stationName
     * @param coords 
     */
    public void setLocationNode2Dimension(String stationName, double[][] coords, String srs) {
        if (coords.length < 1) {
            return;
        }
        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, LOCATION).item(0);

        parent = addNewNodeToParentWithAttribute(GML_NS, LINE_STRING, parent, SRS_NAME, srs);
        parent = addNewNodeToParentWithAttribute(GML_NS, "posList", parent, "srsDimension", "2");
        // add values for each pair of coords
        String coordsString = "\n";
        for (int i = 0; i < coords.length; i++) {
            if (coords[i].length == 2 && Math.abs(coords[i][0]) < 180 && Math.abs(coords[i][1]) < 180) {
                coordsString += coords[i][0] + " " + coords[i][1] + "\n";
            } else if (coords[i].length == 3 && Math.abs(coords[i][0]) < 180 && Math.abs(coords[i][1]) < 180) {
                coordsString += coords[i][0] + " " + coords[i][1] + "\n";
            }
        }
        parent.setTextContent(coordsString);
    }

    /**
     * 
     * @param stationName
     * @param coords 
     */
    public void setLocationNode3Dimension(String stationName, double[][] coords) {
        setLocationNode3Dimension(stationName, coords, "http://www.opengis.net/def/crs/EPSG/0/4329");
    }

    /**
     * 
     * @param stationName
     * @param coords 
     */
    public void setLocationNode3Dimension(String stationName, double[][] coords, String srs) {
        if (coords.length < 1) {
            return;
        }
        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, LOCATION).item(0);

        parent = addNewNodeToParentWithAttribute(GML_NS, LINE_STRING, parent, SRS_NAME, srs);
        parent = addNewNodeToParentWithAttribute(GML_NS, "posList", parent, "srsDimension", "3");
        // add values for each pair of coords
        String coordsString = "\n";

        for (int i = 0; i < coords.length; i++) {
            if (coords[i].length == 3 && Math.abs(coords[i][0]) < 180 && Math.abs(coords[i][1]) < 180 && coords[i][2] > -999) {
                coordsString += coords[i][0] + " " + coords[i][1] + " " + coords[i][2] + "\n";
            }
        }
        parent.setTextContent(coordsString);
    }

    public void setOrderedLocationNode3Dimension(String stationName, double[][] coords) {
        setOrderedLocationNode3Dimension(stationName, coords, "http://www.opengis.net/def/crs/EPSG/0/4329");
    }

    public void setOrderedLocationNode3Dimension(String stationName, double[][] coords, String srs) {
        if (coords.length < 1) {
            return;
        }

        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, LOCATION).item(0);

        parent = addNewNodeToParentWithAttribute(GML_NS, "LineString", parent, "srsName", srs);
        parent = addNewNodeToParentWithAttribute(GML_NS, "posList", parent, "srsDimension", "3");
        String coordsString = "\n";

        TreeMap<Double, Double[]> depthOrdered = new TreeMap<Double, Double[]>();
        for (int i = 0; i < coords.length; i++) {
            if (coords[i].length == 3 && !depthOrdered.containsKey(coords[i][2])
                    && coords[i][2] > -999 && Math.abs(coords[i][0]) < 180 && Math.abs(coords[i][1]) < 180) {
                depthOrdered.put(coords[i][2], new Double[]{coords[i][0], coords[i][1]});
            }
        }

        // go through now ordered tree and add the values
        for (Double key : depthOrdered.keySet()) {
            coordsString += depthOrdered.get(key)[0] + " " + depthOrdered.get(key)[1] + " " + key + "\n";
        }

        parent.setTextContent(coordsString);
    }

    /**
     * Accessor method for adding a gml:Point node to the sml:location node in the
     * xml document
     * @param stationName name of the station
     * @param coords lat, lon of the station's location
     * @deprecated 
     */
    public void setLocationNode(String stationName, double[] coords) {
        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, LOCATION).item(0);

        parent = addNewNodeToParentWithAttribute(GML_NS, "Point", parent, GML_NS, ID,
                "STATION-LOCATION-" + stationName);
        addNewNodeToParentWithTextValue(GML_NS, "coordinates", parent, coords[0] + " " + coords[1]);
    }

    /**
     * Accessor method for adding a gml:boundedBy node to the sml:location node
     * in the xml document
     * @param lowerPoint lat, lon of the lower corner of the bounding box
     * @param upperPoint lat, lon of the upper corner of the bounding box
     * @deprecated 
     */
    public void setLocationNodeWithBoundingBox(String[] lowerPoint, String[] upperPoint) {
        if (lowerPoint.length != 2 || upperPoint.length != 2) {
            throw new IllegalArgumentException("lowerPoint or upperPoint are not valid");
        }

        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, LOCATION).item(0);

        parent = addNewNodeToParent(GML_NS, BOUNDED_BY, parent);
        parent = addNewNodeToParentWithAttribute(GML_NS, ENVELOPE, parent, SRS_NAME, "");
        addNewNodeToParentWithTextValue(GML_NS, LOWER_CORNER, parent, lowerPoint[0] + " " + lowerPoint[1]);
        addNewNodeToParentWithTextValue(GML_NS, UPPER_CORNER, parent, upperPoint[0] + " " + upperPoint[1]);
    }

    /**
     * Removes the sml:location node from the xml document
     */
    public void deleteLocationNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(SML_NS, LOCATION).item(0));
    }

    /**
     * Adds the component information for a station from a list of Variables and the procedure from the request.
     * @param dataVariables list of Variables that are the sensors of the station (usually found using getDataVariables on a dataset)
     * @param procedure the procedure from the request (urn)
     */
    public void setComponentsNode(List<VariableSimpleIF> dataVariables, String procedure) {
        // iterate through our list and create the node
        Element parent = (Element) document.getElementsByTagNameNS(SML_NS, "ComponentList").item(0);

        for (int i = 0; i < dataVariables.size(); i++) {
            String fName = dataVariables.get(i).getFullName();
            // component node
            Element component = createElementNS(SML_NS, COMPONENT);

            component.setAttribute(NAME, SENSOR_ + fName);
            // system node
            Element system = createElementNS(SML_NS, SYSTEM);

            system.setAttributeNS(GML_NS, ID, SENSOR_WITH_SPACER + fName);
            // identification node
            Element ident = createElementNS(SML_NS, IDENTIFICATION);

            ident.setAttributeNS(XLINK_NS, HREF, procedure.replaceAll(":station:", ":sensor:") + ":" + fName);
            // documentation (url) node
            Element doc = createElementNS(SML_NS, DOCUMENTATION);

            // need to construct url for sensor request
            String url = this.uri;
            String[] reqParams = this.query.split("&");
            // look for procedure
            for (int j = 0; j < reqParams.length; j++) {
                if (reqParams[j].contains(PROCEDURE)) // add sensor
                {
                    reqParams[j] += ":" + fName;
                }
            }
            // rejoin
            url += "?" + joinArray(reqParams, "&");
            doc.setAttributeNS(XLINK_NS, HREF, url);
            // description
            Element desc = createElementNS(GML_NS, DESCRIPTION);

            desc.setTextContent(dataVariables.get(i).getDescription());
            // add all nodes
            system.appendChild(ident);
            system.appendChild(doc);
            system.appendChild(desc);
            component.appendChild(system);
            parent.appendChild(component);
        }
    }

    public void addSmlComponent(String compName, String compId, String description, String urn, String dsUrl) {
        /*
         * <sml:component name='compName'>
         *   <sml:System gml:id='compId'>
         *     <gml:description>'description'</gml:description>
         *     <sml:identification xlink:href='urn' />
         *     <sml:documentation xlink:href='dsUrl' />
         *     <sml:outputs>
         *       <sml:OutputList />
         *     </sml:outputs>
         *   </sml:System>
         * </sml:component>
         */
        // add to the <sml:ComponentList> node
        Element parent = (Element) this.document.getElementsByTagNameNS(SML_NS, COMPONENT_LIST).item(0);
        parent = addNewNodeToParentWithAttribute(SML_NS, COMPONENT, parent, NAME, compName);
        parent = addNewNodeToParentWithAttribute(SML_NS, SYSTEM, parent, GML_NS, ID, compId);
        addNewNodeToParentWithTextValue(GML_NS, DESCRIPTION, parent, description);
        addNewNodeToParentWithAttribute(SML_NS, IDENTIFICATION, parent, XLINK_NS, HREF, urn);
        addNewNodeToParentWithAttribute(SML_NS, DOCUMENTATION, parent, XLINK_NS, HREF, dsUrl);
        parent = addNewNodeToParent(SML_NS, OUTPUTS, parent);
        addNewNodeToParent(SML_NS, OUTPUT_LIST, parent);
    }

    public void addSmlOuptutToComponent(String compName, String outName, String definition, String uom) {
        /*
         * <sml:output name='outName'>
         *   <swe:Quantity definition='definition'>
         *     <swe:uom code='degC' />
         *   </swe:Quantity>
         * </sml:output>
         */
        // find a component that has the attribute 'name' with its value same as 'compName'
        NodeList ns = this.document.getElementsByTagNameNS(SML_NS, COMPONENT);
        Element parent = null;
        for (int n = 0; n < ns.getLength(); n++) {
            if (((Element) ns.item(n)).getAttribute(NAME).equalsIgnoreCase(compName)) {
                parent = (Element) ns.item(n);
                parent = (Element) parent.getElementsByTagNameNS(SML_NS, OUTPUT_LIST).item(0);
                parent = addNewNodeToParentWithAttribute(SML_NS, "output", parent,
                        SML_NS, NAME, outName);
                parent = addNewNodeToParentWithAttribute(SWE_NS, QUANTITY, parent,
                        SML_NS, DEFINITION, definition);
                addNewNodeToParentWithAttribute(SWE_NS, UOM, parent, CODE, uom);
            }
        }
    }

    /**
     * Removes the components node from the xml document
     */
    public void deleteComponentsNode() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(SML_NS, "components").item(0));
    }

    public void addIoosServiceMetadata1_0() {
        /*
        <sml:capabilities name="ioosServiceMetadata">
            <swe:SimpleDataRecord>
                <swe:field name="ioosTemplateVersion">
                    <swe:Text definition="http://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0">
                        <swe:value>1.0</swe:value>
                    </swe:Text>
                    </swe:field>
                    <swe:field name="softwareVersion">
                        <swe:Text definition="http://github.com/asascience-open/ncSOS/releases/tag/RC6">
                    <swe:value>RC6</swe:value>
                    </swe:Text>
                </swe:field>
            </swe:SimpleDataRecord>
        </sml:capabilities>
         * 
         */
        Element parent = getParentNode();
        parent = addNewNodeToParentWithAttribute(SML_NS, "capabilities", parent, NAME, "ioosServiceMetadata");
        Element parent1 = addNewNodeToParent(SWE_NS, "SimpleDataRecord", parent);
        
        parent = addNewNodeToParentWithAttribute(SWE_NS, "field", parent1, "", "name", "ioosTemplateVersion");
        parent = addNewNodeToParentWithAttribute(SWE_NS, "Text", parent, "", "definition", "http://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0");
        parent = addNewNodeToParentWithTextValue(SWE_NS, "value", parent, "1.0");
        
       parent = addNewNodeToParentWithAttribute(SWE_NS, "field", parent1, "", "name", "softwareVersion");
        parent = addNewNodeToParentWithAttribute(SWE_NS, "Text", parent, "", "definition", "http://github.com/asascience-open/ncSOS/releases/tag/RC6");
        parent = addNewNodeToParentWithTextValue(SWE_NS, "value", parent, "RC6");
        
    }

    /**
     * sets the name attribute of the sml:position node
     * @param name the value of the name attribute
     * @deprecated 
     */
    public void setPositionName(String name) {
        Element position = (Element) getParentNode().getElementsByTagNameNS(SML_NS, "position").item(0);
        position.setAttribute(NAME, name);
    }

    /**
     * Adds a swe:DataBlockDefinition node to sml:dataDefinition. The new node has its information filled out by the field map
     * @param fieldMap a nested hashmap of which the outer hashmap contains the name of the field (eg time) and the inner hashmap has the various info for the field (definitions, values, etc)
     * @param decimalSeparator a character (or series thereof) that define the decimal separator of the proceeding values node
     * @param blockSeparator a character (or series thereof) that define the block separator of the proceeding values node (separates group of measurements)
     * @param tokenSeparator a character (or series thereof) that define the token separator of the proceeding values node (separates individual measurements)
     * @deprecated 
     */
    public void setPositionDataDefinition(HashMap<String, HashMap<String, String>> fieldMap, String decimalSeparator, String blockSeparator, String tokenSeparator) {
        Element dataDefinition = (Element) getParentNode().getElementsByTagNameNS(SML_NS, "dataDefinition").item(0);
        // add data definition block
        Element parent = addNewNodeToParent(SWE_NS, "DataBlockDefinition", dataDefinition);
        // add components with "whenWhere"
        parent = addNewNodeToParentWithAttribute(SWE_NS, "components", parent, NAME, "whenWhere");
        // add DataRecord for lat, lon, time
        parent = addNewNodeToParent(SWE_NS, DATA_RECORD, parent);
        // print each of our maps
        Element fieldNodeIter;
        for (String key : fieldMap.keySet()) {
            if (key.equalsIgnoreCase("time")) {
                HashMap<String, String> timeMap = (HashMap<String, String>) fieldMap.get(key);
                // add field node
                fieldNodeIter = addNewNodeToParentWithAttribute(SWE_NS, FIELD, parent, NAME, key);
                // add time node
                fieldNodeIter = addNewNodeToParentWithAttribute(SWE_NS, TIME, fieldNodeIter,
                        DEFINITION, timeMap.get(DEFINITION));
                // add uoms for every key that isn't 'definition'
                for (String sKey : timeMap.keySet()) {
                    if (!sKey.equalsIgnoreCase(DEFINITION)) {
                        addNewNodeToParentWithAttribute(SWE_NS, UOM, fieldNodeIter, sKey,
                                timeMap.get(sKey).toString());
                    }
                }
            } else {
                HashMap<String, String> quantityMap = (HashMap<String, String>) fieldMap.get(key);
                // add field node
                fieldNodeIter = addNewNodeToParentWithAttribute(SWE_NS, FIELD, parent, NAME, key);
                // add quantity node
                fieldNodeIter = addNewNodeToParentWithAttribute(SWE_NS, QUANTITY, fieldNodeIter,
                        DEFINITION, quantityMap.get(DEFINITION));
                // add uoms for every key !definition
                for (String sKey : quantityMap.keySet()) {
                    if (!sKey.equalsIgnoreCase(DEFINITION)) {
                        addNewNodeToParentWithAttribute(SWE_NS, UOM, fieldNodeIter, sKey,
                                quantityMap.get(sKey).toString());
                    }
                }
            }
        }
        // lastly we need to add our encoding
        parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, "dataDefinition").item(0);
        // add encoding node
        parent = addNewNodeToParent(SWE_NS, "encoding", parent);
        // add TextBlock node with above attributes
        parent = addNewNodeToParentWithAttribute(SWE_NS, "TextBlock", parent,
                "decimalSeparator", decimalSeparator);
        parent.setAttribute("blockSeparator", blockSeparator);
        parent.setAttribute("tokenSeparator", tokenSeparator);
    }

    /**
     * adds a sml:values node to sml:position, with the parameter as its content.
     * @param valueText the content of the sml:values node
     * @deprecated 
     */
    public void setPositionValue(String valueText) {
        // simple, just add values with text content of parameter
        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, "position").item(0);
        addNewNodeToParentWithTextValue(SML_NS, "values", parent, valueText);
    }

    /**
     * Removes sml:position node from the xml document
     * @deprecated 
     */
    public void deletePosition() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(SML_NS, "position").item(0));
    }

    /**
     * Removes the sml:timePosition from the xml document
     * @deprecated 
     */
    public void deleteTimePosition() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(SML_NS, "timePosition").item(0));
    }

    /**
     * adds a sml:position node to sml:PositionList. Defines the position of the station for a profile (usually has start and end pairs).
     * @param latitudeInfo key-values for filling out needed information for the latitude field of the position (name, axisID, code, value)
     * @param longitudeInfo key-values for filling out needed information for the longitude field of the position (name, axisID, code, value)
     * @param depthInfo key-values for filling out needed information for the depth field of the position (name, axisID, code, value)
     * @param definition definition for the swe:Vector node
     * @deprecated 
     */
    public void setStationPositionsNode(HashMap<String, String> latitudeInfo, HashMap<String, String> longitudeInfo, HashMap<String, String> depthInfo, String definition) {
        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, "PositionList").item(0);

        // add position w/ 'stationPosition' attribute
        parent = addNewNodeToParentWithAttribute(SML_NS, "position", parent, NAME, "stationPosition");
        // add Position, then location nodes
        parent = addNewNodeToParent(SWE_NS, "Position", parent);
        parent = addNewNodeToParent(SWE_NS, "location", parent);
        // add vector id="STATION_LOCATION" definition=definition
        parent = addNewNodeToParentWithAttribute(SWE_NS, "Vector", parent, GML_NS, ID, "STATION_LOCATION");
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
     * defines the position of the station as a bounding box, rather than a pair of vectors.
     * @param upperDepth the upper bounding depth/altitude
     * @param lowerDepth the lower bounding depth/altitude
     * @param boundingBox a LatLonRect that defines the bounding box that encompasses the measurements of interest
     * @deprecated 
     */
    public void setStationPositionsNode(double upperDepth, double lowerDepth, LatLonRect boundingBox) {
        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, "PositionList").item(0);

        // add position w/ 'stationPosition' attribute
        parent = addNewNodeToParentWithAttribute(SML_NS, "position", parent, NAME, "stationPosition");
        // add Position, then location nodes
        parent = addNewNodeToParent(SWE_NS, "Position", parent);
        parent = addNewNodeToParent(SWE_NS, "location", parent);
        // add a bounding box for lat/lon/depth
        parent = addNewNodeToParent(GML_NS, BOUNDED_BY, parent);
        parent = addNewNodeToParentWithAttribute(GML_NS, ENVELOPE, parent, SRS_NAME, "");
        addNewNodeToParentWithTextValue(GML_NS, LOWER_CORNER, parent, boundingBox.getLatMin() + " " + boundingBox.getLonMin() + " " + upperDepth);
        addNewNodeToParentWithTextValue(GML_NS, UPPER_CORNER, parent, boundingBox.getLatMax() + " " + boundingBox.getLonMax() + " " + lowerDepth);
    }

    /**
     * adds a sml:position node to sml:PositionList. Defines the position of the station for a profile (usually has start and end pairs).
     * @param latitudeInfo key-values for filling out needed information for the latitude field of the position (name, axisID, code, value)
     * @param longitudeInfo key-values for filling out needed information for the longitude field of the position (name, axisID, code, value)
     * @param depthInfo key-values for filling out needed information for the depth field of the position (name, axisID, code, value)
     * @param definition definition for the swe:Vector node
     * @deprecated 
     */
    public void setEndPointPositionsNode(HashMap<String, String> latitudeInfo, HashMap<String, String> longitudeInfo, HashMap<String, String> depthInfo, String definition) {
        Element parent = (Element) getParentNode().getElementsByTagNameNS(SML_NS, "PositionList").item(0);

        // follow steps outlined above with slight alterations
        parent = addNewNodeToParentWithAttribute(SML_NS, "position", parent, NAME, "endPosition");
        parent = addNewNodeToParent(SWE_NS, "Position", parent);
        parent = addNewNodeToParent(SWE_NS, "location", parent);
        parent = addNewNodeToParentWithAttribute(SWE_NS, "Vector", parent, GML_NS, ID, "END_LOCATION");
        parent.setAttribute("definition", definition);
        addCoordinateInfoNode(latitudeInfo, parent, "latitude", "Y", "deg");
        addCoordinateInfoNode(longitudeInfo, parent, "longitude", "X", "deg");
        addCoordinateInfoNode(depthInfo, parent, "altitude", "Z", "m");
    }

    /**
     * removes the sml:positions from the xml document
     * @deprecated 
     */
    public void deletePositions() {
        getParentNode().removeChild(getParentNode().getElementsByTagNameNS(SML_NS, "positions").item(0));
    }
}
