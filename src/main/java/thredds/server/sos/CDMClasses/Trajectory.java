package thredds.server.sos.CDMClasses;

import java.io.IOException;
import java.util.List;
import ucar.unidata.geoloc.Station;

/**
 * RPS - ASA
 * @author abird
 * @version 
 *
 * 
 *
 */
public class Trajectory extends baseCDMClass implements iStationData {

    @Override
    public void setData(Object featureCollection) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setInitialLatLonBounaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDataResponse(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getStationName(int idNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getLowerLat(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getLowerLon(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getUpperLat(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getUpperLon(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTimeEnd(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTimeBegin(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
