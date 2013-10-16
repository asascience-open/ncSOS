/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter.ds;

import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author scowan
 */
public class IoosNetwork10 extends IoosPlatform10 {

    public static final String CF_CONVENTIONS = "http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html#discrete-sampling-geometries";
    
    public IoosNetwork10() {
        super();
    }
    
   
    public void addSmlComponent(String componentName) {
        // add a new component to the ComponentList
        /*
         * <sml:component name='componentName'>
         *   <sml:System>
         *     <sml:identification>
         *       <sml:IdentifierList />
         *     </sml:identification>
         *     <sml:validTime />
         *     <sml:location />
         *     <sml:outputs>
         *       <sml:OutputList />
         *     </sml:outputs>
         *   </sml:System>
         * </sml:component>
         */
        Element parent = (Element) this.document.getElementsByTagNameNS(SML_NS, COMPONENT_LIST).item(0);
        parent = addNewNode(parent, COMPONENT, SML_NS, NAME, componentName);
        parent = addNewNode(parent, SYSTEM, SML_NS);
        addNewNode(addNewNode(parent, IDENTIFICATION, SML_NS),  IDENTIFIER_LIST, SML_NS);
        addNewNode(parent, SML_CAPABILITIES, SML_NS, NAME, "observationTimeRange");
        addNewNode(parent, LOCATION, SML_NS);
        addNewNode(addNewNode(parent, OUTPUTS, SML_NS), OUTPUT_LIST, SML_NS);
    }
    
    public void addIdentifierToComponent(String componentName, String identName, String identDef, String identVal) {
        // add identifier to component
        /*
         * <sml:identifier name='identName'>
         *   <sml:Term definition='definition'>
         *     <sml:value>'identVal'</sml:value>
         *   </sml:Term>
         * </sml:identifier>
         */
        // find our component
        Element parent = getComponent(componentName);
        if (parent == null) {
            return;
        }
        // create an identifier in the component
        parent = (Element) parent.getElementsByTagNameNS(SML_NS, IDENTIFIER_LIST).item(0);
        parent = addNewNode(parent, IDENTIFIER, SML_NS, NAME, identName);
        parent = addNewNode(parent, TERM, SML_NS, DEFINITION, identDef);
        addNewNode(parent, SML_VALUE, SML_NS, identVal);
    }
    
    public void setComponentValidTime(String componentName, String beginPosition, String endPosition) {
        /*
         * <gml:TimePeriod>
         *   <gml:beginPosition>'beginPosition'</gml:beginPosition>
         *   <gml:endPosition>'endPosition'</gml:endPosition>
         * </gml:TimePeriod>
         */
        // find our component
        Element parent = getComponent(componentName);
        if (parent == null)
            return;
        //CDM TO_DO
//        // set valid time
        parent = (Element) parent.getElementsByTagNameNS(SML_NS, SML_CAPABILITIES).item(0);
setValidTime(parent, beginPosition, endPosition);
        //        parent = addNewNode(parent, TIME_PERIOD, GML_NS);
//        addNewNode(parent, BEGIN_POSITION, GML_NS, beginPosition);
//        addNewNode(parent, END_POSITION, GML_NS, endPosition);
    }
    
    public void setComponentLocation(String componentName, String srs, String pos) {
        /*
         * <gml:Point srsName='srs'>
         *   <gml:pos>'pos'</gml:pos>
         * </gml:Point>
         */
        // find our component
        Element parent = getComponent(componentName);
        if (parent == null)
            return;
        
        // set location
        parent = (Element) parent.getElementsByTagNameNS(SML_NS, LOCATION).item(0);
        parent = addNewNode(parent, POINT, GML_NS, SRS_NAME, srs);
        addNewNode(parent, POS, GML_NS, pos);
    }
    
    public void setComponentLocation(String componentName, String srs, List<String> pos) {
        /*
         * <gml:LineString srsName='srs'>
         *   <gml:pos>'pos0'</gml:pos>
         *   <gml:pos>'pos1'</gml:pos>
         *   ...
         * </gml:LineString>
         */
        // find our component
        Element parent = getComponent(componentName);
        if (parent == null)
            return;
        
        // set location
        parent = (Element) parent.getElementsByTagNameNS(SML_NS, LOCATION).item(0);
        parent = addNewNode(parent, LINE_STRING, GML_NS, SRS_NAME, srs);
        for (String str : pos) {
            addNewNode(parent, POS, GML_NS, str);
        }
    }
    
    public void setComponentLocation(String componentName, String srs, String lowerCorner, String upperCorner) {
        /*
         * <gml:boundedBy srsName='srs'>
         *   <gml:lowerCorner>'lowerCorner'</gml:lowerCorner>
         *   <gml:upperCorner>'upperCorner'</gml:upperCorner>
         * </gml:boundedBy>
         */
        // find the component
        Element parent = getComponent(componentName);
        if (parent == null)
            return;
        
        // set location
        parent = (Element) parent.getElementsByTagNameNS(SML_NS, LOCATION).item(0);
        parent = addNewNode(parent, BOUNDED_BY, GML_NS, SRS_NAME, srs);
        addNewNode(parent, LOWER_CORNER, GML_NS, lowerCorner);
        addNewNode(parent, UPPER_CORNER, GML_NS, upperCorner);
    }
    
    public void addComponentOutput(String componentName, String outName, String outURN, String outDef, String featureType, String units) {
        /*
         * <sml:output name='outName' xlink:title='outURN'>
         *   <sml:Quantity definition='outDef'>
         *     <gml:metaDataProperty>
         *       <gml:name codeSpace='CF_CONVENTIONS'>'featureType'</gml:name>
         *     </gml:metaDataProperty>
         *     <swe:uom code='units' />
         *   </sml:Quantity>
         * </sml:output>
         */
        Element parent = getComponent(componentName);
        if (parent == null) {
            return;
        }
        // add output
        parent = (Element) parent.getElementsByTagNameNS(SML_NS, OUTPUT_LIST).item(0);
        parent = addNewNode(parent, OUTPUT, SML_NS, NAME, outName);
        parent.setAttributeNS(XLINK_NS, TITLE, outURN);
        parent = addNewNode(parent, QUANTITY, SWE_NS, DEFINITION, outDef);
        addNewNode(addNewNode(parent, META_DATA_PROP, GML_NS), NAME, GML_NS, CODE_SPACE, CF_CONVENTIONS);
        addNewNode(parent, UOM, SWE_NS, CODE, units);
    }
    
    
    private Element getComponent(String componentName) {
        Element parent = (Element) this.document.getElementsByTagNameNS(SML_NS, COMPONENT_LIST).item(0);
        NodeList nl = parent.getElementsByTagNameNS(SML_NS, COMPONENT);
        for (int n=0; n<nl.getLength(); n++) {
            parent = (Element) nl.item(n);
            if (parent.getAttribute(NAME) == null ? componentName == null : parent.getAttribute(NAME).equals(componentName))
                break;
            parent = null;
        }
        return parent;
    }
}
