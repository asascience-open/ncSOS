package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.util.XMLDomUtils;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.Writer;

public class BaseOutputFormatter extends OutputFormatter {

    protected boolean hasError;

    public BaseOutputFormatter() {
        super();
    }

    protected String getTemplateLocation() {
        return "";
    }

    public void addDataFormattedStringToInfoList(String dataFormattedString) {
    }

    protected void setupException(String message) {
        ErrorFormatter ef = new ErrorFormatter();
        ef.setException(message);
        this.document = ef.document;
    }
    protected void setupException(String message, String exceptionCode) {
        ErrorFormatter ef = new ErrorFormatter();
        ef.setException(message, exceptionCode);
        this.document = ef.document;
    }
    protected void setupException(String message, String exceptionCode, String locator) {
        ErrorFormatter ef = new ErrorFormatter();
        ef.setException(message, exceptionCode, locator);
        this.document = ef.document;
    }

    public String getContentType() {
        return "text/xml";
    }
    
    public void writeOutput(Writer writer) throws IOException {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(this.document, writer);
    }

    public void setBoundedBy(String srsName, String lowerCorner, String upperCorner) {
        /*
         * <gml:boundedBy>
         *   <gml:Envelope srsName='srsName'>
         *     <gml:lowerCorner>'lowerCorner'</gml:lowerCorner>
         *     <gml:upperCorner>'upperCorner'</gml:upperCorner>
         *   </gml:Envelope>
         * </gml:boundedBy>
         */
        Namespace gmlns = this.getNamespace("gml");
        Element bb = XMLDomUtils.getNestedChild(this.getRoot(), "boundedBy", gmlns);
        Element env = new Element("Envelope", gmlns);
        env.setAttribute("srsName", srsName);
        env.addContent(new Element("lowerCorner", gmlns).setText(lowerCorner));
        env.addContent(new Element("upperCorner", gmlns).setText(upperCorner));
        bb.addContent(env);
    }

    protected Element addNewNode(Element parent, String nodeName, Namespace nodeNS) {
        Element child = new Element(nodeName, nodeNS);
        parent.addContent(new Element(nodeName, nodeNS));
        return child;
    }

    protected Element addNewNode(Element parent,
            String nodeName,
            Namespace nodeNS,
            String textValue) {
        Element child = new Element(nodeName, nodeNS);
        child.setText(textValue);
        parent.addContent(child);
        return child;
    }

    protected Element addNewNode(Element parent,
            String nodeName,
            Namespace nodeNS,
            String attrName,
            String attrValue) {
        return addNewNode(parent, nodeName, nodeNS, attrName, null, attrValue);
    }

    protected Element addNewNode(Element parent,
            String nodeName,
            Namespace nodeNS,
            String attrName,
            Namespace attrNS,
            String attrValue) {
        Element child = new Element(nodeName, nodeNS);
        if (attrNS == null) {
            child.setAttribute(attrName, attrValue);
        } else {
            child.setAttribute(attrName, attrValue, attrNS);
        }
        parent.addContent(child);
        return child;
    }

    protected Element addNewNode(String parentName,
                                 Namespace parentNS,
                                 String nodeName,
                                 Namespace nodeNS,
                                 String attrName,
                                 String attrValue) {
        Element parent = XMLDomUtils.getNestedChild(this.getRoot(), parentName, parentNS);
        return this.addNewNode(parent, nodeName, nodeNS, attrName, attrValue);
    }

    protected Element addNewNode(String parentName,
                                 Namespace parentNS,
                                 String nodeName,
                                 Namespace nodeNS) {
        Element parent = XMLDomUtils.getNestedChild(this.getRoot(), parentName, parentNS);
        return this.addNewNode(parent, nodeName, nodeNS);
    }
}
