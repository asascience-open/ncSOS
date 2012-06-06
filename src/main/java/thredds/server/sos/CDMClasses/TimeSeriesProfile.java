/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.CDMClasses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.w3c.dom.Document;
import thredds.server.sos.getObs.SOSObservationOffering;
import thredds.server.sos.service.SOSBaseRequestHandler;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.ProfileFeature;
import ucar.nc2.ft.StationProfileFeature;
import ucar.nc2.ft.StationProfileFeatureCollection;
import ucar.nc2.units.DateFormatter;
import ucar.unidata.geoloc.Station;

/**
 * RPS - ASA
 * @author abird
 * @version 
 *
 * handles TIMES SERIES PROFILE CDM DATA TYPE
 *
 */
public class TimeSeriesProfile extends baseCDMClass implements iStationData {

     private StationProfileFeatureCollection tsProfileData;
    private List<Station> tsStationList;
    private final ArrayList<String> eventTimes;
    private final String[] variableNames;

    public TimeSeriesProfile(String[] stationName, String[] eventTime, String[] variableNames) {

        startDate = null;
        endDate = null;
        this.variableNames = variableNames;
        this.reqStationNames = new ArrayList<String>();
        reqStationNames.addAll(Arrays.asList(stationName));
        this.eventTimes = new ArrayList<String>();
        eventTimes.addAll(Arrays.asList(eventTime));

    }
    
    /**
     * gets the timeseriesprofile response for the getcaps request
     * @param featureCollection1
     * @param document
     * @param featureOfInterestBase
     * @param GMLBase
     * @param format
     * @param observedPropertyList
     * @return
     * @throws IOException 
     */
    public static Document getCapsResponse(StationProfileFeatureCollection featureCollection1, Document document, String featureOfInterestBase, String GMLBase, String format, List<String> observedPropertyList) throws IOException {
        StationProfileFeature stationProfileFeature = null;
        SOSObservationOffering newOffering = null;
        DateFormatter timePeriodFormatter = new DateFormatter();
        for (Station station : featureCollection1.getStations()) {
            String stationName = station.getName();
            String stationLat = SOSBaseRequestHandler.formatDegree(station.getLatitude());
            String stationLon = SOSBaseRequestHandler.formatDegree(station.getLongitude());
            newOffering = new SOSObservationOffering();
            newOffering.setObservationStationID(stationName);
            newOffering.setObservationStationLowerCorner(stationLat, stationLon);
            newOffering.setObservationStationUpperCorner(stationLat, stationLon);
            StationProfileFeature feature = featureCollection1.getStationProfileFeature(station);
            //feature.calcBounds();
            stationProfileFeature = featureCollection1.getStationProfileFeature(station);
            List<Date> times = stationProfileFeature.getTimes();
            newOffering.setObservationTimeBegin(timePeriodFormatter.toDateTimeStringISO(times.get(0)));
            newOffering.setObservationTimeEnd(timePeriodFormatter.toDateTimeStringISO(times.get(times.size() - 1)));
            newOffering.setObservationStationDescription(feature.getDescription());
            newOffering.setObservationName(GMLBase + stationName);
            newOffering.setObservationSrsName("EPSG:4326");
            newOffering.setObservationProcedureLink(GMLBase + stationName);
            newOffering.setObservationObserveredList(observedPropertyList);
            newOffering.setObservationFeatureOfInterest(featureOfInterestBase + stationName);
            newOffering.setObservationFormat(format);
            document = CDMUtils.addObsOfferingToDoc(newOffering, document);
        }

        return document;
    }
   

    /****************TIMESERIESPROFILE*******************/
    private String createStationProfileFeature(int stNum) throws IOException {
        StringBuilder builder = new StringBuilder();
        DateFormatter dateFormatter = new DateFormatter();
        List<String> valueList = new ArrayList<String>();


        StationProfileFeature stationProfileFeature = tsProfileData.getStationProfileFeature(tsStationList.get(stNum));
        List<Date> z = stationProfileFeature.getTimes();


        ProfileFeature pf = null;

        //count = 0;
        //if not event time is specified get all the data
        if (eventTimes == null) {
            //test getting items by date(index(0))
            for (int i = 0; i < z.size(); i++) {
                pf = stationProfileFeature.getProfileByDate(z.get(i));
                createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
            }
            return builder.toString();
        } else if (eventTimes.size() > 1) {
            for (int i = 0; i < z.size(); i++) {

                pf = stationProfileFeature.getProfileByDate(z.get(i));

                DateTime dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
                DateTime dtEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);
                DateTime tsDt = new DateTime(pf.getTime(), chrono);

                //find out if current time(searchtime) is one or after startTime
                //same as start
                if (tsDt.isEqual(dtStart)) {
                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
                } //equal end
                else if (tsDt.isEqual(dtEnd)) {
                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
                } //afterStart and before end       
                else if (tsDt.isAfter(dtStart) && (tsDt.isBefore(dtEnd))) {
                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
                }
            }
            return builder.toString();
        } //if the event time is specified get the correct data        
        else {
            for (int i = 0; i < z.size(); i++) {

                if (df.toDateTimeStringISO(z.get(i)).contentEquals(eventTimes.get(0).toString())) {
                    pf = stationProfileFeature.getProfileByDate(z.get(i));
                    createStationProfileData(pf, valueList, dateFormatter, builder, stNum);
                }
            }
            return builder.toString();
        }
    }

    private void createStationProfileData(ProfileFeature pf, List<String> valueList, DateFormatter dateFormatter, StringBuilder builder, int stNum) throws IOException {

        PointFeatureIterator it = pf.getPointFeatureIterator(-1);

        //int num = 0;

        while (it.hasNext()) {
            PointFeature pointFeature = it.next();
            valueList.clear();
            valueList.add(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()));

            for (String variableName : variableNames) {
                valueList.add(pointFeature.getData().getScalarObject(variableName).toString());
            }

            for (int i = 0; i < valueList.size(); i++) {
                builder.append(valueList.get(i));
                if (i < valueList.size() - 1) {
                    builder.append(",");
                }
            }


            //builder.append(tokenJoiner.join(valueList));
            // TODO:  conditional inside loop...
            if (tsProfileData.getStationProfileFeature(tsStationList.get(stNum)).size() > 1) {
                builder.append(" ");
                builder.append("\n");
            }
        }
    }

    /**
     * sets the time series profile data
     */
    @Override
    public void setData(Object featureProfileCollection) throws IOException {
        this.tsProfileData = (StationProfileFeatureCollection) featureProfileCollection;
        tsStationList = tsProfileData.getStations(reqStationNames);

        setNumberOfStations(tsStationList.size());

        DateTime curTime;
        DateTime dtStart = null;
        DateTime dtEnd = null;
        if (tsStationList.size() > 0) {


            for (int i = 0; i < tsStationList.size(); i++) {
                StationProfileFeature sPFeature = tsProfileData.getStationProfileFeature(tsStationList.get(i));
                List<Date> times = sPFeature.getTimes();

                if (i == 0) {
                    setInitialLatLonBounaries(tsStationList);
                    dtStart = new DateTime(times.get(0), chrono);
                    dtEnd = new DateTime(times.get(0), chrono);
                } else {
                    checkLatLonBoundaries(tsStationList, i);
                }

                //check the dates
                for (int j = 0; j < times.size(); j++) {
                    curTime = new DateTime(times.get(j), chrono);

                    if (curTime.isBefore(dtStart)) {
                        dtStart = curTime;
                    } else if (curTime.isAfter(dtEnd)) {
                        dtEnd = curTime;
                    }
                }
            }
            setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
            setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));

        }
    }

    @Override
    public void setInitialLatLonBounaries(List<Station> tsStationList) {
        upperLat = tsStationList.get(0).getLatitude();
        lowerLat = tsStationList.get(0).getLatitude();
        upperLon = tsStationList.get(0).getLongitude();
        lowerLon = tsStationList.get(0).getLongitude();
    }

    @Override
    public String getDataResponse(int stNum) {
        try {
            if (tsProfileData != null) {
                return createStationProfileFeature(stNum);
            }
        } catch (IOException ex) {
            Logger.getLogger(TimeSeriesProfile.class.getName()).log(Level.SEVERE, null, ex);
            return DATA_RESPONSE_ERROR + TimeSeries.class;
        }
        return DATA_RESPONSE_ERROR + TimeSeries.class;
    }

    @Override
    public String getStationName(int idNum) {
        if (tsProfileData != null) {
            return (tsStationList.get(idNum).getName());
        } else {
            return Invalid_Station;
        }
    }

    @Override
    public double getLowerLat(int stNum) {
        if (tsProfileData != null) {
            return (tsStationList.get(stNum).getLatitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getLowerLon(int stNum) {
        if (tsProfileData != null) {
            return (tsStationList.get(stNum).getLongitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLat(int stNum) {
        if (tsProfileData != null) {
            return (tsStationList.get(stNum).getLatitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLon(int stNum) {
        if (tsProfileData != null) {
            return (tsStationList.get(stNum).getLongitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public String getTimeEnd(int stNum) {
        try {
            if (tsProfileData != null) {
                DateTime curTime = null;
                DateTime dtEnd = null;
                StationProfileFeature sPFeature = tsProfileData.getStationProfileFeature(tsStationList.get(stNum));
                List<Date> times = sPFeature.getTimes();
                dtEnd = new DateTime(times.get(0), chrono);
                //check the dates
                for (int j = 0; j < times.size(); j++) {
                    curTime = new DateTime(times.get(j), chrono);

                    if (curTime.isAfter(dtEnd)) {
                        dtEnd = curTime;
                    }
                }
                return (df.toDateTimeStringISO(dtEnd.toDate()));
            }
        } catch (IOException ex) {
            Logger.getLogger(TimeSeriesProfile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ERROR_NULL_DATE;
    }

    @Override
    public String getTimeBegin(int stNum) {
        try {
            if (tsProfileData != null) {
                DateTime curTime = null;
                DateTime dtStart = null;
                StationProfileFeature sPFeature = tsProfileData.getStationProfileFeature(tsStationList.get(stNum));
                List<Date> times = sPFeature.getTimes();
                dtStart = new DateTime(times.get(0), chrono);
                //check the dates
                for (int j = 0; j < times.size(); j++) {
                    curTime = new DateTime(times.get(j), chrono);
                    if (curTime.isBefore(dtStart)) {
                        dtStart = curTime;
                    }
                }
                return (df.toDateTimeStringISO(dtStart.toDate()));
            }
        } catch (IOException ex) {
            Logger.getLogger(TimeSeriesProfile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ERROR_NULL_DATE;
    }

    @Override
    public String getDescription(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
