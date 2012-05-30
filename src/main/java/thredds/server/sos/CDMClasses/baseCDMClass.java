/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.CDMClasses;

import java.util.List;
import org.joda.time.Chronology;
import org.joda.time.chrono.ISOChronology;
import org.w3c.dom.Document;
import ucar.nc2.units.DateFormatter;
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
    public List<String> reqStationNames;
    public int numberOfStations;
    public static final String DATA_RESPONSE_ERROR = "Data Response IO Error: ";
    public static final String ERROR_NULL_DATE = "ERROR NULL Date!!!!";
    public static final int Invalid_Value = -9999999;
    public static final String Invalid_Station = "INVALID_ST";
    Chronology chrono = ISOChronology.getInstance();
    DateFormatter df = new DateFormatter();
    
    
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
    
}
