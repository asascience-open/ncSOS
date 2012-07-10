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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarPeriod;
import ucar.nc2.units.DateFormatter;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author SCowan
 */
public class SOSDescribeSection extends SOSDescribeStation implements SOSDescribeIF {
    
    private int trajectoryNumber;
//    private Integer[] profileIndices;
    private Integer[] profileDataStartIndex, profileDataEndIndex;
    private Variable lat, lon, depth, time;
    private Variable indexVar;
    private CalendarDate[] startDates;
    private LatLonRect bbox;
    
    public SOSDescribeSection( NetcdfDataset dataset, String procedure, CalendarDate[] sDate ) {
        super(dataset, procedure);
        
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
            // look for the variable that describes our indexing
//            if (varName.contains("index")) {
//                try {
//                    // this lists the profile number of each obs
//                    Array obsIndices = var.read();
//                    ArrayList<Integer> indexBuilder = new ArrayList<Integer>();
//                    // iterate through the 'Array' to find all values which match our profileNumber
//                    for (int i=0; i<obsIndices.getSize(); i++) {
//                        if (profileNumber == obsIndices.getInt(i))
//                            indexBuilder.add(i);
//                    }
//                    // convert to our array
//                    profileIndices = indexBuilder.toArray(new Integer[indexBuilder.size()]);
//                } catch (IOException ex) {
//                    System.out.println("Error in DescribeProfile constructor - " + ex.getMessage());
//                }
//            }
//            else 
            if (varName.contains("rowsize")) {
                rowsize = var;
            } else if (varName.contains("index")) {
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
        
        // calculate our bounding box
//        this.bbox = getBoundingBox();
    }
    
    @Override
    public void SetupOutputDocument(DescribeSensorFormatter output) {
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
        RemoveUnusedNodes(output);
    }

    private void RemoveUnusedNodes(DescribeSensorFormatter output) {
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
    
//    private void formatSetPositionsNode(DescribeSensorFormatter output) {
//        HashMap<String,String> latitude, longitude, depthMap;
//        // set our hashmaps for our station
//        latitude = new HashMap<String, String>();
//        for (Attribute attr : lat.getAttributes()) {
//            String attrName = attr.getName().toLowerCase();
//            if (attrName.equals("standard_name")) {
//                latitude.put("name", attr.getStringValue());
//            }
//            else if (attrName.contains("unit")) {
//                latitude.put("code", attr.getStringValue());
//            }
//            else if (attrName.equals("axis")) {
//                latitude.put("axisID", attr.getStringValue());
//            }
//        }
//        
//        longitude = new HashMap<String, String>();
//        for (Attribute attr : lon.getAttributes()) {
//            String attrName = attr.getName().toLowerCase();
//            if (attrName.equals("standard_name")) {
//                longitude.put("name", attr.getStringValue());
//            }
//            else if (attrName.contains("unit")) {
//                longitude.put("code", attr.getStringValue());
//            }
//            else if (attrName.equals("axis")) {
//                longitude.put("axisID", attr.getStringValue());
//            }
//        }
//        
//        depthMap = new HashMap<String, String>();
//        for (Attribute attr : depth.getAttributes()) {
//            String attrName = attr.getName().toLowerCase();
//            if (attrName.equals("standard_name")) {
//                depthMap.put("name", attr.getStringValue());
//            }
//            else if (attrName.contains("unit")) {
//                depthMap.put("code", attr.getStringValue());
//            }
//            else if (attrName.equals("axis")) {
//                depthMap.put("axisID", attr.getStringValue());
//            }
//        }
//        depthMap.put("value", "0");
//        
//        //set our station
////        output.setStationPositionsNode(latitude, longitude, depthMap, "", bbox);
//        // get our depth for the end point
//        try {
//            Array array = depth.read();
//            ArrayList<Double> depthValues = new ArrayList<Double>();
////            if (profileIndices != null) {
////                for (int i=0; i<profileIndices.length; i++) {
////                    depthValues.add(array.getDouble(profileIndices[i]));
////                }
////            } else {
////                for (int j=profileStartIndex; j<profileEndIndex; j++) {
////                    depthValues.add(array.getDouble(j));
////                }
////            }
//            Double largestVal = 0d;
//            for (Double dbl : depthValues) {
//                if (Math.abs(dbl.doubleValue()) > Math.abs(largestVal.doubleValue()))
//                    largestVal = dbl;
//            }
//            depthMap.put("value", largestVal.toString());
//        } catch (IOException ex) {
//            System.out.println("Exception in formatSetPositionsNode - " + ex.getMessage());
//        }
//        // set our end point
//        output.setEndPointPositionsNode(latitude, longitude, depthMap, "", bbox);
//    }
    
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
    
    
//    private LatLonRect getBoundingBox() {
//        
//    }
}
