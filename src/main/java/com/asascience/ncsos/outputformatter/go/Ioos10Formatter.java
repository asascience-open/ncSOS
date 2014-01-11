package com.asascience.ncsos.outputformatter.go;

import com.asascience.ncsos.go.GetObservationRequestHandler;
import com.asascience.ncsos.outputformatter.BaseOutputFormatter;
import com.asascience.ncsos.util.VocabDefinitions;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.util.StringUtils;
import ucar.nc2.Attribute;
import ucar.nc2.constants.FeatureType;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


public class Ioos10Formatter extends BaseOutputFormatter {

    public static final String RESPONSE_OBSERVED_PROPERTIES = "Response Observed Properties";

    // private fields
    private String[] procedures;
    // private static fields
    private static org.apache.logging.log4j.Logger _log = org.apache.logging.log4j.LogManager.getLogger();
    public static final String SENSOR_ID_DEF = "http://mmisw.org/ont/ioos/definition/sensorID";
    // private constant fields
    private static final String TEMPLATE = "templates/GO_ioos10.xml";
    private static final String RESPONSE_FORMAT = "text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0\"";
    private static final FeatureType[] supportedTypes = {FeatureType.POINT, FeatureType.STATION};
    private static final String BLOCK_SEPERATOR = "\n";
    private static final String TOKEN_SEPERATOR = ",";
    private static final String DECIMAL_SEPERATOR = ".";
    private static final String STATIC_STATIONS_DEF = "http://mmisw.org/ont/ioos/swe_element_type/stations";
    private static final String STATIC_STATION_DEF = "http://mmisw.org/ont/ioos/swe_element_type/station";
    private static final String OBS_COLLECTION_DEF = "http://mmisw.org/ont/ioos/swe_element_type/sensorObservationCollection";
    private static final String SENSOR_OBS_COLLECTION = "http://mmisw.org/ont/ioos/swe_element_type/sensorObservations";
    private static final String STATIC_SENSOR_DEF = "http://mmisw.org/ont/ioos/swe_element_type/sensor";
    private static final String STATIC_SENSORS_DEF = "http://mmisw.org/ont/ioos/swe_element_type/sensors";
    private static final String MISSING_REASON = "http://www.opengis.net/def/nil/OGC/0/missing";
    private Namespace OM_NS, GML_NS, SWE2_NS, XLINK_NS, SWE_NS = null;
    private GetObservationRequestHandler handler = null;

    //============== Constructor ============================================//
    public Ioos10Formatter(GetObservationRequestHandler handler) {
        super();
        this.handler = handler;
        this.OM_NS    = this.getNamespace("om");
        this.GML_NS   = this.getNamespace("gml");
        this.XLINK_NS = this.getNamespace("xlink");
        this.SWE_NS   = this.getNamespace("swe");
        this.SWE2_NS   = this.getNamespace("swe2");

        Boolean supported = false;
        for (FeatureType ft : supportedTypes) {
            if (handler.getDatasetFeatureType() == ft) {
                supported = true;
            }
        }

        if (!supported) {
            this.hasError = true;
            this.setupException("Unsupported feature type for the " + RESPONSE_FORMAT + " response format! FeatureType: " + (handler.getDatasetFeatureType().toString()));
        } else {
            this.hasError = false;
            this.handler = handler;
        }
    }

    public String getTemplateLocation() {
        return TEMPLATE;
    }

    /**
     * Populates the response for a GO request with the response format
     * of text/xml;subtype="om/1.0.0/profiles/ioos_sos/1.0"
     */
    private void createIoosSosResponse() {
        try {
            // Set NcSOS version
            this.setVersionMetadata();

            // Get the om:Observation element
            Element obsElement = this.getRoot().getChild("member", this.OM_NS).getChild("Observation", this.OM_NS);
            // Description

            obsElement.addContent(new Element("description", this.GML_NS).setText((String)this.handler.getGlobalAttribute("description", "No description")));

            Element samplingTime = new Element("samplingTime", this.OM_NS);
            samplingTime.addContent(this.createTimePeriodTree());
            obsElement.addContent(samplingTime);

            Element procedure = new Element("procedure", this.OM_NS);
            procedure.addContent(this.createProcessTree());
            obsElement.addContent(procedure);

            Element observProp = new Element("observedProperty", this.OM_NS);
            observProp.addContent(this.createCompositePhenomTree());
            obsElement.addContent(observProp);

            Element foi = new Element("featureOfInterest", this.OM_NS);
            foi.addContent(this.createFeatureCollectionTree());
            obsElement.addContent(foi);

            Element res = new Element("result", this.OM_NS);
            res.addContent(this.createDataRecordTree());
            obsElement.addContent(res);

        } catch (Exception ex) {
            _log.error(ex.toString());
            this.setupException("Unable to correctly create response for request: " + ex.toString());
        }
    }

    private void setVersionMetadata() {
        /*
        <gml:metaDataProperty xlink:title="softwareVersion" xlink:href="https://github.com/asascience-open/ncSOS/releases">
            <gml:version>FILLME</gml:version>
        </gml:metaDataProperty>
         */
        List<Element> mdps = this.getRoot().getChildren("metaDataProperty", this.GML_NS);
        for (Element md : mdps) {
            if (md.getAttribute("title", this.XLINK_NS) != null && md.getAttributeValue("title", this.XLINK_NS).equalsIgnoreCase("softwareVersion")) {
                md.getChild("version", this.GML_NS).setText(NCSOS_VERSION);
            }
        }
    }

    private Element createTimePeriodTree() {
        /*
         * <gml:TimePeriod>
         *   <gml:beginPosition>start_time</gml:beginPosition>
         *   <gml:endPosition>end_time</gml:endPosition>
         * </gml:TimePeriod>
         */
        String startT, endT;
        if (this.handler.getRequestedEventTimes().size() > 0) {
            startT = this.handler.getRequestedEventTimes().get(0);
            endT = this.handler.getRequestedEventTimes().get(this.handler.getRequestedEventTimes().size() - 1);
        } else {
            startT = this.handler.getCDMDataset().getBoundTimeBegin();
            endT = this.handler.getCDMDataset().getBoundTimeEnd();
        }

        Element tp = new Element("TimePeriod", this.GML_NS);

        Element starting = new Element("beginPosition", this.GML_NS);
        starting.setText(startT);
        tp.addContent(starting);

        Element ending = new Element("endPosition", this.GML_NS);
        ending.setText(endT);
        tp.addContent(ending);
        return tp;
    }

    private Element createProcessTree() {
        /*
         * <om:Process>
         *   <gml:member xlink:href="station-urn" />
         *   <gml:member xlink:href="station-urn" />
         * </om:Process>
         */
        Element proc = new Element("Process", this.OM_NS);
        // Add each station to the parent
        for (String p : this.handler.getProcedures()) {
            // This used to query for the station names by index, but that seems silly.
            // String stName = this.handler.getCDMDataset().getStationName(i);
            // member.setAttribute("xlink:href", BaseRequestHandler.getGMLName(stName));
            proc.addContent(new Element("member", this.GML_NS).setAttribute("href", p, this.XLINK_NS));
        }
        return proc;
    }

    private Element createCompositePhenomTree() {
        /*
         * <swe:CompositePhenomenon dimension="number_of_obs_props" gml:id="observedProperties">
         *   <gml:name>Response Observed Properties</gml:name>
         *   <swe:component xlink:href="cf/ioos parameter def" />
         *   <swe:component xlink:href="cf/ioos parameter def" />
         * </swe:CompositePhenomenon>
         */
        List<String> obsProps = new ArrayList<String>(this.handler.getRequestedObservedProperties());
        Element comp = new Element("CompositePhenomenon", this.SWE_NS);
        comp.setAttribute("dimension", String.valueOf(obsProps.size()));
        comp.setAttribute("id", "observedProperties", this.GML_NS);

        Element name = new Element("name", this.GML_NS);
        name.setText(RESPONSE_OBSERVED_PROPERTIES);
        comp.addContent(name);

        // add a swe:component for each observed property
        for (String op : obsProps) {
            Element swe = new Element("component", this.SWE_NS);
            String stdName = this.handler.getVariableStandardName(op);
            swe.setAttribute("href", VocabDefinitions.GetDefinitionForParameter(stdName), this.XLINK_NS);
            comp.addContent(swe);
        }
        return comp;
    }

    private Element createFeatureCollectionTree() {
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
        Element fc = new Element("FeatureCollection", this.GML_NS);

        // metaDataProperty
        Element metadata = new Element("metaDataProperty", this.GML_NS);
        Element name = new Element("name", this.GML_NS);
        name.setAttribute("codeSpace", "http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html#discrete-sampling-geometries");
        name.setText((String)this.handler.getGlobalAttribute("featureType"));
        metadata.addContent(name);
        fc.addContent(metadata);

        // lat-lon bounding box
        Element bbox = new Element("boundedBy", this.GML_NS);
        Element env = new Element("Envelope", this.GML_NS);
        env.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4326");
        env.addContent(new Element("lowerCorner", this.GML_NS).setText(this.handler.getBoundedLowerCorner()));
        env.addContent(new Element("upperCorner", this.GML_NS).setText(this.handler.getBoundedUpperCorner()));
        bbox.addContent(env);
        fc.addContent(bbox);

        // location for each station
        Element loc = new Element("location", this.GML_NS);
        Element mPoint = new Element("MultiPoint", this.GML_NS);
        mPoint.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4326");
        Element ptMembers = new Element("pointMembers", this.GML_NS);
        for (int i = 0; i < this.handler.getProcedures().length; i++) {
            String stName = this.handler.getUrnName(this.handler.getCDMDataset().getStationName(i));
            Element point = new Element("Point", this.GML_NS);

            Element pname = new Element("name", this.GML_NS);
            pname.setText(stName);
            point.addContent(pname);

            Element ppos = new Element("pos", this.GML_NS);
            ppos.setText(this.handler.getStationLowerCorner(i));
            point.addContent(ppos);

            ptMembers.addContent(point);
        }
        mPoint.addContent(ptMembers);
        loc.addContent(mPoint);
        fc.addContent(loc);

        return fc;
    }

    /**
     * Creates the xml element tree that will fit into the <om:member> element
     * in the ioossos_1_0 template.
     * @return Document root to be added into the template
     */
    private Element createDataRecordTree() {
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
        Element dr = new Element("DataRecord", this.SWE2_NS);

        // STATION (STATIC) DATA
        Element static_data = new Element("field", this.SWE2_NS);
        static_data.setAttribute("name", "stations");

        Element static_record = new Element("DataRecord", this.SWE2_NS);
        static_record.setAttribute("definition", STATIC_STATIONS_DEF);

        // create the static data
        for (int i = 0; i < this.handler.getProcedures().length; i++) {
            String stName = this.handler.getCDMDataset().getStationName(i);
            static_record.addContent(createStaticStationData(stName, i));
        }
        static_data.addContent(static_record);
        dr.addContent(static_data);

        // OBSERVATION (DYNAMIC) DATA
        Element dynamic_data = new Element("field", this.SWE2_NS);
        dynamic_data.setAttribute("name", "observationData");

        Element dynamic_array = new Element("DataArray", this.SWE2_NS);
        dynamic_array.setAttribute("definition", OBS_COLLECTION_DEF);
        // get the count of records (total)
        StringBuilder strBuilder = new StringBuilder();
        for (int p = 0; p < this.handler.getProcedures().length; p++) {
            strBuilder.append(this.handler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, p));
        }

        if (StringUtils.countOccurrencesOf(strBuilder.toString(), "ERROR") > 0) {
            this.hasError = true;
            this.setupException(strBuilder.toString());
            return dr;
        }

        int count = StringUtils.countOccurrencesOf(strBuilder.toString(), BLOCK_SEPERATOR);
        // create count element
        dynamic_array.addContent(this.createElementCount(count));

        // create elementType "observations" element
        dynamic_array.addContent(this.createObservationsElement());

        // create encoding element
        dynamic_array.addContent(this.createEncodingElement());

        // add value block to values
        dynamic_array.addContent(this.createValuesElement(strBuilder));

        dynamic_data.addContent(dynamic_array);

        // add dynamic data to data record
        dr.addContent(dynamic_data);
        return dr;
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
         *       <swe2:DataChoice definition="http://mmisw.org/ont/ioos/swe_element_type/sensors">
         *         createDataChoiceForSensor()
         *       <swe2:DataChoice>
         *     </swe2:field>
         *   </swe2:DataRecord>
         * </swe2:elementType>
         */
        Element elementType = new Element("elementType", this.SWE2_NS).setAttribute("name", "observations");
        Element dataRecord = new Element("DataRecord", this.SWE2_NS);
        dataRecord.setAttribute("definition", SENSOR_OBS_COLLECTION);
        Element field = new Element("field", this.SWE2_NS).setAttribute("name", "time");
        Element time = new Element("Time", this.SWE2_NS).setAttribute("definition", "http://www.opengis.net/def/property/OGC/0/SamplingTime");
        time.addContent(new Element("uom", this.SWE2_NS).setAttribute("href", "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian", this.XLINK_NS));
        field.addContent(time);
        dataRecord.addContent(field);
        
        Element sensorField = new Element("field", this.SWE2_NS).setAttribute("name", "sensor");
        Element dataChoice = new Element("DataChoice", this.SWE2_NS);
        dataChoice.setAttribute("definition", STATIC_SENSORS_DEF);
        for (int i = 0; i < this.handler.getProcedures().length; i++) {
            String stName = this.handler.getCDMDataset().getStationName(i);
            // DataRecord has to have at least 2 fields
            List<String> sensors = new ArrayList<String>(this.handler.getRequestedObservedProperties());
            while (sensors.size() < 2) {
                sensors.add("dummy_item");
            }
            for (String sensor : sensors) {
                dataChoice.addContent(createDataChoiceForSensor(stName, sensor));
            }
        }
        sensorField.addContent(dataChoice);
        dataRecord.addContent(sensorField);

        elementType.addContent(dataRecord);
        return elementType;
    }

    private Element createDataChoiceForSensor(String stName, String sensor) {
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
        Element item = null;
        if (sensor.equalsIgnoreCase("dummy_item")) {
            item = new Element("item", this.SWE2_NS).setAttribute("name", sensor);
            return item;
        } else {
            String name = stationToFieldName(stName) + "_" + sensor.toLowerCase();
            item = new Element("item", this.SWE2_NS).setAttribute("name", name);

            String sensorDef = this.handler.getVariableStandardName(sensor);
            String sensorUnits = this.handler.getUnitsString(sensor);

            Element dataRecord = new Element("DataRecord", this.SWE2_NS);
            Element field = new Element("field", this.SWE2_NS).setAttribute("name", sensor);
            Element quantity = new Element("Quantity", this.SWE2_NS).setAttribute("definition", VocabDefinitions.GetDefinitionForParameter(sensorDef));
            quantity.addContent(new Element("uom", this.SWE2_NS).setAttribute("code", sensorUnits));

            // if the variable has a 'fill value' then add it as a nil value
            try {
                for (Attribute attr : this.handler.getVariableByName(sensor).getAttributes()) {
                    if (attr.getShortName().toLowerCase().contains("fillvalue")) {
                        Element nilValues = new Element("nilValues", this.SWE2_NS);
                        Element nnilValues = new Element("NilValues", this.SWE2_NS);

                        Element nvs = new Element("nilValue", this.SWE2_NS).setAttribute("reason", MISSING_REASON);
                        nvs.setText(attr.getValue(0).toString());
                        nnilValues.addContent(nvs);
                        nilValues.addContent(nnilValues);
                        quantity.addContent(nilValues);
                    }
                }
            } catch (Exception ex) {
            }

            field.addContent(quantity);
            dataRecord.addContent(field);

            item.addContent(dataRecord);
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
        List<String> obsProps = this.handler.getRequestedObservedProperties();
        for (String block : strBuilder.toString().split(BLOCK_SEPERATOR)) {
            // split on token seperator
            StringBuilder newBlock = new StringBuilder();
            for (String token : block.split(TOKEN_SEPERATOR)) {
                if (token.contains("time")) {
                    newBlock.append(token.replaceAll("time=", "")).append(TOKEN_SEPERATOR);
                } else if (token.contains("station")) {
                    String[] tokenSplit = token.split("=");
                    int stNum = Integer.parseInt(tokenSplit[1]);
                    newBlock.append(stationToFieldName(this.handler.getProcedures()[stNum])).append("_");
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
        Element values = new Element("values", this.SWE2_NS);
        values.setText(newString.substring(0, newString.length() - BLOCK_SEPERATOR.length()));
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
        Element elmcount = new Element("elementCount", this.SWE2_NS);
        Element ecount = new Element("Count", this.SWE2_NS);
        Element value = new Element("value", this.SWE2_NS);
        value.setText(String.valueOf(count));
        ecount.addContent(value);
        elmcount.addContent(ecount);
        return elmcount;
    }

    private Element createEncodingElement() {
        /*
         * Creates the following:
         * <swe2:encoding>
         *   <swe2:TextEncoding decimalSeparator="DECIMAL_SEPERATOR" tokenSeparator="TOKEN_SEPERATOR" blockSeparator="BLOCK_SEPERATOR" />
         * </swe2:encoding>
         */
        Element encoding = new Element("encoding", this.SWE2_NS);
        Element txe = new Element("TextEncoding", this.SWE2_NS);
        txe.setAttribute("decimalSeparator", DECIMAL_SEPERATOR);
        txe.setAttribute("tokenSeparator", TOKEN_SEPERATOR);
        txe.setAttribute("blockSeparator", BLOCK_SEPERATOR);
        encoding.addContent(txe);
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
        Element stStation = new Element("field", this.SWE2_NS);
        // change 'procedure' into the readable format described in the template
        String name = stationToFieldName(stName);
        stStation.setAttribute(NAME, name);
        // DataRecord
        Element record = new Element("DataRecord", this.SWE2_NS);
        record.setAttribute("id", name);
        record.setAttribute("definition", STATIC_STATION_DEF);
        // add field for stationId
        Element field = new Element("field", this.SWE2_NS);
        field.setAttribute("name", "stationID");
        Element text = new Element("Text", this.SWE2_NS);
        text.setAttribute("definition", "http://mmisw.org/ont/ioos/definition/stationID");
        Element value = new Element("value", this.SWE2_NS);
        value.setText(this.handler.getUrnName(stName));
        text.addContent(value);
        field.addContent(text);
        record.addContent(field);

        // field for platformLocation
        field = new Element("field", this.SWE2_NS);
        field.setAttribute("name", "platformLocation");
        field.addContent(createSwe2Vector(stNum));
        record.addContent(field);

        //fielf for sensors
        field = new Element("field", this.SWE2_NS);
        field.setAttribute("name", "sensors");
        Element dr = new Element("DataRecord", this.SWE2_NS);
        dr.setAttribute(DEFINITION, STATIC_SENSORS_DEF);
        // TODO: Need an example netcdf file that contain an explicit set of sensors in order
        // to properly create this field. It will not always be the case that observed
        // properties are also the sensors.
        // create a sensor field for each observed property
        for (String op : this.handler.getRequestedObservedProperties()) {
            dr.addContent(createSwe2Sensors(stName, stNum, op, name));
        }
        field.addContent(dr);
        record.addContent(field);

        stStation.addContent(record);
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
        Element retval = new Element("field", this.SWE2_NS);
        retval.setAttribute("name", op_name);
        Element dataRecord = new Element("DataReocrd", this.SWE2_NS);
        dataRecord.setAttribute("id", op_name);
        dataRecord.setAttribute(DEFINITION, STATIC_SENSOR_DEF);
        // sensorID field
        Element field = new Element("field", this.SWE2_NS);
        field.setAttribute("name", "sensorID");
        Element text = new Element("Text", this.SWE2_NS);
        text.setAttribute("definition", SENSOR_ID_DEF);
        Element value = new Element("value", this.SWE2_NS);
        value.setText(this.handler.getSensorUrnName(stName, op));
        text.addContent(value);
        field.addContent(text);
        dataRecord.addContent(field);

        // height field
        field = new Element("field", this.SWE2_NS);
        field.setAttribute("name", "height");
        Element quantity = new Element("Quantity", this.SWE2_NS);
        quantity.setAttribute("definintion", "http://mmisw.org/ont/cf/parameter/height");
        quantity.setAttribute("referenceFrame", "#PlatformFrame");
        quantity.addContent(new Element("uom", this.SWE2_NS).setAttribute("code", "m"));
        value = new Element("value", this.SWE2_NS);
        value.setText("0");      // TODO: Need to change this to reflect height of ... something ...
        quantity.addContent(value);
        field.addContent(quantity);
        dataRecord.addContent(field);

        retval.addContent(dataRecord);
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
        Element vector = new Element("Vector", this.SWE2_NS);
        vector.setAttribute("definition", "http://www.opengis.net/def/property/OGC/0/PlatformLocation");
        vector.setAttribute("referenceFrame", "https://ioostech.googlecode.com/svn/trunk/IoosCRS/IoosBuoyCRS.xml");
        vector.setAttribute("localFrame", "#PlatformFrame");
        // coords: lat, lon, z
        String lat = Double.toString(this.handler.getCDMDataset().getLowerLat(stNum));
        String lon = Double.toString(this.handler.getCDMDataset().getLowerLon(stNum));
        String alt = Double.toString(this.handler.getCDMDataset().getLowerAltitude(stNum));
        // create and add the swe2:coordinates
        vector.addContent(createSwe2Coordinate("latitude", "http://mmisw.org/ont/cf/parameter/latitude", "Lat", "deg", lat));
        vector.addContent(createSwe2Coordinate("longitude", "http://mmisw.org/ont/cf/parameter/longitude", "Lon", "deg", lon));
        vector.addContent(createSwe2Coordinate("height", "http://mmisw.org/ont/cf/parameter/height", "Z", "m", alt));
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
        Element coord = new Element("coordinate", this.SWE2_NS).setAttribute("name", name);
        Element quant = new Element("Quantity", this.SWE2_NS);
        quant.setAttribute("definition", definition);
        quant.setAttribute("axisID", axisId);
        // uom
        quant.addContent(new Element("uom", this.SWE2_NS).setAttribute("code", code));
        // value
        Element val = new Element("value", this.SWE2_NS);
        val.setText(value);
        quant.addContent(val);
        // quantity -> coordinate
        coord.addContent(quant);
        return coord;
    }

    /**
     * Go from urn to readable name (as in swe2:field name)
     * @param stName
     * @return 
     */
    private String stationToFieldName(String stName) {
        // get the gml urn
        String urn = this.handler.getUrnName(stName);
        // split on station/sensor
        String[] urnSplit = urn.split("(sensor|station):");
        // get the last index of split
        urn = urnSplit[urnSplit.length - 1];
        // convert to underscore
        String underScorePattern = "[\\+\\-\\s:]+";
        urn = urn.replaceAll(underScorePattern, "_");
        return urn.toLowerCase();
    }

    @Override
    public void writeOutput(Writer writer) throws IOException {
        if (!hasError) {
            this.createIoosSosResponse();
        }
        super.writeOutput(writer);
    }
}
