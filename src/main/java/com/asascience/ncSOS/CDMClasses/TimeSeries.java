/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.CDMClasses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import thredds.server.sos.getObs.SOSObservationOffering;
import thredds.server.sos.service.SOSBaseRequestHandler;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.units.DateFormatter;
import ucar.nc2.units.DateRange;
import ucar.unidata.geoloc.Station;

/**
 * @author abird
 * @version 
 *
 * 
 *
 */
public class TimeSeries extends baseCDMClass implements iStationData {

    private StationTimeSeriesFeatureCollection tsData;
    private List<Station> tsStationList;
    private final ArrayList<String> eventTimes;
    private final String[] variableNames;

    public TimeSeries(String[] stationName, String[] eventTime, String[] variableNames) {
        startDate = null;
        endDate = null;
        this.variableNames = variableNames;
        this.reqStationNames = new ArrayList<String>();
        reqStationNames.addAll(Arrays.asList(stationName));
        this.eventTimes = new ArrayList<String>();
        eventTimes.addAll(Arrays.asList(eventTime));
    }

    /**
     * gets the timeseries response for the getcaps request
     * @param featureCollection
     * @param document
     * @param featureOfInterest
     * @param GMLName
     * @param format
     * @param observedPropertyList
     * @return
     * @throws IOException 
     */
    public static Document getCapsResponse(StationTimeSeriesFeatureCollection featureCollection, Document document, String featureOfInterest, String GMLName, String format, List<String> observedPropertyList) throws IOException {
        String stationName = null;
        String stationLat = null;
        String stationLon = null;
        SOSObservationOffering newOffering = null;
        StationTimeSeriesFeature feature = null;

        List<Station> stationList = featureCollection.getStations();
        for (int i = 0; i < stationList.size(); i++) {
            feature = featureCollection.getStationFeature(stationList.get(i));
            stationName = stationList.get(i).getName();
            stationLat = SOSBaseRequestHandler.formatDegree(stationList.get(i).getLatitude());
            stationLon = SOSBaseRequestHandler.formatDegree(stationList.get(i).getLongitude());
            newOffering = new SOSObservationOffering();
            newOffering.setObservationStationID(stationName);
            newOffering.setObservationStationLowerCorner(stationLat, stationLon);
            newOffering.setObservationStationUpperCorner(stationLat, stationLon);

            // Code that causes slow issues
            /*
            if (stationList.size() < 75) {
            feature.calcBounds();
            newOffering.setObservationTimeBegin(feature.getDateRange().getStart().toDateTimeStringISO());
            newOffering.setObservationTimeEnd(feature.getDateRange().getEnd().toDateTimeStringISO());
            }
             * 
             */

            try {
                feature.calcBounds();
                newOffering.setObservationTimeBegin(feature.getDateRange().getStart().toDateTimeStringISO());
                newOffering.setObservationTimeEnd(feature.getDateRange().getEnd().toDateTimeStringISO());
            } catch (Exception e) {
            }
            //END of slow issue code

            newOffering.setObservationStationDescription(feature.getDescription());
            newOffering.setObservationName(GMLName + stationName);
            newOffering.setObservationSrsName("EPSG:4326");  // TODO? 
            newOffering.setObservationProcedureLink(GMLName + stationName);
            newOffering.setObservationObserveredList(observedPropertyList);
            newOffering.setObservationFeatureOfInterest(featureOfInterest + stationName);
            newOffering.setObservationFormat(format);

            document = CDMUtils.addObsOfferingToDoc(newOffering, document);
        }

        return document;
    }

    /*******************TIMSERIES*************************/
    private String createTimeSeriesData(int stNum) throws IOException {
        //create the iterator for the feature
        PointFeatureIterator iterator = tsData.getStationFeature(tsStationList.get(stNum)).getPointFeatureIterator(-1);

        //create the string builders and other things needed
        StringBuilder builder = new StringBuilder();
        DateFormatter dateFormatter = new DateFormatter();
        List<String> valueList = new ArrayList<String>();
        //Joiner tokenJoiner = Joiner.on(',');
        //int count = 0;

        while (iterator.hasNext()) {
            PointFeature pointFeature = iterator.next();

            //if no event time
            if (eventTimes == null) {
                createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
                //count = (stationTimeSeriesFeature.size());
            } //if bounded event time        
            else if (eventTimes.size() > 1) {
                parseMultiTimeEventTimeSeries(df, chrono, pointFeature, valueList, dateFormatter, builder, stNum);
            } //if single event time        
            else {
                if (eventTimes.get(0).contentEquals(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()))) {
                    createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
                }
            }
        }
        //setCount(count);
        return builder.toString();
    }

    private void createTimeSeriesData(List<String> valueList, DateFormatter dateFormatter, PointFeature pointFeature, StringBuilder builder, int stNum) throws IOException {
        //count++;
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
        if (tsData.getStationFeature(tsStationList.get(stNum)).size() > 1) {
            builder.append(" ");
            builder.append("\n");
        }
    }

    public void parseMultiTimeEventTimeSeries(DateFormatter df, Chronology chrono, PointFeature pointFeature, List<String> valueList, DateFormatter dateFormatter, StringBuilder builder, int stNum) throws IOException {
        //get start/end date based on iso date format date        

        DateTime dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
        DateTime dtEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);
        DateTime tsDt = new DateTime(pointFeature.getObservationTimeAsDate(), chrono);

        //find out if current time(searchtime) is one or after startTime
        //same as start
        if (tsDt.isEqual(dtStart)) {
            createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
        } //equal end
        else if (tsDt.isEqual(dtEnd)) {
            createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
        } //afterStart and before end       
        else if (tsDt.isAfter(dtStart) && (tsDt.isBefore(dtEnd))) {
            createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, stNum);
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
    public void setData(Object featureCollection) throws IOException {
        this.tsData = (StationTimeSeriesFeatureCollection) featureCollection;
        tsStationList = tsData.getStations(reqStationNames);

        setNumberOfStations(tsStationList.size());

        if (tsStationList.size() > 0) {
            DateTime dtStart = null;
            DateTime dtEnd = null;
            DateTime dtStartt = null;
            DateTime dtEndt = null;
            DateRange dateRange = null;
            for (int i = 0; i < tsStationList.size(); i++) {
                //set it on the first one
                //calc bounds in loop
                tsData.getStationFeature(tsStationList.get(i)).calcBounds();
                if (i == 0) {
                    setInitialLatLonBounaries(tsStationList);

                    dateRange = tsData.getStationFeature(tsStationList.get(0)).getDateRange();
                    dtStart = new DateTime(dateRange.getStart().getDate(), chrono);
                    dtEnd = new DateTime(dateRange.getEnd().getDate(), chrono);
                } else {
                    dateRange = tsData.getStationFeature(tsStationList.get(i)).getDateRange();
                    dtStartt = new DateTime(dateRange.getStart().getDate(), chrono);
                    dtEndt = new DateTime(dateRange.getEnd().getDate(), chrono);
                    if (dtStartt.isBefore(dtStart)) {
                        dtStart = dtStartt;
                    }
                    if (dtEndt.isAfter(dtEnd)) {
                        dtEnd = dtEndt;
                    }
                    checkLatLonBoundaries(tsStationList, i);
                }
            }
            setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
            setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));
        }
    }

    @Override
    public String getDataResponse(int stNum) {
        try {
            if (tsData != null) {
                return createTimeSeriesData(stNum);
            }
        } catch (IOException ex) {
            Logger.getLogger(TimeSeries.class.getName()).log(Level.SEVERE, null, ex);
            return DATA_RESPONSE_ERROR + TimeSeries.class;
        }
        return DATA_RESPONSE_ERROR + TimeSeries.class;
    }

    @Override
    public String getStationName(int idNum) {
        if (tsData != null) {
            return (tsStationList.get(idNum).getName());
        } else {
            return Invalid_Station;
        }
    }

    @Override
    public double getLowerLat(int stNum) {
        if (tsData != null) {
            return (tsStationList.get(stNum).getLatitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getLowerLon(int stNum) {
        if (tsData != null) {
            return (tsStationList.get(stNum).getLongitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLat(int stNum) {
        if (tsData != null) {
            return (tsStationList.get(stNum).getLatitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLon(int stNum) {
        if (tsData != null) {
            return (tsStationList.get(stNum).getLongitude());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public String getTimeEnd(int stNum) {
        try {
            if (tsData != null) {
                tsData.getStationFeature(tsStationList.get(stNum)).calcBounds();
                DateRange dateRange = tsData.getStationFeature(tsStationList.get(stNum)).getDateRange();
                DateTime dtEnd = new DateTime(dateRange.getEnd().getDate(), chrono);
                return (df.toDateTimeStringISO(dtEnd.toDate()));
            }
        } catch (IOException ex) {
            Logger.getLogger(TimeSeries.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ERROR_NULL_DATE;
    }

    @Override
    public String getTimeBegin(int stNum) {
        try {
            if (tsData != null) {
                tsData.getStationFeature(tsStationList.get(stNum)).calcBounds();
                DateRange dateRange = tsData.getStationFeature(tsStationList.get(stNum)).getDateRange();
                DateTime dtStart = new DateTime(dateRange.getStart().getDate(), chrono);
                return (df.toDateTimeStringISO(dtStart.toDate()));
            }
        } catch (IOException ex) {
            Logger.getLogger(TimeSeries.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ERROR_NULL_DATE;
    }

    @Override
    public String getDescription(int stNum) {
       return "descrip";
    }
}
