/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.ds;

import com.asascience.ncsos.outputformatter.ErrorFormatter;
import com.asascience.ncsos.outputformatter.ds.IoosNetwork10Formatter;
import com.asascience.ncsos.outputformatter.ds.IoosPlatform10Formatter;
import com.asascience.ncsos.service.BaseRequestHandler;
import ucar.nc2.dataset.NetcdfDataset;

import java.io.IOException;
import java.util.HashMap;

/**
 * Main handler class for Describe Sensor requests. Processes the request to determine
 * what output format is being used as well as determine the feature type of the
 * dataset. Also calls the output handler to prepare the output for the response.
 */
public class BaseDSHandler extends BaseRequestHandler {
    public static final String ALL = "all";
    public static final String NETWORK = "network";
    public static final String SENSOR = "sensor";
    public static final String STATION = "station";
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(BaseDSHandler.class);
    private final String procedure;
    private BaseDSInterface describer;

    private final String ACCEPTABLE_RESPONSE_FORMAT = "text/xml;subtype=\"sensorML/1.0.1/profiles/ioos_sos/1.0\"";
    
    /**
     * Creates a DescribeSensorHandler handler that will parse the information and setup
     * the output handler
     * @param dataset netcdf dataset being read
     * @param outputFormat response format from the request query string
     * @param procedure procedure of the request (urn of station or sensor)
     * @param uri entire uri string from the request
     * @param query entire query string from the request
     * @throws IOException 
     */
    public BaseDSHandler(NetcdfDataset dataset, String outputFormat, String procedure, String uri, String query) throws IOException {
        super(dataset);
        
        this.procedure = procedure;
        
        // test that the dataset can be handled properly
        if (getFeatureDataset() == null && getGridDataset() == null) {
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException("NetCDF-Java could not determine a valid FeatureType for this dataset.");
            return;
        }
        switch (getDatasetFeatureType()) {
            case POINT:
                formatter = new ErrorFormatter();
                ((ErrorFormatter)formatter).setException("NcSOS does not support the Point featureType at this time.");
                return;
        }
        
        // make sure that the outputFormat we received is acceptable
        if (!outputFormat.replaceAll(";\\s+subtype",";subtype").equalsIgnoreCase(ACCEPTABLE_RESPONSE_FORMAT)) {
            // return exception
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException("Unknown outputFormat: " + outputFormat, 
                                                     INVALID_PARAMETER, "outputFormat");
            return;
        }

        // check our procedure
        if (!checkDatasetForProcedure(procedure)) {
            // the procedure does not match any known procedure
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException("Procedure parameter does not match any known procedure. "+
                                            "Please check the capabilities response document for valid procedures.", 
                                            INVALID_PARAMETER, 
                                            "procedure");
            return;
        }
        
        // find out needed info based on whether this is a station or sensor look up
        if (this.procedure.contains(STATION)) {
            setNeededInfoForStation(dataset, uri, query);
            describer.setupOutputDocument(formatter);
        } else if (this.procedure.contains(SENSOR)) {
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException("NcSOS does not currently support DescribeSensor for sensor procedures.",
                                                      OPTION_NOT_SUPPORTED, "procedure");
        } else if (this.procedure.contains(NETWORK)) {
            formatter = new IoosNetwork10Formatter();
            describer = new IoosNetwork10Handler(dataset, procedure, query);
            describer.setupOutputDocument(formatter);
        } else {
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException("Unknown procedure (not a 'station', 'sensor' or 'network'): " + 
                                                    this.procedure, INVALID_PARAMETER, "procedure");
        }
    }

    /**
     * Procedure was a 'network' urn
     * @param dataset dataset we are doing the request against
     * @throws IOException 
     */
    private void setNeededInfoForStation( NetcdfDataset dataset, String uri, String query ) throws IOException {
        // get our information based on feature type
        formatter = new IoosPlatform10Formatter();
        describer = new IoosPlatform10Handler(dataset, procedure, uri);
    }

    private boolean checkDatasetForProcedure(String procedure) {
        if (procedure == null) {
            _log.error("procedure is null");
            return false;
        }
        if (procedure.equals(this.getUrnNetworkAll()))
            return true;
        // get a list of procedures from dataset and compare it to the passed-in procedure
        // get list of station names
        HashMap<Integer,String> stationNames = getStationNames();
        if (stationNames == null) {
            _log.error("stationNames is null");
            return false;
        }
        // go through each station urn and compare it to procedure
        for (String stationName : stationNames.values()) {
            if (getUrnName(stationName).equalsIgnoreCase(procedure))
                return true;
        }
        // go through each sensor urn and compare it to procedure
        for (String sensorName : getSensorNames()) {
            for (String stationName : stationNames.values()) {
                if (getSensorUrnName(stationName, sensorName).equalsIgnoreCase(procedure))
                    return true;
            }
        }
        
        return false;
    }
}
