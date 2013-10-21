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
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
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
        try {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(this.document, writer);
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
        
        setCollectionMetadata();

        Namespace om_ns    = this.getNamespace("om");
        Namespace gml_ns   = this.getNamespace("gml");

        for (int i=0; i<CDMDataset.getNumberOfStations(); i++) {
            document = XMLDomUtils.addObservationElement(document, MEMBER, om_ns, OBSERVATION, om_ns);
            // add description, name and bounded by
            document = XMLDomUtils.addNode(document, OBSERVATION, om_ns,
            								DESCRIPTION, gml_ns, description, i);
            document = XMLDomUtils.addNode(document, OBSERVATION, om_ns,
            								NAME, gml_ns, title, i);
        }
    }

    private void setCollectionMetadata() {

        Namespace om_ns    = this.getNamespace("om");
        Namespace gml_ns   = this.getNamespace("gml");
        Namespace xlink_ns = this.getNamespace("xlink");
        
        // set metadata from generic metadata, um for now just use some place holder
        document = XMLDomUtils.addNode(document, OBSERVATION_COLLECTION, om_ns,
        		META_DATA_PROP, gml_ns, MEMBER, om_ns);
        document =XMLDomUtils.setNodeAttribute(document, META_DATA_PROP, gml_ns,
        									   TITLE, xlink_ns, "disclaimer");
        document = XMLDomUtils.addNode(document, 
        							 META_DATA_PROP, gml_ns,
        							 GENERIC_META_DATA, gml_ns, 0);
        document = XMLDomUtils.addNode(document,  GENERIC_META_DATA, gml_ns,
        							  DESCRIPTION, gml_ns, "DISCLAIMER", 0);
    }

}
