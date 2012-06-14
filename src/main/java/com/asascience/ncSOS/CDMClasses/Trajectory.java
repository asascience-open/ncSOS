package thredds.server.sos.CDMClasses;

import org.joda.time.DateTime;
import ucar.unidata.geoloc.Station;
import org.w3c.dom.Document;
import thredds.server.sos.getObs.SOSObservationOffering;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.TrajectoryFeature;
import ucar.nc2.ft.TrajectoryFeatureCollection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.nc2.units.DateFormatter;

/**
 * RPS - ASA
 * @author abird
 * @version 
 *
 *handles TRAJECTORY CDM DATA TYPE 
 *
 */
public class Trajectory extends baseCDMClass implements iStationData {

    private final ArrayList<String> eventTimes;
    private final String[] variableNames;
    private TrajectoryFeatureCollection trajectoryData;
    private ArrayList<TrajectoryFeature> trajList;

    public Trajectory(String[] stationName, String[] eventTime, String[] variableNames) {
        startDate = null;
        endDate = null;
        this.variableNames = variableNames;
        this.reqStationNames = new ArrayList<String>();
        reqStationNames.addAll(Arrays.asList(stationName));
        this.eventTimes = new ArrayList<String>();
        eventTimes.addAll(Arrays.asList(eventTime));
    }

    public void addAllTrajectoryData(PointFeatureIterator trajFeatureIterator, List<String> valueList, DateFormatter dateFormatter, StringBuilder builder) throws IOException {
        while (trajFeatureIterator.hasNext()) {
            PointFeature trajFeature = trajFeatureIterator.next();
            valueList.clear();
            addDataLine(valueList, dateFormatter, trajFeature, builder);
        }
    }

    public void addDataLine(List<String> valueList, DateFormatter dateFormatter, PointFeature trajFeature, StringBuilder builder) throws IOException {
        valueList.add(dateFormatter.toDateTimeStringISO(trajFeature.getObservationTimeAsDate()));

        for (int i = 0; i < variableNames.length; i++) {
            valueList.add(trajFeature.getData().getScalarObject(variableNames[i]).toString());
            builder.append(valueList.get(i));
            if (i < variableNames.length - 1) {
                builder.append(",");
            }
        }
        builder.append(" ");
        builder.append("\n");
    }

    @Override
    public void setData(Object featureCollection) throws IOException {
        this.trajectoryData = (TrajectoryFeatureCollection) featureCollection;

        trajList = new ArrayList<TrajectoryFeature>();

        DateTime dtSearchStart = null;
        DateTime dtSearchEnd = null;

        boolean firstSet = true;

        //check first to see if the event times are not null
        if (eventTimes != null) {
            //turn event times in to dateTimes to compare
            if (eventTimes.size() >= 1) {
                dtSearchStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
            }
            if (eventTimes.size() == 2) {

                dtSearchEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);
            }

            //temp
            DateTime dtStart = null;
            DateTime dtEnd = null;
            //check
            DateTime dtStartt = null;
            DateTime dtEndt = null;

            while (trajectoryData.hasNext()) {
                TrajectoryFeature trajFeature = trajectoryData.next();
                trajFeature.calcBounds();

                String n = trajFeature.getName();

                //scan through the stationname for a match of id
                for (Iterator<String> it = reqStationNames.iterator(); it.hasNext();) {
                    String stName = it.next();
                    if (stName.equalsIgnoreCase(n)) {
                        trajList.add(trajFeature);
                    }
                }

                if (firstSet) {
                    upperLat = trajFeature.getBoundingBox().getLatMax();
                    lowerLat = trajFeature.getBoundingBox().getLatMin();
                    upperLon = trajFeature.getBoundingBox().getLonMax();
                    lowerLon = trajFeature.getBoundingBox().getLonMin();

                    dtStart = new DateTime(trajFeature.getDateRange().getStart().getDate(), chrono);
                    dtEnd = new DateTime(trajFeature.getDateRange().getEnd().getDate(), chrono);
                    firstSet = false;
                } else {

                    dtStartt = new DateTime(trajFeature.getDateRange().getStart().getDate(), chrono);
                    dtEndt = new DateTime(trajFeature.getDateRange().getEnd().getDate(), chrono);

                    if (dtStartt.isBefore(dtStart)) {
                        dtStart = dtStartt;
                    }
                    if (dtEndt.isAfter(dtEnd)) {
                        dtEnd = dtEndt;
                    }

                    if (trajFeature.getBoundingBox().getLatMax() > upperLat) {
                        upperLat = trajFeature.getBoundingBox().getLatMax();
                    }
                    if (trajFeature.getBoundingBox().getLatMin() < lowerLat) {
                        lowerLat = trajFeature.getBoundingBox().getLatMin();
                    }
                    //lon
                    if (trajFeature.getBoundingBox().getLonMax() > upperLon) {
                        upperLon = trajFeature.getBoundingBox().getLonMax();
                    }
                    if (trajFeature.getBoundingBox().getLonMax() < lowerLon) {
                        lowerLon = trajFeature.getBoundingBox().getLonMin();
                    }
                }

            }
            setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
            setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));
            if (reqStationNames != null) {
                setNumberOfStations(reqStationNames.size());
            }
        }
    }

    @Override
    public void setInitialLatLonBounaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDataResponse(int stNum) {
        try {
            if (trajectoryData != null) {
                return createTrajectoryFeature(stNum);
            }
        } catch (IOException ex) {
            Logger.getLogger(Trajectory.class.getName()).log(Level.SEVERE, null, ex);
            return DATA_RESPONSE_ERROR + Profile.class;
        }
        return DATA_RESPONSE_ERROR + Profile.class;
    }

    @Override
    public String getStationName(int idNum) {
        if (trajList != null) {
            return "TRAJECTORY_" + (trajList.get(idNum).getName());
        } else {
            return Invalid_Station;
        }
    }

    @Override
    public double getLowerLat(int stNum) {
        if (trajList != null) {
            return (trajList.get(stNum).getBoundingBox().getLatMin());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getLowerLon(int stNum) {
        if (trajList != null) {
            return (trajList.get(stNum).getBoundingBox().getLonMin());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLat(int stNum) {
        if (trajList != null) {
            return (trajList.get(stNum).getBoundingBox().getLatMax());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLon(int stNum) {
        if (trajList != null) {
            return (trajList.get(stNum).getBoundingBox().getLonMax());
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public String getTimeEnd(int stNum) {
        if (trajList != null) {
            return df.toDateTimeStringISO(trajList.get(stNum).getDateRange().getEnd().getDate());
        } else {
            return ERROR_NULL_DATE;
        }
    }

    @Override
    public String getTimeBegin(int stNum) {
        if (trajList != null) {
            return df.toDateTimeStringISO(trajList.get(stNum).getDateRange().getStart().getDate());
        } else {
            return ERROR_NULL_DATE;
        }
    }

    /**
     * gets the trajectory response for the getcaps request
     * @param dataset
     * @param document
     * @param featureOfInterest
     * @param GMLName
     * @param format
     * @param observedPropertyList
     * @return
     * @throws IOException 
     */
    public static Document getCapsResponse(FeatureCollection dataset, Document document, String featureOfInterest, String GMLName, String format, List<String> observedPropertyList) throws IOException {
        //PointFeatureIterator trajIter;


        while (((TrajectoryFeatureCollection) dataset).hasNext()) {
            TrajectoryFeature tFeature = ((TrajectoryFeatureCollection) dataset).next();
            tFeature.calcBounds();

            //trajIter = tFeature.getPointFeatureIterator(-1);
            //attributes
            SOSObservationOffering newOffering = new SOSObservationOffering();
            newOffering.setObservationStationLowerCorner(Double.toString(tFeature.getBoundingBox().getLatMin()), Double.toString(tFeature.getBoundingBox().getLonMin()));
            newOffering.setObservationStationUpperCorner(Double.toString(tFeature.getBoundingBox().getLatMax()), Double.toString(tFeature.getBoundingBox().getLonMax()));

            //check the data
            if (tFeature.getDateRange() != null) {
                newOffering.setObservationTimeBegin(tFeature.getDateRange().getStart().toDateTimeStringISO());
                newOffering.setObservationTimeEnd(tFeature.getDateRange().getEnd().toDateTimeStringISO());
            } //find the dates out!
            else {
                System.out.println("no dates yet");
            }

            newOffering.setObservationStationDescription(tFeature.getCollectionFeatureType().toString());
            newOffering.setObservationFeatureOfInterest(featureOfInterest + (tFeature.getName()));
            newOffering.setObservationName(GMLName + (tFeature.getName()));
            newOffering.setObservationStationID((tFeature.getName()));
            newOffering.setObservationProcedureLink(GMLName + ((tFeature.getName())));
            newOffering.setObservationSrsName("EPSG:4326");  // TODO?  
            newOffering.setObservationObserveredList(observedPropertyList);
            newOffering.setObservationFormat(format);

            document = CDMUtils.addObsOfferingToDoc(newOffering, document);
        }

        return document;
    }

    @Override
    public String getDescription(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String createTrajectoryFeature(int stNum) throws IOException {
        StringBuilder builder = new StringBuilder();
        DateFormatter dateFormatter = new DateFormatter();
        TrajectoryFeature trajFeature = trajList.get(stNum);
        addTrajectoryData(dateFormatter, builder, trajFeature, stNum);
        return builder.toString();
    }

    private void addTrajectoryData(DateFormatter dateFormatter, StringBuilder builder, TrajectoryFeature traj, int stNum) throws IOException {

        List<String> valueList = new ArrayList<String>();
        PointFeatureIterator trajFeatureIterator = traj.getPointFeatureIterator(-1);

        DateTime trajTime;
        DateTime dtStart;
        DateTime dtEnd;

        //if no times are specified
        if (eventTimes == null) {
            addAllTrajectoryData(trajFeatureIterator, valueList, dateFormatter, builder);
        } //if more than one date is specified
        else if (eventTimes.size() > 1) {
            //get the dates in iso format
            dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
            dtEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);

            while (trajFeatureIterator.hasNext()) {
                PointFeature trajFeature = trajFeatureIterator.next();
                valueList.clear();

                trajTime = new DateTime(trajFeature.getObservationTimeAsDate(), chrono);

                if (trajTime.isEqual(dtStart)) {
                    addDataLine(valueList, dateFormatter, trajFeature, builder);
                } else if (trajTime.isEqual(dtEnd)) {
                    addDataLine(valueList, dateFormatter, trajFeature, builder);
                } else if (trajTime.isAfter(dtStart) && (trajTime.isBefore(dtEnd))) {
                    addDataLine(valueList, dateFormatter, trajFeature, builder);
                }

            }
        } //if start and end time were specified
        else if (eventTimes.size() == 1) {
            //get the single date in iso format
            dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
            while (trajFeatureIterator.hasNext()) {
                PointFeature trajFeature = trajFeatureIterator.next();
                valueList.clear();
                trajTime = new DateTime(trajFeature.getObservationTimeAsDate(), chrono);

                if (trajTime.isEqual(dtStart)) {
                    addDataLine(valueList, dateFormatter, trajFeature, builder);
                    //if it matches return...
                    break;
                }
            }
        } //times specified are weird, report all
        else {
            addAllTrajectoryData(trajFeatureIterator, valueList, dateFormatter, builder);
        }
    }
}
