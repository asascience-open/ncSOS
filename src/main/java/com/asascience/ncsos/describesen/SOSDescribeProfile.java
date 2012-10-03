/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Handles Describe Sensor requests for Profile feature datasets.
 * Describe Sensor requests to Profile datasets for response "sensorML/1.0.1"
 * output the following xml subroots:
 * *Description
 * *Identification
 * *Classification
 * *Contact(s)
 * *History
 * *Positions (note the plural)
 * *Component(s)
 * @author SCowan
 * @version 1.0.0
 */
public class SOSDescribeProfile extends SOSDescribeStation implements SOSDescribeIF {
    
    int profileNumber;
    int profileStartIndex, profileEndIndex;
    Integer[] profileIndices;
    
    /**
     * Creates a new instance to collect information, from the dataset, needed for
     * a Describe Sensor response.
     * @param dataset netcdf dataset with the Profile feature type
     * @param procedure procedure of the request (station urn)
     */
    public SOSDescribeProfile( NetcdfDataset dataset, String procedure ) throws IOException {
        super(dataset,procedure);
        // ignore errors from the super constructor
        errorString = null;
        
        // stripping "profile" out of station name should result in our profile number
        String profileNumStr = stationName.toLowerCase().replaceAll("(profile)", "");
        profileNumber = Integer.parseInt(profileNumStr);
        
        for (Variable var : dataset.getVariables()) {
            String varName = var.getFullName().toLowerCase();
            // look for the variable that describes our indexing
            if (varName.contains("index")) {
                try {
                    // this lists the profile number of each obs
                    Array obsIndices = var.read();
                    ArrayList<Integer> indexBuilder = new ArrayList<Integer>();
                    // iterate through the 'Array' to find all values which match our profileNumber
                    for (int i=0; i<obsIndices.getSize(); i++) {
                        if (profileNumber == obsIndices.getInt(i))
                            indexBuilder.add(i);
                    }
                    // convert to our array
                    profileIndices = indexBuilder.toArray(new Integer[indexBuilder.size()]);
                } catch (IOException ex) {
                    System.out.println("Error in DescribeProfile constructor - " + ex.getMessage());
                    errorString = "Error in DescribeProfile constructor - " + ex.getMessage();
                    return;
                }
            }
            else if (varName.contains("rowsize")) {
                try {
                    int profileIndex = profileNumber;
                    Array obsIndices = var.read();
                    // add up our lower and upper index bounds
                    profileStartIndex = profileEndIndex = 0;
                    for (int i=0; i<obsIndices.getSize(); i++) {
                        if (i < profileIndex) {
                            profileStartIndex += obsIndices.getInt(i);
                        } else if (i == profileIndex) {
                            profileEndIndex = profileStartIndex + obsIndices.getInt(i);
                            break;
                        }
                    }
                } catch (IOException ex) {
                    System.out.println("Error in DescribeProfile constructor - " + ex.getMessage());
                    errorString = "Error in DescribeProfile constructor - " + ex.getMessage();
                    return;
                }
            }
        }
        
        if (stationVariable == null)
            errorString = "Could not find expected Variable holding needed station information in the dataset";
        
        try {
            if (stationVariable.read().getShape()[0] <= profileNumber)
                errorString = stationName + " is not in the dataset!";
        } catch (Exception ex) {
            errorString = "Error reading station variable: " + ex.getLocalizedMessage();
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
            // positions node
            formatSetPositionsNode(output);
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
        // delete unwanted nodes
        output.deleteLocationNode();
        output.deletePosition();
        output.deleteTimePosition();
    }

    private void formatSetPositionsNode(DescribeSensorFormatter output) {
        HashMap<String,String> latitude, longitude, depthMap;
        // set our hashmaps for our station
        latitude = new HashMap<String, String>();
        for (Attribute attr : latVariable.getAttributes()) {
            String attrName = attr.getName().toLowerCase();
            if (attrName.equals("standard_name")) {
                latitude.put("name", attr.getStringValue());
            }
            else if (attrName.contains("unit")) {
                latitude.put("code", attr.getStringValue());
            }
            else if (attrName.equals("axis")) {
                latitude.put("axisID", attr.getStringValue());
            }
        }
        latitude.put("value", getValueAtProfileIndex(latVariable));
        
        longitude = new HashMap<String, String>();
        for (Attribute attr : lonVariable.getAttributes()) {
            String attrName = attr.getName().toLowerCase();
            if (attrName.equals("standard_name")) {
                longitude.put("name", attr.getStringValue());
            }
            else if (attrName.contains("unit")) {
                longitude.put("code", attr.getStringValue());
            }
            else if (attrName.equals("axis")) {
                longitude.put("axisID", attr.getStringValue());
            }
        }
        longitude.put("value", getValueAtProfileIndex(lonVariable));
        
        depthMap = new HashMap<String, String>();
        for (Attribute attr : depthVariable.getAttributes()) {
            String attrName = attr.getName().toLowerCase();
            if (attrName.equals("standard_name")) {
                depthMap.put("name", attr.getStringValue());
            }
            else if (attrName.contains("unit")) {
                depthMap.put("code", attr.getStringValue());
            }
            else if (attrName.equals("axis")) {
                depthMap.put("axisID", attr.getStringValue());
            }
        }
        depthMap.put("value", "0");
        
        //set our station
        output.setStationPositionsNode(latitude, longitude, depthMap, "");
        // get our depth for the end point
        try {
            Array array = depthVariable.read();
            ArrayList<Double> depthValues = new ArrayList<Double>();
            if (profileIndices != null) {
                for (int i=0; i<profileIndices.length; i++) {
                    depthValues.add(array.getDouble(profileIndices[i]));
                }
            } else {
                for (int j=profileStartIndex; j<profileEndIndex; j++) {
                    depthValues.add(array.getDouble(j));
                }
            }
            Double largestVal = 0d;
            for (Double dbl : depthValues) {
                if (Math.abs(dbl.doubleValue()) > Math.abs(largestVal.doubleValue()))
                    largestVal = dbl;
            }
            depthMap.put("value", largestVal.toString());
        } catch (IOException ex) {
            System.out.println("Exception in formatSetPositionsNode - " + ex.getMessage());
        }
        // set our end point
        output.setEndPointPositionsNode(latitude, longitude, depthMap, "");
    }
    
    private String getValueAtProfileIndex(Variable var) {
        String retval = "";
        try {
            Array varArray = var.read();
            retval += varArray.getDouble(profileNumber-1);
        } catch (IOException ex) {
            
        }
        return retval;
    }
}
