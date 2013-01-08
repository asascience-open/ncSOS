/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeNetworkFormatter;
import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import java.io.IOException;
import org.w3c.dom.Element;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Handles Describe Sensor requests specific Grid feature datasets.
 * Describe Sensor requests to Grid datasets for response format "sensorML/1.0.1"
 * output the following xml subroots:
 * *Description
 * *Identification
 * *Classification
 * *Contact(s)
 * *History
 * *Location
 * *Component(s)
 * @author SCowan
 * @version 1.0.0
 */
public class SOSDescribeGrid extends SOSDescribeStation implements SOSDescribeIF {
    
    private String upperLatitude, upperLongitude, lowerLatitude, lowerLongitude;
    
    /**
     * Creates a new instance that collects information from the netCDF dataset
     * needed for a Describe Sensor response.
     * @param dataset the dataset with a GRID feature type
     * @param procedure the procedure of the request (station urn)
     */
    public SOSDescribeGrid( NetcdfDataset dataset, String procedure, LatLonRect gridBBox ) throws IOException {
        super(dataset, procedure);
        // ignore errors from super constructor
        errorString = null;
        // get the bounding box
        upperLatitude = gridBBox.getUpperRightPoint().getLatitude() + "";
        lowerLatitude = gridBBox.getLowerLeftPoint().getLatitude() + "";
        upperLongitude = gridBBox.getUpperRightPoint().getLongitude() + "";
        lowerLongitude = gridBBox.getLowerLeftPoint().getLongitude() + "";
        
        if (stationVariable == null) {
            errorString = "Unable to find variable with station info";
        }
    }
    
    /**
     * Constructor for 'network-all' requests
     * @param dataset
     * @param gridBBox
     * @throws IOException 
     */
    public SOSDescribeGrid( NetcdfDataset dataset, LatLonRect gridBBox ) throws IOException {
        super(dataset);
        // ignore errors from super constructor
        errorString = null;
        // get the bounding box
        upperLatitude = gridBBox.getUpperRightPoint().getLatitude() + "";
        lowerLatitude = gridBBox.getLowerLeftPoint().getLatitude() + "";
        upperLongitude = gridBBox.getUpperRightPoint().getLongitude() + "";
        lowerLongitude = gridBBox.getLowerLeftPoint().getLongitude() + "";
        
        if (stationVariable == null) {
            errorString = "Unable to find variable with station info";
        }
    }
    
    /*********************
     * Interface Methods *
     **************************************************************************/
    @Override
    public void setupOutputDocument(DescribeSensorFormatter output) {
        if (errorString == null) {
            // system node
            output.setSystemId("station-" + stationName);
            // set description
            formatSetDescription(output);
            // identification node
            formatSetIdentification(output);
            // classification node
            formatSetClassification(output);
            // contact node
            formatSetContactNodes(output);
            // history node
            formatSetHistoryNodes(output);
            // location node
            formatSetLocationNode(output);
            // remove unwanted nodes
            removeUnusedNodes(output);
        } else {
            output.setupExceptionOutput(errorString);
        }
    }
    /**************************************************************************/

    /*****************************
     * Private/Protected Methods *
     *****************************/
    private void removeUnusedNodes(DescribeSensorFormatter output) {
        output.deletePosition();
        output.deletePositions();
        output.deleteTimePosition();
    }
    
    @Override
    protected void formatSetStationComponentList(DescribeNetworkFormatter output) {
        // iterate through each station to set its info
        for (String stName : getStationNames().values()) {
            // add a new node
            Element stNode = output.addNewStationWithId("station-" + stName);
            // set a description for each station - TODO
            output.removeStationDescriptionNode(stNode);
            // set identification for station
            formatSetStationIdentification(output, stNode, stName);
            // set location for station
            String[] upperCorner = new String[] { upperLatitude, upperLongitude };
            String[] lowerCorner = new String[] { lowerLatitude, lowerLongitude };
            output.setStationLocationNodeWithBoundingBox(stNode, lowerCorner, upperCorner);
            // remove unwanted nodes for each station
            output.removeStationPosition(stNode);
            output.removeStationPositions(stNode);
            output.removeStationTimePosition(stNode);
        }
    }
    
    /**
     * 
     * @param output
     */
    @Override
    protected void formatSetLocationNode(DescribeSensorFormatter output) {
        String[] upperCorner = new String[]{ upperLatitude, upperLongitude };
        String[] lowerCorner = new String[]{ lowerLatitude, lowerLongitude };
        
        output.setLocationNodeWithBoundingBox(lowerCorner, upperCorner);
    }
    
}
