/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author scowan
 */
public class GetCapsOutputter implements SOSOutputFormatter {
    
    private Document document;
    
    private final static String TEMPLATE = "templates/sosGetCapabilities.xml";

    /**
     * Creates instance of a Get Capabilities outputter. Reads the sosGetCapabilities.xml
     * file as a template for the response.
     */
    public GetCapsOutputter() {
        document = parseTemplateXML();
    }
    
    /**
     * Gets the XML documents being help by the outputter.
     * @return XML document based on the sosGetCapabilities template
     */
    public Document getDocument() {
        return document;
    }
    
    /**
     * Sets the output XML document
     * @param setter a org.w3c.dom.Document
     */
    public void setDocument(Document setter) {
        this.document = setter;
    }
    
    /*********************/
    /* Interface Methods */
    /**************************************************************************/

    public void AddDataFormattedStringToInfoList(String dataFormattedString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void EmtpyInfoList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setupExceptionOutput(String message) {
        document = XMLDomUtils.getExceptionDom(message);
    }

    public void writeOutput(Writer writer) {
        // output our document to the writer
        DOMSource domSource = new DOMSource(document);
        Result result = new StreamResult(writer);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(domSource, result);
        } catch (Exception e) {
            System.out.println("Error in writing GetCapsOutputter - " + e.getMessage());
        }
    }
    /**************************************************************************/
    
    
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
    
    private String getTemplateLocation() {
        return TEMPLATE;
    }
    
}
