package thredds.server.sos.getObs;

import com.asascience.ncSOS.outputFormatters.OosTethysSwe;
import com.asascience.ncSOS.outputFormatters.SOSOutputFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import thredds.server.sos.CDMClasses.*;
import thredds.server.sos.service.SOSBaseRequestHandler;
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

    public final static String TEMPLATE = "templates/sosGetObservation.xml";
//    private static final String OM_OBSERVATION = "om:Observation";
//    private String stationName;
    public static final String DEPTH = "depth";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    private String[] variableNames;
    private boolean isMultiTime;
    private iStationData CDMDataSet;
    private SOSOutputFormatter output;
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSGetObservationRequestHandler.class);

    /**
     * SOS get obs request handler
     * @param netCDFDataset
     * @param stationName
     * @param variableNames
     * @param eventTime
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
            } else {
                System.out.println("Have a null CDMDataSet, this will cause a null reference exception! - SOSGetObservationRequestHandler.87");
                // print exception and then return the doc
                CDMDataSet = null;
            }
            
            //only set the data is it is valid
            if (CDMDataSet!=null){
            CDMDataSet.setData(getFeatureTypeDataSet());
            }
        }
        
        // set up our formatter
        if(outputFormat.equalsIgnoreCase("oostethysswe")) {
            output = new OosTethysSwe(document, this.variableNames, getFeatureDataset(), CDMDataSet);
            ((OosTethysSwe)output).setMetaData(netCDFDataset.findAttValueIgnoreCase(null, "title", "Empty Title"),
                    netCDFDataset.findAttValueIgnoreCase(null, "history", "Empty History"),
                    netCDFDataset.findAttValueIgnoreCase(null, "institution", "Empty Insitution"),
                    netCDFDataset.findAttValueIgnoreCase(null, "source", "Empty Source"),
                    netCDFDataset.findAttValueIgnoreCase(null, "description", "Empty Description"),
                    netCDFDataset.getLocation(),
                    netCDFDataset.findAttValueIgnoreCase(null, "featureOfInterestBaseQueryURL", "empty FeatureOfInterestBase"));
        }
    }

    /**
     * checks for the presence of height in the netcdf dataset if it finds it but not in the variables selected it adds it
     * @param Axis
     * @param variableNames1
     * @return 
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
                for (int i = 0; i < variableNames1.length; i++) {
                    variableNamesNew.add(variableNames1[i]);
                }
                variableNamesNew.add(Axis.getFullName());
                variableNames1 = new String[variableNames1.length + 1];
                variableNames1 = (String[]) variableNamesNew.toArray(variableNames1);
                //*******************************
            }
        }
        return variableNames1;
    }

    @Override
    public String getTemplateLocation() {
        return TEMPLATE;
    }

    public boolean getIfMultiTime() {
        return isMultiTime;
    }
    
    public void setException(String exceptionMessage) {
        output.outputException(exceptionMessage);
    }

    /*
     * Create the observation data for getObs, passing it to our outputter
     */
    public void parseObservations() {
        for(int s = 0;s<CDMDataSet.getNumberOfStations();s++) {
            String dataString = CDMDataSet.getDataResponse(s);
            System.out.println("Got string: " + dataString);
            for (String dataPoint : dataString.split(";")) {
                if(!dataPoint.equals(""))
                    output.AddDataFormattedStringToInfoList(dataPoint);
            }
        }
                
        output.writeObservationsFromInfoList();
    }
    
    public iStationData getCDMDataset() {
        return CDMDataSet;
    }
}
