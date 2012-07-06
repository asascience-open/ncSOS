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
 *
 * @author SCowan
 */
public class SOSDescribeSensor extends SOSDescribeStation implements SOSDescribeIF {
    
    private String sensorId;
    private Variable sensorVariable;
    
    public SOSDescribeSensor( NetcdfDataset dataset, String procedure ) {
        super(dataset, procedure);
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
            throw new IllegalArgumentException("Unable to find sensor " + sensorId + " in dataset!");
        }
        
        stationCoords = getStationCoords(lat, lon);
    }
    
    /*********************
     * Interface Methods *
     *********************/
    @Override
    public void SetupOutputDocument(DescribeSensorFormatter output) {
        // system node
        output.setSystemId("sensor-" + stationName + "-" + sensorId);
        // set description
        formatSetDescription(output);
        // identification node
        formatSetIdentification(output);
        // contact node
        formatSetContactNodes(output);
        // document node
        formatSetDocumentNodes(output);
        // location node
        formatSetLocationNode(output);
        // remove unwanted nodes
        RemoveUnusedNodes(output);
    }

    /*******************
     * Private Methods *
     *******************/
    private void RemoveUnusedNodes(DescribeSensorFormatter output) {
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
    
    @Override
    protected void formatSetDescription(DescribeSensorFormatter output) {
        output.setDescriptionNode("Sensor metadata for " + sensorId + " on " + stationName);
    }
}
