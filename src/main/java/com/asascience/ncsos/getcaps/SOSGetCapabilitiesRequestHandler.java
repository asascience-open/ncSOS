package com.asascience.ncsos.getcaps;

import com.asascience.ncsos.outputformatter.GetCapsOutputter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.DatasetHandlerAdapter;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
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
    
    private enum Sections {
        OPERATIONSMETADATA, SERVICEIDENTIFICATION, SERVICEPROVIDER, CONTENTS
    }
    
    private String sections;
    private BitSet requestedSections;
    private static final int SECTION_COUNT = 4;
    
    private static CalendarDate setStartDate;
    private static CalendarDate setEndDate;
    private static HashMap<Integer, CalendarDateRange> stationDateRange;
    private static HashMap<Integer, LatLonRect> stationBBox;
    
    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSGetCapabilitiesRequestHandler.class);

    /**
     * Creates an instance of SOSGetCapabilitiesRequestHandler to handle the dataset
     * and uri from the thredds request.
     * @param netCDFDataset dataset for which the Get Capabilities request is being
     * directed to
     * @param threddsURI uri from the thredds Get Capabilities request
     * @param sections string detailing what sections of the GC response should be returned
     * @throws IOException
     */
    public SOSGetCapabilitiesRequestHandler(NetcdfDataset netCDFDataset, String threddsURI, String sections) throws IOException {
        super(netCDFDataset);
        this.threddsURI = threddsURI;
        this.sections = sections.toLowerCase();
        output = new GetCapsOutputter();
        
        requestedSections = new BitSet(SECTION_COUNT);
        
        if (getFeatureDataset() == null) {
            // error, couldn't read dataset
            output.setupExceptionOutput("Unable to read dataset's feature type. Reported as " + FeatureDatasetFactoryManager.findFeatureType(netCDFDataset).toString() + "; unable to process.");
            return;
        }
        
        SetSectionBits();
        
        CalculateBoundsForFeatureSet();
    }
    
    /**
     * Used for creating quick exception responses
     * @param emptyDataset
     * @throws IOException 
     */
    public SOSGetCapabilitiesRequestHandler(NetcdfDataset emptyDataset) throws IOException {
        super(emptyDataset);
        this.sections = "";
        this.threddsURI = "";
        output = new GetCapsOutputter();
    }
    
    public void resetCapabilitiesSections(String sections) {
        this.sections = sections.toLowerCase();
        requestedSections = new BitSet(SECTION_COUNT);
        SetSectionBits();
    }
    
    /**
     * Creates the output for the get capabilities response
     */
    public void parseGetCapabilitiesDocument() {
        GetCapsOutputter out = (GetCapsOutputter) output;
        // early exit if we have an exception output
        if (out.hasExceptionOut())
            return;
        // service identification; parse if it is the section identified or 'all'
        if (this.requestedSections.get(Sections.SERVICEIDENTIFICATION.ordinal())) {
            out.parseServiceIdentification(title ,Region, Access);
        } else {
            // remove identification from doc
            out.removeServiceIdentification();
        }
        
        // service provider; parse if it is the section identified or 'all'
        if (this.requestedSections.get(Sections.SERVICEPROVIDER.ordinal())) {
            out.parseServiceDescription(PublisherURL, PrimaryOwnership);
        } else {
            // remove service provider from doc
            out.removeServiceProvider();
        }
        
        // operations metadata; parse if it is the section identified or 'all'
        if (this.requestedSections.get(Sections.OPERATIONSMETADATA.ordinal())) {
            // set get capabilities output
            out.setOperationGetCaps(threddsURI);
            // set get observation output
            if (getGridDataset() != null)
                out.setOperationGetObs(threddsURI, setStartDate, setEndDate, getSensorNames(), getStationNames().values().toArray(new String[getStationNames().values().size()]), true, getGridDataset().getBoundingBox());
            else
                out.setOperationGetObs(threddsURI, setStartDate, setEndDate, getSensorNames(), getStationNames().values().toArray(new String[getStationNames().values().size()]), false, null);
            // set describe sensor output
            out.setOperationDescSen(threddsURI, getStationNames().values().toArray(new String[getStationNames().values().size()]), getSensorNames());
        } else {
            // remove operations metadata
            out.removeOperations();
        }
        
        // Contents; parse if it is the section identified or 'all'
        if (this.requestedSections.get(Sections.CONTENTS.ordinal())) {
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
        } else {
            // remove Contents node
            out.removeContents();
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
                            if (DatasetHandlerAdapter.calcBounds(feature)) {
                                if (start == null || start.isAfter(feature.getCalendarDateRange().getStart()))
                                    start = feature.getCalendarDateRange().getStart();
                                if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd()))
                                    end = feature.getCalendarDateRange().getEnd();
                                stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                                stationBBox.put(stationIndex, feature.getBoundingBox());
                                stationIndex++;
                            } else {
                                GetExtentsFromSubFeatures(feature, stationIndex);
                            }
                        }
                    } catch (Exception ex) {
                        _log.error("CalculateDateRangesForFeatureSet - Exception caught inside TRAJECTORY case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            _log.error("\t" + elm.toString());
                        }
                    }
                    break;
                case STATION:
                    try {
                        StationTimeSeriesFeatureCollection collection = (StationTimeSeriesFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while(collection.hasNext()) {
                            StationTimeSeriesFeature feature = collection.next();
                            if (DatasetHandlerAdapter.calcBounds(feature)) {
                                if (start == null || start.isAfter(feature.getCalendarDateRange().getStart()))
                                    start = feature.getCalendarDateRange().getStart();
                                if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd()))
                                    end = feature.getCalendarDateRange().getEnd();
                                stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                                stationBBox.put(stationIndex, feature.getBoundingBox());
                            } else {
                                GetExtentsFromSubFeatures(feature, stationIndex);
                            }
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        _log.error("CalculateDateRangesForFeatureSet - Exception caught inside STATION case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            _log.error("\t" + elm.toString());
                        }
                    }
                    break;
                case PROFILE:
                    try {
                        ProfileFeatureCollection collection = (ProfileFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while(collection.hasNext()) {
                            ProfileFeature feature = collection.next();
                            if (DatasetHandlerAdapter.calcBounds(feature)) {
                                CalendarDate profileDate = CalendarDate.of(feature.getTime());
                                if (start == null || start.isAfter(profileDate))
                                    start = profileDate;
                                if (end == null || end.isBefore(profileDate))
                                    end = profileDate;
                                stationDateRange.put(stationIndex, CalendarDateRange.of(profileDate, profileDate));
                                stationBBox.put(stationIndex, new LatLonRect(feature.getLatLon(), feature.getLatLon()));
                                stationIndex++;
                            } else {
                                GetExtentsFromSubFeatures(feature, stationIndex);
                            }
                        }
                    } catch (Exception ex) {
                        _log.error("CalculateDateRangesForFeatureSet - Exception caught inside PROFILE case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            _log.error("\t" + elm.toString());
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
                            if (DatasetHandlerAdapter.calcBounds(flattened)) {
                                if (start == null || start.isAfter(flattened.getCalendarDateRange().getStart()))
                                    start = flattened.getCalendarDateRange().getStart();
                                if (end == null || end.isBefore(flattened.getCalendarDateRange().getEnd()))
                                    end = flattened.getCalendarDateRange().getEnd();
                                stationDateRange.put(stationIndex, flattened.getCalendarDateRange());
                                stationBBox.put(stationIndex, flattened.getBoundingBox());
                                stationIndex++;
                            } else {
                                GetExtentsFromSubFeatures(flattened, stationIndex);
                            }
                        }
                    } catch (Exception ex) {
                        _log.error("CalculateDateRangesForFeatureSet - Exception caught inside STATION_PROFILE case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            _log.error("\t" + elm.toString());
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
                            if (DatasetHandlerAdapter.calcBounds(flattened)) {
                                if (start == null || start.isAfter(flattened.getCalendarDateRange().getStart()))
                                    start = flattened.getCalendarDateRange().getStart();
                                if (end == null || end.isBefore(flattened.getCalendarDateRange().getEnd()))
                                    end = flattened.getCalendarDateRange().getEnd();
                                stationDateRange.put(stationIndex, flattened.getCalendarDateRange());
                                stationBBox.put(stationIndex, flattened.getBoundingBox());
                                stationIndex++;
                            } else {
                                GetExtentsFromSubFeatures(flattened, stationIndex);
                            }
                        }
                    } catch (Exception ex) {
                        _log.error("CalculateDateRangesForFeatureSet - Exception caught inside SECTION case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            _log.error("\t" + elm.toString());
                        }
                    }
                    break;
                default:
                    _log.error("Unknown feature type - getDatasetFeatureType is ??");
                    output.setupExceptionOutput("Feature set is currently unsupported.");
                    return;
            }
            setStartDate = start;
            setEndDate = end;
        } else {
            _log.error("Unknown feature type - getDatasetFeatureType is null");
        }
    }
    
    private void GetExtentsFromSubFeatures(PointFeatureCollection coll, int index) {
        try {
            // calculate the bounds of this particular station
            CalendarDate start = CalendarDate.present();
            CalendarDate end = CalendarDate.of(0);
            double minLat = Double.POSITIVE_INFINITY, maxLat = Double.NEGATIVE_INFINITY, minLon = Double.POSITIVE_INFINITY, maxLon = Double.NEGATIVE_INFINITY;
            for (coll.resetIteration();coll.hasNext();) {
                PointFeature pf = coll.next();
                if (pf.getObservationTimeAsCalendarDate().isAfter(end))
                    end = pf.getObservationTimeAsCalendarDate();
                else if (pf.getObservationTimeAsCalendarDate().isBefore(start))
                    start = pf.getObservationTimeAsCalendarDate();

                if (minLat > pf.getLocation().getLatitude())
                    minLat = pf.getLocation().getLatitude();
                else if (maxLat < pf.getLocation().getLatitude())
                    maxLat = pf.getLocation().getLatitude();

                if (minLon > pf.getLocation().getLongitude())
                    minLon = pf.getLocation().getLongitude();
                else if (maxLon < pf.getLocation().getLongitude())
                    maxLon = pf.getLocation().getLongitude();
            }
            // add the values to the table
            stationDateRange.put(index, CalendarDateRange.of(start, end));
            stationBBox.put(index, new LatLonRect(new LatLonPointImpl(minLat, minLon), new LatLonPointImpl(maxLat, maxLon)));
        } catch (Exception ex) {
            // failed, um just add global bounds
            _log.error("GetExtentsFromSubFeatures: Could not manually get extents, adding globals...\n\t" + ex.toString());
            stationDateRange.put(index, CalendarDateRange.of(CalendarDate.of(0), CalendarDate.of(CalendarDate.present().getDifferenceInMsecs(CalendarDate.of(0)))));
            stationBBox.put(index, new LatLonRect(new LatLonPointImpl(-90, -180), new LatLonPointImpl(90, 180)));
        } catch (Error err) {
            _log.error("GetExtentsFromSubFeatures: Could not manually get extents, adding globals...\n\t" + err.toString());
            stationDateRange.put(index, CalendarDateRange.of(CalendarDate.of(0), CalendarDate.of(CalendarDate.present().getDifferenceInMsecs(CalendarDate.of(0)))));
            stationBBox.put(index, new LatLonRect(new LatLonPointImpl(-90, -180), new LatLonPointImpl(90, 180)));
        }
    }
    
    
    private void SetSectionBits() {
        try {
            for (String sect : sections.split(",")) {
                if (sect.equals("all")) {
                    requestedSections.set(0, SECTION_COUNT);
                } else {
                    requestedSections.set(Sections.valueOf(sect.toUpperCase()).ordinal());
                }
            }
        } catch (Exception ex) {
            _log.error(ex.toString());
            // assume that an invalid value was passed in the sections parameter, print out exception out
            output.setupExceptionOutput("Invalid value for 'Sections' parameter, please see GetCapabilities for valid values.");
        }
    }
}
