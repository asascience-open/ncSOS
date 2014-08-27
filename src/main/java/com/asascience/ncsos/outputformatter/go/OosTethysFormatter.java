package com.asascience.ncsos.outputformatter.go;

import com.asascience.ncsos.go.GetObservationRequestHandler;
import com.asascience.ncsos.outputformatter.BaseOutputFormatter;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class OosTethysFormatter extends BaseOutputFormatter {

    private static final String TEMPLATE = "templates/GO_oostethys.xml";
    private static final String OBSERVATION = "Observation";
    private static final String BLOCK_SEPERATOR = " ";
    private static final String TOKEN_SEPERATOR = ",";
    private static final String DECIMAL_SEPERATOR = ".";
    private GetObservationRequestHandler handler = null;
    private Namespace OM_NS, GML_NS, SWE_NS, XLINK_NS = null;
    
    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(OosTethysFormatter.class);
    
    public OosTethysFormatter(GetObservationRequestHandler obsHandler) {
        super();
        this.OM_NS    = this.getNamespace("om");
        this.GML_NS   = this.getNamespace("gml");
        this.XLINK_NS = this.getNamespace("xlink");
        this.SWE_NS   = this.getNamespace("swe");

        if (obsHandler == null) {
            this.hasError = true;
            this.setupException("Unable to create observation collection - missing or invalid info.");
        } else {
            this.handler = obsHandler;
        }
    }

    public String getTemplateLocation() {
        return TEMPLATE;
    }

    public void writeOutput(Writer writer) throws IOException {
        // create output if we don't already have an exception
        if (!hasError) {
            parseObservations(this.handler.getProcedures());
        }
        super.writeOutput(writer);
    }

    private void parseObservations(String[] procedures) {
        _log.debug(procedures.length + " procedures");
        // set the station observation name, desc and bounds
        setCollectionInfo();
        // iterate through the requested stations
        for (int index=0; index < procedures.length; index++) {
            String proc = procedures[index];
            Element parent = addNewObservation();
            setObservationMeta(parent, proc, index);
            parent.addContent(getResultElement(index));
        }
    }
    
    
    private Element addNewObservation() {
        Element member = (Element) this.getRoot().getChild(MEMBER, this.OM_NS);
        Element observation = new Element(OBSERVATION, OM_NS);

        Element parent = new Element(DESCRIPTION, GML_NS);
        observation.addContent(parent);
        parent = new Element(NAME, GML_NS);
        observation.addContent(parent);
        parent = new Element(BOUNDED_BY, GML_NS);
        observation.addContent(parent);
        parent = new Element(SAMPLING_TIME, OM_NS);
        observation.addContent(parent);
        
        member.addContent(observation);
        
        return observation;
    }
    
    private void setObservationMeta(Element parent, String procName, int index) {
        // set description and name
//        parent.getElementsByTagName("gml:description").item(0).setTextContent("");
//        parent.getElementsByTagName("name").item(0).setTextContent("");
        // set bounded by
        Element bounds = (Element) parent.getChild(BOUNDED_BY, GML_NS);
        Element envelope = new Element(ENVELOPE, GML_NS);
        envelope.setAttribute(SRS_NAME, handler.getCrsName());
        envelope.addContent(createNodeWithText(GML_NS, LOWER_CORNER, this.handler.getStationLowerCorner(index)));
        envelope.addContent(createNodeWithText(GML_NS, UPPER_CORNER, this.handler.getStationUpperCorner(index)));
        bounds.addContent(envelope);
        // sampling time
        Element samplingTime = (Element) parent.getChild(SAMPLING_TIME, OM_NS);
        Element timePeriod = new Element(TIME_PERIOD, GML_NS);
        timePeriod.setAttribute("id", "DATA_TIME", GML_NS);
        timePeriod.addContent(createNodeWithText(GML_NS, BEGIN_POSITION, this.handler.getStartTime(index)));
        timePeriod.addContent(createNodeWithText(GML_NS, END_POSITION, this.handler.getEndTime(index)));
        samplingTime.addContent(timePeriod);
        // procedure
        parent.addContent(createNodeWithAttribute(OM_NS, PROCEDURE, XLINK_NS, "href", procName));
        // add each of the observed properties we are looking for
        for (String obs : this.handler.getObservedProperties()) {
            // don't add height/depth vars; lat & lon
            if (!obs.equalsIgnoreCase("alt") && !obs.equalsIgnoreCase("height") && !obs.equalsIgnoreCase("z") &&
                !obs.equalsIgnoreCase("lat") && !obs.equalsIgnoreCase("lon"))
                parent.addContent(createNodeWithAttribute(OM_NS, OBSERVED_PROPERTY, XLINK_NS, "href", obs));
        }
        // feature of interest
        parent.addContent(createNodeWithAttribute(OM_NS, FEATURE_INTEREST, XLINK_NS, "href", procName));
    }
    

    private Element createNodeWithText(Namespace elemNs, String elemName, String text) {
        Element retval = new Element(elemName, elemNs);
        retval.setText(text);
        return retval;
    }
    
    private Element createNodeWithAttribute(Namespace elemNs, String elemName, Namespace attrNs, String attrName, String attrValue) {
        Element retval = new Element(elemName, elemNs);
        retval.setAttribute(attrName, attrValue, attrNs);
        return retval;
    }

    private Element createNodeWithAttribute(Namespace elemNs, String elemName, String attrName, String attrValue) {
        Element retval = new Element(elemName, elemNs);
        retval.setAttribute(attrName, attrValue);
        return retval;
    }

    private Element createField(String name, String code) {
        return createField(name, code, null);
    }
    
    private Element createField(String name, String code, String fillValue) {
        Element retval = new Element(FIELD, SWE_NS);
        retval.setAttribute("name", name);
        Element quantity = new Element(QUANTITY, SWE_NS);
        String definition = this.handler.getObservedOfferingUrl(name);
        quantity.setAttribute(DEFINITION, definition);        
        if (fillValue != null) {
            Element nilValues = new Element("nilValues", SWE_NS);
            Element filValues = createNodeWithAttribute(SWE_NS, "nilValue", "reason", "http://www.opengis.net/def/nil/OGC/0/missing");
            filValues.setText(fillValue);
            nilValues.addContent(filValues);
            quantity.addContent(nilValues);
        }
        quantity.addContent(createNodeWithAttribute(SWE_NS, UOM, CODE, code));
        retval.addContent(quantity);
        return retval;
    }
    
    private Element createTimeField(String name, String def) {
        Element retval = createNodeWithAttribute(SWE_NS, FIELD, NAME, name);
        Element time = createNodeWithAttribute(SWE_NS, "Time", DEFINITION, "http://www.opengis.net/def/property/OGC/0/SamplingTime");
        time.addContent(createNodeWithAttribute(SWE_NS, UOM, XLINK_NS, "href", "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian"));
        retval.addContent(time);
        return retval;
    }
    
    private Element getEncodingElement() {
        Element retval = new Element("encoding", SWE_NS);
        Element txtBlock = createNodeWithAttribute(SWE_NS, "TextBlock", "blockSeparator", BLOCK_SEPERATOR);
        txtBlock.setAttribute("decimalSeparator", DECIMAL_SEPERATOR);
        txtBlock.setAttribute("tokenSeparator", TOKEN_SEPERATOR);
        retval.addContent(txtBlock);
        return retval;
    }

    private Element getResultElement(int index) {
        Element parent = new Element("result", OM_NS);
        
        Element dataArray = new Element(DATA_ARRAY, OM_NS);
        
        Element elemCount = new Element(ELEMENT_COUNT, SWE_NS);
        Element count = new Element(COUNT, SWE_NS);
        count.addContent(createNodeWithText(SWE_NS, SML_VALUE, "" + this.handler.getObservedProperties().length));
        elemCount.addContent(count);
        dataArray.addContent(elemCount);
        
        Element elemType = new Element("elementType", SWE_NS);
        elemType.setAttribute(NAME, "SimpleDataArray");
        Element dataRecord = new Element(DATA_RECORD, SWE_NS);
        boolean timeSet = false;
        Element timeField = null;
        ArrayList<Element> opFields = new ArrayList<Element>();
        for (String obsProp: this.handler.getObservedProperties()) {
            if (obsProp.toLowerCase().contains(TIME) && !timeSet) {
                timeField = createTimeField(obsProp, "iso8601");
                timeSet = true;
            } else {
                // need to set the data record for each observed property which requires the name and source (both of which can be retrieved from the observed property)
                // and the units the measurement is taken in
                // source (namespace) is the value before parameter, name is the last value in the split
                if (this.handler.hasFillValue(obsProp)) {
                    opFields.add(createField(obsProp, this.handler.getUnitsString(obsProp), this.handler.getFillValue(obsProp)));
                } else {
                    opFields.add(createField(obsProp, this.handler.getUnitsString(obsProp)));
                }
            }
        }
        if (!timeSet) {
            timeField = createTimeField("time", "iso8601");
        }
        dataRecord.addContent(timeField);
        for (Element field : opFields) {
            dataRecord.addContent(field);
        }
        elemType.addContent(dataRecord);
        dataArray.addContent(elemType);
        
        dataArray.addContent(getEncodingElement());
        
//        dataArray.appendChild(createNodeWithText("swe:values", obsHandler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, index)));
        dataArray.addContent(createNodeWithText(SWE_NS, "values",
                processDataBlock(this.handler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, index))));
        
        parent.addContent(dataArray);
        
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
        for (String obsProp : this.handler.getObservedProperties()) {
            if (obsProp.equals(name))
                return true;
        }
        return false;
    }

    private void setCollectionInfo() {
        Element coll = this.getRoot();
        coll.getChild(NAME, GML_NS).setText((String)this.handler.getGlobalAttribute("title", "No 'title' global attribute"));
        coll.getChild(DESCRIPTION, GML_NS).setText((String)this.handler.getGlobalAttribute("summary", "No 'summary' global attribute"));
        
        Element bounds = (Element) coll.getChild(BOUNDED_BY, GML_NS);
        Element env = bounds.getChild(ENVELOPE, GML_NS);
        env.setAttribute(SRS_NAME, handler.getCrsName());
        env.getChild(LOWER_CORNER, GML_NS).setText(this.handler.getBoundedLowerCorner());
        env.getChild(UPPER_CORNER, GML_NS).setText(this.handler.getBoundedUpperCorner());
    }
}
