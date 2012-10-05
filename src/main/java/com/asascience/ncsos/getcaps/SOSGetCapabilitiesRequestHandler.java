package com.asascience.ncsos.getcaps;

import com.asascience.ncsos.outputformatter.GetCapsOutputter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.geotoolkit.util.collection.CheckedHashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.*;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Creates basic Get Capabilites request handler that can read from a netcdf dataset
 * the information needed to populate a get capabilities template.
 * @author Abird
 * @version 1.0.0
 */
public class SOSGetCapabilitiesRequestHandler extends SOSBaseRequestHandler {

    private final String threddsURI;
    
    private static final String OWS = "http://www.opengis.net/ows/1.1";
    
    private static CalendarDate setStartDate;
    private static CalendarDate setEndDate;
    private static HashMap<Integer, CalendarDateRange> stationDateRange;
    private static HashMap<Integer, LatLonRect> stationBBox;

    /**
     * Creates an instance of SOSGetCapabilitiesRequestHandler to handle the dataset
     * and uri from the thredds request.
     * @param netCDFDataset dataset for which the Get Capabilities request is being
     * directed to
     * @param threddsURI uri from the thredds Get Capabilities request
     * @throws IOException
     */
    public SOSGetCapabilitiesRequestHandler(NetcdfDataset netCDFDataset, String threddsURI) throws IOException {
        super(netCDFDataset);
        this.threddsURI = threddsURI;
        output = new GetCapsOutputter();
        
        CalculateBoundsForFeatureSet();
    }
    
    public SOSGetCapabilitiesRequestHandler(NetcdfDataset emptyDataset) throws IOException {
        super(emptyDataset);
        this.threddsURI = "";
        output = new GetCapsOutputter();
    }
    
    /**
     * Creates the output for the get capabilities response
     */
    public void parseGetCapabilitiesDocument() {
        GetCapsOutputter out = (GetCapsOutputter) output;
        // service identification
        out.parseServiceIdentification(getTitle() ,Region, Access);
        
        // service provider
        out.parseServiceDescription(DataPage, PrimaryOwnership);
        
        // operations metadata
        // set get capabilities output
        out.setOperationGetCaps(threddsURI);
        // set get observation output
        if (getGridDataset() != null)
            out.setOperationGetObs(threddsURI, setStartDate, setEndDate, getSensorNames(), getStationNames().values().toArray(new String[getStationNames().values().size()]), true, getGridDataset().getBoundingBox());
        else
            out.setOperationGetObs(threddsURI, setStartDate, setEndDate, getSensorNames(), getStationNames().values().toArray(new String[getStationNames().values().size()]), false, null);
        // set describe sensor output
        out.setOperationDescSen(threddsURI, getStationNames().values().toArray(new String[getStationNames().values().size()]), getSensorNames());
        
        // Contents
        // observation offering list
        // network-all
        // get the bounds
        Double latMin = Double.MAX_VALUE, latMax = Double.NEGATIVE_INFINITY, lonMin = Double.MAX_VALUE, lonMax = Double.NEGATIVE_INFINITY;
        for (LatLonRect rect : stationBBox.values()) {
            latMin = (latMin > rect.getLatMin()) ? rect.getLatMin() : latMin;
            latMax = (latMax < rect.getLatMax()) ? rect.getLatMax() : latMax;
            lonMin = (lonMin > rect.getLonMin()) ? rect.getLonMin() : lonMin;
            lonMax = (lonMax < rect.getLonMax()) ? rect.getLonMax() : lonMax;
        }
        LatLonRect setRange = new LatLonRect(new LatLonPointImpl(latMin, lonMin), new LatLonPointImpl(latMax, lonMax));
        CalendarDateRange setTime = null;
        if (setStartDate != null && setEndDate != null)
            setTime = CalendarDateRange.of(setStartDate,setEndDate);
        out.setObservationOfferingNetwork(setRange, getStationNames().values().toArray(new String[getStationNames().values().size()]), getSensorNames(), setTime);
        // iterate through our stations and add them
        for (Integer index : getStationNames().keySet()) {
            ((GetCapsOutputter)output).setObservationOfferingList(getStationNames().get(index), index.intValue(), stationBBox.get(index), getSensorNames(), stationDateRange.get(index));
        }
    }
    
    private void CalculateBoundsForFeatureSet() {
        if (getDatasetFeatureType() != null) {
            stationDateRange = new HashMap<Integer, CalendarDateRange>();
            stationBBox = new HashMap<Integer, LatLonRect>();
            CalendarDate start = null, end = null;
            int stationIndex = 0;
            switch (getDatasetFeatureType()) {
                case TRAJECTORY:
                    try {
                        TrajectoryFeatureCollection collection = (TrajectoryFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while(collection.hasNext()) {
                            TrajectoryFeature feature = collection.next();
                            feature.calcBounds();
                            if (start == null || start.isAfter(feature.getCalendarDateRange().getStart()))
                                start = feature.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd()))
                                end = feature.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                            stationBBox.put(stationIndex, feature.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside TRAJECTORY case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
                        }
                    }
                    break;
                case STATION:
                    try {
                        StationTimeSeriesFeatureCollection collection = (StationTimeSeriesFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while(collection.hasNext()) {
                            StationTimeSeriesFeature feature = collection.next();
                            feature.calcBounds();
                            if (start == null || start.isAfter(feature.getCalendarDateRange().getStart()))
                                start = feature.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd()))
                                end = feature.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                            stationBBox.put(stationIndex, feature.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside STATION case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
                        }
                    }
                    break;
                case PROFILE:
                    try {
                        ProfileFeatureCollection collection = (ProfileFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while(collection.hasNext()) {
                            ProfileFeature feature = collection.next();
                            feature.calcBounds();
                            if (start == null || start.isAfter(feature.getCalendarDateRange().getStart()))
                                start = feature.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd()))
                                end = feature.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                            stationBBox.put(stationIndex, feature.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside PROFILE case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
                        }
                    }
                    break;
                case GRID:
                    start = getGridDataset().getCalendarDateStart();
                    end = getGridDataset().getCalendarDateEnd();
                    stationDateRange.put(0, CalendarDateRange.of(start, end));
                    break;
                case STATION_PROFILE:
                    try {
                        CalendarDateRange nullrange = null;
                        StationProfileFeatureCollection collection = (StationProfileFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while(collection.hasNext()) {
                            StationProfileFeature feature = collection.next();
                            PointFeatureCollection flattened = feature.flatten(null, nullrange);
                            flattened.calcBounds();
                            if (start == null || start.isAfter(flattened.getCalendarDateRange().getStart()))
                                start = flattened.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(flattened.getCalendarDateRange().getEnd()))
                                end = flattened.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, flattened.getCalendarDateRange());
                            stationBBox.put(stationIndex, flattened.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside STATION_PROFILE case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
                        }
                    }
                    break;
                case SECTION:
                    try {
                        CalendarDateRange nullrange = null;
                        SectionFeatureCollection collection = (SectionFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while(collection.hasNext()) {
                            SectionFeature feature = collection.next();
                            PointFeatureCollection flattened = feature.flatten(null, nullrange);
                            flattened.calcBounds();
                            if (start == null || start.isAfter(flattened.getCalendarDateRange().getStart()))
                                start = flattened.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(flattened.getCalendarDateRange().getEnd()))
                                end = flattened.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, flattened.getCalendarDateRange());
                            stationBBox.put(stationIndex, flattened.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside SECTION case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
                        }
                    }
                    break;
                default:
                    System.out.println("Unknown feature type - getDatasetFeatureType is ??");
                    output.setupExceptionOutput("Feature set is currently unsupported.");
                    return;
            }
            setStartDate = start;
            setEndDate = end;
        } else {
            System.out.println("Unknown feature type - getDatasetFeatureType is null");
        }
    }
}
