package com.asascience.ncsos.getcaps;

import com.asascience.ncsos.cdmclasses.*;
import com.asascience.ncsos.outputformatter.GetCapsOutputter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.DiscreteSamplingGeometryUtil;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.*;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.ProfileFeatureCollection;
import ucar.nc2.ft.StationProfileFeatureCollection;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;

/**
 * Creates basic Get Capabilites request handler that can read from a netcdf dataset
 * the information needed to populate a get capabilities template.
 * @author Abird
 * @version 1.0.0
 */
public class SOSGetCapabilitiesRequestHandler extends SOSBaseRequestHandler {

    private final String threddsURI;
    
    private static final String OWS = "http://www.opengis.net/ows/1.1";

    /**
     * Creates an instance of SOSGetCapabilitiesRequestHandler to handle the dataset
     * and uri from the thredds request.
     * @param netCDFDataset dataset for which the Get Capabilities request is being
     * directed to
     * @param threddsURI uri from the thredds Get Capabilities request
     * @throws IOException
     */
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
                
                fstElmnt.getElementsByTagName("ows:AccessConstraints").item(0).setNodeValue(Access);
            }
        }
    }

    private NodeList getXMLNode(Element fstElmnt, String xmlLocation) {
        NodeList tagNameNodeList = fstElmnt.getElementsByTagName(xmlLocation);
        Element fstNmElmnt1 = (Element) tagNameNodeList.item(0);
        NodeList fstNm1 = fstNmElmnt1.getChildNodes();
        return fstNm1;
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

        // set org info
        // url
        getXMLNode(fstElmnt, "ows:ProviderSite").item(0).setNodeValue(DataPage);
        // name
        getXMLNode(fstElmnt, "ows:ProviderName").item(0).setNodeValue(PrimaryOwnership);
    }

    /**
     * 
     */
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
                        setGetCapabilitiesDescribeSensorMetadata(fstNmElmnt);
                    }
                }
            }
        }
    }
    
    private void setGetCapabilitiesDescribeSensorMetadata(Element firstNameElement) {
        // set request link
        setGetCapabilitiesOperationsMetaData(firstNameElement);
        // set procedure allowed values of each station and sensor for the dataset
        Element procedure = null;
        NodeList nodes = firstNameElement.getElementsByTagName("ows:Parameter");
        for (int i=0; i<nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            if (elem.getAttribute("name").equalsIgnoreCase("procedure")) {
                procedure = elem;
                break;
            }
        }
        if (procedure != null && getStationNames() != null) {
            // add allowed values node
            Element allowedValues = getDocument().createElement("ows:AllowedValues");
            procedure.appendChild(allowedValues);
            for (String stationName : getStationNames().values()) {
                Element elem = getDocument().createElement("ows:Value");
                elem.setTextContent(getGMLName(stationName));
                allowedValues.appendChild(elem);
                for (String senName : getSensorNames()) {
                    Element sElem = getDocument().createElement("ows:Value");
                    sElem.setTextContent(getSensorGMLName(stationName, senName));
                    allowedValues.appendChild(sElem);
                }
            }
        }
    }

    private void setGetCapabilitiesOperationsMetaData(Element fstNmElmnt) {
        //set get capabilities GET request link
        NodeList fstNm1 = fstNmElmnt.getElementsByTagName("ows:Get");
        Element fstNmElmnt1 = (Element) fstNm1.item(0);
        fstNmElmnt1.setAttribute("xlink:href", threddsURI);

        //set get capabilities Post request link
//        NodeList fstNm12 = fstNmElmnt.getElementsByTagName("ows:Post");
//        Element fstNmElmnt12 = (Element) fstNm12.item(0);
//        if (fstNmElmnt12 != null)
//            fstNmElmnt12.setAttribute("xlink:href", threddsURI);
    }

    /**
     * parses the observation list object and add the observations to the node
     * main location for parsing CDM get caps response 
     * @throws IOException 
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
                    setDocument(Trajectory.getCapsResponse(getFeatureTypeDataSet(), getDocument(), getFeatureOfInterestBase(), getGMLNameBase(), observedPropertyList));
                } catch (Exception e) {
                }
                break;
            case STATION:
                setDocument(TimeSeries.getCapsResponse((StationTimeSeriesFeatureCollection)getFeatureTypeDataSet(),getDocument(),getFeatureOfInterestBase(),getGMLNameBase(),observedPropertyList));
                break;
            case STATION_PROFILE:
                setDocument(TimeSeriesProfile.getCapsResponse((StationProfileFeatureCollection)getFeatureTypeDataSet(),getDocument(),getFeatureOfInterestBase(),getGMLNameBase(),observedPropertyList));
                break;
            case PROFILE:
                setDocument(Profile.getCapsResponse((ProfileFeatureCollection)getFeatureTypeDataSet(),getDocument(),getFeatureOfInterestBase(),getGMLNameBase(),observedPropertyList));
                break;
            case GRID:
                setDocument(Grid.getCapsResponse(getGridDataset(), getDocument(), getGMLNameBase()));
                break;
            case SECTION:
                setDocument(Section.getCapsResponse(getFeatureTypeDataSet(), getDocument(), getFeatureOfInterestBase(), getGMLNameBase(), observedPropertyList));
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
