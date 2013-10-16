package com.asascience.ncsos.outputformatter;

/**
 *
 * @author SCowan
 */
/// class contains necessary info for finding a data slice in a data set, then
/// saving found information. Used primarily by GRID datasets
public class DataSlice {

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
            for (int i = 0; i < dataValue.length; i++) {
                retval[i] = dataValue[i];
            }
            return retval;
        }
        return null;
    }
}
