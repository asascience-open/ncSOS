package thredds.server.sos.getObs;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.w3c.dom.Element;
import thredds.server.sos.util.XMLDomUtils;
import org.w3c.dom.Document;
import thredds.server.sos.service.DatasetMetaData;

import thredds.server.sos.util.TimeUtils;
import ucar.ma2.Array;

/**
 * Get Observation Parser
 * @author abird
 */
public class MockGetObservationParser {

    private final DatasetMetaData dst;
    //private String templateFileLocation = getClass().getClassLoader().getResource("templates/sosGetObservation.xml").getPath();
     String templateLocation = "templates/sosGetObservation.xml";
    InputStream isTemplate = getClass().getClassLoader().getResourceAsStream(templateLocation);

    Document doc;
    private String routeElement;
    private long obsVarLength;
    private int[] obsEntry;
    private int obsArraySize;
    private boolean isDepthAvailable = false;
    private int requestStationNumber;

    public MockGetObservationParser() {
        this.dst = new DatasetMetaData();
        dst.setTitle("Title");
        dst.setHistory("History");
        dst.setInstitution("Institution");
        dst.setSource("Source");
        dst.setInstitution("ASA");
        dst.setLocation("Location");
    }

    public MockGetObservationParser(DatasetMetaData dst) {
        this.dst = dst;
    }

  public InputStream getTemplateStream(){
        return isTemplate;
    }

    public String getTemplateLocation(){
      return templateLocation;
    }

    public void parseTemplateXML() {
        doc = XMLDomUtils.getTemplateDom(isTemplate);
        setRouteElement(doc.getDocumentElement().getNodeName());
    }

    private void addDatasetResults(String[] obsProperty) {
        //TODO Test the following
        //add Data Block Definition
        doc = XMLDomUtils.addNode(doc, "om:result", "swe:DataArray");
        //element count
        doc = XMLDomUtils.addNode(doc, "swe:DataArray", "swe:elementCount");
        doc = XMLDomUtils.addNode(doc, "swe:elementCount", "swe:Count");
        doc = XMLDomUtils.addNodeAndValue(doc, "swe:Count", "swe:value", "COUNT!!!!");
        //element Type
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:DataArray", "swe:elementType", "name", "SimpleDataArray");
        //add data record
        doc = XMLDomUtils.addNode(doc, "swe:elementType", "swe:DataRecord");

        createDataFields(obsProperty);

        //add encoding value
        doc = XMLDomUtils.addNode(doc, "swe:DataArray", "swe:encoding");
        // text block
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:encoding", "swe:TextBlock", "blockSeparator", " ");
        XMLDomUtils.setAttributeFromNode(doc, "swe:encoding", "swe:TextBlock", "decimalSeparator", ".");
        XMLDomUtils.setAttributeFromNode(doc, "swe:encoding", "swe:TextBlock", "tokenSeparator", ",");

        try {
            //set the data
            doc = XMLDomUtils.addNodeAndValue(doc, "swe:DataArray", "swe:values", createObsValuesString());
            //while i am at it set the count value
            XMLDomUtils.setNodeValue(doc, "om:Observation", "swe:value", Long.toString(obsVarLength));

        } catch (Exception ex) {
            doc = XMLDomUtils.addNodeAndValue(doc, "swe:DataArray", "swe:values", "ERROR!");
            Logger.getLogger(MockGetObservationParser.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

    private void addDateLatLonToValues(StringBuilder strBuild, String dateTime, double latVal, double lonVal) {
        strBuild.append(dateTime);
        strBuild.append(",");
        strBuild.append(Double.toString(latVal));
        strBuild.append(",");
        strBuild.append(Double.toString(lonVal));
        strBuild.append(",");
    }

    /**
     * adds all the data including the depth values, different code due to the complicated for loops for data addition,
     * this is mostly EPA data
     * @return string
     */
    private String addDepthDataValues(double latVal, double lonVal, long timeVarLength) throws Exception {
        String valuesString;
        lonVal = dst.getLonVarData().getDouble(0);
        latVal = dst.getLatVarData().getDouble(0);
        latVal = dst.parseDoubleValue(latVal);
        lonVal = dst.parseDoubleValue(lonVal);
        long depthVarLength = dst.getObsDepth().getSize();

        StringBuilder strBuildDepth = new StringBuilder();


        for (int t = 0; t < timeVarLength; t++) {
            for (int z = 0; z < depthVarLength; z++) {
                String dateTime = createIsoTimeDate(t);
                addPlatformNameToValues(strBuildDepth);
                addDateLatLonToValues(strBuildDepth, dateTime, latVal, lonVal);


                //get depth array
                double depthVal = dst.getObsDepth().getDouble(z);
                strBuildDepth.append(Double.toString(depthVal));
                strBuildDepth.append(",");
                for (int j = 0; j < obsArraySize; j++) {
                    int index = (int) (z + (depthVarLength * t));

                    double obsValue = dst.getObsPropVarARRAYData().get(j).getDouble(index);
                    //double obsValue = dst.getObsPropVarData().getDouble(i);
                    strBuildDepth.append(Double.toString(obsValue));
                    if (j != obsArraySize - 1) {
                        strBuildDepth.append(",");
                    }
                }
                strBuildDepth.append(" ");
                strBuildDepth.append("\n");
            }
        }

        //strBuildDepth.append("DEPTH!!!!!");
        valuesString = strBuildDepth.toString();
        return valuesString;
    }

    /**
     * Normal use for data values ie without depth, this is mostly station data
     * @param latVal
     * @param lonVal
     * @param timeVarLength
     * @return
     * @throws Exception
     */
    private String addNormalDataValues(double latVal, double lonVal, long timeVarLength) throws Exception {
        String valuesString;
        //&& (timeVarLength == obsVarLength)){
        //Lon and lat has one entry and time and obs match
        latVal = dst.parseDoubleValue(dst.getLatVarData().getDouble(0));
        lonVal = dst.parseDoubleValue(dst.getLonVarData().getDouble(0));
        StringBuilder strBuildNormal = new StringBuilder();
        for (int i = 0; i < timeVarLength; i++) {
            String dateTime = createIsoTimeDate(i);
            addPlatformNameToValues(strBuildNormal);
            addDateLatLonToValues(strBuildNormal, dateTime, latVal, lonVal);
            for (int j = 0; j < obsArraySize; j++) {
                double obsValue = dst.getObsPropVarARRAYData().get(j).getDouble(i);
                //double obsValue = dst.getObsPropVarData().getDouble(i);
                strBuildNormal.append(Double.toString(obsValue));
                if (j != obsArraySize - 1) {
                    strBuildNormal.append(",");
                }
            }
            strBuildNormal.append(" ");
            strBuildNormal.append("\n");
        }
        valuesString = strBuildNormal.toString();
        return valuesString;
    }

    private void addPlatformNameToValues(StringBuilder strBuild) {
        if (dst.getTitle().length() > 10) {
            String stationName = dst.getStringStationNames()[requestStationNumber];
            strBuild.append(stationName);
            strBuild.append(",");
        } else {
            strBuild.append(dst.getTitle());
            strBuild.append(",");
        }
    }

    private void checkDepthIsAvailable() {
        for (int z = 0; z < dst.getObservedPropsList().size(); z++) {
            String obsProp1 = (String) dst.getObservedPropsList().get(z);
            if (obsProp1.equalsIgnoreCase("z")) {
                isDepthAvailable = true;
            }
        }
    }

    private void createDataFields(String[] obsProperty) {
        int propIndex = 4;
        //add fields
        //platform
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:DataRecord", "swe:field", "name", "PlatformName");
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:field", "swe:Quantity", 0, "definition", "urn:mmisw.org#platform");
        //time
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:DataRecord", "swe:field", "name", "time");
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:field", "swe:Time", 1, "definition", "urn:ogc:phenomenon:time:iso8601");
        //lat
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:DataRecord", "swe:field", "name", "lat");
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:field", "swe:Quantity", 2, "definition", "urn:ogc:phenomenon:latitude:wgs84");
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:Quantity", "swe:uom", 2 - 1, "code", "deg");
        //lon
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:DataRecord", "swe:field", "name", "lon");
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:field", "swe:Quantity", 3, "definition", "urn:ogc:phenomenon:latitude:wgs84");
        doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:Quantity", "swe:uom", 3 - 1, "code", "deg");
        //depth
        if (isDepthAvailable == true) {
            doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:DataRecord", "swe:field", "name", "depth");
            doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:field", "swe:Quantity", propIndex, "definition", "http://mmisw.org/cf#depth");
            doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:Quantity", "swe:uom", propIndex - 1, "code", "m");
            propIndex = 5;
        }

        // add observed property
        for (int i = 0; i < obsProperty.length; i++) {
            String obsVal = obsProperty[i];
            int obsE = obsEntry[i];
            doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:DataRecord", "swe:field", "name", obsVal);
            doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:field", "swe:Quantity", propIndex + i, "definition", "urn:ogc:def:phenomenon:mmisw.org:cf:" + obsVal);
            doc = XMLDomUtils.addNodeAndAttribute(doc, "swe:Quantity", "swe:uom", (propIndex + i) - 1, "code", (String) dst.getObservedPropsUnitList().get(obsE));
        }


    }

    private String createIsoTimeDate(int i) throws Exception {
        double timeDbl = dst.getTimeVarData().getDouble(i);
        String timeStr = Double.toString(timeDbl);
        String isodataTime = dst.parseDateString(timeStr);
        return isodataTime;
    }

    private String getLatLonString() {
        Array lats = dst.getStationLats();
        Array lons = dst.getStationLons();
        double lat = roundFourDecimals(lats.getDouble(requestStationNumber));
        double lon = roundFourDecimals(lons.getDouble(requestStationNumber));
        String lonlatStr = Double.toString(lat) + " " + Double.toString(lon) + " 0";
        return lonlatStr;
    }

    private String multiTimeEventReturn(int startIndex, int endIndex, double latVal, double lonVal, String valuesString) throws Exception {
        StringBuilder strBuildTime = new StringBuilder();
        for (int t = startIndex; t <= endIndex; t++) {
            String dateTime = createIsoTimeDate(t);
            addPlatformNameToValues(strBuildTime);
            addDateLatLonToValues(strBuildTime, dateTime, latVal, lonVal);
            for (int j = 0; j < obsArraySize; j++) {
                double obsValue = dst.getObsPropVarARRAYData().get(j).getDouble(t);
                strBuildTime.append(Double.toString(obsValue));
                if (j != obsArraySize - 1) {
                    strBuildTime.append(",");
                }
            }
            strBuildTime.append(" ");
            strBuildTime.append("\n");
        }
        /*
        setBeginTime(createIsoTimeDate(startIndex));
        setEndTime(createIsoTimeDate(endIndex));
        setCount(endIndex-startIndex);
         */
        valuesString = strBuildTime.toString();
        return valuesString;
    }

    private void setRouteElement(String routeElement) {
        this.routeElement = routeElement;
    }

    public Document getDom() {
        return doc;
    }

    public String getSystemGMLID() {
        String container = "om:ObservationCollection";
        String attribute = "gml:id";
        return XMLDomUtils.getObsGMLIDAttributeFromNode(doc, container, attribute);
    }

    public void setSystemGMLID(String value) {
        String container = "om:ObservationCollection";
        String attribute = "gml:id";
        XMLDomUtils.setObsGMLIDAttributeFromNode(doc, container, attribute, value);
    }

    void setSystemGMLID() {
        String container = "om:ObservationCollection";
        String attribute = "gml:id";

        long length = dst.getStringStationNames().length;

        if (length == 1) {
            String stationName = dst.getStringStationNames()[0];
            XMLDomUtils.setObsGMLIDAttributeFromNode(doc, container, attribute, dst.getStationPrefix() + (stationName));
        } else {
            XMLDomUtils.setObsGMLIDAttributeFromNode(doc, container, attribute, dst.getStationPrefix() + (dst.getRequestedStationName()));
        }
    }

    public void setCollectionDescription() {
        String container = "om:ObservationCollection";
        String node = "gml:description";
        XMLDomUtils.setNodeValue(doc, container, node, dst.getDescription());
    }

    public String getCollectionDescription() {
        String container = "om:ObservationCollection";
        String node = "gml:description";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    public void setCollectionName() {
        String container = "om:ObservationCollection";
        String node = "gml:name";
        XMLDomUtils.setNodeValue(doc, container, node, dst.getTitle());
    }

    public String getCollectionName() {
        String container = "om:ObservationCollection";
        String node = "gml:name";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    public void setCollectionSourceName() {
        String container = "gml:boundedBy";
        String node = "gml:Envelope";
        String attribute = "srsName";
        XMLDomUtils.setAttributeFromNode(doc, container, node, attribute, dst.getGMLName(dst.getStringStationNames()[requestStationNumber]));
    }

    public String getCollectionSourceName() {
        String container = "gml:boundedBy";
        String node = "gml:Envelope";
        String attribute = "srsName";
        return XMLDomUtils.getAttributeFromNode(doc, container, node, attribute);
    }

    public void setCollectionLowerCornerEnvelope() {
        String container = "gml:Envelope";
        String node = "gml:lowerCorner";
        String lonlatStr = getLatLonString();

        XMLDomUtils.setNodeValue(doc, container, node, lonlatStr);
    }

    public String getCollectionLowerCornerEnvelope() {
        String container = "gml:Envelope";
        String node = "gml:lowerCorner";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    @Deprecated
    public double roundThreeDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.###");
        return Double.valueOf(twoDForm.format(d));
    }

    public double roundFourDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.####");
        return Double.valueOf(twoDForm.format(d));
    }

    public void setCollectionUpperCornerEnvelope() {
        String container = "gml:Envelope";
        String node = "gml:upperCorner";
        String lonlatStr = getLatLonString();

        XMLDomUtils.setNodeValue(doc, container, node, lonlatStr);
    }

    public String getCollectionUpperCornerEnvelope() {
        String container = "gml:Envelope";
        String node = "gml:upperCorner";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    /**
     * Looks in the dataset observed properties to see if the request value is contained in the dataset
     * @param observedProperty
     * @return boolean T/F
     */
    public boolean parseObsListForRequestedProperty(String[] observedProperty) {
        List propList = dst.getObservedPropsList();
        boolean valid = false;
        obsEntry = new int[observedProperty.length + 1];

        for (int j = 0; j < observedProperty.length; j++) {
            String obsValue = observedProperty[j];

            for (int i = 0; i < propList.size(); i++) {
                String propStr = (String) propList.get(i);

                if (propStr.equalsIgnoreCase(obsValue)) {
                    obsEntry[j] = i;
                    valid = true;
                }
            }
        }
        return valid;
    }

    /*
     * Create the observation XML data for getObs
     */
    public void parseObservations(String[] obsProperty) {

        setObsCollectionMetaData();

        if (parseObsListForRequestedProperty(obsProperty) == true) {
            //add observation
            doc = XMLDomUtils.addObservationElement(doc);
            //add description
            doc = XMLDomUtils.addNodeAndValue(doc, "om:Observation", "gml:description", dst.getDescription());
            //add name
            doc = XMLDomUtils.addNodeAndValue(doc, "om:Observation", "gml:name", dst.getDescription());
            //add bounded by
            doc = XMLDomUtils.addNode(doc, "om:Observation", "gml:boundedBy");
            //add envelope and attribute
            doc = XMLDomUtils.addNodeToNodeAndAttribute(doc, "om:Observation", "gml:boundedBy", "gml:Envelope", "srsName", dst.getGMLName(dst.getStringStationNames()[requestStationNumber]));
            //add Lower GPS coors
            String upper = getLatLonString();
            String lower = getLatLonString();
            doc = XMLDomUtils.addNodeToNodeAndValue(doc, "gml:Envelope", "gml:lowerCorner", lower);
            //add Upper GPS coors
            doc = XMLDomUtils.addNodeToNodeAndValue(doc, "gml:Envelope", "gml:upperCorner", upper);
            //add sampling time
            doc = XMLDomUtils.addNode(doc, "om:Observation", "om:samplingTime");
            //add time instant
            doc = XMLDomUtils.addNodeToNodeAndAttribute(doc, "om:Observation", "om:samplingTime", "gml:TimePeriod", "gml:id", "DATA_TIME");
            //add time positions (being and end)
            doc = XMLDomUtils.addNodeToNodeAndValue(doc, "gml:TimePeriod", "gml:beginPosition", dst.getObservationOfferingList().get(requestStationNumber).getObservationTimeBegin());
            doc = XMLDomUtils.addNodeToNodeAndValue(doc, "gml:TimePeriod", "gml:endPosition", dst.getObservationOfferingList().get(requestStationNumber).getObservationTimeEnd());
            //add procedure
            doc = XMLDomUtils.addNodeAndAttribute(doc, "om:Observation", "om:procedure", "xlink:href", dst.getLocation());

            checkDepthIsAvailable();

            //add observedProperties
            for (int i = 0; i < obsProperty.length; i++) {
                String obsVal = obsProperty[i];
                doc = XMLDomUtils.addNodeAndAttribute(doc, "om:Observation", "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + obsVal);
            }

            if (isDepthAvailable == true) {
                doc = XMLDomUtils.addNodeAndAttribute(doc, "om:Observation", "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + "depth");
            }

            //add feature of interest
            doc = XMLDomUtils.addNodeAndAttribute(doc, "om:Observation", "om:featureOfInterest", "xlink:href", "NA");
            //add results Node
            doc = XMLDomUtils.addNode(doc, "om:Observation", "om:result");

            addDatasetResults(obsProperty);

        }
    }

    public boolean getIsDepth() {
        return isDepthAvailable;
    }

    public String getObservationDescription() {
        String container = "om:Observation";
        String node = "gml:description";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    public boolean isNodeAvailable(String node, String container) {
        Element value = XMLDomUtils.checkNodeExists(doc, container, node);
        if (value != null) {
            if (value.getNodeName().equalsIgnoreCase(node)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public String getObservationName() {
        String container = "om:Observation";
        String node = "gml:name";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    public String getObservationEnvelope() {
        String container = "om:Observation";
        String node = "gml:boundedBy";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    public String getObservationEnvelopeSrsName() {
        String container = "om:Observation";
        String node = "gml:Envelope";
        String attribute = "srsName";
        return XMLDomUtils.getAttributeFromNode(doc, container, node, attribute);
    }

    String getObservationLowerCorner() {
        String container = "om:Observation";
        String node = "gml:lowerCorner";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    String getObservationUpperCorner() {
        String container = "om:Observation";
        String node = "gml:upperCorner";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    String getObservationTimeInstant() {
        String container = "om:Observation";
        String node = "gml:TimePeriod";
        String attribute = "gml:id";
        return XMLDomUtils.getAttributeFromNode(doc, container, node, attribute);
    }

    String getObservationTimePosition() {
        String container = "om:Observation";
        String node = "gml:beginPosition";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    String getObservationProcedure() {
        String container = "om:Observation";
        String node = "om:procedure";
        String attribute = "xlink:href";
        return XMLDomUtils.getAttributeFromNode(doc, container, node, attribute);
    }

    String getObservationObservedProperty() {
        String container = "om:Observation";
        String node = "om:observedProperty";
        String attribute = "xlink:href";
        return XMLDomUtils.getAttributeFromNode(doc, container, node, attribute);
    }

    public String createObsValuesString() throws Exception {
        String valuesString = null;
        double lonVal = 0;
        double latVal = 0;

        //if the obs probs are valid
        if (dst.getObsPropVarData() != null) {
            //get the lengths and run a check
            long lonVarLength = dst.getLonVarData().getSize();
            long latVarLength = dst.getLatVarData().getSize();
            long timeVarLength = dst.getTimeVarData().getSize();
            obsVarLength = dst.getObsPropVarData().getSize();
            obsArraySize = dst.getObsPropVarARRAYData().size();

            if (isDepthAvailable == true) {

                valuesString = addDepthDataValues(latVal, lonVal, timeVarLength);
                return valuesString;

            } else if (isSearchTimeAvailable() == true) {
                //start  
                lonVal = dst.parseDoubleValue(dst.getLonVarData().getDouble(requestStationNumber));
                latVal = dst.parseDoubleValue(dst.getLatVarData().getDouble(requestStationNumber));

                if (isMultiTime() == false) {
                    for (int i = 0; i < timeVarLength; i++) {
                        String dateTime = createIsoTimeDate(i);
                        if (dateTime.equalsIgnoreCase(dst.getSearchTimes()[0])) {
                            return valuesString = singleTimeEventReturn(i, latVal, lonVal, valuesString);
                        }
                    }
                } else if (isMultiTime() == true) {
                        int startIndex = 0;
                        int endIndex = 0;

                        for (int i = 0; i < timeVarLength; i++) {
                            String dateTime = createIsoTimeDate(i);
                            if (dateTime.equalsIgnoreCase(dst.getSearchTimes()[0])) {
                                startIndex = i;
                            } else if (dateTime.equalsIgnoreCase(dst.getSearchTimes()[1])) {
                                endIndex = i;
                            }
                        }
                        if (startIndex != endIndex) {
                            return valuesString = multiTimeEventReturn(startIndex, endIndex, latVal, lonVal, valuesString);
                        }
                        else //search the data time indexs for closest request
                        {
                                List<DateTime> ldt = new ArrayList();

                                for (int i = 0; i < dst.getTimeVarData().getSize(); i++) {
                                    long j = dst.getTimeVarData().getLong(i);
                                    DateTime e = new DateTime(createIsoTimeDate(i));
                                    ldt.add(e);

                                }
                                DateTime st = new DateTime(dst.getSearchTimes()[0]);
                                DateTime en = new DateTime(dst.getSearchTimes()[1]);

                                int localst = TimeUtils.findNearestTimeIndex(ldt, st);
                                int localen = TimeUtils.findNearestTimeIndex(ldt, en);
                                return valuesString = multiTimeEventReturn(localst, localen, latVal, lonVal, valuesString);
                        }

                }
            } else {
                //if normal
                if ((lonVarLength == 1) && (latVarLength == 1)) { //&& (timeVarLength == obsVarLength)){
                    //Lon and lat has one entry and time and obs match
                    valuesString = addNormalDataValues(latVal, lonVal, timeVarLength);
                    return valuesString;
                }
            }
        }
        return "ERROR!";
    }

    private String singleTimeEventReturn(int i, double latVal, double lonVal, String valuesString) throws Exception {
        StringBuilder strBuildTime = new StringBuilder();
        addPlatformNameToValues(strBuildTime);
        addDateLatLonToValues(strBuildTime, createIsoTimeDate(i), latVal, lonVal);
        for (int j = 0; j < obsArraySize; j++) {
            double obsValue = dst.getObsPropVarARRAYData().get(j).getDouble(i);
            strBuildTime.append(Double.toString(obsValue));
            if (j != obsArraySize - 1) {
                strBuildTime.append(",");
            }
        }
        //strBuildTime.append(" ");
        //strBuildTime.append("\n");
        /*
        setBeginTime(createIsoTimeDate(i));
        setEndTime(createIsoTimeDate(i));
        setCount(1);
         */
        valuesString = strBuildTime.toString();
        return valuesString;
    }

    private void setObsCollectionMetaData() {
        setStationSearchID();
        setSystemGMLID();
        setCollectionDescription();
        setCollectionName();
        setCollectionSourceName();
        setCollectionLowerCornerEnvelope();
        setCollectionUpperCornerEnvelope();
    }

    public boolean isSearchTimeAvailable() {
        return dst.isSearchTimeAvailable();
    }

    public boolean isMultiTime() {
        return dst.isMultiTime();
    }

    public String getResultValues() {
        String container = "om:Observation";
        String node = "swe:values";
        return XMLDomUtils.getNodeValue(doc, container, node);
    }

    /**
     * allows the reset of the start date if a start date was entered
     * @param time, String
     */
    private void setBeginTime(String time) {
        String container = "om:Observation";
        String node = "gml:beginPositionEnd";
        XMLDomUtils.setNodeValue(doc, container, node, time);
    }

    /**
     * allows the reset of the end date if an end date was entered
     * @param time, String
     */
    private void setEndTime(String time) {
        String container = "om:Observation";
        String node = "gml:EndPosition";
        XMLDomUtils.setNodeValue(doc, container, node, time);
    }

    /**
     * allows the reset of the count param to match the new dates
     * @param time, String
     */
    private void setCount(int count) {
        String container = "om:Observation";
        String node = "swe:value";
        XMLDomUtils.setNodeValue(doc, container, node, Integer.toString(count));
    }

    private void setStationSearchID() {
        if(dst.getRequestedStationName()!=null){

        for (int i = 0; i < dst.getStringStationNames().length; i++) {
            String name = dst.getStringStationNames()[i];
            if (name.equalsIgnoreCase(dst.getRequestedStationName())) {
                requestStationNumber = i;
            }

        }
        }
        else{
            requestStationNumber=0;
        }
    }
}
