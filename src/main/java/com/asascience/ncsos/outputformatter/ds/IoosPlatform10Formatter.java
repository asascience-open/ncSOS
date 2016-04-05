/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter.ds;

import com.asascience.ncsos.outputformatter.BaseOutputFormatter;
import com.asascience.ncsos.util.XMLDomUtils;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author SCowan
 */
public class IoosPlatform10Formatter extends BaseOutputFormatter {
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
    public static final String MEMBER = "member";
    private final static String OBS_TR_DEF = "http://mmisw.org/ont/ioos/swe_element_type/observationTimeRange";
    protected Namespace GML_NS,SML_NS,XLINK_NS,SWE_NS = null;

    public IoosPlatform10Formatter() {
        super();
        this.GML_NS = this.getNamespace("gml");
        this.SML_NS = this.getNamespace("sml");
        this.XLINK_NS = this.getNamespace("xlink");
        this.SWE_NS = this.getNamespace("swe");
    }

    @Override
    public String getTemplateLocation() {
        return TEMPLATE_LOCATION;
    }

    //<editor-fold defaultstate="collapsed" desc="public methods">
    /**
     * Accessor function for setting the description text of the gml:description node
     * @param description usually the description attribute value of a netcdf dataset
     */
    public void setDescriptionNode(String description) {
        XMLDomUtils.getNestedChild(this.getRoot(), DESCRIPTION, this.GML_NS).setText(description);
    }

    /**
     * Sets the text content of the gml:name node
     * @param name name (urn) of the station
     */
    public void setName(String name) {
        XMLDomUtils.getNestedChild(this.getRoot(), "name", this.GML_NS).setText(name);
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
        Element ident = addNewNode(IDENTIFIER_LIST, this.SML_NS, IDENTIFIER, this.SML_NS, NAME, name);
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
        addNewNode(parent, CODE_SPACE, this.SML_NS, HREF, this.XLINK_NS, "http://mmisw.org/ont/ioos/" + codeSpace);
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

        Element memberNode = this.getRoot().getChild(MEMBER, this.SML_NS);
        if(memberNode != null) {
            Element systemNode = memberNode.getChild(SYSTEM, this.SML_NS);
            if(systemNode != null){
                Element parentNode = systemNode.getChild(SML_CAPABILITIES, SML_NS);
                    parentNode.setAttribute(NAME, OBSERVATION_TIME_RANGE);
                    Element parent = addNewNode(parentNode, DATA_RECORD, this.SWE_NS);
                    setValidTime(parent, timeBegin, timeEnd);
                }
            }
        }

    public void setValidTime(Element parent, String timeBegin, String timeEnd) {
        parent = addNewNode(parent, FIELD, SWE_NS, NAME, OBSERVATION_TIME_RANGE);
        parent = addNewNode(parent, TIME_RANGE, SWE_NS, DEFINITION, OBS_TR_DEF);
        addNewNode(parent, SML_VALUE, SWE_NS, timeBegin + " " + timeEnd);
    }

    /**
     * Adds a sml:capabilities node that defines a networkProcedures section
     *
     * @param urn the network's urn
     */
    public void addSmlCapabilitiesNetwork(String parentName, String urn) {
        /*
         * <sml:capabilities name='name'>
         *   <swe:SimpleDataRecord>
         *     <swe:field name="network">
         *       <swe:Text definition="http://mmisw.org/ont/ioos/definition/networkID">
         *          <swe:value>{{ urn }}</swe:value>
         *       </swe:Text>
         *     </swe:field>
         *   </swe:SimpleDataRecord>
         * </sml:capabilities>
         */
        
        Element memberNode = this.getRoot().getChild(MEMBER, this.SML_NS);
        if(memberNode != null) {
            Element systemNode = memberNode.getChild(SYSTEM, this.SML_NS);
            if(systemNode != null){
                Element prevSmlCap = systemNode.getChild(SML_CAPABILITIES, SML_NS);
                int currCapIndex =  systemNode.getContent().indexOf(prevSmlCap);
                if(currCapIndex >= 0)
                    currCapIndex++;
                Element parentNode = addNewNode(systemNode, SML_CAPABILITIES, SML_NS, currCapIndex);

                
                parentNode.setAttribute( NAME, "networkProcedures");

                parentNode = addNewNode(parentNode, SIMPLEDATARECORD, SWE_NS);
  
                Element field = addNewNode(parentNode, FIELD, SWE_NS, NAME, "network-all");
                Element textNode = addNewNode(field, TEXT, SWE_NS, DEFINITION, "http://mmisw.org/ont/ioos/definition/networkID");
                addNewNode(textNode, VALUE, SWE_NS, urn);
            }
        }
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
        Element contact = XMLDomUtils.getNestedChild(this.getRoot(), CONTACT_LIST, this.SML_NS);
        contact = this.addNewNode(contact, MEMBER, this.SML_NS);
        contact.setAttribute(ROLE, role, this.XLINK_NS);
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
                Element sparent = addNewNode(parent, key, SML_NS, null);
                HashMap<String, String> vals = (HashMap<String, String>) contactInfo.get(key);
                for (String vKey : vals.keySet()) {
                    if (vals.get(vKey) != null) {
                        addNewNode(sparent, vKey, SML_NS, vals.get(vKey));
                    }
                }
            }
        }
        // add online resource if it exists
        if (onlineResource != null) {
            addNewNode(parent, ONLINERESOURCE, SML_NS, HREF, XLINK_NS, onlineResource);
        }
    }

    public void setVersionMetadata() {
        /*
        <sml:capabilities name="ioosServiceMetadata">
            <swe:SimpleDataRecord>
              <swe:field name="ioosTemplateVersion">
                <swe:Text definition="http://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0">
                  <swe:value>1.0</swe:value>
                </swe:Text>
              </swe:field>
              <swe:field name="softwareVersion">
                <swe:Text definition="http://github.com/asascience-open/ncSOS/releases">
                  <swe:value>THIS IS THE ONLY THIS THIS DOES</swe:value>
                </swe:Text>
              </swe:field>
            </swe:SimpleDataRecord>
        </sml:capabilities>
        */
        Element parent = getRoot();
        List<Element> caps = parent.getChildren(CAPABILITIES, this.SML_NS);
        for (Element e : caps) {
            if (e.getAttribute(NAME) != null && (e.getAttributeValue(NAME).equalsIgnoreCase("ioosServiceMetadata"))) {
                Element sdr = e.getChild("SimpleDataRecord", this.SWE_NS);
                List<Element> fields = sdr.getChildren("field", this.SWE_NS);
                for (Element field : fields) {
                    if (field.getAttribute(NAME) != null && field.getAttributeValue(NAME).equalsIgnoreCase("softwareVersion")) {
                        field.getChild("Text", this.SWE_NS).getChild("value", this.SWE_NS).setText(NCSOS_VERSION);
                    }
                }
            }
        }
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
        Element retval = new Element(nameOfNewNode, nameSpace);
        retval.setText(textContentValue);
        parentNode.addContent(retval);
        return retval;
    }
  
    private Element addNewNodeToParent(String nameSpace,
            String nameOfNewNode,
            Element parentNode) {
        Element retval = new Element(nameOfNewNode, nameSpace);
        parentNode.addContent(retval);
        return retval;
    }

    
    private Element addNewNodeToParentWithAttribute(
            String nameSpace,
            String nameOfNewNode, Element parentNode,
            Namespace attributeNS,
            String attributeName, String attributeValue) {
        Element retval = new Element(nameOfNewNode, nameSpace);

        if (attributeNS != null) {
            retval.setAttribute(attributeName, attributeValue, attributeNS);
        } else {
            retval.setAttribute(attributeName, attributeValue);
        }
        parentNode.addContent(retval);
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
    	String dsUrlEncoded = dsUrl;
    	try {
			dsUrlEncoded = URLEncoder.encode(dsUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // add to the <sml:ComponentList> node
        Element compList = addNewNode( COMPONENT_LIST, SML_NS, COMPONENT, SML_NS, NAME, compName);
        Element system = addNewNode(compList, SYSTEM, SML_NS, ID, GML_NS, compId);
        addNewNode(system, DESCRIPTION, GML_NS, description);
        addNewNode(system, IDENTIFICATION, SML_NS, HREF, XLINK_NS, urn);
        addNewNode(system, DOCUMENTATION, SML_NS, HREF, XLINK_NS, dsUrlEncoded);
        Element outputs = new Element(OUTPUTS, this.SML_NS);
        outputs.addContent(new Element(OUTPUT_LIST, SML_NS));
        system.addContent(outputs);
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
        Element cl = XMLDomUtils.getNestedChild(this.getRoot(), COMPONENT_LIST, this.SML_NS);
        Element parent = null;
        for (Element n : (List<Element>)cl.getChildren(COMPONENT, this.SML_NS)) {
            if (n.getAttribute(NAME) != null && n.getAttributeValue(NAME).equalsIgnoreCase(compName)) {
                parent = XMLDomUtils.getNestedChild(n, OUTPUT_LIST, this.SML_NS);
                parent = addNewNode(parent, OUTPUT, SML_NS, NAME, outName);
                parent = addNewNode(parent, QUANTITY, SWE_NS, DEFINITION, definition);
                addNewNode(parent, UOM, SWE_NS, CODE, parseUnitString(uom));
            }
        }
    }

    public static String parseUnitString(String units){   
    	String unitStr;
    	try {
			unitStr = URLEncoder.encode(units, "UTF-8").replaceAll("\\%2D", "-");
			unitStr = unitStr.replaceAll("\\%2F","/").replaceAll("\\+", "%20");
			
		} catch (UnsupportedEncodingException e) {
	        unitStr =units.replaceAll("[\\s+]", BLANK);
	        unitStr =unitStr.replaceAll("\\:\\.", BLANK);
	        //keeps
	        unitStr = unitStr.replaceAll("[^A-Za-z0-9-]", BLANK);
		}
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
