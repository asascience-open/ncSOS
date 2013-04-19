package com.asascience.ncsos.cdmclasses;

import java.io.IOException;
import java.util.List;
import ucar.unidata.geoloc.Station;

/**
 * Interface used for the CDM Feature Types
 * Provides functional interface for handling various feature type datasets in providing
 * information for Get Capabilities and Get Observation requests.
 * @author abird
 * @version 1.0
 */
public interface iStationData {
 
    /**
     * Initializes data from the featureCollection parameter
     * @param featureCollection FeatureTypeDataSet for the wrapper for the queried dataset
     * @throws IOException 
     */
    public void setData(Object featureCollection) throws IOException;
    
    /**
     * checks the lat lon boundaries for a given station list
     * @param stationList list of stations to be returned for the request
     * @param i station index in list to check
     */
    public void checkLatLonAltBoundaries(List<Station> stationList, int i);
    
    /**
     * @return the list of station names
     */
    public List<String> getStationNames();
    
    /**
     * Does what is says on the tin
     * @return the upper longitude of the bounding box for the set of queried stations
     */
    public double getBoundUpperLon();
    
    /**
     * Does what is says on the tin
     * @return the upper latitude of the bounding box for the set of queried stations
     */
    public double getBoundUpperLat();
    
    /**
     * Does what is says on the tin
     * @return the lower longitude of the bounding box for the set of queried stations
     */
    public double getBoundLowerLon();
    
    /**
     * Does what is says on the tin
     * @return the lower latitude of the bounding box for the set of queried stations
     */
    public double getBoundLowerLat();
    
    /**
     * Does what is says on the tin
     * @return the lower altitude of the bounding box for the set of queried stations
     */
    public double getBoundLowerAlt();
    
    /**
     * Does what is says on the tin
     * @return the upper altitude of the bounding box for the set of queried stations
     */
    public double getBoundUpperAlt();
    
    /**
     * Does what is says on the tin
     * @return the starting time stamp for the set of queried stations
     */
    public String getBoundTimeBegin();
    
    /**
     * Does what is says on the tin
     * @return the final time stamp for the set of queried stations
     */
    public String getBoundTimeEnd();
    
    /**
     * Does what is says on the tin
     * @param startDateStr time stamp to set the starting time stamp of the set of queried stations
     */
    public void setStartDate(String startDateStr);
    
    /**
     * Does what is says on the tin
     * @param endDateStr time stamp to set the ending time stamp of the set of queried stations
     */
    public void setEndDate(String endDateStr);
    
    /**
     * initializes the upper and lower lon lat boundaries
     * @param tsStationList list of queried stations
     */
    public void setInitialLatLonBoundaries(List<Station> tsStationList);
    
    /**
     * Does what is says on the tin
     * @param numOfStations changes the number of stations for the set of queried stations
     */
    public void setNumberOfStations(int numOfStations);
    
    /**
     * Does what is says on the tin
     * @return number of stations in station list
     */
    public int getNumberOfStations();
    
    /**
     * 
     * @param stNum
     * @return 
     */
    public boolean isStationInFinalList(int stNum);
    
    /**
     * call to actually get the data response in the form of a create then add
     * @param stNum station index of the set of queried stations
     * @return String that contains a formatted response of values for Get Observation response
     */
    public String getDataResponse(int stNum);
    
    /**
     * Does what is says on the tin
     * @param idNum station index of the set of queried stations
     * @return station name of station at index idNum
     */
    public String getStationName(int idNum);
    
    /**
     * Does what is says on the tin
     * @param stNum station index of the set of queried stations
     * @return lower latitude of bounding box
     */
    public double getLowerLat(int stNum);
    
    /**
     * Does what is says on the tin
     * @param stNum station index of the set of queried stations
     * @return lower longitude of bounding box
     */
    public double getLowerLon(int stNum);
    
    /**
     * Does what is says on the tin
     * @param stNum station index of the set of queried stations
     * @return upper latitude of bounding box
     */
    public double getUpperLat(int stNum);
    
    /**
     * Does what is says on the tin
     * @param stNum station index of the set of queried stations
     * @return upper longitude of bounding box
     */
    public double getUpperLon(int stNum);
    
    /**
     * Does what is says on the tin
     * @param stNum station index of the set of queried stations
     * @return lower altitude of bounding box
     */
    public double getLowerAltitude(int stNum);
    
    /**
     * Does what is says on the tin
     * @param stNum station index of the set of queried stations
     * @return upper altitude of bounding box
     */
    public double getUpperAltitude(int stNum);
    
    /**
     * Does what is says on the tin
     * @param stNum station index of the set of queried stations
     * @return timestamp for final event time of station
     */
    public String getTimeEnd(int stNum);
    
    /**
     * Does what is says on the tin
     * @param stNum station index of the set of queried stations
     * @return timestamp for initial event time of station
     */
    public String getTimeBegin(int stNum);

    /**
     * Does what is says on the tin
     * @param stNum station index of the set of queried stations
     * @return description of station
     */
    public String getDescription(int stNum);
    
    /**
     * Retrieves all of the lat-lon coordinates for the station.
     * @param stNum station number
     * @return a List of Strings that follow "lat lon"
     */
    public List<String> getLocationsString(int stNum);
       
    
}
