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
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
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
    private final static String capabilitiesElement = "sos:Capabilities";
    private final static String observationOfferingElement = "sos:ObservationOfferingList";

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
        } catch (Exception ex) {
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
     * sets the service identification information 
     */
    public void parseServiceIdentification(String title, String history, String access) {
        NodeList nodeLst = getDocument().getElementsByTagName("ows:ServiceIdentification");

        for (int s = 0; s < nodeLst.getLength(); s++) {

            Node fstNode = nodeLst.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                //looks at the one node
                Element fstElmnt = (Element) fstNode;
                //looks at title
                NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("ows:Title");
                Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                NodeList fstNm = fstNmElmnt.getChildNodes();
                fstNm.item(0).setTextContent(title.trim());

                //looks at the adstract
                NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("ows:Abstract");
                Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
                NodeList lstNm = lstNmElmnt.getChildNodes();
                lstNm.item(0).setTextContent(history.trim());
                
                fstElmnt.getElementsByTagName("ows:AccessConstraints").item(0).setTextContent(access.trim());
            }
        }
    }
    
    public void removeServiceIdentification() {
        Element capsNode = (Element) getDocument().getElementsByTagName(capabilitiesElement).item(0);
        Node node = capsNode.getElementsByTagName("ows:ServiceIdentification").item(0);
        
        capsNode.removeChild(node);
    }

    /**
     * sets the service description, this is typically additional created user/site information
     */
    public void parseServiceDescription(String dataPage, String primaryOwnership) {
        //get service provider node list
        NodeList serviceProviderNodeList = getDocument().getElementsByTagName("ows:ServiceProvider");
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        // set org info
        // url
        fstElmnt.getElementsByTagName("ows:ProviderSite").item(0).setTextContent(dataPage.trim());
        // name
        fstElmnt.getElementsByTagName("ows:ProviderName").item(0).setTextContent(primaryOwnership.trim());
    }
    
    public void removeServiceProvider() {
        Element capsNode = (Element) getDocument().getElementsByTagName(capabilitiesElement).item(0);
        Node serviceProviderNode = capsNode.getElementsByTagName("ows:ServiceProvider").item(0);
        
        capsNode.removeChild(serviceProviderNode);
    }
    
    /**
     * 
     * @param threddsURI 
     */
    public void setOperationGetCaps(String threddsURI) {
        setOperationMethods((Element)getCaps.getElementsByTagName("ows:HTTP").item(0), threddsURI);
    }
    
    public void removeOperations() {
        Element capsNode = (Element) getDocument().getElementsByTagName(capabilitiesElement).item(0);
        Node operationsNode = capsNode.getElementsByTagName("ows:OperationsMetadata").item(0);
        capsNode.removeChild(operationsNode);
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
    public void setOperationGetObs(String threddsURI, CalendarDate startDate, CalendarDate endDate, List<String> dataVarShortNames, String[] stationNames, boolean gridDataset, LatLonRect gridBbox, FeatureType ftype) {
        // set url info
        setOperationMethods((Element)getObs.getElementsByTagName("ows:HTTP").item(0), threddsURI);
        // set info for get observation operation
        // get our parameters that need to be filled
        Element eventtime = null, offering = null, observedproperty = null, procedure = null;
        NodeList nodes = getObs.getElementsByTagName("ows:Parameter");
        for (int i=0; i<nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            if (elem.getAttribute("name").equalsIgnoreCase("offering")) {
                offering = elem;
            }
            else if (elem.getAttribute("name").equalsIgnoreCase("observedProperty")) {
                observedproperty = elem;
            }
            else if (elem.getAttribute("name").equalsIgnoreCase("eventTime")) {
                eventtime = elem;
            }
            else if (elem.getAttribute("name").equalsIgnoreCase("procedure")) {
                procedure = elem;
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
            // add network-all as an offering
            Element network = document.createElement("ows:Value");
            network.setTextContent("network-all");
            aV.appendChild(network);
            for (String name : stationNames) {
                Element value = getDocument().createElement("ows:Value");
                value.setTextContent(name);
                aV.appendChild(value);
            }
        }
        // set procedure parameter - list of procedures
        // add allowed values node
        Element allowedValues = getDocument().createElement("ows:AllowedValues");
        procedure.appendChild(allowedValues);
        // add network-all value
        Element na = getDocument().createElement("ows:Value");
        na.setTextContent(SOSBaseRequestHandler.getGMLNetworkAll());
        allowedValues.appendChild(na);
        for (String stationName : stationNames) {
            Element elem = getDocument().createElement("ows:Value");
            elem.setTextContent(SOSBaseRequestHandler.getGMLName(stationName));
            allowedValues.appendChild(elem);
            for (String senName : dataVarShortNames) {
                Element sElem = getDocument().createElement("ows:Value");
                sElem.setTextContent(SOSBaseRequestHandler.getSensorGMLName(stationName, senName));
                allowedValues.appendChild(sElem);
            }
        }
        // set additional response formats, supported
        switch(ftype) {
            case STATION:
                // add new ioos response format for TimeSeries data
                NodeList nlist = getObs.getElementsByTagName("ows:Parameter");
                for (int n = 0; n<nlist.getLength(); n++) {
                    Element elm = (Element) nlist.item(n);
                    if ("responseFormat".equals(elm.getAttribute("name"))) {
                        Element av = (Element) elm.getElementsByTagName("ows:AllowedValues").item(0);
                        Element rf = document.createElement("ows:Value");
                        rf.setTextContent("text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0\"");
                        av.appendChild(rf);
                    }
                }
                break;
        }
        // add lat and lon as parameters if we are a grid dataset
        if (gridDataset)
            addLatLonParameters(getObs, gridBbox);
    }
    
    /**
     * 
     * @param threddsURI
     * @param stationNames
     * @param sensorNames 
     */
    public void setOperationDescSen(String threddsURI, String[] stationNames, List<String> sensorNames) {
        // set url info
        setOperationMethods((Element)descSen.getElementsByTagName("ows:HTTP").item(0), threddsURI);
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
            // add network-all value
            Element na = getDocument().createElement("ows:Value");
            na.setTextContent("urn:ioos:network:" + SOSBaseRequestHandler.getNamingAuthority() + ":all");
            allowedValues.appendChild(na);
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
    
    public void setObservationOfferingNetwork(LatLonRect datasetRect, String[] stations, List<String> sensors, CalendarDateRange datasetTime, FeatureType ftype) {
        // add the network-all observation offering
        Element offeringList = (Element) document.getElementsByTagName(observationOfferingElement).item(0);
        Element obsOffering = document.createElement("sos:ObservationOffering");
        obsOffering.setAttribute("gml:id", "network-all");
        // add description, name and srs
        Element desc = document.createElement("gml:description");
        desc.setTextContent("All stations in the netCDF dataset.");
        obsOffering.appendChild(desc);
        Element name = document.createElement("gml:name");
        name.setTextContent("urn:ioos:network:" + SOSBaseRequestHandler.getNamingAuthority() + ":all");
        obsOffering.appendChild(name);
        Element srsName = getDocument().createElement("gml:srsName");
        srsName.setTextContent("EPSG:4326");
        obsOffering.appendChild(srsName);
        // bounds
        obsOffering.appendChild(getStationBounds(datasetRect));
        // time
        if (datasetTime != null)
            obsOffering.appendChild(getStationPeriod(datasetTime));
        // add network all to procedure list
        Element naProcedure = getDocument().createElement("sos:procedure");
        naProcedure.setAttribute("xlink:href", SOSBaseRequestHandler.getGMLNetworkAll());
        obsOffering.appendChild(naProcedure);
        // procedures
        for (String str : stations) {
            Element proc = getDocument().createElement("sos:procedure");
            proc.setAttribute("xlink:href", SOSBaseRequestHandler.getGMLName(str));
            obsOffering.appendChild(proc);
        }
        // observed properties
        for (String str : sensors) {
            Element value = getDocument().createElement("sos:observedProperty");
            value.setAttribute("xlink:href", str);
            obsOffering.appendChild(value);
        }
        // feature of interests
        for (String str: stations) {
            Element foi = getDocument().createElement("sos:featureOfInterest");
            foi.setAttribute("xlink:href", SOSBaseRequestHandler.getGMLName(str));
            obsOffering.appendChild(foi);
        }
        // response format
        Element rf = getDocument().createElement("sos:responseFormat");
        rf.setTextContent("text/xml; subtype=\"om/1.0.0\"");
        obsOffering.appendChild(rf);
        // if supported by feature type, add new repsonse format
        switch(ftype) {
            case STATION:
                rf = getDocument().createElement("sos:responseFormat");
                rf.setTextContent("text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0");
                obsOffering.appendChild(rf);
                break;
            default:
                break;
        }
        // response model/mode -- blank for now?
        Element rm = getDocument().createElement("sos:responseModel");
        rm.setTextContent("om:ObservationCollection");
        obsOffering.appendChild(rm);
        Element rm2 = getDocument().createElement("sos:responseMode");
        rm2.setTextContent("inline");
        obsOffering.appendChild(rm2);

        // add offering
        offeringList.appendChild(obsOffering);
    }
    
    public void setObservationOfferingList(String stationName, int stationIndex, LatLonRect rect, List<String> sensorNames, CalendarDateRange stationDates, FeatureType ftype) {
        Element offeringList = (Element) getDocument().getElementsByTagName(observationOfferingElement).item(0);
        // iterate through offerings (stations)
        Element obsOffering = getDocument().createElement("sos:ObservationOffering");
        obsOffering.setAttribute("gml:id", stationName);
        // gml:name
        Element gmlName = getDocument().createElement("gml:name");
        gmlName.setTextContent(SOSBaseRequestHandler.getGMLName(stationName));
        obsOffering.appendChild(gmlName);
        // gml:srsName - default to EPSG:4326 for now
        Element srsName = getDocument().createElement("gml:srsName");
        srsName.setTextContent("EPSG:4326");
        obsOffering.appendChild(srsName);
        // bounds
        obsOffering.appendChild(getStationBounds(rect));
        // add time envelope
        obsOffering.appendChild(getStationPeriod(stationDates));
        // feature of interest -- station name?
        Element foi = getDocument().createElement("sos:featureOfInterest");
        foi.setAttribute("xlink:href", SOSBaseRequestHandler.getGMLName(stationName));
        obsOffering.appendChild(foi);
        // observed properties
        for (String str : sensorNames) {
            Element value = getDocument().createElement("sos:observedProperty");
            value.setAttribute("xlink:href", str);
            obsOffering.appendChild(value);
        }
        // procedure for this station
        Element selfProcedure = getDocument().createElement("sos:procedure");
        selfProcedure.setAttribute("xlink:href", SOSBaseRequestHandler.getGMLName(stationName));
        obsOffering.appendChild(selfProcedure);
        // procedures
        for (String str : sensorNames) {
            Element proc = getDocument().createElement("sos:procedure");
            proc.setAttribute("xlink:href", SOSBaseRequestHandler.getSensorGMLName(stationName, str));
            obsOffering.appendChild(proc);
        }
        // response format
        Element rf = getDocument().createElement("sos:responseFormat");
        rf.setTextContent("text/xml;subtype=\"om/1.0.0\"");
        obsOffering.appendChild(rf);
        // if supported by feature type, add new repsonse format
        switch(ftype) {
            case STATION:
                rf = getDocument().createElement("sos:responseFormat");
                rf.setTextContent("text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0");
                obsOffering.appendChild(rf);
                break;
            default:
                break;
        }
        // response model/mode -- blank for now?
        Element rm = getDocument().createElement("sos:responseModel");
        rm.setTextContent("om:ObservationCollection");
        obsOffering.appendChild(rm);
        rm = getDocument().createElement("sos:responseMode");
        rm.setTextContent("inline");
        obsOffering.appendChild(rm);

        // add offering
        offeringList.appendChild(obsOffering);
    }
    
    public void removeContents() {
        Element capsNode = (Element) getDocument().getElementsByTagName(capabilitiesElement).item(0);
        Node contentNode = capsNode.getElementsByTagName("sos:Contents").item(0);
        capsNode.removeChild(contentNode);
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
    
    private Element getStationPeriod(CalendarDateRange stationDateRange) {
        Element retval = getDocument().createElement("sos:time");
        if (stationDateRange != null) {
            // time
            Element timePeriod = getDocument().createElement("gml:TimePeriod");
            timePeriod.setAttribute("xsi:type", "gml:TimePeriodType");
            // begin
            Element begin = getDocument().createElement("gml:beginPosition");
            begin.setTextContent(stationDateRange.getStart().toString());
            timePeriod.appendChild(begin);
            // end
            Element end = getDocument().createElement("gml:endPosition");
            end.setTextContent(stationDateRange.getEnd().toString());
            timePeriod.appendChild(end);
            retval.appendChild(timePeriod);
        }
        return retval;
    }
    
    private Element getStationBounds(LatLonRect rect) {
        Element retval = getDocument().createElement("gml:boundedBy");
        
        if (rect != null) {
            Element envelope = getDocument().createElement("gml:Envelope");
            envelope.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4326");
            // lower corner
            Element lowercorner = getDocument().createElement("gml:lowerCorner");
            lowercorner.setTextContent(rect.getLowerLeftPoint().getLatitude() + " " + rect.getLowerLeftPoint().getLongitude());
            envelope.appendChild(lowercorner);
            // upper corner
            Element uppercorner = getDocument().createElement("gml:upperCorner");
            uppercorner.setTextContent(rect.getUpperRightPoint().getLatitude() + " " + rect.getUpperRightPoint().getLongitude());
            envelope.appendChild(uppercorner);
            retval.appendChild(envelope);
        }
        
        return retval;
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
        lat.setAttribute("use", "required");
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
        lon.setAttribute("use", "required");
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
