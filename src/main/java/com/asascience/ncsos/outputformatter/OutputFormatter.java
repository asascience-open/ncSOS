package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

/**
 * Provides common functions for classes that define the response outputs for various
 * SOS requests. Most importantly, provides a common end point for writing the response
 * to a writer provided by the controller.
 */
public abstract class OutputFormatter {

    private HashMap<String, Namespace> namespaces;
    public static final String NCSOS_VERSION = "RC7";
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
    protected Document  document;

    public OutputFormatter() {
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

    /**
     * Returns the template path to parse
     */
    protected abstract String getTemplateLocation();

    /**
     * Adds data from a formatted string to some container defined in the individual formatters.
     *
     * @param dataFormattedString a csv string that usually follows the format of key=value,key1=value1,key2=value2,etc
     *                            'value' can be csvs as well, allowing for multiple values per key
     */
    public abstract void addDataFormattedStringToInfoList(String dataFormattedString);

    /**
     * Writes prepared output to the writer (usually will be a response stream from a http request
     *
     * @param writer the stream where the output will be written to.
     */
    public abstract void writeOutput(Writer writer) throws IOException;
    
    /**
     * The Content-type of this response
     */
    public abstract String getContentType();
}
