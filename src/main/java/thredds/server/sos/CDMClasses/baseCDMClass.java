/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.CDMClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ucar.unidata.geoloc.Station;

/**
 * @author abird
 * @version 
 *
 * 
 *
 */
public abstract class baseCDMClass implements iStationData {

    public double upperLon;
    public double lowerLat;
    public double lowerLon;
    public double upperLat;
    public String startDate;
    public String endDate;
    public List<String> stationNames;
    private int numberOfStations;

    @Override
    public void checkLatLonBoundaries(List<Station> stationList, int i) {
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
    }

    @Override
    public List<String> getStationNames() {
        return stationNames;
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
}
