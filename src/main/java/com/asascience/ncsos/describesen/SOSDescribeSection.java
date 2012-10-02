/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarPeriod;
import ucar.nc2.units.DateFormatter;

/**
 * Handles Describe Sensor requests for Section (Trajectory Profiles) feature datasets.
 * Describe Sensor requests to Grid datasets for response format "sensorML/1.0.1"
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
public class SOSDescribeSection extends SOSDescribeStation implements SOSDescribeIF {
    
    private int trajectoryNumber;
//    private Integer[] profileIndices;
    private Integer[] profileDataStartIndex, profileDataEndIndex;
    private Variable lat, lon, depth, time;
    private Variable indexVar;
    private CalendarDate[] startDates;
    
    /**
     * Creates an instance to collect information, from the dataset, needed for a
     * Describe Sensor response.
     * @param dataset netcdf dataset with the Section (TrajectoryProfile) feature type
     * @param procedure the request procedure (station urn)
     * @param sDate starting date of the dataset (value of when time elapsed is 0)
     */
    public SOSDescribeSection( NetcdfDataset dataset, String procedure, CalendarDate[] sDate ) throws IOException {
        super(dataset, procedure);
        // ignore errors from parent constructor
        errorString = null;
        
        // get our profile number
        String tStr = stationName.toLowerCase().replaceAll("(profile)", "");
        tStr = tStr.replaceAll("(trajectory)", "");
        trajectoryNumber = Integer.parseInt(tStr);
        
        profileDataStartIndex = profileDataEndIndex = null;
        
        lat = lon = depth = time = null;
        
        this.startDates = sDate;
        
        Variable rowsize = null;
        
        for (Variable var : dataset.getVariables()) {
            String varName = var.getFullName().toLowerCase();
            if (varName.contains("rowsize")) {
                rowsize = var;
            }
            else if (varName.contains("index")) {
                indexVar = var;
            }
            // look for lat, lon, depth
            else if (varName.contains("lat")) {
                lat = var;
            }
            else if (varName.contains("lon")) {
                lon = var;
            }
            else if (varName.contains("z") || varName.contains("alt")) {
                depth = var;
            }
            else if (varName.contains("time")) {
                time = var;
            }
            else if (varName.equalsIgnoreCase("profile") || varName.equalsIgnoreCase("trajectory")) {
                stationVariable = var;
            }
        }
        
        if (stationVariable == null)
            errorString = "Could not find expected variable containing information about stations in dataset";
        
        try {
            if (stationVariable.read().getShape()[0] <= trajectoryNumber)
                errorString = stationName + " is not in the dataset!";
        } catch (Exception ex) {
            errorString = "Error reading station variable: " + ex.getLocalizedMessage();
            return;
        }
        
        if (rowsize != null) {
            // get our starting indices
            try {
                Array sizeIndices = indexVar.read();
                Array obsIndices = rowsize.read();
                ArrayList<Integer> startIndices = new ArrayList<Integer>();
                ArrayList<Integer> endIndices = new ArrayList<Integer>();
                for (int j=0; j<sizeIndices.getSize(); j++) {
                    if (sizeIndices.getInt(j) == trajectoryNumber) {
                        int startIndex = 0;
                        int endIndex = 0;
                        // add up our lower and upper index bounds
                        for (int i=0; i<obsIndices.getSize(); i++) {
                            if (i < j) {
                                startIndex += obsIndices.getInt(i);
                            } else if (i == j) {
                                endIndex = startIndex + obsIndices.getInt(i);
                                break;
                            }
                        }
                        startIndices.add(startIndex);
                        endIndices.add(endIndex);
                    }
                }
                
                profileDataStartIndex = startIndices.toArray(new Integer[startIndices.size()]);
                profileDataEndIndex = endIndices.toArray(new Integer[endIndices.size()]);
            } catch (IOException ex) {
                System.out.println("Error in DescribeProfile constructor - " + ex.getMessage());
            }
        }
    }
    
    /*************************
     * SOSDescribeIF Methods *
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
            // position node
            formatSetPositionNode(output);
            // remove unwanted nodes
            removeUnusedNodes(output);
        } else {
            output.setupExceptionOutput(errorString);
        }
    }
    
    /**************************************************************************/

    private void removeUnusedNodes(DescribeSensorFormatter output) {
        output.deleteLocationNode();
        output.deleteTimePosition();
        output.deletePositions();
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
        varMap.put("xlink:href", time.getUnitsString());
        mapMap.put("time", varMap);
        // lat
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", lat.getUnitsString());
        mapMap.put("lat", varMap);
        // lon
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", lon.getUnitsString());
        mapMap.put("lon", varMap);
        //depth
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", depth.getUnitsString());
        mapMap.put("altitude", varMap);
        
        output.setPositionDataDefinition(mapMap, ".", " ", ",");
        
        // now we need our values
        StringBuilder strB = new StringBuilder();
        try {
            DateFormatter formatter = new DateFormatter();
            Array timeArray, latArray, lonArray, indexArray;
//            if (profileIndices != null) {
//                for (int i=0; i<profileIndices.length; i++) {
//                    int obsIndex = profileIndices[i].intValue();
//                    strB.append(formatter.toDateTimeStringISO(startDate.add(timeArray.getDouble(obsIndex), CalendarPeriod.Field.Second).toDate())).append(",");
//                    strB.append(latArray.getDouble(obsIndex)).append(",");
//                    strB.append(lonArray.getDouble(obsIndex)).append(" ");
//                }
            if (profileDataStartIndex == null) {
                // read the arrays at our index
                latArray = getDataArrayFrom2Dimension(lat);
                lonArray = getDataArrayFrom2Dimension(lon);
                timeArray = getDataArrayFrom2Dimension(time);
                float[] depthArray = (float[]) get2DimDataArrayFrom3Dimension(depth).copyTo1DJavaArray();
                for (int i=0; i<startDates.length; i++) {
                    for (int di=0; di<latArray.getSize(); di++) {
                        strB.append(formatter.toDateTimeStringISO(startDates[i].add(timeArray.getDouble(i), CalendarPeriod.Field.Second).toDate())).append(",");
                        strB.append(latArray.getDouble(i)).append(",");
                        strB.append(lonArray.getDouble(i)).append(",");
                        strB.append(depthArray[di]).append(" ");
                    }
                }
            } else {
                latArray = lat.read();
                timeArray = time.read();
                lonArray = lon.read();
                indexArray = indexVar.read();
                Array depthArray = depth.read();
                int profileNumber = 0;
                for (int k=0; k<indexArray.getSize(); k++) {
                    if (indexArray.getInt(k) == trajectoryNumber) {
                        for (int j=profileDataStartIndex[profileNumber]; j<profileDataEndIndex[profileNumber]; j++) {
                            strB.append(formatter.toDateTimeStringISO(startDates[profileNumber].add(timeArray.getDouble(profileNumber), CalendarPeriod.Field.Second).toDate())).append(",");
                            strB.append(latArray.getDouble(profileNumber)).append(",");
                            strB.append(lonArray.getDouble(profileNumber)).append(",");
                            strB.append(depthArray.getObject(j)).append(" ");   
                        }
                        profileNumber++;
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Exception " + ex.getMessage());
        } finally {
            // print out our values
            output.setPositionValue(strB.toString());
        }
    }
    
    /**
     * Reduces a data Array of rank 2 to rank 1 of just the data values. Uses the
     * current trajectory and profiles to extract which data values are pertinent
     * to the response.
     * @param var data Variable from the netcdf dataset
     * @return Array of rank 1 containing relevant values
     */
    private Array getDataArrayFrom2Dimension(Variable var) {
        try {
            Range trajectoryRange = new Range(trajectoryNumber,trajectoryNumber);
            Range profileRange = new Range(0,var.getShape(var.getRank()-1)-1);
            List<Range> listRange = new ArrayList<Range> ();
            listRange.add(trajectoryRange);
            listRange.add(profileRange);

            return var.read().section(listRange);
        } catch (IOException ex) {
//            Logger.getLogger(SOSDescribeSection.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
        catch (InvalidRangeException ex) {
//            Logger.getLogger(SOSDescribeSection.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
        
        return null;
    }
    
    /**
     * Reduces an Array of rank 3 to rank 1 of just the data values. Uses current
     * trajectory and profiles to extract data values pertinent to the response.
     * @param var data Variable from the netcdf dataset
     * @return Array of rank 1 with relevant data values.
     */
    private Array get2DimDataArrayFrom3Dimension(Variable var) {
        try {
            Range trajectoryRange = new Range(trajectoryNumber,trajectoryNumber);
            Range profileRange = new Range(0,var.getShape(var.getRank()-2)-1);
            Range dataRange = new Range(0,var.getShape(var.getRank()-1)-1);
            List<Range> listRange = new ArrayList<Range> ();
            listRange.add(trajectoryRange);
            listRange.add(profileRange);
            listRange.add(dataRange);

            return var.read().section(listRange);
        } catch (IOException ex) {
//            Logger.getLogger(SOSDescribeSection.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
        catch (InvalidRangeException ex) {
//            Logger.getLogger(SOSDescribeSection.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
        
        return null;
    }
}
