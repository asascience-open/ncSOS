/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeNetworkFormatter;
import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import org.w3c.dom.Element;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Used to populate output for TimeSeries and TimeSeriesProfile feature sets. Is also used
 * as a parent class for most other feature types.
 * Describe Sensor requests to TimeSeries and TimeSeriesProfile datasets for
 * response format "sensorML/1.0.1" output the following xml subroots:
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
public class SOSDescribeStation extends SOSBaseRequestHandler implements SOSDescribeIF {
    
    protected Attribute platformType, historyAttribute;
    protected String stationName;
    protected String description;
    protected double[][] stationCoords;
    protected ArrayList<Attribute> contributorAttributes;
    protected ArrayList<Variable> documentVariables;
    protected final String procedure;
    protected String errorString;
    
    protected static final String MMI_DEF_URL = "http://mmisw.org/ont/ioos/definition/";
    
    /**
     * Creates an instance to collect needed information, from the dataset, for
     * a Describe Sensor response.
     * @param dataset netcdf dataset of feature type TimeSeries and TimeSeriesProfile
     * @param procedure procedure of the request (station urn)
     */
    public SOSDescribeStation( NetcdfDataset dataset, String procedure ) throws IOException {
        super(dataset);
        // get desired variables
        for (Variable var : dataset.getVariables()) {
            
//            if (var.getFullName().toLowerCase().contains("lat")) {
//                lat = var;
//            }
//            else if (var.getFullName().toLowerCase().contains("lon")) {
//                lon = var;
//            }
            if (var.getFullName().toLowerCase().contains("doc")) {
                if (documentVariables == null)
                    documentVariables = new ArrayList<Variable>();
                documentVariables.add(var);
            }
        }
        
        errorString = null;
        
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
            if (attrName.contains("contributor")) {
                if (contributorAttributes == null)
                    contributorAttributes = new ArrayList<Attribute>();
                contributorAttributes.add(attr);
            }
        }
        
        // set our coords
        if (stationVariable != null) {
            stationCoords = getStationCoords(latVariable, lonVariable);
            if (stationCoords == null || stationCoords.length < 1)
                errorString = "Could not find station " + stationName + " in dataset";
        } else {
            errorString = "Could not find a variable containing station info.";
        }
        
        // description
        description = dataset.findAttValueIgnoreCase(null, "description", "no description");

    }
    
    /**
     * Creates an instance to collect needed information, from the dataset, for
     * a Describe Sensor 'network-all' response.
     * @param dataset netcdf dataset of feature type TimeSeries
     */
    public SOSDescribeStation( NetcdfDataset dataset ) throws IOException {
        super(dataset);
        
        this.procedure = "";
        
        // get desired variables
        for (Variable var : dataset.getVariables()) {
            if (var.getFullName().toLowerCase().contains("doc")) {
                if (documentVariables == null)
                    documentVariables = new ArrayList<Variable>();
                documentVariables.add(var);
            }
        }
        
        errorString = null;
        
        // get our platform type
        platformType = dataset.findGlobalAttributeIgnoreCase("platformtype");
        // history attribute
        historyAttribute = dataset.findGlobalAttributeIgnoreCase("history");
        // creator contact info
        for (Attribute attr : dataset.getGlobalAttributes()) {
            String attrName = attr.getName().toLowerCase();
            if (attrName.contains("contributor")) {
                if (contributorAttributes == null)
                    contributorAttributes = new ArrayList<Attribute>();
                contributorAttributes.add(attr);
            }
        }
        
        // description
        description = dataset.findAttValueIgnoreCase(null, "description", "no description");
    }

    /*********************/
    /* Interface Methods */
    /**************************************************************************/
    
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
    
    public void setupOutputDocument(DescribeNetworkFormatter output) {
        if (errorString == null) {
            // system node
            output.setNetworkSystemId("network-all");
            // network identification
            formatSetNetworkIdentification(output);
            // classification
            formatSetClassification(output);
            // set history
            formatSetHistoryNodes(output);
            // set components
            formatSetStationComponentList(output);
        } else {
            output.setupExceptionOutput(errorString);
        }
    }
    
    /**************************************************************************/
    
    /*****************************
     * Private/Protected Methods *
     *****************************/
    
    /*****************************
     * Network-All Formatters    *
     *****************************/
    
    protected void formatSetStationComponentList(DescribeNetworkFormatter output) {
        // iterate through each station name, add a station to the component list and setup each station
        for (String stName : getStationNames().values()) {
            System.out.println("Setting station: " + stName);
            int stIndex = getStationIndex(stName);
            // add a new station
            Element stNode = output.addNewStationWithId("station-"+stName);
            // set a description for each station - TODO
            output.removeStationDescriptionNode(stNode);
            // set identification for station
            formatSetStationIdentification(output, stNode, stName);
            // set location for station
            double[][] coords = getStationCoords(latVariable, lonVariable, stIndex);
            if (coords.length > 1)
                output.setOrderedStationLocationNode3Dimension(stNode, stationName, coords);
            else
                output.setStationLocationNode2Dimension(stNode, stationName, coords);
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
    protected void formatSetClassification(DescribeNetworkFormatter output) {
        if (platformType != null) {
            output.addToNetworkClassificationNode(platformType.getName(), "", platformType.getStringValue());
        } else {
            output.removeNetworkClassificationNode();
        }
    }
    
    /**
     * 
     * @param output 
     */
    protected void formatSetDescription(DescribeNetworkFormatter output) {
        output.setNetworkDescriptionNode(description);
    }
    
    /**
     * 
     * @param output 
     */
    protected void formatSetContactNodes(DescribeNetworkFormatter output) {
        if (!InventoryContactName.equalsIgnoreCase("") || !InventoryContactEmail.equalsIgnoreCase("") || !InventoryContactPhone.equalsIgnoreCase("")) {
            String role = "http://mmisw.org/ont/ioos/definition/operator";
            HashMap<String, HashMap<String, String>> domainContactInfo = new HashMap<String, HashMap<String, String>>();
            HashMap<String, String> address = new HashMap<String, String>();
            address.put("sml:electronicMailAddress", InventoryContactEmail);
            domainContactInfo.put("sml:address", address);
            HashMap<String, String> phone = new HashMap<String, String>();
            phone.put("sml:voice", InventoryContactPhone);
            domainContactInfo.put("sml:phone", phone);
            output.addContactNode(role, InventoryContactName, domainContactInfo);
        }
        if (!DataContactName.equalsIgnoreCase("") || !DataContactEmail.equalsIgnoreCase("") || !DataContactPhone.equalsIgnoreCase("")) {
            String role = "http://mmisw.org/ont/ioos/definition/publisher";
            HashMap<String, HashMap<String, String>> domainContactInfo = new HashMap<String, HashMap<String, String>>();
            HashMap<String, String> address = new HashMap<String, String>();
            address.put("sml:electronicMailAddress", DataContactEmail);
            domainContactInfo.put("sml:address", address);
            HashMap<String, String> phone = new HashMap<String, String>();
            phone.put("sml:voice", DataContactPhone);
            domainContactInfo.put("sml:phone", phone);
            output.addContactNode(role, InventoryContactName, domainContactInfo);
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
    protected void formatSetNetworkIdentification(DescribeNetworkFormatter formatter) {
        formatter.setNetworkIdentificationNode();
    }
    
    /**
     * 
     * @param output 
     */
    protected void formatSetHistoryNodes(DescribeNetworkFormatter output) {
        if (historyAttribute != null) {
            output.setHistoryEvents(historyAttribute.getStringValue());
        } else {
            output.deleteHistoryNode();
        }
    }
    
    /**
     * 
     * @param formatter 
     */
    protected void formatSetStationIdentification(DescribeNetworkFormatter formatter, Element stationNode, String stationName) {
        ArrayList<String> identNames = new ArrayList<String>();
        ArrayList<String> identDefinitions = new ArrayList<String>();
        ArrayList<String> identValues = new ArrayList<String>();
        identNames.add("StationId"); identDefinitions.add("stationID"); identValues.add(SOSBaseRequestHandler.getGMLName(stationName));
        for (Attribute attr : getStationAttributes()) {
            if (attr.getName().equalsIgnoreCase("cf_role") || attr.getName().toLowerCase().contains("hdf5"))
                continue;
            identNames.add(attr.getName()); identDefinitions.add(""); identValues.add(attr.getStringValue());
        }
        formatter.setStationIdentificationNode(stationNode, identNames.toArray(new String[identNames.size()]),
                identDefinitions.toArray(new String[identDefinitions.size()]),
                identValues.toArray(new String[identValues.size()]));
    }
    
    /*****************************
     * Single Station Formatters *
     *****************************/

    /**
     * Reads dataset for platform information (usually just platform type) for the
     * output. If no info is found, informs output to delete the Classification
     * root node.
     * @param output a DescribeSensorFormatter instance (held by the handler)
     */
    protected void formatSetClassification(DescribeSensorFormatter output) {
        if (platformType != null) {
            output.addToClassificationNode(platformType.getName(), "", platformType.getStringValue());
        } else {
            output.deleteClassificationNode();
        }
    }

    /**
     * Reads the dataset for contact information and passes along to output.
     * @param output a DescribeSensorFormatter instance (held by the handler)
     */
    protected void formatSetContactNodes(DescribeSensorFormatter output) {
        if (!InventoryContactName.equalsIgnoreCase("") || !InventoryContactEmail.equalsIgnoreCase("") || !InventoryContactPhone.equalsIgnoreCase("")) {
            String role = "http://mmisw.org/ont/ioos/definition/operator";
            HashMap<String, HashMap<String, String>> domainContactInfo = new HashMap<String, HashMap<String, String>>();
            HashMap<String, String> address = new HashMap<String, String>();
            address.put("sml:electronicMailAddress", InventoryContactEmail);
            domainContactInfo.put("sml:address", address);
            HashMap<String, String> phone = new HashMap<String, String>();
            phone.put("sml:voice", InventoryContactPhone);
            domainContactInfo.put("sml:phone", phone);
            output.addContactNode(role, InventoryContactName, domainContactInfo);
        }
        if (!DataContactName.equalsIgnoreCase("") || !DataContactEmail.equalsIgnoreCase("") || !DataContactPhone.equalsIgnoreCase("")) {
            String role = "http://mmisw.org/ont/ioos/definition/publisher";
            HashMap<String, HashMap<String, String>> domainContactInfo = new HashMap<String, HashMap<String, String>>();
            HashMap<String, String> address = new HashMap<String, String>();
            address.put("sml:electronicMailAddress", DataContactEmail);
            domainContactInfo.put("sml:address", address);
            HashMap<String, String> phone = new HashMap<String, String>();
            phone.put("sml:voice", DataContactPhone);
            domainContactInfo.put("sml:phone", phone);
            output.addContactNode(role, InventoryContactName, domainContactInfo);
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
     * Reads the dataset for station Attributes and sends gathered info to formatter.
     * @param formatter a DescribeSensorFormatter instance (held by the handler)
     */
    protected void formatSetIdentification(DescribeSensorFormatter formatter) {
        ArrayList<String> identNames = new ArrayList<String>();
        ArrayList<String> identDefinitions = new ArrayList<String>();
        ArrayList<String> identValues = new ArrayList<String>();
        identNames.add("StationId"); identDefinitions.add(MMI_DEF_URL + "stationID"); identValues.add(procedure);
        for (Attribute attr : getStationAttributes()) {
            if (attr.getName().equalsIgnoreCase("cf_role") || attr.getName().toLowerCase().contains("hdf5"))
                continue;
            identNames.add(attr.getName()); identDefinitions.add(MMI_DEF_URL + attr.getName()); identValues.add(attr.getValue(0).toString());
        }
        formatter.setIdentificationNode(identNames.toArray(new String[identNames.size()]),
                identDefinitions.toArray(new String[identDefinitions.size()]),
                identValues.toArray(new String[identValues.size()]));
    }
    
    
    protected double[][] getStationCoords(Variable lat, Variable lon) {
        return getStationCoords(lat, lon, getStationIndex(stationName));
    }
    
    /**
     * finds the latitude and longitude of the station defined by fields
     * stationVariable and stationName
     * @param lat latitude Variable from the dataset
     * @param lon longitude Variabe from the dataset
     * @return an array of the latitude and longitude pair
     */
    private double[][] getStationCoords(Variable lat, Variable lon, int stationIndex) {
        try {
            if (stationIndex >= 0 && depthVariable == null || depthVariable.getRank() < 1) {
                Double[] coords = new Double[] { Double.NaN, Double.NaN };
            
                // find lat/lon values for the station
                coords[0] = lat.read().getDouble(stationIndex);
                coords[1] = lon.read().getDouble(stationIndex);

                return new double[][] { { coords[0], coords[1], 0 } };
            } else if (stationIndex >= 0 && depthVariable.getRank() == 3) {
                // rank 1 is time index, rank 2 is depth index
                int depth = depthVariable.getShape(2) * depthVariable.getShape(1);
                double[][] retval = new double[depth][3];
                double dlat = lat.read().getDouble(stationIndex);
                double dlon = lon.read().getDouble(stationIndex);
                // initialize depths to NaN
                for (int j=0; j<depth; j++) {
                    retval[j][2] = Double.NaN;
                }
                Array depthArray = depthVariable.read().section(new int[] { stationIndex, 0, 0 }, new int[] { 1, depthVariable.getShape(1), depthVariable.getShape(2) });
                depthArray.reduce();
                // add each depth to the retval
                int currentDepthIndex = 0;
                for (int o=0; o<depthVariable.getShape(1); o++) {
                    for (int i=0; i<depthVariable.getShape(2); i++) {
                        retval[currentDepthIndex][0] = dlat;
                        retval[currentDepthIndex][1] = dlon;
                        retval[currentDepthIndex][2] = depthArray.getDouble(currentDepthIndex);
                        currentDepthIndex++;
                    }
                }
                return retval;
            } else if (stationIndex >=0 && depthVariable.getRank() == 2) {
                int depth = depthVariable.getShape(1);
                double[][] retval = new double[depth][3];
                double dlat = lat.read().getDouble(stationIndex);
                double dlon = lon.read().getDouble(stationIndex);
                // initialize depths to NaN
                for (int j=0; j<depth; j++) {
                    retval[j][2] = Double.NaN;
                }
                Array depthArray = depthVariable.read().section(new int[] { stationIndex, 0 }, new int[] { 1, depth });
                depthArray.reduce();
                // add each depth to the retval
                for (int i=0; i<depth; i++) {
                    retval[i][0] = dlat;
                    retval[i][1] = dlon;
                    retval[i][2] = depthArray.getDouble(i);
                }
                return retval;
            } else if (stationIndex >= 0 && depthVariable.getRank() == 1) {
                int depth = depthVariable.getShape(0);
                double[][] retval = new double[depth][3];
                double dlat = lat.read().getDouble(stationIndex);
                double dlon = lon.read().getDouble(stationIndex);
                // initialize depths to NaN
                for (int j=0; j<depth; j++) {
                    retval[j][2] = Double.NaN;
                }
                Array depthArray = depthVariable.read();
                // add each depth to retval
                for (int i=0; i<depth; i++) {
                    retval[i][0] = dlat;
                    retval[i][1] = dlon;
                    retval[i][2] = depthArray.getDouble(i);
                }
                return retval;
            } else {
                System.err.println("Cannot get station coords! index: " + stationIndex + "; depth variable's rank is " + depthVariable.getRank());
                System.err.println("Depth var name: " + depthVariable.getFullName());
                System.err.println("Depth shape: ");
                for (int i=0; i<depthVariable.getShape().length;i++) {
                    System.err.print(depthVariable.getShape(i) + " | ");
                }
                return null;
            }
        } catch (Exception e) {
            System.out.println("exception in getStationCoords " + e.getMessage());
            return null;
        }
    }

    /**
     * Gives output the value of the description global Attribute
     * @param output a DescribeSensorFormatter instance (held by the handler)
     */
    protected void formatSetDescription(DescribeSensorFormatter output) {
        output.setDescriptionNode(description);
    }

    /**
     * Gives output the value of the history global Attribute, or tells output
     * to delete the History root node if there is no Attribute
     * @param output a DescribeSensorFormatter instance (held by the handler)
     */
    protected void formatSetHistoryNodes(DescribeSensorFormatter output) {
        if (historyAttribute != null) {
            output.setHistoryEvents(historyAttribute.getStringValue());
        } else {
            output.deleteHistoryNode();
        }
    }

    /**
     * Gives output the station name and coordinates for the Location root node.
     * @param output a DescribeSensorFormatter instance (held by the handler)
     */
    protected void formatSetLocationNode(DescribeSensorFormatter output) {
        if (stationCoords != null) {
            if (stationCoords.length > 1) {
                if (getCRSSRSAuthorities() != null)
                    output.setOrderedLocationNode3Dimension(stationName, stationCoords, getCRSSRSAuthorities()[0]);
                else
                    output.setOrderedLocationNode3Dimension(stationName, stationCoords);
            } else {
                if (getCRSSRSAuthorities() != null)
                    output.setLocationNode2Dimension(stationName, stationCoords, getCRSSRSAuthorities()[0]);
                else
                    output.setLocationNode2Dimension(stationName, stationCoords);
            }
        }
    }
    
    private void removeUnusedNodes(DescribeSensorFormatter output) {
        output.deletePosition();
        output.deleteTimePosition();
        output.deletePositions();
    }
}
