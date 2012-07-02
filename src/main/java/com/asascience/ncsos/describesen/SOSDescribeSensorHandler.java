/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSDescribeSensorHandler extends SOSBaseRequestHandler {
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSDescribeSensorHandler.class);
    private String procedure;
    private String description;
    private Variable procedureVar;
    private Variable stationVar;
    
    private final String ACCEPTABLE_RESPONSE_FORMAT = "text/xml;subtype=\"sensorML/1.0.1\"";
    
    /**
     * Creates a DescribeSensor handler that will parse the information and setup the output handle
     * @param dataset
     * @param responseFormat
     * @param procedure
     * @throws IOException 
     */
    public SOSDescribeSensorHandler(NetcdfDataset dataset, String responseFormat, String procedure) throws IOException {
        super(dataset);
        
        output = new DescribeSensorFormatter();
        
        // make sure that the responseFormat we recieved is acceptable
        if (!responseFormat.equalsIgnoreCase(ACCEPTABLE_RESPONSE_FORMAT)) {
            // return exception
            output.setupExceptionOutput("Unhandled response format " + responseFormat);
        } else {
            this.procedure = procedure;
        }
        
        description = dataset.findAttValueIgnoreCase(null, "description", "empty");
        String[] tmpArray = procedure.split(":");
        System.out.println("variables break down:");
        for (Variable var : dataset.getVariables()) {
            System.out.println(var.getNameAndDimensions());
        }
        
        procedureVar = dataset.findVariable(tmpArray[tmpArray.length - 1]);
        
        if (procedureVar != null) {
            System.out.println("Found procedure variable; attributes:");
            for (Attribute attr : procedureVar.getAttributes()) {
                System.out.println(attr.getName());
            }
        } else {
            System.out.println("Couldn't find procedure variable");
        }
        
        // setup our xml doc for writing
        setupDescribeSensorDoc();
    }

    /**
     * Exception version, used to create skeleton SOSDescribeSensorHandler that can throw an exception
     * @param dataset dataset, mostly unused
     * @throws IOException 
     */
    public SOSDescribeSensorHandler(NetcdfDataset dataset) throws IOException {
        super(dataset);
    }
    
    /*****************************
     * XML Node Setter Functions *
     *****************************/
    
    private Document getDocument() {
        return ((DescribeSensorFormatter)output).getDocument();
    }
    
    private void setupDescribeSensorDoc() {
        Document doc = getDocument();
        
        // call each of our setter functions
        setDescription(doc);
        setIdentification(doc);
        setClassification(doc);
        setContact(doc);
        setDocumentation(doc);
        setHistory(doc);
        setLocation(doc);
        setPosition(doc);
        setTimePosition(doc);
        setComponents(doc);
    }
    
    private void SetNodeValue(Document doc, String container, String nodeName, String value ) {
        NodeList nodeList = doc.getElementsByTagName(container);
        Node fNode = nodeList.item(0);
        nodeList = ((Element) fNode).getElementsByTagName(nodeName);
        Element node = (Element) nodeList.item(0);
        node.setTextContent(value);
    }
    
    private void setDescription(Document document) {
        // set out description
        SetNodeValue(document, "System", "gml:description", description);
    }
    
    private void setIdentification(Document document) {
        // use station var to print out all of its attributes as part of the identification
    }
    
    private void setClassification(Document document) {
        
    }
    
    private void setContact(Document document) {
        
    }
    
    private void setDocumentation(Document document) {
        
    }
    
    private void setHistory(Document document) {
        
    }
    
    private void setLocation(Document document) {
        
    }
    
    private void setPosition(Document document) {
        
    }
    
    private void setTimePosition(Document document) {
        
    }
    
    private void setComponents(Document document) {
        
    }
}
