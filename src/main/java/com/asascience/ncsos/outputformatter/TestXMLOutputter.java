/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.cdmclasses.iStationData;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author scowan
 */
public class TestXMLOutputter extends OutputFormatter {

    private ArrayList<DataSlice> infoList;
    private iStationData CDMDataset;
    // observation metadata strings
    private String title, history, institution, source, description, location, featureOfInterest;
    
    private final String NAN = "NaN";
    private final String TEMPLATE = "templates/testxmloutputter.xml";
    
    /**
     * Example class for creating a new outputter for observation requests. Mostly
     * follows the text/xml;subtype="om/1.0.0" standard.
     * @param variableNames observedProperties from the request.
     * @param cdmDataset cdmdataset from the base request handler
     * @param netcdfDataset dataset being polled
     */
    public TestXMLOutputter(String[] variableNames,
            iStationData cdmDataset,
            NetcdfDataset netcdfDataset) {
        this.CDMDataset = cdmDataset;
        description = title = history = institution = source = location = featureOfInterest = "empty";
    }
    
    /*********************
     * Interface Methods *
     *********************/
    public void addDataFormattedStringToInfoList(String dataFormattedString) {
        if (infoList == null) {
            infoList = new ArrayList<DataSlice>();
        }
        
        //parse the formatted string
    }

    public void emtpyInfoList() {
        infoList = null;
    }

    /**
     * sets the xml doc to an exception output
     * @param message 
     */
    public void setupExceptionOutput(String message) {
        document = XMLDomUtils.getExceptionDom(message);
    }

    /**
     * writes out xml to the writer
     * @param writer 
     */
    public void writeOutput(Writer writer) {
        parseOuput();
        
        DOMSource domSource = new DOMSource(document);
        Result result = new StreamResult(writer);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(domSource, result);
        } catch (Exception e) {
            System.out.println("Error in writing TestXMLOutputter - " + e.getMessage());
        }
    }
    /********************************************************************************/
    
    private Document parseTemplateXML() {
        InputStream templateInputStream = null;
        try {
            templateInputStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE);
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
    
    private void parseOuput() {
        // sets up the xml doc for output to the stream.
        if (CDMDataset == null) {
            setupExceptionOutput("null dataset");
            return;
        }
        
        document = parseTemplateXML();
        initNamespaces();

        
        setCollectionMetadata();
        
        for (int i=0; i<CDMDataset.getNumberOfStations(); i++) {
            document = XMLDomUtils.addObservationElement(document, MEMBER, OM_NS, OBSERVATION, OM_NS);
            // add description, name and bounded by
            document = XMLDomUtils.addNode(document, OBSERVATION, OM_NS,
            								DESCRIPTION, GML_NS, description, i);
            document = XMLDomUtils.addNode(document, OBSERVATION, OM_NS,
            								NAME, GML_NS, title, i);
        }
    }

    private void setCollectionMetadata() {
        System.out.println("setCollectionMetadata");
        // set out collection header
//        setSystemGMLID();
        
        // set metadata from generic metadata, um for now just use some place holder
        document = XMLDomUtils.addNode(document, OBSERVATION_COLLECTION, OM_NS,
        		META_DATA_PROP, GML_NS, MEMBER, OM_NS);
        document =XMLDomUtils.setNodeAttribute(document, META_DATA_PROP, GML_NS, 
        									   TITLE, XLINK_NS, "disclaimer");
        document = XMLDomUtils.addNode(document, 
        							 META_DATA_PROP, GML_NS, 
        							 GENERIC_META_DATA, GML_NS, 0);
        document = XMLDomUtils.addNode(document,  GENERIC_META_DATA, GML_NS,
        							  DESCRIPTION, GML_NS, "DISCLAIMER", 0);
    }
    
    private void setSystemGMLID() {

        StringBuilder b = new StringBuilder();
        if (CDMDataset != null) {

            for (int i = 0; i < CDMDataset.getNumberOfStations(); i++) {
                b.append(CDMDataset.getStationName(i));
                b.append(",");
            }
        }
        // so below is odd, the 'getGMLID' function returns the string that is passed into it. I am assuming that there needs
        // to be more to it than that, so leaving this in commented out until i can affirm what this should be
//        XMLDomUtils.setObsGMLIDAttributeFromNode(document, "om:ObservationCollection", "gml:id", getGMLID("GML_ID_NAME"));
        // meantime place-holder
        XMLDomUtils.setObsGMLIDAttributeFromNode(document, 
        			OBSERVATION_COLLECTION, OM_NS,
        			ID, GML_NS, "GML_ID_NAME");
    }
}
