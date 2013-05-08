///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
package com.asascience.ncsos.service;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.joda.time.Chronology;
//import org.joda.time.DateTime;
//import org.joda.time.chrono.ISOChronology;
//import ucar.nc2.ft.*;
//import ucar.nc2.units.DateFormatter;
//import ucar.nc2.units.DateRange;
//import ucar.unidata.geoloc.Station;
//
///**
// * Deprecated - stores the station data, easy to access and use, abstracts complexity of station information
// * @author abird
// * @version Deprecated
// */
//@Deprecated
//public class StationData {
//
//    private List<String> stationNames;
//    private StationTimeSeriesFeatureCollection tsData;
//    private StationProfileFeatureCollection tsProfileData;
//    private ProfileFeatureCollection profileData;
//    private double upperLon;
//    private double lowerLat;
//    private double lowerLon;
//    private double upperLat;
//    private String startDate;
//    private String endDate;
//    private final ArrayList<String> eventTimes;
//    private List<Station> tsStationList;
//    private List<ProfileFeature> profileList;
//    private int numberOfStations;
//    private final String[] variableNames;
//
//    public StationData(String[] stationName, String[] eventTime, String[] variableNames) {
//        startDate = null;
//        endDate = null;
//
//        this.variableNames = variableNames;
//
//        this.stationNames = new ArrayList<String>();
//        stationNames.addAll(Arrays.asList(stationName));
//
//        this.eventTimes = new ArrayList<String>();
//        eventTimes.addAll(Arrays.asList(eventTime));
//
//    }
//
//    public void checkLatLonBoundaries(List<Station> tsStationList, int i) {
//        //LAT?LON PARSING
//        //lat
//        if (tsStationList.get(i).getLatitude() > upperLat) {
//            upperLat = tsStationList.get(i).getLatitude();
//        }
//        if (tsStationList.get(i).getLatitude() < lowerLat) {
//            lowerLat = tsStationList.get(i).getLatitude();
//        }
//        //lon
//        if (tsStationList.get(i).getLongitude() > upperLon) {
//            upperLon = tsStationList.get(i).getLongitude();
//        }
//        if (tsStationList.get(i).getLongitude() < lowerLon) {
//            lowerLon = tsStationList.get(i).getLongitude();
//        }
//    }
//
//    public List<String> getStationNames() {
//        return stationNames;
//    }
//
//    public double getBoundUpperLon() {
//        return upperLon;
//    }
//
//    public double getBoundUpperLat() {
//        return upperLat;
//    }
//
//    public double getBoundLowerLon() {
//        return lowerLon;
//    }
//
//    public double getBoundLowerLat() {
//        return lowerLat;
//    }
//
//    public String getBoundTimeBegin() {
//        return startDate;
//    }
//
//    public String getBoundTimeEnd() {
//        return endDate;
//    }
//
//    public void setStartDate(String startDateStr) {
//        this.startDate = startDateStr;
//    }
//
//    public void setEndDate(String endDateStr) {
//        this.endDate = endDateStr;
//    }
//
//    /**
//     * TIMESERIES
//     * sets the station data (lat/lon/date) boundaries for timeSeries
//     * @param featureCollection 
//     */
//    public void setData(StationTimeSeriesFeatureCollection featureCollection) throws IOException {
//        this.tsData = featureCollection;
//        tsStationList = tsData.getStations(stationNames);
//
//        setNumberOfStations(tsStationList.size());
//
//        if (tsStationList.size() > 0) {
//            Chronology chrono = ISOChronology.getInstance();
//            DateFormatter df = new DateFormatter();
//            DateTime dtStart = null;
//            DateTime dtEnd = null;
//            DateTime dtStartt = null;
//            DateTime dtEndt = null;
//            DateRange dateRange = null;
//            for (int i = 0; i < tsStationList.size(); i++) {
//                //set it on the first one
//                //calc bounds in loop
//                tsData.getStationFeature(tsStationList.get(i)).calcBounds();
//                if (i == 0) {
//                    setInitialLatLonBounaries(tsStationList);
//
//                    dateRange = tsData.getStationFeature(tsStationList.get(0)).getDateRange();
//                    dtStart = new DateTime(dateRange.getStart().getDate(), chrono);
//                    dtEnd = new DateTime(dateRange.getEnd().getDate(), chrono);
//                } else {
//                    dateRange = tsData.getStationFeature(tsStationList.get(i)).getDateRange();
//                    dtStartt = new DateTime(dateRange.getStart().getDate(), chrono);
//                    dtEndt = new DateTime(dateRange.getEnd().getDate(), chrono);
//                    if (dtStartt.isBefore(dtStart)) {
//                        dtStart = dtStartt;
//                    }
//                    if (dtEndt.isAfter(dtEnd)) {
//                        dtEnd = dtEndt;
//                    }
//                    checkLatLonBoundaries(tsStationList, i);
//                }
//            }
//            setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
//            setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));
//        }
//    }
//
//    /**
//     * TIMESERIES PROFILE
//     * sets the station data
//     * @param featureProfileCollection 
//     */
//    public void setData(StationProfileFeatureCollection featureProfileCollection) throws IOException {
//        this.tsProfileData = featureProfileCollection;
//        tsStationList = tsProfileData.getStations(stationNames);
//
//        setNumberOfStations(tsStationList.size());
//
//        Chronology chrono = ISOChronology.getInstance();
//        DateTime curTime;
//        DateTime dtStart = null;
//        DateTime dtEnd = null;
//        if (tsStationList.size() > 0) {
//
//
//            for (int i = 0; i < tsStationList.size(); i++) {
//                StationProfileFeature sPFeature = tsProfileData.getStationProfileFeature(tsStationList.get(i));
//                List<Date> times = sPFeature.getTimes();
//
//                if (i == 0) {
//                    setInitialLatLonBounaries(tsStationList);
//                    dtStart = new DateTime(times.get(0), chrono);
//                    dtEnd = new DateTime(times.get(0), chrono);
//                } else {
//                    checkLatLonBoundaries(tsStationList, i);
//                }
//
//                //check the dates
//                for (int j = 0; j < times.size(); j++) {
//                    curTime = new DateTime(times.get(j), chrono);
//
//                    if (curTime.isBefore(dtStart)) {
//                        dtStart = curTime;
//                    } else if (curTime.isAfter(dtEnd)) {
//                        dtEnd = curTime;
//                    }
//                }
//            }
//
//            DateFormatter df = new DateFormatter();
//            setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
//            setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));
//
//        }
//    }
//
//    public void setInitialLatLonBounaries(List<Station> tsStationList) {
//        upperLat = tsStationList.get(0).getLatitude();
//        lowerLat = tsStationList.get(0).getLatitude();
//        upperLon = tsStationList.get(0).getLongitude();
//        lowerLon = tsStationList.get(0).getLongitude();
//    }
//
//    /**
//     * sets the station data
//     * @param profilePeatureCollection 
//     */
//    public void setData(ProfileFeatureCollection profilePeatureCollection) throws IOException {
//        this.profileData = profilePeatureCollection;
//
//        profileList = new ArrayList<ProfileFeature>();
//
//        Chronology chrono = ISOChronology.getInstance();
//        DateTime dtSearchStart = null;
//        DateTime dtSearchEnd = null;
//        DateFormatter df = new DateFormatter();
//
//        boolean firstSet = true;
//
//        //check first to see if the event times are not null
//        if (eventTimes != null) {
//            //turn event times in to dateTimes to compare
//            if (eventTimes.size() >= 1) {
//                dtSearchStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
//            }
//            if (eventTimes.size() == 2) {
//
//                dtSearchEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);
//            }
//
//            //temp
//            DateTime dtStart = null;
//            DateTime dtEnd = null;
//            //check
//            DateTime dtStartt = null;
//            DateTime dtEndt = null;
//            String profileID = null;
//
//            while (profileData.hasNext()) {
//                ProfileFeature pFeature = profileData.next();
//                pFeature.calcBounds();
//
//                //scan through the data and get the profile id number
//                PointFeatureIterator pp = pFeature.getPointFeatureIterator(-1);
//                while (pp.hasNext()) {
//                    PointFeature pointFeature = pp.next();
//                    profileID = getProfileIDFromProfile(pointFeature);
//                    //System.out.println(profileID);
//                    break;
//                }
//
//                //scan through the stationname for a match of id
//                for (Iterator<String> it = stationNames.iterator(); it.hasNext();) {
//                    String stName = it.next();
//                    if (stName.equalsIgnoreCase(profileID)) {
//                        profileList.add(pFeature);
//                    }
//                }
//
//                if (profileID == null) {
//                    profileID = "0";
//                    profileList.add(pFeature);
//                }
//
//                if (firstSet) {
//                    upperLat = pFeature.getLatLon().getLatitude();
//                    lowerLat = pFeature.getLatLon().getLatitude();
//                    upperLon = pFeature.getLatLon().getLongitude();
//                    lowerLon = pFeature.getLatLon().getLongitude();
//
//                    dtStart = new DateTime(pFeature.getTime(), chrono);
//                    dtEnd = new DateTime(pFeature.getTime(), chrono);
//                    firstSet = false;
//                } else {
//
//                    dtStartt = new DateTime(pFeature.getTime(), chrono);
//                    dtEndt = new DateTime(pFeature.getTime(), chrono);
//
//                    if (dtStartt.isBefore(dtStart)) {
//                        dtStart = dtStartt;
//                    }
//                    if (dtEndt.isAfter(dtEnd)) {
//                        dtEnd = dtEndt;
//                    }
//
//                    if (pFeature.getLatLon().getLatitude() > upperLat) {
//                        upperLat = pFeature.getLatLon().getLatitude();
//                    }
//                    if (pFeature.getLatLon().getLatitude() < lowerLat) {
//                        lowerLat = pFeature.getLatLon().getLatitude();
//                    }
//                    //lon
//                    if (pFeature.getLatLon().getLongitude() > upperLon) {
//                        upperLon = pFeature.getLatLon().getLongitude();
//                    }
//                    if (pFeature.getLatLon().getLongitude() < lowerLon) {
//                        lowerLon = pFeature.getLatLon().getLongitude();
//                    }
//                }
//            }
//
//            setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
//            setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));
//            if (stationNames != null) {
//                setNumberOfStations(stationNames.size());
//            }
//        }
//    }
//
//    /**
//     * get the list of stations set
//     * @return 
//     */
//    private List<Station> getStationList() {
//        return tsStationList;
//    }
//
//    /**
//     * get the list of profiles set
//     * @return 
//     */
//    private List<ProfileFeature> getProfileList() {
//        return profileList;
//    }
//
//    /**
//     * set the number of stations
//     * @param numOfStations 
//     */
//    private void setNumberOfStations(int numOfStations) {
//        this.numberOfStations = numOfStations;
//    }
//
//    /**
//     * get the number of stations (used for multiple obs offerings)
//     * @return 
//     */
//    public int getNumberOfStations() {
//        return numberOfStations;
//    }
//
//    public String getDataResponse(int stNum) {
//
//        try {
//            if (tsData != null) {
//                return createTimeSeriesData(stNum);
//            } else if (tsProfileData != null) {
//                return createStationProfileFeature(stNum);
//            } else if (profileData != null) {
//                return createProfileFeature(stNum);
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(StationData.class.getName()).log(Level.SEVERE, null, ex);
//            return "IO ERROR";
//        }
//        return "ERRRRRRRRRROR!";
//    }
//
//    /*******************TIMSERIES*************************/
//    private String createTimeSeriesData(int stNum) throws IOException {
//        DateFormatter df = new DateFormatter();
//
//        //create the iterator for the feature
//        PointFeatureIterator iterator = tsData.getStationFeature(tsStationList.get(stNum)).getPointFeatureIterator(-1);
//
//        //create the string builders and other things needed
//        StringBuilder builder = new StringBuilder();
//        DateFormatter dateFormatter = new DateFormatter();
//        List<String> valueList = new ArrayList<String>();
//        //Joiner tokenJoiner = Joiner.on(',');
//        Chronology chrono = ISOChronology.getInstance();
//
//        //int count = 0;
//
//        while (iterator.hasNext()) {
//            PointFeature pointFeature = iterator.next();
//
//            //if no event time
//            if (eventTimes == null) {
//                createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
//                //count = (stationTimeSeriesFeature.size());
//            } //if bounded event time        
//            else if (eventTimes.size() > 1) {
//                parseMultiTimeEventTimeSeries(df, chrono, pointFeature, valueList, dateFormatter, builder, stNum);
//            } //if single event time        
//            else {
//                if (eventTimes.get(0).contentEquals(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()))) {
//                    createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
//                }
//            }
//        }
//        //setCount(count);
//        return builder.toString();
//    }
//
//    private void createTimeSeriesData(List<String> valueList, DateFormatter dateFormatter, PointFeature pointFeature, StringBuilder builder, int stNum) throws IOException {
//        //count++;
//        valueList.clear();
//        valueList.add(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()));
//        for (String variableName : variableNames) {
//            valueList.add(pointFeature.getData().getScalarObject(variableName).toString());
//        }
//     
//        for (int i = 0; i < valueList.size(); i++) {
//            builder.append(valueList.get(i));
//            if (i < valueList.size()-1){
//                builder.append(",");
//            }
//        }
//           
//        //builder.append(tokenJoiner.join(valueList));
//        if (tsData.getStationFeature(tsStationList.get(stNum)).size() > 1) {
//            builder.append(" ");
//            builder.append("\n");
//        }
//    }
//
//    public void parseMultiTimeEventTimeSeries(DateFormatter df, Chronology chrono, PointFeature pointFeature, List<String> valueList, DateFormatter dateFormatter, StringBuilder builder, int stNum) throws IOException {
//        //get start/end date based on iso date format date        
//
//        DateTime dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
//        DateTime dtEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);
//        DateTime tsDt = new DateTime(pointFeature.getObservationTimeAsDate(), chrono);
//
//        //find out if current time(searchtime) is one or after startTime
//        //same as start
//        if (tsDt.isEqual(dtStart)) {
//            createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
//        } //equal end
//        else if (tsDt.isEqual(dtEnd)) {
//            createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
//        } //afterStart and before end       
//        else if (tsDt.isAfter(dtStart) && (tsDt.isBefore(dtEnd))) {
//            createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
//        }
//    }
//
//    /****************TIMESERIESPROFILE*******************/
//    private String createStationProfileFeature(int stNum) throws IOException {
//        StringBuilder builder = new StringBuilder();
//        DateFormatter dateFormatter = new DateFormatter();
//        List<String> valueList = new ArrayList<String>();
//
//
//        StationProfileFeature stationProfileFeature = tsProfileData.getStationProfileFeature(tsStationList.get(stNum));
//        List<Date> z = stationProfileFeature.getTimes();
//
//        DateFormatter df = new DateFormatter();
//        ProfileFeature pf = null;
//
//        //count = 0;
//
//        Chronology chrono = ISOChronology.getInstance();
//        //if not event time is specified get all the data
//        if (eventTimes == null) {
//            //test getting items by date(index(0))
//            for (int i = 0; i < z.size(); i++) {
//                pf = stationProfileFeature.getProfileByDate(z.get(i));
//                createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
//            }
//            return builder.toString();
//        } else if (eventTimes.size() > 1) {
//            for (int i = 0; i < z.size(); i++) {
//
//                pf = stationProfileFeature.getProfileByDate(z.get(i));
//
//                DateTime dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
//                DateTime dtEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);
//                DateTime tsDt = new DateTime(pf.getTime(), chrono);
//
//                //find out if current time(searchtime) is one or after startTime
//                //same as start
//                if (tsDt.isEqual(dtStart)) {
//                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
//                } //equal end
//                else if (tsDt.isEqual(dtEnd)) {
//                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
//                } //afterStart and before end       
//                else if (tsDt.isAfter(dtStart) && (tsDt.isBefore(dtEnd))) {
//                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
//                }
//            }
//            return builder.toString();
//        } //if the event time is specified get the correct data        
//        else {
//            for (int i = 0; i < z.size(); i++) {
//
//                if (df.toDateTimeStringISO(z.get(i)).contentEquals(eventTimes.get(0).toString())) {
//                    pf = stationProfileFeature.getProfileByDate(z.get(i));
//                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
//                }
//            }
//            return builder.toString();
//        }
//    }
//
//    private void createStationProfileData(ProfileFeature pf, List<String> valueList, DateFormatter dateFormatter, StringBuilder builder,  int stNum) throws IOException {
//
//        PointFeatureIterator it = pf.getPointFeatureIterator(-1);
//
//        //int num = 0;
//
//        while (it.hasNext()) {
//            PointFeature pointFeature = it.next();
//            valueList.clear();
//            valueList.add(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()));
//
//            for (String variableName : variableNames) {
//                valueList.add(pointFeature.getData().getScalarObject(variableName).toString());
//            }
//            
//            for (int i = 0; i < valueList.size(); i++) {
//            builder.append(valueList.get(i));
//            if (i < valueList.size()-1){
//                builder.append(",");
//            }
//             }
//            
//            
//            //builder.append(tokenJoiner.join(valueList));
//            if (tsProfileData.getStationProfileFeature(tsStationList.get(stNum)).size() > 1) {
//                builder.append(" ");
//                builder.append("\n");
//            }
//        }
//        //count = count + stationProfileFeature.size();
//        //setCount(count);
//    }
//
//    /*******************PROFILE**************************/
//    private String createProfileFeature(int stNum) throws IOException {
//
//        StringBuilder builder = new StringBuilder();
//        DateFormatter dateFormatter = new DateFormatter();
//        List<String> valueList = new ArrayList<String>();
//
//        //if multi Time
//        if (eventTimes.size() > 1) {
//            Chronology chrono = ISOChronology.getInstance();
//            DateFormatter df = new DateFormatter();
//            //get the profile collection, and loop through
//            //while has next
//
//            for (int i = 0; i < profileList.size(); i++) {
//                //grab the profile
//                ProfileFeature pFeature = profileList.get(i);
//                if (pFeature != null) {
//
//                    //output the name
//                    DateTime dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
//                    DateTime dtEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);
//                    DateTime tsDt = new DateTime(pFeature.getName(), chrono);
//
//                    //find out if current time(searchtime) is one or after startTime
//                    //same as start
//                    if (tsDt.isEqual(dtStart)) {
//                        addProfileData(valueList, dateFormatter, builder, pFeature.getPointFeatureIterator(-1), stNum);
//                    } //equal end
//                    else if (tsDt.isEqual(dtEnd)) {
//                        addProfileData(valueList, dateFormatter, builder, pFeature.getPointFeatureIterator(-1), stNum);
//                    } //afterStart and before end       
//                    else if (tsDt.isAfter(dtStart) && (tsDt.isBefore(dtEnd))) {
//                        addProfileData(valueList, dateFormatter, builder, pFeature.getPointFeatureIterator(-1), stNum);
//                    }
//                    //setCount(pFeature.size());
//
//                }
//            }
//            return builder.toString();
//
//        } //if not multiTime        
//        else {
//            ProfileFeature pFeature = profileList.get(stNum);
//            addProfileData(valueList, dateFormatter, builder, pFeature.getPointFeatureIterator(-1), stNum);
//
//            //setCount(profileF.size());
//            return builder.toString();
//        }
//
//    }
//
//    public void addProfileData(List<String> valueList, DateFormatter dateFormatter, StringBuilder builder,  PointFeatureIterator profileIterator, int stNum) throws IOException {
//        //set the iterator the the correct profile
//        while (profileIterator.hasNext()) {
//            PointFeature pointFeature = profileIterator.next();
//            valueList.clear();
//            valueList.add(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()));
//
//            String profileID = getProfileIDFromProfile(pointFeature);
//            //if there is a profile id use it against the data that is requested
//            if (profileID != null) {
//                //System.out.println(profileID);
//                if (profileID.equalsIgnoreCase(stationNames.get(stNum))) {
//                    addProfileDataToBuilder(valueList, pointFeature, builder);
//                }
//            } else {
//                addProfileDataToBuilder(valueList, pointFeature, builder);
//            }
//        }
//    }
//
//    public void addProfileDataToBuilder(List<String> valueList, PointFeature pointFeature, StringBuilder builder) throws IOException {
//        for (String variableName : variableNames) {
//            valueList.add(pointFeature.getData().getScalarObject(variableName).toString());
//        }
//        
//        for (int i = 0; i < valueList.size(); i++) {
//            builder.append(valueList.get(i));
//            if (i < valueList.size()-1){
//                builder.append(",");
//            }
//        }
//        
//        //builder.append(tokenJoiner.join(valueList));
//        builder.append(" ");
//        builder.append("\n");
//    }
//
//    public static String getProfileIDFromProfile(PointFeature pointFeature) {
//        String profileID = null;
//        //Try and get profileID
//        try {
//            profileID = (pointFeature.getData().getScalarObject("profile").toString());
//        } //if it is not there dont USE IT!,,,,,but maybe warn that it is not there?        
//        catch (Exception e) {
//            //Logger.getLogger(SOSGetObservationRequestHandler.class.getName()).log(Level.INFO, "ERROR PROFILE ID NO AVAILABLE \n Must be single Profile \n", e);
//        }
//        return profileID;
//    }
//
//    ///////////////////////////////////////////////////////////////////////////////////////////////////////
//    
//    /**
//     * get begin time of obs
//     * @param stNum
//     * @return 
//     */
//    public String getTimeBegin(int stNum) {
//        Chronology chrono = ISOChronology.getInstance();
//        DateFormatter df = new DateFormatter();
//        try {
//            if (tsData != null) {
//                tsData.getStationFeature(tsStationList.get(stNum)).calcBounds();
//                DateRange dateRange = tsData.getStationFeature(tsStationList.get(stNum)).getDateRange();
//                DateTime dtStart = new DateTime(dateRange.getStart().getDate(), chrono);
//                return (df.toDateTimeStringISO(dtStart.toDate()));
//
//            } else if (tsProfileData != null) {
//
//                DateTime curTime = null;
//                DateTime dtStart = null;
//                StationProfileFeature sPFeature = tsProfileData.getStationProfileFeature(tsStationList.get(stNum));
//                List<Date> times = sPFeature.getTimes();
//                dtStart = new DateTime(times.get(0), chrono);
//                //check the dates
//                for (int j = 0; j < times.size(); j++) {
//                    curTime = new DateTime(times.get(j), chrono);
//                    if (curTime.isBefore(dtStart)) {
//                        dtStart = curTime;
//                    }
//                }
//                return (df.toDateTimeStringISO(dtStart.toDate()));
//            } else if (profileData != null) {
//                return df.toDateTimeStringISO(profileList.get(stNum).getTime());
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(StationData.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return "ERROR NULL Date!!!!";
//    }
//
//    /**
//     * get end time of obs
//     * @param stNum
//     * @return 
//     */
//    public String getTimeEnd(int stNum) {
//        Chronology chrono = ISOChronology.getInstance();
//        DateFormatter df = new DateFormatter();
//        try {
//            if (tsData != null) {
//                tsData.getStationFeature(tsStationList.get(stNum)).calcBounds();
//                DateRange dateRange = tsData.getStationFeature(tsStationList.get(stNum)).getDateRange();
//                DateTime dtEnd = new DateTime(dateRange.getEnd().getDate(), chrono);
//                return (df.toDateTimeStringISO(dtEnd.toDate()));
//            } else if (tsProfileData != null) {
//
//                DateTime curTime = null;
//                DateTime dtEnd = null;
//                StationProfileFeature sPFeature = tsProfileData.getStationProfileFeature(tsStationList.get(stNum));
//                List<Date> times = sPFeature.getTimes();
//                dtEnd = new DateTime(times.get(0), chrono);
//                //check the dates
//                for (int j = 0; j < times.size(); j++) {
//                    curTime = new DateTime(times.get(j), chrono);
//
//                    if (curTime.isAfter(dtEnd)) {
//                        dtEnd = curTime;
//                    }
//                }
//                return (df.toDateTimeStringISO(dtEnd.toDate()));
//            } else if (profileData != null) {
//                return df.toDateTimeStringISO(profileList.get(stNum).getTime());
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(StationData.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return "ERROR NULL Date!!!!";
//
//    }
//
//    public String getStationName(int idNum) {
//        if (tsData != null) {
//            return (tsStationList.get(idNum).getName());
//        } else if (tsProfileData != null) {
//            return (tsStationList.get(idNum).getName());
//        } else if (profileData != null) {
//            return "PROFILE_" + (stationNames.get(idNum));
//        }
//        return "ERROR NULL Date!!!!";
//    }
//
//    public double getLowerLat(int stNum) {
//        if (tsData != null) {
//            return (tsStationList.get(stNum).getLatitude());
//        } else if (tsProfileData != null) {
//            return (tsStationList.get(stNum).getLatitude());
//        } else if (profileData != null) {
//            return profileList.get(stNum).getLatLon().getLatitude();
//        }
//        return -9999999;
//    }
//
//    public double getLowerLon(int stNum) {
//        if (tsData != null) {
//            return (tsStationList.get(stNum).getLongitude());
//        } else if (tsProfileData != null) {
//            return (tsStationList.get(stNum).getLongitude());
//        } else if (profileData != null) {
//            return profileList.get(stNum).getLatLon().getLongitude();
//        }
//        return -9999999;
//    }
//
//    public double getUpperLat(int stNum) {
//        if (tsData != null) {
//            return (tsStationList.get(stNum).getLatitude());
//        } else if (tsProfileData != null) {
//            return (tsStationList.get(stNum).getLatitude());
//        } else if (profileData != null) {
//            return profileList.get(stNum).getLatLon().getLatitude();
//        }
//        return -9999999;
//    }
//
//    public double getUpperLon(int stNum) {
//        if (tsData != null) {
//            return (tsStationList.get(stNum).getLongitude());
//        } else if (tsProfileData != null) {
//            return (tsStationList.get(stNum).getLongitude());
//        } else if (profileData != null) {
//            return profileList.get(stNum).getLatLon().getLongitude();
//        }
//        return -9999999;
//    }
//}
