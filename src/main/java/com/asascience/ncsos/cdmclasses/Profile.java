/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.cdmclasses;

import com.asascience.ncsos.getobs.SOSObservationOffering;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.ProfileFeature;
import ucar.nc2.ft.ProfileFeatureCollection;
import ucar.nc2.units.DateFormatter;
import ucar.unidata.geoloc.Station;

/**
 * Provides methods to gather information from Profile datasets needed for requests: GetCapabilities, GetObservations
 * @author abird
 * @version 1.0.0
 */
public class Profile extends baseCDMClass implements iStationData {

    private final String[] variableNames;
    private List<ProfileFeature> profileList;
    private final ArrayList<String> eventTimes;
    private ProfileFeatureCollection profileData;
    private ArrayList<Double> altMin, altMax;
   
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

        if (eventTime != null) {
            this.eventTimes = new ArrayList<String>();
            this.eventTimes.addAll(Arrays.asList(eventTime));
        } else
            this.eventTimes = null;
        
        lowerAlt = Double.POSITIVE_INFINITY;
        upperAlt = Double.NEGATIVE_INFINITY;
    }
    
    /**
     * gets the Profile response for the getcaps request
     * @param profileCollection
     * @param document
     * @param featureOfInterestBase
     * @param GMLName
     * @param observedPropertyList
     * @return
     * @throws IOException  
     */
    public static Document getCapsResponse(ProfileFeatureCollection profileCollection, Document document, String featureOfInterestBase, String GMLName, List<String> observedPropertyList) throws IOException {
        String profileID = null;
        
        PointFeatureIterator pp = null;
        //profiles act like stations at present
        while (profileCollection.hasNext()) {
            ProfileFeature pFeature = profileCollection.next();

            //scan through the data and get the profile id number
            pp = pFeature.getPointFeatureIterator(-1);
            while (pp.hasNext()) {
                PointFeature pointFeature = pp.next();
                profileID = getProfileIDFromProfile(pointFeature);
                //System.out.println(profileID);
                break;
            }

            //attributes
            SOSObservationOffering newOffering = new SOSObservationOffering();

            newOffering.setObservationStationLowerCorner(Double.toString(pFeature.getLatLon().getLatitude()), Double.toString(pFeature.getLatLon().getLongitude()));
            newOffering.setObservationStationUpperCorner(Double.toString(pFeature.getLatLon().getLatitude()), Double.toString(pFeature.getLatLon().getLongitude()));

            pFeature.calcBounds();

            //check the data
            if (pFeature.getDateRange() != null) {
                newOffering.setObservationTimeBegin(pFeature.getDateRange().getStart().toDateTimeStringISO());
                newOffering.setObservationTimeEnd(pFeature.getDateRange().getEnd().toDateTimeStringISO());
            } //find the dates out!
            else {
                System.out.println("no dates yet");
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

        if (profileCollection.isMultipleNested() == false) {
            System.out.println("not nested");
        } else {
            System.out.println("nested");
        }
        
         return document;
    }

    /************************
     * iStationData Methods *
     **************************************************************************/

    @Override
    public void setData(Object profilePeatureCollection) throws IOException {
        this.profileData = (ProfileFeatureCollection) profilePeatureCollection;

        profileList = new ArrayList<ProfileFeature>();

        DateTime dtSearchStart = null;
        DateTime dtSearchEnd = null;
        
        altMin = new ArrayList<Double>();
        altMax = new ArrayList<Double>();

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
            String profileID = null;

            while (profileData.hasNext()) {
                ProfileFeature pFeature = profileData.next();
                pFeature.calcBounds();

                //scan through the data and get the profile id number
                PointFeatureIterator pp = pFeature.getPointFeatureIterator(-1);
                while (pp.hasNext()) {
                    PointFeature pointFeature = pp.next();
                    profileID = getProfileIDFromProfile(pointFeature);
                    //System.out.println(profileID);
                    break;
                }

                //scan through the stationname for a match of id
                for (Iterator<String> it = reqStationNames.iterator(); it.hasNext();) {
                    String stName = it.next();
                    if (stName.equalsIgnoreCase(profileID)) {
                        profileList.add(pFeature);
                        
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
                        
                        altMin.add(altmin);
                        altMax.add(altmax);
                    }
                }

                if (profileID == null) {
                    profileID = "0";
                    profileList.add(pFeature);
                }

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
            }
            setStartDate(df.toDateTimeStringISO(dtStart.toDate()));
            setEndDate(df.toDateTimeStringISO(dtEnd.toDate()));
            if (reqStationNames != null) {
                setNumberOfStations(reqStationNames.size());
            }
        }
    }

    @Override
    public void setInitialLatLonBoundaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDataResponse(int stNum) {
        try {
            if (profileData != null) {
                return createProfileFeature(stNum);
            } else {
                System.out.println("profileData is null, not creating a string");
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
        if (profileData != null) {
            return profileList.get(stNum).getLatLon().getLatitude();
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getLowerLon(int stNum) {
        if (profileData != null) {
            return profileList.get(stNum).getLatLon().getLongitude();
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLat(int stNum) {
        if (profileData != null) {
            return profileList.get(stNum).getLatLon().getLatitude();
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public double getUpperLon(int stNum) {
        if (profileData != null) {
            return profileList.get(stNum).getLatLon().getLongitude();
        } else {
            return Invalid_Value;
        }
    }
    
    @Override
    public double getLowerAltitude(int stNum) {
        if (altMin != null && altMin.size() > stNum) {
            double retval = altMin.get(stNum);
            if (retval == Double.NaN || retval == Double.POSITIVE_INFINITY)
                retval = 0;
            return retval;
        } else {
            return Invalid_Value;
        }
    }
    
    @Override
    public double getUpperAltitude(int stNum) {
        if (altMax != null && altMax.size() > stNum) {
            double retval = altMax.get(stNum);
            if (retval == Double.NaN || retval == Double.POSITIVE_INFINITY)
                retval = 0;
            return retval;
        } else {
            return Invalid_Value;
        }
    }

    @Override
    public String getTimeEnd(int stNum) {
        if (profileData != null) {
            return df.toDateTimeStringISO(profileList.get(stNum).getTime());
        } else {
            return ERROR_NULL_DATE;
        }
    }

    @Override
    public String getTimeBegin(int stNum) {

        if (profileData != null) {
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
    
    private void addProfileData(List<String> valueList, DateFormatter dateFormatter, StringBuilder builder, PointFeatureIterator profileIterator, int stNum) {
        //set the iterator the the correct profile
        try {
            while (profileIterator.hasNext()) {
                PointFeature pointFeature = profileIterator.next();
                valueList.clear();
                valueList.add("time=" + dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()));

                String profileID = getProfileIDFromProfile(pointFeature);
                //if there is a profile id use it against the data that is requested
                if (profileID != null) {
                    //System.out.println(profileID);
                    if (profileID.equalsIgnoreCase(reqStationNames.get(stNum))) {
                        addProfileDataToBuilder(valueList, pointFeature, builder);
                    } else {
                        System.out.println("Not adding profile data to builder");
                    }
                } else {
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

        //builder.append(tokenJoiner.join(valueList));
//        builder.append(" ");
//        builder.append("\n");
        builder.append(";");
    }

    private static String getProfileIDFromProfile(PointFeature pointFeature) {
        String profileID = null;
        //Try and get profileID
        try {
            profileID = (pointFeature.getData().getScalarObject("profile").toString());
        } //if it is not there dont USE IT!,,,,,but maybe warn that it is not there?        
        catch (Exception e) {
            //Logger.getLogger(SOSGetObservationRequestHandler.class.getName()).log(Level.INFO, "ERROR PROFILE ID NO AVAILABLE \n Must be single Profile \n", e);
        }
        return profileID;
    }
    
    private String createProfileFeature(int stNum) throws IOException {
        StringBuilder builder = new StringBuilder();
        DateFormatter dateFormatter = new DateFormatter();
        List<String> valueList = new ArrayList<String>();

        //if multi Time
        if (eventTimes.size() > 1) {
            //get the profile collection, and loop through
            //while has next

            for (int i = 0; i < profileList.size(); i++) {
                //grab the profile
                ProfileFeature pFeature = profileList.get(i);
                if (pFeature != null) {

                    //output the name
                    DateTime dtStart = new DateTime(df.getISODate(eventTimes.get(0)), chrono);
                    DateTime dtEnd = new DateTime(df.getISODate(eventTimes.get(1)), chrono);
                    DateTime tsDt = new DateTime(pFeature.getName(), chrono);
                    
                    //find out if current time(searchtime) is one or after startTime
                    //same as start
                    if (tsDt.isEqual(dtStart)) {
                        addProfileData(valueList, dateFormatter, builder, pFeature.getPointFeatureIterator(-1), stNum);
                    } //equal end
                    else if (tsDt.isEqual(dtEnd)) {
                        addProfileData(valueList, dateFormatter, builder, pFeature.getPointFeatureIterator(-1), stNum);
                    } //afterStart and before end       
                    else if (tsDt.isAfter(dtStart) && (tsDt.isBefore(dtEnd))) {
                        addProfileData(valueList, dateFormatter, builder, pFeature.getPointFeatureIterator(-1), stNum);
                    } else {
                        System.out.println("tsDt is not equal to any known case");
                    }
                    //setCount(pFeature.size());
                    // exit if we get an error
                    if (builder.toString().contains("ERROR"))
                        break;
                }
            }

        } //if not multiTime        
        else {
            ProfileFeature pFeature = profileList.get(stNum);
            addProfileData(valueList, dateFormatter, builder, pFeature.getPointFeatureIterator(-1), stNum);
        }
        
        return builder.toString();
    }

}
