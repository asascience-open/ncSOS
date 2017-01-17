/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.cdmclasses;

import com.asascience.ncsos.go.ObservationOffering;
import com.asascience.ncsos.service.BaseRequestHandler;

import org.joda.time.DateTime;
import org.w3c.dom.Document;

import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.ft.*;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.nc2.units.DateFormatter;
import ucar.unidata.geoloc.Station;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides methods to gather information from TimeSeriesProfile datasets needed for requests: GetCapabilities, GetObservations
 * @author abird
 * @version 1.0.0
 */
public class TimeSeriesProfile extends baseCDMClass implements iStationData {

    private StationProfileFeatureCollection tsProfileData;
    private List<Station> tsStationList;
    private final ArrayList<String> eventTimes;
    private final String[] variableNames;
    private ArrayList<Double> altMin, altMax;
    private Map<String, List<Double>> numberHeightsForStation;
    private boolean requestedFirst;
    private boolean requestedLast;
    private boolean multDimTimVar;
    CoordinateAxis heightAxis;
    /**
     * 
     * @param stationName
     * @param eventTime
     * @param variableNames
     */
    public TimeSeriesProfile(String[] stationName, String[] eventTime, 
                             String[] variableNames, boolean requestedFirst,
                             boolean requestedLast, boolean multDimTimeVar,
                             CoordinateAxis heightAxis) {

        startDate = null;
        endDate = null;
        this.variableNames = variableNames;
        this.multDimTimVar = multDimTimeVar;
        this.reqStationNames = new ArrayList<String>();
        this.requestedFirst = requestedFirst;
        this.requestedLast = requestedLast;
        this.heightAxis = heightAxis;
        reqStationNames.addAll(Arrays.asList(stationName));
        if (eventTime != null) {
            this.eventTimes = new ArrayList<String>();
            this.eventTimes.addAll(Arrays.asList(eventTime));
        } else
            this.eventTimes = null;
        lowerAlt = Double.POSITIVE_INFINITY;
        upperAlt = Double.NEGATIVE_INFINITY;
    }

    /****************TIMESERIESPROFILE*******************/
    private String createStationProfileFeature(int stNum) throws IOException {
        StringBuilder builder = new StringBuilder();
        DateFormatter dateFormatter = new DateFormatter();
        List<String> valueList = new ArrayList<String>();

        StationProfileFeature stationProfileFeature = tsProfileData.getStationProfileFeature(tsStationList.get(stNum));
        List<Date> z = stationProfileFeature.getTimes();

        ProfileFeature pf = null;
        Set<Date> processedDates = new HashSet<Date>();
      
        //if not event time is specified get all the data
        if (eventTimes == null) {
            //test getting items by date(index(0))
            stationProfileFeature.resetIteration();
//            for (int i = 0; i < z.size(); i++) {
            while(stationProfileFeature.hasNext()){
               // pf = stationProfileFeature.getProfileByDate(z.get(i));
                pf = stationProfileFeature.next();
                System.out.println(pf.getTime().toGMTString());
                if(this.multDimTimVar || !processedDates.contains(pf.getTime()) ){
                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
                    if (builder.toString().contains("ERROR"))
                        break;
                    processedDates.add(pf.getTime());
                }
             
            }
        } else if (eventTimes.size() > 1) {
            Date startDate = null;
            Date endDate = null;
            if(this.requestedFirst){
                startDate = z.get(0);
                if(!requestedLast && eventTimes.get(0).equals(eventTimes.get(1))){
                    endDate = startDate;
                }
            }
       
            if(this.requestedLast){
                endDate = z.get(z.size() - 1);
                if(!requestedFirst && eventTimes.get(0).equals(eventTimes.get(1))){
                    startDate = endDate;
                }
            }
            if(endDate == null) {
                endDate = CalendarDateFormatter.isoStringToDate( eventTimes.get(1));
            }
            if(startDate == null) {
                startDate = CalendarDateFormatter.isoStringToDate( eventTimes.get(0));
            }
            for (int i = 0; i < z.size(); i++) {

                // check to make sure the data is within the start/stop
                if(z.get(i).compareTo(startDate) >= 0 &&  z.get(i).compareTo(endDate)  <= 0){
                    if(this.multDimTimVar || !processedDates.contains(z.get(i)) ){
                        pf = stationProfileFeature.getProfileByDate(z.get(i));

                        DateTime dtStart = new DateTime(startDate, chrono);
                        DateTime dtEnd = new DateTime(endDate, chrono);
                        DateTime tsDt = new DateTime(pf.getTime(), chrono);

                        //find out if current time(searchtime) is one or after startTime
                        //same as start
                        if (tsDt.isEqual(dtStart)) {
                            createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
                        } //equal end
                        else if (tsDt.isEqual(dtEnd)) {
                            createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
                        } //afterStart and before end       
                        else if (tsDt.isAfter(dtStart) && (tsDt.isBefore(dtEnd))) {
                            createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
                        }
                        if (builder.toString().contains("ERROR"))
                            break;
                        processedDates.add(z.get(i));
                    }
                }
            }
        } //if the event time is specified get the correct data        
        else {
            for (int i = 0; i < z.size(); i++) {

                if (df.toDateTimeStringISO(z.get(i)).contentEquals(eventTimes.get(0).toString())) {
                    pf = stationProfileFeature.getProfileByDate(z.get(i));
                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
                }
                
                if (builder.toString().contains("ERROR"))
                    break;
            }
        }
        return builder.toString();
    }

    public int getNumberProfilesForStation(String station){
        int numProfiles = 0;
        if(this.numberHeightsForStation.containsKey(station)){
            numProfiles = this.numberHeightsForStation.get(station).size();
        }
        return numProfiles;
    }
    
    public String getHeightAxisUnits(){
    	String heightUnits = null;
    	if(this.heightAxis != null){
    		heightUnits = this.heightAxis.getUnitsString();
    	}
    	return heightUnits;
    }
    
    public List<Double> getProfileHeightsForStation(String station){
        List<Double> profHeights;
        if(this.numberHeightsForStation.containsKey(station)){
            profHeights = this.numberHeightsForStation.get(station);
        }
        else{
            profHeights = new ArrayList<Double>();
        }
        return profHeights;
    }
    
    private void createStationProfileData(ProfileFeature pf, List<String> valueList, 
                                           DateFormatter dateFormatter, StringBuilder builder, int stNum) {

        try {
            PointFeatureIterator it = pf.getPointFeatureIterator(-1);
            List<Double> binAlts = this.getProfileHeightsForStation(tsStationList.get(stNum).getName());
            while (it.hasNext()) {
                PointFeature pointFeature = it.next();
                valueList.clear();
                valueList.add(TIME_STR + dateFormatter.toDateTimeStringISO(
                		new Date(pointFeature.getObservationTimeAsCalendarDate().getMillis())));
                valueList.add(STATION_STR + stNum);
                Object heightOb = null;
                if(this.heightAxis != null)
                    heightOb = pointFeature.getData().getScalarObject(this.heightAxis.getShortName());
               
                double alt;
                if(heightOb != null)
                    alt = Double.valueOf(heightOb.toString());
                else
                    alt = pointFeature.getLocation().getAltitude();
                
                if(binAlts != null && binAlts.contains(alt)){
                    valueList.add(BIN_STR + binAlts.indexOf(alt));
                }
                for (String variableName : variableNames) {
                    valueList.add(variableName + "=" + pointFeature.getData().getScalarObject(variableName).toString());
                }

                for (int i = 0; i < valueList.size(); i++) {
                    builder.append(valueList.get(i));
                    if (i < valueList.size() - 1) {
                        builder.append(",");
                    }
                }

                //builder.append(tokenJoiner.join(valueList));
                // TODO:  conditional inside loop...
                if (tsProfileData.getStationProfileFeature(tsStationList.get(stNum)).size() > 1) {
    //                builder.append(" ");
    //                builder.append("\n");
                    builder.append(";");
                }
            }
        } catch (Exception ex ) {
            // print error
            builder.delete(0, builder.length());
            builder.append("ERROR =reading data from dataset: ").append(ex.getLocalizedMessage()).append(". Most likely this property does not exist or is improperly stored in the dataset.");
        }
    }

    
    
    
    /**
     * sets the time series profile data
     * @param featureProfileCollection 
     */
    @Override
    public void setData(Object featureProfileCollection) throws IOException {
        this.tsProfileData = (StationProfileFeatureCollection) featureProfileCollection;
        String genericName = this.tsProfileData.getCollectionFeatureType().name()+"-";

        tsStationList = tsProfileData.getStations(reqStationNames);

        // Try to get stations by name, both with URN procedure and without
        tsStationList = tsProfileData.getStations(reqStationNames);
        for (String s : reqStationNames) {
            String[] urns = s.split(":");
            String statUrn = urns[urns.length - 1];

            Station st = tsProfileData.getStation(urns[urns.length - 1]);
            if (st != null) {
                tsStationList.add(st);
            }
            else if (statUrn.startsWith(genericName)){
            	// check to see if generic name (ie: STATION-0)
            	try {
            		Integer sIndex = Integer.valueOf(statUrn.substring(genericName.length()));
            		st = tsProfileData.getStations().get(sIndex);
            		if(st != null){
            			tsStationList.add(st);
            		}
            	}
            	catch(Exception n){
            		n.printStackTrace();
            	}
            }
        }

        setNumberOfStations(tsStationList.size());
        
        altMin = new ArrayList<Double>();
        altMax = new ArrayList<Double>();
        numberHeightsForStation = new HashMap<String, List<Double>>();
        DateTime curTime;
        DateTime dtStart = null;
        DateTime dtEnd = null;
        if (tsStationList.size() > 0) {


            for (int i = 0; i < tsStationList.size(); i++) {
                StationProfileFeature sPFeature = tsProfileData.getStationProfileFeature(tsStationList.get(i));
                List<Date> times = sPFeature.getTimes();

                if (i == 0) {
                    setInitialLatLonBoundaries(tsStationList);
                    dtStart = new DateTime(times.get(0), chrono);
                    dtEnd = new DateTime(times.get(0), chrono);
                } else {
                    checkLatLonAltBoundaries(tsStationList, i);
                }

                //check the dates
                for (int j = 0; j < times.size(); j++) {
                    curTime = new DateTime(times.get(j), chrono);

                    if (curTime.isBefore(dtStart)) {
                        dtStart = curTime;
                    } else if (curTime.isAfter(dtEnd)) {
                        dtEnd = curTime;
                    }
                }
            }
            setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
            setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));
            
            // iterate through the stations and check the altitudes by their profiles
            for (int j = 0; j < tsStationList.size(); j++) {
                StationProfileFeature profile = tsProfileData.getStationProfileFeature(tsStationList.get(j));
                double altmin = Double.POSITIVE_INFINITY;
                double altmax = Double.NEGATIVE_INFINITY;
                List<Double> altVals = new ArrayList<Double>();
                for (profile.resetIteration();profile.hasNext();) {
                    ProfileFeature nProfile = profile.next();
                    for (nProfile.resetIteration();nProfile.hasNext();) {
                        PointFeature point = nProfile.next();
                        Object heightOb = null;
                        if(this.heightAxis != null)
                            heightOb = point.getData().getScalarObject(this.heightAxis.getShortName());
                       
                        double alt;
                        if(heightOb != null)
                            alt = Double.valueOf(heightOb.toString());
                        else
                            alt = point.getLocation().getAltitude();
                        if (!Double.toString(alt).equalsIgnoreCase("nan")) {
                            if (alt > altmax) 
                                altmax = alt;
                            if (alt < altmin) 
                                altmin = alt;
                            if (alt > upperAlt)
                                upperAlt = alt;
                            if (alt < lowerAlt)
                                lowerAlt = alt;
                        }
                       if(!altVals.contains(alt))
                           altVals.add(alt);
                    }
                    if(!this.multDimTimVar)
                        break;
               }
                this.numberHeightsForStation.put(tsStationList.get(j).getName(), altVals);
                altMin.add(altmin);
                altMax.add(altmax);
            }
        }
    }

    
  
    
    @Override
    public void setInitialLatLonBoundaries(List<Station> tsStationList) {
        upperLat = tsStationList.get(0).getLatitude();
        lowerLat = tsStationList.get(0).getLatitude();
        upperLon = tsStationList.get(0).getLongitude();
        lowerLon = tsStationList.get(0).getLongitude();
    }

    @Override
    public String getDataResponse(int stNum) {
        try {
            if (tsProfileData != null) {
                return createStationProfileFeature(stNum);
            }
        } catch (IOException ex) {
            Logger.getLogger(TimeSeriesProfile.class.getName()).log(Level.SEVERE, null, ex);
            return DATA_RESPONSE_ERROR + TimeSeries.class;
        }
        return DATA_RESPONSE_ERROR + TimeSeries.class;
    }

    @Override
    public String getStationName(int idNum) {
        if (tsProfileData != null && getNumberOfStations() > idNum) {
        	String statName = tsStationList.get(idNum).getName();
        	if (statName.isEmpty()){
        		// return generic
        		statName = this.tsProfileData.getCollectionFeatureType().name()+"-"+idNum;
        	}
            return statName;
        } 
        else {
            return Invalid_Station;
        }
    }

    @Override
    public double getLowerLat(int stNum) {
        if (tsProfileData != null) {
            return (tsStationList.get(stNum).getLatitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getLowerLon(int stNum) {
        if (tsProfileData != null) {
            return (tsStationList.get(stNum).getLongitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLat(int stNum) {
        if (tsProfileData != null) {
            return (tsStationList.get(stNum).getLatitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLon(int stNum) {
        if (tsProfileData != null) {
            return (tsStationList.get(stNum).getLongitude());
        } else {
            return Invalid_Value;
        }
    }
    
    @Override
    public double getLowerAltitude(int stNum) {
        if (altMin != null && altMin.size() > stNum) {
            double retval = altMin.get(stNum);
            if (Double.toString(retval).equalsIgnoreCase("nan"))
                retval = 0;
            return retval;
        } else {
            return Invalid_Value;
        }
    }
    
    @Override
    public double getUpperAltitude(int stNum) {
        if (altMax != null && altMax.size() > stNum) {
            double retval = altMax.get(stNum);
            if (Double.toString(retval).equalsIgnoreCase("nan"))
                retval = 0;
            return retval;
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public String getTimeEnd(int stNum) {
        try {
            if (tsProfileData != null) {
                DateTime curTime = null;
                DateTime dtEnd = null;
                StationProfileFeature sPFeature = tsProfileData.getStationProfileFeature(tsStationList.get(stNum));
                List<Date> times = sPFeature.getTimes();
                dtEnd = new DateTime(times.get(0), chrono);
                //check the dates
                for (int j = 0; j < times.size(); j++) {
                    curTime = new DateTime(times.get(j), chrono);

                    if (curTime.isAfter(dtEnd)) {
                        dtEnd = curTime;
                    }
                }
                return (df.toDateTimeStringISO(dtEnd.toDate()));
            }
        } catch (IOException ex) {
            Logger.getLogger(TimeSeriesProfile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ERROR_NULL_DATE;
    }

    @Override
    public String getTimeBegin(int stNum) {
        try {
            if (tsProfileData != null) {
                DateTime curTime = null;
                DateTime dtStart = null;
                StationProfileFeature sPFeature = tsProfileData.getStationProfileFeature(tsStationList.get(stNum));
                List<Date> times = sPFeature.getTimes();
                dtStart = new DateTime(times.get(0), chrono);
                //check the dates
                for (int j = 0; j < times.size(); j++) {
                    curTime = new DateTime(times.get(j), chrono);
                    if (curTime.isBefore(dtStart)) {
                        dtStart = curTime;
                    }
                }
                return (df.toDateTimeStringISO(dtStart.toDate()));
            }
        } catch (IOException ex) {
            Logger.getLogger(TimeSeriesProfile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ERROR_NULL_DATE;
    }

    @Override
    public String getDescription(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<String> getLocationsString(int stNum) {
        List<String> retval = new ArrayList<String>();
        retval.add(this.getLowerLat(stNum) + " " + this.getLowerLon(stNum));
        return retval;
    }
}
