package com.asascience.ncsos.service;

//package thredds.server.sos.service;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.Writer;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.*;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//import org.joda.time.DateTime;
//import org.w3c.dom.Document;
//import org.xml.sax.SAXException;
//import thredds.server.sos.getCaps.SOSGetCapabilitiesRequestHandler;
//import thredds.server.sos.getObs.SOSGetObservationRequestHandler;
//import thredds.server.sos.util.XMLDomUtils;
//import ucar.nc2.dataset.NetcdfDataset;
//
///**
// * SOSParserOld based on EnhancedMetadataService
// * @author: Andrew Bird
// * Date: 2011
// */
//public class SOSParserOld {
//
////    private static final Logger _log = Logger.getLogger(SOSParserOld.class);
//    private String service;
//    private String version;
//    private String request;
//    private String observedProperty;
//    private String responseType;
//    private String[] offering;
//    private String singleEventTime;
//    //used for the cases where multiple props are selected
//    private String[] observedProperties;
//    //used for the cases where muliple event times are selected
//    private String[] eventTime;
//    //used for cacheing getcaps and get obs results
//    private String cache;
//    private String CACHE_TRUE = "true";
//    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSParserOld.class);
//    private Map<String, String> latLonRequest = new HashMap();
//
//    /**
//     * Enhance NCML with Data Discovery conventions elements if not already in place in the metadata.
//     *
//     * @param dataset NetcdfDataset to enhance the NCML
//     * @param writer writer to send enhanced NCML to
//     */
//    public void enhance(final NetcdfDataset dataset, final Writer writer, final String query, String threddsURI) {
//        enhance(dataset, writer, query, threddsURI, null);
//    }
//
//    public void enhance(final NetcdfDataset dataset, final Writer writer, final String query, String threddsURI, String savePath) {
//        eventTime = null;
//        
//
//        try {
//            if (query != null) {
//                //if query is not empty
//                //set the query params then call on the fly
//                splitQuery(query);
//
//                //if all the fields are valid ie not null
//                if ((service != null) && (request != null) && (version != null)) {
//                    //get caps
//                    if (request.equalsIgnoreCase("GetCapabilities")) {
//                        //check to see if use cache was set to true if so load the data file DONT PARSE IT
//                        if (cache != null && cache.equalsIgnoreCase(CACHE_TRUE) && savePath != null) {
//                            //Check to see if get caps exists, if it does not actual parse the file
//                            _log.info("cache option selected");
//                            File f = new File(savePath + getCacheXmlFileName(threddsURI));
//                            long start = System.currentTimeMillis();
//                            if (f.exists()) {
//                                //if the file exists check the modified data against current data
//                                long fileDateTime = f.lastModified();
//                                Date fileDate = new Date(fileDateTime);
//                                DateTime currentDateTime = new DateTime();
//                                DateTime SevenDaysEarlier = currentDateTime.minusDays(7);
//                                Date minusSevenFromNow = SevenDaysEarlier.toDate();
//                                //if the file is older than seven days reprocess the data
//                                if (fileDate.before(minusSevenFromNow)) {
//                                    createGetCapsCacheFile(dataset, threddsURI, writer, savePath);
//                                } else {
//
//                                    fileIsInDate(f, writer);
//                                }
//                            } else {
//                                //create the file as it does not exist
//                                //Check Directory                                   
//                                createGetCapsCacheFile(dataset, threddsURI, writer, savePath);
//                            }
//                            long elapsedTimeMillis = System.currentTimeMillis() - start;
//                            float elapsedTimeSec = elapsedTimeMillis / 1000F;
//                            _log.info("Time to complete  - MILI:" + elapsedTimeMillis + ":SEC: " + elapsedTimeSec);
//                        } else {
//                            normalSOSRequestNoCacheParam(dataset, threddsURI, writer);
//                        }
//
//                    } else if (request.equalsIgnoreCase("DescribeSensor")) {
//                        writeErrorXMLCodeWithError(writer, "DescribeSensor is not currently supported");
//                    } else if (request.equalsIgnoreCase("GetObservation")) {
//                        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset,
//                                offering,
//                                observedProperties,
//                                eventTime,
//                                responseType,
//                                latLonRequest);
//                        if (handler.getFeatureDataset() == null) {
//                            // unknown/bad feature type
//                            handler.setException("Uknown or invalid feature type");
//                        } else if (handler.getDatasetFeatureType() == ucar.nc2.constants.FeatureType.GRID && handler.getCDMDataset() == null) {
//                            handler.setException("Lat and lon must be supplied on a GetObservation request for GRID data");
//                        } else {
//                            handler.parseObservations();
//                        }
//                        writeDocumentToResponse(handler.getDocument(), writer);
//                        handler.finished();
//                    } else {
//                        writeErrorXMLCodeWithError(writer, "Unknown request " + request);
//                    }
//
//                } //else if the above is not true print invalid xml text
//                else {
//                    writeErrorXMLCodeWithError(writer, "Invalid request; requires: version, service and request in query");
//                }
//            } else if (query == null) {
//                //if the entry is null just print out the get caps xml
//                _log.info("Null query string/params: using get caps");
//                SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
//                handler.parseTemplateXML();
//                handler.parseServiceIdentification();
//                handler.parseServiceDescription();
//                handler.parseOperationsMetaData();
//                handler.parseObservationList();
//                writeDocumentToResponse(handler.getDocument(), writer);
//                handler.finished();
//            }
//
//            //catch
//        } catch (NullPointerException e) {
////            _log.error(e);
//            Logger.getLogger(SOSParserOld.class.getName()).log(Level.SEVERE, "Null Pointer in Data?", e);
//        } catch (Exception e) {
////            _log.error(e);
//            Logger.getLogger(SOSParserOld.class.getName()).log(Level.SEVERE, "Null", e);
//        }
//    }
//
//    
//    
//    /**
//     * get the XML file name base on the request string add the / for correct directory
//     */
//    public String getCacheXmlFileName(String threddsURI) {
//
//        String[] splitStr = threddsURI.split("/");
//        String dName = splitStr[splitStr.length - 1];
//        splitStr = null;
//        splitStr = dName.split("nc");
//
//        return "/" + splitStr[0] + "xml";
//    }
//
//    /**
//     * Normal sos request without any file writing
//     * @param dataset
//     * @param threddsURI
//     * @param writer
//     * @throws IOException
//     * @throws TransformerException 
//     */
//    private void normalSOSRequestNoCacheParam(final NetcdfDataset dataset, String threddsURI, final Writer writer) throws IOException, TransformerException {
//        _log.info("Normal request no caching");
//        SOSGetCapabilitiesRequestHandler handler = performSOSGetCaps(dataset, threddsURI, writer);
//        handler.finished();
//        handler = null;
//        writer.flush();
//        writer.close();
//    }
//
//    /**
//     * if the file is older than seven days create it,
//     * or
//     * if the cached file does not exist
//     * @param dataset
//     * @param threddsURI
//     * @param writer
//     * @param savePath
//     * @throws IOException
//     * @throws TransformerException 
//     */
//    private void createGetCapsCacheFile(final NetcdfDataset dataset, String threddsURI, final Writer writer, String savePath) throws IOException, TransformerException {
//        _log.info("CACHING: Cached file does not exist or override...");
//        //parse the file anyway
//        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
//        handler.parseServiceIdentification();
//        handler.parseServiceDescription();
//        handler.parseOperationsMetaData();
//        handler.parseObservationList();
//        writeDocumentToResponse(handler.getDocument(), writer);
//        fileWriter(savePath, getCacheXmlFileName(threddsURI), handler.getDocument());
//        handler.finished();
//        handler = null;
//        //GetCaps file writing if not there
//        writer.flush();
//        writer.close();
//    }
//
//    /**
//     * checks to see if the file in questions is in date
//     * @param f
//     * @param writer
//     * @throws TransformerException
//     * @throws SAXException
//     * @throws IOException
//     * @throws ParserConfigurationException 
//     */
//    private void fileIsInDate(File f, final Writer writer) throws TransformerException, SAXException, IOException, ParserConfigurationException {
//        _log.info("CACHING: using cached XML file");
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document doc = builder.parse(f);
//        writeDocumentToResponse(doc, writer);
//        writer.flush();
//        writer.close();
//    }
//
//    /**
//     * performs SOS get caps request
//     * @param dataset
//     * @param threddsURI
//     * @param writer
//     * @return
//     * @throws IOException
//     * @throws TransformerException 
//     */
//    private SOSGetCapabilitiesRequestHandler performSOSGetCaps(final NetcdfDataset dataset, String threddsURI, final Writer writer) throws IOException, TransformerException {
//        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
//        handler.parseServiceIdentification();
//        handler.parseServiceDescription();
//        handler.parseOperationsMetaData();
//        handler.parseObservationList();
//        writeDocumentToResponse(handler.getDocument(), writer);
//        return handler;
//    }
//
//    /**
//     * performs the file writing of the dom to XML file
//     * @param base
//     * @param fileName
//     * @param dom 
//     */
//    private void fileWriter(String base, String fileName, Document dom) throws IOException {
//        try {
//            File file = new File(base + fileName);
//            file.createNewFile();
//            DOMSource source = new DOMSource(dom);
//            Result result = new StreamResult(file);
//            // for some reason, the line below is needed to make tests successful, but I imagine that it will not work in production -- Sean
////            result.setSystemId("file:///" + file.toString());
//            Transformer transformer = TransformerFactory.newInstance().newTransformer();
//            transformer.transform(source, result);
//            _log.info("Your file has been written");
//        } catch (TransformerConfigurationException e) {
//            _log.info("CACHING:issue with XML file writing");
//        } catch (TransformerException e) {
//            _log.info("CACHING:issue with XML file writing");
//        }
//    }
//
//    private void writeErrorXMLCodeWithError(final Writer writer, String errorMsg) throws IOException, TransformerException {
//        Document doc = XMLDomUtils.getExceptionDom(errorMsg);
//        writeDocumentToResponse(doc, writer);
//    }
//
//    /**
//     * used for testing cache value does not work in instant
//     * @return 
//     */
//    public String getCacheValue() {
//        return cache;
//    }
//
//    public void writeDocumentToResponse(Document dom, final Writer writer) throws TransformerException, IOException {
//        DOMSource source = new DOMSource(dom);
//        Result result = new StreamResult(writer);
//        Transformer transformer = TransformerFactory.newInstance().newTransformer();
//        transformer.transform(source, result);
//    }
//
//    public void splitQuery(String query) {
//        String[] splitQuery = query.split("&");
//        service = null;
//        version = null;
//        request = null;
//        cache = null;
//        observedProperty = null;
//        responseType = "oostethysswe";
//
//        if (splitQuery.length > 2) {
//            for (int i = 0; i < splitQuery.length; i++) {
//                String parsedString = splitQuery[i];
//                String[] splitServiceStr = parsedString.split("=");
//                if (splitServiceStr[0].equalsIgnoreCase("service")) {
//                    service = splitServiceStr[1];
//                } else if (splitServiceStr[0].equalsIgnoreCase("version")) {
//                    version = splitServiceStr[1];
//                } else if (splitServiceStr[0].equalsIgnoreCase("useCache")) {
//                    cache = splitServiceStr[1];
//                } else if (splitServiceStr[0].equalsIgnoreCase("request")) {
//                    request = splitServiceStr[1];
//                } else if (splitServiceStr[0].equalsIgnoreCase("response")) {
//                    responseType = splitServiceStr[1];
//                } else if (splitServiceStr[0].equalsIgnoreCase("observedProperty")) {
//                    observedProperty = splitServiceStr[1];
//                    if (observedProperty.contains(",")) {
//                        observedProperties = observedProperty.split(",");
//                    } else {
//                        observedProperties = new String[]{observedProperty};
//                    }
//                } else if (splitServiceStr[0].equalsIgnoreCase("offering")) {
//                    //replace all the eccaped : with real ones
//                    String temp = splitServiceStr[1];
//                    String replaceOffer = temp.replaceAll("%3A", ":");
//
//                    //split on ,
//                    String[] howManyStation = replaceOffer.split(",");
//
//                    List<String> stList = new ArrayList<String>();
//
//                    for (int j = 0; j < howManyStation.length; j++) {
//                        //split on :
//                        String[] splitStr = howManyStation[j].split(":");
//                        String stationName = splitStr[splitStr.length - 1];
//                        stList.add(stationName);
//                    }
//
//                    //String[] toArray = (String[] )stList.toArray();
//                    //MetadataParser.offering = toArray;
//
////                    Object[] objectArray = stList.toArray();
//                    String[] array = (String[]) stList.toArray(new String[stList.size()]);
//
//                    offering = array;
//
//                } else if (splitServiceStr[0].equalsIgnoreCase("eventtime")) {
//
//                    singleEventTime = splitServiceStr[1];
//                    if (singleEventTime.contains("/")) {
//                        eventTime = singleEventTime.split("/");
//                    } else {
//                        eventTime = new String[]{singleEventTime};
//                    }
//
//                } else if (splitServiceStr[0].equalsIgnoreCase("lat")) {
//                    latLonRequest.put("lat", splitServiceStr[1]);
//                    
//                } else if (splitServiceStr[0].equalsIgnoreCase("lon")) {
//                    latLonRequest.put("lon", splitServiceStr[1]);
//                } else if (splitServiceStr[0].equalsIgnoreCase("depth")) {
//                    latLonRequest.put("depth", splitServiceStr[1]);
//                }
//
//            }
//        }
//    }
//}
