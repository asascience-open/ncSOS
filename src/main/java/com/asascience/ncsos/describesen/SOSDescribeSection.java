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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.w3c.dom.Element;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.ProfileFeature;
import ucar.nc2.ft.SectionFeature;
import ucar.nc2.ft.SectionFeatureCollection;
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
    private Variable indexVar;
    private CalendarDate[] startDates;
    
    Variable rowSize;
    SectionFeatureCollection featureCollection;
    
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
        
        // get our trajectory number
        String tStr = stationName.toLowerCase().replaceAll("(profile)", "");
        tStr = tStr.replaceAll("(trajectory)", "");
        trajectoryNumber = Integer.parseInt(tStr);
        
        this.startDates = sDate;
        
        rowSize = indexVar = null;
        profileDataStartIndex = profileDataEndIndex = null;
        
        for (Variable var : dataset.getVariables()) {
            String varName = var.getFullName().toLowerCase();
            if (varName.contains("rowsize")) {
                rowSize = var;
            }
            else if (varName.contains("index")) {
                indexVar = var;
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
        
        if (rowSize != null && indexVar != null) {
            // get our starting indices
            try {
                Array sizeIndices = indexVar.read();
                Array obsIndices = rowSize.read();
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
    
    /**
     * Constructor for 'network-all' requests
     * @param dataset
     * @param collection
     * @throws IOException 
     */
    public SOSDescribeSection( NetcdfDataset dataset, SectionFeatureCollection collection ) throws IOException {
        super(dataset);
        
        featureCollection = collection;
        
        rowSize = indexVar = null;
        profileDataStartIndex = profileDataEndIndex = null;
        
        for (Variable var : dataset.getVariables()) {
            String varName = var.getFullName().toLowerCase();
            if (varName.contains("rowsize")) {
                rowSize = var;
            }
            else if (varName.contains("index")) {
                indexVar = var;
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
            formatSetLocationNode(output);
            // remove unwanted nodes
//            removeUnusedNodes(output);
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
    
    private void formatSetLocationNode(DescribeNetworkFormatter output, Element stationNode) {
        output.setStationLocationNode3Dimension(stationNode, stationName, getCoordsForSetStation());
    }
    
    private void readTrajectoryInformation(int trajNumber) {
        profileDataStartIndex = profileDataEndIndex = null;
        this.trajectoryNumber = trajNumber;
        
        if (rowSize != null && indexVar != null) {
            // get our starting indices
            try {
                Array indexArray = indexVar.read();
                Array rowSizeArray = rowSize.read();
                ArrayList<Integer> startIndices = new ArrayList<Integer>();
                ArrayList<Integer> endIndices = new ArrayList<Integer>();
                for (int j=0; j<indexArray.getSize(); j++) {
                    if (indexArray.getInt(j) == trajNumber) {
                        int startIndex = 0;
                        int endIndex = 0;
                        // add up our lower and upper index bounds
                        for (int i=0; i<rowSizeArray.getSize(); i++) {
                            if (i < j) {
                                startIndex += rowSizeArray.getInt(i);
                            } else if (i == j) {
                                endIndex = startIndex + rowSizeArray.getInt(i);
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
                System.out.println("readTrajectoryInformation - " + ex.toString() + "\n\t" + ex.getStackTrace()[0].toString());
            }
        } else {
            // set dates for the trajetory number
            try {
                ArrayList<CalendarDate> secColStart = new ArrayList<CalendarDate>();
                int i=-1;
                for (featureCollection.resetIteration();featureCollection.hasNext();) {
                    SectionFeature section = featureCollection.next();
                    if (++i == (trajNumber-1)) {
                        i=0;
                        for (section.resetIteration();section.hasNext();) {
                            ProfileFeature pfeature = section.next();
                            DatasetHandlerAdapter.calcBounds(pfeature);
                            for (pfeature.resetIteration();pfeature.hasNext();) {
                                // iterate through data to make sure various items (ie start date) isn't null
                                PointFeature pointf = pfeature.next();
                            }
                            if (pfeature.getCalendarDateRange() != null && pfeature.getCalendarDateRange().getStart() != null)
                                secColStart.add(pfeature.getCalendarDateRange().getStart());
                        }
                        break;
                    }
                }
                startDates = secColStart.toArray(new CalendarDate[secColStart.size()]);
            } catch (Exception ex) {
                System.out.println("readTrajectoryInformation - " + ex.toString() + "\n\t" + ex.getStackTrace()[0].toString());
            }
        }
    }
    
    private double[][] getCoordsForSetStation() {
        double[][] coords = null;
        try {
            if (profileDataStartIndex == null) {
                if (depthVariable.getShape().length > 2) {
                    // read the arrays at our index
                    int depth1 = depthVariable.getShape(1);
                    int depth2 = depthVariable.getShape(2);
                    double[] latArray = (double[]) latVariable.read().get1DJavaArray(double.class);
                    double[] lonArray = (double[]) lonVariable.read().get1DJavaArray(double.class);
                    Array depthArray = depthVariable.read().section(new int[] { trajectoryNumber, 0, 0 }, new int[] { 1, depth1, depth2 });
                    depthArray = depthArray.reduce();
                    coords = new double[(int)depthArray.getSize()][3];
                    for (int i=0; i<depth1; i++) {
                        for (int j=0; j<depth2; j++) {
                            int curIndex = (i*depth2) + j;
                            if (Double.compare(latArray[depth1*trajectoryNumber + i], Double.NaN) == 0 || Double.compare(lonArray[depth1*trajectoryNumber + i], Double.NaN) == 0 || Double.compare(depthArray.getDouble(curIndex), Double.NaN) == 0) {
                                coords[curIndex][2] = -999;
                                continue;
                            } else {
                                coords[curIndex][0] = latArray[depth1*trajectoryNumber + i];
                                coords[curIndex][1] = lonArray[depth1*trajectoryNumber + i];
                                coords[curIndex][2] = depthArray.getDouble(curIndex);
                            }
                        }
                    }
                } else {
                    Array latArray = latVariable.read();
                    Array lonArray = lonVariable.read();
                    Array depthArray = depthVariable.read().reshape(new int[] { depthVariable.getShape(0) * depthVariable.getShape(1) });
                    coords = new double[(int)depthArray.getSize()][3];
                    int latarraysize = (int)latArray.getSize();
                    int deptharraysize = (int)depthVariable.getShape(1);
                    for (int di=0; di<latarraysize; di++) {
                        for (int ii=0; ii<deptharraysize;ii++) {
                            coords[(int)(di*deptharraysize + ii)][0] = latArray.getDouble(di);
                            coords[(int)(di*deptharraysize + ii)][1] = lonArray.getDouble(di);
                            coords[(int)(di*deptharraysize + ii)][2] = depthArray.getDouble((int)(di*deptharraysize + ii));
                        }
                    }
                }
            } else {
                Array latArray = latVariable.read();
                Array lonArray = lonVariable.read();
                Array indexArray = indexVar.read();
                Array depthArray = depthVariable.read();
                int profileNumber = 0;
                List<Double[]> latLon = new ArrayList<Double[]>();
                List<Double> depth = new ArrayList<Double>();
                for (int k=0; k<indexArray.getSize(); k++) {
                    if (indexArray.getInt(k) == trajectoryNumber) {
                        for (int j=profileDataStartIndex[profileNumber]; j<profileDataEndIndex[profileNumber]; j++) {
                            latLon.add(new Double[] { latArray.getDouble(k), lonArray.getDouble(k) });
                            depth.add(depthArray.getDouble(j));
                        }
                        profileNumber++;
                    }
                }
                coords = new double[depth.size()][3];
                for (int d=0; d<depth.size(); d++) {
                    coords[d][0] = latLon.get(d)[0];
                    coords[d][1] = latLon.get(d)[1];
                    coords[d][2] = depth.get(d);
                }
            }
        } catch (Exception ex) {
            System.out.println("formatSetLocationNode - " + ex.toString() + "\n\t" + ex.getStackTrace()[0].toString());
        }
        return coords;
    }
    
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
            // set location for station
            readTrajectoryInformation(stIndex);
            formatSetLocationNode(output, stNode);
            // remove unwanted nodes for each station
            output.removeStationPosition(stNode);
            output.removeStationPositions(stNode);
            output.removeStationTimePosition(stNode);
        }
    }
    
    @Override
    protected void formatSetLocationNode(DescribeSensorFormatter output) {
        output.setLocationNode3Dimension(stationName, getCoordsForSetStation());
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
        varMap.put("xlink:href", timeVariable.getUnitsString());
        mapMap.put("time", varMap);
        // lat
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", latVariable.getUnitsString());
        mapMap.put("lat", varMap);
        // lon
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", lonVariable.getUnitsString());
        mapMap.put("lon", varMap);
        //depth
        varMap = new HashMap<String, String>();
        varMap.put("definition", "some:definition");
        varMap.put("code", depthVariable.getUnitsString());
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
                latArray = getDataArrayFrom2Dimension(latVariable);
                lonArray = getDataArrayFrom2Dimension(lonVariable);
                timeArray = getDataArrayFrom2Dimension(timeVariable);
                float[] depthArray = (float[]) get2DimDataArrayFrom3Dimension(depthVariable).copyTo1DJavaArray();
                for (int i=0; i<startDates.length; i++) {
                    for (int di=0; di<latArray.getSize(); di++) {
                        strB.append(formatter.toDateTimeStringISO(startDates[i].add(timeArray.getDouble(i), CalendarPeriod.Field.Second).toDate())).append(",");
                        strB.append(latArray.getDouble(i)).append(",");
                        strB.append(lonArray.getDouble(i)).append(",");
                        strB.append(depthArray[di]).append(" ");
                    }
                }
            } else {
                latArray = latVariable.read();
                timeArray = timeVariable.read();
                lonArray = lonVariable.read();
                indexArray = indexVar.read();
                Array depthArray = depthVariable.read();
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
