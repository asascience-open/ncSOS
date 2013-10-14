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
public class GetCapsOutputter extends SOSOutputFormatter {

    public static final String ABSTRACT = "Abstract";
    public static final String ACCESSCONSTRAINTS = "AccessConstraints";
    public static final String CONTENTS = "Contents";
    public static final String DESCRIBESENSOR = "describesensor";
    public static final String EPSG4326 = "EPSG:4326";
    public static final String EXTENDED_CAPABILITIES = "ExtendedCapabilities";
    public static final String GETCAPABILITIES = "getcapabilities";
    public static final String GETOBSERVATION = "getobservation";
    public static final String GMLTIMEPERIODTYPE = "gml:TimePeriodType";
    public static final String HTTP = "HTTP";
    public static final String INLINE = "inline";
    public static final String TITLE = "Title";
    public static final String META_DATA_PROPERTY = "metaDataProperty";
    public static final String NCSOS_NAME = "ncSOS";
    public static final String NETWORK_ALL = "network-all";
    public static final String OMOBSERVATIONCOLLECTION = "om:ObservationCollection";
    public static final String OPERATION = "Operation";
    public static final String OPERATIONSMETADATA = "OperationsMetadata";
    public static final String PROVIDERNAME = "ProviderName";
    public static final String PROVIDERSITE = "ProviderSite";
    public static final String RC6_NAME = "RC6";
    public static final String RESPONSEMODE = "responseMode";
    public static final String SERVICEIDENTIFICATION = "ServiceIdentification";
    public static final String SERVICEPROVIDER = "ServiceProvider";
    public static final String SRSNAME = "srsName";
    public static final String VERSION_1_0 = "1.0";
    public static final String XLINK_HREF = "xlink:href";
    public static final String XLINKHREF = XLINK_HREF;
    public static final String XLINK_TITLE = "xlink:title";
    public static final String XSITYPE = "xsi:type";
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

        initNamespaces();

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
        NodeList nodeLst = getDocument().getElementsByTagNameNS(OWS_NS, SERVICEIDENTIFICATION);

        for (int s = 0; s < nodeLst.getLength(); s++) {

            Node fstNode = nodeLst.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                //looks at the one node
                Element fstElmnt = (Element) fstNode;
                //looks at title
                NodeList fstNmElmntLst = fstElmnt.getElementsByTagNameNS(OWS_NS, TITLE);
                Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                NodeList fstNm = fstNmElmnt.getChildNodes();
                fstNm.item(0).setTextContent(title.trim());

                //looks at the adstract
                NodeList lstNmElmntLst = fstElmnt.getElementsByTagNameNS(OWS_NS, ABSTRACT);
                Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
                NodeList lstNm = lstNmElmnt.getChildNodes();
                lstNm.item(0).setTextContent(history.trim());

                fstElmnt.getElementsByTagNameNS(OWS_NS, ACCESSCONSTRAINTS).item(0).setTextContent(access.trim());
            }
        }
    }

    public void removeServiceIdentification() {
        Element capsNode = (Element) getDocument().getElementsByTagNameNS(SOS_NS, CAPABILITIES).item(0);
        Node node = capsNode.getElementsByTagNameNS(OWS_NS, SERVICEIDENTIFICATION).item(0);

        capsNode.removeChild(node);
    }

    /**
     * sets the service description, this is typically additional created user/site information
     */
    public void parseServiceDescription(String dataPage, String primaryOwnership) {
        //get service provider node list
        NodeList serviceProviderNodeList = getDocument().getElementsByTagNameNS(OWS_NS, SERVICEPROVIDER);
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;
        // set org info
        // url
        fstElmnt.getElementsByTagNameNS(OWS_NS, PROVIDERSITE).item(0).getAttributes().getNamedItemNS(XLINK_NS, "href").setNodeValue(dataPage.trim());
        // name
        fstElmnt.getElementsByTagNameNS(OWS_NS, PROVIDERNAME).item(0).setTextContent(primaryOwnership.trim());
    }

    public void removeServiceProvider() {
        Element capsNode = (Element) getDocument().getElementsByTagNameNS(SOS_NS, CAPABILITIES).item(0);
        Node serviceProviderNode = capsNode.getElementsByTagNameNS(OWS_NS, SERVICEPROVIDER).item(0);

        capsNode.removeChild(serviceProviderNode);
    }

    /**
     * 
     * @param threddsURI 
     */
    public void setOperationGetCaps(String threddsURI) {
        setOperationMethods((Element) getCaps.getElementsByTagNameNS(OWS_NS, HTTP).item(0), threddsURI);
    }

    public void removeOperations() {
        Element capsNode = (Element) getDocument().getElementsByTagNameNS(SOS_NS, CAPABILITIES).item(0);
        Node operationsNode = capsNode.getElementsByTagNameNS(OWS_NS, OPERATIONSMETADATA).item(0);
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
    public void setOperationGetObs(String threddsURI, CalendarDate startDate, CalendarDate endDate,
            List<String> dataVarShortNames, String[] stationNames,
            boolean gridDataset, LatLonRect gridBbox, FeatureType ftype) {
        // set url info
        setOperationMethods((Element) getObs.getElementsByTagNameNS(OWS_NS, HTTP).item(0), threddsURI);
        // set info for get observation operation
        // get our parameters that need to be filled
        Element eventtime = null, offering = null, observedproperty = null, procedure = null;
        NodeList nodes = getObs.getElementsByTagNameNS(OWS_NS, PARAMETER);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            String nameAtt = elem.getAttribute(NAME);
            if (nameAtt != null) {
                if (nameAtt.equals(OFFERING)) {
                    offering = elem;
                } else if (nameAtt.equals(OBSERVED_PROPERTY)) {
                    observedproperty = elem;
                }
            }
        }

        if (offering != null) {
            addObservationalOfferings(offering, stationNames);
        }

        // set observedProperty parameter
        if (observedproperty != null) {
            addObservationalOfferings(observedproperty, dataVarShortNames);
        }
    }

    private void addObservationalOfferings(Element parentNode, String[] stationNames) {
        Element allowedValues = createElementNS(OWS_NS, ALLOWED_VALUES);
        parentNode.appendChild(allowedValues);

        Element value = createElementNS(OWS_NS, VALUE);
        value.setTextContent(NETWORK_ALL);
        allowedValues.appendChild(value);
        for (String name : stationNames) {
            value = createElementNS(OWS_NS, VALUE);
            value.setTextContent(name);
            allowedValues.appendChild(value);
        }
    }

    public void addObservationalOfferings(Element parentNode, List<String> dataVarShortNames) {
        Element allowedValues = createElementNS(OWS_NS, ALLOWED_VALUES);
        parentNode.appendChild(allowedValues);
        for (String name : dataVarShortNames) {
            Element value = createElementNS(OWS_NS, VALUE);
            value.setTextContent(name);
            allowedValues.appendChild(value);
        }
    }

    public void addExtendedCapabilities() {
        Element operationMetadata = (Element) document.getElementsByTagNameNS(OWS_NS, OPERATIONSMETADATA).item(0);

        if (operationMetadata != null) {
            //section
            Element extendCaps = createElementNS(OWS_NS, EXTENDED_CAPABILITIES);
            //first props
            Element metaProps = createElementNS(GML_NS, META_DATA_PROPERTY);
            metaProps.setAttribute(XLINK_TITLE, "ioosTemplateVersion");
            metaProps.setAttribute(XLINK_HREF, "http://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0");

            Element version = createElementNS(GML_NS, VERSION);
            version.setTextContent(VERSION_1_0);
            metaProps.appendChild(version);
            //add it to the parent node
            extendCaps.appendChild(metaProps);

            //second props
            metaProps = createElementNS(GML_NS, META_DATA_PROPERTY);
            metaProps.setAttribute(XLINK_TITLE, "softwareVersion");
            metaProps.setAttribute(XLINK_HREF, "http://github.com/asascience-open/" + NCSOS_NAME + "/releases/tag/" + RC6_NAME);
            String[] nodeList = {NAME, VERSION};
            String[] nodeValues = {NCSOS_NAME, RC6_NAME};
            for (int i = 0; i < nodeList.length; i++) {
                version = createElementNS(GML_NS, nodeList[i]);
                version.setTextContent(nodeValues[i]);
                metaProps.appendChild(version);
            }

            extendCaps.appendChild(metaProps);

            //add nodes
            if (extendCaps != null) {
                operationMetadata.appendChild(extendCaps);
            }
        }
    }

    /**
     * 
     * @param threddsURI
     * @param stationNames
     * @param sensorNames 
     */
    public void setOperationDescSen(String threddsURI, String[] stationNames, List<String> sensorNames) {
        // set url info
        setOperationMethods((Element) descSen.getElementsByTagNameNS(OWS_NS, HTTP).item(0), threddsURI);
        // set procedure allowed values of each station and sensor for the dataset
        Element procedure = null;
        NodeList nodes = descSen.getElementsByTagNameNS(OWS_NS, PARAMETER);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            if (elem != null && elem.getAttribute(NAME).equals(PROCEDURE)) {
                procedure = elem;
                break;
            }
        }
        if (procedure != null && stationNames != null) {
            // add allowed values node
            Element allowedValues = createElementNS(OWS_NS, ALLOWED_VALUES);
            procedure.appendChild(allowedValues);
            // add network-all value
            Element na = createElementNS(OWS_NS, VALUE);

            na.setTextContent("urn:ioos:network:" + SOSBaseRequestHandler.getNamingAuthority() + ":all");
            allowedValues.appendChild(na);
            for (String stationName : stationNames) {
                Element elem = createElementNS(OWS_NS, VALUE);

                elem.setTextContent(SOSBaseRequestHandler.getGMLName(stationName));
                allowedValues.appendChild(elem);
                for (String senName : sensorNames) {
                    Element sElem = createElementNS(OWS_NS, VALUE);

                    sElem.setTextContent(SOSBaseRequestHandler.getSensorGMLName(stationName, senName));
                    allowedValues.appendChild(sElem);
                }
            }
        }
    }

    public void setObservationOfferingNetwork(LatLonRect datasetRect, String[] stations, List<String> sensors, CalendarDateRange datasetTime, FeatureType ftype) {
        // add the network-all observation offering
        Element offeringList = (Element) document.getElementsByTagNameNS(SOS_NS, OBSERVATION_OFFERING_LIST).item(0);
        Element obsOffering = createElementNS(SOS_NS, OBSERVATION_OFFERING);
        obsOffering.setAttributeNS(GML_NS, ID, NETWORK_ALL);

        // add description, name and srs
        Element desc = createElementNS(GML_NS, DESCRIPTION);
        desc.setTextContent("All stations in the netCDF dataset.");
        obsOffering.appendChild(desc);
        Element name = createElementNS(GML_NS, NAME);
        name.setTextContent(NETWORK_URN + SOSBaseRequestHandler.getNamingAuthority() + NETWORK_URN_END_ALL);
        obsOffering.appendChild(name);
        Element srsName = createElementNS(GML_NS, SRSNAME);
        srsName.setTextContent(EPSG4326);
        obsOffering.appendChild(srsName);
        // bounds
        obsOffering.appendChild(getStationBounds(datasetRect));
        // time
        if (datasetTime != null) {
            obsOffering.appendChild(getStationPeriod(datasetTime));
        }
        // add network all to procedure list
        Element naProcedure = createElementNS(SOS_NS, PROCEDURE);
        naProcedure.setAttribute(XLINK_HREF, SOSBaseRequestHandler.getGMLNetworkAll());
        obsOffering.appendChild(naProcedure);
        // procedures
        for (String str : stations) {
            Element proc = createElementNS(SOS_NS, PROCEDURE);
            proc.setAttribute(XLINK_HREF, SOSBaseRequestHandler.getGMLName(str));
            obsOffering.appendChild(proc);
        }
        // observed properties
        for (String str : sensors) {
            Element value = createElementNS(SOS_NS, "observedProperty");
            value.setAttribute(XLINK_HREF, str);
            obsOffering.appendChild(value);
        }
        // feature of interests
        for (String str : stations) {
            Element foi = createElementNS(SOS_NS, "featureOfInterest");
            foi.setAttribute(XLINK_HREF, SOSBaseRequestHandler.getGMLName(str));
            obsOffering.appendChild(foi);
        }
        // response format
        Element rf = createElementNS(SOS_NS, RESPONSE_FORMAT);
        rf.setTextContent("text/xml; subtype=\"om/1.0.0\"");
        obsOffering.appendChild(rf);
        // if supported by feature type, add new repsonse format
        switch (ftype) {
            case STATION:
                rf = createElementNS(SOS_NS, RESPONSE_FORMAT);
                rf.setTextContent("text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0");
                obsOffering.appendChild(rf);
                break;
            default:
                break;
        }
        // result model/mode -- blank for now?
        Element rm = createElementNS(SOS_NS, RESULT_MODEL);
        rm.setTextContent(OMOBSERVATIONCOLLECTION);
        obsOffering.appendChild(rm);
        Element rm2 = createElementNS(SOS_NS, RESPONSE_MODE);
        rm2.setTextContent("inline");
        obsOffering.appendChild(rm2);

        // add offering
        offeringList.appendChild(obsOffering);
    }

    public void setObservationOfferingList(String stationName, LatLonRect rect, List<String> sensorNames, CalendarDateRange stationDates, FeatureType ftype) {
        Element offeringList = (Element) getDocument().getElementsByTagNameNS(SOS_NS, OBSERVATION_OFFERING_LIST).item(0);
        // iterate through offerings (stations)
        Element obsOffering = createElementNS(SOS_NS, OBSERVATION_OFFERING);

        obsOffering.setAttributeNS(GML_NS, ID, stationName);
        // gml:name
        Element gmlName = createElementNS(GML_NS, NAME);
        gmlName.setTextContent(SOSBaseRequestHandler.getGMLName(stationName));
        obsOffering.appendChild(gmlName);
        // gml:srsName - default to EPSG:4326 for now
        Element srsName = createElementNS(GML_NS, SRSNAME);
        srsName.setTextContent(EPSG4326);
        obsOffering.appendChild(srsName);
        // bounds
        obsOffering.appendChild(getStationBounds(rect));
        // add time envelope
        obsOffering.appendChild(getStationPeriod(stationDates));
        // feature of interest -- station name?
        Element foi = createElementNS(SOS_NS, FEATURE_INTEREST);
        foi.setAttribute(XLINK_HREF, SOSBaseRequestHandler.getGMLName(stationName));
        obsOffering.appendChild(foi);
        // observed properties
        for (String str : sensorNames) {
            Element value = createElementNS(SOS_NS, OBSERVED_PROPERTY);
            value.setAttribute(XLINK_HREF, str);
            obsOffering.appendChild(value);
        }
        // procedure for this station
        Element selfProcedure = createElementNS(SOS_NS, PROCEDURE);
        selfProcedure.setAttribute(XLINK_HREF, SOSBaseRequestHandler.getGMLName(stationName));
        obsOffering.appendChild(selfProcedure);
        // response format
        Element rf = createElementNS(SOS_NS, RESPONSE_FORMAT);
        rf.setTextContent("text/xml;subtype=\"om/1.0.0\"");
        obsOffering.appendChild(rf);
        // if supported by feature type, add new repsonse format
        switch (ftype) {
            case STATION:
                rf = createElementNS(SOS_NS, RESPONSE_FORMAT);
                rf.setTextContent(this.IOOS_RF_1_0);
                obsOffering.appendChild(rf);
                break;
            default:
                break;
        }
        // result model/mode -- blank for now?
        Element rm = createElementNS(SOS_NS, RESULT_MODEL);
        rm.setTextContent(OMOBSERVATIONCOLLECTION);
        obsOffering.appendChild(rm);
        rm = createElementNS(SOS_NS, RESPONSEMODE);
        rm.setTextContent(INLINE);
        obsOffering.appendChild(rm);

        // add offering
        offeringList.appendChild(obsOffering);
    }

    public void removeContents() {
        Element capsNode = (Element) getDocument().getElementsByTagNameNS(SOS_NS, CAPABILITIES).item(0);
        Node contentNode = capsNode.getElementsByTagNameNS(SOS_NS, CONTENTS).item(0);
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
        NodeList getList = parent.getElementsByTagNameNS(OWS_NS, "Get");
        Element getElm = (Element) getList.item(0);
        getElm.setAttribute(XLINK_HREF, threddsURI);

        //set get capabilities Post request link -- not supported TODO
//        NodeList fstNm12 = fstNmElmnt.getElementsByTagName("ows:Post");
//        Element fstNmElmnt12 = (Element) fstNm12.item(0);
//        if (fstNmElmnt12 != null)
//            fstNmElmnt12.setAttribute("xlink:href", threddsURI);
    }

    private Element getStationPeriod(CalendarDateRange stationDateRange) {
        Element retval = createElementNS(SOS_NS, TIME);
        if (stationDateRange != null) {
            // time
            Element timePeriod = createElementNS(GML_NS, TIME_PERIOD);
            timePeriod.setAttribute(XSITYPE, GMLTIMEPERIODTYPE);
            // begin
            Element begin = createElementNS(GML_NS, BEGIN_POSITION);
            begin.setTextContent(stationDateRange.getStart().toString());
            timePeriod.appendChild(begin);
            // end
            Element end = createElementNS(GML_NS, END_POSITION);
            end.setTextContent(stationDateRange.getEnd().toString());
            timePeriod.appendChild(end);
            retval.appendChild(timePeriod);
        }
        return retval;
    }

    private Element getStationBounds(LatLonRect rect) {
        Element retval = createElementNS(GML_NS, BOUNDED_BY);

        if (rect != null) {
            Element envelope = createElementNS(GML_NS, ENVELOPE);
            envelope.setAttribute(SRSNAME, "http://www.opengis.net/def/crs/EPSG/0/4326");
            // lower corner
            Element lowercorner = createElementNS(GML_NS, LOWER_CORNER);
            lowercorner.setTextContent(rect.getLowerLeftPoint().getLatitude() + " " + rect.getLowerLeftPoint().getLongitude());
            envelope.appendChild(lowercorner);
            // upper corner
            Element uppercorner = createElementNS(GML_NS, UPPER_CORNER);
            uppercorner.setTextContent(rect.getUpperRightPoint().getLatitude() + " " + rect.getUpperRightPoint().getLongitude());
            envelope.appendChild(uppercorner);
            retval.appendChild(envelope);
        }

        return retval;
    }

    private void prepOperationsMetadata() {
        Element operationMetadata = (Element) document.getElementsByTagNameNS(OWS_NS, OPERATIONSMETADATA).item(0);
        // get our operations
        NodeList operations = operationMetadata.getElementsByTagNameNS(OWS_NS, OPERATION);
        for (int i = 0; i < operations.getLength(); i++) {
            Element op = (Element) operations.item(i);
            if (op.getAttribute(NAME).equalsIgnoreCase(GETCAPABILITIES)) {
                getCaps = op;
            } else if (op.getAttribute(NAME).equalsIgnoreCase(GETOBSERVATION)) {
                getObs = op;
            } else if (op.getAttribute(NAME).equalsIgnoreCase(DESCRIBESENSOR)) {
                descSen = op;
            }
        }
    }

    private void addLatLonParameters(Element parent, LatLonRect bbox) {
        // lat
        Element lat = createElementNS(OWS_NS, PARAMETER);
        lat.setAttribute(NAME, LAT);
        lat.setAttribute(USE, REQUIRED);
        Element latAllowedValues = createElementNS(OWS_NS, ALLOWED_VALUES);
        // min
        Element latMin = createElementNS(OWS_NS, MINIMUM_VALUE);
        latMin.setTextContent(bbox.getLowerLeftPoint().getLatitude() + "");
        latAllowedValues.appendChild(latMin);
        // max
        Element latMax = createElementNS(OWS_NS, MAXIMUM_VALUE);
        latMax.setTextContent(bbox.getUpperRightPoint().getLatitude() + "");
        latAllowedValues.appendChild(latMax);
        lat.appendChild(latAllowedValues);
        parent.appendChild(lat);
        // lon 
        Element lon = createElementNS(OWS_NS, PARAMETER);
        lon.setAttribute(NAME, LON);
        lon.setAttribute(USE, REQUIRED);
        Element lonAllowedValues = createElementNS(OWS_NS, ALLOWED_VALUES);
        // min
        Element lonMin = createElementNS(OWS_NS, MINIMUM_VALUE);
        lonMin.setTextContent(bbox.getLowerLeftPoint().getLongitude() + "");
        lonAllowedValues.appendChild(lonMin);
        // max
        Element lonMax = createElementNS(OWS_NS, MAXIMUM_VALUE);
        lonMax.setTextContent(bbox.getUpperRightPoint().getLongitude() + "");
        lonAllowedValues.appendChild(lonMax);
        lon.appendChild(lonAllowedValues);
        parent.appendChild(lon);
    }
}
