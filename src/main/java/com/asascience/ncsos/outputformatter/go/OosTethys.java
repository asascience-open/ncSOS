/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter.go;

import com.asascience.ncsos.getobs.SOSGetObservationRequestHandler;
import com.asascience.ncsos.outputformatter.DataSlice;
import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 *
 * @author SCowan
 */
public class OosTethys extends SOSOutputFormatter {

    private static final String TEMPLATE = "templates/GO_oostethys.xml";
    private static final String XLINK = "xlink:href";
    private static final String OBSERVATION = "Observation";
    private static final String STATION_GML_BASE = "urn:ioos:station:" + SOSBaseRequestHandler.getNamingAuthority() + ":";
    private static final String MMI_CF = "http://mmisw.org/ont/cf/parameter/";
    private static final String BLOCK_SEPERATOR = " ";
    private static final String TOKEN_SEPERATOR = ",";
    private static final String DECIMAL_SEPERATOR = ".";
    
    private final SOSGetObservationRequestHandler obsHandler;
    
    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(OosTethys.class);
    
    private DOMImplementationLS impl;
    private ArrayList<DataSlice> infoList;
    
    public OosTethys(SOSGetObservationRequestHandler obsHandler) {
        
        if (obsHandler == null) {
            BadInputs();
            this.obsHandler = null;
            return;
        }
        
        this.infoList = new ArrayList<DataSlice>();
        parseTemplateXML();
        initNamespaces();

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
            this.document = XMLDomUtils.getTemplateDom(templateInputStream);
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
//        System.out.println("addDataFormattedStringToInfoList unused by OosTethys");
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
        Element member = (Element) document.getElementsByTagNameNS(OM_NS, MEMBER).item(0);
        Element observation = createElementNS(OM_NS, OBSERVATION);
        
        Element parent = createElementNS(GML_NS, DESCRIPTION);
        observation.appendChild(parent);
        parent = createElementNS(GML_NS, NAME);
        observation.appendChild(parent);
        parent = createElementNS(GML_NS, BOUNDED_BY);
        observation.appendChild(parent);
        parent = createElementNS(OM_NS, SAMPLING_TIME);
        observation.appendChild(parent);
        
        member.appendChild(observation);
        
        return observation;
    }
    
    private void setObservationMeta(Element parent, String procName, int index) {
        // set description and name
//        parent.getElementsByTagName("gml:description").item(0).setTextContent("");
//        parent.getElementsByTagName("name").item(0).setTextContent("");
        // set bounded by
        Element bounds = (Element) parent.getElementsByTagNameNS(GML_NS, BOUNDED_BY).item(0);
        Element envelope = createElementNS(GML_NS, ENVELOPE);
        envelope.setAttribute(SRS_NAME, getSRSName());
        envelope.appendChild(createNodeWithText(GML_NS, LOWER_CORNER, obsHandler.getStationLowerCorner(index)));
        envelope.appendChild(createNodeWithText(GML_NS, UPPER_CORNER, obsHandler.getStationUpperCorner(index)));
        bounds.appendChild(envelope);
        // sampling time
        Element samplingTime = (Element) parent.getElementsByTagNameNS(OM_NS, SAMPLING_TIME).item(0);
        Element timePeriod = createElementNS(GML_NS, TIME_PERIOD);
        timePeriod.setAttribute("gml:id", "DATA_TIME");
        timePeriod.appendChild(createNodeWithText(GML_NS, BEGIN_POSITION, obsHandler.getStartTime(index)));
        timePeriod.appendChild(createNodeWithText(GML_NS, END_POSITION, obsHandler.getEndTime(index)));
        samplingTime.appendChild(timePeriod);
        // procedure
        parent.appendChild(createNodeWithAttribute(OM_NS, PROCEDURE, XLINK, procName));
        // add each of the observed properties we are looking for
        for (String obs : obsHandler.getObservedProperties()) {
            // don't add height/depth vars; lat & lon
            if (!obs.equalsIgnoreCase("alt") && !obs.equalsIgnoreCase("height") && !obs.equalsIgnoreCase("z") &&
                !obs.equalsIgnoreCase("lat") && !obs.equalsIgnoreCase("lon"))
                parent.appendChild(createNodeWithAttribute(OM_NS, OBSERVED_PROPERTY, XLINK, obs));
        }
        // feature of interest
        parent.appendChild(createNodeWithAttribute(OM_NS, FEATURE_INTEREST, XLINK, procName));
    }
    
    private String getSRSName() {
        if (obsHandler.getCRSSRSAuthorities() != null) {
            _log.debug(obsHandler.getCRSSRSAuthorities()[0]);
            return obsHandler.getCRSSRSAuthorities()[0];
        } else {
            return "http://www.opengis.net/def/crs/EPSG/0/4326";
        }
    }
    
    private Element createNodeWithText(String elemNs,String elemName,  String text) {
        Element retval = createElementNS(elemNs,elemName);
        retval.setTextContent(text);
        return retval;
    }
    
    private Element createNodeWithAttribute(String elemNs,String elemName,  String attrName, String attrValue) {
        Element retval = createElementNS(elemNs, elemName);
        retval.setAttribute(attrName, attrValue);
        return retval;
    }
    
    private Element createField(String name, String code) {
        return createField(name, code, null);
    }
    
    private Element createField(String name, String code, String fillValue) {
        Element retval = createElementNS(SWE_NS, FIELD);
        retval.setAttribute("name", name);
        Element quantity =createElementNS(SWE_NS, QUANTITY);
        String definition = MMI_CF + name;
        quantity.setAttribute(DEFINITION, definition);        
        if (fillValue != null) {
            Element nilValues = createElementNS(SWE_NS, "nilValues");
            Element filValues = createNodeWithAttribute(SWE_NS, "nilValue", "reason", "http://www.opengis.net/def/nil/OGC/0/missing");
            filValues.setTextContent(fillValue);
            nilValues.appendChild(filValues);
            quantity.appendChild(nilValues);
        }
        quantity.appendChild(createNodeWithAttribute(SWE_NS, UOM, CODE, code));
        retval.appendChild(quantity);
        return retval;
    }
    
    private Element createTimeField(String name, String def) {
        Element retval = createNodeWithAttribute(SWE_NS, FIELD, NAME, name);
        Element time = createNodeWithAttribute(SWE_NS, TIME, DEFINITION, "http://www.opengis.net/def/property/OGC/0/SamplingTime");
        time.appendChild(createNodeWithAttribute(SWE_NS, UOM, XLINK, "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian"));
        retval.appendChild(time);
        return retval;
    }
    
    private Element getEncodingElement() {
        Element retval = createElementNS(SWE_NS, "encoding");
        Element txtBlock = createNodeWithAttribute(SWE_NS, "TextBlock", "blockSeparator", BLOCK_SEPERATOR);
        txtBlock.setAttribute("decimalSeparator", DECIMAL_SEPERATOR);
        txtBlock.setAttribute("tokenSeparator", TOKEN_SEPERATOR);
        retval.appendChild(txtBlock);
        return retval;
    }

    private org.w3c.dom.Node getResultNode(int index) {
        Element parent = createElementNS(OM_NS, "result");
        
        Element dataArray = createElementNS(SWE_NS, DATA_ARRAY);
        
        Element elemCount = createElementNS(SWE_NS, ELEMENT_COUNT);
        Element count = createElementNS(SWE_NS, COUNT);
        count.appendChild(createNodeWithText(SWE_NS, SML_VALUE, "" + obsHandler.getObservedProperties().length));
        elemCount.appendChild(count);
        dataArray.appendChild(elemCount);
        
        Element elemType = createElementNS(SWE_NS, "elementType");
        elemType.setAttribute(NAME, "SimpleDataArray");
        Element dataRecord = createElementNS(SWE_NS, DATA_RECORD);
        boolean timeSet = false;
        Element timeField = null;
        ArrayList<Element> opFields = new ArrayList<Element>();
        for (String obsProp: obsHandler.getObservedProperties()) {
            if (obsProp.toLowerCase().contains(TIME) && !timeSet) {
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
        dataArray.appendChild(createNodeWithText(SWE_NS,"values", 
        		processDataBlock(obsHandler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, index))));
        
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
        NodeList col1 = document.getElementsByTagNameNS(OM_NS, "ObservationCollection");
        Element coll = (Element) col1.item(0);
        coll.getElementsByTagNameNS(GML_NS, NAME).item(0).setTextContent(obsHandler.getTitle());
        coll.getElementsByTagNameNS(GML_NS, DESCRIPTION).item(0).setTextContent(obsHandler.getDescription());
        
        Element bounds = (Element) coll.getElementsByTagNameNS(GML_NS, BOUNDED_BY).item(0);
        ((Element)bounds.getElementsByTagNameNS(GML_NS, ENVELOPE).item(0)).setAttribute(SRS_NAME, getSRSName());
        bounds.getElementsByTagNameNS(GML_NS, LOWER_CORNER).item(0).setTextContent(obsHandler.getBoundedLowerCorner());
        bounds.getElementsByTagNameNS(GML_NS, UPPER_CORNER).item(0).setTextContent(obsHandler.getBoundedUpperCorner());
    }
}
