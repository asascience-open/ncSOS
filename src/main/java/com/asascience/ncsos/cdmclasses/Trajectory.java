package com.asascience.ncsos.cdmclasses;

import com.asascience.ncsos.getobs.SOSObservationOffering;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Document;
import ucar.nc2.ft.*;
import ucar.nc2.units.DateFormatter;
import ucar.unidata.geoloc.Station;

/**
 * Provides methods to gather information from Trajectory datasets needed for requests: GetCapabilities, GetObservations
 * @author abird
 * @version 1.0.0
 */
public class Trajectory extends baseCDMClass implements iStationData {

    private final ArrayList<String> eventTimes;
    private final String[] variableNames;
    private TrajectoryFeatureCollection trajectoryData;
    private ArrayList<TrajectoryFeature> trajList;
    private ArrayList<Double> altMin, altMax;

    /**
     * 
     * @param requestedStationNames
     * @param eventTime
     * @param variableNames
     */
    public Trajectory(String[] requestedStationNames, String[] eventTime, String[] variableNames) {
        startDate = null;
        endDate = null;
        this.variableNames = variableNames;
        this.reqStationNames = new ArrayList<String>();
        reqStationNames.addAll(Arrays.asList(requestedStationNames));
        
        if (eventTime != null) {
            this.eventTimes = new ArrayList<String>();
            this.eventTimes.addAll(Arrays.asList(eventTime));
        } else
            this.eventTimes = null;
        
        upperAlt = Double.NEGATIVE_INFINITY;
        lowerAlt = Double.POSITIVE_INFINITY;
    }

    private void addAllTrajectoryData(PointFeatureIterator trajFeatureIterator, List<String> valueList, DateFormatter dateFormatter, StringBuilder builder) throws IOException {
        while (trajFeatureIterator.hasNext()) {
            PointFeature trajFeature = trajFeatureIterator.next();
            valueList.clear();
            addDataLine(valueList, dateFormatter, trajFeature, builder);
        }
    }

    private void addDataLine(List<String> valueList, DateFormatter dateFormatter, PointFeature trajFeature, StringBuilder builder) {
        valueList.add("time=" + dateFormatter.toDateTimeStringISO(trajFeature.getObservationTimeAsDate()));

        try {
            for (int i = 0; i < variableNames.length; i++) {
                valueList.add(variableNames[i] + "=" + trajFeature.getData().getScalarObject(variableNames[i]).toString());
            }

            for (String str : valueList) {
                builder.append(str).append(",");
            }

            builder.deleteCharAt(builder.length() - 1).append(";");
        } catch (Exception ex) {
            // print error
            builder.delete(0, builder.length());
            builder.append("ERROR =reading data from dataset: ").append(ex.getLocalizedMessage()).append(". Most likely this property does not exist or is improperly stored in the dataset.");
        }
    }

    @Override
    public void setData(Object featureCollection) throws IOException {
        this.trajectoryData = (TrajectoryFeatureCollection) featureCollection;
        int collectionCount = 0;
        while(trajectoryData.hasNext()) { trajectoryData.next(); collectionCount++; }
        this.trajectoryData.resetIteration();

        trajList = new ArrayList<TrajectoryFeature>();

        DateTime dtSearchStart = null;
        DateTime dtSearchEnd = null;
        
        altMax = new ArrayList<Double>();
        altMin = new ArrayList<Double>();

        boolean firstSet = true;

        //check first to see if the event times are not null
        if (eventTimes != null) {
            //turn event times in to dateTimes to compare
            if (eventTimes.size() >= 1) {
                dtSearchStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
            }
            if (eventTimes.size() == 2) {

                dtSearchEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);
            }
        } else {
            // set search start to earliest possible
            dtSearchStart = new DateTime(0, chrono);
        }

        //temp
        DateTime dtStart = null;
        DateTime dtEnd = null;
        //check
        DateTime dtStartt = null;
        DateTime dtEndt = null;
        
        while (trajectoryData.hasNext()) {
            TrajectoryFeature trajFeature = trajectoryData.next();
            trajFeature.calcBounds();

            String n = trajFeature.getName();
            
            // ok, so this is necessary if the collection names are not setup properly. This conditional
            // will probably only fix 1% of the collections that are improperly set.
            if (reqStationNames.size() == collectionCount)
                trajList.add(trajFeature);

            // find a better solution for getting features from the collection - TODO
            //scan through the stationname for a match of id
            for (String stName : reqStationNames) {
                if (stName.equalsIgnoreCase(n)) {
                    if (!trajList.contains(trajFeature))
                        trajList.add(trajFeature);
                    break;
                }
            }
        }
        
        // Could not find any matching stations in the collection
        if (trajList.size() < 1) {
            return;
        }
        
        for (Iterator<TrajectoryFeature> it = trajList.iterator(); it.hasNext();) {
            TrajectoryFeature feature = it.next();
            double localAltMin = Double.POSITIVE_INFINITY;
            double localAltMax = Double.NEGATIVE_INFINITY;
            for (feature.resetIteration();feature.hasNext();) {
                PointFeature point = feature.next();

                if (point == null || point.getLocation() == null) {
                    System.out.println("point or its location is null");
                    continue;
                }

                double altitude = point.getLocation().getAltitude();
                if (altitude == Invalid_Value) {
                    System.out.println("Invalid value for altitude");
                    continue;
                }

                if (altitude > localAltMax) 
                    localAltMax = altitude;
                if (altitude < localAltMin)
                    localAltMin = altitude;
            }
            if (localAltMin < lowerAlt)
                lowerAlt = localAltMin;
            if (localAltMax > upperAlt)
                upperAlt = localAltMax;
            altMax.add(localAltMax);
            altMin.add(localAltMin);
            
            if (firstSet) {
                upperLat = feature.getBoundingBox().getLatMax();
                lowerLat = feature.getBoundingBox().getLatMin();
                upperLon = feature.getBoundingBox().getLonMax();
                lowerLon = feature.getBoundingBox().getLonMin();

                dtStart = new DateTime(feature.getDateRange().getStart().getDate(), chrono);
                dtEnd = new DateTime(feature.getDateRange().getEnd().getDate(), chrono);
                firstSet = false;
            } else {

                dtStartt = new DateTime(feature.getDateRange().getStart().getDate(), chrono);
                dtEndt = new DateTime(feature.getDateRange().getEnd().getDate(), chrono);

                if (dtStartt.isBefore(dtStart)) {
                    dtStart = dtStartt;
                }
                if (dtEndt.isAfter(dtEnd)) {
                    dtEnd = dtEndt;
                }

                if (feature.getBoundingBox().getLatMax() > upperLat) {
                    upperLat = feature.getBoundingBox().getLatMax();
                }
                if (feature.getBoundingBox().getLatMin() < lowerLat) {
                    lowerLat = feature.getBoundingBox().getLatMin();
                }
                //lon
                if (feature.getBoundingBox().getLonMax() > upperLon) {
                    upperLon = feature.getBoundingBox().getLonMax();
                }
                if (feature.getBoundingBox().getLonMax() < lowerLon) {
                    lowerLon = feature.getBoundingBox().getLonMin();
                }
            }
        }
        setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
        setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));
        if (reqStationNames != null) {
            setNumberOfStations(reqStationNames.size());
        }
    }

    @Override
    public void setInitialLatLonBoundaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDataResponse(int stNum) {
        try {
            if (trajectoryData != null) {
                return createTrajectoryFeature(stNum);
            }
        } catch (IOException ex) {
            Logger.getLogger(Trajectory.class.getName()).log(Level.SEVERE, null, ex);
            return DATA_RESPONSE_ERROR + Profile.class;
        }
        return DATA_RESPONSE_ERROR + Profile.class;
    }

    @Override
    public String getStationName(int idNum) {
        if (trajList != null) {
            return "TRAJECTORY_" + (trajList.get(idNum).getName());
        } else {
            return Invalid_Station;
        }
    }

    @Override
    public double getLowerLat(int stNum) {
        if (trajList != null) {
            return (trajList.get(stNum).getBoundingBox().getLatMin());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getLowerLon(int stNum) {
        if (trajList != null) {
            return (trajList.get(stNum).getBoundingBox().getLonMin());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLat(int stNum) {
        if (trajList != null) {
            return (trajList.get(stNum).getBoundingBox().getLatMax());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLon(int stNum) {
        if (trajList != null) {
            return (trajList.get(stNum).getBoundingBox().getLonMax());
        } else {
            return Invalid_Value;
        }
    }
    
    @Override
    public double getLowerAltitude(int stNum) {
        try {
            if (altMin != null) {
                return altMin.get(stNum);
            }
        } catch (Exception e) { 
            System.out.println("Exception in getLowerAltitude - " + e.getMessage());
        }
        return Invalid_Value;
    }
    @Override
    public double getUpperAltitude(int stNum) {
        try {
            if (altMax != null) {
                return altMax.get(stNum);
            }
        } catch (Exception e) {
            System.out.println("error in get upper altitude - " + e.getMessage());
        }
        return Invalid_Value;
    }

    @Override
    public String getTimeEnd(int stNum) {
        if (trajList != null) {
            return df.toDateTimeStringISO(trajList.get(stNum).getDateRange().getEnd().getDate());
        } else {
            return ERROR_NULL_DATE;
        }
    }

    @Override
    public String getTimeBegin(int stNum) {
        if (trajList != null) {
            return df.toDateTimeStringISO(trajList.get(stNum).getDateRange().getStart().getDate());
        } else {
            return ERROR_NULL_DATE;
        }
    }

    /**
     * gets the trajectory response for the getcaps request
     * @param dataset
     * @param document
     * @param featureOfInterest
     * @param GMLName
     * @param observedPropertyList
     * @return
     * @throws IOException 
     */
    public static Document getCapsResponse(FeatureCollection dataset, Document document, String featureOfInterest, String GMLName, List<String> observedPropertyList) throws IOException {
        //PointFeatureIterator trajIter;


        while (((TrajectoryFeatureCollection) dataset).hasNext()) {
            TrajectoryFeature tFeature = ((TrajectoryFeatureCollection) dataset).next();
            tFeature.calcBounds();

            //trajIter = tFeature.getPointFeatureIterator(-1);
            //attributes
            SOSObservationOffering newOffering = new SOSObservationOffering();
            newOffering.setObservationStationLowerCorner(Double.toString(tFeature.getBoundingBox().getLatMin()), Double.toString(tFeature.getBoundingBox().getLonMin()));
            newOffering.setObservationStationUpperCorner(Double.toString(tFeature.getBoundingBox().getLatMax()), Double.toString(tFeature.getBoundingBox().getLonMax()));

            //check the data
            if (tFeature.getDateRange() != null) {
                newOffering.setObservationTimeBegin(tFeature.getDateRange().getStart().toDateTimeStringISO());
                newOffering.setObservationTimeEnd(tFeature.getDateRange().getEnd().toDateTimeStringISO());
            } //find the dates out!
            else {
                System.out.println("no dates yet");
            }

            newOffering.setObservationStationDescription(tFeature.getCollectionFeatureType().toString());
            newOffering.setObservationFeatureOfInterest(featureOfInterest + (tFeature.getName()));
            newOffering.setObservationName(GMLName + (tFeature.getName()));
            newOffering.setObservationStationID((tFeature.getName()));
            newOffering.setObservationProcedureLink(GMLName + ((tFeature.getName())));
            newOffering.setObservationSrsName("EPSG:4326");  // TODO?  
            newOffering.setObservationObserveredList(observedPropertyList);

            document = CDMUtils.addObsOfferingToDoc(newOffering, document);
        }

        return document;
    }

    @Override
    public String getDescription(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String createTrajectoryFeature(int stNum) throws IOException {
        StringBuilder builder = new StringBuilder();
        DateFormatter dateFormatter = new DateFormatter();
        TrajectoryFeature trajFeature = trajList.get(stNum);
        addTrajectoryData(dateFormatter, builder, trajFeature, stNum);
        return builder.toString();
    }

    private void addTrajectoryData(DateFormatter dateFormatter, StringBuilder builder, TrajectoryFeature traj, int stNum) throws IOException {

        List<String> valueList = new ArrayList<String>();
        PointFeatureIterator trajFeatureIterator = traj.getPointFeatureIterator(-1);

        DateTime trajTime;
        DateTime dtStart;
        DateTime dtEnd;

        //if no times are specified
        if (eventTimes == null) {
            addAllTrajectoryData(trajFeatureIterator, valueList, dateFormatter, builder);
        } //if more than one date is specified
        else if (eventTimes.size() > 1) {
            //get the dates in iso format
            dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
            dtEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);

            while (trajFeatureIterator.hasNext()) {
                PointFeature trajFeature = trajFeatureIterator.next();
                valueList.clear();

                trajTime = new DateTime(trajFeature.getObservationTimeAsDate(), chrono);

                if (trajTime.isEqual(dtStart)) {
                    addDataLine(valueList, dateFormatter, trajFeature, builder);
                } else if (trajTime.isEqual(dtEnd)) {
                    addDataLine(valueList, dateFormatter, trajFeature, builder);
                } else if (trajTime.isAfter(dtStart) && (trajTime.isBefore(dtEnd))) {
                    addDataLine(valueList, dateFormatter, trajFeature, builder);
                }

            }
        } //if start and end time were specified
        else if (eventTimes.size() == 1) {
            //get the single date in iso format
            dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
            while (trajFeatureIterator.hasNext()) {
                PointFeature trajFeature = trajFeatureIterator.next();
                valueList.clear();
                trajTime = new DateTime(trajFeature.getObservationTimeAsDate(), chrono);

                if (trajTime.isEqual(dtStart)) {
                    addDataLine(valueList, dateFormatter, trajFeature, builder);
                    //if it matches return...
                    break;
                }
            }
        } //times specified are weird, report all
        else {
            addAllTrajectoryData(trajFeatureIterator, valueList, dateFormatter, builder);
        }
    }
}
