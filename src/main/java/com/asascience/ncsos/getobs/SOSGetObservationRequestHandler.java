package com.asascience.ncsos.getobs;

import com.asascience.ncsos.cdmclasses.*;
import com.asascience.ncsos.outputformatter.GetCapsOutputter;
import com.asascience.ncsos.outputformatter.OosTethysSwe;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    
    private String[] variableNames;
    private iStationData CDMDataSet;
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSGetObservationRequestHandler.class);
    private String contentType;

    /**
     * SOS get obs request handler
     * @param netCDFDataset dataset for which the get observation request is being made
     * @param stationName collection of offerings from the request
     * @param variableNames collection of observed properties from the request
     * @param eventTime event time range from the request
     * @param outputFormat response format from the request
     * @param latLonRequest map of the latitudes and longitude (points or ranges) from the request
     * @throws IOException 
     */
    public SOSGetObservationRequestHandler(NetcdfDataset netCDFDataset,
            String[] stationName,
            String[] variableNames,
            String[] eventTime,
            String outputFormat,
            Map<String, String> latLonRequest) throws IOException {
        super(netCDFDataset);
        //this.stationName = stationName[0];        
        CoordinateAxis heightAxis = netCDFDataset.findCoordinateAxis(AxisType.Height);

        this.variableNames = checkNetcdfFileForAxis(heightAxis, variableNames);
        
        //grid operation
        if (getDatasetFeatureType() == FeatureType.GRID) {
            Variable depthAxis;
            if (!latLonRequest.isEmpty()) {
                depthAxis = (netCDFDataset.findVariable("depth"));
                if (depthAxis != null) {
                    this.variableNames = checkNetcdfFileForAxis((CoordinateAxis1D) depthAxis, this.variableNames);
                }
                this.variableNames = checkNetcdfFileForAxis(netCDFDataset.findCoordinateAxis(AxisType.Lat), this.variableNames);
                this.variableNames = checkNetcdfFileForAxis(netCDFDataset.findCoordinateAxis(AxisType.Lon), this.variableNames);

                CDMDataSet = new Grid(stationName, eventTime, this.variableNames, latLonRequest);
                CDMDataSet.setData(getGridDataset());
            } 
        }
        //if the stations are not of cdm type grid then check to see and set cdm data type        
        else {

            if (getDatasetFeatureType() == FeatureType.TRAJECTORY) {
                CDMDataSet = new Trajectory(stationName, eventTime, this.variableNames);
            } else if (getDatasetFeatureType() == FeatureType.STATION) {
                CDMDataSet = new TimeSeries(stationName, eventTime, this.variableNames);
            } else if (getDatasetFeatureType() == FeatureType.STATION_PROFILE) {
                CDMDataSet = new TimeSeriesProfile(stationName, eventTime, this.variableNames);
            } else if (getDatasetFeatureType() == FeatureType.PROFILE) {
                CDMDataSet = new Profile(stationName, eventTime, this.variableNames);
            } else if (getDatasetFeatureType() == FeatureType.SECTION) {
                CDMDataSet = new Section(stationName, eventTime, this.variableNames);
            }else {
                _log.error("Have a null CDMDataSet, this will cause a null reference exception! - SOSGetObservationRequestHandler.87");
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
        
        // set up our formatter
        if(outputFormat.equalsIgnoreCase("text/xml;subtype=\"om/1.0.0\"")) {
            contentType = "text/xml";
            output = new OosTethysSwe(this.variableNames, getFeatureDataset(), CDMDataSet, netCDFDataset);
        } else {
            _log.error("Uknown/Unhandled responseFormat: " + outputFormat);
            output = new GetCapsOutputter();
            output.setupExceptionOutput("Could not recognize output format");
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
            if (foundZ == false) {
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
//            System.out.println("Got string: " + dataString);
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
}
