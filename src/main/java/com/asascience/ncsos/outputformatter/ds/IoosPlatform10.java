/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter.ds;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asascience.ncsos.outputformatter.BaseOutputFormatter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author SCowan
 */
public class IoosPlatform10 extends BaseOutputFormatter {
    public static final String BLANK = "";
    public static final String CAPABILITIES = "capabilities";
    public static final String CLASSIFIER = "classifier";
    public static final String CLASSIFIERLIST = "ClassifierList";

    public static final String CONTACTINFO = "contactInfo";
    public static final String DEFINITION = "definition";
    public static final String DOCUMENTATION = "documentation";
    public static final String FIELD = "field";
    public static final String NAME = "name";
    public static final String ONLINERESOURCE = "onlineResource";
    public static final String ORGANIZATION_NAME = "organizationName";
    public static final String RESPONSIBLE_PARTY = "ResponsibleParty";
    public static final String ROLE = "role";
    public static final String SIMPLEDATARECORD = "SimpleDataRecord";
    public static final String TEXT = "Text";
    public static final String VALUE = "value";
    private static final String TEMPLATE_LOCATION = "templates/DS_ioos10.xml";
    private final static String IOOSURL = "http://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0";
    private final static String OBSERVATION_TIME_RANGE = "observationTimeRange";
    private final static String OBS_TR_DEF = "http://mmisw.org/ont/ioos/definition/observationTimeRange";

    public IoosPlatform10() {
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
        document.getElementsByTagNameNS(GML_NS, DESCRIPTION).item(0).setTextContent(description);
    }

    /**
     * Sets the text content of the gml:name node
     * @param name name (urn) of the station
     */
    public void setName(String name) {
        document.getElementsByTagNameNS(GML_NS, NAME).item(0).setTextContent(name);
    }

    public void setBoundedBy(String srsName, String lowerCorner, String upperCorner) {
        /*
         * <gml:boundedBy>
         *   <gml:Envelope srsName='srsName'>
         *     <gml:lowerCorner>'lowerCorner'</gml:lowerCorner>
         *     <gml:upperCorner>'upperCorner'</gml:upperCorner>
         *   </gml:Envelope>
         * </gml:boundedBy>
         */
        Element parent = (Element) this.document.getElementsByTagNameNS(GML_NS, BOUNDED_BY).item(0);
        parent = addNewNode(parent, ENVELOPE, GML_NS, SRS_NAME, srsName);
        addNewNode(parent, LOWER_CORNER, GML_NS, lowerCorner);
        addNewNode(parent, UPPER_CORNER, GML_NS, upperCorner);
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
        Element ident = addNewNode(IDENTIFIER_LIST, SML_NS, IDENTIFIER, SML_NS, NAME, name);
        ident = addNewNode(ident, TERM, SML_NS, DEFINITION, definition);
        addNewNode(ident, SML_VALUE, SML_NS, value);
    }

    /**
     * Accessor for setting the sml:classification node in the xml document
     * @param classifierName name of the station classification (eg 'platformType')
     * @param definition sml:Term definition of the classification
     * @param classifierValue value of the classification (eg 'GLIDER')
     */
    public void addToClassificationNode(String classifierName, String definition, String classifierValue) {
        Element parent = addNewNode(CLASSIFIERLIST, SML_NS, CLASSIFIER, SML_NS, NAME, classifierName);
        parent = addNewNode(parent, TERM, SML_NS, DEFINITION, definition);
        addNewNode(parent, SML_VALUE, SML_NS, classifierValue);
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
        Element parent = addNewNode(CLASSIFIERLIST, SML_NS, CLASSIFIER, SML_NS, NAME, name);
        parent = addNewNode(parent, TERM, SML_NS, DEFINITION, definition);
        addNewNode(parent, CODE_SPACE, SML_NS, HREF, XLINK_NS, "http://mmisw.org/ont/ioos/" + codeSpace);
        addNewNode(parent, SML_VALUE, SML_NS, value);
    }

    /**
     * Set the sml:validTime node
     * @param timeBegin the beginPosition value
     * @param timeEnd the endPosition value
     */
    public void setValidTime(String timeBegin, String timeEnd) {
        /*
        
        <sml:capabilities name="observationTimeRange">
        <swe:DataRecord>
        <swe:field name="observationTimeRange">
        <swe:TimeRange definition="http://mmisw.org/ont/ioos/definition/observationTimeRange">
        <swe:value>2008-04-28T08:00:00.000Z 2012-12-27T19:00:00.000Z</swe:value>
        </swe:TimeRange>
        </swe:field>
        </swe:DataRecord>
        </sml:capabilities>     
        
         */
        Element parentNode = (Element) this.document.getElementsByTagNameNS(SML_NS, SML_CAPABILITIES).item(0);
        parentNode.setAttribute(NAME, OBSERVATION_TIME_RANGE);
        Element parent = addNewNode(SML_CAPABILITIES, SML_NS, DATA_RECORD, SWE_NS);
        setValidTime(parent, timeBegin, timeEnd);

    }

    public void setValidTime(Element parent, String timeBegin, String timeEnd) {
        parent = addNewNode(parent, FIELD, SWE_NS, NAME, OBSERVATION_TIME_RANGE);
        parent = addNewNode(parent, TIME_RANGE, SWE_NS, DEFINITION, OBS_TR_DEF);
        addNewNode(parent, SML_VALUE, SWE_NS, timeBegin + " " + timeEnd);
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
        Element parent = (Element) ((parentName != null) ? this.document.getElementsByTagNameNS(SML_NS, parentName).item(0) : this.getParentNode());
        parent = addNewNode(parent, CAPABILITIES, SML_NS, NAME, name);
        parent = addNewNode(parent, SIMPLEDATARECORD, SWE_NS);
        parent = addNewNode(parent, META_DATA_PROP, GML_NS, TITLE, XLINK_NS, title);
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
        // add sml:member as the head node, in the ContactList
//        document = XMLDomUtils.addNode(document, "sml:System", "sml:contact", "sml:history");
        Element contact = (Element) getParentNode().getElementsByTagNameNS(SML_NS, CONTACT_LIST).item(0);
        contact = this.addNewNode(contact, MEMBER, SML_NS);
        contact.setAttributeNS(XLINK_NS, ROLE, role);
        /* *** */
        Element parent = addNewNode(contact, RESPONSIBLE_PARTY, SML_NS);
        /* *** */
        addNewNode(parent, ORGANIZATION_NAME, SML_NS, organizationName);
        /* *** */
        parent = addNewNode(parent, CONTACTINFO, SML_NS);



        /* *** */
        // super nesting for great justice
        if (contactInfo != null) {
            for (String key : contactInfo.keySet()) {
                // add key as node
                Element sparent = addNewNode(parent, key, null);
                HashMap<String, String> vals = (HashMap<String, String>) contactInfo.get(key);
                for (String vKey : vals.keySet()) {
                    if (vals.get(vKey) != null) {
                        addNewNode(sparent, vKey, SML_NS, vals.get(vKey).toString());
                    }
                }
            }
        }
        // add online resource if it exists
        if (onlineResource != null) {
            addNewNode(parent, ONLINERESOURCE, SML_NS, HREF, XLINK_NS, onlineResource);
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
        parent = addNewNodeToParentWithAttribute(SML_NS, CAPABILITIES, parent, NAME, "ioosServiceMetadata");
        Element parent1 = addNewNodeToParent(SWE_NS, SIMPLEDATARECORD, parent);
        
        parent = addNewNodeToParentWithAttribute(SWE_NS, FIELD, parent1, BLANK, NAME, "ioosTemplateVersion");
        parent = addNewNodeToParentWithAttribute(SWE_NS, TEXT, parent, BLANK, DEFINITION, "http://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0");
        parent = addNewNodeToParentWithTextValue(SWE_NS, VALUE, parent, "1.0");
        
       parent = addNewNodeToParentWithAttribute(SWE_NS, FIELD, parent1, BLANK, NAME, "softwareVersion");
        parent = addNewNodeToParentWithAttribute(SWE_NS, TEXT, parent, BLANK, DEFINITION, "http://github.com/asascience-open/ncSOS/releases/tag/RC6");
        parent = addNewNodeToParentWithTextValue(SWE_NS, VALUE, parent, "RC6");
        
    }
    
      private Element addNewNodeToParentWithAttribute(
            String nameSpace,
            String nameOfNewNode, Element parentNode,
            String attributeName, String attributeValue) {

        return addNewNodeToParentWithAttribute(nameSpace,
                nameOfNewNode, parentNode, null,
                attributeName, attributeValue);
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
        Element parent = addNewNode(COMPONENT_LIST, SML_NS, COMPONENT, SML_NS, NAME, compName);
        parent = addNewNode(parent, SYSTEM, SML_NS, ID, GML_NS, compId);
        addNewNode(parent, DESCRIPTION, GML_NS, description);
        addNewNode(parent, IDENTIFICATION, SML_NS, HREF, XLINK_NS, urn);
        addNewNode(parent, DOCUMENTATION, SML_NS, HREF, XLINK_NS, dsUrl);
        parent = addNewNode(parent, OUTPUTS, SML_NS);
        addNewNode(parent, OUTPUT_LIST, SML_NS);
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
        NodeList ns = this.document.getElementsByTagNameNS(SML_NS, COMPONENT);
        Element parent = null;
        for (int n = 0; n < ns.getLength(); n++) {
            if (((Element) ns.item(n)).getAttribute(NAME).equalsIgnoreCase(compName)) {
                parent = (Element) ns.item(n);
                parent = (Element) parent.getElementsByTagNameNS(SML_NS, OUTPUT_LIST).item(0);
                parent = addNewNode(parent, OUTPUT, SML_NS, NAME, outName);
                parent = addNewNode(parent, QUANTITY, SWE_NS, DEFINITION, definition);
                addNewNode(parent, UOM, SWE_NS, CODE, parseUnitString(uom));
            }
        }
    }

    public static String parseUnitString(String units){        
        String unitStr =units.replaceAll("[\\s+]", BLANK);
        unitStr =unitStr.replaceAll("\\:\\.", BLANK);
        //keeps
        unitStr = unitStr.replaceAll("[^A-Za-z0-9-]", BLANK);
        return unitStr;
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
        Element parent = addNewNode(LOCATION, SML_NS, POINT, GML_NS, SRS_NAME, srsName);
        addNewNode(parent, POS, GML_NS, pos);
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
        Element parent = addNewNode(LOCATION, SML_NS, LINE_STRING, GML_NS, SRS_NAME, srsName);
        for (String pos : posLine) {
            addNewNode(parent, POS, GML_NS, pos);
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
        Element parent = addNewNode(LOCATION, SML_NS, BOUNDED_BY, GML_NS, SRS_NAME, srsName);
        addNewNode(parent, LOWER_CORNER, GML_NS, lowerCorner);
        addNewNode(parent, UPPER_CORNER, GML_NS, upperCorner);
    }
    //</editor-fold>
}
