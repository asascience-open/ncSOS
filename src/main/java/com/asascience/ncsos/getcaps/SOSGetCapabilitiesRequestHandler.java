package com.asascience.ncsos.getcaps;

import com.asascience.ncsos.outputformatter.GetCapsOutputter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.geotoolkit.util.collection.CheckedHashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ucar.nc2.VariableSimpleIF;
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
    
    private static final String OWS = "http://www.opengis.net/ows/1.1";
    
    private static CalendarDate setStartDate;
    private static CalendarDate setEndDate;
    private static HashMap<Integer, CalendarDateRange> stationDateRange;
    private static HashMap<Integer, LatLonRect> stationBBox;

    /**
     * Creates an instance of SOSGetCapabilitiesRequestHandler to handle the dataset
     * and uri from the thredds request.
     * @param netCDFDataset dataset for which the Get Capabilities request is being
     * directed to
     * @param threddsURI uri from the thredds Get Capabilities request
     * @throws IOException
     */
    public SOSGetCapabilitiesRequestHandler(NetcdfDataset netCDFDataset, String threddsURI) throws IOException {
        super(netCDFDataset);
        this.threddsURI = threddsURI;
        output = new GetCapsOutputter();
        
        CalculateBoundsForFeatureSet();
    }
    
    public SOSGetCapabilitiesRequestHandler(NetcdfDataset emptyDataset) throws IOException {
        super(emptyDataset);
        this.threddsURI = "";
        output = new GetCapsOutputter();
    }
    
    private Document getDocument() {
        return ((GetCapsOutputter)output).getDocument();
    }
    private void setDocument(Document setter) {
        ((GetCapsOutputter)output).setDocument(setter);
    }

    /**
     * sets the service identification information 
     */
    public void parseServiceIdentification() {
        NodeList nodeLst = getDocument().getElementsByTagName("ows:ServiceIdentification");

        for (int s = 0; s < nodeLst.getLength(); s++) {

            Node fstNode = nodeLst.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                //looks at the one node
                Element fstElmnt = (Element) fstNode;
                //looks at title
                NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("ows:Title");
                Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                NodeList fstNm = fstNmElmnt.getChildNodes();
                fstNm.item(0).setNodeValue(getTitle());

                //looks at the adstract
                NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("ows:Abstract");
                Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
                NodeList lstNm = lstNmElmnt.getChildNodes();
                if (getHistory() == null) {
                    lstNm.item(0).setNodeValue("");
                } else {
                    lstNm.item(0).setNodeValue(getHistory());
                }
                
                fstElmnt.getElementsByTagName("ows:AccessConstraints").item(0).setNodeValue(Access);
            }
        }
    }

    private NodeList getXMLNode(Element fstElmnt, String xmlLocation) {
        NodeList tagNameNodeList = fstElmnt.getElementsByTagName(xmlLocation);
        Element fstNmElmnt1 = (Element) tagNameNodeList.item(0);
        NodeList fstNm1 = fstNmElmnt1.getChildNodes();
        return fstNm1;
    }

    /**
     * sets the service description, this is typically additional created user/site information
     */
    public void parseServiceDescription() {
        //get service provider node list
        NodeList serviceProviderNodeList = getDocument().getElementsByTagName("ows:ServiceProvider");
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;

        // set org info
        // url
        getXMLNode(fstElmnt, "ows:ProviderSite").item(0).setNodeValue(DataPage);
        // name
        getXMLNode(fstElmnt, "ows:ProviderName").item(0).setNodeValue(PrimaryOwnership);
    }

    /**
     * 
     */
    public void parseOperationsMetaData() {
        //get operations meta data
        NodeList operationsNodeList = getDocument().getElementsByTagName("ows:OperationsMetadata");
        //set get capabilities meta data
        for (int s = 0; s < operationsNodeList.getLength(); s++) {

            Node fstNode = operationsNodeList.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                //looks at the one node
                Element fstElmnt = (Element) fstNode;
                //looks at title
                NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("ows:Operation");

                for (int jj = 0; jj < fstNmElmntLst.getLength(); jj++) {

                    Element fstNmElmnt = (Element) fstNmElmntLst.item(jj);
                    //String c = fstNmElmnt.getAttribute("name");
                    //System.out.println("Name: "  + fstNmElmnt.getAttribute("name"));

                    if (fstNmElmnt.getAttribute("name").contentEquals("GetCapabilities")) {
                        setGetCapabilitiesOperationsMetaData(fstNmElmnt);
                    } else if (fstNmElmnt.getAttribute("name").contentEquals("GetObservation")) {
                        setGetCapabilitiesGetObservationMetadata(fstNmElmnt);
                    } else if (fstNmElmnt.getAttribute("name").contentEquals("DescribeSensor")) {
                        setGetCapabilitiesDescribeSensorMetadata(fstNmElmnt);
                    }
                }
            }
        }
    }
    
    private void setGetCapabilitiesGetObservationMetadata(Element firstNameElement) {
        // set request link
        setGetCapabilitiesOperationsMetaData(firstNameElement);
        // get our parameters that need to be filled
        Element eventtime = null, offering = null, observedproperty = null;
        NodeList nodes = firstNameElement.getElementsByTagName("ows:Parameter");
        for (int i=0; i<nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            if (elem.getAttribute("name").equalsIgnoreCase("offering")) {
                offering = elem;
                continue;
            }
            if (elem.getAttribute("name").equalsIgnoreCase("observedProperty")) {
                observedproperty = elem;
                continue;
            }
            if (elem.getAttribute("name").equalsIgnoreCase("eventTime")) {
                eventtime = elem;
                continue;
            }
        }
        // set eventtime
        if (eventtime != null && setEndDate != null && setStartDate != null) {
            Element allowedValues = getDocument().createElement("ows:AllowedValues");
            eventtime.appendChild(allowedValues);
            Element range = getDocument().createElement("ows:Range");
            allowedValues.appendChild(range);
            // min value
            Element min = getDocument().createElement("ows:MinimumValue");
            min.setTextContent(setStartDate.toString());
            allowedValues.appendChild(min);
            // max value
            Element max = getDocument().createElement("ows:MaximumValue");
            max.setTextContent(setEndDate.toString());
            allowedValues.appendChild(max);
        }
        // set observedProperty parameter
        if (observedproperty != null) {
            Element allowedValues = getDocument().createElement("ows:AllowedValues");
            observedproperty.appendChild(allowedValues);
            for (Iterator<VariableSimpleIF> it = getFeatureDataset().getDataVariables().iterator(); it.hasNext();) {
                VariableSimpleIF var = it.next();
                Element value = getDocument().createElement("ows:value");
                value.setTextContent(var.getShortName());
                allowedValues.appendChild(value);
            }
        }
        // set offering parameter - list of station names
        if (offering != null && getStationNames() != null) {
            Element aV = getDocument().createElement("ows:AllowedValues");
            offering.appendChild(aV);
            for (String stationName : getStationNames().values()) {
                Element value = getDocument().createElement("ows:Value");
                value.setTextContent(stationName);
                aV.appendChild(value);
            }
        }
    }
    
    private void setGetCapabilitiesDescribeSensorMetadata(Element firstNameElement) {
        // set request link
        setGetCapabilitiesOperationsMetaData(firstNameElement);
        // set procedure allowed values of each station and sensor for the dataset
        Element procedure = null;
        NodeList nodes = firstNameElement.getElementsByTagName("ows:Parameter");
        for (int i=0; i<nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            if (elem.getAttribute("name").equalsIgnoreCase("procedure")) {
                procedure = elem;
                break;
            }
        }
        if (procedure != null && getStationNames() != null) {
            // add allowed values node
            Element allowedValues = getDocument().createElement("ows:AllowedValues");
            procedure.appendChild(allowedValues);
            for (String stationName : getStationNames().values()) {
                Element elem = getDocument().createElement("ows:Value");
                elem.setTextContent(getGMLName(stationName));
                allowedValues.appendChild(elem);
                for (String senName : getSensorNames()) {
                    Element sElem = getDocument().createElement("ows:Value");
                    sElem.setTextContent(getSensorGMLName(stationName, senName));
                    allowedValues.appendChild(sElem);
                }
            }
        }
    }

    private void setGetCapabilitiesOperationsMetaData(Element fstNmElmnt) {
        //set get capabilities GET request link
        NodeList fstNm1 = fstNmElmnt.getElementsByTagName("ows:Get");
        Element fstNmElmnt1 = (Element) fstNm1.item(0);
        fstNmElmnt1.setAttribute("xlink:href", threddsURI);

        //set get capabilities Post request link
//        NodeList fstNm12 = fstNmElmnt.getElementsByTagName("ows:Post");
//        Element fstNmElmnt12 = (Element) fstNm12.item(0);
//        if (fstNmElmnt12 != null)
//            fstNmElmnt12.setAttribute("xlink:href", threddsURI);
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
                            feature.calcBounds();
                            if (start == null || start.isAfter(feature.getCalendarDateRange().getStart()))
                                start = feature.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd()))
                                end = feature.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                            stationBBox.put(stationIndex, feature.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside TRAJECTORY case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
                        }
                    }
                    break;
                case STATION:
                    try {
                        StationTimeSeriesFeatureCollection collection = (StationTimeSeriesFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while(collection.hasNext()) {
                            StationTimeSeriesFeature feature = collection.next();
                            feature.calcBounds();
                            if (start == null || start.isAfter(feature.getCalendarDateRange().getStart()))
                                start = feature.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd()))
                                end = feature.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                            stationBBox.put(stationIndex, feature.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside STATION case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
                        }
                    }
                    break;
                case PROFILE:
                    try {
                        ProfileFeatureCollection collection = (ProfileFeatureCollection) getFeatureTypeDataSet();
                        collection.resetIteration();
                        while(collection.hasNext()) {
                            ProfileFeature feature = collection.next();
                            feature.calcBounds();
                            if (start == null || start.isAfter(feature.getCalendarDateRange().getStart()))
                                start = feature.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(feature.getCalendarDateRange().getEnd()))
                                end = feature.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, feature.getCalendarDateRange());
                            stationBBox.put(stationIndex, feature.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside PROFILE case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
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
                            flattened.calcBounds();
                            if (start == null || start.isAfter(flattened.getCalendarDateRange().getStart()))
                                start = flattened.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(flattened.getCalendarDateRange().getEnd()))
                                end = flattened.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, flattened.getCalendarDateRange());
                            stationBBox.put(stationIndex, flattened.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside STATION_PROFILE case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
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
                            flattened.calcBounds();
                            if (start == null || start.isAfter(flattened.getCalendarDateRange().getStart()))
                                start = flattened.getCalendarDateRange().getStart();
                            if (end == null || end.isBefore(flattened.getCalendarDateRange().getEnd()))
                                end = flattened.getCalendarDateRange().getEnd();
                            stationDateRange.put(stationIndex, flattened.getCalendarDateRange());
                            stationBBox.put(stationIndex, flattened.getBoundingBox());
                            stationIndex++;
                        }
                    } catch (Exception ex) {
                        System.out.println("CalculateDateRangesForFeatureSet - Exception caught inside SECTION case: " + ex.toString());
                        for (StackTraceElement elm : ex.getStackTrace()) {
                            System.out.println("\t" + elm.toString());
                        }
                    }
                    break;
                default:
                    System.out.println("Unknown feature type - getDatasetFeatureType is ??");
                    output.setupExceptionOutput("Feature set is currently unsupported.");
                    return;
            }
            setStartDate = start;
            setEndDate = end;
        } else {
            System.out.println("Unknown feature type - getDatasetFeatureType is null");
        }
    }
    
    private void SetObservationOfferingList() {
        Element offeringList = (Element) getDocument().getElementsByTagName("ObservationOfferingList").item(0);
        if (getStationNames() != null) {
            // iterate through offerings (stations)
            for (String stationName : getStationNames().values()) {
                Element obsOffering = getDocument().createElement("ObservationOffering");
                obsOffering.setAttribute("gml:id", stationName);
                // gml:name
                Element gmlName = getDocument().createElement("gml:name");
                gmlName.setTextContent(getGMLName(stationName));
                obsOffering.appendChild(gmlName);
                // gml:srsName - default to EPSG:4326 for now
                Element srsName = getDocument().createElement("gml:srsName");
                srsName.setTextContent("EPSG:4326");
                obsOffering.appendChild(srsName);
                // bounds
                obsOffering.appendChild(getStationBounds(getStationIndex(stationName)));
                // add time envelope
                obsOffering.appendChild(getStationPeriod(getStationIndex(stationName)));
                // feature of interest -- station name?
                Element foi = getDocument().createElement("featureOfInterest");
                foi.setAttribute("xlink:href", stationName);
                // observed properties
                for (Iterator<VariableSimpleIF> it = getFeatureDataset().getDataVariables().iterator(); it.hasNext();) {
                    VariableSimpleIF var = it.next();
                    Element value = getDocument().createElement("observedProperty");
                    value.setAttribute("xlink:href", var.getShortName());
                    obsOffering.appendChild(value);
                }
                // procedures
                for (String str : getSensorNames()) {
                    Element proc = getDocument().createElement("procedure");
                    proc.setAttribute("xlink:href", getSensorGMLName(stationName, str));
                    obsOffering.appendChild(proc);
                }
                // response format
                Element rf = getDocument().createElement("responseFormat");
                rf.setTextContent("text/xml; subtype=\"om/1.0.0\"");
                obsOffering.appendChild(rf);
                // response model/mode -- blank for now?
                obsOffering.appendChild(getDocument().createElement("responseModel"));
                obsOffering.appendChild(getDocument().createElement("responseMode"));

                // add offering
                offeringList.appendChild(obsOffering);
            }
        }
    }
    
    private Element getStationBounds(int stationIndex) {
        Element retval = getDocument().createElement("gml:boundedBy");
        
        if (stationBBox.get(stationIndex) != null) {
            LatLonRect rect = stationBBox.get(stationIndex);
            Element envelope = getDocument().createElement("gml:Envelope");
            envelope.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4326");
            // lower corner
            Element lowercorner = getDocument().createElement("gml:lowerCorner");
            lowercorner.setTextContent(rect.getLowerLeftPoint().getLatitude() + " " + rect.getLowerLeftPoint().getLongitude());
            envelope.appendChild(lowercorner);
            // upper corner
            Element uppercorner = getDocument().createElement("gml:upperCorner");
            uppercorner.setTextContent(rect.getUpperRightPoint().getLatitude() + " " + rect.getUpperRightPoint().getLongitude());
            envelope.appendChild(uppercorner);
            retval.appendChild(envelope);
        }
        
        return retval;
    }
    
    private Element getStationPeriod(int stationIndex) {
        Element retval = getDocument().createElement("time");
        if (stationDateRange != null && stationDateRange.get(stationIndex) != null) {
            // time
            Element timePeriod = getDocument().createElement("gml:TimePeriod");
            timePeriod.setAttribute("xsi:type", "gml:TimePeriodType");
            // begin
            Element begin = getDocument().createElement("gml:beginPosition");
            begin.setTextContent(stationDateRange.get(stationIndex).getStart().toString());
            timePeriod.appendChild(begin);
            // end
            Element end = getDocument().createElement("gml:endPosition");
            end.setTextContent(stationDateRange.get(stationIndex).getEnd().toString());
            timePeriod.appendChild(end);
            retval.appendChild(timePeriod);
        }
        return retval;
    }
        
    /**
     * parses the observation list object and add the observations to the node
     * main location for parsing CDM get caps response 
     * @throws IOException 
     */
    public void parseObservationList() {
        SetObservationOfferingList();
    }
//    public void parseObservationList() throws IOException {
//        List<VariableSimpleIF> variableList = null;
//        List<String> observedPropertyList = null;
//        
//        // check for null feature type and return error if it is
//        if(getDatasetFeatureType() == null) {
//            setDocument(XMLDomUtils.getExceptionDom("Invalid or unknown feature type"));
//            return;
//        }
//
//        if (getDatasetFeatureType() != FeatureType.GRID) {
//            variableList = DiscreteSamplingGeometryUtil.getDataVariables(getFeatureDataset());
//            observedPropertyList = new ArrayList<String>(variableList.size());
//            List<String> observedPropertyUnitList = new ArrayList<String>(variableList.size());
//
//            for (VariableSimpleIF variable : variableList) {
//                observedPropertyList.add(variable.getShortName()); // TODO ? getName() instead?
//                observedPropertyUnitList.add(variable.getUnitsString());
//            }
//        }
//
//        //***************************************
//        
//        // use CDM to get, getCaps;
//        switch (getDatasetFeatureType()) {
//            case TRAJECTORY:
//                try {
//                    setDocument(Trajectory.getCapsResponse(getFeatureTypeDataSet(), getDocument(), getFeatureOfInterestBase(), getGMLNameBase(), observedPropertyList));
//                } catch (Exception e) {
//                }
//                break;
//            case STATION:
//                setDocument(TimeSeries.getCapsResponse((StationTimeSeriesFeatureCollection)getFeatureTypeDataSet(),getDocument(),getFeatureOfInterestBase(),getGMLNameBase(),observedPropertyList));
//                break;
//            case STATION_PROFILE:
//                setDocument(TimeSeriesProfile.getCapsResponse((StationProfileFeatureCollection)getFeatureTypeDataSet(),getDocument(),getFeatureOfInterestBase(),getGMLNameBase(),observedPropertyList));
//                break;
//            case PROFILE:
//                setDocument(Profile.getCapsResponse((ProfileFeatureCollection)getFeatureTypeDataSet(),getDocument(),getFeatureOfInterestBase(),getGMLNameBase(),observedPropertyList));
//                break;
//            case GRID:
//                setDocument(Grid.getCapsResponse(getGridDataset(), getDocument(), getGMLNameBase()));
//                break;
//            case SECTION:
//                setDocument(Section.getCapsResponse(getFeatureTypeDataSet(), getDocument(), getFeatureOfInterestBase(), getGMLNameBase(), observedPropertyList));
//                break;
//            default:
//                if (getDatasetFeatureType() != null) {
//                    output.setupExceptionOutput("Unsupported feature type for request of GetCapabilities: " + getDatasetFeatureType().name());
//                } else {
//                    output.setupExceptionOutput("Null feature type for request of GetCapabilities");
//                }
//                break;
//        }
//    }
}
