package thredds.server.sos.getObs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import thredds.server.sos.CDMClasses.Grid;
import thredds.server.sos.CDMClasses.Profile;
import thredds.server.sos.CDMClasses.TimeSeries;
import thredds.server.sos.CDMClasses.TimeSeriesProfile;
import thredds.server.sos.CDMClasses.Trajectory;
import thredds.server.sos.CDMClasses.iStationData;
import thredds.server.sos.service.SOSBaseRequestHandler;
//import thredds.server.sos.service.StationData;
import thredds.server.sos.util.XMLDomUtils;
import ucar.nc2.Variable;

import ucar.nc2.VariableSimpleIF;
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
    private static final String OM_OBSERVATION = "om:Observation";
    private String stationName;
    private String[] variableNames;
    private boolean isMultiTime;
    private iStationData CDMDataSet;

    /**
     * SOS get obs request handler
     * @param netCDFDataset
     * @param stationName
     * @param variableNames
     * @param eventTime
     * @throws IOException 
     */
    public SOSGetObservationRequestHandler(NetcdfDataset netCDFDataset, String[] stationName, String[] variableNames, String[] eventTime, Map<String, String> latLonRequest) throws IOException {
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
                return;
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
                CDMDataSet = null;
            }
            
            //only set the data is it is valid
            if (CDMDataSet!=null){
            CDMDataSet.setData(getFeatureTypeDataSet());
            }
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

    private void addDatasetResults(String[] obsProperty, int stationNumber) {
        //TODO Test the following
        //add Data Block Definition
        document = XMLDomUtils.addNodeAllOptions(document, "om:result", "swe:DataArray", stationNumber);
        //element count
        document = XMLDomUtils.addNodeAllOptions(document, "swe:DataArray", "swe:elementCount", stationNumber);
        document = XMLDomUtils.addNodeAllOptions(document, "swe:elementCount", "swe:Count", stationNumber);
        document = XMLDomUtils.addNodeAllOptions(document, "swe:Count", "swe:value", "COUNT!!!!", stationNumber);
        //element Type
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataArray", "swe:elementType", "name", "SimpleDataArray", stationNumber);
        //add data record
        document = XMLDomUtils.addNodeAllOptions(document, "swe:elementType", "swe:DataRecord", stationNumber);

        createDataFields(obsProperty, stationNumber);

        //add encoding value
        document = XMLDomUtils.addNodeAllOptions(document, "swe:DataArray", "swe:encoding", stationNumber);
        // text block
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:encoding", "swe:TextBlock", "blockSeparator", " ", stationNumber);
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "decimalSeparator", ".");
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "tokenSeparator", ",");

        try {
            //set the data
            document = XMLDomUtils.addNodeAllOptions(document, "swe:DataArray", "swe:values", createObsValuesString(stationNumber), stationNumber);
        } catch (Exception ex) {
            document = XMLDomUtils.addNodeAllOptions(document, "swe:DataArray", "swe:values", "ERROR!", stationNumber);
            Logger.getLogger(SOSGetObservationRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createDataFields(String[] observedProperties, int stationNumber) {
        int fieldIndex = 0;
        int quantityIndex = 0;

//        //add fields
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataRecord", "swe:field", "name", "time", stationNumber);
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:field", "swe:Time", fieldIndex++, "definition", "urn:ogc:phenomenon:time:iso8601", stationNumber);

        // add observed property
        for (String observedProperty : observedProperties) {

            VariableSimpleIF variable = getFeatureDataset().getDataVariable(observedProperty);
            document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataRecord", "swe:field", "name", observedProperty, stationNumber);
            document = XMLDomUtils.addNodeAndAttribute(document, "swe:field", "swe:Quantity", fieldIndex++, "definition", "urn:ogc:def:phenomenon:mmisw.org:cf:" + observedProperty, stationNumber);

            //added logic abird
            if (variable != null) {
                document = XMLDomUtils.addNodeAndAttribute(document, "swe:Quantity", "swe:uom", quantityIndex++, "code", variable.getUnitsString(), stationNumber);
            } else {
                quantityIndex++;
            }
        }
    }

    public String getSystemGMLID() {
        return XMLDomUtils.getObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id");
    }

    public void setSystemGMLID(String value) {
        XMLDomUtils.setObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id", value);
    }

    void setSystemGMLID() {

        StringBuilder b = new StringBuilder();
        if (CDMDataSet != null) {

            for (int i = 0; i < CDMDataSet.getNumberOfStations(); i++) {
                b.append(stationName);
                b.append(",");
            }
        }


        XMLDomUtils.setObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id", getGMLID("GML_ID_NAME"));
    }

    public void setCollectionDescription() {
        XMLDomUtils.setNodeValue(document, "om:ObservationCollection", "gml:description", getDescription());
    }

    public String getCollectionDescription() {
        return XMLDomUtils.getNodeValue(document, "om:ObservationCollection", "gml:description");
    }

    public void setCollectionName() {
        XMLDomUtils.setNodeValue(document, "om:ObservationCollection", "gml:name", getTitle());
    }

    public String getCollectionName() {
        return XMLDomUtils.getNodeValue(document, "om:ObservationCollection", "gml:name");
    }

    public void setCollectionSourceName() {
        XMLDomUtils.setAttributeFromNode(document, "gml:boundedBy", "gml:Envelope", "srsName", "EPSG:4326");
    }

    public String getCollectionSourceName() {
        return XMLDomUtils.getAttributeFromNode(document, "gml:boundedBy", "gml:Envelope", "srsName");
    }

    public void setCollectionLowerCornerEnvelope() {
        XMLDomUtils.setNodeValue(document, "gml:Envelope", "gml:lowerCorner", getBoundLowerLatLonStr());
    }

    public String getCollectionLowerCornerEnvelope() {
        return XMLDomUtils.getNodeValue(document, "gml:Envelope", "gml:lowerCorner");
    }

    public void setCollectionUpperCornerEnvelope() {
        XMLDomUtils.setNodeValue(document, "gml:Envelope", "gml:upperCorner", getBoundUpperLatLonStr());
    }

    public String getCollectionUpperCornerEnvelope() {
        return XMLDomUtils.getNodeValue(document, "gml:Envelope", "gml:upperCorner");
    }

    /**
     * sets the obs initial data
     */
    private void setObsCollectionMetaData() {
        setSystemGMLID();
        setCollectionDescription();
        setCollectionName();
        setCollectionSourceName();
        setCollectionLowerCornerEnvelope();
        setCollectionUpperCornerEnvelope();
    }

    /*
     * Create the observation XML data for getObs
     */
    public void parseObservations() {

        if (CDMDataSet == null) {
            setDocument(XMLDomUtils.getExceptionDom());
        } else {
            setObsCollectionMetaData();

            int numStations;
                numStations = CDMDataSet.getNumberOfStations();


            //add observation 
            //*********THIS IS FOR NUMBER OF STATIONS!
            for (int stNum = 0; stNum < numStations; stNum++) {
                document = XMLDomUtils.addObservationElement(document);
                //add description
                //if (CDMDataSet != null) {
                //document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:description", CDMDataSet.getDescription(stNum), stNum);
                //} else {
                document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:description", getDescription(), stNum);
                //}
                //add name
                document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:name", getDescription(), stNum);
                //add bounded by
                document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:boundedBy", stNum);
                //add envelope and attribute
                document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:boundedBy", "gml:Envelope", "srsName", getGMLName(CDMDataSet.getStationName(stNum)), stNum);

                //add lat lon string
                document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:Envelope", "gml:lowerCorner", getStationLowerLatLonStr(stNum), stNum);
                //add Upper GPS coors
                document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:Envelope", "gml:upperCorner", getStationUpperLatLonStr(stNum), stNum);
                //add sampling time
                document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "om:samplingTime", stNum);
                //add time instant
                document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "om:samplingTime", "gml:TimePeriod", "gml:id", "DATA_TIME", stNum);
                //add time positions (being and end)
                if (CDMDataSet != null) {
                    document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:beginPosition", CDMDataSet.getTimeBegin(stNum), stNum);
                    document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:endPosition", CDMDataSet.getTimeEnd(stNum), stNum);
                }
                //add procedure
                document = XMLDomUtils.addNodeAndAttribute(document, OM_OBSERVATION, "om:procedure", "xlink:href", getLocation(), stNum);

                //add observedProperties
                for (int i = 0; i < variableNames.length; i++) {
                    String variableName = variableNames[i];
                    document = XMLDomUtils.addNodeAndAttribute(document, OM_OBSERVATION, "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + variableName, stNum);
                }

                //if (isDepthAvailable == true) {
                //doc = XMLDomUtils.addNodeAndAttribute(doc, OM_OBSERVATION, "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + "depth");
                //}

                //add feature of interest
                if (CDMDataSet != null) {
                    document = XMLDomUtils.addNodeAndAttribute(document, OM_OBSERVATION, "om:featureOfInterest", "xlink:href", getFeatureOfInterest(CDMDataSet.getStationName(stNum)), stNum);
                }
                //add results Node
                document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "om:result", stNum);

                addDatasetResults(variableNames, stNum);

            }
        }
    }

    public String getObservationDescription() {
        return XMLDomUtils.getNodeValue(document, OM_OBSERVATION, "gml:description");
    }

    public String getObservationName() {
        return XMLDomUtils.getNodeValue(document, OM_OBSERVATION, "gml:name");
    }

    public String getObservationEnvelope() {
        return XMLDomUtils.getNodeValue(document, OM_OBSERVATION, "gml:boundedBy");
    }

    public String getObservationEnvelopeSrsName() {
        return XMLDomUtils.getAttributeFromNode(document, OM_OBSERVATION, "gml:Envelope", "srsName");
    }

    String getObservationLowerCorner() {
        return XMLDomUtils.getNodeValue(document, OM_OBSERVATION, "gml:lowerCorner");
    }

    String getObservationUpperCorner() {
        return XMLDomUtils.getNodeValue(document, OM_OBSERVATION, "gml:upperCorner");
    }

    String getObservationTimeInstant() {
        return XMLDomUtils.getAttributeFromNode(document, OM_OBSERVATION, "gml:TimePeriod", "gml:id");
    }

    String getObservationTimePosition() {
        return XMLDomUtils.getNodeValue(document, OM_OBSERVATION, "gml:beginPosition");
    }

    String getObservationProcedure() {
        return XMLDomUtils.getAttributeFromNode(document, OM_OBSERVATION, "om:procedure", "xlink:href");
    }

    String getObservationObservedProperty() {
        return XMLDomUtils.getAttributeFromNode(document, OM_OBSERVATION, "om:observedProperty", "xlink:href");
    }

    /**
     * This sets the values param in the output based on station number index  
     * @param stNum
     * @return data response using variables
     * @throws Exception 
     */
    public String createObsValuesString(int stNum) throws Exception {
        setCount(variableNames.length + 1, stNum);
        return CDMDataSet.getDataResponse(stNum);

    }

    @Deprecated
    public String getResultValues() {
        return XMLDomUtils.getNodeValue(document, OM_OBSERVATION, "swe:values");
    }

    @Deprecated
    private void setBeginTime(String time) {
        XMLDomUtils.setNodeValue(document, OM_OBSERVATION, "gml:beginPosition", time);
    }

    @Deprecated
    private void setEndTime(String time) {
        XMLDomUtils.setNodeValue(document, OM_OBSERVATION, "gml:EndPosition", time);
    }

    /**
     * set data count?
     * @param count
     * @param StNum 
     */
    private void setCount(int count, int StNum) {
        XMLDomUtils.setNodeValue(document, OM_OBSERVATION, "swe:value", Integer.toString(count), StNum);
    }

    /**
     * get the lower lat lon string 
     * @return 
     */
    private String getStationLowerLatLonStr(int stNum) {
        return (new StringBuilder()).append(formatDegree(CDMDataSet.getLowerLat(stNum))).append(" ").append(formatDegree(CDMDataSet.getLowerLon(stNum))).append(" ").append("0").toString();
    }

    /**
     * get the upper lat lon string 
     * @return 
     */
    private String getStationUpperLatLonStr(int stNum) {
        return (new StringBuilder()).append(formatDegree(CDMDataSet.getUpperLat(stNum))).append(" ").append(formatDegree(CDMDataSet.getUpperLon(stNum))).append(" ").append("0").toString();
    }

    /**
     * get the upper lat lon string all stations
     * @return 
     */
    private String getBoundUpperLatLonStr() { 
        return (new StringBuilder()).append(formatDegree(CDMDataSet.getBoundUpperLat())).append(" ").append(formatDegree(CDMDataSet.getBoundUpperLon())).append(" ").append("0").toString();
    }

    /**
     * get the lower lat lon string all stations
     * @return 
     */
    private String getBoundLowerLatLonStr() {
        return (new StringBuilder()).append(formatDegree(CDMDataSet.getBoundLowerLat())).append(" ").append(formatDegree(CDMDataSet.getBoundLowerLon())).append(" ").append("0").toString();

    }
}
