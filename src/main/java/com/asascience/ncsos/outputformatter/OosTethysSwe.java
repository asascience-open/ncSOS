/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.cdmclasses.iStationData;
import com.asascience.ncsos.getobs.SOSGetObservationRequestHandler;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.FeatureDataset;

/**
 * Sets up a xml document for a response to a Get Observation request that specifies
 * text/xml;subtype="om/1.0.0" as its response format.
 * @author SCowan
 */
public class OosTethysSwe implements SOSOutputFormatter {
    
    private ArrayList<DataSlice> infoList;
    private Document document;
    private String[] variableNames;
    private FeatureDataset featureDataset;
    private iStationData CDMDataSet;
    private String stationName;
    
    // metadata
    private String title, history, institution, source, description, location, featureOfInterest;
    
    private static final String OM_OBSERVATION = "om:Observation";
    private static final String STATION_GML_BASE = "urn:tds:station.sos:";
    private static final String NAN = "NaN";
    private static final String TEMPLATE = "templates/oostethysswe.xml";
    private static final String BLOCK_SEPERATOR = " ";
    private static final String TOKEN_SEPERATOR = ",";
    private static final String DECIMAL_SEPERATOR = ".";
    
    private DOMImplementationLS impl;
    
    /**
     * Creates a new text/xml;subtype="om/1.0.0" response, filling in the metadata
     * from the feature dataset.
     * @param variableNames the observedProperties from the request query
     * @param featureDataset the feature dataset from the base request handler
     * @param cdmDataset the CDMDataset from the base request handler
     * @param netcdfDataset the netcdf dataset that the request is polling
     */
    public OosTethysSwe(String[] variableNames,
            FeatureDataset featureDataset,
            iStationData cdmDataset,
            NetcdfDataset netcdfDataset) {
        infoList = new ArrayList<DataSlice>();
        this.featureDataset = featureDataset;
        this.CDMDataSet = cdmDataset;
        this.variableNames = variableNames;
        
        document = parseTemplateXML();
        
        setMetaData(netcdfDataset.findAttValueIgnoreCase(null, "title", "NONE"),
                netcdfDataset.findAttValueIgnoreCase(null, "history", "NONE"),
                netcdfDataset.findAttValueIgnoreCase(null, "institution", "NONE"),
                netcdfDataset.findAttValueIgnoreCase(null, "source", "NONE"),
                netcdfDataset.findAttValueIgnoreCase(null, "description", "NONE"),
                netcdfDataset.findAttValueIgnoreCase(null, "location", "NONE"),
                netcdfDataset.findAttValueIgnoreCase(null, "featureOfInterestBaseQueryURL", null));
        
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    /* ***************** */
    /* Interface methods */
    /**************************************************************************/
    
    public void addDataFormattedStringToInfoList(String dataFormattedString) throws IllegalArgumentException {
        // CSV that should be of the form: eventtime, depth, lat, lon, data value
        String[] values = dataFormattedString.split(",");
        double lat, lon, depth;
        lat = lon = depth = Double.NaN;
        String eventtime = null;
        float[] dataValues = null;
        int stationNumber = -1;
        
        if(infoList == null)
            infoList = new ArrayList<DataSlice>();
        
        for (String val : values) {
            // print error if one is recieved
            if (val.contains("ERROR")) {
                setupExceptionOutput(val);
                return;
            }
            // skip any empty pieces
            if (!val.contains("="))
                continue;
            String[] valuePiece = val.split("=");
            // if..else if..else for determining piece
            if (valuePiece[0].equals("depth")) {
                try {
                    depth = Double.parseDouble(valuePiece[1]);
                } catch (Exception e) {
                    depth = Double.NaN;
                    System.out.println("Error parsing depth " + valuePiece[1] + " - " + e.getMessage());
                }
            } else if (valuePiece[0].equals("lat")) {
                try {
                    lat = Double.parseDouble(valuePiece[1]);
                } catch (Exception e) {
                    lat = Double.NaN;
                    System.out.println("Error parsing lat " + valuePiece[1] + " - " + e.getMessage());
                }
            } else if (valuePiece[0].equals("lon")) {
                try {
                    lon = Double.parseDouble(valuePiece[1]);
                } catch (Exception e) {
                    lon = Double.NaN;
                    System.out.println("Error parsing lon " + valuePiece[1] + " - " + e.getMessage());
                }
            } else if (valuePiece[0].equals("time")) {
                eventtime = valuePiece[1];
            } else if (valuePiece[0].equals("station")) {
                try {
                    stationNumber = Integer.parseInt(valuePiece[1]);
                } catch (Exception ex) {
                    stationNumber = -1;
                }
            } else {
                // assume a data value and add it to the string array
                if (dataValues == null) {
                    dataValues = new float[1];
                } else {
                    dataValues = expandDataArray(dataValues);
                }
                try {
                    dataValues[dataValues.length - 1] = Float.parseFloat(valuePiece[1]);
                } catch (Exception e) {
                    dataValues[dataValues.length - 1] = Float.NaN;
                    System.out.println("Error parsing data value " + valuePiece[1] + " - " + e.getMessage());
                }
            }
        }
        
        // add to info list
        infoList.add(new DataSlice(lat, lon, depth, eventtime, dataValues));
        infoList.get(infoList.size()-1).setStationNumber(stationNumber);
    }

    public void emtpyInfoList() {
        infoList = null;
    }

    public void setupExceptionOutput(String message) {
        document = XMLDomUtils.getExceptionDom(message);
    }

    public void writeOutput(Writer writer) {
        if (!document.getFirstChild().getNodeName().equalsIgnoreCase("exceptionreport"))
            parseObservations();
        // output our document to the writer
        LSSerializer xmlSerializer = impl.createLSSerializer();
        LSOutput xmlOut = impl.createLSOutput();
        xmlSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        xmlOut.setCharacterStream(writer);
        xmlSerializer.write(document, xmlOut);
    }
    
    /**************************************************************************/
    
    private String getTemplateLocation() {
        return TEMPLATE;
    }
    
    private Document parseTemplateXML() {
        InputStream templateInputStream = null;
        try {
            templateInputStream = getClass().getClassLoader().getResourceAsStream(getTemplateLocation());
            return XMLDomUtils.getTemplateDom(templateInputStream);
        } finally {
            if (templateInputStream != null) {
                try {
                    templateInputStream.close();
                } catch (IOException e) {
                    // ignore, closing..
                }
            }
        }
    }
    
    private float[] expandDataArray(float[] dataArray) {
        float[] retval = new float[dataArray.length+1];
        
        for(int i=0;i<dataArray.length;i++) {
            retval[i] = dataArray[i];
        }
        
        return retval;
    }
    
    private void addDatasetResults(int stationNumber) {
        String varNameLen = Integer.toString(variableNames.length+1);
        //add Data Block Definition
        document = XMLDomUtils.addNode(document, "om:result", "swe:DataArray", stationNumber);
        //element count
        document = XMLDomUtils.addNode(document, "swe:DataArray", "swe:elementCount", stationNumber);
        document = XMLDomUtils.addNode(document, "swe:elementCount", "swe:Count", stationNumber);
        document = XMLDomUtils.addNode(document, "swe:Count", "swe:value", varNameLen, stationNumber);
        //element Type
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataArray", "swe:elementType", "name", "SimpleDataArray", stationNumber);
        //add data record
        document = XMLDomUtils.addNode(document, "swe:elementType", "swe:DataRecord", stationNumber);
        
        setDataValues(stationNumber);
        
        //add encoding value
        document = XMLDomUtils.addNode(document, "swe:DataArray", "swe:encoding", stationNumber);
        // text block
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:encoding", "swe:TextBlock", "blockSeparator", BLOCK_SEPERATOR, stationNumber);
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "decimalSeparator", DECIMAL_SEPERATOR);
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "tokenSeparator", TOKEN_SEPERATOR);

        try {
            //set the data
            document = XMLDomUtils.addNode(document, "swe:DataArray", "swe:values", createObservationString(stationNumber), stationNumber);
        } catch (Exception ex) {
            setupExceptionOutput(ex.getMessage());
            Logger.getLogger(SOSGetObservationRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void setDataValues(int stationNumber) {
        int fieldIndex = 0;
        int quantityIndex = 0;

        //add fields
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:DataRecord", "swe:field", "name", "time", stationNumber);
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:field", "swe:Time", fieldIndex++, "definition", "urn:ogc:phenomenon:time:iso8601", stationNumber);
        
        // add lat and lon first
        for (String observedProperty : variableNames) {
            if (observedProperty.contains("lat") || observedProperty.contains("lon")) {
                VariableSimpleIF variable = featureDataset.getDataVariable(observedProperty);
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

        // add other observed property
        for (String observedProperty : variableNames) {
            if (!observedProperty.contains("lat") && !observedProperty.contains("lon")) {
                VariableSimpleIF variable = featureDataset.getDataVariable(observedProperty);
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
    }
    
    private void parseObservations() {
        if (CDMDataSet == null) {
            setupExceptionOutput("CDMDataSet is null");
            return;
        }
        
        setObsCollectionMetaData();

        int numStations;
            numStations = CDMDataSet.getNumberOfStations();
            
        //add observation 
        //*********THIS IS FOR NUMBER OF STATIONS!
        for (int stNum = 0; stNum < numStations; stNum++) {
            if (CDMDataSet.isStationInFinalList(stNum)) {
                document = XMLDomUtils.addObservationElement(document);
                //add description
                //if (CDMDataSet != null) {
                //document = XMLDomUtils.addNode(document, OM_OBSERVATION, "gml:description", CDMDataSet.getDescription(stNum), stNum);
                //} else {
                document = XMLDomUtils.addNode(document, OM_OBSERVATION, "gml:description", description, stNum);
                //}
                //add name
                document = XMLDomUtils.addNode(document, OM_OBSERVATION, "gml:name", title, stNum);
                //add bounded by
                document = XMLDomUtils.addNode(document, OM_OBSERVATION, "gml:boundedBy", stNum);
                //add envelope and attribute
                document = XMLDomUtils.addNode(document, OM_OBSERVATION, "gml:boundedBy", "gml:Envelope", "srsName", STATION_GML_BASE + CDMDataSet.getStationName(stNum), stNum);

                //add lat lon string
                document = XMLDomUtils.addNode(document, OM_OBSERVATION, "gml:Envelope", "gml:lowerCorner", getStationLowerLatLonStr(stNum), stNum);
                //add Upper GPS coors
                document = XMLDomUtils.addNode(document, OM_OBSERVATION, "gml:Envelope", "gml:upperCorner", getStationUpperLatLonStr(stNum), stNum);
                //add sampling time
                document = XMLDomUtils.addNode(document, OM_OBSERVATION, "om:samplingTime", stNum);
                //add time instant
                document = XMLDomUtils.addNode(document, OM_OBSERVATION, "om:samplingTime", "gml:TimePeriod", "gml:id", "DATA_TIME", stNum);
                //add time positions (being and end)
                if (CDMDataSet != null) {
                    document = XMLDomUtils.addNode(document, OM_OBSERVATION, "gml:TimePeriod", "gml:beginPosition", CDMDataSet.getTimeBegin(stNum), stNum);
                    document = XMLDomUtils.addNode(document, OM_OBSERVATION, "gml:TimePeriod", "gml:endPosition", CDMDataSet.getTimeEnd(stNum), stNum);
                }
                //add procedure
                document = XMLDomUtils.addNodeAndAttribute(document, OM_OBSERVATION, "om:procedure", "xlink:href", location, stNum);

                //add observedProperties
                for (int i = 0; i < variableNames.length; i++) {
                    String variableName = variableNames[i];
                    document = XMLDomUtils.addNodeAndAttribute(document, OM_OBSERVATION, "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + variableName, stNum);
                }

                //if (isDepthAvailable == true) {
                //doc = XMLDomUtils.addNodeAndAttribute(doc, OM_OBSERVATION, "om:observedProperty", "xlink:href", "http://marinemetadata.org/cf#" + "depth");
                //}

                //add feature of interest
                if (featureOfInterest != null) {
                    document = XMLDomUtils.addNodeAndAttribute(document, OM_OBSERVATION, "om:featureOfInterest", "xlink:href", featureOfInterest + CDMDataSet.getStationName(stNum), stNum);
                } else {
                    document = XMLDomUtils.addNodeAndAttribute(document, OM_OBSERVATION, "om:featureOfInterest", "xlink:href", CDMDataSet.getStationName(stNum), stNum);
                }
                //add results Node
                document = XMLDomUtils.addNode(document, OM_OBSERVATION, "om:result", stNum);

                addDatasetResults(stNum);
                
            } else {
                // add empty observation
                document = XMLDomUtils.addObservationElement(document);
            }
        }
    }
    
    private void setMetaData(String title,
            String history,
            String institution,
            String source,
            String description,
            String location,
            String featureOfInterest) {
        this.title = title;
        this.history = history;
        this.institution = institution;
        this.source = source;
        this.description = description;
        this.location = location;
        this.featureOfInterest = featureOfInterest;
    }
            
    private void setSystemGMLID() {

        StringBuilder b = new StringBuilder();
        if (CDMDataSet != null) {

            for (int i = 0; i < CDMDataSet.getNumberOfStations(); i++) {
                b.append(stationName);
                b.append(",");
            }
        }
        // so below is odd, the 'getGMLID' function returns the string that is passed into it. I am assuming that there needs
        // to be more to it than that, so leaving this in commented out until i can affirm what this should be
//        XMLDomUtils.setObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id", getGMLID("GML_ID_NAME"));
        // meantime place-holder
        XMLDomUtils.setObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id", "GML_ID_NAME");
    }

    private void setCollectionDescription() {
        XMLDomUtils.setNodeValue(document, "om:ObservationCollection", "gml:description", description);
    }

    private void setCollectionName() {
        XMLDomUtils.setNodeValue(document, "om:ObservationCollection", "gml:name", title);
    }

    private void setCollectionSourceName() {
        XMLDomUtils.setAttributeFromNode(document, "gml:boundedBy", "gml:Envelope", "srsName", "EPSG:4326");
    }

    private void setCollectionLowerCornerEnvelope() {
        XMLDomUtils.setNodeValue(document, "gml:Envelope", "gml:lowerCorner", getBoundLowerLatLonStr());
    }

    private void setCollectionUpperCornerEnvelope() {
        XMLDomUtils.setNodeValue(document, "gml:Envelope", "gml:upperCorner", getBoundUpperLatLonStr());
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
    
    private String createObservationString(int stationNumber) {
        StringBuilder retVal = new StringBuilder();
        for(DataSlice ds : infoList) {
            if (ds.getStationNumber() != -1 && ds.getStationNumber() != stationNumber)
                continue;
            // add the slice to the string
            if(ds.getEventTime() != null)
                retVal.append(ds.getEventTime()).append(TOKEN_SEPERATOR);
            if(!ds.getLatitude().toString().equals(NAN))
                retVal.append(ds.getLatitude().toString()).append(TOKEN_SEPERATOR);
            if(!ds.getLongitude().toString().equals(NAN))
                retVal.append(ds.getLongitude().toString()).append(TOKEN_SEPERATOR);
            if(!ds.getDepth().toString().equals(NAN))
                retVal.append(ds.getDepth().toString()).append(TOKEN_SEPERATOR);
            if(ds.getDataValues() != null) {
                for (Float dv : ds.getDataValues()) {
                    retVal.append(dv.toString()).append(TOKEN_SEPERATOR);
                }
                // remove last TOKEN_SEPERATOR
                retVal = retVal.deleteCharAt(retVal.length()-1);
            }
            retVal.append(BLOCK_SEPERATOR);
        }
        if (retVal.length() > 1) {
            // remove last BLOCK_SEPERATOR
            retVal = retVal.deleteCharAt(retVal.length()-1);
        }
        return retVal.toString();
    }
    
     /**
     * get the lower lat lon string 
     * @return 
     */
    private String getStationLowerLatLonStr(int stNum) {
        return (new StringBuilder()).append(SOSBaseRequestHandler.formatDegree(CDMDataSet.getLowerLat(stNum))).append(" ").append(SOSBaseRequestHandler.formatDegree(CDMDataSet.getLowerLon(stNum))).append(" ").append(CDMDataSet.getLowerAltitude(stNum)).toString();
    }

    /**
     * get the upper lat lon string 
     * @return 
     */
    private String getStationUpperLatLonStr(int stNum) {
        return (new StringBuilder()).append(SOSBaseRequestHandler.formatDegree(CDMDataSet.getUpperLat(stNum))).append(" ").append(SOSBaseRequestHandler.formatDegree(CDMDataSet.getUpperLon(stNum))).append(" ").append(CDMDataSet.getUpperAltitude(stNum)).toString();
    }

    /**
     * get the upper lat lon string all stations
     * @return 
     */
    private String getBoundUpperLatLonStr() { 
        return (new StringBuilder()).append(SOSBaseRequestHandler.formatDegree(CDMDataSet.getBoundUpperLat())).append(" ").append(SOSBaseRequestHandler.formatDegree(CDMDataSet.getBoundUpperLon())).append(" ").append(CDMDataSet.getBoundUpperAlt()).toString();
    }

    /**
     * get the lower lat lon string all stations
     * @return 
     */
    private String getBoundLowerLatLonStr() {
        return (new StringBuilder()).append(SOSBaseRequestHandler.formatDegree(CDMDataSet.getBoundLowerLat())).append(" ").append(SOSBaseRequestHandler.formatDegree(CDMDataSet.getBoundLowerLon())).append(" ").append(CDMDataSet.getBoundLowerAlt()).toString();

    }
    
}
