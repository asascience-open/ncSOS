/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import java.util.HashMap;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author SCowan
 */
public class DescribeSensorPlatformMilestone1_0 extends BaseOutputFormatter {
    
    private static final String TEMPLATE_LOCATION = "templates/describePlatformM1.0.xml";
    private final static String IOOSURL = "http://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0";

    public DescribeSensorPlatformMilestone1_0() {
        super();
        loadTemplateXML(TEMPLATE_LOCATION);
    }
    
    //<editor-fold defaultstate="collapsed" desc="public methods">
    /**
     * Accessor function for setting the description text of the gml:description node
     * @param description usually the description attribute value of a netcdf dataset
     */
    public void setDescriptionNode(String description) {
        // get our description node and set its string content
        document.getElementsByTagName("gml:description").item(0).setTextContent(description);
    }
    
    /**
     * Sets the text content of the gml:name node
     * @param name name (urn) of the station
     */
    public void setName(String name) {
         document.getElementsByTagName("gml:name").item(0).setTextContent(name);
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
        Element ident = addNewNode("sml:IdentifierList", "sml:identifier",  "name", name);
        ident = addNewNode(ident, "sml:Term", "definition", definition);
        addNewNode(ident, "sml:value", value);
    }
    
    /**
     * Accessor for setting the sml:classification node in the xml document
     * @param classifierName name of the station classification (eg 'platformType')
     * @param definition sml:Term definition of the classification
     * @param classifierValue value of the classification (eg 'GLIDER')
     */
    public void addToClassificationNode(String classifierName, String definition, String classifierValue) {
        Element parent = addNewNode("sml:ClassifierList", "sml:classifier", "name", classifierName);
        parent = addNewNode(parent, "sml:Term", "definition", definition);
        addNewNode(parent, "sml:value", classifierValue);
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
        Element parent = addNewNode("sml:ClassifierList", "sml:classifier", "name", name);
        parent = addNewNode(parent, "sml:Term", "definition", definition);
        addNewNode(parent, "sml:codeSpace", "xlink:href", "http://mmisw.org/ont/ioos/" + codeSpace);
        addNewNode(parent, "sml:value", value);
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
       Element parent = addNewNode("sml:validTime", "gml:TimePeriod");
       addNewNode(parent, "gml:beginPosition", timeBegin);
       addNewNode(parent, "gml:endPosition", timeEnd);
    }
    
    /**
     * Adds a sml:capabilities node that defines a metadata property for the sensor
     * @param name name of the capability
     * @param title name of the metadata
     * @param href reference to the metadata
     */
    public void addSmlCapabilitiesGmlMetadata(String parentName, String name, String title, String href) {
        /*
         * <sml:capabilities name='name'>
         *   <swe:SimpleDataRecord>
         *     <gml:metaDataProperty xlink:title='title' xlink:href='href' />
         *   </swe:SimpleDataRecord>
         * </sml:capabilities>
         */
        Element parent = (Element) ((parentName != null) ? this.document.getElementsByTagName(parentName).item(0) : this.getParentNode());
        parent = addNewNode(parent, "sml:capabilities", "name", name);
        parent = addNewNode(parent, "swe:SimpleDataRecord");
        parent = addNewNode(parent, "gml:metaDataProperty", "xlink:title", title);
        parent.setAttribute("xlink:href", href);
    }
    
    /**
     * Accessor method to add a contact node to the sml:System node structure. Most
     * information is gathered from global Attributes of the dataset
     * @param role the role of the contact (eg publisher)
     * @param organizationName the name of the orginization or individual
     * @param contactInfo info for contacting the...um...contact
     */
    public void addContactNode(String role, String organizationName, HashMap<String, HashMap<String, String>> contactInfo, String onlineResource) {
        // add sml:member as the head node, in the ContactList
//        document = XMLDomUtils.addNode(document, "sml:System", "sml:contact", "sml:history");
        Element contact = (Element) getParentNode().getElementsByTagName("sml:ContactList").item(0);
        contact = this.addNewNode(contact, "sml:member");
        contact.setAttribute("xlink:role", role);
        /* *** */
        Element parent = addNewNode(contact, "sml:ResponseibleParty");
        /* *** */
        addNewNode(parent, "sml:organizationName", organizationName);
        /* *** */
        parent = addNewNode(parent, "sml:contactInfo");
        /* *** */
        // super nesting for great justice
        if (contactInfo != null) {
            for (String key : contactInfo.keySet()) {
                // add key as node
                Element sparent = addNewNode(parent, key);
                HashMap<String, String> vals = (HashMap<String, String>)contactInfo.get(key);
                for (String vKey : vals.keySet()) {
                    if (vals.get(vKey) != null)
                        addNewNode(sparent, vKey, vals.get(vKey).toString());
                }
            }
        }
        // add online resource if it exists
        if (onlineResource != null) {
            addNewNode(parent, "sml:onlineResource", "xlink:href", onlineResource);
        }
    }

    /**
     * Adds constanct xml construct <sml:capabilities name="ioosServiceMetadata">
     */
    public void addIoosServiceMetadata1_0() {
        /*
         * <sml:capabilities name="ioosServiceMetadata">
         *   <swe:SimpleDataRecord>
         *     <gml:metaDataProperty xlink:title="ioosTemplateVersion" xlink:href="http://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0">
         *       <gml:version>1.0</gml:version>
         *     </gml:metaDataProperty>
         *   </swe:SimpleDataRecord>
         * </sml:capabilities>
         */
        Element parent = getParentNode();
        parent = addNewNode(parent, "sml:capabilities", "name", "ioosServiceMetadata");
        parent = addNewNode(parent, "swe:SimpleDataRecord");
        parent = addNewNode(parent, "gml:metaDataProperty", "xlink:title", "ioosTemplateVersion");
        parent.setAttribute("xlink:href", IOOSURL);
        addNewNode(parent, "gml:version", "1.0");
    }
    
    /**
     * Adds an <sml:component> complex element, to the ComponentList
     * @param compName name of the component
     * @param compId id of the component
     * @param description description of the component
     * @param urn URN of the component
     * @param dsUrl describe sensor URL for the component
     */
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
        Element parent = addNewNode("sml:ComponentList", "sml:component", "name", compName);
        parent = addNewNode(parent, "sml:System", "gml:id", compId);
        addNewNode(parent, "gml:description", description);
        addNewNode(parent, "sml:identification", "xlink:href", urn);
        addNewNode(parent, "sml:documentation", "xlink:href", dsUrl);
        parent = addNewNode(parent, "sml:outputs");
        addNewNode(parent, "sml:OutputList");
    }
    
    /**
     * Adds an output measurement for a component
     * @param compName component name to add to
     * @param outName name of output
     * @param definition definition for the output
     * @param uom units of the output's measurement
     */
    public void addSmlOuptutToComponent(String compName, String outName, String definition, String uom) {
        /*
         * <sml:output name='outName'>
         *   <swe:Quantity definition='definition'>
         *     <swe:uom code='degC' />
         *   </swe:Quantity>
         * </sml:output>
         */
        // find a component that has the attribute 'name' with its value same as 'compName'
        NodeList ns = this.document.getElementsByTagName("sml:component");
        Element parent = null;
        for (int n=0; n<ns.getLength(); n++) {
            if (((Element)ns.item(n)).getAttribute("name").equalsIgnoreCase(compName)) {
                parent = (Element) ns.item(n);
                parent = (Element) parent.getElementsByTagName("sml:OutputList").item(0);
                parent = addNewNode(parent, "sml:output", "name", outName);
                parent = addNewNode(parent, "swe:Quantity", "definition", definition);
                addNewNode(parent, "swe:uom", "code", uom);
            }
        }
    }
    
    /**
     * Sets a gml:Point for the location
     * @param srsName srs/crs projection being used for coords
     * @param pos lat lon of the location
     */
    public void setSmlPosLocationPoint(String srsName, String pos) {
        /*
         * <sml:location>
         *   <gml:Point srsName='srsName'>
         *     <gml:pos>'pos'</gml:pos>
         *   </gml:Point>
         * </sml:location
         */
        Element parent = addNewNode("sml:location", "gml:Point", "srsName", srsName);
        addNewNode(parent, "gml:pos", pos);
    }
    
    public void setSmlPosLocationLine(String srsName, List<String> posLine) {
        /*
         * <sml:location>
         *   <gml:LineString srsName='srsName'>
         *     <gml:pos>'pos[1]'</gml:pos>
         *     <gml:pos>'pos[2]'</gml:pos>
         *     ...
         *   </gml:LineString>
         * </sml:location>
         */
        Element parent = addNewNode("sml:location", "gml:LineString", "srsName", srsName);
        for (String pos : posLine) {
            addNewNode(parent, "gml:pos", pos);
        }
    }
    
    public void setSmlPosLocationBbox(String srsName, String lowerCorner, String upperCorner) {
        /*
         * <sml:location>
         *   <gml:boundedBy srsName='srsName'>
         *     <gml:lowerCorner>'lowerCorenr'</gml:lowerCorner>
         *     <gml:upperCorner>'upperCorner'</gml:upperCorner>
         *   </gml:boundedBy>
         * </sml:location
         */
        Element parent = addNewNode("sml:location", "gml:boundedBy", "srsName", srsName);
        addNewNode(parent, "gml:lowerCorner", lowerCorner);
        addNewNode(parent, "gml:upperCorner", upperCorner);
    }
    
    //</editor-fold>
    
}
