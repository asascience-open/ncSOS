/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import java.util.ArrayList;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Handles Describe Sensor requests specific for calling sensors directly. Due to
 * the generic output of a direct Describe Sensor call to a station sensor, all
 * feature types use this for describing their sensor(s).
 * Describe Sensor requests to sensor(s) for the response format "sensorML/1.0.1"
 * output the following xml subroots:
 * *Description
 * *Identification
 * *Contact(s)
 * *Location
 * @author SCowan
 * @version 1.0.0
 */
public class SOSDescribeSensor extends SOSDescribeStation implements SOSDescribeIF {
    
    private String sensorId;
    private Variable sensorVariable;
    
    /**
     * Creates instance to collect information, from the dataset, need for a
     * Describe Sensor response for Sensors.
     * @param dataset netcdf dataset of any feature type
     * @param procedure request procedure (urn of sensor)
     */
    public SOSDescribeSensor( NetcdfDataset dataset, String procedure ) {
        super(dataset, procedure);
        // ignore errors from the station constructor
        errorString = null;
        Variable lat, lon;
        lat = lon = null;
        
        // set our actual station id (and sensor id)
        String[] sensorSplit = procedure.split("(::)");
        sensorId = sensorSplit[1];
        String[] stationSplit = sensorSplit[0].split(":");
        stationName = stationSplit[stationSplit.length-1];
        
        // get our sensor var
        for (Variable var : dataset.getVariables()) {
            String varName = var.getFullName().toLowerCase();
            if (varName.equalsIgnoreCase(sensorId)) {
                sensorVariable = var;
            }
            else if (varName.contains("lat"))
                lat = var;
            else if (varName.contains("lon"))
                lon = var;
        }
        
        if (sensorVariable == null) {
            errorString = "Unable to find sensor " + sensorId + " in the dataset!";
        }
        
        stationCoords = getStationCoords(lat, lon);
        
        if (stationCoords[0] == Double.NaN && stationCoords[1] == Double.NaN)
            errorString = "Unable to find station " + stationName + " in the dataset!";
    }
    
    /*********************
     * Interface Methods *
     **************************************************************************/
    
    @Override
    public void setupOutputDocument(DescribeSensorFormatter output) {
        if (errorString == null) {
            // system node
            output.setSystemId("sensor-" + stationName + "-" + sensorId);
            // set description
            formatSetDescription(output);
            // identification node
            formatSetIdentification(output);
            // contact node
            formatSetContactNodes(output);
            // location node
            formatSetLocationNode(output);
            // remove unwanted nodes
            removeUnusedNodes(output);
        } else {
            output.setupExceptionOutput(errorString);
        }
    }
    
    /**************************************************************************/

    /*******************
     * Private Methods *
     *******************/
    private void removeUnusedNodes(DescribeSensorFormatter output) {
        output.deleteClassificationNode();
        output.deleteComponentsNode();
        output.deleteHistoryNode();
        output.deletePosition();
        output.deletePositions();
        output.deleteTimePosition();
    }
    
    /*********************
     * Protected Methods *
     *********************/
    
    /**
     * Sets the sml:Identification node for Describe Sensor "sensorML/1.0.1" requests
     * @param output a DescribeSensorFormatter instance
     */
    @Override
    protected void formatSetIdentification(DescribeSensorFormatter output) {
        ArrayList<String> identNames = new ArrayList<String>();
        ArrayList<String> identDefinitions = new ArrayList<String>();
        ArrayList<String> identValues = new ArrayList<String>();
        identNames.add("SensorId"); identDefinitions.add("sensorID"); identValues.add(procedure);
        for (Attribute attr : sensorVariable.getAttributes()) {
            identNames.add(attr.getName()); identDefinitions.add(""); identValues.add(attr.getStringValue());
        }
        output.setIdentificationNode(identNames.toArray(new String[identNames.size()]),
                identDefinitions.toArray(new String[identDefinitions.size()]),
                identValues.toArray(new String[identValues.size()]));
    }
    
    /**
     * Sets the gml:description node for Describe Sensor "sensorML/1.0.1" requests
     * @param output  a DescribeSensorFormatter instance
     */
    @Override
    protected void formatSetDescription(DescribeSensorFormatter output) {
        output.setDescriptionNode("Sensor metadata for " + sensorId + " on " + stationName);
    }
}
