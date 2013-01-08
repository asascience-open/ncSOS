/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeNetworkFormatter;
import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import com.asascience.ncsos.util.DatasetHandlerAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.w3c.dom.Element;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.TrajectoryFeature;
import ucar.nc2.ft.TrajectoryFeatureCollection;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarPeriod;
import ucar.nc2.units.DateFormatter;

/**
 * Handles Describe Sensor requests for Trajectory feature datasets.
 * Describe Sensor requests to Trajectory datasets for response format "sensorML/1.0.1"
 * output the following xml subroots:
 * *Description
 * *Identification
 * *Classification
 * *Contact(s)
 * *History
 * *Position
 * *Component(s)
 * @author SCowan
 * @version 1.0.0
 */
public class SOSDescribeTrajectory extends SOSDescribeStation implements SOSDescribeIF {
    
    private Variable indexDescriptor;
    private int trajectoryIndex;
    private int trajectoryLowerObsIndex, trajectoryUpperObsIndex;
    private Integer[] trajectoryObsIndices;
    private CalendarDate startDate;
    
    /**
     * Creates a new instance that collects, from the dataset, information needed
     * for a Describe Sensor response.
     * @param dataset a netcdf dataset of a Trajectory feature type
     * @param procedure request procedure (station urn)
     * @param startDate start date of the dataset (elapsed time is 0)
     */
    public SOSDescribeTrajectory( NetcdfDataset dataset, String procedure ) throws IOException {
        super(dataset, procedure);
        // ignore errors from parent constructor
        errorString = null;
        
        
        for (Variable var : dataset.getVariables()) {
            String varName = var.getFullName().toLowerCase();
            // look for our var for either index or rowSize vars
            if (varName.contains("rowsize") || varName.contains("index")) {
                indexDescriptor = var;
            }
        }
        
        this.startDate = getStartDate();
        
        if (getStationIndex(stationName) < 0) {
            errorString = "Station not part of dataset: " + stationName;
        }
        
        // get our observation indices
        setCurrentTrajectoryInfo(getStationIndex(stationName));
    }
    
    public SOSDescribeTrajectory( NetcdfDataset dataset ) throws IOException {
        super(dataset);
        // ignore errors from parent constructor
        errorString = null;
        
        for (Variable var : dataset.getVariables()) {
            String varName = var.getFullName().toLowerCase();
            // look for our var for either index or rowSize vars
            if (varName.contains("rowsize") || varName.contains("index")) {
                indexDescriptor = var;
            }
        }
        
        this.startDate = getStartDate();
    }

    /*********************/
    /* Interface Methods */
    /**************************************************************************/
    
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
            // location
            formatSetLocation(output);
            // remove unwanted nodes
//            removeUnusedNodes(output);
        } else {
            output.setupExceptionOutput(errorString);
        }
    }
    
//    @Override
//    public void setupOutputDocument(DescribeNetworkFormatter output) {
//        if (errorString == null) {
//            // system node
//            output.setNetworkSystemId("network-all");
//            // set network ident
//            formatSetNetworkIdentification(output);
//            // classification
//            formatSetClassification(output);
//            // history
//            formatSetHistoryNodes(output);
//        } else {
//            output.setupExceptionOutput(errorString);
//        }
//    }
    
    /**************************************************************************/
    
    @Override
    protected void formatSetStationComponentList(DescribeNetworkFormatter output) {
        // iterate through each station name, add a station to the component list and setup each station
        for (String stName : getStationNames().values()) {
            int stIndex = getStationIndex(stName);
            // add a new station
            Element stNode = output.addNewStationWithId("station-"+stName);
            // set a description for each station - TODO
            output.removeStationDescriptionNode(stNode);
            // set identification for station
            formatSetStationIdentification(output, stNode, stName);
            // set location info
            setCurrentTrajectoryInfo(stIndex);
            formatSetTrajectoryLocation(output, stNode);
            // remove unwanted nodes for each station
//            output.removeStationLocationNode(stNode);
//            output.removeStationPositions(stNode);
//            output.removeStationTimePosition(stNode);
        }
    }
    
    /*******************
     * Private Methods *
     *******************/
    
    private CalendarDate getStartDate() {
        try {
            // need our starting date for the observations from our FeatureTypeDataSet wrapper
            TrajectoryFeatureCollection feature = (TrajectoryFeatureCollection) getFeatureTypeDataSet();
            CalendarDate retval = null;
            for (feature.resetIteration();feature.hasNext();) {
                TrajectoryFeature traj = feature.next();
                DatasetHandlerAdapter.calcBounds(traj);
                for (traj.resetIteration();traj.hasNext();) {
                    PointFeature pf = traj.next();
                    if (retval == null)
                        retval = pf.getObservationTimeAsCalendarDate();
                    else if (pf.getObservationTimeAsCalendarDate().compareTo(retval) < 0)
                        retval = pf.getObservationTimeAsCalendarDate();
                }
            }
            return retval;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    private void setCurrentTrajectoryInfo(int trajNumber) {
        trajectoryIndex = trajNumber;
        trajectoryLowerObsIndex = trajectoryUpperObsIndex = -1;
        trajectoryObsIndices = null;
        // get our observation indices
        if (indexDescriptor != null) {
            if (indexDescriptor.getFullName().toLowerCase().contains("rowsize")) {
                // contiguous ragged
                trajectoryLowerObsIndex = 0;
                trajectoryUpperObsIndex = 0;
                try {
                    // our obs is contiguous, need to find where it starts and ends
                    Array rowArray = indexDescriptor.read();
                    for (int i=0; i<rowArray.getSize(); i++) {
                        if (i < trajNumber) {
                            trajectoryLowerObsIndex += rowArray.getInt(i);
                        } else if (i == trajNumber) {
                            trajectoryUpperObsIndex = trajectoryLowerObsIndex + rowArray.getInt(i);
                            break;
                        }
                    }
                } catch (IOException ex) {
                    System.out.println("exception " + ex.getMessage());
                }
            } else {
                try {
                    // our observation is not contiguous (indexed ragged), so we need to get the indices of each observation that coresponds to our station
                    Array indexArray;
                    indexArray = indexDescriptor.read();
                    ArrayList<Integer> indexBuilder = new ArrayList<Integer>();
                    for (int i=0; i<indexArray.getSize(); i++) {
                        if (indexArray.getInt(i) == trajNumber) {
                            indexBuilder.add(i);
                        }
                    }
                    trajectoryObsIndices = indexBuilder.toArray(new Integer[indexBuilder.size()]);
                } catch (IOException ex) {
                    System.out.println("exception " + ex.getMessage());
                }
            }
        } else {
            // trajectory has multiple/single dimension(s)
            
        }
    }
    
    private void formatSetTrajectoryLocation(DescribeNetworkFormatter output, Element stNode) {
        output.setStationLocationNode2Dimension(stNode, stationName, getCoordinatesForLocationNode());
    }
    
    private void formatSetLocation(DescribeSensorFormatter output) {
        if (getCoordinateNames() == null || getCoordinateNames().size() < 1) {
            output.setLocationNode2Dimension(stationName, getCoordinatesForLocationNode());
        }
        else {
            output.setLocationNode2Dimension(stationName, getCoordinatesForLocationNode(), getCoordinateNames().get(0));
        }
    }
    
    private double[][] getCoordinatesForLocationNode() {
        double[][] coords = new double[0][0];
        try {
            // get the coords for each point in the trajectory
            Array latArray = latVariable.read();
            Array lonArray = lonVariable.read();

            if (trajectoryObsIndices != null) {
                // indexed ragged
                coords = new double[trajectoryObsIndices.length][2];
                for (int i=0; i<trajectoryObsIndices.length; i++) {
                    int obsIndex = trajectoryObsIndices[i].intValue();
                    coords[i][0] = latArray.getDouble(obsIndex);
                    coords[i][1] = lonArray.getDouble(obsIndex);
                }
            } else if (trajectoryLowerObsIndex >= 0 && trajectoryUpperObsIndex >= trajectoryLowerObsIndex) {
                // contiguous ragged
                coords = new double[trajectoryUpperObsIndex-trajectoryLowerObsIndex][2];
                for (int j=trajectoryLowerObsIndex; j<trajectoryUpperObsIndex; j++) {
                    int coordIndex = j - trajectoryLowerObsIndex;
                    coords[coordIndex][0] = latArray.getDouble(j);
                    coords[coordIndex][1] = lonArray.getDouble(j);
                }
            } else if (latVariable.getShape().length > 1) {
                // multiple dimensions (trajectories)
                int numberOfObs = latVariable.getShape(1);
                coords = new double[numberOfObs][2];
                double[] latVals = (double[]) latArray.section(new int[] { trajectoryIndex, 0 }, new int[] { 1, numberOfObs }).get1DJavaArray(double.class);
                double[] lonVals = (double[]) lonArray.section(new int[] { trajectoryIndex, 0 }, new int[] { 1, numberOfObs }).get1DJavaArray(double.class);
                for (int i=0; i<numberOfObs; i++) {
                    coords[i][0] = latVals[i];
                    coords[i][1] = lonVals[i];
                }
            } else {
                // single dimension (trajectory)
                int numberOfObs = latVariable.getShape(0);
                coords = new double[numberOfObs][2];
                double[] latVals = (double[]) latArray.get1DJavaArray(double.class);
                double[] lonVals = (double[]) lonArray.get1DJavaArray(double.class);
                for (int i=0; i<numberOfObs; i++) {
                    coords[i][0] = latVals[i];
                    coords[i][1] = lonVals[i];
                }
            }
        } catch (Exception ex) {
            System.out.println("formatSetLocation - " + ex.toString() + "\n\t" + ex.getStackTrace()[0].toString());
        }
        
        return coords;
    }
    
    private void removeUnusedNodes(DescribeSensorFormatter output) {
        output.deleteLocationNode();
        output.deletePositions();
        // time position requires data not obtained here
        output.deleteTimePosition();
    }

    private void formatSetPositionNode(DescribeSensorFormatter output) {
        // set our position name
        output.setPositionName("stationTrajectory");
        // set our data definition
        // first get our contentmap from our vars
        HashMap<String, String> varMap;
        HashMap<String, HashMap<String, String>> mapMap = new LinkedHashMap<String, HashMap<String, String>>();
        // add time, lat and lon in that order
        // time
        varMap = new HashMap<String, String>();
        varMap.put("definition", "OGC:time");
        varMap.put("xlink:href", "ISO####:date");
        mapMap.put("time", varMap);
        // lat
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", "deg");
        mapMap.put("lat", varMap);
        // lon
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", "deg");
        mapMap.put("lon", varMap);
        
        output.setPositionDataDefinition(mapMap, ".", " ", ",");
        
        // now we need our values
        StringBuilder strB = new StringBuilder();
        try {
            DateFormatter formatter = new DateFormatter();
            Array timeArray = timeVariable.read();
            Array latArray = latVariable.read();
            Array lonArray = lonVariable.read();
            
            if (trajectoryObsIndices != null) {
                for (int i=0; i<trajectoryObsIndices.length; i++) {
                    int obsIndex = trajectoryObsIndices[i].intValue();
                    strB.append(formatter.toDateTimeStringISO(startDate.add(timeArray.getDouble(obsIndex), CalendarPeriod.Field.Second).toDate())).append(",");
                    strB.append(latArray.getDouble(obsIndex)).append(",");
                    strB.append(lonArray.getDouble(obsIndex)).append(" ");
                }
            } else {
                for (int j=trajectoryLowerObsIndex; j<trajectoryUpperObsIndex; j++) {
                    strB.append(formatter.toDateTimeStringISO(startDate.add(timeArray.getDouble(j), CalendarPeriod.Field.Second).toDate())).append(",");
                    strB.append(latArray.getDouble(j)).append(",");
                    strB.append(lonArray.getDouble(j)).append(" ");
                }
            }
        } catch (IOException ex) {
            System.out.println("Exception " + ex.getMessage());
        } 
        
        // print out our values
        output.setPositionValue(strB.toString());
    }
    
    private void formatSetStationPositionNode(DescribeNetworkFormatter output, Element stationNode, int stIndex) {
        // set our position name
        output.setStationPositionName(stationNode, "stationTrajectory");
        // set our data definition
        // first get our contentmap from our vars
        HashMap<String, String> varMap;
        HashMap<String, HashMap<String, String>> mapMap = new LinkedHashMap<String, HashMap<String, String>>();
        // add time, lat and lon in that order
        // time
        varMap = new HashMap<String, String>();
        varMap.put("definition", "OGC:time");
        varMap.put("xlink:href", "ISO####:date");
        mapMap.put("time", varMap);
        // lat
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", "deg");
        mapMap.put("lat", varMap);
        // lon
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", "deg");
        mapMap.put("lon", varMap);
        
        output.setStationPositionDataDefinition(stationNode, mapMap, ".", " ", ",");
        
        // now we need our values
        StringBuilder strB = null;
        if (indexDescriptor != null)
            strB = setStationValueStringFromIndexDescriptor(stIndex);
        else
            strB = setStationValueStringFromStationIndex(stIndex);
        
        
        // print out our values
        output.setStationPositionValue(stationNode, strB.toString());
    }
    
    private StringBuilder setStationValueStringFromStationIndex(int stIndex) {
        StringBuilder strB = new StringBuilder();
        
        try {
            DateFormatter formatter = new DateFormatter();
            double NAN = -999.9;
            int timeShape = 0, latShape = 0, lonShape = 0;
            double[] timeVals, latVals, lonVals;
            int[] origin;
            
            if (timeVariable.getShape().length > 1) {
                timeShape = timeVariable.getShape(1);
                latShape = latVariable.getShape(1);
                lonShape = lonVariable.getShape(1);
                
                origin = new int[] { stIndex, 0 };
                timeVals = (double[]) timeVariable.read().section(origin, new int[] { 1, timeShape }).get1DJavaArray(double.class);
                latVals = (double[]) latVariable.read().section(origin, new int[] { 1, latShape }).get1DJavaArray(double.class);
                lonVals = (double[]) lonVariable.read().section(origin, new int[] { 1, lonShape }).get1DJavaArray(double.class);
            } else {
                timeShape = timeVariable.getShape(0);
                latShape = latVariable.getShape(0);
                lonShape = lonVariable.getShape(0);
                
                origin = new int[] { stIndex };
                timeVals = (double[]) timeVariable.read().section(origin, new int[] { timeShape }).get1DJavaArray(double.class);
                latVals = (double[]) latVariable.read().section(origin, new int[] { latShape }).get1DJavaArray(double.class);
                lonVals = (double[]) lonVariable.read().section(origin, new int[] { lonShape }).get1DJavaArray(double.class);
            }
            
            int maxLength = (timeVals.length < latVals.length) ? timeVals.length : latVals.length;
            maxLength = (maxLength > lonVals.length) ? lonVals.length : maxLength;
            
            for (int i=0; i<maxLength; i++) {
                if (timeVals[i] >= 0 && latVals[i] != NAN && lonVals[i] != NAN) {
                    strB.append(formatter.toDateTimeStringISO(startDate.add(timeVals[i], CalendarPeriod.Field.Second).toDate())).append(",");
                    strB.append(latVals[i]).append(",");
                    strB.append(lonVals[i]).append(" ");
                }
            }
            // iterate through the arrays, adding the values
        } catch (Exception ex) {
            System.out.println("setStationValueStringFromStationIndex - Exception " + ex.getMessage());
        }
        
        return strB;
    }
    
    private StringBuilder setStationValueStringFromIndexDescriptor(int stIndex) {
        StringBuilder strB = new StringBuilder();
        try {
            DateFormatter formatter = new DateFormatter();
            Array timeArray = timeVariable.read();
            Array latArray = latVariable.read();
            Array lonArray = lonVariable.read();
            
            Integer[] indices = readStationIndices(stIndex);
            
            if (indices.length != 3 || indices[0] != -1) {
                for (int i=0; i<indices.length; i++) {
                    int obsIndex = indices[i].intValue();
                    strB.append(formatter.toDateTimeStringISO(startDate.add(timeArray.getDouble(obsIndex), CalendarPeriod.Field.Second).toDate())).append(",");
                    strB.append(latArray.getDouble(obsIndex)).append(",");
                    strB.append(lonArray.getDouble(obsIndex)).append(" ");
                }
            } else if (indices.length > 2) {
                for (int j=indices[1]; j<indices[2]; j++) {
                    strB.append(formatter.toDateTimeStringISO(startDate.add(timeArray.getDouble(j), CalendarPeriod.Field.Second).toDate())).append(",");
                    strB.append(latArray.getDouble(j)).append(",");
                    strB.append(lonArray.getDouble(j)).append(" ");
                }
            }
            else {
                throw new ArrayStoreException("setStationValueStringFromIndexDescriptor - Unexpected index read from station " + stIndex);
            }
        } catch (IOException ex) {
            System.out.println("setStationValueStringFromIndexDescriptor - Exception " + ex.getMessage());
        } 
        return strB;
    }
    
    private Integer[] readStationIndices(int stIndex) {
        Integer[] retval = null;
        int lowerIndex = 0;
        int upperIndex = 0;
        // get our observation indices
        if (indexDescriptor.getFullName().toLowerCase().contains("rowsize")) {
            try {
                // our obs is contiguous, need to find where it starts and ends
                Array rowArray = indexDescriptor.read();
                for (int i=0; i<rowArray.getSize(); i++) {
                    if (i < stIndex) {
                        lowerIndex += rowArray.getInt(i);
                    } else if (i == stIndex) {
                        upperIndex = lowerIndex + rowArray.getInt(i);
                        break;
                    }
                }
                retval = new Integer[] { -1, lowerIndex, upperIndex };
            } catch (IOException ex) {
                System.out.println("readStationIndices - exception " + ex.getMessage());
            }
        } else {
            try {
                // our observation is not contiguous, so we need to get the indices of each observation that coresponds to our station
                Array indexArray;
                indexArray = indexDescriptor.read();
                ArrayList<Integer> indexBuilder = new ArrayList<Integer>();
                for (int i=0; i<indexArray.getSize(); i++) {
                    if (indexArray.getInt(i) == stIndex) {
                        indexBuilder.add(i);
                    }
                }
                retval = indexBuilder.toArray(new Integer[indexBuilder.size()]);
            } catch (IOException ex) {
                System.out.println("readStationIndices - exception " + ex.getMessage());
            }
        }
        
        return retval;
    }
    
}