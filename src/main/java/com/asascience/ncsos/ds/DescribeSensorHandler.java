/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.ds;

import com.asascience.ncsos.outputformatter.ds.OosTethysFormatter;
import com.asascience.ncsos.outputformatter.OutputFormatter;
import java.io.IOException;
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
public class DescribeSensorHandler extends DescribeStationHandler implements BaseDSInterface {
    public static final String SENSOR = "sensor-";
    public static final String SPACER = "-";
    
    private String sensorId;
    private Variable sensorVariable;
    
    /**
     * Creates instance to collect information, from the dataset, need for a
     * Describe Sensor response for Sensors.
     * @param dataset netcdf dataset of any feature type
     * @param procedure request procedure (urn of sensor)
     */
    public DescribeSensorHandler(NetcdfDataset dataset, String procedure) throws IOException {
        super(dataset, procedure);
        // ignore errors from the station constructor
        errorString = null;
        
        // set our actual station id (and sensor id)
//        String[] sensorSplit = procedure.split("(::)");
//        sensorId = sensorSplit[1];
        String[] stationSplit = procedure.split(":");
        stationName = stationSplit[stationSplit.length-2];
        sensorId = stationSplit[stationSplit.length-1];
        
        // get our sensor var
        for (Variable var : dataset.getVariables()) {
            String varName = var.getFullName().toLowerCase();
            if (varName.equalsIgnoreCase(sensorId)) {
                sensorVariable = var;
            }
        }
        
        if (sensorVariable == null) {
            errorString = "Unable to find sensor " + sensorId + " in the dataset!";
        }
        
        stationCoords = getStationCoords(latVariable, lonVariable);
        
        if (stationCoords == null || stationCoords.length < 1)
            errorString = "Unable to find station " + stationName + " in the dataset!";
    }
    
    /*********************
     * Interface Methods *
     **************************************************************************/

    @Override
    public void setupOutputDocument(OutputFormatter output) {
        OosTethysFormatter dsf = new OosTethysFormatter();
        if (errorString == null) {
            // system node
            dsf.setSystemId(SENSOR + stationName + SPACER + sensorId);
            // set description
            formatSetDescription(dsf);
            // identification node
            formatSetIdentification(dsf);
            // contact node
            formatSetContactNodes(dsf);
            // location node
            formatSetLocationNode(dsf);
            // remove unwanted nodes
            removeUnusedNodes(dsf);
        } else {
            output.setupExceptionOutput(errorString);
        }
    }
    
    /**************************************************************************/

    /*******************
     * Private Methods *
     *******************/
    private void removeUnusedNodes(OosTethysFormatter output) {
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
     * @param output a OosTethysFormatter instance
     */
    @Override
    protected void formatSetIdentification(OosTethysFormatter output) {
        ArrayList<String> identNames = new ArrayList<String>();
        ArrayList<String> identDefinitions = new ArrayList<String>();
        ArrayList<String> identValues = new ArrayList<String>();
        identNames.add("SensorId"); identDefinitions.add(MMI_DEF_URL + "sensorID"); identValues.add(procedure);
        for (Attribute attr : sensorVariable.getAttributes()) {
            identNames.add(attr.getFullName()); identDefinitions.add(MMI_DEF_URL + attr.getFullName()); identValues.add(attr.getValue(0).toString());
        }
        output.setIdentificationNode(identNames.toArray(new String[identNames.size()]),
                identDefinitions.toArray(new String[identDefinitions.size()]),
                identValues.toArray(new String[identValues.size()]));
    }
    
    /**
     * Sets the gml:description node for Describe Sensor "sensorML/1.0.1" requests
     * @param output  a OosTethysFormatter instance
     */
    @Override
    protected void formatSetDescription(OosTethysFormatter output) {
        output.setDescriptionNode("Sensor metadata for " + sensorId + " on " + stationName);
    }
}
