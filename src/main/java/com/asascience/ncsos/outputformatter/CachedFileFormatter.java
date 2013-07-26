/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;
import java.io.File;
import java.io.Writer;
import java.util.BitSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 *
 * @author SCowan
 */
public class CachedFileFormatter extends SOSOutputFormatter {
    
    private Document document;
    private String errorString;
    private boolean exceptionFlag;
    private DOMImplementationLS impl;
    
    private enum Sections {
        OPERATIONSMETADATA, SERVICEIDENTIFICATION, SERVICEPROVIDER, CONTENTS
    }
    private static final int SECTION_COUNT = 4;
    private BitSet requestedSections;
    
    private final static String capabilitiesElement = "Capabilities";
    
    public CachedFileFormatter(File fileToRead) {
        errorString = null;
        exceptionFlag = false;
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(fileToRead);
        } catch (Exception ex) {
            ex.printStackTrace();
            setupExceptionOutput("Could not read Cached file: " + fileToRead.getAbsolutePath());
        }
        
        requestedSections = new BitSet(SECTION_COUNT);
    }
    
    public void setSections(String sections) {
        String[] sectionSplit = sections.split(",");
        
        for (String sect : sectionSplit) {
            if (sect.equals("all")) {
                requestedSections.set(0, SECTION_COUNT);
            } else {
                requestedSections.set(Sections.valueOf(sect.toUpperCase()).ordinal());
            }
        }
        
        Element capsNode = (Element) document.getElementsByTagNameNS(SOS_NS, capabilitiesElement).item(0);
        
        if (!this.requestedSections.get(Sections.SERVICEIDENTIFICATION.ordinal())) {
            Node node = capsNode.getElementsByTagNameNS(OWS_NS,"ServiceIdentification").item(0);
            capsNode.removeChild(node);
        }
        if (!this.requestedSections.get(Sections.SERVICEPROVIDER.ordinal())) {
            Node node = capsNode.getElementsByTagNameNS(OWS_NS, "ServiceProvider").item(0);
            capsNode.removeChild(node);
        }
        if (!this.requestedSections.get(Sections.OPERATIONSMETADATA.ordinal())) {
            Node node = capsNode.getElementsByTagNameNS(OWS_NS, "OperationsMetadata").item(0);
            capsNode.removeChild(node);
        }
        if (!this.requestedSections.get(Sections.CONTENTS.ordinal())) {
            Node node = capsNode.getElementsByTagNameNS(SOS_NS, "Contents").item(0);
            capsNode.removeChild(node);
        }
    }

    /***************************************************************************
     *   Interface Methods    **************************************************
     **************************************************************************/
    
    public void addDataFormattedStringToInfoList(String dataFormattedString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void emtpyInfoList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setupExceptionOutput(String message) {
        document = XMLDomUtils.getExceptionDom(message);
        exceptionFlag = true;
    }

    public void writeOutput(Writer writer) {
        // output our document to the writer
        LSSerializer xmlSerializer = impl.createLSSerializer();
        LSOutput xmlOut = impl.createLSOutput();
        xmlSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        xmlOut.setCharacterStream(writer);
        xmlSerializer.write(document, xmlOut);
    }
    
}
