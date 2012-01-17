package thredds.server.sos.service;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.ArrayUtils;
import thredds.server.sos.util.XMLDomUtils;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;
import ucar.ma2.StructureMembers.Member;

import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureCollection;
import ucar.nc2.ft.PointFeatureCollectionIterator;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.ProfileFeature;
import ucar.nc2.ft.ProfileFeatureCollection;
import ucar.nc2.ft.StationProfileFeature;
import ucar.nc2.ft.StationProfileFeatureCollection;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.units.DateFormatter;
import ucar.nc2.units.DateRange;
import ucar.unidata.geoloc.Station;

/**
 * Get Observation Parser
 * @author abird
 */
public class SOSGetObservationRequestHandler extends SOSBaseRequestHandler {

    public final static String TEMPLATE = "templates/sosGetObservation.xml";
    private String stationName;
    private String[] variableNames;
    private String[] eventTime;
    private Station station;
    private StationTimeSeriesFeature stationTimeSeriesFeature;
    private StationProfileFeature stationProfileFeature;
    private ProfileFeatureCollection pfc;
    private ProfileFeature profileF;
    private int count;

    public SOSGetObservationRequestHandler(NetcdfDataset netCDFDataset, String stationName, String[] variableNames, String[] eventTime) throws IOException {
        super(netCDFDataset);
        this.stationName = stationName;
        this.variableNames = variableNames;
        this.eventTime = eventTime;

        //added logic block abird
        if (getFeatureCollection() != null) {
            station = getFeatureCollection().getStation(stationName);
            stationTimeSeriesFeature = getFeatureCollection().getStationFeature(station);
            stationTimeSeriesFeature.calcBounds();
        }

        //added abird
        //profile Collection
        //timeseriesprofile
        //get the station profile based on station name
        if (getFeatureProfileCollection() != null) {
            station = getFeatureProfileCollection().getStation(stationName);
            stationProfileFeature = getFeatureProfileCollection().getStationProfileFeature(station);

            CoordinateAxis heightAxis = netCDFDataset.findCoordinateAxis(AxisType.Height);
            this.variableNames = checkNetcdfFileForHeight(heightAxis, variableNames);
        }

        //added abird
        //profile
        //gets the profile based on event time
        if (getProfileFeatureCollection() != null) {
            pfc = getProfileFeatureCollection();

            CoordinateAxis heightAxis = netCDFDataset.findCoordinateAxis(AxisType.Height);
            this.variableNames = checkNetcdfFileForHeight(heightAxis, variableNames);

            //set the correct requested profile
            while (pfc.hasNext()) {
                ProfileFeature pFeature = pfc.next();

                if (pFeature.getName().equals(eventTime[0])) {
                    profileF = pFeature;
                }
            }
        }

    }

    /**
     * checks for the presence of height in the netcdf dataset if it finds it but not in the variables selected it adds it
     * @param heightAxis
     * @param variableNames1
     * @return 
     */
    private String[] checkNetcdfFileForHeight(CoordinateAxis heightAxis, String[] variableNames1) {
        if (heightAxis != null) {
            List<String> variableNamesNew = new ArrayList<String>();
            //check to see if Z present
            boolean foundZ = false;
            for (int i = 0; i < variableNames1.length; i++) {
                String zAvail = variableNames1[i];

                if (zAvail.equalsIgnoreCase(heightAxis.getName())) {
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
                variableNamesNew.add(heightAxis.getName());
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

    private void addDatasetResults(String[] obsProperty) {
        //TODO Test the following
        //add Data Block Definition
        document = XMLDomUtils.addNode(document, "om:result", "swe:DataArray");
        //element count
        document = XMLDomUtils.addNode(document, "swe:DataArray", "swe:elementCount");
        document = XMLDomUtils.addNode(document, "swe:elementCount", "swe:Count");
        document = XMLDomUtils.addNodeAndValue(document, "swe:Count", "swe:value", "COUNT!!!!");
        //element Type
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataArray", "swe:elementType", "name", "SimpleDataArray");
        //add data record
        document = XMLDomUtils.addNode(document, "swe:elementType", "swe:DataRecord");

        createDataFields(obsProperty);

        //add encoding value
        document = XMLDomUtils.addNode(document, "swe:DataArray", "swe:encoding");
        // text block
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:encoding", "swe:TextBlock", "blockSeparator", " ");
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "decimalSeparator", ".");
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "tokenSeparator", ",");

        try {
            //set the data
            document = XMLDomUtils.addNodeAndValue(document, "swe:DataArray", "swe:values", createObsValuesString());
        } catch (Exception ex) {
            document = XMLDomUtils.addNodeAndValue(document, "swe:DataArray", "swe:values", "ERROR!");
            Logger.getLogger(SOSGetObservationRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void createDataFields(String[] observedProperties) {
        int fieldIndex = 0;
        int quantityIndex = 0;

//        //add fields
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataRecord", "swe:field", "name", "time");
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:field", "swe:Time", fieldIndex++, "definition", "urn:ogc:phenomenon:time:iso8601");

        // add observed property
        for (String observedProperty : observedProperties) {

            VariableSimpleIF variable = getFeatureDataset().getDataVariable(observedProperty);
            document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataRecord", "swe:field", "name", observedProperty);
            document = XMLDomUtils.addNodeAndAttribute(document, "swe:field", "swe:Quantity", fieldIndex++, "definition", "urn:ogc:def:phenomenon:mmisw.org:cf:" + observedProperty);

            //added logic abird
            if (variable != null) {
                document = XMLDomUtils.addNodeAndAttribute(document, "swe:Quantity", "swe:uom", quantityIndex++, "code", variable.getUnitsString());
            }
        }


    }

    private String createStationProfileFeature() throws IOException {
        StringBuilder builder = new StringBuilder();
        DateFormatter dateFormatter = new DateFormatter();
        List<String> valueList = new ArrayList<String>();
        
        
        List<Date> z = stationProfileFeature.getTimes();
        Joiner tokenJoiner = Joiner.on(',');
        DateFormatter df = new DateFormatter();
        ProfileFeature pf = null;
        
        count = 0;
        
        //if not event time is specified get all the data
        if(eventTime ==null){
            //test getting items by date(index(0))
            for (int i = 0; i < z.size(); i++) {
              pf = stationProfileFeature.getProfileByDate(z.get(i));  
              createStationProfileData(pf, valueList, dateFormatter, builder, tokenJoiner);
            }
            return builder.toString();
        }
        //if the event time is specified get the correct data        
        else{
            for (int i = 0; i < z.size(); i++) {      
                
                if (df.toDateTimeStringISO(z.get(i)).contentEquals(eventTime[0].toString())){
                    pf = stationProfileFeature.getProfileByDate(z.get(i));
                    createStationProfileData(pf, valueList, dateFormatter, builder, tokenJoiner);
                }   
                
            }
             return builder.toString();
        }
    }

    private void createStationProfileData(ProfileFeature pf, List<String> valueList, DateFormatter dateFormatter, StringBuilder builder, Joiner tokenJoiner) throws IOException {

        PointFeatureIterator it = pf.getPointFeatureIterator(-1);

        //int num = 0;

        while (it.hasNext()) {
            PointFeature pointFeature = it.next();
            valueList.clear();
            valueList.add(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()));        
             
            for (String variableName : variableNames) {
                valueList.add(pointFeature.getData().getScalarObject(variableName).toString());
            }
            builder.append(tokenJoiner.join(valueList));
            // TODO:  conditional inside loop...
            if (stationProfileFeature.size() > 1) {
                builder.append(" ");
                builder.append("\n");
            }
        }
        count  = count + stationProfileFeature.size();
        setCount(count);
    }

    private String createStationTimeSeriesFeature() throws IOException {
        
        DateFormatter df = new DateFormatter();
        
        PointFeatureIterator iterator = stationTimeSeriesFeature.getPointFeatureIterator(-1);
                        
        StringBuilder builder = new StringBuilder();
        DateFormatter dateFormatter = new DateFormatter();
        List<String> valueList = new ArrayList<String>();
        Joiner tokenJoiner = Joiner.on(',');
        
        count = 0;
        
        while (iterator.hasNext()) {
            PointFeature pointFeature = iterator.next();
            
            if (eventTime==null){
                createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, tokenJoiner);
                count = (stationTimeSeriesFeature.size());
            }else{
                if (eventTime[0].contentEquals(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()))){
                    createTimeSeriesData(valueList, dateFormatter, pointFeature, builder, tokenJoiner);                    
                }
            }   
        }
        setCount(count);
        return builder.toString();
    }

    private void createTimeSeriesData(List<String> valueList, DateFormatter dateFormatter, PointFeature pointFeature, StringBuilder builder, Joiner tokenJoiner) throws IOException {
       count ++;
       valueList.clear();
       valueList.add(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()));
       for (String variableName : variableNames) {
           valueList.add(pointFeature.getData().getScalarObject(variableName).toString());
       }
       builder.append(tokenJoiner.join(valueList));
       // TODO:  conditional inside loop...
       if (stationTimeSeriesFeature.size() > 1) {
           builder.append(" ");
           builder.append("\n");
       }
    }

    private String createProfileFeature() throws IOException {
        PointFeatureIterator iterator = profileF.getPointFeatureIterator(-1);
        StringBuilder builder = new StringBuilder();
        DateFormatter dateFormatter = new DateFormatter();
        List<String> valueList = new ArrayList<String>();
        Joiner tokenJoiner = Joiner.on(',');

        while (iterator.hasNext()) {
            PointFeature pointFeature = iterator.next();
            valueList.clear();
            valueList.add(dateFormatter.toDateTimeStringISO(pointFeature.getObservationTimeAsDate()));

            for (String variableName : variableNames) {
                valueList.add(pointFeature.getData().getScalarObject(variableName).toString());
            }
            builder.append(tokenJoiner.join(valueList));
            builder.append(" ");
            builder.append("\n");
        }
        setCount(profileF.size());
        return builder.toString();
    }

    private String getLatLonString() {
        //station
        //added logic abird
        if (station != null) {
            return (new StringBuilder()).append(formatDegree(station.getLatitude())).append(" ").append(formatDegree(station.getLongitude())).append(" ").append("0").toString();
        }
        //profile
        //added abird
        if (profileF != null) {
            return (new StringBuilder()).append(formatDegree(profileF.getLatLon().getLatitude())).append(" ").append(formatDegree(profileF.getLatLon().getLongitude())).append(" ").append("0").toString();
        }
        return null;
    }

    public String getSystemGMLID() {
        return XMLDomUtils.getObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id");
    }

    public void setSystemGMLID(String value) {
        XMLDomUtils.setObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id", value);
    }

    void setSystemGMLID() {
        XMLDomUtils.setObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id", getGMLID(stationName));
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
        String lonlatStr = getLatLonString();
        XMLDomUtils.setNodeValue(document, "gml:Envelope", "gml:lowerCorner", lonlatStr);
    }

    public String getCollectionLowerCornerEnvelope() {
        return XMLDomUtils.getNodeValue(document, "gml:Envelope", "gml:lowerCorner");
    }

    public void setCollectionUpperCornerEnvelope() {
        XMLDomUtils.setNodeValue(document, "gml:Envelope", "gml:upperCorner", getLatLonString());
    }

    public String getCollectionUpperCornerEnvelope() {
        return XMLDomUtils.getNodeValue(document, "gml:Envelope", "gml:upperCorner");
    }

    /*
     * Create the observation XML data for getObs
     */
    public void parseObservations() {

        setObsCollectionMetaData();

        //add observation
        document = XMLDomUtils.addObservationElement(document);
        //add description
        document = XMLDomUtils.addNodeAndValue(document, "om:Observation", "gml:description", getDescription());
        //add name
        document = XMLDomUtils.addNodeAndValue(document, "om:Observation", "gml:name", getDescription());
        //add bounded by
        document = XMLDomUtils.addNode(document, "om:Observation", "gml:boundedBy");
        //add envelope and attribute
        document = XMLDomUtils.addNodeToNodeAndAttribute(document, "om:Observation", "gml:boundedBy", "gml:Envelope", "srsName", getGMLName(stationName));
        String upper = getLatLonString();
        String lower = getLatLonString();
        document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:Envelope", "gml:lowerCorner", lower);
        //add Upper GPS coors
        document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:Envelope", "gml:upperCorner", upper);
        //add sampling time
        document = XMLDomUtils.addNode(document, "om:Observation", "om:samplingTime");
        //add time instant
        document = XMLDomUtils.addNodeToNodeAndAttribute(document, "om:Observation", "om:samplingTime", "gml:TimePeriod", "gml:id", "DATA_TIME");
        //add time positions (being and end)

        //added logic abird
        //station timeseries
        if (stationTimeSeriesFeature != null) {
            document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:beginPosition", stationTimeSeriesFeature.getDateRange().getStart().toDateTimeStringISO());
            document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:endPosition", stationTimeSeriesFeature.getDateRange().getEnd().toDateTimeStringISO());
        }

        //timeseries profile
        if (stationProfileFeature != null) {
            try {
                List<Date> times = stationProfileFeature.getTimes();
                DateFormatter timePeriodFormatter = new DateFormatter();
                document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:beginPosition", timePeriodFormatter.toDateTimeStringISO(times.get(0)));
                document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:endPosition", timePeriodFormatter.toDateTimeStringISO(times.get(times.size() - 1)));               
            } catch (IOException ex) {
                Logger.getLogger(SOSGetObservationRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //profile
        if (profileF != null) {
            DateFormatter timePeriodFormatter = new DateFormatter();
            document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:beginPosition", timePeriodFormatter.toDateTimeStringISO(profileF.getTime()));
            document = XMLDomUtils.addNodeToNodeAndValue(document, "gml:TimePeriod", "gml:endPosition", timePeriodFormatter.toDateTimeStringISO(profileF.getTime()));
        }

        //add procedure
        document = XMLDomUtils.addNodeAndAttribute(document, "om:Observation", "om:procedure", "xlink:href", getLocation());

        //add observedProperties
        for (int i = 0; i < variableNames.length; i++) {
            String variableName = variableNames[i];
            document = XMLDomUtils.addNodeAndAttribute(document, "om:Observation", "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + variableName);
        }

//        if (isDepthAvailable == true) {
//            doc = XMLDomUtils.addNodeAndAttribute(doc, "om:Observation", "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + "depth");
//        }

        //add feature of interest
        document = XMLDomUtils.addNodeAndAttribute(document, "om:Observation", "om:featureOfInterest", "xlink:href", getFeatureOfInterest(stationName));
        //add results Node
        document = XMLDomUtils.addNode(document, "om:Observation", "om:result");

        addDatasetResults(variableNames);

    }

    public String getObservationDescription() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:description");
    }

    public String getObservationName() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:name");
    }

    public String getObservationEnvelope() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:boundedBy");
    }

    public String getObservationEnvelopeSrsName() {
        return XMLDomUtils.getAttributeFromNode(document, "om:Observation", "gml:Envelope", "srsName");
    }

    String getObservationLowerCorner() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:lowerCorner");
    }

    String getObservationUpperCorner() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:upperCorner");
    }

    String getObservationTimeInstant() {
        return XMLDomUtils.getAttributeFromNode(document, "om:Observation", "gml:TimePeriod", "gml:id");
    }

    String getObservationTimePosition() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "gml:beginPosition");
    }

    String getObservationProcedure() {
        return XMLDomUtils.getAttributeFromNode(document, "om:Observation", "om:procedure", "xlink:href");
    }

    String getObservationObservedProperty() {
        return XMLDomUtils.getAttributeFromNode(document, "om:Observation", "om:observedProperty", "xlink:href");
    }

    public String createObsValuesString() throws Exception {

        if (station != null) {
            String latVal = formatDegree(station.getLatitude());
            String lonVal = formatDegree(station.getLongitude());
        }
        if (profileF != null) {
            String latVal = formatDegree(profileF.getLatLon().getLatitude());
            String lonVal = formatDegree(profileF.getLatLon().getLongitude());
        }


        //****************************************
        // TODO: eventTime filtering...
        //Times series feature 
        //added logic abird
        if (stationTimeSeriesFeature != null) {
            return createStationTimeSeriesFeature();
        }


        //****************************************
        //station profile time series
        //added abird
        if (stationProfileFeature != null) {
            return createStationProfileFeature();
        }


        //****************************************
        //Profile
        //added abird
        if (profileF != null) {
            return createProfileFeature();
        }


        //all else fails
        return null;
    }

    private void setObsCollectionMetaData() {
        setSystemGMLID();
        setCollectionDescription();
        setCollectionName();
        setCollectionSourceName();
        //added abird
        setCollectionLowerCornerEnvelope();
        setCollectionUpperCornerEnvelope();



    }

    public String getResultValues() {
        return XMLDomUtils.getNodeValue(document, "om:Observation", "swe:values");
    }

    private void setBeginTime(String time) {
        XMLDomUtils.setNodeValue(document, "om:Observation", "gml:beginPosition", time);
    }

    private void setEndTime(String time) {
        XMLDomUtils.setNodeValue(document, "om:Observation", "gml:EndPosition", time);
    }

    private void setCount(int count) {
        XMLDomUtils.setNodeValue(document, "om:Observation", "swe:value", Integer.toString(count));
    }
}
