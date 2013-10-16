/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides common functions for classes that define the response outputs for various
 * SOS requests. Most importantly, provides a common end point for writing the response
 * to a writer provided by the controller.
 * @author SCowan
 * @version 1.0.0
 */
public abstract class OutputFormatter {

    private Map<String, String> nsToPrefix;
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
    public static final String OBSERVATION_OFFERING = "ObservationOfferingInterface";
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
    public static final String IOOS_RF_1_0 = "text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0\"";
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
    public static final String LAT = "lat";
    public static final String LON = "lon";
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
    private final static String OWS = "xmlns:ows";
    private final static String GML = "xmlns:gml";
    private final static String SOS = "xmlns:sos";
    private final static String SML = "xmlns:sml";
    private final static String SWE = "xmlns:swe";
    private final static String OM = "xmlns:om";
    private final static String SWE2 = "xmlns:swe2";
    private final static String XLINK = "xmlns:xlink";
    protected Document document;
    protected String OWS_NS;
    protected String GML_NS;
    protected String SOS_NS;
    protected String SML_NS;
    protected String SWE_NS;
    protected String OM_NS;
    protected String XLINK_NS;
    protected String SWE2_NS;

    public OutputFormatter() {
        nsToPrefix = new HashMap<String, String>();


    }

    public String getNamespacePrefix(String namespace) {
        return nsToPrefix.get(namespace);

    }

    protected Element createElementNS(String elemNs, String elemVal) {
        Element elem = document.createElementNS(elemNs, elemVal);
        elem.setPrefix(this.getNamespacePrefix(elemNs));
        return elem;
    }

    protected void initNamespaces() {
        if (document != null) {
            Element root = document.getDocumentElement();
            OM_NS = root.getAttribute(OM);
            OWS_NS = root.getAttribute(OWS);
            GML_NS = root.getAttribute(GML);
            SOS_NS = root.getAttribute(SOS);
            SML_NS = root.getAttribute(SML);
            SWE_NS = root.getAttribute(SWE);
            SWE2_NS = root.getAttribute(SWE2);
            XLINK_NS = root.getAttribute(XLINK);
            nsToPrefix.put(OM_NS, "om");
            nsToPrefix.put(OWS_NS, "ows");
            nsToPrefix.put(GML_NS, "gml");
            nsToPrefix.put(SOS_NS, "sos");
            nsToPrefix.put(SML_NS, "sml");
            nsToPrefix.put(SWE_NS, "swe");
            nsToPrefix.put(SWE2_NS, "swe2");
            nsToPrefix.put(XLINK_NS, "xlink");
        }
    }

    /**
     * Adds data from a formatted string to some container defined in the individual formatters.
     * @param dataFormattedString a csv string that usually follows the format of key=value,key1=value1,key2=value2,etc
     *  'value' can be csvs as well, allowing for multiple values per key
     */
    public abstract void addDataFormattedStringToInfoList(String dataFormattedString);

    /**
     * Sets up the outputter to write an exception when writeOutput is invoked.
     * @param message - message to display to the user
     */
    public abstract void setupExceptionOutput(String message);

    /**
     * Writes prepared output to the writer (usually will be a response stream from a http request
     * @param writer the stream where the output will be written to.
     */
    public abstract void writeOutput(Writer writer);
}
