/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.BitSet;

/**
 *
 * @author SCowan
 */
public class CachedFileFormatter extends XmlOutputFormatter {
    
    private Document document;
    private String errorString;
    private boolean exceptionFlag;
    
    private enum Sections {
        OPERATIONSMETADATA, SERVICEIDENTIFICATION, SERVICEPROVIDER, CONTENTS
    }
    private static final int SECTION_COUNT = 4;
    private BitSet requestedSections;
    private String TEMPLATE;
    private String fileToRead;
    
    private final static String capabilitiesElement = "Capabilities";
    
    public CachedFileFormatter(File fileToRead) {
        this.fileToRead = fileToRead.getAbsolutePath();
        this.document = XMLDomUtils.loadFile(getClass().getClassLoader().getResourceAsStream(this.fileToRead));
        this.initNamespaces();
        requestedSections = new BitSet(SECTION_COUNT);
    }

    protected String getTemplateLocation() {
        return this.fileToRead;
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

        Namespace sosns = Namespace.getNamespace("sos");
        Namespace owsns = Namespace.getNamespace("ows");
        Element capsNode = this.getRoot().getChild(capabilitiesElement, sosns);

        if (!this.requestedSections.get(Sections.SERVICEIDENTIFICATION.ordinal())) {
            capsNode.removeContent(capsNode.getChild("ServiceIdentification", owsns));
        }
        if (!this.requestedSections.get(Sections.SERVICEPROVIDER.ordinal())) {
            capsNode.removeContent(capsNode.getChild("ServiceProvider", owsns));
        }
        if (!this.requestedSections.get(Sections.OPERATIONSMETADATA.ordinal())) {
            capsNode.removeContent(capsNode.getChild("OperationsMetadata", owsns));
        }
        if (!this.requestedSections.get(Sections.CONTENTS.ordinal())) {
            capsNode.removeContent(capsNode.getChild("Contents", sosns));
        }
    }

    /***************************************************************************
     *   Interface Methods    **************************************************
     **************************************************************************/
    
  

    public void writeOutput(Writer writer) throws IOException {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(this.document, writer);
    }
    
    public String getContentType() {
        return "text/xml";
    }

}
