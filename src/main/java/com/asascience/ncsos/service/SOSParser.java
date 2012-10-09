/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.service;

import com.asascience.ncsos.describesen.SOSDescribeSensorHandler;
import com.asascience.ncsos.getcaps.SOSGetCapabilitiesRequestHandler;
import com.asascience.ncsos.getobs.SOSGetObservationRequestHandler;
import com.asascience.ncsos.outputformatter.GetCapsOutputter;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.dataset.NetcdfDataset;
/**
 * Reads and parses a sos request coming in from thredds.
 * @author abird
 * @modified scowan
 */
public class SOSParser {
    private HashMap<String, Object> queryParameters;
    private Logger _log;
    private Map<String, String> coordsHash;
    
    private final String defService = "sos";
    private final String defVersion = "1.0.0";
    private final String TRUE_STRING = "true";

    
    
    // enum for supported request types (used primarily for string comparison)
    private enum SupportedRequests {
        GetCapabilities, GetObservation, DescribeSensor
    }
    
    /**
     * Sets the logger for error output using the SOSParser class.
     */
    public SOSParser() {
        _log = LoggerFactory.getLogger(SOSParser.class);
    }
    
    /**
     * enhance - provides direct access to parsing a sos request and create handler for the type of request coming in
     * @param dataset NetcdfDataset to enhance the NCML
     * @param query query string provided by request
     * @param threddsURI
     * @return 
     * @throws IOException  
     */
    public HashMap<String, Object> enhance(final NetcdfDataset dataset, final String query, String threddsURI) throws IOException {
        return enhance(dataset, query, threddsURI, null);
    }

    /**
     * enhance - provides direct access to parsing a sos request and create handler for the type of request coming in
     * @param dataset NetcdfDataset to enhance the NCML
     * @param query query string provided by request
     * @param threddsURI
     * @param savePath provides a directory to cache requests for quick replies (currently unsupported)
     * @return
     * @throws IOException  
     */
    public HashMap<String, Object> enhance(final NetcdfDataset dataset, final String query, String threddsURI, String savePath) throws IOException {
        // clear anything that can cause issue if we were to use the same parser for multiple requests
        queryParameters = new HashMap<String, Object>();
        coordsHash = new HashMap<String, String>();
        
        HashMap<String, Object> retval = new HashMap<String, Object>();
        
        SOSGetCapabilitiesRequestHandler capHandler = null;
        
        if (query != null) {
            // parse the query string
            ParseQuery(query);
        } else {
            // we are assuming that a request made here w/o a query string is a 'GetCapabilities' request
            // add the request to our query parameters, as well as some other default values
            queryParameters.put("request", "GetCapabilities");
            queryParameters.put("service", defService);
            queryParameters.put("version", defVersion);
        }
        
        // make sure we do not have any errors in our query
        if (queryParameters.containsKey("error")) {
            // issue in parsing, return an error
            capHandler = new SOSGetCapabilitiesRequestHandler(null);
            capHandler.getOutputHandler().setupExceptionOutput("Error with request - " + queryParameters.get("error").toString());
            retval.put("outputHandler", capHandler.getOutputHandler());
            retval.put("responseContentType", "text/xml");
            return retval;
        }
        
        // check to make sure needed params are in the query
        if (!queryParameters.containsKey("request")) {
            capHandler = new SOSGetCapabilitiesRequestHandler(null);
            capHandler.getOutputHandler().setupExceptionOutput("Missing required parameter 'request'.");
            retval.put("outputHandler", capHandler.getOutputHandler());
            retval.put("responseContentType", "text/xml");
            return retval;
        }
        
        // check version number
        if (!queryParameters.containsKey("version") ||
            !queryParameters.get("version").toString().equalsIgnoreCase("1.0.0")) {
            capHandler = new SOSGetCapabilitiesRequestHandler(null);
            capHandler.getOutputHandler().setupExceptionOutput("Version is a required parameter for all requests. Currently 1.0.0 is accepted.");
            retval.put("outputHandler", capHandler.getOutputHandler());
            retval.put("responseContentType", "text/xml");
            return retval;
        }
        
        // check service param
        if (!queryParameters.containsKey("service") ||
            !queryParameters.get("service").toString().equalsIgnoreCase("sos")) {
            capHandler = new SOSGetCapabilitiesRequestHandler(null);
            capHandler.getOutputHandler().setupExceptionOutput("Expected service parameter with a value of 'SOS'.");
            retval.put("outputHandler", capHandler.getOutputHandler());
            retval.put("responseContentType", "text/xml");
            return retval;
        }
        
        try {
            switch (SupportedRequests.valueOf(queryParameters.get("request").toString())) {
                case GetCapabilities:
                    // indicate that our response will be in xml
                    retval.put("responseContentType", "text/xml");
                    // check to see if cache is enabled
                    if (queryParameters.containsKey("cache") && queryParameters.get("cache").toString().equals(TRUE_STRING) && savePath != null) {
                        //Check to see if get caps exists, if it does not actual parse the file
                        _log.info("Cache enabled for GetCapabilities");
                        File f = new File(savePath + getCacheXmlFileName(threddsURI));
//                        long start = System.currentTimeMillis();
                        if (f.exists()) {
                            //if the file exists check the modified data against current data
                            long fileDateTime = f.lastModified();
                            Date fileDate = new Date(fileDateTime);
                            DateTime currentDateTime = new DateTime();
                            DateTime SevenDaysEarlier = currentDateTime.minusDays(7);
                            Date minusSevenFromNow = SevenDaysEarlier.toDate();
                            //if the file is older than seven days reprocess the data
                            if (fileDate.before(minusSevenFromNow)) {
                                createGetCapsCacheFile(dataset, threddsURI, savePath);
                            } else {
                                fileIsInDate(f);
                            }
                        } else {
                            try {
                                //create the file as it does not exist
                                //Check Directory 
                                capHandler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
                                parseGetCaps(capHandler);
                            } catch (IOException ex) {
                                _log.error(ex.getMessage());
                                capHandler = null;
                            }
                        }
//                        long elapsedTimeMillis = System.currentTimeMillis() - start;
//                        float elapsedTimeSec = elapsedTimeMillis / 1000F;
//                        _log.info("Time to complete cached request - MILI:" + elapsedTimeMillis + ":SEC: " + elapsedTimeSec);
                    } else {
                        try {
                            capHandler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
                            parseGetCaps(capHandler);
                        } catch (IOException ex) {
                            _log.error(ex.getMessage());
                            capHandler = null;
                        }
                    }
                    if (capHandler != null)
                        retval.put("outputHandler", capHandler.getOutputHandler());
                    else {
                        capHandler = new SOSGetCapabilitiesRequestHandler(null);
                        capHandler.getOutputHandler().setupExceptionOutput("Internal Error in preparing output for GetCapabilities request, received null handler.");
                        retval.put("outputHandler", capHandler.getOutputHandler());
                    }
                    break;
                case GetObservation:
                    // assert that we have our needed parameters
                    if (!queryParameters.containsKey("offering") ||
                            !queryParameters.containsKey("observedproperty") ||
                            !queryParameters.containsKey("responseformat")) {
                        // print out xml error using get caps error?
                        try {
                            capHandler = new SOSGetCapabilitiesRequestHandler(null);
                            capHandler.getOutputHandler().setupExceptionOutput("Observation requests must have offering, observedProperty, responseFormat as query parameters");
                            retval.put("outputHandler", capHandler.getOutputHandler());
                            retval.put("responseContentType", "text/xml");
                        } catch (Exception e) { }
                        break;
                    }
                    // setup our coordsHash
                    if (queryParameters.containsKey("lat") && queryParameters.containsKey("lon")) {
                        coordsHash.put("lat", queryParameters.get("lat").toString());
                        coordsHash.put("lon", queryParameters.get("lon").toString());
                    }
                    if (queryParameters.containsKey("depth")) {
                        coordsHash.put("depth", queryParameters.get("depth").toString());
                    }
                    try {
                        // create a new handler for our get observation request and then write its result to output
                        SOSGetObservationRequestHandler obsHandler = new SOSGetObservationRequestHandler(dataset,
                                (String[])queryParameters.get("offering"),
                                (String[])queryParameters.get("observedproperty"),
                                (String[])queryParameters.get("eventtime"),
                                queryParameters.get("responseformat").toString(),
                                coordsHash);
                        // below indicates that we got an exception and we should return it
                        if (obsHandler.getOutputHandler().getClass() == GetCapsOutputter.class) {
                            retval.put("responseContentType", "text/xml");
                            retval.put("outputHandler", obsHandler.getOutputHandler());
                            break;
                        }
                        // set our content type for the response
                        retval.put("responseContentType", obsHandler.getContentType());
                        if (obsHandler.getFeatureDataset() == null) {
                            obsHandler.setException("Uknown or invalid feature type");
                        } else if (obsHandler.getDatasetFeatureType() == ucar.nc2.constants.FeatureType.GRID && obsHandler.getCDMDataset() == null) {
                            obsHandler.setException("Latitude and Longitude must be provided for a GetObservation request involving GRID datasets");
                        } else {
                            obsHandler.parseObservations();
                        }
                        // add our handler to the return value
                        retval.put("outputHandler", obsHandler.getOutputHandler());
                    } catch (IOException ex) {
                        _log.error(ex.getMessage());
                        capHandler = new SOSGetCapabilitiesRequestHandler(null);
                        capHandler.getOutputHandler().setupExceptionOutput("Internal Error in creating output for GetObservation request - " + ex.toString());
                        retval.put("outputHandler", capHandler.getOutputHandler());
                    }
                    break;
                case DescribeSensor:
                    try {
                        // resposne will always be text/xml
                        retval.put("responseContentType", "text/xml");
                        SOSDescribeSensorHandler sensorHandler;
                        if (!queryParameters.containsKey("responseformat")) {
                            sensorHandler = new SOSDescribeSensorHandler(dataset);
                            sensorHandler.getOutputHandler().setupExceptionOutput("responseFormat must be supplied for " + queryParameters.get("request").toString() + " requests to the " + queryParameters.get("service").toString() + " version " + queryParameters.get("version").toString() + " service");
                        } else if (!queryParameters.containsKey("procedure")) {
                            sensorHandler = new SOSDescribeSensorHandler(dataset);
                            sensorHandler.getOutputHandler().setupExceptionOutput("procedure must be supplied for " + queryParameters.get("request").toString() + " requests to the " + queryParameters.get("service").toString() + " version " + queryParameters.get("version").toString() + " service");
                        }else {
                            // create a describe sensor handler
                            sensorHandler = new SOSDescribeSensorHandler(dataset,
                                    queryParameters.get("responseformat").toString(),
                                    queryParameters.get("procedure").toString(),
                                    threddsURI,
                                    query);
                        }
                        retval.put("outputHandler",sensorHandler.getOutputHandler());
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                        for (int e = 0; e < 10; e++) {
                            System.out.println("\t" + ex.getStackTrace()[e]);
                        }
                        capHandler = new SOSGetCapabilitiesRequestHandler(null);
                        capHandler.getOutputHandler().setupExceptionOutput("Internal System Exception in setting up DescribeSensor handler - " + ex.toString());
                        retval.put("outputHandler", capHandler.getOutputHandler());
                        _log.error("Internal System Exception in setting up DescribeSensor handler - " + ex.toString());
                    }
                    break;
                default:
                    // return a 'not supported' error
                    capHandler = new SOSGetCapabilitiesRequestHandler(null);
                    capHandler.getOutputHandler().setupExceptionOutput(queryParameters.get("request").toString() + " is not a supported request");
                    retval.put("outputHandler", capHandler.getOutputHandler());
                    System.out.println(queryParameters.get("request").toString() + " is not a supported request");
                    _log.error("Invalid request parameter: " + queryParameters.get("request").toString() + " is not a supported request");
                    break;
            }
        } catch (IllegalArgumentException ex) {
            // create a get caps respons with exception
            _log.error("Exception with request: " + ex.getMessage());
            System.out.println("Exception encountered " + ex.getMessage());
            try {
                capHandler = new SOSGetCapabilitiesRequestHandler(null);
                capHandler.getOutputHandler().setupExceptionOutput("Unrecognized request " + queryParameters.get("request").toString());
                retval.put("outputHandler", capHandler.getOutputHandler());
            } catch (Exception e) { }
        }
        
        return retval;
    }
    
    private void parseGetCaps(SOSGetCapabilitiesRequestHandler capHandler) throws IOException {
        // do our parsing
        if (((GetCapsOutputter)capHandler.getOutputHandler()).hasExceptionOut())
            return;
        capHandler.parseGetCapabilitiesDocument();
    }
    
    private void ParseQuery(String query) {
        String[] queryArguments = query.split("&");
        for(String arg : queryArguments) {
            String[] keyVal = arg.split("=");
            if (keyVal.length != 2) {
                queryParameters.put("error", "invalid argument " + arg);
            } else {
                if (keyVal[0].equalsIgnoreCase("offering")) {
                    String[] howManyStation = keyVal[1].replace("%3A", ":").split(",");
                    List<String> stList = new ArrayList<String>();

                    for (int j = 0; j < howManyStation.length; j++) {
                        //split on :
                        String[] splitStr = howManyStation[j].split(":");
                        String stationName = splitStr[splitStr.length - 1];
                        stList.add(stationName);
                    }
                    
                    queryParameters.put(keyVal[0].toLowerCase(), (String[]) stList.toArray(new String[stList.size()]) );
                } else if (keyVal[0].equalsIgnoreCase("responseformat")) {
                    try {
                        String val = URLDecoder.decode(keyVal[1], "UTF-8");
                        queryParameters.put(keyVal[0].toLowerCase(),val);
                    } catch (Exception e) {
                        System.out.println("Exception in decoding: " + keyVal[1] + " - " + e.getMessage());
                        _log.error("Exception in decoding: " + keyVal[1] + " - " + e.getMessage());
                        queryParameters.put(keyVal[0],keyVal[1]);
                    }
                } else if (keyVal[0].equalsIgnoreCase("eventtime")) {
                    String[] eventtime;
                    if (keyVal[1].contains("/")) {
                        eventtime = keyVal[1].split("/");
                    } else {
                        eventtime = new String[] { keyVal[1] };
                    }
                    queryParameters.put(keyVal[0].toLowerCase(),eventtime);
                } else if (keyVal[0].equalsIgnoreCase("observedproperty")) {
                    String[] param;
                    if (keyVal[1].contains(",")) {
                        param = keyVal[1].split(",");
                    } else {
                        param = new String[] { keyVal[1] };
                    }
                    queryParameters.put(keyVal[0].toLowerCase(),param);
                } else {
                    queryParameters.put(keyVal[0].toLowerCase(), keyVal[1]);
                }
            }
        }
    }
        
    private String getCacheXmlFileName(String threddsURI) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createGetCapsCacheFile(NetcdfDataset dataset, String threddsURI, String savePath) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void fileIsInDate(File f) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void normalSOSRequestNoCacheParam(NetcdfDataset dataset, String threddsURI) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
