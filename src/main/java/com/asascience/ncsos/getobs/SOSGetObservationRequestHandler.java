package com.asascience.ncsos.getobs;

import com.asascience.ncsos.cdmclasses.*;
import com.asascience.ncsos.outputformatter.gc.GetCapsOutputter;
import com.asascience.ncsos.outputformatter.go.Ioos10;
import com.asascience.ncsos.outputformatter.go.OosTethys;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.ListComprehension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Get Observation Parser
 * @author abird
 */
public class SOSGetObservationRequestHandler extends SOSBaseRequestHandler {
    public static final String DEPTH = "depth";
    public static final String STANDARD_NAME = "standard_name";

    public static final String TEXTXML = "text/xml";
    public static final String UNKNOWN = "unknown";
    private String[] obsProperties;
    private String[] procedures;
    private iStationData CDMDataSet;
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSGetObservationRequestHandler.class);
    private String contentType;
    private static final String FILL_VALUE_NAME = "_FillValue";
    private static final String IOOSOM1_0_0 = "text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0\"";
    private static final String OM1_0_0 = "text/xml;subtype=\"om/1.0.0\"";
    private final List<String> eventTimes;

    /**
     * SOS get obs request handler
     * @param netCDFDataset dataset for which the get observation request is being made
     * @param requestedStationNames collection of offerings from the request
     * @param variableNames collection of observed properties from the request
     * @param eventTime event time range from the request
     * @param outputFormat response format from the request
     * @param latLonRequest map of the latitudes and longitude (points or ranges) from the request
     * @throws IOException 
     */
    public SOSGetObservationRequestHandler(NetcdfDataset netCDFDataset,
            String[] requestedStationNames,
            String offering,
            String[] variableNames,
            String[] eventTime,
            String responseFormat,
            Map<String, String> latLonRequest) throws IOException {
        super(netCDFDataset);

        if (eventTime != null && eventTime.length > 0) {
            eventTimes = Arrays.asList(eventTime);
        } else {
            eventTimes = new ArrayList<String>();
        }

        // set up our formatter
        if (responseFormat.equalsIgnoreCase(OM1_0_0)) {
            contentType = TEXTXML;
//            output = new OosTethysSwe(this.obsProperties, getFeatureDataset(), CDMDataSet, netCDFDataset);
            output = new OosTethys(this);
        } else if (responseFormat.equalsIgnoreCase(IOOSOM1_0_0)) {
            contentType = TEXTXML;
            output = new Ioos10(this);
        } else {
            _log.error("Unknown/Unhandled responseFormat: " + responseFormat);
            output = new GetCapsOutputter();
            output.setupExceptionOutput("Could not recognize response format: " + responseFormat);
        }

        // make sure that all of the requested variable names are in the dataset
        for (String vars : variableNames) {
            boolean isInDataset = false;
            for (Variable dVar : netCDFDataset.getVariables()) {
                if (dVar.getFullName().equalsIgnoreCase(vars)) {
                    isInDataset = true;
                    break;
                }
            }
            if (!isInDataset) {
                // make output an exception
                _log.error("observed property - " + vars + " - was not found in the dataset");
                // print exception and then return the doc
                output = new GetCapsOutputter();
                output.setupExceptionOutput("observed property - " + vars + " - was not found in the dataset");
                CDMDataSet = null;
                return;
            }
        }

        //this.stationName = stationName[0];        
        CoordinateAxis heightAxis = netCDFDataset.findCoordinateAxis(AxisType.Height);

        this.obsProperties = checkNetcdfFileForAxis(heightAxis, variableNames);


        // strip out each of the station names

        // unaltered procedures
        this.procedures = Arrays.copyOf(requestedStationNames, requestedStationNames.length);
        for (int i = 0; i < requestedStationNames.length; i++) {
            requestedStationNames[i] = requestedStationNames[i].substring(requestedStationNames[i].lastIndexOf(":") + 1);
        }

        // get all station names if 'network-all'
        try {
            if (requestedStationNames.length == 1 && requestedStationNames[0].equalsIgnoreCase("all")) {
                requestedStationNames = getStationNames().values().toArray(new String[getStationNames().values().size()]);
                // need to set the procedures to this new set
                List<String> naProcs = ListComprehension.map(new ArrayList<String>(getStationNames().values()) {
                }, new ListComprehension.Func<String, String>() {

                    public String apply(String in) {
                        return SOSBaseRequestHandler.getGMLName(in);
                    }
                });
                this.procedures = naProcs.toArray(new String[naProcs.size()]);
            }
        } catch (Exception ex) {
            _log.error(ex.toString());
            requestedStationNames = null;
        }


        // check that the procedures are valid
        checkProcedureValidity();
        // and are a part of the offering

        if (offering != null) {
            checkProceduresAgainstOffering(offering);
        }

        setCDMDatasetForStations(netCDFDataset, requestedStationNames, eventTime, latLonRequest);
    }

    private void setCDMDatasetForStations(NetcdfDataset netCDFDataset, String[] requestedStationNames, String[] eventTime, Map<String, String> latLonRequest) throws IOException {
        // strip out text if the station is defined by indices
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
        //grid operation
        if (getDatasetFeatureType() == FeatureType.GRID) {
            Variable depthAxis;
            if (!latLonRequest.isEmpty()) {
                depthAxis = (netCDFDataset.findVariable(DEPTH));
                if (depthAxis != null) {
                    this.obsProperties = checkNetcdfFileForAxis((CoordinateAxis1D) depthAxis, this.obsProperties);
                }
                this.obsProperties = checkNetcdfFileForAxis(netCDFDataset.findCoordinateAxis(AxisType.Lat), this.obsProperties);
                this.obsProperties = checkNetcdfFileForAxis(netCDFDataset.findCoordinateAxis(AxisType.Lon), this.obsProperties);

                CDMDataSet = new Grid(requestedStationNames, eventTime, this.obsProperties, latLonRequest);
                CDMDataSet.setData(getGridDataset());
            }
        } //if the stations are not of cdm type grid then check to see and set cdm data type        
        else {

            if (getDatasetFeatureType() == FeatureType.TRAJECTORY) {
                CDMDataSet = new Trajectory(requestedStationNames, eventTime, this.obsProperties);
            } else if (getDatasetFeatureType() == FeatureType.STATION) {
                CDMDataSet = new TimeSeries(requestedStationNames, eventTime, this.obsProperties);
            } else if (getDatasetFeatureType() == FeatureType.STATION_PROFILE) {
                CDMDataSet = new TimeSeriesProfile(requestedStationNames, eventTime, this.obsProperties);
            } else if (getDatasetFeatureType() == FeatureType.PROFILE) {
                CDMDataSet = new Profile(requestedStationNames, eventTime, this.obsProperties);
            } else if (getDatasetFeatureType() == FeatureType.SECTION) {
                CDMDataSet = new Section(requestedStationNames, eventTime, this.obsProperties);
            } else {
                _log.error("Have a null CDMDataSet, this will cause a null reference exception!");
                // print exception and then return the doc
                output = new GetCapsOutputter();
                output.setupExceptionOutput("Null Dataset; could not recognize feature type");
                CDMDataSet = null;
                return;
            }
            //only set the data is it is valid
            if (CDMDataSet != null) {
                CDMDataSet.setData(getFeatureTypeDataSet());
            }
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
     * sets the output to display an exception message
     * @param exceptionMessage the exception message to display in the return
     */
    public void setException(String exceptionMessage) {
        output.setupExceptionOutput(exceptionMessage);
    }

    /**
     * Create the observation data for getObs, passing it to our formatter
     */
    public void parseObservations() {
        for (int s = 0; s < CDMDataSet.getNumberOfStations(); s++) {
            String dataString = CDMDataSet.getDataResponse(s);
            for (String dataPoint : dataString.split(";")) {
                if (!dataPoint.equals("")) {
                    output.addDataFormattedStringToInfoList(dataPoint);
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

    /**
     * The content type header for the response
     * @return the content type of the response (text/xml)
     */
    public String getContentType() {
        return contentType;
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
//        return retval;
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

    private void checkProcedureValidity() {
        List<String> stProc = new ArrayList<String>();
        stProc.add(SOSBaseRequestHandler.getGMLNetworkAll());
        for (String stname : this.getStationNames().values()) {
            for (String senname : this.getSensorNames()) {
                stProc.add(SOSBaseRequestHandler.getSensorGMLName(stname, senname));
            }
            stProc.add(SOSBaseRequestHandler.getGMLName(stname));
        }

        for (String proc : this.procedures) {
            if (ListComprehension.filter(stProc, proc).size() < 1) {
                _log.error("Invalid procedure: " + proc);
                output.setupExceptionOutput("Invalid procedure " + proc + ". Check GetCapabilities document for valid procedures.");
            }
        }

    }

    private void checkProceduresAgainstOffering(String offering) {
        // if the offering is 'network-all' no error (network-all should have all procedures)
        if (offering.equalsIgnoreCase("network-all")) {
            return;
        }
        // currently in ncSOS the only offerings that exist are network-all and each of the stations
        // in the dataset. So basically we need to check that the offering exists
        // in each of the procedures requested.
        for (String proc : this.procedures) {
            if (!proc.toLowerCase().contains(offering.toLowerCase())) {
                _log.error("Invalid procedure " + proc + " for offering " + offering);
                output.setupExceptionOutput("Procedure " + proc + " does not exist in the offering " + offering + ". Check GetCapabilities document for valid procedures for this offering.");
            }
        }

    }
}
