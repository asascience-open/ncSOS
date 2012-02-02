package thredds.server.sos.service;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import thredds.server.sos.util.XMLDomUtils;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * MetadataParser based on EnhancedMetadataService
 * @author: Andrew Bird
 * Date: 2011
 */
public class MetadataParser {

//    private static final Logger _log = Logger.getLogger(MetadataParser.class);
    private static String service;
    private static String version;
    private static String request;
    private static String observedProperty;
    private static String[] offering;
    private static String singleEventTime;
    //used for the cases where multiple props are selected
    private static String[] observedProperties;
    //used for the cases where muliple event times are selected
    private static String[] eventTime;

    /**
     * Enhance NCML with Data Discovery conventions elements if not already in place in the metadata.
     *
     * @param dataset NetcdfDataset to enhance the NCML
     * @param writer writer to send enhanced NCML to
     */
    public static void enhance(final NetcdfDataset dataset, final Writer writer, final String query, String threddsURI) {

        eventTime = null;

        try {
            if (query != null) {
                //if query is not empty
                //set the query params then call on the fly
            splitQuery(query);

                System.out.println(query);
                
                //if all the fields are valid ie not null
                if ((service != null) && (request != null) && (version != null)) {
                    //get caps
                    if (request.equalsIgnoreCase("GetCapabilities")) {
                        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset,threddsURI);
                        handler.parseServiceIdentification();
                        handler.parseServiceDescription();
                        handler.parseOperationsMetaData();
                        handler.parseObservationList();
                        writeDocument(handler.getDocument(), writer);
                        handler.finished();
                    } else if (request.equalsIgnoreCase("DescribeSensor")) {
                        writeErrorXMLCode(writer);
                    } else if (request.equalsIgnoreCase("GetObservation")) {
                        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset,offering,observedProperties,eventTime);
                        handler.parseObservations();                        
                        writeDocument(handler.getDocument(), writer);
                        handler.finished();
                    } else {
                        writeErrorXMLCode(writer);
                    }

                } //else if the above is not true print invalid xml text
                else {
                    writeErrorXMLCode(writer);
                }
            } else if (query == null) {
                //if the entry is null just print out the get caps xml
//                _log.info("Null query string/params: using get caps");
                SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset,threddsURI);
                handler.parseTemplateXML();
                handler.parseServiceIdentification();
                handler.parseServiceDescription();
                handler.parseOperationsMetaData();
                handler.parseObservationList();
                writeDocument(handler.getDocument(), writer);
                handler.finished();
            }

            //catch
        } catch (NullPointerException e) {
//            _log.error(e);
            Logger.getLogger(MetadataParser.class.getName()).log(Level.SEVERE, "Null Pointer in Data?", e);
        } catch (Exception e) {
//            _log.error(e);
            Logger.getLogger(MetadataParser.class.getName()).log(Level.SEVERE, "Null", e);
        }
    }

    private static void writeErrorXMLCode(final Writer writer) throws IOException, TransformerException {
        Document doc = XMLDomUtils.getExceptionDom();
        writeDocument(doc, writer);
    }

    public static void writeDocument(Document dom, final Writer writer) throws TransformerException, IOException {
        DOMSource source = new DOMSource(dom);
        Result result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, result);
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
                        MetadataParser.observedProperties = observedProperty.split(",");
                    } else {
                        MetadataParser.observedProperties = new String[]{MetadataParser.observedProperty};
                    }
                } else if (splitServiceStr[0].equalsIgnoreCase("offering")) {
                    //replace all the eccaped : with real ones
                    String temp = splitServiceStr[1];
                    String replaceOffer = temp.replaceAll("%3A", ":");
                    
                    //split on ,
                    String[] howManyStation = replaceOffer.split(","); 
                    
                    List<String> stList = new ArrayList<String>();
                    
                    for (int j = 0; j < howManyStation.length; j++) {
                        //split on :
                        String[] splitStr = howManyStation[j].split(":");
                        String stationName = splitStr[splitStr.length-1];
                        stList.add(stationName);
                    }
                    
                    //String[] toArray = (String[] )stList.toArray();
                    //MetadataParser.offering = toArray;
                    
                    Object[] objectArray = stList.toArray();
                    String[] array = (String[])stList.toArray(new String[stList.size()]);
                    
                    MetadataParser.offering = array;
                    
                } else if (splitServiceStr[0].equalsIgnoreCase("eventtime")) {

                    MetadataParser.singleEventTime = splitServiceStr[1];
                    if (singleEventTime.contains("/")) {
                        MetadataParser.eventTime = singleEventTime.split("/");
                    } else {
                        MetadataParser.eventTime = new String[]{MetadataParser.singleEventTime};
                    }

                }
            }
        }
    }
}
