/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.*;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.DatasetHandlerAdapter;
import com.asascience.ncsos.util.DiscreteSamplingGeometryUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.*;
import ucar.nc2.time.CalendarDate;

/**
 * Main handler class for Describe Sensor requests. Processes the request to determine
 * what output format is being used as well as determine the feature type of the
 * dataset. Also calls the output handler to prepare the output for the response.
 * @author SCowan
 * @version 1.0.0
 */
public class SOSDescribeSensorHandler extends SOSBaseRequestHandler {
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSDescribeSensorHandler.class);
    private final String procedure;
    private ISOSDescribeSensor describer;
    
    private final String ACCEPTABLE_RESPONSE_FORMAT = "text/xml;subtype=\"sensorML/1.0.1\"";
    
    /**
     * Creates a DescribeSensor handler that will parse the information and setup
     * the output handler
     * @param dataset netcdf dataset being read
     * @param responseFormat response format from the request query string; only
     * accepted response format is "text/xml;subtype=\"sensorML/1.0.1\""
     * @param procedure procedure of the request (urn of station or sensor)
     * @param uri entire uri string from the request
     * @param query entire query string from the request
     * @throws IOException 
     */
    public SOSDescribeSensorHandler(NetcdfDataset dataset, String responseFormat, String procedure, String uri, String query) throws IOException {
        super(dataset);
        
        this.procedure = procedure;
        
        // test that the dataset can be handled properly
        if (getFeatureDataset() == null && getGridDataset() == null) {
            output = new BaseOutputFormatter();
            output.setupExceptionOutput("Unable to handle requested dataset. Make sure that it has a properly defined feature type.");
            return;
        }
        
        // make sure that the responseFormat we recieved is acceptable
        if (!responseFormat.equalsIgnoreCase(ACCEPTABLE_RESPONSE_FORMAT)) {
            // return exception
            output = new BaseOutputFormatter();
            _log.error("got unhandled response format " + responseFormat + "; printing exception...");
            output.setupExceptionOutput("Unhandled response format " + responseFormat);
            return;
        }
        
        // check our procedure
        if (!checkDatasetForProcedure(procedure)) {
            // the procedure does not match any known procedure
            output = new BaseOutputFormatter();
            _log.error("Could not match procedure " + procedure);
            output.setupExceptionOutput("Procedure parameter does not match any known procedure. Please check the capabilities response document for valid procedures.");
            return;
        }
        
        // find out needed info based on whether this is a station or sensor look up
        if (this.procedure.contains("station")) {
            setNeededInfoForStation(dataset, uri, query);
            describer.setupOutputDocument(output);
        } else if (this.procedure.contains("sensor")) {
            output = new DescribeSensorFormatter(uri, query);
            setNeededInfoForSensor(dataset);
            describer.setupOutputDocument((DescribeSensorFormatter)output);
        } else if (this.procedure.contains("network")) {
            output = new DescribeSensorNetworkMilestone1_0();
            describer = new SOSDescribeNetworkM1_0(dataset, procedure, query);
            describer.setupOutputDocument(output);
//            output = new DescribeNetworkFormatter(uri, query);
//            setNeededInfoForNetwork(dataset);
//            describer.setupOutputDocument((DescribeNetworkFormatter)output);
        } else {
            output = new BaseOutputFormatter();
            output.setupExceptionOutput("Unknown procedure (not a station, sensor or 'network'): " + this.procedure);
            return;
        }
    }

    /**
     * Exception version, used to create skeleton SOSDescribeSensorHandler that
     * can throw an exception
     * @param dataset dataset, mostly unused
     * @throws IOException 
     */
    public SOSDescribeSensorHandler(NetcdfDataset dataset) throws IOException {
        super(dataset);
        
        output = new DescribeSensorFormatter();
        this.procedure = null;
    }
    
    /**
     * Procedure was a 'network' urn
     * @param dataset dataset we are doing the request against
     * @throws IOException 
     */
    private void setNeededInfoForStation( NetcdfDataset dataset, String uri, String query ) throws IOException {
        // get our information based on feature type
        output = new DescribeSensorPlatformMilestone1_0();
        describer = new SOSDescribePlatformM1_0(dataset, procedure, uri);
    }
    
    private void setNeededInfoForSensor( NetcdfDataset dataset ) throws IOException {
        // describe sensor (sensor) is very similar to describe sensor (station)
        describer = new SOSDescribeSensor(dataset, procedure);
    }

    private boolean checkDatasetForProcedure(String procedure) {
        if (procedure == null) {
            _log.error("procedure is null");
            return false;
        }
        if (procedure.toLowerCase().contains("network") && procedure.toLowerCase().contains("all"))
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
            if (getGMLName(stationName).equalsIgnoreCase(procedure))
                return true;
        }
        // go through each sensor urn and compare it to procedure
        for (String sensorName : getSensorNames()) {
            for (String stationName : stationNames.values()) {
                if (getSensorGMLName(stationName, sensorName).equalsIgnoreCase(procedure))
                    return true;
            }
        }
        
        return false;
    }

    private void setNeededInfoForNetwork(NetcdfDataset dataset) throws IOException {
        // get our information based on feature type
        switch (getFeatureDataset().getFeatureType()) {
            case STATION:
            case STATION_PROFILE:
                describer = new SOSDescribeStation(dataset);
                break;
            case TRAJECTORY:
                describer = new SOSDescribeTrajectory(dataset);
                break;
            case GRID:
                DatasetHandlerAdapter.calcBounds(getFeatureDataset());
                describer = new SOSDescribeGrid(dataset, getFeatureDataset().getBoundingBox());
                break;
            case PROFILE:
                describer = new SOSDescribeProfile(dataset);
                break;
            case SECTION:
                DatasetHandlerAdapter.calcBounds(getFeatureDataset());
                describer = new SOSDescribeSection(dataset, (SectionFeatureCollection) getFeatureTypeDataSet());
                break;
        }
    }
}
