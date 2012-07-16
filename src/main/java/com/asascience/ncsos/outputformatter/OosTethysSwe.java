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
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.FeatureDataset;

/**
 *
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
    
    /**
     * 
     * @param variableNames
     * @param featureDataset
     * @param cdmDataset
     * @param netcdfDataset
     */
    public OosTethysSwe(String[] variableNames,
            FeatureDataset featureDataset,
            iStationData cdmDataset,
            NetcdfDataset netcdfDataset) {
        infoList = new ArrayList<DataSlice>();
        this.featureDataset = featureDataset;
        this.CDMDataSet = cdmDataset;
        this.variableNames = variableNames;
        
        title = history = institution = source = description = location = "none";
        featureOfInterest = "";
        document = parseTemplateXML();
        
        setMetaData(netcdfDataset.findAttValueIgnoreCase(null, "title", "empty title"),
                netcdfDataset.findAttValueIgnoreCase(null, "history", "empty history"),
                netcdfDataset.findAttValueIgnoreCase(null, "institution", "empty institution"),
                netcdfDataset.findAttValueIgnoreCase(null, "source", "empty source"),
                netcdfDataset.findAttValueIgnoreCase(null, "description", "empty description"),
                netcdfDataset.findAttValueIgnoreCase(null, "location", "empty location"),
                netcdfDataset.findAttValueIgnoreCase(null, "featureOfInterestBaseQueryURL", null));
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
    
    /**
     * 
     * @return
     */
    public String getTemplateLocation() {
        return TEMPLATE;
    }

    /* *****************
     * Interface methods
     ******************* */
    public void AddDataFormattedStringToInfoList(String dataFormattedString) throws IllegalArgumentException {
        // CSV that should be of the form: eventtime, depth, lat, lon, data value
        String[] values = dataFormattedString.split(",");
        double lat, lon, depth;
        lat = lon = depth = Double.NaN;
        String eventtime = null;
        float[] dataValues = null;
        
        if(infoList == null)
            infoList = new ArrayList<DataSlice>();
        
        for (String val : values) {
            // skip an empty pieces
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
    }

    public void EmtpyInfoList() {
        infoList = null;
    }

    public void setupExceptionOutput(String message) {
        document = XMLDomUtils.getExceptionDom(message);
    }

    public void writeOutput(Writer writer) {
        parseObservations();
        // output our document to the writer
        DOMSource domSource = new DOMSource(document);
        Result result = new StreamResult(writer);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(domSource, result);
        } catch (Exception e) {
            System.out.println("Error in writing OosTethysSwe - " + e.getMessage());
        }
    }
    
    /********************************************/
    
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
        
        setDataValues(stationNumber);
        
        //add encoding value
        document = XMLDomUtils.addNodeAllOptions(document, "swe:DataArray", "swe:encoding", stationNumber);
        // text block
        document = XMLDomUtils.addNodeAndAttribute(document, "swe:encoding", "swe:TextBlock", "blockSeparator", " ", stationNumber);
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "decimalSeparator", ".");
        XMLDomUtils.setAttributeFromNode(document, "swe:encoding", "swe:TextBlock", "tokenSeparator", ",");

        try {
            //set the data
            document = XMLDomUtils.addNodeAllOptions(document, "swe:DataArray", "swe:values", createObservationString(), stationNumber);
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

        // add observed property
        for (String observedProperty : variableNames) {
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
            document = XMLDomUtils.addObservationElement(document);
            //add description
            //if (CDMDataSet != null) {
            //document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:description", CDMDataSet.getDescription(stNum), stNum);
            //} else {
            document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:description", description, stNum);
            //}
            //add name
            document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:name", title, stNum);
            //add bounded by
            document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:boundedBy", stNum);
            //add envelope and attribute
            document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:boundedBy", "gml:Envelope", "srsName", STATION_GML_BASE + CDMDataSet.getStationName(stNum), stNum);

            //add lat lon string
            document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:Envelope", "gml:lowerCorner", getStationLowerLatLonStr(stNum), stNum);
            //add Upper GPS coors
            document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:Envelope", "gml:upperCorner", getStationUpperLatLonStr(stNum), stNum);
            //add sampling time
            document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "om:samplingTime", stNum);
            //add time instant
            document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "om:samplingTime", "gml:TimePeriod", "gml:id", "DATA_TIME", stNum);
            //add time positions (being and end)
            if (CDMDataSet != null) {
                document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:TimePeriod", "gml:beginPosition", CDMDataSet.getTimeBegin(stNum), stNum);
                document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "gml:TimePeriod", "gml:endPosition", CDMDataSet.getTimeEnd(stNum), stNum);
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
            document = XMLDomUtils.addNodeAllOptions(document, OM_OBSERVATION, "om:result", stNum);

            addDatasetResults(stNum);
        }
    }
            
    void setSystemGMLID() {

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
    
    private String createObservationString() {
        StringBuilder retVal = new StringBuilder();
        for(DataSlice ds : infoList) {
            // add the slice to the string
            if(ds.getEventTime() != null)
                retVal.append(ds.getEventTime()).append(",");
            if(!ds.getLatitude().toString().equals(NAN))
                retVal.append(ds.getLatitude().toString()).append(",");
            if(!ds.getLongitude().toString().equals(NAN))
                retVal.append(ds.getLongitude().toString()).append(",");
            if(!ds.getDepth().toString().equals(NAN))
                retVal.append(ds.getDepth().toString()).append(",");
            if(ds.getDataValues() != null) {
                for (Float dv : ds.getDataValues()) {
                    retVal.append(dv.toString()).append(",");
                }
                // remove last comma
                retVal = retVal.deleteCharAt(retVal.length()-1);
            }
            retVal.append(" \n");
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
