package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

/**
 * Provides common functions for classes that define the response outputs for various
 * SOS requests. Most importantly, provides a common end point for writing the response
 * to a writer provided by the controller.
 */
public abstract class XmlOutputFormatter extends OutputFormatter {

    private HashMap<String, Namespace> namespaces;
    public static final String NCSOS_VERSION = com.asascience.ncsos.outputformatter.XmlOutputFormatter.class.getPackage().getImplementationVersion();
    public static final String OBSERVATION = "Observation";
    public static final String OBSERVATION_COLLECTION = "ObservationCollection";
    public static final String MEMBER = "member";
    public static final String SAMPLING_TIME = "samplingTime";
    public static final String META_DATA_PROP = "metaDataProperty";
    public static final String TIME_PERIOD = "TimePeriod";
    public static final String BEGIN_POSITION = "beginPosition";
    public static final String END_POSITION = "endPosition";
    public static final String IDENTIFICATION = "identification";
    public static final String BOUNDED_BY = "boundedBy";
    public static final String ENVELOPE = "Envelope";
    public static final String UPPER_CORNER = "upperCorner";
    public static final String LOWER_CORNER = "lowerCorner";
    public static final String GENERIC_META_DATA = "GenericMetaData";
    public static final String POINT = "Point";
    public static final String POS = "pos";
    public static final String RESULT = "result";
    public static final String NAME = "name";
    public static final String TIME = "time";
    public static final String ITEM = "item";
    public static final String FIELD = "field";
    public static final String CODE_SPACE = "codeSpace";
    public static final String CODE = "code";
    public static final String DEFINITION = "definition";
    public static final String UOM = "uom";
    public static final String TERM = "Term";
    public static final String OFFERING = "offering";
    public static final String OBSERVED_PROPERTY = "observedProperty";
    public static final String OBSERVATION_OFFERING = "ObservationOffering";
    public static final String CAPABILITIES = "Capabilities";
    public static final String SML_CAPABILITIES = "capabilities";
    public static final String OBSERVATION_OFFERING_LIST = "ObservationOfferingList";
    public static final String EVENT_TIME = "eventTime";
    public static final String PROCEDURE = "procedure";
    public static final String ALLOWED_VALUES = "AllowedValues";
    public static final String PARAMETER = "Parameter";
    public static final String MINIMUM_VALUE = "MinimumValue";
    public static final String MAXIMUM_VALUE = "MaximumValue";
    public static final String SYSTEM = "System";
    public static final String COMPONENT_LIST = "ComponentList";
    public static final String COMPONENT = "component";
    public static final String HREF = "href";
    public static final String VALUE = "Value";
    public static final String IDENTIFIER_LIST = "IdentifierList";
    public static final String RESPONSE_FORMAT = "responseFormat";
    public static final String NETWORK_URN = "urn:ioos:network:";
    public static final String NETWORK_URN_END_ALL = ":all";
    public static final String DESCRIPTION = "description";
    public static final String RESULT_MODEL = "resultModel";
    public static final String RESPONSE_MODE = "responseMode";
    public static final String ID = "id";
    public static final String VALID_TIME = "validTime";
    public static final String LOCATION = "location";
    public static final String LINE_STRING = "LineString";
    public static final String SRS_NAME = "srsName";
    public static final String FEATURE_INTEREST = "featureOfInterest";
    public static final String FEATURE_COLLECTION = "FeatureCollection";
    public static final String USE = "use";
    public static final String REQUIRED = "required";
    public static final String HISTORY = "history";
    public static final String IDENTIFIER = "identifier";
    public static final String SML_VALUE = "value";
    public static final String OUTPUTS = "outputs";
    public static final String OUTPUT = "output";
    public static final String TIME_RANGE = "TimeRange";
    public static final String OUTPUT_LIST = "OutputList";
    public static final String QUANTITY = "Quantity";
    public static final String AXIS_ID = "axisID";
    public static final String VERSION = "version";
    public static final String TITLE = "title";
    public static final String DATA_RECORD = "DataRecord";
    public static final String DATA_ARRAY = "DataArray";
    public static final String COORDINATE = "coordinate";
    public static final String ELEMENT_COUNT = "elementCount";
    public static final String CONTACT_LIST = "ContactList";
    public static final String COUNT = "Count";
    public static final String VALUES = "values";

    protected Document  document;

    public XmlOutputFormatter() {
        this.document = XMLDomUtils.loadFile(getClass().getClassLoader().getResourceAsStream(this.getTemplateLocation()));
        this.initNamespaces();
    }

     public Element getRoot() {
        return this.document.getRootElement();
    }

    protected void initNamespaces() {
        this.namespaces = new HashMap<String, Namespace>();
        Element root = this.getRoot();
        this.namespaces.put(root.getNamespace().getPrefix().toLowerCase(), root.getNamespace());
        for (Namespace a : (List<Namespace>) root.getAdditionalNamespaces()) {
            this.namespaces.put(a.getPrefix().toLowerCase(), a);
        }
    }

    public Namespace getNamespace(String namespace) {
        return this.namespaces.get(namespace.toLowerCase());
    }

    
  

    protected void setupException(String message) {
        ErrorFormatter ef = new ErrorFormatter();
        ef.setException(message);
        this.document = ef.document;
    }
    protected void setupException(String message, String exceptionCode) {
        ErrorFormatter ef = new ErrorFormatter();
        ef.setException(message, exceptionCode);
        this.document = ef.document;
    }
    protected void setupException(String message, String exceptionCode, String locator) {
        ErrorFormatter ef = new ErrorFormatter();
        ef.setException(message, exceptionCode, locator);
        this.document = ef.document;
    }
    
    
    protected String getNcsosVersion(){
    	String ncVer = NCSOS_VERSION;
    	// Workaround for issue with tomcat 8.0.24 and accessing the manifest file of the libraries
    	if(ncVer == null){
    		try {
    			String temp = OutputFormatter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
    			ncVer = temp.substring(temp.lastIndexOf("ncsos-") + 6, temp.indexOf(".jar"));
    		} catch (Exception e1) {}

    	}
    	return ncVer;
    }
    
    public String getContentType() {
        return "text/xml";
    }
    
    public void writeOutput(Writer writer) throws IOException {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(this.document, writer);
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
        Namespace gmlns = this.getNamespace("gml");
        Element bb = XMLDomUtils.getNestedChild(this.getRoot(), "boundedBy", gmlns);
        Element env = new Element("Envelope", gmlns);
        env.setAttribute("srsName", srsName);
        env.addContent(new Element("lowerCorner", gmlns).setText(lowerCorner));
        env.addContent(new Element("upperCorner", gmlns).setText(upperCorner));
        bb.addContent(env);
    }

    protected Element addNewNode(Element parent, String nodeName, Namespace nodeNS) {
        Element child = new Element(nodeName, nodeNS);
        parent.addContent(child);
        return child;
    }

    protected Element addNewNode(Element parent, String nodeName, Namespace nodeNS, int childIndex) {
        Element child = new Element(nodeName, nodeNS);
        if(childIndex >= 0)
            parent.addContent(childIndex, child);
        else 
            parent.addContent(child);
        return child;
    }
    
    protected Element addNewNode(Element parent,
            String nodeName,
            Namespace nodeNS,
            String textValue) {
        Element child = new Element(nodeName, nodeNS);
        child.setText(textValue);
        parent.addContent(child);
        return child;
    }

    protected Element addNewNode(Element parent,
            String nodeName,
            Namespace nodeNS,
            String attrName,
            String attrValue) {
        return addNewNode(parent, nodeName, nodeNS, attrName, null, attrValue);
    }

    protected Element addNewNode(Element parent,
            String nodeName,
            Namespace nodeNS,
            String attrName,
            Namespace attrNS,
            String attrValue) {
        Element child = new Element(nodeName, nodeNS);
        if (attrNS == null) {
            child.setAttribute(attrName, attrValue);
        } else {
            child.setAttribute(attrName, attrValue, attrNS);
        }
        parent.addContent(child);
        return child;
    }

    protected Element addNewNode(String parentName,
                                 Namespace parentNS,
                                 String nodeName,
                                 Namespace nodeNS,
                                 String attrName,
                                 String attrValue) {
        Element parent = XMLDomUtils.getNestedChild(this.getRoot(), parentName, parentNS);
        return this.addNewNode(parent, nodeName, nodeNS, attrName, attrValue);
    }

    protected Element addNewNode(String parentName,
                                 Namespace parentNS,
                                 String nodeName,
                                 Namespace nodeNS) {
        Element parent = XMLDomUtils.getNestedChild(this.getRoot(), parentName, parentNS);
        return this.addNewNode(parent, nodeName, nodeNS);
    }
    /**
     * Returns the template path to parse
     */
    protected abstract String getTemplateLocation();

  
 
    
}
