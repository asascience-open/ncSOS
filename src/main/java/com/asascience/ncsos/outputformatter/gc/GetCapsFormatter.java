/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.outputformatter.gc;

import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.service.BaseRequestHandler;
import com.asascience.ncsos.util.XMLDomUtils;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonRect;

import java.io.Writer;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author kwilcox
 */
public class GetCapsFormatter extends OutputFormatter {

    public static final String CONTENTS = "Contents";
    public static final String EPSG4326 = "EPSG:4326";
    public static final String HTTP = "HTTP";
    public static final String OPERATIONS_METADATA = "OperationsMetadata";
    public static final String SERVICE_IDENTIFICATION = "ServiceIdentification";
    public static final String SERVICE_PROVIDER = "ServiceProvider";
    private boolean exceptionFlag = false;
    private final static String TEMPLATE = "templates/GC.xml";

    public GetCapsFormatter() {
        super();
    }

    @Override
    protected String getTemplateLocation() {
        return TEMPLATE;
    }

    public boolean hasExceptionOut() {
        return exceptionFlag;
    }

    public void parseServiceIdentification(HashMap<String, String> attrs) {
        Namespace owsns = this.getNamespace("ows");
        Element si = this.getRoot().getChild(SERVICE_IDENTIFICATION, owsns);
        // Name
        si.getChild("Title", owsns).setText(attrs.get("title"));
        // Title
        si.getChild("Abstract", owsns).setText(attrs.get("summary"));
        // Access
        si.getChild("AccessConstraints", owsns).setText(attrs.get("access_constraints"));
        // Keywords
        Element keywords = si.getChild("Keywords", owsns);
        Element k = null;
        for (String keyword : attrs.get("keywords").split(",")) {
            keywords.addContent(new Element("Keyword", owsns).setText(keyword));
        }
        // Fees
        si.getChild("Fees", owsns).setText(attrs.get("fees"));
    }

    public void removeServiceIdentification() {
        this.getRoot().removeChild(SERVICE_IDENTIFICATION, this.getNamespace("ows"));
    }

    public void parseServiceDescription(HashMap<String, String> attrs) {
        Namespace owsns = this.getNamespace("ows");
        Element si = this.getRoot().getChild(SERVICE_PROVIDER, owsns);
        // ProviderName
        si.getChild("ProviderName", owsns).setText(attrs.get("publisher_name"));
        // ProviderSite
        si.getChild("ProviderSite", owsns).getAttribute("xhref", this.getNamespace("xlink")).setValue(attrs.get("publisher_url"));
        // ServiceContact
        Element sc = si.getChild("ServiceContact", owsns);
        sc.getChild("IndividualName", owsns).setText(attrs.get("publisher_name"));
        sc.getChild("ContactInfo", owsns).getChild("Phone", owsns).getChild("Voice", owsns).setText(attrs.get("publisher_phone"));
        sc.getChild("ContactInfo", owsns).getChild("Address", owsns).getChild("ElectronicMailAddress", owsns).setText(attrs.get("publisher_email"));
    }

    public void removeServiceProvider() {
        this.getRoot().removeChild(SERVICE_PROVIDER, this.getNamespace("ows"));
    }

    public void setURL(String threddsURI) {
        Namespace owsns = this.getNamespace("ows");
        Element si = this.getRoot().getChild(OPERATIONS_METADATA, owsns);
        for (Object e : si.getChildren("Operation", owsns)) {
            this.setHTTPMethods((Element) e, threddsURI);
        }
    }

    public void setOperationsMetadataGetObs(HashMap<String, String> attrs, String threddsURI, List<String> dataVarShortNames, String[] stationNames) {
        Namespace owsns = this.getNamespace("ows");
        Element si = this.getRoot().getChild(OPERATIONS_METADATA, owsns);
        for (Object e : si.getChildren("Operation", owsns)) {
            Element op = (Element) e;
            if (op.getAttributeValue("name").equalsIgnoreCase("GetObservation")) {
                for (Object par : op.getChildren("Parameter", owsns)) {
                    Element p = (Element) par;
                    String name = p.getAttributeValue("name");
                    Element allowed = new Element("AllowedValues", owsns);
                    if (name.equalsIgnoreCase("offering")) {
                        for (String s : stationNames) {
                            allowed.addContent(new Element("Value", owsns).setText(s));
                        }
                        // Always add a 'network-all' offering
                        allowed.addContent(new Element("Value", owsns).setText("network-all"));
                        p.addContent(allowed);
                    } else if (name.equalsIgnoreCase("observedProperty")) {
                        for (String s : dataVarShortNames) {
                            allowed.addContent(new Element("Value", owsns).setText(s));
                        }
                        p.addContent(allowed);
                    } else if (name.equalsIgnoreCase("procedure")) {
                        for (String s : stationNames) {
                            allowed.addContent(new Element("Value", owsns).setText(BaseRequestHandler.STATION_URN_BASE + attrs.get("naming_authority") + ":" + s));
                        }
                        p.addContent(allowed);
                    }
                }
            }
        }
    }

    public void removeOperationsMetadata() {
        this.getRoot().removeChild(OPERATIONS_METADATA, this.getNamespace("ows"));
    }

    public void setVersionMetadata() {
        Namespace owsns = this.getNamespace("ows");
        Namespace gmlns = this.getNamespace("gml");
        List<Element> md = this.getRoot().getChild(OPERATIONS_METADATA, owsns)
                .getChild("ExtendedCapabilities", owsns)
                .getChildren("metaDataProperty", gmlns);
        for (Element e : md) {
            if (e.getAttributeValue("href", this.getNamespace("xlink")).equalsIgnoreCase("softwareVersion")) {
                e.getChild("version", gmlns).setText(NCSOS_VERSION);
            }
        }
    }

    /**
     * @param threddsURI
     * @param stationNames
     * @param sensorNames
     */
    public void setOperationsMetadataDescSen(HashMap<String, String> attrs, String threddsURI, List<String> sensorNames, String[] stationNames) {
        Namespace owsns = this.getNamespace("ows");
        Element si = this.getRoot().getChild(OPERATIONS_METADATA, owsns);
        for (Object e : si.getChildren("Operation", owsns)) {
            Element op = (Element) e;
            if (op.getAttributeValue("name").equalsIgnoreCase("DescribeSensor")) {
                for (Object par : op.getChildren("Parameter", owsns)) {
                    Element p = (Element) par;
                    String name = p.getAttributeValue("name");
                    Element allowed = new Element("AllowedValues", owsns);
                    if (name.equalsIgnoreCase("procedure")) {
                        for (String s : stationNames) {
                            allowed.addContent(new Element("Value", owsns).setText(BaseRequestHandler.STATION_URN_BASE + attrs.get("naming_authority") + ":" + s));
                        }
                        p.addContent(allowed);
                    }
                }
            }
        }
    }

    private Element buildOffering() {
        return new Element("ObservationOffering", this.getNamespace("sos"));
    }

    public void setObservationOfferingNetwork(HashMap<String, String> attrs, LatLonRect datasetRect, String[] stations, List<String> sensors, CalendarDateRange dates, FeatureType ftype) {
        Namespace owsns = this.getNamespace("ows");
        Namespace gmlns = this.getNamespace("gml");
        Namespace sosns = this.getNamespace("sos");
        Namespace xlinkns = this.getNamespace("xlink");
        Element ol = this.getRoot().getChild(CONTENTS, owsns).getChild(OBSERVATION_OFFERING_LIST, owsns);

        Element offering = this.buildOffering();
        // Id
        offering.setAttribute("id", "network-all", gmlns);
        // Name
        offering.addContent(new Element("name", gmlns).setText(BaseRequestHandler.NETWORK_URN_BASE + attrs.get("naming_authority") + ":all"));
        // Description
        offering.addContent(new Element("description", gmlns).setText("Network offering containing all features in the dataset"));
        // SRS
        offering.addContent(new Element("srsName", gmlns).setText(EPSG4326));
        // Bounded By
        offering.addContent(this.getBoundedBy(datasetRect));
        // Time
        offering.addContent(this.getTimePeriod(dates));
        // Procedure
        offering.addContent(new Element("procedure", sosns).setAttribute("href", BaseRequestHandler.NETWORK_URN_BASE + attrs.get("naming_authority") + ":all", xlinkns));
        for (String s : stations) {
            offering.addContent(new Element("procedure", sosns).setAttribute("href", BaseRequestHandler.STATION_URN_BASE + attrs.get("naming_authority") + ":" + s, xlinkns));
        }
        // ObservedProperty
        for (String s : sensors) {
            offering.addContent(new Element("observedProperty", sosns).setAttribute("href", s, xlinkns));
        }
        // FeatureOfInterest
        for (String s : stations) {
            offering.addContent(new Element("featureOfInterest", sosns).setAttribute("href", BaseRequestHandler.STATION_URN_BASE + attrs.get("naming_authority") + ":" + s, xlinkns));
        }
        // ResponseFormat
        offering.addContent(new Element("responseFormat", sosns).setText("text/xml; subtype=\"om/1.0.0\""));
        switch (ftype) {
            case STATION:
                offering.addContent(new Element("responseFormat", sosns).setText("text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0\""));
                break;
            default:
                break;
        }
        // ResultModel
        offering.addContent(new Element("resultModel", sosns).setText("om:ObservationCollection"));
        // ResponseMode
        offering.addContent(new Element("responseMode", sosns).setText("inline"));

        // Add the offering to the list
        ol.addContent(offering);
    }

    public void setObservationOffering(HashMap<String, String> attrs, String stationName, LatLonRect datasetRect, List<String> sensors, CalendarDateRange dates, FeatureType ftype) {
        Namespace owsns = this.getNamespace("ows");
        Namespace gmlns = this.getNamespace("gml");
        Namespace sosns = this.getNamespace("sos");
        Namespace xlinkns = this.getNamespace("xlink");
        Element ol = this.getRoot().getChild(CONTENTS, owsns).getChild(OBSERVATION_OFFERING_LIST, owsns);

        Element offering = this.buildOffering();
        // Id
        offering.setAttribute("id", stationName, gmlns);
        // Name
        offering.addContent(new Element("name", gmlns).setText(BaseRequestHandler.STATION_URN_BASE + attrs.get("naming_authority") + ":" + stationName));
        // Description
        // offering.getChild("description", gmlns).setText("Network offering containing all features in the dataset");
        // SRS
        offering.addContent(new Element("srsName", gmlns).setText(EPSG4326));
        // Bounded By
        offering.addContent(this.getBoundedBy(datasetRect));
        // Time
        offering.addContent(this.getTimePeriod(dates));
        // Procedure
        offering.addContent(new Element("procedure", sosns).setAttribute("href", BaseRequestHandler.STATION_URN_BASE + attrs.get("naming_authority") + ":" + stationName, xlinkns));
        // ObservedProperty
        for (String s : sensors) {
            offering.addContent(new Element("observedProperty", sosns).setAttribute("href", s, xlinkns));
        }
        // FeatureOfInterest
        offering.addContent(new Element("featureOfInterest", sosns).setAttribute("href", BaseRequestHandler.STATION_URN_BASE + attrs.get("naming_authority") + ":" + stationName, xlinkns));
        // ResponseFormat
        offering.addContent(new Element("responseFormat", sosns).setText("text/xml; subtype=\"om/1.0.0\""));
        switch (ftype) {
            case STATION:
                offering.addContent(new Element("responseFormat", sosns).setText("text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0\""));
                break;
            default:
                break;
        }
        // ResultModel
        offering.addContent(new Element("resultModel", sosns).setText("om:ObservationCollection"));
        // ResponseMode
        offering.addContent(new Element("responseMode", sosns).setText("inline"));

        // Add the offering to the list
        ol.addContent(offering);
    }

    public void removeContents() {
        this.getRoot().removeChild(CONTENTS, this.getNamespace("ows"));
    }

    /*********************/
    /* Interface Methods */

    /**
     * **********************************************************************
     */
    public void addDataFormattedStringToInfoList(String dataFormattedString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setupExceptionOutput(String message) {
        document = XMLDomUtils.getExceptionDom(message);
        exceptionFlag = true;
    }

    public void writeOutput(Writer writer) throws IOException {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(this.document, writer);
    }

    private void setHTTPMethods(Element parent, String threddsURI) {
        Namespace owsns = this.getNamespace("ows");
        // GET
        parent.getChild("DCP", owsns).getChild("HTTP", owsns).getChild("Get", owsns).setAttribute("href", threddsURI, this.getNamespace("xlink"));
        // TODO: When (if) we ever support POST methods, add an additional Post tag here with the URL
    }

    private Element getBoundedBy(LatLonRect rect) {
        Namespace gmlns = this.getNamespace("gml");
        Element bb = new Element("boundedBy", gmlns);
        Element env = new Element("Envelope", gmlns);
        env.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4326");
        env.getChild("lowerCorner", gmlns).setText(rect.getLowerLeftPoint().getLatitude() + " " + rect.getLowerLeftPoint().getLongitude());
        env.getChild("upperCorner", gmlns).setText(rect.getUpperRightPoint().getLatitude() + " " + rect.getUpperRightPoint().getLongitude());
        bb.addContent(env);
        return bb;
    }

    private Element getTimePeriod(CalendarDateRange dateRange) {
        Namespace gmlns = this.getNamespace("gml");
        Namespace sosns = this.getNamespace("sos");
        Element tm = new Element("time", sosns);
        Element tp = new Element("TimePeriod", gmlns);
        tm.addContent(tp);
        tp.getChild("beginPosition", gmlns).setText(dateRange.getStart().toString());
        tp.getChild("endPosition", gmlns).setText(dateRange.getEnd().toString());
        return tm;
    }
}
