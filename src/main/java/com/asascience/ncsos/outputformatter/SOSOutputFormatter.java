/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import java.io.Writer;

/**
 *
 * @author SCowan
 */

/// class contains necessary info for finding a data slice in a data set, then 
/// saving found information. Used primarily by GRID datasets
class DataSlice {
    private double latitude, longitude, depth;
    private String eventTime;
    private float[] dataValue;
    private int stationNumber;
    
    public DataSlice(double latitude, double longitude, double depth, String eventTime, float[] dataValue) {
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
    public Double getLatitude() {
        return latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public Double getDepth() {
        return depth;
    }
    public Float[] getDataValues() {
        if (dataValue != null) {
            Float[] retval = new Float[dataValue.length];
            for(int i=0;i<dataValue.length;i++) {
                retval[i] = dataValue[i];
            }
            return retval;
        }
        return null;
    }
}

/**
 * Provides common functions for classes that define the response outputs for various
 * SOS requests. Most importantly, provides a common end point for writing the response
 * to a writer provided by the controller.
 * @author SCowan
 * @version 1.0.0
 */
public interface SOSOutputFormatter {
    
    /**
     * Adds data from a formatted string to some container defined in the individual formatters.
     * @param dataFormattedString a csv string that usually follows the format of key=value,key1=value1,key2=value2,etc
     *  'value' can be csvs as well, allowing for multiple values per key
     */
    public void AddDataFormattedStringToInfoList(String dataFormattedString);
    /**
     * Empties the container defined in the individual formatters. Usually something like: infoList = null;
     */
    public void EmtpyInfoList();
    /**
     * Sets up the outputter to write an exception when writeOutput is invoked.
     * @param message - message to display to the user
     */
    public void setupExceptionOutput(String message);
    /**
     * Writes prepared output to the writer (usually will be a response stream from a http request
     * @param writer the stream where the output will be written to.
     */
    public void writeOutput(Writer writer);
}
