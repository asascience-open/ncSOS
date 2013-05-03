/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author scowan
 */
public class DescribeSensorNetworkMilestone1_0 extends DescribeSensorPlatformMilestone1_0 {
    
    public static final String NETWORK_TEMPLATE = "templates/describeNetworkM1.0.xml";
    public static final String CF_CONVENTIONS = "http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html#discrete-sampling-geometries";
    
    public DescribeSensorNetworkMilestone1_0() {
        super();
        loadTemplateXML(NETWORK_TEMPLATE);
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
        Element parent = (Element) this.document.getElementsByTagName("gml:boundedBy").item(0);
        parent = addNewNode(parent, "gml:Envelope", "srsName", srsName);
        addNewNode(parent, "gml:lowerCorner", lowerCorner);
        addNewNode(parent, "gml:upperCorner", upperCorner);
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
        Element parent = (Element) this.document.getElementsByTagName("sml:ComponentList").item(0);
        parent = addNewNode(parent, "sml:component", "name", componentName);
        parent = addNewNode(parent, "sml:System");
        addNewNode(addNewNode(parent, "sml:identification"), "sml:IdentifierList");
        addNewNode(parent, "sml:validTime");
        addNewNode(parent, "sml:location");
        addNewNode(addNewNode(parent, "sml:outputs"), "sml:OutputList");
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
        parent = (Element) parent.getElementsByTagName("IdentifierList").item(0);
        parent = addNewNode(parent, "sml:identifier", "name", identName);
        parent = addNewNode(parent, "sml:Term", "definition", identDef);
        addNewNode(parent, "sml:value", identVal);
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
        
        // set valid time
        parent = (Element) parent.getElementsByTagName("sml:validTime").item(0);
        parent = addNewNode(parent, "gml:TimePeriod");
        addNewNode(parent, "gml:beginPosition", beginPosition);
        addNewNode(parent, "gml:endPosition", endPosition);
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
        parent = (Element) parent.getElementsByTagName("sml:location").item(0);
        parent = addNewNode(parent, "gml:Point", "srsName", srs);
        addNewNode(parent, "gml:pos", pos);
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
        parent = (Element) parent.getElementsByTagName("sml:location").item(0);
        parent = addNewNode(parent, "gml:LineString", "srsName", srs);
        for (String str : pos) {
            addNewNode(parent, "gml:pos", str);
        }
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
        parent = (Element) parent.getElementsByTagName("sml:OutputList").item(0);
        parent = addNewNode(parent, "sml:output", "name", outName);
        parent.setAttribute("xlink:title", outURN);
        parent = addNewNode(parent, "swe:Quantity", "definition", outDef);
        addNewNode(addNewNode(parent, "gml:metaDataProperty"), "gml:name", "codeSpace", CF_CONVENTIONS);
        addNewNode(parent, "swe:uom", "code", units);
    }
    
    
    private Element getComponent(String componentName) {
        Element parent = (Element) this.document.getElementsByTagName("sml:ComponentList").item(0);
        NodeList nl = parent.getElementsByTagName("sml:component");
        for (int n=0; n<nl.getLength(); n++) {
            parent = (Element) nl.item(n);
            if (parent.getAttribute("name") == null ? componentName == null : parent.getAttribute("name").equals(componentName))
                break;
            parent = null;
        }
        return parent;
    }
}
