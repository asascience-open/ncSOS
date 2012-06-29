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
<<<<<<< HEAD
=======
import com.asascience.ncsos.getcaps.SOSGetCapabilitiesRequestHandler;
import com.asascience.ncsos.getobs.SOSGetObservationRequestHandler;
import com.asascience.ncsos.outputformatter.GetCapsOutputter;
import java.net.URLDecoder;
import java.net.URLEncoder;
>>>>>>> fixed tests to adhere to new ouptut format name
import ucar.nc2.dataset.NetcdfDataset;
/**
 * Reads and parses a request coming in from thredds
 * @author scowan
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
    
    public SOSParser() {
        _log = LoggerFactory.getLogger(SOSParser.class);
    }
    
    /**
     * enhance - provides direct access to parsing a sos request and create handler for the type of request coming in
     * @param dataset NetcdfDataset to enhance the NCML
     * @param query query string provided by request
     * @param threddsURI 
     */
    public HashMap<String, Object> enhance(final NetcdfDataset dataset, final String query, String threddsURI) throws IOException {
        return enhance(dataset, query, threddsURI, null);
    }

    /**
     * enhance - provides direct access to parsing a sos request and create handler for the type of request coming in
     * @param dataset NetcdfDataset to enhance the NCML
     * @param query query string provided by request
     * @param threddsURI
     * @param savePath 
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
            return retval;
        }
        // now attempt to create our request
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
                        long start = System.currentTimeMillis();
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
                                System.out.println("created GetCaps handler");
                            } catch (IOException ex) {
                                _log.error(ex.getMessage());
                            }
                        }
                        long elapsedTimeMillis = System.currentTimeMillis() - start;
                        float elapsedTimeSec = elapsedTimeMillis / 1000F;
                        _log.info("Time to complete cached request - MILI:" + elapsedTimeMillis + ":SEC: " + elapsedTimeSec);
                    } else {
                        try {
                            capHandler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
                            parseGetCaps(capHandler);
                            System.out.println("created GetCaps handler");
                        } catch (IOException ex) {
                            _log.error(ex.getMessage());
                        }
                    }
                    System.out.println("capHandler output- " + capHandler.getOutputHandler().toString());
                    if (capHandler != null)
                        retval.put("outputHandler", capHandler.getOutputHandler());
                    else
                        retval.put("outputHandler", null);
                    break;
                case GetObservation:
                    // assert that we have our needed parameters
                    if (!queryParameters.containsKey("offering") ||
                            !queryParameters.containsKey("observedproperty") ||
                            !queryParameters.containsKey("eventtime") ||
                            !queryParameters.containsKey("responseformat")) {
                        // print out xml error using get caps error?
                        try {
                            capHandler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
                            capHandler.getOutputHandler().setupExceptionOutput("Observation requests must have offering, observedProperty, eventtime, responseFormat as query parameters");
                            retval.put("outputHandler", capHandler.getOutputHandler());
                            retval.put("responseContentType", "text/xml");
                        } catch (Exception e) { }
                        break;
                    }
                    // setup our coordsHash
                    if (queryParameters.containsKey("lat")) {
                        coordsHash.put("lat", queryParameters.get("lat").toString());
                    }
                    if (queryParameters.containsKey("lon")) {
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
<<<<<<< HEAD
                            retval.put("responseContentType", "text/xml");
=======
>>>>>>> fixed tests to adhere to new ouptut format name
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
                        retval.put("outputHandler", null);
                    }
                    break;
                case DescribeSensor:
                    // resposne will always be text/xml
                    retval.put("responseContentType", "text/xml");
                    SOSDescribeSensorHandler sensorHandler;
                    if (!queryParameters.containsKey("responseformat") || !queryParameters.containsKey("procedure")) {
                        sensorHandler = new SOSDescribeSensorHandler(dataset);
                        sensorHandler.getOutputHandler().setupExceptionOutput("responseformat and procedure are required for DescribeSensor requests");
                    } else {
                        // create a describe sensor handler
                        sensorHandler = new SOSDescribeSensorHandler(dataset,
                                queryParameters.get("responseformat").toString(),
                                queryParameters.get("procedure").toString(),
                                threddsURI,
                                query);
                    }
                    retval.put("outputHandler",sensorHandler.getOutputHandler());
                    break;
                default:
                    // return a 'not supported' error
                    System.out.println(queryParameters.get("request").toString() + " is not a supported request");
                    break;
            }
        } catch (IllegalArgumentException ex) {
            // create a get caps respons with exception
            _log.error("Exception with request: " + ex.getMessage());
            System.out.println("Exception encountered " + ex.getMessage());
            try {
                capHandler = new SOSGetCapabilitiesRequestHandler(dataset, threddsURI);
            } catch (Exception e) { }
            capHandler.getOutputHandler().setupExceptionOutput("Request \'" + queryParameters.get("request").toString() + "\' is not supported.");
            retval.put("outputHandler", capHandler.getOutputHandler());
        }
        
        return retval;
    }
    
    private void parseGetCaps(SOSGetCapabilitiesRequestHandler capHandler) throws IOException {
        // do our parsing
        capHandler.parseServiceIdentification();
        capHandler.parseServiceDescription();
        capHandler.parseOperationsMetaData();
        capHandler.parseObservationList();
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
                        queryParameters.put(keyVal[0],val);
                    } catch (Exception e) {
                        System.out.println("Exception in decoding: " + keyVal[1] + " - " + e.getMessage());
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
