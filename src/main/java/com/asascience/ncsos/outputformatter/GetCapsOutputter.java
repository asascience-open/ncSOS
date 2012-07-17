/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 *
 * @author scowan
 */
public class GetCapsOutputter implements SOSOutputFormatter {
    
    private Document document;
    private DOMImplementationLS impl;
    
    private final static String TEMPLATE = "templates/sosGetCapabilities.xml";

    /**
     * Creates instance of a Get Capabilities outputter. Reads the sosGetCapabilities.xml
     * file as a template for the response.
     */
    public GetCapsOutputter() {
        document = parseTemplateXML();
        
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (InstantiationException ex) {
            System.out.println(ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println(ex.getMessage());
        } catch (ClassCastException ex) {
            System.out.println(ex.getMessage());
        }
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

    public void addDataFormattedStringToInfoList(String dataFormattedString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void emtpyInfoList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setupExceptionOutput(String message) {
        document = XMLDomUtils.getExceptionDom(message);
    }

    public void writeOutput(Writer writer) {
        // output our document to the writer
        LSSerializer xmlSerializer = impl.createLSSerializer();
        LSOutput xmlOut = impl.createLSOutput();
        xmlSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        xmlOut.setCharacterStream(writer);
        xmlSerializer.write(document, xmlOut);
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
