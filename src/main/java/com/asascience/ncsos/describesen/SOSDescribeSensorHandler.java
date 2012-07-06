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
import ucar.nc2.Attribute;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.TrajectoryFeature;
import ucar.nc2.ft.TrajectoryFeatureCollection;
import ucar.nc2.time.CalendarDate;

/**
 *
 * @author SCowan
 */
public class SOSDescribeSensorHandler extends SOSBaseRequestHandler {
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSDescribeSensorHandler.class);
    private String procedure;
    private ArrayList<Attribute> contactInfo;
    private ArrayList<Attribute> documentationInfo;
    private SOSDescribeIF describer;
    
    private final String ACCEPTABLE_RESPONSE_FORMAT = "text/xml;subtype=\"sensorML/1.0.1\"";
    
    /**
     * Creates a DescribeSensor handler that will parse the information and setup the output handle
     * @param dataset
     * @param responseFormat
     * @param procedure
     * @throws IOException 
     */
    public SOSDescribeSensorHandler(NetcdfDataset dataset, String responseFormat, String procedure, String uri, String query) throws IOException {
        super(dataset);
        
        output = new DescribeSensorFormatter(uri, query);
        
        // make sure that the responseFormat we recieved is acceptable
        if (!responseFormat.equalsIgnoreCase(ACCEPTABLE_RESPONSE_FORMAT)) {
            // return exception
            output.setupExceptionOutput("Unhandled response format " + responseFormat);
            return;
        }
        
        this.procedure = procedure;
        
        getContactInfoAttributes(dataset.getGlobalAttributes());
        getDocumentationAttributes(dataset.getGlobalAttributes());
        
        // find out needed info based on whether this is a station or sensor look up
        if (this.procedure.contains("station")) {
            setNeededInfoForStation(dataset);
            describer.SetupOutputDocument((DescribeSensorFormatter)output);
            // need to set the components here should be universal for station inquiries
            ((DescribeSensorFormatter)output).setComponentsNode(DiscreteSamplingGeometryUtil.getDataVariables(getFeatureDataset()), procedure);
        } else if (this.procedure.contains("sensor")) {
            setNeededInfoForSensor(dataset);
            describer.SetupOutputDocument((DescribeSensorFormatter)output);
        } else {
            output.setupExceptionOutput("Unknown procedure (not a station or sensor): " + this.procedure);
        }
    }

    /**
     * Exception version, used to create skeleton SOSDescribeSensorHandler that can throw an exception
     * @param dataset dataset, mostly unused
     * @throws IOException 
     */
    public SOSDescribeSensorHandler(NetcdfDataset dataset) throws IOException {
        super(dataset);
    }
    
    private void setNeededInfoForStation( NetcdfDataset dataset ) throws IOException {
        // get our information based on feature type
        switch (getFeatureDataset().getFeatureType()) {
            case STATION:
            case STATION_PROFILE:
                describer = new SOSDescribeStation(dataset, procedure);
                break;
            case TRAJECTORY:
                // need our starting date for the observations
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
                break;
            case PROFILE:
                describer = new SOSDescribeProfile(dataset, procedure);
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
    
    private void getContactInfoAttributes( List<Attribute> globalAttrs ) {
        // look for creator attributes, as well as institution
        contactInfo = new ArrayList<Attribute>();
        for (Attribute attr : globalAttrs) {
            String name = attr.getName();
            if (name.contains("contact")) {
                contactInfo.add(attr);
            }
        }
    }
    
    private void getDocumentationAttributes( List<Attribute> globalAttrs ) {
        // look for any attributes related to documentation
        documentationInfo = new ArrayList<Attribute>();
        for (Attribute attr : globalAttrs) {
            String name = attr.getName();
            if (name.contains("doc")) {
                documentationInfo.add(attr);
            }
        }
    }
}
