/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Used to populate output for TimeSeries and TimeSeriesProfile feature sets. Is also used
 * as a parent class for most other feature types.
 * Describe Sensor requests to TimeSeries and TimeSeriesProfile datasets output
 * the following xml subroots:
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
public class SOSDescribeStation implements SOSDescribeIF {
    
    protected Variable stationVariable;
    protected Attribute platformType, historyAttribute;
    protected String stationName;
    protected String description;
    protected double[] stationCoords;
    protected ArrayList<Attribute> creatorAttributes, contributorAttributes, publisherAttributes;
    protected ArrayList<Variable> documentVariables;
    protected final String procedure;
    
    /**
     * 
     * @param dataset
     * @param procedure
     */
    public SOSDescribeStation( NetcdfDataset dataset, String procedure ) {
        Variable lat, lon;
        lat = lon = null;
        // get desired variables
        for (Variable var : dataset.getVariables()) {
            if (var.getFullName().matches("(station)[_]*(name)")) {
                stationVariable = var;
            }
            else if (var.getFullName().toLowerCase().contains("lat")) {
                lat = var;
            }
            else if (var.getFullName().toLowerCase().contains("lon")) {
                lon = var;
            }
            else if (var.getFullName().toLowerCase().contains("doc")) {
                if (documentVariables == null)
                    documentVariables = new ArrayList<Variable>();
                documentVariables.add(var);
            }
        }
        
        this.procedure = procedure;
        
        String[] procSplit = procedure.split(":");
        stationName = procSplit[procSplit.length - 1];
        
        // get our platform type
        platformType = dataset.findGlobalAttributeIgnoreCase("platformtype");
        // history attribute
        historyAttribute = dataset.findGlobalAttributeIgnoreCase("history");
        // creator contact info
        for (Attribute attr : dataset.getGlobalAttributes()) {
            String attrName = attr.getName().toLowerCase();
            if (attrName.contains("creator") || attrName.contains("institution")) {
                if (creatorAttributes == null)
                    creatorAttributes = new ArrayList<Attribute>();
                creatorAttributes.add(attr);
            }
            else if (attrName.contains("publisher")) {
                if (publisherAttributes == null)
                    publisherAttributes = new ArrayList<Attribute>();
                publisherAttributes.add(attr);
            }
            else if (attrName.contains("contributor")) {
                if (contributorAttributes == null)
                    contributorAttributes = new ArrayList<Attribute>();
                contributorAttributes.add(attr);
            }
        }
        
        // set our coords
        if (stationVariable != null) {
            stationCoords = getStationCoords(lat, lon);
        }
        
        // description
        description = dataset.findAttValueIgnoreCase(null, "description", "no description");

    }

    /*********************
     * Interface Methods *
     **************************************************************************/
    
    public void setupOutputDocument(DescribeSensorFormatter output) {
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
    }
    
    /**************************************************************************/
    
    /*****************************
     * Private/Protected Methods *
     *****************************/

    /**
     * 
     * @param output 
     */
    protected void formatSetClassification(DescribeSensorFormatter output) {
        if (platformType != null) {
            output.addToClassificationNode(platformType.getName(), "", platformType.getStringValue());
        } else {
            output.deleteClassificationNode();
        }
    }

    /**
     * 
     * @param output
     */
    protected void formatSetContactNodes(DescribeSensorFormatter output) {
        if (creatorAttributes != null) {
            HashMap<String, HashMap<String, String>> domainContactInfo = new HashMap<String, HashMap<String, String>>();
            HashMap<String, String> subDomainInfo;
            String role, orginizationName = "";
            role = "creator";
            for (Attribute attr : creatorAttributes) {
                String attrName = attr.getName().toLowerCase();
                if (attrName.contains("institution") || attrName.contains("name")) {
                    orginizationName = attr.getStringValue();
                }
                else if (attrName.contains("url") || attrName.contains("email")) {
                    if (domainContactInfo.containsKey("electronicAddress")) {
                        subDomainInfo = (HashMap<String,String>)domainContactInfo.get("electronicAddress");
                    } else {
                        subDomainInfo = new HashMap<String, String>();
                    }
                    subDomainInfo.put(attr.getName(), attr.getStringValue());
                    domainContactInfo.put("electronicAddress",subDomainInfo);
                }
            }
            output.addContactNode(role, orginizationName, domainContactInfo);
        }
        if (publisherAttributes != null) {
            HashMap<String, HashMap<String, String>> domainContactInfo = new HashMap<String, HashMap<String, String>>();
            HashMap<String, String> subDomainInfo;
            String role, orginizationName = "";
            role = "publisher";
            for (Attribute attr : publisherAttributes) {
                String attrName = attr.getName().toLowerCase();
                if (attrName.contains("name")) {
                    orginizationName = attr.getStringValue();
                }
                else if (attrName.contains("url") || attrName.contains("email")) {
                    if (domainContactInfo.containsKey("electronicAddress")) {
                        subDomainInfo = (HashMap<String,String>)domainContactInfo.get("electronicAddress");
                    } else {
                        subDomainInfo = new HashMap<String, String>();
                    }
                    subDomainInfo.put(attr.getName(), attr.getStringValue());
                    domainContactInfo.put("electronicAddress",subDomainInfo);
                }
            }
            output.addContactNode(role, orginizationName, domainContactInfo);
        }
        if (contributorAttributes != null) {
            String role = "", name = "";
            for (Attribute attr : contributorAttributes) {
                if (attr.getName().toLowerCase().contains("role")) {
                    role = attr.getStringValue();
                }
                else if (attr.getName().toLowerCase().contains("name")) {
                    name = attr.getStringValue();
                }
            }
            output.addContactNode(role, name, null);
        }
    }
    
    /**
     * 
     * @param formatter
     */
    protected void formatSetIdentification(DescribeSensorFormatter formatter) {
        ArrayList<String> identNames = new ArrayList<String>();
        ArrayList<String> identDefinitions = new ArrayList<String>();
        ArrayList<String> identValues = new ArrayList<String>();
        identNames.add("StationId"); identDefinitions.add("stationID"); identValues.add(procedure);
        for (Attribute attr : stationVariable.getAttributes()) {
            identNames.add(attr.getName()); identDefinitions.add(""); identValues.add(attr.getStringValue());
        }
        formatter.setIdentificationNode(identNames.toArray(new String[identNames.size()]),
                identDefinitions.toArray(new String[identDefinitions.size()]),
                identValues.toArray(new String[identValues.size()]));
    }
    
    /**
     * 
     * @param stationVar
     * @param nameOfStation
     * @return
     */
    protected int getStationIndex(Variable stationVar, String nameOfStation) {
        int retval = -1;
        try {
            // get the station index in the array
            char[] charArray = (char[]) stationVar.read().get1DJavaArray(char.class);
            // find the length of the strings, assumes that the array has only a rank of 2; string length should be the 1st index
            int[] aShape = stationVar.read().getShape();
            String[] names = new String[aShape[0]];
            StringBuilder strB = null;
            int ni = 0;
            for (int i=0;i<charArray.length;i++) {
                if(i % aShape[1] == 0) {
                    if (strB != null)
                        names[ni++] = strB.toString();
                    strB = new StringBuilder();
                }
                // ignore null
                if (charArray[i] != '\u0000')
                    strB.append(charArray[i]);
            }
            // now find our station index
            for (int j=0; j<names.length; j++) {
                if(names[j].equalsIgnoreCase(nameOfStation)) {
                    retval = j;
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Received error looking for station " + nameOfStation + " - " + ex.getMessage());
        }
        
        return retval;
    }
    
    /**
     * 
     * @param lat
     * @param lon
     * @return
     */
    protected double[] getStationCoords(Variable lat, Variable lon) {
        try {
            // get the lat/lon of the station
            // station id should be the last value in the procedure
            int stationIndex = getStationIndex(stationVariable, stationName);
            
            // find lat/lon values for the station
            double[] coords = new double[] { Double.NaN, Double.NaN };
            coords[0] = lat.read().getDouble(stationIndex);
            coords[1] = lon.read().getDouble(stationIndex);

            return coords;
        } catch (Exception e) {
            System.out.println("exception in getStationCoords " + e.getMessage());
            return null;
        }
    }

    /**
     * 
     * @param output
     */
    protected void formatSetDescription(DescribeSensorFormatter output) {
        output.setDescriptionNode(description);
    }

    /**
     * 
     * @param output
     */
    protected void formatSetHistoryNodes(DescribeSensorFormatter output) {
        if (historyAttribute != null) {
            output.setHistoryEvents(historyAttribute.getStringValue());
        } else {
            output.deleteHistoryNode();
        }
    }

    /**
     * 
     * @param output
     */
    protected void formatSetLocationNode(DescribeSensorFormatter output) {
        if (stationCoords != null)
            output.setLocationNode(stationName, stationCoords);
    }
    
    private void removeUnusedNodes(DescribeSensorFormatter output) {
        output.deletePosition();
        output.deleteTimePosition();
        output.deletePositions();
    }
}
