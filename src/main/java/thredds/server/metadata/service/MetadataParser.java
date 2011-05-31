/*
 *
 */
package thredds.server.metadata.service;

import java.io.IOException;
import java.io.StringWriter;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import thredds.server.metadata.bean.Extent;
import thredds.server.metadata.util.ThreddsExtentUtil;
import ucar.nc2.dataset.NetcdfDataset;

import java.io.Writer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import thredds.server.metadata.getObs.MockGetObservationParser;
import thredds.server.metadata.getCaps.MockGetCapabilitiesParser;
import thredds.server.metadata.util.XMLDomUtils;

/**
 * MetadataParser based on EnhancedMetadataService
 * @author: Andrew Bird
 * Date: 2011
 */
public class MetadataParser {

    private static final Logger _log = Logger.getLogger(MetadataParser.class);
    private static String xmlString;
    private static MockGetCapabilitiesParser MockGetCapP;
    private static String service;
    private static String version;
    private static String request;
    private static String observedProperty;
    private static MockGetObservationParser MockGetObsP;
    private static String offering;
    private static String singleEventTime;
    //true if multiple props are used
    private static boolean isMultiObsProperties;
    //used for the cases where multiple props are selected
    private static String[] observedProperties;
    //true is multiple times are used
    private static boolean isMultiTime;
    //used for the cases where muliple event times are selected
    private static String[] EventTime;

    /**
     * Enhance NCML with Data Discovery conventions elements if not already in place in the metadata.
     *
     * @param dataset NetcdfDataset to enhance the NCML
     * @param writer writer to send enhanced NCML to
     */
    public static void enhance(final NetcdfDataset dataset, final Writer writer, final String query) {

        MockGetCapP = null;
        xmlString = null;
        EventTime =null;
        isMultiTime = false;
        isMultiObsProperties = false;
        //get the extent of the Netcdf file Lat/lon/time
        Extent ext = null;
        try {
            //get basic information
            ext = ThreddsExtentUtil.getExtent(dataset);

            DatasetMetaData dst = new DatasetMetaData(ext, dataset);
            dst.extractData();

            if (query != null) {
                //if query is not empty
                //set the query params then call on the fly
                splitQuery(query);


                //if all the fields are valid ie not null
                if ((service != null) && (request != null) && (version != null)) {
                    //get caps
                    if (request.equalsIgnoreCase("GetCapabilities")) {
                        createGetCapsResults(dst, writer);
                    } else if (request.equalsIgnoreCase("DescribeSensor")) {
                    } else if (request.equalsIgnoreCase("GetObservation")) {
                        if (EventTime !=null){
                            dst.setSearchTimes(EventTime,isMultiTime);

                        }
                        dst.setRequestedStationName(MetadataParser.offering);
                        dst.setDatasetArrayValues(observedProperties);
                        createGetObsResults(dst, writer);
                        
                    } else {
                        writeErrorXMLCode(writer);
                    }

                } //else if the above is not true print invalid xml text
                else {
                    writeErrorXMLCode(writer);
                }
            } else if (query == null) {
                //if the entry is null just print out the get caps xml
                _log.info("Null query string/params: using get caps");
                createGetCapsResults(dst, writer);
            }

            //finally close the dataset
            dst.closeDataSet();

            //catch
        } catch (Exception e) {
            _log.error(e);
        }
    }

    private static void writeErrorXMLCode(final Writer writer) throws IOException, TransformerException {
        Document doc = XMLDomUtils.getExceptionDom();
        createStringFromDom(doc, writer);
    }

    private static void createGetCapsResults(DatasetMetaData dst, final Writer writer) throws IOException, TransformerException {
        MockGetCapP = new MockGetCapabilitiesParser(dst);
        MockGetCapP.parseTemplateXML();
        MockGetCapP.parseServiceIdentification();
        MockGetCapP.parseServiceDescription();
        MockGetCapP.parseOperationsMetaData();
        MockGetCapP.parseObservationList();
        //create string output
        createStringFromDom(MockGetCapP.getDom(), writer);
    }

    public static void createStringFromDom(Document dom, final Writer writer) throws TransformerException, IOException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(dom);
        transformer.transform(source, result);
        xmlString = result.getWriter().toString();
        writer.append(xmlString);
    }

    public static String getxmlString() {
        return xmlString;
    }

    public static MockGetCapabilitiesParser getGetCapsParser() {
        return MockGetCapP;
    }

    public static void splitQuery(String query) {
        String[] splitQuery = query.split("&");
        service = null;
        version = null;
        request = null;
        observedProperty = null;

        if (splitQuery.length > 2) {
            for (int i = 0; i < splitQuery.length; i++) {
                String parsedString = splitQuery[i];
                String[] splitServiceStr = parsedString.split("=");
                if (splitServiceStr[0].equalsIgnoreCase("service")) {
                    MetadataParser.service = splitServiceStr[1];
                } else if (splitServiceStr[0].equalsIgnoreCase("version")) {
                    MetadataParser.version = splitServiceStr[1];
                } else if (splitServiceStr[0].equalsIgnoreCase("request")) {
                    MetadataParser.request = splitServiceStr[1];
                } else if (splitServiceStr[0].equalsIgnoreCase("observedProperty")) {
                    MetadataParser.observedProperty = splitServiceStr[1];
                    if (observedProperty.contains(",")) {
                        isMultiObsProperties = true;
                        MetadataParser.observedProperties = observedProperty.split(",");
                    } else {
                        MetadataParser.observedProperties = new String[]{MetadataParser.observedProperty};
                    }
                } else if (splitServiceStr[0].equalsIgnoreCase("offering")) {
                    //replace all the eccaped : with real ones
                    String temp = splitServiceStr[1];
                    MetadataParser.offering = temp.replaceAll("%3A", ":");

                } else if (splitServiceStr[0].equalsIgnoreCase("eventtime")) {

                    MetadataParser.singleEventTime = splitServiceStr[1];
                    if (singleEventTime.contains("/")) {
                        isMultiTime = true;
                        MetadataParser.EventTime = singleEventTime.split("/");
                    } else {
                        isMultiTime = false;
                        MetadataParser.EventTime = new String[]{MetadataParser.singleEventTime};
                    }

                }
            }
        }
    }

    public static String getService() {
        return service;
    }

    public static String getVersion() {
        return version;
    }

    public static String getRequest() {
        return request;
    }

    @Deprecated
    public static String getObservedProperty() {
        return observedProperty;
    }

    public static String[] getEventTime() {
        return EventTime;
    }

    public static String getOffering() {
        return offering;
    }

    public static String[] getObservedProperties() {
        return observedProperties;
    }

    @Deprecated
    public static boolean isMultiEventTime() {
        return isMultiTime;
    }

    private static void createGetObsResults(DatasetMetaData dst, Writer writer) throws IOException, TransformerException {
        MockGetObsP = new MockGetObservationParser(dst);
        //check the obs property
        boolean check = MockGetObsP.parseObsListForRequestedProperty(observedProperties);
        if (check == true) {
            MockGetObsP.parseTemplateXML();
            MockGetObsP.parseObservations(observedProperties);
            //create string output
            createStringFromDom(MockGetObsP.getDom(), writer);
        } else {
            writeErrorXMLCode(writer);
        }
    }
}
