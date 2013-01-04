/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.cdmclasses.baseCDMClass;
import com.asascience.ncsos.getobs.SOSGetObservationRequestHandler;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 *
 * @author SCowan
 */
public class OosTethysSweV2 implements SOSOutputFormatter {

    private static final String TEMPLATE = "templates/oostethysswe.xml";
    private static final String XLINK = "xlink:href";
    private static final String OM_OBSERVATION = "om:Observation";
    private static final String STATION_GML_BASE = "urn:ioos:station:" + SOSBaseRequestHandler.getNamingAuthority() + ":";
    private static final String MMI_CF = "http://mmisw.org/ont/cf/parameter/";
    private static final String BLOCK_SEPERATOR = " ";
    private static final String TOKEN_SEPERATOR = ",";
    private static final String DECIMAL_SEPERATOR = ".";
    
    private final SOSGetObservationRequestHandler obsHandler;
    
    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(OosTethysSweV2.class);
    
    private DOMImplementationLS impl;
    private Document document;
    private ArrayList<DataSlice> infoList;
    
    public OosTethysSweV2(SOSGetObservationRequestHandler obsHandler) {
        
        if (obsHandler == null) {
            BadInputs();
            this.obsHandler = null;
            return;
        }
        
        this.infoList = new ArrayList<DataSlice>();
        parseTemplateXML();
        this.obsHandler = obsHandler;
        
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            this.impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            this.impl = null;
        }
    }
    
    private void BadInputs() {
        setupExceptionOutput("Unable to create observation collection - missing or invalid info.");
    }
    
    private void parseTemplateXML() {
        InputStream templateInputStream = null;
        try {
            templateInputStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE);
            document = XMLDomUtils.getTemplateDom(templateInputStream);
        } catch (Exception ex) {
            _log.error(ex.toString());
        } finally {
            if (templateInputStream != null) {
                try {
                    templateInputStream.close();
                } catch (IOException e) {
                    // ignore, closing..
                    _log.error(e.toString());
                }
            }
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Interface Methods">
    public void addDataFormattedStringToInfoList(String dataFormattedString) {
//        System.out.println("addDataFormattedStringToInfoList unused by OosTethysSweV2");
    }
    
    public void emtpyInfoList() {
        infoList = null;
    }
    
    public void setupExceptionOutput(String message) {
        _log.debug(message);
        document = XMLDomUtils.getExceptionDom(message);
    }
    
    public void writeOutput(Writer writer) {
        // create output if we don't already have an exception  
        if (!document.getFirstChild().getNodeName().equalsIgnoreCase("exceptionreport"))
            parseObservations(obsHandler.getProcedures());
        // output our document to the writer
        LSSerializer xmlSerializer = impl.createLSSerializer();
        LSOutput xmlOut = impl.createLSOutput();
        xmlSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        xmlOut.setCharacterStream(writer);
        _log.debug(document.toString());
        xmlSerializer.write(document, xmlOut);
    }
    //</editor-fold>

    private void parseObservations(String[] procedures) {
        _log.debug(procedures.length + " procedures");
        // set the station observation name, desc and bounds
        setCollectionInfo();
        // iterate through the requested stations
        for (int index=0; index<procedures.length; index++) {
            String proc = procedures[index];
            
            Element parent = addNewObservation();
            setObservationMeta(parent, proc, index);
            parent.appendChild(getResultNode(index));
        }
    }
    
    
    private Element addNewObservation() {
        Element member = (Element) document.getElementsByTagName("om:member").item(0);
        Element observation = document.createElement(OM_OBSERVATION);
        
        Element parent = document.createElement("gml:description");
        observation.appendChild(parent);
        parent = document.createElement("gml:name");
        observation.appendChild(parent);
        parent = document.createElement("gml:boundedBy");
        observation.appendChild(parent);
        parent = document.createElement("om:samplingTime");
        observation.appendChild(parent);
        
        member.appendChild(observation);
        
        return observation;
    }
    
    private void setObservationMeta(Element parent, String procName, int index) {
        // set description and name
//        parent.getElementsByTagName("gml:description").item(0).setTextContent("");
//        parent.getElementsByTagName("name").item(0).setTextContent("");
        // set bounded by
        Element bounds = (Element) parent.getElementsByTagName("gml:boundedBy").item(0);
        Element envelope = document.createElement("gml:Envelope");
        envelope.setAttribute("srsName", getSRSName());
        envelope.appendChild(createNodeWithText("gml:lowerCorner", obsHandler.getStationLowerCorner(index)));
        envelope.appendChild(createNodeWithText("gml:upperCorner", obsHandler.getStationUpperCorner(index)));
        bounds.appendChild(envelope);
        // sampling time
        Element samplingTime = (Element) parent.getElementsByTagName("om:samplingTime").item(0);
        Element timePeriod = document.createElement("gml:TimePeriod");
        timePeriod.setAttribute("gml:id", "DATA_TIME");
        timePeriod.appendChild(createNodeWithText("gml:beginPosition", obsHandler.getStartTime(index)));
        timePeriod.appendChild(createNodeWithText("gml:endPosition", obsHandler.getEndTime(index)));
        samplingTime.appendChild(timePeriod);
        // procedure
        parent.appendChild(createNodeWithAttribute("om:procedure", XLINK, STATION_GML_BASE + procName));
        // add each of the observed properties we are looking for
        for (String obs : obsHandler.getObservedProperties()) {
            // don't add height/depth vars; lat & lon
            if (!obs.equalsIgnoreCase("alt") && !obs.equalsIgnoreCase("height") && !obs.equalsIgnoreCase("z") &&
                !obs.equalsIgnoreCase("lat") && !obs.equalsIgnoreCase("lon"))
                parent.appendChild(createNodeWithAttribute("om:observedProperty", XLINK, obs));
        }
        // feature of interest
        parent.appendChild(createNodeWithAttribute("om:featureOfInterest", XLINK, obsHandler.getFeatureOfInterest(procName)));
    }
    
    private String getSRSName() {
        if (obsHandler.getCRSSRSAuthorities() != null) {
            _log.debug(obsHandler.getCRSSRSAuthorities()[0]);
            return obsHandler.getCRSSRSAuthorities()[0];
        } else {
            return "http://www.opengis.net/def/crs/EPSG/0/4326";
        }
    }
    
    private Element createNodeWithText(String elemName, String text) {
        Element retval = document.createElement(elemName);
        retval.setTextContent(text);
        return retval;
    }
    
    private Element createNodeWithAttribute(String elemName, String attrName, String attrValue) {
        Element retval = document.createElement(elemName);
        retval.setAttribute(attrName, attrValue);
        return retval;
    }
    
    private Element createField(String name, String code) {
        return createField(name, code, null);
    }
    
    private Element createField(String name, String code, String fillValue) {
        Element retval = document.createElement("swe:field");
        retval.setAttribute("name", name);
        Element quantity = document.createElement("swe:Quantity");
        String definition = MMI_CF + name;
        quantity.setAttribute("definition", definition);
        quantity.appendChild(createNodeWithAttribute("swe:uom", "code", code));
        if (fillValue != null) {
            Element nilValues = document.createElement("swe:nilValues");
            Element filValues = createNodeWithAttribute("swe:nilValue", "reason", "http://www.opengis.net/def/nil/OGC/0/missing");
            filValues.setTextContent(fillValue);
            nilValues.appendChild(filValues);
            quantity.appendChild(nilValues);
        }
        retval.appendChild(quantity);
        return retval;
    }
    
    private Element createTimeField(String name, String def) {
        Element retval = createNodeWithAttribute("swe:field", "name", name);
        Element time = createNodeWithAttribute("swe:Time", "definition", "http://www.opengis.net/def/property/OGC/0/SamplingTime");
        time.appendChild(createNodeWithAttribute("swe:uom", XLINK, "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian"));
        retval.appendChild(time);
        return retval;
    }
    
    private Element getEncodingElement() {
        Element retval = document.createElement("swe:encoding");
        Element txtBlock = createNodeWithAttribute("swe:TextBlock", "blockSeparator", BLOCK_SEPERATOR);
        txtBlock.setAttribute("decimalSeparator", DECIMAL_SEPERATOR);
        txtBlock.setAttribute("tokenSeparator", TOKEN_SEPERATOR);
        retval.appendChild(txtBlock);
        return retval;
    }

    private org.w3c.dom.Node getResultNode(int index) {
        Element parent = document.createElement("om:result");
        
        Element dataArray = document.createElement("swe:dataArray");
        
        Element elemCount = document.createElement("swe:elementCount");
        Element count = document.createElement("swe:Count");
        count.appendChild(createNodeWithText("swe:value", "" + obsHandler.getObservedProperties().length));
        elemCount.appendChild(count);
        dataArray.appendChild(elemCount);
        
        Element elemType = document.createElement("swe:elementType");
        elemType.setAttribute("name", "SimpleDataArray");
        Element dataRecord = document.createElement("swe:DataRecord");
        boolean timeSet = false;
        Element timeField = null;
        ArrayList<Element> opFields = new ArrayList<Element>();
        for (String obsProp: obsHandler.getObservedProperties()) {
            if (obsProp.toLowerCase().contains("time") && !timeSet) {
                timeField = createTimeField(obsProp, "iso8601");
                timeSet = true;
            } else {
                // need to set the data record for each observed property which requires the name and source (both of which can be retrieved from the observed property)
                // and the units the measurement is taken in
                // source (namespace) is the value before parameter, name is the last value in the split
                if (obsHandler.hasFillValue(obsProp)) {
                    opFields.add(createField(obsProp, obsHandler.getUnitsString(obsProp), obsHandler.getFillValue(obsProp)));
                } else {
                    opFields.add(createField(obsProp, obsHandler.getUnitsString(obsProp)));
                }
            }
        }
        if (!timeSet) {
            timeField = createTimeField("time", "iso8601");
        }
        dataRecord.appendChild(timeField);
        for (Element field : opFields) {
            dataRecord.appendChild(field);
        }
        elemType.appendChild(dataRecord);
        dataArray.appendChild(elemType);
        
        dataArray.appendChild(getEncodingElement());
        
//        dataArray.appendChild(createNodeWithText("swe:values", obsHandler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, index)));
        dataArray.appendChild(createNodeWithText("swe:values", processDataBlock(obsHandler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, index))));
        
        parent.appendChild(dataArray);
        
        return parent;
    }
    
    private String processDataBlock(String dataBlock) {
        // split on token separator then on '='
        StringBuilder retval = new StringBuilder();
        String[] blockSplit = dataBlock.split(BLOCK_SEPERATOR);
        for (String block : blockSplit) {
            String[] tokenSplit = block.split(TOKEN_SEPERATOR);
            for (String obsValue : tokenSplit) {
                String[] obs = obsValue.split("=");
                if (obs.length > 1 && (obs[0].equals("time") || isInRequestObservedProperties(obs[0]))) {
                    retval.append(obs[1]).append(TOKEN_SEPERATOR);
                }
            }
            // remove last token seperator
            if (retval.length() > 1)
                retval.deleteCharAt(retval.length()-1);
            retval.append(BLOCK_SEPERATOR);
        }
        // remove last block separator
        if (retval.length() > 1)
                retval.deleteCharAt(retval.length()-1);
        
        return retval.toString();
    }
    
    private boolean isInRequestObservedProperties(String name) {
        for (String obsProp : obsHandler.getObservedProperties()) {
            if (obsProp.equals(name))
                return true;
        }
        return false;
    }

    private void setCollectionInfo() {
        Element coll = (Element) document.getElementsByTagName("om:ObservationCollection").item(0);
        coll.getElementsByTagName("gml:name").item(0).setTextContent(obsHandler.getTitle());
        coll.getElementsByTagName("gml:description").item(0).setTextContent(obsHandler.getDescription());
        
        Element bounds = (Element) coll.getElementsByTagName("gml:boundedBy").item(0);
        ((Element)bounds.getElementsByTagName("gml:Envelope").item(0)).setAttribute("srsName", getSRSName());
        bounds.getElementsByTagName("gml:lowerCorner").item(0).setTextContent(obsHandler.getBoundedLowerCorner());
        bounds.getElementsByTagName("gml:upperCorner").item(0).setTextContent(obsHandler.getBoundedUpperCorner());
    }
}
