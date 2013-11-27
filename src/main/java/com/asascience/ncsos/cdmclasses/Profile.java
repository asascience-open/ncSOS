/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.cdmclasses;

import com.asascience.ncsos.go.ObservationOffering;
import com.asascience.ncsos.util.DatasetHandlerAdapter;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.ProfileFeature;
import ucar.nc2.ft.ProfileFeatureCollection;
import ucar.nc2.units.DateFormatter;
import ucar.unidata.geoloc.Station;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides methods to gather information from Profile datasets needed for requests: GetCapabilities, GetObservations
 * @author abird
 * @version 1.0.0
 */
public class Profile extends baseCDMClass implements iStationData {

    private final String[] variableNames;
    private HashMap<Integer, ProfileFeature> profileList;
    private final ArrayList<String> eventTimes;
    private ProfileFeatureCollection profileData;
   
    /**
     * 
     * @param stationName
     * @param eventTime
     * @param variableNames
     */
    public Profile(String[] stationName, String[] eventTime, String[] variableNames) {
        startDate = null;
        endDate = null;

        this.variableNames = variableNames;

        this.reqStationNames = new ArrayList<String>();
        reqStationNames.addAll(Arrays.asList(stationName));
        
        this.eventTimes = new ArrayList<String>();
        if (eventTime != null)
            this.eventTimes.addAll(Arrays.asList(eventTime));
        
        lowerAlt = Double.POSITIVE_INFINITY;
        upperAlt = Double.NEGATIVE_INFINITY;
    }
    
    /**
     * gets the Profile response for the gc request
     * @param profileCollection
     * @param document
     * @param featureOfInterestBase
     * @param GMLName
     * @param observedPropertyList
     * @return
     * @throws IOException  
     */
    public static Document getCapsResponse(ProfileFeatureCollection profileCollection, Document document, String featureOfInterestBase, String GMLName, List<String> observedPropertyList) throws IOException {
        String profileID = profileCollection.getName();

        //profiles act like stations at present
        while (profileCollection.hasNext()) {
            ProfileFeature pFeature = profileCollection.next();

            //attributes
            ObservationOffering newOffering = new ObservationOffering();

            newOffering.setObservationStationLowerCorner(Double.toString(pFeature.getLatLon().getLatitude()), Double.toString(pFeature.getLatLon().getLongitude()));
            newOffering.setObservationStationUpperCorner(Double.toString(pFeature.getLatLon().getLatitude()), Double.toString(pFeature.getLatLon().getLongitude()));

            DatasetHandlerAdapter.calcBounds(pFeature);

            //check the data
            if (pFeature.getDateRange() != null) {
                newOffering.setObservationTimeBegin(pFeature.getDateRange().getStart().toDateTimeStringISO());
                newOffering.setObservationTimeEnd(pFeature.getDateRange().getEnd().toDateTimeStringISO());
            } //find the dates out!
            else {
                _log.error("no dates yet");
            }


            newOffering.setObservationStationDescription(pFeature.getCollectionFeatureType().toString());
            if (profileID != null) {
                newOffering.setObservationStationID("PROFILE_" + profileID);
                newOffering.setObservationProcedureLink(GMLName+("PROFILE_" + profileID));
                newOffering.setObservationName(GMLName+(profileID));
                newOffering.setObservationFeatureOfInterest(featureOfInterestBase+("PROFILE_" + profileID));
            } else {
                newOffering.setObservationFeatureOfInterest(featureOfInterestBase+(pFeature.getName()));
                newOffering.setObservationStationID((pFeature.getName()));
                newOffering.setObservationProcedureLink(GMLName+((pFeature.getName())));
                newOffering.setObservationFeatureOfInterest(featureOfInterestBase+(pFeature.getName()));
            }
            newOffering.setObservationSrsName("EPSG:4326");  // TODO?  
            newOffering.setObservationObserveredList(observedPropertyList);
            document = CDMUtils.addObsOfferingToDoc(newOffering,document);
        }
        
         return document;
    }
    
    public boolean isStationInFinalCollection(int stNum) {
        if (profileList != null && profileList.containsKey(stNum))
            return true;
        return false;
    }

    /************************
     * iStationData Methods *
     **************************************************************************/

    @Override
    public void setData(Object profileFeatureCollection) throws IOException {
        this.profileData = (ProfileFeatureCollection) profileFeatureCollection;
        
        profileList = new HashMap<Integer, ProfileFeature>();

        boolean firstSet = true;
        
        //temp
        DateTime dtStart = null;
        DateTime dtEnd = null;
        //check
        DateTime dtStartt = null;
        DateTime dtEndt = null;
        String profileID = null;
        
        while (profileData.hasNext()) {
            ProfileFeature pFeature = profileData.next();
            DatasetHandlerAdapter.calcBounds(pFeature);

            profileID = pFeature.getName();
            
            DateTime eventStart = (eventTimes.size() >= 1) ? new DateTime(df.getISODate(eventTimes.get(0)), chrono) : null;
            DateTime eventEnd = (eventTimes.size() > 1) ? new DateTime(df.getISODate(eventTimes.get(1)), chrono) : null;
            
            //scan through the stationname for a match of id
            if (profileID != null && reqStationNames.contains(profileID)) {
                // check to make sure the profile falls into the event time
                if (eventStart != null) {
                    // does it lie after or at start?
                    if (pFeature.getTime().before(eventStart.toDate()))
                        continue;
                    if (eventEnd != null) {
                        // does it lie before or at end?
                        if (pFeature.getTime().after(eventEnd.toDate()))
                            continue;
                    }
                }
                // get the index of the station we are adding
                Integer stNum = 0;
                for (int sti =0; sti < reqStationNames.size(); sti++) {
                    if (reqStationNames.get(sti).equalsIgnoreCase(profileID))
                        stNum = sti;
                }
                profileList.put(stNum, pFeature);

                // check local altitude
                double altmin = Double.POSITIVE_INFINITY;
                double altmax = Double.NEGATIVE_INFINITY;

                for (pFeature.resetIteration();pFeature.hasNext();) {
                    PointFeature point = pFeature.next();

                    double alt = point.getLocation().getAltitude();

                    if (alt < altmin)
                        altmin = alt;
                    if (alt > altmax)
                        altmax = alt;
                }

                if (altmin < lowerAlt)
                    lowerAlt = altmin;
                if (altmax > upperAlt)
                    upperAlt = altmax;

                if (firstSet) {
                    upperLat = pFeature.getLatLon().getLatitude();
                    lowerLat = pFeature.getLatLon().getLatitude();
                    upperLon = pFeature.getLatLon().getLongitude();
                    lowerLon = pFeature.getLatLon().getLongitude();
                    
                    dtStart = new DateTime(pFeature.getTime(), chrono);
                    dtEnd = new DateTime(pFeature.getTime(), chrono);
                    firstSet = false;
                } else {

                    dtStartt = new DateTime(pFeature.getTime(), chrono);
                    dtEndt = new DateTime(pFeature.getTime(), chrono);

                    if (dtStartt.isBefore(dtStart)) {
                        dtStart = dtStartt;
                    }
                    if (dtEndt.isAfter(dtEnd)) {
                        dtEnd = dtEndt;
                    }

                    if (pFeature.getLatLon().getLatitude() > upperLat) {
                        upperLat = pFeature.getLatLon().getLatitude();
                    }
                    if (pFeature.getLatLon().getLatitude() < lowerLat) {
                        lowerLat = pFeature.getLatLon().getLatitude();
                    }
                    //lon
                    if (pFeature.getLatLon().getLongitude() > upperLon) {
                        upperLon = pFeature.getLatLon().getLongitude();
                    }
                    if (pFeature.getLatLon().getLongitude() < lowerLon) {
                        lowerLon = pFeature.getLatLon().getLongitude();
                    }
                }
            } else {
                // null profile id... shouldn't happen (defaults to "0")
            }
        }
        setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
        setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));
        if (reqStationNames != null) {
            setNumberOfStations(reqStationNames.size());
        }
    }
    
    @Override
    public boolean isStationInFinalList(int stNum) {
        if (profileList != null && profileList.containsKey(stNum))
            return true;
        return false;
    }

    @Override
    public void setInitialLatLonBoundaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDataResponse(int stNum) {
        try {
            if (profileData != null && profileList.containsKey(stNum)) {
                return createProfileFeature(stNum);
            } else {
                _log.warn("profileData " + stNum + " is null, not creating a string");
            }
        } catch (IOException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
            return DATA_RESPONSE_ERROR + Profile.class;
        }
        return DATA_RESPONSE_ERROR + Profile.class;
    }

    @Override
    public String getStationName(int idNum) {
        if (profileData != null) {
            return "PROFILE_" + (reqStationNames.get(idNum));
        } else {
            return Invalid_Station;
        }
    }

    @Override
    public double getLowerLat(int stNum) {
        if (profileData != null && profileList.containsKey(stNum)) {
            return profileList.get(stNum).getLatLon().getLatitude();
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getLowerLon(int stNum) {
        if (profileData != null && profileList.containsKey(stNum)) {
            return profileList.get(stNum).getLatLon().getLongitude();
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLat(int stNum) {
        if (profileData != null && profileList.containsKey(stNum)) {
            return profileList.get(stNum).getLatLon().getLatitude();
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLon(int stNum) {
        if (profileData != null && profileList.containsKey(stNum)) {
            return profileList.get(stNum).getLatLon().getLongitude();
        } else {
            return Invalid_Value;
        }
    }
    
    @Override
    public double getLowerAltitude(int stNum) {
        return this.lowerAlt;
    }
    
    @Override
    public double getUpperAltitude(int stNum) {
        return this.upperAlt;
    }

    @Override
    public String getTimeEnd(int stNum) {
        if (profileData != null && profileList.containsKey(stNum)) {
            return df.toDateTimeStringISO(profileList.get(stNum).getTime());
        } else {
            return ERROR_NULL_DATE;
        }
    }

    @Override
    public String getTimeBegin(int stNum) {

        if (profileData != null && profileList.containsKey(stNum)) {
            return df.toDateTimeStringISO(profileList.get(stNum).getTime());
        } else {
            return ERROR_NULL_DATE;
        }
    }

    @Override
    public String getDescription(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**************************************************************************/
    
    private void addProfileData(List<String> valueList, DateFormatter dateFormatter, StringBuilder builder, ProfileFeature profileFeature, int stNum) {
        //set the iterator the the correct profile
        try {
            PointFeatureIterator pointIterator = profileFeature.getPointFeatureIterator(-1);
            while (pointIterator.hasNext()) {
                PointFeature pointFeature = pointIterator.next();

                String profileID = profileFeature.getName();
                //if there is a profile id use it against the data that is requested
                if (profileID != null) {
                    valueList.clear();
                    valueList.add("time=" + dateFormatter.toDateTimeStringISO(profileFeature.getTime()));
                    valueList.add("station=" + stNum);
                    addProfileDataToBuilder(valueList, pointFeature, builder);
                }
            }
        } catch (Exception ex) {
            // error reading
            builder.delete(0, builder.length());
            builder.append("ERROR =reading data from dataset: ").append(ex.getLocalizedMessage()).append(". Most likely this property does not exist or is improperly stored in the dataset.");
        }
    }

    private void addProfileDataToBuilder(List<String> valueList, PointFeature pointFeature, StringBuilder builder) throws IOException {
        for (String variableName : variableNames) {
            valueList.add(variableName + "=" + pointFeature.getData().getScalarObject(variableName).toString());
        }

        for (int i = 0; i < valueList.size(); i++) {
            builder.append(valueList.get(i));
            if (i < valueList.size() - 1) {
                builder.append(",");
            }
        }

        builder.append(";");
    }

    private String createProfileFeature(int stNum) throws IOException {
        if (profileList != null && profileList.containsKey((Integer)stNum)) {
            StringBuilder builder = new StringBuilder();
            DateFormatter dateFormatter = new DateFormatter();
            List<String> valueList = new ArrayList<String>();

            ProfileFeature pFeature = profileList.get(stNum);
            addProfileData(valueList, dateFormatter, builder, pFeature, stNum);

            return builder.toString();
        }
        return "";
    }

    public List<String> getLocationsString(int stNum) {
        List<String> retval = new ArrayList<String>();
        retval.add(this.getLowerLat(stNum) + " " + this.getLowerLon(stNum));
        return retval;
    }

}
