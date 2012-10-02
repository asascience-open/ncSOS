/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.DiscreteSamplingGeometryUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ucar.nc2.Variable;
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
    private String procedure;
    private SOSDescribeIF describer;
    
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
        
        output = new DescribeSensorFormatter(uri, query);
        
        // make sure that the responseFormat we recieved is acceptable
        if (!responseFormat.equalsIgnoreCase(ACCEPTABLE_RESPONSE_FORMAT)) {
            // return exception
            System.out.println("got unhandled response format " + responseFormat + "; printing exception...");
            output.setupExceptionOutput("Unhandled response format " + responseFormat);
            return;
        }
        
        // check our procedure
        if (!checkDatasetForProcedure(procedure)) {
            // the procedure does not match any known procedure
            output.setupExceptionOutput("Procedure parameter does not match any known procedure. Please check the capabilities response document for valid procedures.");
            return;
        }
        
        this.procedure = procedure;
        
        // test that the dataset can be handled properly
        if (getFeatureDataset() == null)
        {
            output.setupExceptionOutput("Unable to handle requested dataset. Make sure that it has a properly defined feature type.");
            return;
        }
        
        // find out needed info based on whether this is a station or sensor look up
        if (this.procedure.contains("station")) {
            setNeededInfoForStation(dataset);
            describer.setupOutputDocument((DescribeSensorFormatter)output);
        } else if (this.procedure.contains("sensor")) {
            setNeededInfoForSensor(dataset);
            describer.setupOutputDocument((DescribeSensorFormatter)output);
        } else {
            output.setupExceptionOutput("Unknown procedure (not a station or sensor): " + this.procedure);
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
    }
    
    private void setNeededInfoForStation( NetcdfDataset dataset ) throws IOException {
        // get our information based on feature type
        switch (getFeatureDataset().getFeatureType()) {
            case STATION:
            case STATION_PROFILE:
                describer = new SOSDescribeStation(dataset, procedure);
                ((DescribeSensorFormatter)output).setComponentsNode(DiscreteSamplingGeometryUtil.getDataVariables(getFeatureDataset()), procedure);
                break;
            case TRAJECTORY:
                // need our starting date for the observations from our FeatureTypeDataSet wrapper
                TrajectoryFeatureCollection feature = (TrajectoryFeatureCollection) getFeatureTypeDataSet();
                CalendarDate colStart = null;
                for (feature.resetIteration();feature.hasNext();) {
                    TrajectoryFeature traj = feature.next();
                    traj.calcBounds();
                    for (traj.resetIteration();traj.hasNext();) {
                        PointFeature pf = traj.next();
                        if (colStart == null)
                            colStart = pf.getObservationTimeAsCalendarDate();
                        else if (pf.getObservationTimeAsCalendarDate().compareTo(colStart) < 0)
                            colStart = pf.getObservationTimeAsCalendarDate();
                    }
                }
                describer = new SOSDescribeTrajectory(dataset, procedure, colStart);
                ((DescribeSensorFormatter)output).setComponentsNode(DiscreteSamplingGeometryUtil.getDataVariables(getFeatureDataset()), procedure);
                break;
            case PROFILE:
                describer = new SOSDescribeProfile(dataset, procedure);
                ((DescribeSensorFormatter)output).setComponentsNode(DiscreteSamplingGeometryUtil.getDataVariables(getFeatureDataset()), procedure);
                break;
            case GRID:
                describer = new SOSDescribeGrid(dataset, procedure);
                ((DescribeSensorFormatter)output).setComponentsNode(getGridDataset().getDataVariables(),procedure);
                break;
            case SECTION:
                // trajectory profile; need the trajectory number to get the right info
                String[] tStr = procedure.split(":");
                String nStr = tStr[tStr.length-1].toLowerCase();
                nStr = nStr.replaceAll("(profile)", "").replaceAll("(trajectory)", "");
                int tNumber = Integer.parseInt(nStr);
                
                getFeatureDataset().calcBounds();
                SectionFeatureCollection sectionCollection = (SectionFeatureCollection) getFeatureTypeDataSet();
                ArrayList<CalendarDate> secColStart = new ArrayList<CalendarDate>();
                int i=-1;
                for (sectionCollection.resetIteration();sectionCollection.hasNext();) {
                    SectionFeature section = sectionCollection.next();
                    if (++i == (tNumber-1)) {
                        i=0;
                        for (section.resetIteration();section.hasNext();) {
                            ProfileFeature pfeature = section.next();
                            pfeature.calcBounds();
                            for (pfeature.resetIteration();pfeature.hasNext();) {
                                // iterate through data to make sure various items (ie start date) isn't null
                                PointFeature pointf = pfeature.next();
                            }
                            if (pfeature.getCalendarDateRange() != null && pfeature.getCalendarDateRange().getStart() != null)
                                secColStart.add(pfeature.getCalendarDateRange().getStart());
                        }
                        break;
                    }
                }
                
                describer = new SOSDescribeSection(dataset, procedure, secColStart.toArray(new CalendarDate[secColStart.size()]));
                ((DescribeSensorFormatter)output).setComponentsNode(DiscreteSamplingGeometryUtil.getDataVariables(getFeatureDataset()), procedure);
                break;
            default:
                _log.error("Unhandled feature type: " + getFeatureDataset().getFeatureType().toString());
                break;
        }
        
    }
    
    private void setNeededInfoForSensor( NetcdfDataset dataset ) {
        // describe sensor (sensor) is very similar to describe sensor (station)
        describer = new SOSDescribeSensor(dataset, procedure);
    }

    private boolean checkDatasetForProcedure(String procedure) {
        if (procedure == null)
            return false;
        // get a list of procedures from dataset and compare it to the passed-in procedure
        // get list of station names
        List<String> stationNames = getStationNames();
        // go through each station urn and compare it to procedure
        for (String stationName : stationNames) {
            if (getGMLName(stationName).equals(procedure))
                return true;
        }
        // go through each sensor urn and compare it to procedure
        for (String sensorName : getSensorNames()) {
            for (String stationName : stationNames) {
                if (getSensorGMLName(stationName, sensorName).equals(procedure))
                    return true;
            }
        }
        
        return false;
    }
}
