/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.cdmclasses;

import java.util.List;
import org.joda.time.Chronology;
import org.joda.time.chrono.ISOChronology;
import ucar.nc2.units.DateFormatter;
import ucar.unidata.geoloc.Station;

/**
 * @author abird
 * @version 1.0.0
 */
public abstract class baseCDMClass implements iStationData {

    protected double upperLon, lowerLon, lowerLat, upperLat, lowerAlt, upperAlt;
    protected String startDate;
    protected String endDate;
    protected List<String> reqStationNames;
    protected int numberOfStations;
    protected static final String DATA_RESPONSE_ERROR = "Data Response IO Error: ";
    protected static final String ERROR_NULL_DATE = "ERROR NULL Date!!!!";
    protected static final int Invalid_Value = -9999999;
    protected static final String Invalid_Station = "INVALID_ST";
    protected Chronology chrono = ISOChronology.getInstance();
    protected DateFormatter df = new DateFormatter();
    
    protected static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(baseCDMClass.class);
    
    
    @Override
    public void checkLatLonAltBoundaries(List<Station> stationList, int i) {
        //LAT?LON PARSING
        //lat
        if (stationList.get(i).getLatitude() > upperLat) {
            upperLat = stationList.get(i).getLatitude();
        }
        if (stationList.get(i).getLatitude() < lowerLat) {
            lowerLat = stationList.get(i).getLatitude();
        }
        //lon
        if (stationList.get(i).getLongitude() > upperLon) {
            upperLon = stationList.get(i).getLongitude();
        }
        if (stationList.get(i).getLongitude() < lowerLon) {
            lowerLon = stationList.get(i).getLongitude();
        }
        // alt
        if (stationList.get(i).getAltitude() > upperAlt) {
            upperAlt = stationList.get(i).getAltitude();
        }
        if (stationList.get(i).getAltitude() < lowerAlt) {
            lowerAlt = stationList.get(i).getAltitude();
        }
    }

    @Override
    public List<String> getStationNames() {
        return reqStationNames;
    }

    @Override
    public double getBoundUpperLon() {
        return upperLon;
    }

    @Override
    public double getBoundUpperLat() {
        return upperLat;
    }

    @Override
    public double getBoundLowerLon() {
        return lowerLon;
    }

    @Override
    public double getBoundLowerLat() {
        return lowerLat;
    }

    @Override
    public String getBoundTimeBegin() {
        return startDate;
    }

    @Override
    public String getBoundTimeEnd() {
        return endDate;
    }

    @Override
    public void setStartDate(String startDateStr) {
        this.startDate = startDateStr;
    }

    @Override
    public void setEndDate(String endDateStr) {
        this.endDate = endDateStr;
    }

    @Override
    public void setNumberOfStations(int numOfStations) {
        this.numberOfStations = numOfStations;
    }

    @Override
    public int getNumberOfStations() {
        return numberOfStations;
    }
    
    @Override
    public boolean isStationInFinalList(int stNum) {
        return true;
    }
    
    @Override
    public double getLowerAltitude(int stNum) {
        return 0;
    }
    @Override
    public double getUpperAltitude(int stNum) {
        return 0;
    }
    
    @Override
    public double getBoundLowerAlt() {
        if (Double.toString(lowerAlt).contains("fin") || Double.toString(lowerAlt).equalsIgnoreCase("nan"))
            return 0;
        return lowerAlt;
    }
    
    @Override
    public double getBoundUpperAlt() {
        if (Double.toString(lowerAlt).contains("fin") || Double.toString(upperAlt).equalsIgnoreCase("nan"))
            return 0;
        return upperAlt;
    }
    
}
