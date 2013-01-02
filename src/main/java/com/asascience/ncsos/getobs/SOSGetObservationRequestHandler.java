package com.asascience.ncsos.getobs;

import com.asascience.ncsos.cdmclasses.*;
import com.asascience.ncsos.outputformatter.GetCapsOutputter;
import com.asascience.ncsos.outputformatter.OosTethysSwe;
import com.asascience.ncsos.outputformatter.OosTethysSweV2;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import ucar.ma2.DataType;
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
    
    private String[] obsProperties;
    private String[] procedures;
    private iStationData CDMDataSet;
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSGetObservationRequestHandler.class);
    private String contentType;
    
    private static final String FILL_VALUE_NAME = "_FillValue";

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
            String[] variableNames,
            String[] eventTime,
            String outputFormat,
            Map<String, String> latLonRequest) throws IOException {
        super(netCDFDataset);
        
        // set up our formatter
        if(outputFormat.equalsIgnoreCase("text/xml;subtype=\"om/1.0.0\"")) {
            contentType = "text/xml";
//            output = new OosTethysSwe(this.obsProperties, getFeatureDataset(), CDMDataSet, netCDFDataset);
            output = new OosTethysSweV2(this);
        } else {
            _log.error("Uknown/Unhandled responseFormat: " + outputFormat);
            output = new GetCapsOutputter();
            output.setupExceptionOutput("Could not recognize output format");
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
        
        // get all station names if 'network-all'
        if (requestedStationNames.length == 1 && requestedStationNames[0].equalsIgnoreCase("network-all")) {
            requestedStationNames = getStationNames().values().toArray(new String[getStationNames().values().size()]);
        }
        
        this.procedures = requestedStationNames;
        
        setCDMDatasetForStations(netCDFDataset, requestedStationNames, eventTime, latLonRequest);
    }
    
    private void setCDMDatasetForStations(NetcdfDataset netCDFDataset, String[] requestedStationNames, String[] eventTime, Map<String, String> latLonRequest) throws IOException {
        // strip out text if the station is defined by indices
        if (isStationDefinedByIndices()) {
            String[] editedStationNames = new String[requestedStationNames.length];
            for (int i=0; i<requestedStationNames.length; i++) {
                if (requestedStationNames[i].contains("unknown")) {
                    editedStationNames[i] = "unknown";
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
                depthAxis = (netCDFDataset.findVariable("depth"));
                if (depthAxis != null) {
                    this.obsProperties = checkNetcdfFileForAxis((CoordinateAxis1D) depthAxis, this.obsProperties);
                }
                this.obsProperties = checkNetcdfFileForAxis(netCDFDataset.findCoordinateAxis(AxisType.Lat), this.obsProperties);
                this.obsProperties = checkNetcdfFileForAxis(netCDFDataset.findCoordinateAxis(AxisType.Lon), this.obsProperties);

                CDMDataSet = new Grid(requestedStationNames, eventTime, this.obsProperties, latLonRequest);
                CDMDataSet.setData(getGridDataset());
            } 
        }
        //if the stations are not of cdm type grid then check to see and set cdm data type        
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
            }else {
                _log.error("Have a null CDMDataSet, this will cause a null reference exception!");
                // print exception and then return the doc
                output = new GetCapsOutputter();
                output.setupExceptionOutput("Null Dataset; could not recognize feature type");
                CDMDataSet = null;
                return;
            }
            //only set the data is it is valid
            if (CDMDataSet!=null){
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
        for(int s = 0;s<CDMDataSet.getNumberOfStations();s++) {
            String dataString = CDMDataSet.getDataResponse(s);
            for (String dataPoint : dataString.split(";")) {
                if(!dataPoint.equals(""))
                    output.addDataFormattedStringToInfoList(dataPoint);
            }
        }                
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
        String retval = CDMDataSet.getDataResponse(relIndex);
        return retval.replaceAll("\\.", decimal).replaceAll(",", token).replaceAll(";", block);
//        return retval;
    }
    //</editor-fold>

    public String getFillValue(String obsProp) {
        Attribute[] attrs = getAttributesOfVariable(obsProp);
        for (Attribute attr : attrs) {
            if (attr.getName().equalsIgnoreCase(FILL_VALUE_NAME)) {
                return attr.getValue(0).toString();
            }
        }
        return "";
    }

    public boolean hasFillValue(String obsProp) {
        Attribute[] attrs = getAttributesOfVariable(obsProp);
        if (attrs == null)
            return false;
        for (Attribute attr : attrs) {
            if (attr.getName().equalsIgnoreCase(FILL_VALUE_NAME))
                return true;
        }
        return false;
        
    }
}
