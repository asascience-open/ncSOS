/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.getobs.SOSGetObservationRequestHandler;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.VocabDefinitions;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import ucar.nc2.Attribute;
import ucar.nc2.constants.FeatureType;

/**
 *
 * @author SCowan
 */
public class IoosSos10 extends BaseOutputFormatter {
    
    // private fields
    private String[] procedures;
    
    // private final fields
    private final SOSGetObservationRequestHandler parent;
    
    // private static fields
    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(IoosSos10.class);
    
    public static final String SENSOR_ID_DEF = "http://mmisw.org/ont/ioos/definition/sensorID";
    // private constant fields
    private static final String TEMPLATE = "templates/ioossos_1_0.xml";
    private static final String RESPONSE_FORMAT = "text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0\"";
    private static final String SWE2_SCHEMALOCATION = "http://www.opengis.net/swe/2.0 http://schemas.opengis.net/sweCommon/2.0/swe.xsd";
    private static final String ioosTemplateURL = "http://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0";
    private static final FeatureType[] supportedTypes = { FeatureType.POINT, FeatureType.STATION };
    private static final String BLOCK_SEPERATOR = "\n";
    private static final String TOKEN_SEPERATOR = ",";
    private static final String DECIMAL_SEPERATOR = ".";
    private static final String STATIC_STATIONS_DEF ="http://mmisw.org/ont/ioos/definition/stations";
    private static final String STATIC_STATION_DEF = "http://mmisw.org/ont/ioos/definition/station";
    private static final String OBS_COLLECTION_DEF = "http://mmisw.org/ont/ioos/definition/sensorObservationCollection";
    private static final String SENSOR_OBS_COLLECTION = "http://mmisw.org/ont/ioos/definition/sensorObservations";
    private static final String STATIC_SENSOR_DEF = "http://mmisw.org/ont/ioos/definition/sensor";
    private static final String STATIC_SENSORS_DEF = "http://mmisw.org/ont/ioos/definition/sensors";
    private boolean hasError;
    
    //============== Constructor ============================================//
    public IoosSos10(SOSGetObservationRequestHandler parent) {
        Boolean supported = false;
        for (FeatureType ft : supportedTypes) {
            if (parent.getDatasetFeatureType() == ft)
                supported = true;
        }
        
        if (!supported) {
            UnsupportedFeatureType(parent.getDatasetFeatureType());
            this.parent = null;
        } else {
            this.hasError = false;
            this.parent = parent;
        }
    }
    
    //============== Private Methods ========================================//
    
    private void UnsupportedFeatureType(FeatureType ft) {
        this.setupExceptionOutput("Unsupported feature type for the " + RESPONSE_FORMAT + " response format! FeatureType: " + ft.toString());
    }
    
    /**
     * Populates the response for a GO request with the response format
     * of text/xml;subtype="om/1.0.0/profiles/ioos_sos/1.0"
     */
    private void createIoosSosResponse() {
        try {
            // create the document from template
            loadTemplateXML(TEMPLATE);
            // set description
            Element description = (Element) this.document.getElementsByTagNameNS(GML_NS, DESCRIPTION).item(0);
            description.setTextContent(this.parent.getGlobalAttribute("description", "none"));
            // fill out metadata properties
            this.constructMetadataProperties();
            // fill out <om:samplingTime>
            this.document = XMLDomUtils.addNode(this.document, 
            									SAMPLING_TIME, 
            									OM_NS, 
            									this.createSamplingTimeTree());
            // fill out <om:procedure>
            this.document = XMLDomUtils.addNode(this.document, 
            									PROCEDURE,
            									OM_NS,
            									this.createProcedureTree());
            // fill out <om:observedProperty>
            this.document = XMLDomUtils.addNode(this.document,
            									OBSERVED_PROPERTY,
            									OM_NS,
            									this.createObservedPropertyTree());
            // fill out <om:featureOfInterest>
            this.document = XMLDomUtils.addNode(this.document,
            									FEATURE_INTEREST,
            									OM_NS, 
            									this.createFeatureOfInterestTree());
            // fill out <om:result>
            this.document = XMLDomUtils.addNode(this.document,
            									RESULT,
            									OM_NS, 
            									this.createResultTree());
        } catch (Exception ex) {
            _log.error(ex.toString());
            ex.printStackTrace();
            this.setupExceptionOutput("Unable to correctly create response for request.");
        }
    }

    private void constructMetadataProperties() {
        /*
         * Add an element for each global metadata property
         */
        // disclaimer
        List<SubElement> metaData = new ArrayList<SubElement>();
        HashMap<String,String> attrs = new HashMap<String, String>();
        SubElement subElm = new SubElement(GENERIC_META_DATA, GML_NS);
        String disclaimer = this.parent.getGlobalAttribute("disclaimer", null);
        if (disclaimer != null) {
            metaData.add(subElm);
            subElm = new SubElement(GML_NS, DESCRIPTION);
            subElm.textContent = disclaimer;
            metaData.add(subElm);
            attrs.put("xlink:title", "disclaimer");
            this.createMetadataProperty(attrs, metaData);
        }
        
        // ioosTemplateVersion
        metaData = new ArrayList<SubElement>();
        attrs = new HashMap<String, String>();
        subElm = new SubElement(VERSION, GML_NS);
        subElm.textContent = "1.0";
        metaData.add(subElm);
        attrs.put("xlink:title", "ioosTemplateVersion");
        attrs.put("xlink:href", ioosTemplateURL);
        this.createMetadataProperty(attrs, metaData);
    }
    
    private org.w3c.dom.Node createMetadataProperty(HashMap<String,String> attributes, List<SubElement> elements) {
        /*
         * <gml:metaDataProperty xlink:title='title'>
         *   'Add sub elements for each in list'
         * </gml:metaDataProperty>
         */
        Element parent = (Element) this.document.getElementsByTagNameNS(OM_NS, OBSERVATION_COLLECTION).item(0);
        parent = this.addNewNode(parent, META_DATA_PROP, GML_NS);
        for (Map.Entry<String,String> entry : attributes.entrySet()) {
            parent.setAttribute(entry.getKey(), entry.getValue());
        }
        
        for (SubElement elm : elements) {
            parent.appendChild(this.createSubElement(elm));
        }
        
        return parent;
    }
    
    //<editor-fold defaultstate="collapsed" desc="om:Observation children">
    
    private Node createSamplingTimeTree() {
        /*
         * <gml:TimePeriod>
         *   <gml:beginPosition>start_time</gml:beginPosition>
         *   <gml:endPosition>end_time</gml:endPosition>
         * </gml:TimePeriod>
         */
        Element parent = createElementNS(GML_NS, TIME_PERIOD);
        // add a begin and end position
        Element begin = createElementNS(GML_NS, BEGIN_POSITION);
        String startT, endT;
        if (this.parent.getRequestedEventTimes().size() > 0) {
            startT = this.parent.getRequestedEventTimes().get(0);
            endT = this.parent.getRequestedEventTimes().get(this.parent.getRequestedEventTimes().size()-1);
        } else {
            startT = this.parent.getCDMDataset().getBoundTimeBegin();
            endT = this.parent.getCDMDataset().getBoundTimeEnd();
        }
        begin.setTextContent(startT);
        parent.appendChild(begin);
        
        Element end = createElementNS(GML_NS, END_POSITION);
        end.setTextContent(endT);
        parent.appendChild(end);
        return parent;
    }
    
    private org.w3c.dom.Node createProcedureTree() {
        /*
         * <om:Process>
         *   <gml:member xlink:href="station-urn" />
         *   <gml:member xlink:href="station-urn" />
         * </om:Process>
         */
        Element parent = createElementNS(OM_NS,"Process");
        // add each station to the parent
        for (int i=0; i<this.parent.getProcedures().length; i++) {
            String stName = this.parent.getCDMDataset().getStationName(i);
            Element member = createElementNS(GML_NS, MEMBER);
            member.setAttribute("xlink:href", SOSBaseRequestHandler.getGMLName(stName));
            parent.appendChild(member);
        }
        return parent;
    }
    
    private org.w3c.dom.Node createObservedPropertyTree() {
        /*
         * <swe:CompositePhenomenon dimension="number_of_obs_props" gml:id="observedProperties">
         *   <gml:name>Response Observed Properties</gml:name>
         *   <swe:component xlink:href="cf/ioos parameter def" />
         *   <swe:component xlink:href="cf/ioos parameter def" />
         * </swe:CompositePhenomenon>
         */
        List<String> obsProps = new ArrayList<String>(this.parent.getRequestedObservedProperties());
        Element parent = createElementNS(SWE_NS, "CompositePhenomenon");
        parent.setAttribute("dimenion", obsProps.size() + "");
        parent.setAttributeNS(GML_NS, ID, "observedProperties");
        
        Element name = createElementNS(GML_NS, NAME);
        name.setTextContent("Response Observed Properties");
        parent.appendChild(name);
        // add a swe:component for each observed property
        for (String op : obsProps) {
            Element component = createElementNS(SWE_NS, COMPONENT);
            String stdName = this.parent.getVariableStandardName(op);
            component.setAttributeNS (XLINK_NS, HREF, VocabDefinitions.GetDefinitionForParameter(stdName));
            parent.appendChild(component);
        }
        return parent;
    }
    
    private org.w3c.dom.Node createFeatureOfInterestTree() {
        /*
         * <gml:FeatureCollection>
         *   <gml:metaDataProperty>
         *     <gml:name codeSpace="http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html#discrete-sampling-geometries">
         *       "feature-type"
         *     </gml:name>
         *   </gml:metaDataProperty>
         *   <gml:boundedBy>
         *     <gml:Envelope srsName="http://www.opengis.net/def/crs/EPSG/0/4326">
         *       <gml:lowerCorner>lon lat</gml:lowerCorner>
         *       <gml:upperCorner>lon lat</gml:upperCorner>
         *     </gml:Envelope>
         *   </gml:boundedBy>
         *   <gml:location>
         *     <gmlMultiPoint srsName="http://www.opengis.net/def/crs/EPSG/0/4326">
         *       <gml:pointMembers>
         *         <gml:Point>
         *           <gml:name>urn</gml:name>
         *           <gml:pos>lon lat</gml:pos>
         *         </gml:Point>
         *         <gml:Point>
         *           <gml:name>urn</gml:name>
         *           <gml:pos>lon lat</gml:pos>
         *         </gml:Point>
         *       </gml:pointMembers>
         *     </gml:MultiPoint>
         *   </gml:location
         * </gml:FeatureCollection>
         */
        Element parent = createElementNS(GML_NS, FEATURE_COLLECTION);
        // metadata property with feature type
        Element metadata = createElementNS(GML_NS, META_DATA_PROP);
        Element name = createElementNS(GML_NS, NAME);
        name.setAttribute(CODE_SPACE, "http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html#discrete-sampling-geometries");
        name.setTextContent(this.parent.getFeatureType());
        metadata.appendChild(name);
        parent.appendChild(metadata);
        
        // lat-lon bounding box
        Element bbox = createElementNS(GML_NS, BOUNDED_BY);
        Element env = createElementNS(GML_NS, ENVELOPE);
        env.setAttribute(SRS_NAME, "http://www.opengis.net/def/crs/EPSG/0/4326");
        Element corner = createElementNS(GML_NS, LOWER_CORNER);
        corner.setTextContent(this.parent.getBoundedLowerCorner());
        env.appendChild(corner);
        corner = createElementNS(GML_NS, UPPER_CORNER);
        corner.setTextContent(this.parent.getBoundedUpperCorner());
        env.appendChild(corner);
        bbox.appendChild(env);
        
        parent.appendChild(bbox);
        
        // location for each station
        Element loc = createElementNS(GML_NS, LOCATION);
        Element mPoint = createElementNS(GML_NS, "MultiPoint");
        mPoint.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4326");
        Element pMmbrs = createElementNS(GML_NS, "pointMembers");
        for (int i=0; i<this.parent.getProcedures().length; i++) {
            String stName = SOSBaseRequestHandler.getGMLName(this.parent.getCDMDataset().getStationName(i));
            Element point = createElementNS(GML_NS, POINT);
            Element pname = createElementNS(GML_NS, NAME);
            pname.setTextContent(stName);
            point.appendChild(pname);
            Element ppos = createElementNS(GML_NS, POS);
            ppos.setTextContent(this.parent.getStationLowerCorner(i));
            point.appendChild(ppos);
            
            pMmbrs.appendChild(point);
        }
        mPoint.appendChild(pMmbrs);
        loc.appendChild(mPoint);
        
        parent.appendChild(loc);
        
        return parent;
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="om:result">
    
    /**
     * Creates the xml element tree that will fit into the <om:member> element
     * in the ioossos_1_0 template.
     * @return Document root to be added into the template
     */
    private Node createResultTree() {
        /*
         * Creates the following:
         * <swe2:DataRecord xsi:schemaLocation=SWE2_SCHEMALOCATION>
         *   <swe2:field name="stations">
         *     <swe2:DataRecord>
         *       <!-- iteratively creates station data -->
         *     </swe2:DataRecord>
         *   </swe2:field>
         *   
         *   <swe2:field name="observationData">
         *     <swe2:DataArray>
         *       <!-- create dynamic data -->
         *       createElementCount()
         * 
         *       <swe2:values>data_block</swe2:values>
         *     </swe2:DataArray>
         *   </swe2:field>
         * <swe2:DataRecord>
         */
        // create DataRecord Element
        org.w3c.dom.Element parent = createSwe2Element(DATA_RECORD, "xsi:schemaLocation", SWE2_SCHEMALOCATION);
        // create 1st field, the static data field
        org.w3c.dom.Element static_data = createElementNS(SWE2_NS, FIELD);
        // set name to 'stations'
        static_data.setAttribute(NAME, "stations");
        // add static data to data record
        parent.appendChild(static_data);
        // create dataRecord for static data
        Element static_record = createSwe2Element(DATA_RECORD, DEFINITION, STATIC_STATIONS_DEF);
        // create the static data
        for (int i=0; i<this.parent.getProcedures().length; i++) {
            String stName = this.parent.getCDMDataset().getStationName(i);
            static_record.appendChild(createStaticStationData(stName, i));
        }
        // add record element to static data field element
        static_data.appendChild(static_record);
        // create 2nd field, dynamic data (observation data)
        org.w3c.dom.Element dynamic_data = createElementNS(SWE2_NS, FIELD);
        dynamic_data.setAttribute(NAME, "observationData");
        
        // create the dynamic data (obwervationData)
        Element dynamic_array = createSwe2Element(DATA_ARRAY);
        dynamic_array.setAttribute(DEFINITION,  OBS_COLLECTION_DEF);
        // get the count of records (total)
        StringBuilder strBuilder = new StringBuilder();
        for (int p=0;p<this.parent.getProcedures().length;p++) {
            strBuilder.append(this.parent.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, p));
        }
        int count = StringUtils.countOccurrencesOf(strBuilder.toString(), BLOCK_SEPERATOR);
        // create count element
        dynamic_array.appendChild(createElementCount(count));
        
        // create elementType "observations" element
        dynamic_array.appendChild(createObservationsElement());
        
        // create encoding element
        dynamic_array.appendChild(createEncodingElement());
        
        // add value block to values
        dynamic_array.appendChild(createValuesElement(strBuilder));
        
        dynamic_data.appendChild(dynamic_array);
        
        // add dynamic data to data record
        parent.appendChild(dynamic_data);
        return parent;
    }
    
    //<editor-fold defaultstate="collapsed" desc="dynamic data">
    
    private Element createObservationsElement() {
        /*
         * <swe2:elementType name="observations">
         *   <swe2:DataRecord>
         *     <swe2:field name="time">
         *       <swe2:Time definition="http://www.opengis.net/def/property/OGC/0/SamplingTime">
         *         <swe2:uom xlink:href="http://www.opengis.net/def/uom/ISO-8601/0/Gregorian" />
         *       </swe2:Time>
         *     </swe2:field>
         * 
         *     <swe2:field name="sensor">
         *       <swe2:DataChoice>
         *         createDataChoiceForSensor()
         *       <swe2:DataChoice>
         *     </swe2:field>
         *   </swe2:DataRecord>
         * </swe2:elementType>
         */
        Element elementType = createSwe2Element("elementType", NAME, "observations");
        Element dataRecord = createSwe2Element(DATA_RECORD);
        dataRecord.setAttribute(DEFINITION, SENSOR_OBS_COLLECTION );
        Element field = createSwe2Element(FIELD, NAME, "time");
        Element time = createSwe2Element(TIME, DEFINITION, "http://www.opengis.net/def/property/OGC/0/SamplingTime");
        time.appendChild(createSwe2Element(UOM, "xlink:href", "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian"));
        field.appendChild(time);
        dataRecord.appendChild(field);
        
        field = createSwe2Element(FIELD, NAME, "sensor");
        Element dataChoice = createSwe2Element("DataChoice");
        for (int i=0; i<this.parent.getProcedures().length; i++) {
            String stName = this.parent.getCDMDataset().getStationName(i);
            // DataRecord has to have at least 2 fields
            List<String> sensors = new ArrayList<String>(this.parent.getRequestedObservedProperties());
            while (sensors.size() < 2) {
                sensors.add("dummy_item");
            }
            for (String sensor : sensors) {
                dataChoice.appendChild(createDataChoiceForSensor(stName, sensor));
            }
        }
        field.appendChild(dataChoice);
        dataRecord.appendChild(field);
        
        elementType.appendChild(dataRecord);
        return elementType;
    }

    private Node createDataChoiceForSensor(String stName, String sensor) {
        /*
         * <swe2:item name="sensor_name">
         *   <swe2:DataRecord>
         *     <swe2:field name="sensor">
         *       <swe2:Quantity definition="cf/ioos definition">
         *         <swe2:uom code="units" />
         *       </swe2:Quantity>
         *     </swe2:field>
         *   </swe2:DataRecord>
         * </swe2:item>
         */
        // create the friendly name
        if (sensor.equalsIgnoreCase("dummy_item")) {
            Element item = createSwe2Element(ITEM, NAME, sensor);
            return item;
        } else {
            String fieldName = stationToFieldName(stName);
            String name = fieldName + "_" + sensor.toLowerCase();
            Element item = createSwe2Element(ITEM, NAME,name);

            String sensorDef = this.parent.getVariableStandardName(sensor);
            String sensorUnits = this.parent.getUnitsString(sensor);

            Element dataRecord = createSwe2Element(DATA_RECORD);
            Element field = createSwe2Element(FIELD, NAME, sensor);
            Element quantity = createSwe2Element(QUANTITY, DEFINITION,
            		VocabDefinitions.GetDefinitionForParameter(sensorDef));
            quantity.appendChild(createSwe2Element(UOM, CODE, sensorUnits));
            
            // if the variable has a 'fill value' then add it as a nil value
            try {
                for (Attribute attr : this.parent.getVariableByName(sensor).getAttributes()) {
                    if (attr.getShortName().toLowerCase().contains("fillvalue")) {
                        Element nilValues = createSwe2Element("nilValues");
                        Element nvs = createSwe2Element("nilValue", "reason", "Fill Value");
                        nvs.setTextContent(attr.getValue(0).toString());
                        nilValues.appendChild(nvs);
                        quantity.appendChild(nilValues);
                    }
                }
            } catch (Exception ex) { }
            
            field.appendChild(quantity);
            dataRecord.appendChild(field);

            item.appendChild(dataRecord);
            return item;
        }
    }
    
    private Element createValuesElement(StringBuilder strBuilder) {
        /*
         * Creates:
         * <swe2:valuse>data_blocks</swe2:values>
         */
        // need to operate to pull out the unwanted information from the data string
        // here is what it looks like from the observation handler:
        //time=1990-01-01T00:00:00Z,station=0,temperature=22.0,alt=5.6375227[BLOCK_SEPERATOR]time=1990-01-01T00:00:00Z,station=1,temperature=14.0,alt=7.396358
        // we need to remove all of the 'keys' (ie time, station, etc) and replace with desired info
        StringBuilder newString = new StringBuilder();
        List<String> obsProps = this.parent.getRequestedObservedProperties();
        for (String block : strBuilder.toString().split(BLOCK_SEPERATOR)) {
            // split on token seperator
            StringBuilder newBlock = new StringBuilder();
            for (String token : block.split(TOKEN_SEPERATOR)) {
                if (token.contains("time")) {
                    newBlock.append(token.replaceAll("time=", "")).append(TOKEN_SEPERATOR);
                } else if (token.contains("station")) {
                    String[] tokenSplit = token.split("=");
                    int stNum = Integer.parseInt(tokenSplit[1]);
                    newBlock.append(stationToFieldName(this.parent.getProcedures()[stNum])).append("_");
                } else {
                    String[] tokenSplit = token.split("=");
                    if (obsProps.contains(tokenSplit[0]) && tokenSplit.length > 1) {
                        // create a new block for each measurement
                        // add name of measurement to match the data choice
                        newString.append(newBlock.toString()).append(tokenSplit[0]).append(TOKEN_SEPERATOR);
                        newString.append(tokenSplit[1]);
                        newString.append(BLOCK_SEPERATOR);
                    }
                }
            }
        }
        // remove the last block seperator
        Element values = createSwe2Element(VALUES);
       // values.setTextContent(newString.substring(0, newString.length() - BLOCK_SEPERATOR.length()));
        values.setTextContent(newString.substring(0, newString.length() - BLOCK_SEPERATOR.length()));
        return values;
    }

    private Element createElementCount(int count) {
        /*
         * Creates the following:
         * <swe2:elementCount>
         *   <swe2:Count>
         *     <swe2:value>count</swe2:value>
         *   </swe2:Count>
         * </swe2:elementCount>
         */
        Element elmcount = createSwe2Element(ELEMENT_COUNT);
        Element ecount = createSwe2Element(COUNT);
        Element value = createSwe2Element(SML_VALUE);
        value.setTextContent(Integer.toString(count));
        ecount.appendChild(value);
        elmcount.appendChild(ecount);
        return elmcount;
    }

    private Node createEncodingElement() {
        /*
         * Creates the following:
         * <swe2:encoding>
         *   <swe2:TextEncoding decimalSeparator="DECIMAL_SEPERATOR" tokenSeperator="TOKEN_SEPERATOR" blockSeperator="BLOCK_SEPERATOR" />
         * </swe2:encoding>
         */
        Element encoding = createSwe2Element("encoding");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("decimalSeperator", DECIMAL_SEPERATOR);
        map.put("tokenSeperator", TOKEN_SEPERATOR);
        map.put("blockSeperator", BLOCK_SEPERATOR);
        encoding.appendChild(createSwe2Element("TextEncoding", map));
        return encoding;
    }
    
    //</editor-fold>
  
    //<editor-fold defaultstate="collapsed" desc="static data">
    
    private Element createStaticStationData(String stName, int stNum) {
        /*
         * Creates the following:
         * <swe2:field name="station_name">
         *   <swe2:DataRecord id="station_name">
         *     <swe2:field name="stationID">
         *       <swe2:Text definition="http://mmisw.org/ont/ioos/definition/stationID">
         *         <swe2:value>"procedure"</swe2:value>
         *       </swe2:Text>
         *     </swe2:field>
         *     <swe2:field name="platformLocation">
         *       createSwe2Vector()
         *     </swe2:field>
         *     <swe2:field name="sensors">
         *       <swe2:DataRecord>
         *         createSwe2Sensors()
         *       </swe2:DataRecord>
         *     </swe2:field>
         *   <swe2:DataRecord>
         * </swe2:field>
         */
        Element stStation = createElementNS(SWE2_NS, FIELD);
        // change 'procedure' into the readable format described in the template
        String name = stationToFieldName(stName);
        stStation.setAttribute(NAME, name);
        // DataRecord
        Element record = createSwe2Element(DATA_RECORD, ID, name);
        record.setAttribute(DEFINITION,  STATIC_STATION_DEF);
        // add field for stationId
        Element field = createSwe2Element(FIELD, NAME, "stationID");
        Element text = createSwe2Element("Text", DEFINITION, "http://mmisw.org/ont/ioos/definition/stationID");
        Element value = createSwe2Element(SML_VALUE);
        value.setTextContent(SOSBaseRequestHandler.getGMLName(stName));
        text.appendChild(value);
        field.appendChild(text);
        record.appendChild(field);
        
        // field for platformLocation
        field = createSwe2Element(FIELD, NAME, "platformLocation");
        field.appendChild(createSwe2Vector(stNum));
        record.appendChild(field);
        
        //fielf for sensors
        field = createSwe2Element(FIELD, NAME, "sensors");
        Element dr = createSwe2Element(DATA_RECORD);
        dr.setAttribute(DEFINITION, STATIC_SENSORS_DEF);
        // TODO: Need an example netcdf file that contain an explicit set of sensors in order
        // to properly create this field. It will not always be the case that observed
        // properties are also the sensors.
        // create a sensor field for each observed property
        for (String op : this.parent.getRequestedObservedProperties()) {
            dr.appendChild(createSwe2Sensors(stName, stNum, op, name));
        }
        field.appendChild(dr);
        record.appendChild(field);
        
        stStation.appendChild(record);
        return stStation;
    }
    
    private Element createSwe2Sensors(String stName, int stNum, String op, String stFormName) {
        /*
         * Creates the following:
         * <swe2:field name="station_name_sensor_name">
         *   <swe2:DataRecord id="station_name_sensor_name">
         *     <swe2:field name="sensorID">
         *       <swe2:Text definition="http://mmisw.org/ont/ioos/definition/sensorID">
         *         <swe2:value>"sensor_name"</swe2:value>
         *       </swe2:Text>
         *     </swe2:field>
         *     <swe2:field name="height">
         *       <swe2:Quantity definition="http://mmisw.org/ont/cf/parameter/height" referenceFrame="#PlatformFrame">
         *         <swe2:uom code="m" />
         *         <swe2:value>height</value>
         *       </swe2:Quantity>
         *     </swe2:field>
         *   </swe2:DataRecord>
         * </swe2:field>
         */
        String op_name = stFormName + "_" + op.toLowerCase();
        Element retval = createSwe2Element(FIELD, NAME, op_name);
        Element dataRecord = createSwe2Element(DATA_RECORD, ID, op_name);
        dataRecord.setAttribute(DEFINITION, STATIC_SENSOR_DEF);
        // sensorID field
        Element field = createSwe2Element(FIELD, NAME, "sensorID");
        Element text = createSwe2Element("Text", DEFINITION, SENSOR_ID_DEF);
        Element value = createSwe2Element(SML_VALUE);
        value.setTextContent(SOSBaseRequestHandler.getSensorGMLName(stName, op));
        text.appendChild(value);
        field.appendChild(text);
        dataRecord.appendChild(field);
        
        // height field
        field = createSwe2Element(FIELD, NAME, "height");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("definition", "http://mmisw.org/ont/cf/parameter/height");
        map.put("referenceFrame", "#PlatformFrame");
        Element quantity = createSwe2Element(QUANTITY, map);
        quantity.appendChild(createSwe2Element(UOM, "code", "m"));
        value = createSwe2Element(SML_VALUE);
        value.setTextContent("0");      // TODO: Need to change this to reflect height of ... something ...
        quantity.appendChild(value);
        field.appendChild(quantity);
        dataRecord.appendChild(field);
        
        retval.appendChild(dataRecord);
        return retval;
    }
    
    private Element createSwe2Vector(int stNum) {
        /*
         * Creates the following:
         * <swe2:Vector definition="http://www.opengis.get/def/property/OGC/0/PlatformLocation" localFrame="#PlatformFrame" referenceFrame="https://ioostech.googlecode.com/svn/trunk/IoosCRS/IoosBuoyCRS.xml">
         *   createSwe2Coordinate(latitude)
         *   createSwe2Coordinate(longitude)
         *   createSwe2Coordinate(height)
         * </swe2:Vector>
         */
        // vector
        HashMap<String,String> attributes = new HashMap<String, String>();
        attributes.put("definition","http://www.opengis.net/def/property/OGC/0/PlatformLocation");
        attributes.put("referenceFrame", "https://ioostech.googlecode.com/svn/trunk/IoosCRS/IoosBuoyCRS.xml");
        attributes.put("localFrame","#PlatformFrame");
        Element vector = createSwe2Element("Vector", attributes);
        // coords: lat, lon, z
        String lat = Double.toString(this.parent.getCDMDataset().getLowerLat(stNum));
        String lon = Double.toString(this.parent.getCDMDataset().getLowerLon(stNum));
        String alt = Double.toString(this.parent.getCDMDataset().getLowerAltitude(stNum));
        // create and add the swe2:coordinates
        vector.appendChild(createSwe2Coordinate("latitude", "http://mmisw.org/ont/cf/parameter/latitude", "Lat", "deg", lat));
        vector.appendChild(createSwe2Coordinate("longitude", "http://mmisw.org/ont/cf/parameter/longitude", "Lon", "deg", lon));
        vector.appendChild(createSwe2Coordinate("height", "http://mmisw.org/ont/cf/parameter/height", "Z", "m", alt));
        return vector;
    }
    
    private Element createSwe2Coordinate(String name, String definition, String axisId, String code, String value) {
        /*
         * Creates the following:
         * <swe2:coordinate name="name">
         *   <swe2:Quantity axisID="axisId", definition="definition">
         *     <swe2:uom code="code" />
         *     <swe2:value>value</value>
         *   </swe2:Quantity>
         * </swe2:coordinate>
         */
        Element coord = createSwe2Element(COORDINATE, NAME, name);
        HashMap<String,String> attrs = new HashMap<String, String>();
        attrs.put(DEFINITION, definition);
        attrs.put(AXIS_ID, axisId);
        Element quant = createSwe2Element(QUANTITY, attrs);
        // uom
        quant.appendChild(createSwe2Element(UOM, CODE, code));
        // value
        attrs.clear();
        Element val = createSwe2Element(SML_VALUE, attrs);
        val.setTextContent(value);
        quant.appendChild(val);
        // quantity -> coordinate
        coord.appendChild(quant);
        return coord;
    }
    
    //</editor-fold>
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="createSwe2Element">
    
    private Element createSwe2Element(String name) {
        HashMap<String, String> map = new HashMap<String, String>();
        return createSwe2Element(name, map);
    }
    
    private Element createSwe2Element(String name, String attributeName, String attributeValue) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(attributeName, attributeValue);
        return createSwe2Element(name, map);
    }
    
    private Element createSwe2Element(String name, HashMap<String,String> attributes) {
        // create a "swe2:DataRecord" element, add all of the attributes in the hashmap, return
        Element dataRecord = createElementNS(SWE2_NS, name);
        for (String attrName : attributes.keySet()) {
            dataRecord.setAttribute(attrName, attributes.get(attrName));
        }
        return dataRecord;
    }
    
    //</editor-fold>
    
    /**
     * Go from urn to readable name (as in swe2:field name)
     * @param stName
     * @return 
     */
    private String stationToFieldName(String stName) {
        // get the gml urn
        String urn = SOSBaseRequestHandler.getGMLName(stName);
        // split on station/sensor
        String[] urnSplit = urn.split("(sensor|station):");
        // get the last index of split
        urn = urnSplit[urnSplit.length - 1];
        // convert to underscore
        String underScorePattern = "[\\+\\-\\s:]+";
        urn = urn.replaceAll(underScorePattern, "_");
        return urn.toLowerCase();
    }

    //============== Output Formatter Interface =============================//
    @Override
    public void setupExceptionOutput(String message) {
        _log.debug(message);
        this.hasError = true;
        this.document = XMLDomUtils.getExceptionDom(message);
    }
    
    @Override
    public void writeOutput(Writer writer) {
        if (!hasError)
            this.createIoosSosResponse();
        super.writeOutput(writer);
    }
    //</editor-fold>
    //=======================================================================//
    
}
