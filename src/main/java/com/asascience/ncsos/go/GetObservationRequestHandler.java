package com.asascience.ncsos.go;

import com.asascience.ncsos.cdmclasses.*;
import com.asascience.ncsos.outputformatter.ErrorFormatter;
import com.asascience.ncsos.outputformatter.go.Ioos10Formatter;
import com.asascience.ncsos.outputformatter.go.OosTethysFormatter;
import com.asascience.ncsos.service.BaseRequestHandler;
import com.asascience.ncsos.util.ListComprehension;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.CF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GetObservationRequestHandler extends BaseRequestHandler {
    public static final String DEPTH = "depth";
    public static final String STANDARD_NAME = "standard_name";
    private static final String LAT = "latitude";
    private static final String LON = "longitude";

    public static final String TEXTXML = "text/xml";
    public static final String UNKNOWN = "unknown";
    private String[] obsProperties;
    private String[] procedures;
    private iStationData CDMDataSet;
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(GetObservationRequestHandler.class);
    private static final String FILL_VALUE_NAME = "_FillValue";
    private static final String IOOS10_RESPONSE_FORMAT = "text/xml;schema=\"om/1.0.0/profiles/ioos_sos/1.0\"";
    private static final String OOSTETHYS_RESPONSE_FORMAT = "text/xml;schema=\"om/1.0.0\"";
    private final List<String> eventTimes;

    /**
     * SOS get obs request handler
     * @param netCDFDataset dataset for which the get observation request is being made
     * @param requestedStationNames collection of offerings from the request
     * @param variableNames collection of observed properties from the request
     * @param eventTime event time range from the request
     * @param responseFormat response format from the request
     * @param latLonRequest map of the latitudes and longitude (points or ranges) from the request
     * @throws IOException 
     */
    public GetObservationRequestHandler(NetcdfDataset netCDFDataset,
                                        String[] requestedProcedures,
                                        String offering,
                                        String[] variableNames,
                                        String[] eventTime,
                                        String responseFormat,
                                        Map<String, String> latLonRequest) throws IOException {
        super(netCDFDataset);

        // Translate back to an URN.  (gml:id fields in XML can't have colons)
        offering = offering.replace("_-_",":");

        if (eventTime != null && eventTime.length > 0) {
            eventTimes = Arrays.asList(eventTime);
        } else {
            eventTimes = new ArrayList<String>();
        }

        // set up our formatter
        if (responseFormat.equalsIgnoreCase(OOSTETHYS_RESPONSE_FORMAT)) {
            formatter = new OosTethysFormatter(this);
        } else if (responseFormat.equalsIgnoreCase(IOOS10_RESPONSE_FORMAT)) {
            formatter = new Ioos10Formatter(this);
        } else {
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException("Could not recognize response format: " + responseFormat, INVALID_PARAMETER, "responseFormat");
        }

        // Since the obsevedProperties can be standard_name attributes, map everything to an actual variable name here.

        String[] actualVariableNames = variableNames.clone();

        // make sure that all of the requested variable names are in the dataset
        for (int i = 0 ; i < variableNames.length ; i++) {
            String vars = variableNames[i];
            boolean isInDataset = false;
            for (Variable dVar : netCDFDataset.getVariables()) {
                if (dVar.getFullName().equalsIgnoreCase(vars)) {
                    isInDataset = true;
                    break;
                } else {
                    Attribute std = dVar.findAttributeIgnoreCase(CF.STANDARD_NAME);
                    if (std != null && std.getStringValue().equalsIgnoreCase(vars)) {
                        isInDataset = true;
                        // Replace standard_name with the variable name
                        actualVariableNames[i] = dVar.getFullName();
                    }
                }
            }
            if (!isInDataset) {
                formatter = new ErrorFormatter();
                ((ErrorFormatter)formatter).setException("observed property - " + vars + " - was not found in the dataset", INVALID_PARAMETER, "observedProperty");
                CDMDataSet = null;
                return;
            }
        }

        CoordinateAxis heightAxis = netCDFDataset.findCoordinateAxis(AxisType.Height);

        this.obsProperties = checkNetcdfFileForAxis(heightAxis, actualVariableNames);

        // Figure out what procedures to use...
        try {
            if (requestedProcedures == null) {
                if (offering.equalsIgnoreCase(this.getUrnNetworkAll())) {
                    // All procedures
                    requestedProcedures = getStationNames().values().toArray(new String[getStationNames().values().size()]);
                } else {
                    // Just the single procedure supplied by the offering
                    requestedProcedures = new String[1];
                    requestedProcedures[0] = offering;
                }
            } else {
                if (requestedProcedures.length == 1 && requestedProcedures[0].equalsIgnoreCase(getUrnNetworkAll())) {
                    requestedProcedures = getStationNames().values().toArray(new String[getStationNames().values().size()]);
                } else {
                    for (int i = 0; i < requestedProcedures.length; i++) {
                        requestedProcedures[i] = requestedProcedures[i].substring(requestedProcedures[i].lastIndexOf(":") + 1);
                    }
                }
            }
            // Now map them all to the station URN
            List<String> naProcs = ListComprehension.map(Arrays.asList(requestedProcedures),
                 new ListComprehension.Func<String, String>() {
                     public String apply(String in) {
                         return getUrnName(in);
                     }
                 }
            );
            this.procedures = naProcs.toArray(new String[naProcs.size()]);
        } catch (Exception ex) {
            _log.error(ex.toString());
            this.procedures = null;
        }

        // check that the procedures are valid
        checkProcedureValidity();
        // and are a part of the offering
        if (offering != null) {
            checkProceduresAgainstOffering(offering);
        }

        setCDMDatasetForStations(netCDFDataset, eventTime, latLonRequest);
    }

    private void setCDMDatasetForStations(NetcdfDataset netCDFDataset, String[] eventTime, Map<String, String> latLonRequest) throws IOException {
        // strip out text if the station is defined by indices
        /*
        if (isStationDefinedByIndices()) {
            String[] editedStationNames = new String[requestedStationNames.length];
            for (int i = 0; i < requestedStationNames.length; i++) {
                if (requestedStationNames[i].contains(UNKNOWN)) {
                    editedStationNames[i] = UNKNOWN;
                } else {
                    editedStationNames[i] = requestedStationNames[i].replaceAll("[A-Za-z]+", "");
                }
            }
            // copy array
            requestedStationNames = editedStationNames.clone();
        }
        */
        //grid operation
        if (getDatasetFeatureType() == FeatureType.GRID) {

            // Make sure latitude and longitude are specified
            if (!latLonRequest.containsKey(LON)) {
                formatter = new ErrorFormatter();
                ((ErrorFormatter)formatter).setException("No longitude point specified", MISSING_PARAMETER, "longitude");
                CDMDataSet = null;
                return;
            }
            if (!latLonRequest.containsKey(LAT)) {
                formatter = new ErrorFormatter();
                ((ErrorFormatter)formatter).setException("No latitude point specified", MISSING_PARAMETER, "latitude");
                CDMDataSet = null;
                return;
            }

            List<String> lats = Arrays.asList(latLonRequest.get(LAT).split(","));
            for (String s : lats) {
                try {
                    Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    formatter = new ErrorFormatter();
                    ((ErrorFormatter)formatter).setException("Invalid latitude specified", INVALID_PARAMETER, "latitude");
                    CDMDataSet = null;
                    return;
                }
            }
            List<String> lons = Arrays.asList(latLonRequest.get(LON).split(","));
            for (String s : lons) {
                try {
                    Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    formatter = new ErrorFormatter();
                    ((ErrorFormatter)formatter).setException("Invalid longitude specified", INVALID_PARAMETER, "longitude");
                    CDMDataSet = null;
                    return;
                }
            }


            Variable depthAxis;
            if (!latLonRequest.isEmpty()) {
                depthAxis = (netCDFDataset.findVariable(DEPTH));
                if (depthAxis != null) {
                    this.obsProperties = checkNetcdfFileForAxis((CoordinateAxis1D) depthAxis, this.obsProperties);
                }
                this.obsProperties = checkNetcdfFileForAxis(netCDFDataset.findCoordinateAxis(AxisType.Lat), this.obsProperties);
                this.obsProperties = checkNetcdfFileForAxis(netCDFDataset.findCoordinateAxis(AxisType.Lon), this.obsProperties);

                CDMDataSet = new Grid(this.procedures, eventTime, this.obsProperties, latLonRequest);
                CDMDataSet.setData(getGridDataset());
            }
        } //if the stations are not of cdm type grid then check to see and set cdm data type        
        else {
            if (getDatasetFeatureType() == FeatureType.TRAJECTORY) {
                CDMDataSet = new Trajectory(this.procedures, eventTime, this.obsProperties);
            } else if (getDatasetFeatureType() == FeatureType.STATION) {
                CDMDataSet = new TimeSeries(this.procedures, eventTime, this.obsProperties);
            } else if (getDatasetFeatureType() == FeatureType.STATION_PROFILE) {
                CDMDataSet = new TimeSeriesProfile(this.procedures, eventTime, this.obsProperties);
            } else if (getDatasetFeatureType() == FeatureType.PROFILE) {
                CDMDataSet = new Profile(this.procedures, eventTime, this.obsProperties);
            } else if (getDatasetFeatureType() == FeatureType.SECTION) {
                CDMDataSet = new Section(this.procedures, eventTime, this.obsProperties);
            } else {
                formatter = new ErrorFormatter();
                ((ErrorFormatter)formatter).setException("NetCDF-Java could not recognize the dataset's FeatureType");
                CDMDataSet = null;
                return;
            }
            //only set the data is it is valid
            CDMDataSet.setData(getFeatureTypeDataSet());
        }
    }

    /**
     * checks for the presence of height in the netcdf dataset if it finds it but not in the variables selected it adds it
     * @param Axis the axis being checked
     * @param variableNames1 the observed properties from the request (split)
     * @return updated observed properties (with altitude added, if found)
     */
    private String[] checkNetcdfFileForAxis(CoordinateAxis Axis, String[] variableNames1) {
        if (Axis != null) {
            List<String> variableNamesNew = new ArrayList<String>();
            //check to see if Z present
            boolean foundZ = false;
            for (int i = 0; i < variableNames1.length; i++) {
                String zAvail = variableNames1[i];

                if (zAvail.equalsIgnoreCase(Axis.getFullName())) {
                    foundZ = true;
                    break;
                }
            }

            //if it not found add it!
            if (!foundZ && !Axis.getDimensions().isEmpty()) {
                variableNamesNew = new ArrayList<String>();
                variableNamesNew.addAll(Arrays.asList(variableNames1));
                variableNamesNew.add(Axis.getFullName());
                variableNames1 = new String[variableNames1.length + 1];
                variableNames1 = (String[]) variableNamesNew.toArray(variableNames1);
                //*******************************
            }
        }
        return variableNames1;
    }

     /**
     * Create the observation data for go, passing it to our formatter
     */
    public void parseObservations() {
        for (int s = 0; s < CDMDataSet.getNumberOfStations(); s++) {
            String dataString = CDMDataSet.getDataResponse(s);
            for (String dataPoint : dataString.split(";")) {
                if (!dataPoint.equals("")) {
                    formatter.addDataFormattedStringToInfoList(dataPoint);
                }
            }
        }
    }

    /**
     * Returns the 'standard_name' attribute of a variable, if it exists
     * @param varName the name of the variable
     * @return the 'standard_name' if it exists, otherwise ""
     */
    public String getVariableStandardName(String varName) {
        String retval = UNKNOWN;

        for (Variable var : netCDFDataset.getVariables()) {
            if (varName.equalsIgnoreCase(var.getFullName())) {
                Attribute attr = var.findAttribute(STANDARD_NAME);
                if (attr != null) {
                    retval = attr.getStringValue();
                }
            }
        }

        return retval;
    }

    public List<String> getRequestedEventTimes() {
        return this.eventTimes;
    }

    /**
     * Gets the dataset wrapped by the cdm feature type giving multiple easy to 
     * access functions
     * @return dataset wrapped by iStationData
     */
    public iStationData getCDMDataset() {
        return CDMDataSet;
    }

    //<editor-fold defaultstate="collapsed" desc="Helper functions for building GetObs XML">
    /**
     * Looks up a stations index by a string name
     * @param stName the name of the station to look for
     * @return the index of the station (-1 if it does not exist)
     */
    public int getIndexFromStationName(String stName) {
        return getStationIndex(stName);
    }

    public String getStationLowerCorner(int relIndex) {
        return formatDegree(CDMDataSet.getLowerLat(relIndex)) + " " + formatDegree(CDMDataSet.getLowerLon(relIndex));
    }

    public String getStationUpperCorner(int relIndex) {
        return formatDegree(CDMDataSet.getUpperLat(relIndex)) + " " + formatDegree(CDMDataSet.getUpperLon(relIndex));
    }

    public String getBoundedLowerCorner() {
        return formatDegree(CDMDataSet.getBoundLowerLat()) + " " + formatDegree(CDMDataSet.getBoundLowerLon());
    }

    public String getBoundedUpperCorner() {
        return formatDegree(CDMDataSet.getBoundUpperLat()) + " " + formatDegree(CDMDataSet.getBoundUpperLon());
    }

    public String getStartTime(int relIndex) {
        return CDMDataSet.getTimeBegin(relIndex);
    }

    public String getEndTime(int relIndex) {
        return CDMDataSet.getTimeEnd(relIndex);
    }

    public List<String> getRequestedObservedProperties() {
        CoordinateAxis heightAxis = netCDFDataset.findCoordinateAxis(AxisType.Height);

        List<String> retval = Arrays.asList(obsProperties);

        if (heightAxis != null) {
            retval = ListComprehension.filterOut(retval, heightAxis.getShortName());
        }

        return retval;
    }

    public String[] getObservedProperties() {
        return obsProperties;
    }

    public String[] getProcedures() {
        return procedures;
    }

    public String getUnitsString(String dataVarName) {
        return getUnitsOfVariable(dataVarName);
    }

    public String getValueBlockForAllObs(String block, String decimal, String token, int relIndex) {
        _log.info("Getting data for index: " + relIndex);
        String retval = CDMDataSet.getDataResponse(relIndex);
        return retval.replaceAll("\\.", decimal).replaceAll(",", token).replaceAll(";", block);
    }
    //</editor-fold>

    public String getFillValue(String obsProp) {
        Attribute[] attrs = getAttributesOfVariable(obsProp);
        for (Attribute attr : attrs) {
            if (attr.getFullNameEscaped().equalsIgnoreCase(FILL_VALUE_NAME)) {
                return attr.getValue(0).toString();
            }
        }
        return "";
    }

    public boolean hasFillValue(String obsProp) {
        Attribute[] attrs = getAttributesOfVariable(obsProp);
        if (attrs == null) {
            return false;
        }
        for (Attribute attr : attrs) {
            if (attr.getFullNameEscaped().equalsIgnoreCase(FILL_VALUE_NAME)) {
                return true;
            }
        }
        return false;

    }

    private void checkProcedureValidity() throws IOException {
        List<String> stProc = new ArrayList<String>();
        stProc.add(this.getUrnNetworkAll());
        for (String stname : this.getStationNames().values()) {
            for (String senname : this.getSensorNames()) {
                stProc.add(this.getSensorUrnName(stname, senname));
            }
            stProc.add(this.getUrnName(stname));
        }

        for (String proc : this.procedures) {
            if (ListComprehension.filter(stProc, proc).size() < 1) {
                formatter = new ErrorFormatter();
                ((ErrorFormatter)formatter).setException("Invalid procedure " + proc + ". Check GetCapabilities document for valid procedures.", INVALID_PARAMETER, "procedure");
            }
        }

    }

    private void checkProceduresAgainstOffering(String offering) throws IOException {
        // if the offering is 'network-all' no error (network-all should have all procedures)
        if (offering.equalsIgnoreCase(this.getUrnNetworkAll())) {
            return;
        }
        // currently in ncSOS the only offerings that exist are network-all and each of the stations
        // in the dataset. So basically we need to check that the offering exists
        // in each of the procedures requested.
        for (String proc : this.procedures) {
            if (!proc.toLowerCase().contains(offering.toLowerCase())) {
                formatter = new ErrorFormatter();
                ((ErrorFormatter)formatter).setException("Offering: " + proc + " does not exist in the dataset.  Check GetCapabilities document for valid offerings.", INVALID_PARAMETER, "offering");
            }
        }

    }
}
