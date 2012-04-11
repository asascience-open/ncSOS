/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.CDMClasses;

import java.io.IOException;
import java.util.List;
import ucar.nc2.dt.StationCollection;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.ProfileFeature;
import ucar.unidata.geoloc.Station;

/**
 * @author abird
 * @version 
 *
 * 
 *
 */
public interface iStationData {
 
    void setData(Object featureCollection) throws IOException;
    
    void checkLatLonBoundaries(List<Station> tsStationList, int i);
    
    List<String> getStationNames();
    
    double getBoundUpperLon();
    
    double getBoundUpperLat();
    
    double getBoundLowerLon();
    
    double getBoundLowerLat();
    
    String getBoundTimeBegin();
    
    String getBoundTimeEnd();
    
    void setStartDate(String startDateStr);
    
    void setEndDate(String endDateStr);
    
    void setInitialLatLonBounaries(List<Station> tsStationList);
    
    void setNumberOfStations(int numOfStations);
    
    int getNumberOfStations();
    
    String getDataResponse(int stNum);
    
    String getStationName(int idNum);
    
    double getLowerLat(int stNum);
    
    double getLowerLon(int stNum);
    
    double getUpperLat(int stNum);
    
    double getUpperLon(int stNum);
    
    String getTimeEnd(int stNum);
    
    String getTimeBegin(int stNum);
    
}
