package thredds.server.sos.getCaps;

import com.asascience.ncSOS.outputFormatters.GetCapsOutputter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.Chronology;
import org.joda.time.chrono.ISOChronology;
import org.w3c.dom.*;
import thredds.server.sos.CDMClasses.*;
import thredds.server.sos.getObs.ObservationOffering;
import thredds.server.sos.service.SOSBaseRequestHandler;
import thredds.server.sos.util.DiscreteSamplingGeometryUtil;
import thredds.server.sos.util.XMLDomUtils;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.ProfileFeatureCollection;
import ucar.nc2.ft.StationProfileFeatureCollection;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.units.DateFormatter;

/**
 *
 * @author Abird
 */
public class SOSGetCapabilitiesRequestHandler extends SOSBaseRequestHandler {

    private final String threddsURI;
    private final String format = "text/xml; subtype=\"om/1.0.0\"";
    Chronology chrono = ISOChronology.getInstance();
    DateFormatter dateFormatter = new DateFormatter();

    public SOSGetCapabilitiesRequestHandler(NetcdfDataset netCDFDataset, String threddsURI) throws IOException {
        super(netCDFDataset);
        this.threddsURI = threddsURI;
        output = new GetCapsOutputter();
    }
    
    private Document getDocument() {
        return ((GetCapsOutputter)output).getDocument();
    }
    private void setDocument(Document setter) {
        ((GetCapsOutputter)output).setDocument(setter);
    }

    /**
     * sets the service identification information 
     */
    public void parseServiceIdentification() {
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
                fstNm.item(0).setNodeValue(getTitle());

                //looks at the adstract
                NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("ows:Abstract");
                Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
                NodeList lstNm = lstNmElmnt.getChildNodes();
                if (getHistory() == null) {
                    lstNm.item(0).setNodeValue("");
                } else {
                    lstNm.item(0).setNodeValue(getHistory());
                }
            }

        }
    }

    private NodeList getXMLNode(Element fstElmnt, String xmlLocation) {
        NodeList tagNameNodeList = fstElmnt.getElementsByTagName(xmlLocation);
        Element fstNmElmnt1 = (Element) tagNameNodeList.item(0);
        NodeList fstNm1 = fstNmElmnt1.getChildNodes();
        return fstNm1;
    }

    private void checkEndDateElementNode(ObservationOffering offering, Element obsOfferingTimeEndEl) throws DOMException {
        //check the string to see if it either needs attribute of element
        if ((offering.getObservationTimeEnd().isEmpty()) || (offering.getObservationTimeEnd().length() < 2) || (offering.getObservationTimeEnd().contentEquals(""))) {
            obsOfferingTimeEndEl.setAttribute("indeterminatePosition", "unknown");
        } else {
            obsOfferingTimeEndEl.appendChild(getDocument().createTextNode(offering.getObservationTimeEnd()));
        }
    }

    private void setProviderName(Element fstElmnt, String xmlLocation) throws DOMException {
        //get the node named be the string
        NodeList fstNm1 = getXMLNode(fstElmnt, xmlLocation);
        if (getSource() == null) {
            fstNm1.item(0).setNodeValue("");
        } else {
            fstNm1.item(0).setNodeValue(getSource());
        }
    }

    private void setProviderSite(Element fstElmnt, String xmlLocation) throws DOMException {
        NodeList fstNm1 = getXMLNode(fstElmnt, xmlLocation);
        if (getInstitution() == null) {
            fstNm1.item(0).setNodeValue("");
        } else {
            fstNm1.item(0).setNodeValue(getInstitution());
        }
    }

    public String getInvividualNameSP() {
        return "";
    }

    public String getPositionNameSP() {
        return "";
    }

    public String getPhoneNoSP() {
        return "";
    }

    public String getHTTPGetAddress() {
        return threddsURI;
    }

    /**
     * sets the service description, this is typically additional created user/site information
     */
    public void parseServiceDescription() {
        //get service provider node list
        NodeList serviceProviderNodeList = getDocument().getElementsByTagName("ows:ServiceProvider");
        //get the first node in the the list matching the above name
        Node fstNode = serviceProviderNodeList.item(0);
        //create an element from the node
        Element fstElmnt = (Element) fstNode;

        setProviderName(fstElmnt, "ows:ProviderName");
        setProviderSite(fstElmnt, "ows:ProviderSite");
        setServiceContractName(fstElmnt, "ows:IndividualName");
        setServiceContractPositionName(fstElmnt, "ows:PositionName");
        setServiceContractPhoneNumber(fstElmnt, "ows:Voice");
    }

    private void setServiceContractName(Element fstElmnt, String xmlLocation) {
        NodeList fstNm1 = getXMLNode(fstElmnt, xmlLocation);
        fstNm1.item(0).setNodeValue(getInvividualNameSP());
    }

    private void setServiceContractPositionName(Element fstElmnt, String xmlLocation) {
        NodeList fstNm1 = getXMLNode(fstElmnt, xmlLocation);
        fstNm1.item(0).setNodeValue(getPositionNameSP());
    }

    private void setServiceContractPhoneNumber(Element fstElmnt, String xmlLocation) {
        NodeList fstNm1 = getXMLNode(fstElmnt, xmlLocation);
        fstNm1.item(0).setNodeValue(getPhoneNoSP());
    }

    public void parseOperationsMetaData() {
        //get operations meta data
        NodeList operationsNodeList = getDocument().getElementsByTagName("ows:OperationsMetadata");
        //set get capabilities meta data
        for (int s = 0; s < operationsNodeList.getLength(); s++) {

            Node fstNode = operationsNodeList.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                //looks at the one node
                Element fstElmnt = (Element) fstNode;
                //looks at title
                NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("ows:Operation");

                for (int jj = 0; jj < fstNmElmntLst.getLength(); jj++) {

                    Element fstNmElmnt = (Element) fstNmElmntLst.item(jj);
                    //String c = fstNmElmnt.getAttribute("name");
                    //System.out.println("Name: "  + fstNmElmnt.getAttribute("name"));

                    if (fstNmElmnt.getAttribute("name").contentEquals("GetCapabilities")) {
                        setGetCapabilitiesOperationsMetaData(fstNmElmnt);
                    } else if (fstNmElmnt.getAttribute("name").contentEquals("GetObservation")) {
                        setGetCapabilitiesOperationsMetaData(fstNmElmnt);
                    } else if (fstNmElmnt.getAttribute("name").contentEquals("DescribeSensor")) {
                        setGetCapabilitiesOperationsMetaData(fstNmElmnt);
                    }
                }
            }
        }
    }

    public void setGetCapabilitiesOperationsMetaData(Element fstNmElmnt) {
        //set get capabilities GET request link
        NodeList fstNm1 = fstNmElmnt.getElementsByTagName("ows:Get");
        Element fstNmElmnt1 = (Element) fstNm1.item(0);
        fstNmElmnt1.setAttribute("xlink:href", threddsURI);

        //set get capabilities Post request link
        NodeList fstNm12 = fstNmElmnt.getElementsByTagName("ows:Post");
        Element fstNmElmnt12 = (Element) fstNm12.item(0);
        fstNmElmnt12.setAttribute("xlink:href", threddsURI);
    }

    /**
     * parses the observation list object and add the observations to the node
     * main location for parsing CDM get caps response 
     */
    public void parseObservationList() throws IOException {
        List<VariableSimpleIF> variableList = null;
        List<String> observedPropertyList = null;
        
        // check for null feature type and return error if it is
        if(getDatasetFeatureType() == null) {
            setDocument(XMLDomUtils.getExceptionDom("Invalid or unknown feature type"));
            return;
        }

        if (getDatasetFeatureType() != FeatureType.GRID) {
            variableList = DiscreteSamplingGeometryUtil.getDataVariables(getFeatureDataset());
            observedPropertyList = new ArrayList<String>(variableList.size());
            List<String> observedPropertyUnitList = new ArrayList<String>(variableList.size());

            for (VariableSimpleIF variable : variableList) {
                observedPropertyList.add(variable.getShortName()); // TODO ? getName() instead?
                observedPropertyUnitList.add(variable.getUnitsString());
            }
        }

        //***************************************
        
        // use CDM to get, getCaps;
        switch (getDatasetFeatureType()) {
            case TRAJECTORY:
                try {
                    setDocument(Trajectory.getCapsResponse(getFeatureTypeDataSet(), getDocument(), getFeatureOfInterestBase(), getGMLNameBase(), format, observedPropertyList));
                } catch (Exception e) {
                }
                break;
            case STATION:
                setDocument(TimeSeries.getCapsResponse((StationTimeSeriesFeatureCollection)getFeatureTypeDataSet(),getDocument(),getFeatureOfInterestBase(),getGMLNameBase(),format,observedPropertyList));
                break;
            case STATION_PROFILE:
                setDocument(TimeSeriesProfile.getCapsResponse((StationProfileFeatureCollection)getFeatureTypeDataSet(),getDocument(),getFeatureOfInterestBase(),getGMLNameBase(),format,observedPropertyList));
                break;
            case PROFILE:
                setDocument(Profile.getCapsResponse((ProfileFeatureCollection)getFeatureTypeDataSet(),getDocument(),getFeatureOfInterestBase(),getGMLNameBase(),format,observedPropertyList));
                break;
            case GRID:
                setDocument(Grid.getCapsResponse(getGridDataset(), getDocument(), getGMLNameBase(), format));
                break;
            default:
                if (getDatasetFeatureType() != null) {
                    output.setupExceptionOutput("Unsupported feature type for request of GetCapabilities: " + getDatasetFeatureType().name());
                } else {
                    output.setupExceptionOutput("Null feature type for request of GetCapabilities");
                }
                break;
        }
    }
}
