package com.asascience.ncsos.outputformatter.ds;

import com.asascience.ncsos.util.XMLDomUtils;
import org.jdom.Element;

import java.util.List;

public class IoosNetwork10Formatter extends IoosPlatform10Formatter {

    public static final String CF_CONVENTIONS = "http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html#discrete-sampling-geometries";
    
    public IoosNetwork10Formatter() {
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
        Element parent = XMLDomUtils.getNestedChild(this.getRoot(), COMPONENT_LIST, SML_NS);
        Element comp = new Element(COMPONENT, SML_NS).setAttribute(NAME, componentName);
        Element sys  = new Element(SYSTEM, SML_NS);
        sys.addContent(new Element(IDENTIFICATION, SML_NS).addContent(new Element(IDENTIFIER_LIST, SML_NS)));
        sys.addContent(new Element(SML_CAPABILITIES, SML_NS).setAttribute(NAME, "observationTimeRange").addContent(new Element(DATA_RECORD, SWE_NS)));
        sys.addContent(new Element(LOCATION, SML_NS));
        sys.addContent(new Element(OUTPUTS, SML_NS).addContent(new Element(OUTPUT_LIST, SML_NS)));
        comp.addContent(sys);
        parent.addContent(comp);
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

        Element idl = XMLDomUtils.getNestedChild(getComponent(componentName), IDENTIFIER_LIST, SML_NS);
        Element ident = addNewNode(idl, IDENTIFIER, SML_NS, NAME, identName);
        Element term  = addNewNode(ident, TERM, SML_NS, DEFINITION, identDef);
        addNewNode(term, SML_VALUE, SML_NS, identVal);
    }
    
    public void setComponentValidTime(String componentName, String beginPosition, String endPosition) {
        /*
         * <gml:TimePeriod>
         *   <gml:beginPosition>'beginPosition'</gml:beginPosition>
         *   <gml:endPosition>'endPosition'</gml:endPosition>
         * </gml:TimePeriod>
         */

        Element cap = XMLDomUtils.getNestedChild(getComponent(componentName), SML_CAPABILITIES, SML_NS);
        setValidTime(cap.getChild(DATA_RECORD, SWE_NS), beginPosition, endPosition);
    }
    
    public void setComponentLocation(String componentName, String srs, String pos) {
        /*
         * <gml:Point srsName='srs'>
         *   <gml:pos>'pos'</gml:pos>
         * </gml:Point>
         */

        Element loc = XMLDomUtils.getNestedChild(getComponent(componentName), LOCATION, SML_NS);
        Element pt = addNewNode(loc, POINT, GML_NS, SRS_NAME, srs);
        addNewNode(pt, POS, GML_NS, pos);
    }
    
    public void setComponentLocation(String componentName, String srs, List<String> pos) {
        /*
         * <gml:LineString srsName='srs'>
         *   <gml:pos>'pos0'</gml:pos>
         *   <gml:pos>'pos1'</gml:pos>
         *   ...
         * </gml:LineString>
         */

        Element loc = XMLDomUtils.getNestedChild(getComponent(componentName), LOCATION, SML_NS);
        Element ls = addNewNode(loc, LINE_STRING, GML_NS, SRS_NAME, srs);
        for (String str : pos) {
            addNewNode(ls, POS, GML_NS, str);
        }
    }
    
    public void setComponentLocation(String componentName, String srs, String lowerCorner, String upperCorner) {
        /*
         * <gml:boundedBy srsName='srs'>
         *   <gml:lowerCorner>'lowerCorner'</gml:lowerCorner>
         *   <gml:upperCorner>'upperCorner'</gml:upperCorner>
         * </gml:boundedBy>
         */

        Element loc = XMLDomUtils.getNestedChild(getComponent(componentName), LOCATION, SML_NS);
        Element bb = addNewNode(loc, BOUNDED_BY, GML_NS, SRS_NAME, srs);
        addNewNode(bb, LOWER_CORNER, GML_NS, lowerCorner);
        addNewNode(bb, UPPER_CORNER, GML_NS, upperCorner);
    }
    
    public void removeSmlLocation(){
        Element parent = XMLDomUtils.getNestedChild(this.getRoot(), SYSTEM, SML_NS);
        parent.removeChildren(LOCATION, SML_NS);
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

        Element output_list = XMLDomUtils.getNestedChild(getComponent(componentName), OUTPUT_LIST, SML_NS);
        Element output   = new Element(OUTPUT, SML_NS).setAttribute(NAME, outName);
        Element quantity = new Element(QUANTITY, SWE_NS).setAttribute(DEFINITION, outDef);
        Element uom      = new Element(UOM, SWE_NS).setAttribute(CODE, parseUnitString(units));

        quantity.addContent(uom);
        output.addContent(quantity);
        output_list.addContent(output);
    }

    private Element getComponent(String componentName) {
        Element parent = XMLDomUtils.getNestedChild(this.getRoot(), COMPONENT_LIST, SML_NS);
        List<Element> nl = parent.getChildren(COMPONENT, SML_NS);
        for (Element p : nl) {
            if (p.getAttributeValue(NAME).equals(componentName)) {
                return p;
            }
        }

        return null;
    }
}
