package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.go.GetObservationRequestHandler;
import com.asascience.ncsos.service.BaseRequestHandler;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Namespace;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class BaseOutputFormatter extends OutputFormatter {

    protected class SubElement {

        public HashMap<String, String> attributes;
        public String tag;
        public String tagNS;
        public String textContent;

        public SubElement(String tag, String tagNS) {
            this.attributes = new HashMap<String, String>();
            this.textContent = null;
            this.tag = tag;
            this.tagNS = tagNS;
        }
    }
    protected BaseRequestHandler handler = null;
    protected boolean hasError;
    protected String DEFAULT_VALUE = "UNKNOWN";
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BaseOutputFormatter.class);

    public BaseOutputFormatter() {
        super();
    }

    public void addDataFormattedStringToInfoList(String dataFormattedString) {
    }

    public void setupExceptionOutput(String message) {
        this.document = XMLDomUtils.getExceptionDom(message);
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
        Element bb = this.getRoot().getChild("boundedBy", gmlns);
        Element env = new Element("Envelope", gmlns);
        env.setAttribute("srsName", srsName);
        env.getChild("lowerCorner", gmlns).setText(lowerCorner);
        env.getChild("upperCorner", gmlns).setText(upperCorner);
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
        Element parent = this.getRoot().getChild(parentName, parentNS);
        return this.addNewNode(this.getRoot(), nodeName, nodeNS);
    }
}
