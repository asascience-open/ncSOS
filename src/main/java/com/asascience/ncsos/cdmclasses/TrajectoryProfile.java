/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.cdmclasses;

import java.io.IOException;
import java.util.List;
import ucar.unidata.geoloc.Station;

/**
 *
 * @author SCowan
 */
public class TrajectoryProfile extends baseCDMClass implements iStationData {

    /************************
     * iStationData Methods *
     ************************/
    
    public void setData(Object featureCollection) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInitialLatLonBounaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDataResponse(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getStationName(int idNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLowerLat(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLowerLon(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getUpperLat(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getUpperLon(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTimeEnd(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTimeBegin(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
