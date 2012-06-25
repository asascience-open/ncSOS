/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncSOS.outputFormatters;

/**
 *
 * @author SCowan
 */

/// class contains necessary info for finding a data slice in a data set, then 
/// saving found information. Used primarily by GRID datasets
class DataSlice {
    private double latitude, longitude, depth;
    private String eventTime;
    private float dataValue;
    private int stationNumber;
    
    public DataSlice(double latitude, double longitude, double depth, String eventTime, float dataValue) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.depth = depth;
        this.eventTime = eventTime;
        this.dataValue = dataValue;
    }
    
    public void setStationNumber(int stNum) {
        this.stationNumber = stNum;
    }
    public int getStationNumber() {
        return this.stationNumber;
    }
    
    public String getEventTime() {
        return eventTime;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public double getDepth() {
        return depth;
    }
    public float getDataValue() {
        return dataValue;
    }
}

public interface SOSOutputFormatter {
    
    // Interface methods
//    public void AddToInfoList(double latitude, double longitude, double depth, float dataValue, String eventtime);
    public void AddDataFormattedStringToInfoList(String dataFormattedString);
    public void EmtpyInfoList();
    public void outputException(String message);
    public void writeObservationsFromInfoList();
}
