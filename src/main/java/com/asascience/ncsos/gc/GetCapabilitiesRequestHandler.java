package com.asascience.ncsos.gc;

import com.asascience.ncsos.outputformatter.ErrorFormatter;
import com.asascience.ncsos.outputformatter.gc.GetCapsFormatter;
import com.asascience.ncsos.service.BaseRequestHandler;
import com.asascience.ncsos.util.DatasetHandlerAdapter;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.*;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;

/**
 * Creates basic Get Capabilites request handler that can read from a netcdf dataset
 * the information needed to populate a get capabilities template.
 * @author Abird
 * @version 1.0.0
 */
public class GetCapabilitiesRequestHandler extends BaseRequestHandler {

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
    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(GetCapabilitiesRequestHandler.class);

    /**
     * Creates an instance of GetCapabilitiesRequestHandler to handle the dataset
     * and uri from the thredds request.
     * @param netCDFDataset dataset for which the Get Capabilities request is being
     * directed to
     * @param threddsURI uri from the thredds Get Capabilities request
     * @param sections string detailing what sections of the GC response should be returned
     * @throws IOException
     */
    public GetCapabilitiesRequestHandler(NetcdfDataset netCDFDataset, String threddsURI, String sections) throws IOException {
        super(netCDFDataset, false);
        this.threddsURI = threddsURI;
        this.sections = sections.toLowerCase();
        this.formatter = new GetCapsFormatter(this);

        

        SetSectionBits();
    }

    
    private void initializeDataParams() throws IOException{
    	if(!this.isInitialized){
    		this.initializeDataset();
    		if (getFeatureDataset() == null) {
                // error, couldn't read dataset
                formatter = new ErrorFormatter();
                StringBuffer sb = new StringBuffer();
                if(netCDFDataset == null) {
                  ((ErrorFormatter)formatter).setException("Unable to read the dataset's feature type. NULL dataset.");
                } else if(FeatureDatasetFactoryManager.findFeatureType(netCDFDataset) == null) {
                  ((ErrorFormatter)formatter).setException("Unable to read the dataset's feature type. Null feature type reported by netCDF");
                } else {
                  ((ErrorFormatter)formatter).setException("Unable to read the dataset's feature type. Reported as " + FeatureDatasetFactoryManager.findFeatureType(netCDFDataset).toString() + "; unable to process.");
                }
                return;
            }
            CalculateBoundsForFeatureSet();
            

    		
    	}
    	
    }
    public void resetCapabilitiesSections(String sections) throws IOException {
        this.sections = sections.toLowerCase();
        this.requestedSections = new BitSet(SECTION_COUNT);
        SetSectionBits();
    }

    /**
     * Creates the output for the get capabilities response
     * @throws IOException 
     */
    public void parseGetCapabilitiesDocument() throws IOException {

        if(this.requestedSections.cardinality() > 0){
            if (!(this.requestedSections.get(Sections.SERVICEIDENTIFICATION.ordinal()) &&
                    this.requestedSections.cardinality() == 1)) 
                this.initializeDataParams();
            else
                parseGlobalAttributes();
        }
        // early exit if we have an exception output    	
    	if (formatter instanceof ErrorFormatter) {
            return;
        }
        GetCapsFormatter out = (GetCapsFormatter) formatter;

        // service identification; parse if it is the section identified or 'all'
        if (this.requestedSections.get(Sections.SERVICEIDENTIFICATION.ordinal())) {
            out.parseServiceIdentification(this.global_attributes);
        } else {
            // remove identification from doc
            out.removeServiceIdentification();
        }
//// check here to see if we need to initialize the data
//        if(this.requestedSections.get(Sections.SERVICEIDENTIFICATION.ordinal()) &&
//        		this.requestedSections.cardinality() == 1)){
//        	
//        }
        // service provider; parse if it is the section identified or 'all'
        if (this.requestedSections.get(Sections.SERVICEPROVIDER.ordinal())) {
        	
            out.parseServiceDescription();
        } else {
            // remove service provider from doc
            out.removeServiceProvider();
        }

        // operations metadata; parse if it is the section identified or 'all'
        if (this.requestedSections.get(Sections.OPERATIONSMETADATA.ordinal())) {
        	
            // Set the THREDDS URI
            out.setURL(threddsURI);
            // Set the GetObservation Operation
            out.setOperationsMetadataGetObs(threddsURI, getSensorNames(), getStationNames().values().toArray(new String[getStationNames().values().size()]));
            // Set the DescribeSensor Operation
            out.setOperationsMetadataDescSen(threddsURI, getSensorNames(), getStationNames().values().toArray(new String[getStationNames().values().size()]));
            // Set the ExtendedCapabilities
            out.setVersionMetadata();
        } else {
            // remove operations metadata
            out.removeOperationsMetadata();
        }

        // Contents; parse if it is the section identified or 'all'
        if (this.requestedSections.get(Sections.CONTENTS.ordinal())) {
            // observation offering list
            // network-all
            // get the bounds
        	// early exit if we have an exception output
        
            Double latMin = Double.MAX_VALUE, latMax = Double.NEGATIVE_INFINITY, lonMin = Double.MAX_VALUE, lonMax = Double.NEGATIVE_INFINITY;
            for (LatLonRect rect : stationBBox.values()) {
                latMin = (latMin > rect.getLatMin()) ? rect.getLatMin() : latMin;
                latMax = (latMax < rect.getLatMax()) ? rect.getLatMax() : latMax;
                lonMin = (lonMin > rect.getLonMin()) ? rect.getLonMin() : lonMin;
                lonMax = (lonMax < rect.getLonMax()) ? rect.getLonMax() : lonMax;
            }
            LatLonRect setRange = new LatLonRect(new LatLonPointImpl(latMin, lonMin), new LatLonPointImpl(latMax, lonMax));
            CalendarDateRange setTime = null;
            if (setStartDate != null && setEndDate != null) {
                setTime = CalendarDateRange.of(setStartDate, setEndDate);
            }

            out.setObservationOfferingNetwork(setRange, getStationNames().values().toArray(new String[getStationNames().values().size()]), getSensorNames(), setTime, this.getFeatureDataset().getFeatureType());
            // Add an offering for every station
            for (Integer index : getStationNames().keySet()) {
                ((GetCapsFormatter) formatter).setObservationOffering(this.getUrnName(getStationNames().get(index)), stationBBox.get(index), getSensorNames(), stationDateRange.get(index), this.getFeatureDataset().getFeatureType());
            }
        } else {
            // remove Contents node
            out.removeContents();
        }
    }

    private void CalculateBoundsForFeatureSet() throws IOException {
        FeatureType featype = this.getDatasetFeatureType();
        if (featype != null) {
            this.stationDateRange = new HashMap<Integer, CalendarDateRange>();
            this.stationBBox = new HashMap<Integer, LatLonRect>();
            CalendarDate start = null, end = null;
            int stationIndex = 0;
            switch (featype) {
                case TRAJECTORY:
                    try {
                        TrajectoryFeatureCollection collection = (TrajectoryFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while (collection.hasNext()) {
                            TrajectoryFeature feature = collection.next();
                            if (DatasetHandlerAdapter.calcBounds(feature)) {
                                if (start == null || start.isAfter(feature.getCalendarDateRange().getStart())) {
                                    start = feature.getCalendarDateRange().getStart();
                                }
                                if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd())) {
                                    end = feature.getCalendarDateRange().getEnd();
                                }
                                this.stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                                this.stationBBox.put(stationIndex, feature.getBoundingBox());
                                stationIndex++;
                            } else {
                                GetExtentsFromSubFeatures(feature, stationIndex);
                            }
                        }
                    } catch (Exception ex) {
                        _log.error(ex.getMessage(), ex);
                    }
                    break;
                case STATION:
                    try {
                        StationTimeSeriesFeatureCollection collection = (StationTimeSeriesFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while (collection.hasNext()) {
                            StationTimeSeriesFeature feature = collection.next();
                            if (DatasetHandlerAdapter.calcBounds(feature)) {
                                if (start == null || start.isAfter(feature.getCalendarDateRange().getStart())) {
                                    start = feature.getCalendarDateRange().getStart();
                                }
                                if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd())) {
                                    end = feature.getCalendarDateRange().getEnd();
                                }
                                this.stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                                this.stationBBox.put(stationIndex, feature.getBoundingBox());
                            } else {
                                GetExtentsFromSubFeatures(feature, stationIndex);
                            }
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        _log.error(ex.getMessage(), ex);
                    }
                    break;
                case PROFILE:
                    try {
                        ProfileFeatureCollection collection = (ProfileFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while (collection.hasNext()) {
                            ProfileFeature feature = collection.next();
                            if (DatasetHandlerAdapter.calcBounds(feature)) {
                                CalendarDate profileDate = CalendarDate.of(feature.getTime());
                                if (start == null || start.isAfter(profileDate)) {
                                    start = profileDate;
                                }
                                if (end == null || end.isBefore(profileDate)) {
                                    end = profileDate;
                                }
                                this.stationDateRange.put(stationIndex, CalendarDateRange.of(profileDate, profileDate));
                                this.stationBBox.put(stationIndex, new LatLonRect(feature.getLatLon(), feature.getLatLon()));
                                stationIndex++;
                            } else {
                                GetExtentsFromSubFeatures(feature, stationIndex);
                            }
                        }
                    } catch (Exception ex) {
                        _log.error(ex.getMessage(), ex);
                    }
                    break;
                case GRID:
                    start = getGridDataset().getCalendarDateStart();
                    end = getGridDataset().getCalendarDateEnd();
                    this.stationDateRange.put(0, CalendarDateRange.of(start, end));
                    break;
                case STATION_PROFILE:
                    try {
                        CalendarDateRange nullrange = null;
                        StationProfileFeatureCollection collection = (StationProfileFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while (collection.hasNext()) {
                            StationProfileFeature feature = collection.next();
                            PointFeatureCollection flattened = feature.flatten(null, nullrange);
                            if (DatasetHandlerAdapter.calcBounds(flattened)) {
                                if (start == null || start.isAfter(flattened.getCalendarDateRange().getStart())) {
                                    start = flattened.getCalendarDateRange().getStart();
                                }
                                if (end == null || end.isBefore(flattened.getCalendarDateRange().getEnd())) {
                                    end = flattened.getCalendarDateRange().getEnd();
                                }
                                this.stationDateRange.put(stationIndex, flattened.getCalendarDateRange());
                                this.stationBBox.put(stationIndex, flattened.getBoundingBox());
                                stationIndex++;
                            } else {
                                GetExtentsFromSubFeatures(flattened, stationIndex);
                            }
                        }
                    } catch (Exception ex) {
                        _log.error(ex.getMessage(), ex);
                    }
                    break;
                case SECTION:
                    try {
                        CalendarDateRange nullrange = null;
                        SectionFeatureCollection collection = (SectionFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while (collection.hasNext()) {
                            SectionFeature feature = collection.next();
                            PointFeatureCollection flattened = feature.flatten(null, nullrange);
                            if (DatasetHandlerAdapter.calcBounds(flattened)) {
                                if (start == null || start.isAfter(flattened.getCalendarDateRange().getStart())) {
                                    start = flattened.getCalendarDateRange().getStart();
                                }
                                if (end == null || end.isBefore(flattened.getCalendarDateRange().getEnd())) {
                                    end = flattened.getCalendarDateRange().getEnd();
                                }
                                this.stationDateRange.put(stationIndex, flattened.getCalendarDateRange());
                                this.stationBBox.put(stationIndex, flattened.getBoundingBox());
                                stationIndex++;
                            } else {
                                GetExtentsFromSubFeatures(flattened, stationIndex);
                            }
                        }
                    } catch (Exception ex) {
                        _log.error(ex.getMessage(), ex);
                    }
                    break;
                case POINT:
                    _log.error("NcSOS does not support the Point featureType at this time.");
                    formatter = new ErrorFormatter();
                    ((ErrorFormatter)formatter).setException("NcSOS does not support the Point featureType at this time.");
                    return;
                default:
                    _log.error("Unknown feature type - NetCDF-Java could not figure out what this dataset was!");
                    formatter = new ErrorFormatter();
                    ((ErrorFormatter)formatter).setException("Unknown feature type - NetCDF-Java could not figure out what this dataset was!");
                    return;
            }
            this.setStartDate = start;
            this.setEndDate = end;
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
            for (coll.resetIteration(); coll.hasNext();) {
                PointFeature pf = coll.next();
                if (pf.getObservationTimeAsCalendarDate().isAfter(end)) {
                    end = pf.getObservationTimeAsCalendarDate();
                } else if (pf.getObservationTimeAsCalendarDate().isBefore(start)) {
                    start = pf.getObservationTimeAsCalendarDate();
                }

                if (minLat > pf.getLocation().getLatitude()) {
                    minLat = pf.getLocation().getLatitude();
                } else if (maxLat < pf.getLocation().getLatitude()) {
                    maxLat = pf.getLocation().getLatitude();
                }

                if (minLon > pf.getLocation().getLongitude()) {
                    minLon = pf.getLocation().getLongitude();
                } else if (maxLon < pf.getLocation().getLongitude()) {
                    maxLon = pf.getLocation().getLongitude();
                }
            }
            // add the values to the table
            this.stationDateRange.put(index, CalendarDateRange.of(start, end));
            this.stationBBox.put(index, new LatLonRect(new LatLonPointImpl(minLat, minLon), new LatLonPointImpl(maxLat, maxLon)));
        } catch (Exception ex) {
            // failed, um just add global bounds
            _log.error("GetExtentsFromSubFeatures: Could not manually get extents, adding globals...\n\t" + ex.toString());
            this.stationDateRange.put(index, CalendarDateRange.of(CalendarDate.of(0), CalendarDate.of(CalendarDate.present().getDifferenceInMsecs(CalendarDate.of(0)))));
            this.stationBBox.put(index, new LatLonRect(new LatLonPointImpl(-90, -180), new LatLonPointImpl(90, 180)));
        } catch (Error err) {
            _log.error("GetExtentsFromSubFeatures: Could not manually get extents, adding globals...\n\t" + err.toString());
            this.stationDateRange.put(index, CalendarDateRange.of(CalendarDate.of(0), CalendarDate.of(CalendarDate.present().getDifferenceInMsecs(CalendarDate.of(0)))));
            this.stationBBox.put(index, new LatLonRect(new LatLonPointImpl(-90, -180), new LatLonPointImpl(90, 180)));
        }
    }

    private void SetSectionBits() throws IOException {
        this.requestedSections = new BitSet(this.SECTION_COUNT);
        try {
            for (String sect : this.sections.split(",")) {
                if (sect.equals("all")) {
                    this.requestedSections.set(0, this.SECTION_COUNT);
                }
                else if(sect.equals("")){
                    continue;
                }
                else {
                    this.requestedSections.set(Sections.valueOf(sect.toUpperCase()).ordinal());
                }
            }
        } catch (Exception ex) {
            _log.error(ex.toString());
            // assume that an invalid value was passed in the sections parameter, print out exception out
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException("Invalid value for 'Sections' parameter, please see GetCapabilities for valid values.", INVALID_PARAMETER, "sections");
        }
    }
}
