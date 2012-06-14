package thredds.server.sos.CDMClasses;

import java.io.IOException;
import java.util.List;
import ucar.unidata.geoloc.Station;
import org.w3c.dom.Document;

/**
 * @author abird
 * @version 1.0
 *
 * Interface used for the CDM Feature Types
 *
 */
public interface iStationData {
 
    //sets that CDM data
    void setData(Object featureCollection) throws IOException;
    
    //checks the lat lon boundaries for a given station list
    void checkLatLonBoundaries(List<Station> stationList, int i);
    
    //returns the list of station names
    List<String> getStationNames();
    
    //Does what is says on the tin
    double getBoundUpperLon();
    
    //Does what is says on the tin
    double getBoundUpperLat();
    
    //Does what is says on the tin
    double getBoundLowerLon();
    
    //Does what is says on the tin
    double getBoundLowerLat();
    
    //Does what is says on the tin
    String getBoundTimeBegin();
    
    //Does what is says on the tin
    String getBoundTimeEnd();
    
    //Does what is says on the tin
    void setStartDate(String startDateStr);
    
    //Does what is says on the tin
    void setEndDate(String endDateStr);
    
    //initializes the upper and lower lon lat boundaries
    void setInitialLatLonBounaries(List<Station> tsStationList);
    
    //Does what is says on the tin
    void setNumberOfStations(int numOfStations);
    
    //Does what is says on the tin
    int getNumberOfStations();
    
    //call to actually get the data response in the form of a create then add
    String getDataResponse(int stNum);
    
    //Does what is says on the tin
    String getStationName(int idNum);
    
    //Does what is says on the tin
    double getLowerLat(int stNum);
    
    //Does what is says on the tin
    double getLowerLon(int stNum);
    
    //Does what is says on the tin
    double getUpperLat(int stNum);
    
    //Does what is says on the tin
    double getUpperLon(int stNum);
    
    //Does what is says on the tin
    String getTimeEnd(int stNum);
    
    //Does what is says on the tin
    String getTimeBegin(int stNum);

    public String getDescription(int stNum);
       
    
}
