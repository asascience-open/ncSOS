/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter;

import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.time.CalendarDate;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author scowan
 */
public class GetCapsOutputter implements SOSOutputFormatter {
    
    private Document document;
    private DOMImplementationLS impl;
    private boolean exceptionFlag;
    
    private Element getCaps, getObs, descSen;
    
    private final static String TEMPLATE = "templates/sosGetCapabilities.xml";

    /**
     * Creates instance of a Get Capabilities outputter. Reads the sosGetCapabilities.xml
     * file as a template for the response.
     */
    public GetCapsOutputter() {
        document = parseTemplateXML();
        
        exceptionFlag = false;
        
        getCaps = getObs = descSen = null;
        prepOperationsMetadata();
        
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
    
    /**
     * Returns flag that is set when setupExceptionOutput is called.
     * @return boolean flag: T if there is an exception
     */
    public boolean hasExceptionOut() {
        return exceptionFlag;
    }
    
    /**
     * 
     * @param threddsURI 
     */
    public void setOperationGetCaps(String threddsURI) {
        setOperationMethods((Element)getCaps.getElementsByTagName("ows:HTTP").item(0), threddsURI);
    }
    
    /**
     * 
     * @param threddsURI
     * @param startDate
     * @param endDate
     * @param dataVarShortNames
     * @param stationNames
     * @param gridDataset
     * @param gridBbox 
     */
    public void setOperationGetObs(String threddsURI, CalendarDate startDate, CalendarDate endDate, List<String> dataVarShortNames, String[] stationNames, boolean gridDataset, LatLonRect gridBbox) {
        // set url info
        setOperationMethods((Element)getObs.getElementsByTagName("ows:HTTP").item(0), threddsURI);
        // set info for get observation operation
        // get our parameters that need to be filled
        Element eventtime = null, offering = null, observedproperty = null;
        NodeList nodes = getObs.getElementsByTagName("ows:Parameter");
        for (int i=0; i<nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            if (elem.getAttribute("name").equalsIgnoreCase("offering")) {
                offering = elem;
                continue;
            }
            if (elem.getAttribute("name").equalsIgnoreCase("observedProperty")) {
                observedproperty = elem;
                continue;
            }
            if (elem.getAttribute("name").equalsIgnoreCase("eventTime")) {
                eventtime = elem;
                continue;
            }
        }
        // set eventtime
        if (eventtime != null && endDate != null && startDate != null) {
            Element allowedValues = getDocument().createElement("ows:AllowedValues");
            eventtime.appendChild(allowedValues);
            Element range = getDocument().createElement("ows:Range");
            allowedValues.appendChild(range);
            // min value
            Element min = getDocument().createElement("ows:MinimumValue");
            min.setTextContent(startDate.toString());
            allowedValues.appendChild(min);
            // max value
            Element max = getDocument().createElement("ows:MaximumValue");
            max.setTextContent(endDate.toString());
            allowedValues.appendChild(max);
        }
        // set observedProperty parameter
        if (observedproperty != null) {
            Element allowedValues = getDocument().createElement("ows:AllowedValues");
            observedproperty.appendChild(allowedValues);
            for (String name : dataVarShortNames) {
                Element value = getDocument().createElement("ows:value");
                value.setTextContent(name);
                allowedValues.appendChild(value);
            }
        }
        // set offering parameter - list of station names
        if (offering != null && stationNames != null) {
            Element aV = getDocument().createElement("ows:AllowedValues");
            offering.appendChild(aV);
            for (String name : stationNames) {
                Element value = getDocument().createElement("ows:Value");
                value.setTextContent(name);
                aV.appendChild(value);
            }
        }
        // add lat and lon as parameters if we are a grid dataset
        if (gridDataset)
            addLatLonParameters(getObs, null);
    }
    
    /**
     * 
     * @param threddsURI
     * @param stationNames
     * @param sensorNames 
     */
    public void setOperationDescSen(String threddsURI, String[] stationNames, List<String> sensorNames) {
        // set procedure allowed values of each station and sensor for the dataset
        Element procedure = null;
        NodeList nodes = descSen.getElementsByTagName("ows:Parameter");
        for (int i=0; i<nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            if (elem.getAttribute("name").equalsIgnoreCase("procedure")) {
                procedure = elem;
                break;
            }
        }
        if (procedure != null && stationNames!= null) {
            // add allowed values node
            Element allowedValues = getDocument().createElement("ows:AllowedValues");
            procedure.appendChild(allowedValues);
            for (String stationName : stationNames) {
                Element elem = getDocument().createElement("ows:Value");
                elem.setTextContent(SOSBaseRequestHandler.getGMLName(stationName));
                allowedValues.appendChild(elem);
                for (String senName : sensorNames) {
                    Element sElem = getDocument().createElement("ows:Value");
                    sElem.setTextContent(SOSBaseRequestHandler.getSensorGMLName(stationName, senName));
                    allowedValues.appendChild(sElem);
                }
            }
        }
    }
    
//    public void setObservationOfferingList() {
//        Element offeringList = (Element) getDocument().getElementsByTagName("ObservationOfferingList").item(0);
//        if (getStationNames() != null) {
//            // iterate through offerings (stations)
//            for (String stationName : getStationNames().values()) {
//                Element obsOffering = getDocument().createElement("ObservationOffering");
//                obsOffering.setAttribute("gml:id", stationName);
//                // gml:name
//                Element gmlName = getDocument().createElement("gml:name");
//                gmlName.setTextContent(getGMLName(stationName));
//                obsOffering.appendChild(gmlName);
//                // gml:srsName - default to EPSG:4326 for now
//                Element srsName = getDocument().createElement("gml:srsName");
//                srsName.setTextContent("EPSG:4326");
//                obsOffering.appendChild(srsName);
//                // bounds
//                obsOffering.appendChild(getStationBounds(getStationIndex(stationName)));
//                // add time envelope
//                obsOffering.appendChild(getStationPeriod(getStationIndex(stationName)));
//                // feature of interest -- station name?
//                Element foi = getDocument().createElement("featureOfInterest");
//                foi.setAttribute("xlink:href", stationName);
//                // observed properties
//                for (Iterator<VariableSimpleIF> it = getFeatureDataset().getDataVariables().iterator(); it.hasNext();) {
//                    VariableSimpleIF var = it.next();
//                    Element value = getDocument().createElement("observedProperty");
//                    value.setAttribute("xlink:href", var.getShortName());
//                    obsOffering.appendChild(value);
//                }
//                // procedures
//                for (String str : getSensorNames()) {
//                    Element proc = getDocument().createElement("procedure");
//                    proc.setAttribute("xlink:href", getSensorGMLName(stationName, str));
//                    obsOffering.appendChild(proc);
//                }
//                // response format
//                Element rf = getDocument().createElement("responseFormat");
//                rf.setTextContent("text/xml; subtype=\"om/1.0.0\"");
//                obsOffering.appendChild(rf);
//                // response model/mode -- blank for now?
//                obsOffering.appendChild(getDocument().createElement("responseModel"));
//                obsOffering.appendChild(getDocument().createElement("responseMode"));
//
//                // add offering
//                offeringList.appendChild(obsOffering);
//            }
//        }
//    }
    
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
    
    private void setOperationMethods(Element parent, String threddsURI) {
        //set get capabilities GET request link
        NodeList getList = parent.getElementsByTagName("ows:Get");
        Element getElm = (Element) getList.item(0);
        getElm.setAttribute("xlink:href", threddsURI);

        //set get capabilities Post request link -- not supported TODO
//        NodeList fstNm12 = fstNmElmnt.getElementsByTagName("ows:Post");
//        Element fstNmElmnt12 = (Element) fstNm12.item(0);
//        if (fstNmElmnt12 != null)
//            fstNmElmnt12.setAttribute("xlink:href", threddsURI);
    }
    
    
    
    private void prepOperationsMetadata() {
        Element operationMetadata = (Element) document.getElementsByTagName("ows:OperationsMetadata").item(0);
        // get our operations
        NodeList operations = operationMetadata.getElementsByTagName("ows:Operation");
        for (int i=0; i<operations.getLength(); i++) {
            Element op = (Element) operations.item(i);
            if (op.getAttribute("name").equalsIgnoreCase("getcapabilities"))
                getCaps = op;
            else if (op.getAttribute("name").equalsIgnoreCase("getobservation"))
                getObs = op;
            else if (op.getAttribute("name").equalsIgnoreCase("describesensor"))
                descSen = op;
        }
    }
    
    private void addLatLonParameters(Element parent, LatLonRect bbox) {
        // lat
        Element lat = getDocument().createElement("ows:Parameter");
        lat.setAttribute("name", "lat");
        lat.setAttribute("use", "optional");
        Element latAllowedValues = getDocument().createElement("ows:AllowedValues");
        // min
        Element latMin = getDocument().createElement("ows:MinimumValue");
        latMin.setTextContent(bbox.getLowerLeftPoint().getLatitude() + "");
        latAllowedValues.appendChild(latMin);
        // max
        Element latMax = getDocument().createElement("ows:MaximumValue");
        latMax.setTextContent(bbox.getUpperRightPoint().getLatitude() + "");
        latAllowedValues.appendChild(latMax);
        lat.appendChild(latAllowedValues);
        parent.appendChild(lat);
        // lon
        Element lon = getDocument().createElement("ows:Parameter");
        lon.setAttribute("name", "lon");
        lon.setAttribute("use", "optional");
        Element lonAllowedValues = getDocument().createElement("ows:AllowedValues");
        // min
        Element lonMin = getDocument().createElement("ows:MinimumValue");
        lonMin.setTextContent(bbox.getLowerLeftPoint().getLongitude() + "");
        lonAllowedValues.appendChild(lonMin);
        // max
        Element lonMax = getDocument().createElement("ows:MaximumValue");
        lonMax.setTextContent(bbox.getUpperRightPoint().getLongitude() + "");
        lonAllowedValues.appendChild(lonMax);
        lon.appendChild(lonAllowedValues);
        parent.appendChild(lon);
    }
    
}
