/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.getCaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.w3c.dom.DOMException;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import thredds.server.sos.service.DatasetMetaData;


/**
 *
 * @author Abird
 */
public class MockGetCapabilitiesParser {

    private Document doc;
    private String routeElement;

    String templateLocation = "templates/sosGetCapabilities.xml";
    InputStream isTemplate = getClass().getClassLoader().getResourceAsStream(templateLocation);


    private final DatasetMetaData dst;
    private ObservationOffering obsOffer;

    public MockGetCapabilitiesParser(DatasetMetaData dst) {
        this.dst = dst;
    }



    MockGetCapabilitiesParser() {
        this.dst = new DatasetMetaData();
        dst.setTitle("Title");
        dst.setHistory("History");
        dst.setInstitution("Institution");
        dst.setSource("Source");
        dst.setInstitution("ASA");
        dst.setLocation("Location");
    }


    public InputStream getTemplateStream(){
        return isTemplate;
    }

    public String getTemplateLocation(){
      return templateLocation;
    }

    public String getRouteElement() {
        return routeElement;
    }

    public void parseTemplateXML() {
        try {   
            //File file = new File(templateFileLocation);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(isTemplate);
            doc.getDocumentElement().normalize();
            setRouteElement(doc.getDocumentElement().getNodeName());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Document getDom() {
        return doc;
    }

    public void parseServiceIdentification() {
        NodeList nodeLst = doc.getElementsByTagName("ows:ServiceIdentification");

        for (int s = 0; s < nodeLst.getLength(); s++) {

            Node fstNode = nodeLst.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                //looks at the one node
                Element fstElmnt = (Element) fstNode;
                //looks at title
                NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("ows:Title");
                Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                NodeList fstNm = fstNmElmnt.getChildNodes();
                fstNm.item(0).setNodeValue(getTitleSI());

                //looks at the adstract
                NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("ows:Abstract");
                Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
                NodeList lstNm = lstNmElmnt.getChildNodes();
                if (getAbstractSI() == null) {
                    lstNm.item(0).setNodeValue("");
                    dst.setSource("");
                } else {
                    lstNm.item(0).setNodeValue(getAbstractSI());
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
            obsOfferingTimeEndEl.appendChild(doc.createTextNode(offering.getObservationTimeEnd()));
        }
    }

    private void setProviderName(Element fstElmnt, String xmlLocation) throws DOMException {
        //get the node named be the string
        NodeList fstNm1 = getXMLNode(fstElmnt, xmlLocation);
        if (getProviderNameSP() == null) {
            fstNm1.item(0).setNodeValue("");
            dst.setSource("");
        } else {
            fstNm1.item(0).setNodeValue(getProviderNameSP());
        }
    }

    private void setProviderSite(Element fstElmnt, String xmlLocation) throws DOMException {
        NodeList fstNm1 = getXMLNode(fstElmnt, xmlLocation);
        if (getProviderSiteSP() == null) {
            fstNm1.item(0).setNodeValue("");
            dst.setInstitution("");
        } else {
            fstNm1.item(0).setNodeValue(getProviderSiteSP());
        }
    }

    private void setRouteElement(String routeElement) {
        this.routeElement = routeElement;
    }

    public void writeXMLDOMToFile(Document doc, String filename) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // Prepare the output file
            File file = new File(filename);
            Result result = new StreamResult(file);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    //
    //TODO ADD THESE TO MOCK GET CAPS RESULT!!!!!!
    //
    public String getTitleSI() {
        return dst.getTitle();
    }

    public String getAbstractSI() {
        return dst.getHistory();
    }

    public String getProviderNameSP() {
        return dst.getSource();
    }

    public String getProviderSiteSP() {
        return dst.getInstitution();
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
        return dst.getThreddsPath();
    }

    //
    //TODO ADD THESE TO MOCK GET CAPS RESULT!!!!!!
    //
    public void parseServiceDescription() {
        //get service provider node list
        NodeList serviceProviderNodeList = doc.getElementsByTagName("ows:ServiceProvider");
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
        NodeList operationsNodeList = doc.getElementsByTagName("ows:OperationsMetadata");
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
        fstNmElmnt1.setAttribute("xlink:href", getHTTPGetAddress());

        //set get capabilities Post request link
        NodeList fstNm12 = fstNmElmnt.getElementsByTagName("ows:Post");
        Element fstNmElmnt12 = (Element) fstNm12.item(0);
        fstNmElmnt12.setAttribute("xlink:href", getHTTPGetAddress());
    }

    public void parseMockObservationOffering() {
        //get the observation offerings for the list
        ObservationOffering offering = getMockObservationOffering();
        addObsOfferingToDoc(offering);
    }

    public void parseObservationOffering() {
        ObservationOffering offering = getObservationOffering();
        addObsOfferingToDoc(offering);
    }

    public ObservationOffering getObservationOffering() {
        return obsOffer;
    }

    public void setObservationOffering(ObservationOffering obsOffer) {
        this.obsOffer = obsOffer;
    }

    public ObservationOffering getMockObservationOffering() {
        return new MockObservationOffering();
    }

    /**
     * parses the observation list object and add the observations to the node
     */
    public void parseObservationList() {
        List<ObservationOffering> obsList = dst.getObservationOfferingList();
        for (int i = 0; i < obsList.size(); i++) {
            ObservationOffering offering = obsList.get(i);
            addObsOfferingToDoc(offering);
        }
    }

    public void addObsOfferingToDoc(ObservationOffering offering) {

        NodeList obsOfferingList = doc.getElementsByTagName("ObservationOfferingList");

        Element obsOfferEl = (Element) obsOfferingList.item(0);

        obsOfferEl.appendChild(constructObsOfferingNodes(offering));

    }

    public Element constructObsOfferingNodes(ObservationOffering offering) {
        //Create the observation offering
        Element obsOfferingEl = doc.createElement("ObservationOffering");
        //add the station ID to the created element
        obsOfferingEl.setAttribute("gml:id", offering.getObservationStationID());

        //create the description and add the offering info
        Element obsOfferingDescripEl = doc.createElement("gml:description");
        obsOfferingDescripEl.appendChild(doc.createTextNode(offering.getObservationStationDescription()));

        //create the obs name and add it to the element
        Element obsOfferingNameEl = doc.createElement("gml:name");
        obsOfferingNameEl.appendChild(doc.createTextNode(offering.getObservationName()));

        //create the source name el and add data
        Element obsOfferingSrsNameEl = doc.createElement("gml:srsName");
        obsOfferingSrsNameEl.appendChild(doc.createTextNode(offering.getObservationSrsName()));

        //create bounded area node
        Element obsOfferingBoundedByEl = doc.createElement("gml:boundedBy");
        // create the envelope node and add attribute srs name
        Element obsOfferingEnvelopeEl = doc.createElement("gml:Envelope");
        obsOfferingEnvelopeEl.setAttribute("srsName", offering.getObservationSrsName());
        //create the lower coner node
        Element obsOfferinglowerCornerEl = doc.createElement("gml:lowerCorner");
        obsOfferinglowerCornerEl.appendChild(doc.createTextNode(offering.getObservationStationLowerCorner()));
        //create the upper corner node
        Element obsOfferingUpperCornerEl = doc.createElement("gml:upperCorner");
        obsOfferingUpperCornerEl.appendChild(doc.createTextNode(offering.getObservationStationUpperCorner()));

        //add the upper and lower to the envelope node
        obsOfferingEnvelopeEl.appendChild(obsOfferinglowerCornerEl);
        obsOfferingEnvelopeEl.appendChild(obsOfferingUpperCornerEl);
        //add the envelope node to the bounded by node
        obsOfferingBoundedByEl.appendChild(obsOfferingEnvelopeEl);

        //create time node
        Element obsOfferingTimeEl = doc.createElement("time");
        //create time period node
        Element obsOfferingTimePeriodEl = doc.createElement("gml:TimePeriod");
        //create begin position node
        Element obsOfferingTimeBeginEl = doc.createElement("gml:beginPosition");
        obsOfferingTimeBeginEl.appendChild(doc.createTextNode(offering.getObservationTimeBegin()));
        //create end position node
        Element obsOfferingTimeEndEl = doc.createElement("gml:endPosition");
        checkEndDateElementNode(offering, obsOfferingTimeEndEl);

        //add time begin to time period
        obsOfferingTimePeriodEl.appendChild(obsOfferingTimeBeginEl);
        //add time end to time period
        obsOfferingTimePeriodEl.appendChild(obsOfferingTimeEndEl);
        //add time period to time
        obsOfferingTimeEl.appendChild(obsOfferingTimePeriodEl);

        //create procedure node and add element
        Element obsOfferingProcedureEl = doc.createElement("procedure");
        obsOfferingProcedureEl.setAttribute("xlink:href", offering.getObservationProcedureLink());

        //create feature of interest node and add element
        Element obsOfferingFeatureOfInterestEl = doc.createElement("featureOfInterest");
        obsOfferingFeatureOfInterestEl.setAttribute("xlink:href", offering.getObservationFeatureOfInterest());

        //create response format
        Element obsOfferingFormatEl = doc.createElement("responseFormat");
        obsOfferingFormatEl.appendChild(doc.createTextNode(offering.getObservationFormat()));

        //create response model
        Element obsOfferingModelEl = doc.createElement("responseModel");
        obsOfferingModelEl.appendChild(doc.createTextNode(offering.getObservationModel()));

        //create response model
        Element obsOfferingModeEl = doc.createElement("responseMode");
        obsOfferingModeEl.appendChild(doc.createTextNode(offering.getObservationResponseMode()));

        //add the new elements to the XML doc
        obsOfferingEl.appendChild(obsOfferingDescripEl);
        obsOfferingEl.appendChild(obsOfferingNameEl);
        obsOfferingEl.appendChild(obsOfferingSrsNameEl);
        obsOfferingEl.appendChild(obsOfferingBoundedByEl);
        obsOfferingEl.appendChild(obsOfferingTimeEl);
        obsOfferingEl.appendChild(obsOfferingProcedureEl);

        //create obs property node and add element
        for (int i = 0; i < offering.getObservationObserveredList().size(); i++) {
            Element obsOfferingObsPropertyEll = doc.createElement("observedProperty");
            obsOfferingObsPropertyEll.setAttribute("xlink:href", (String) offering.getObservationObserveredList().get(i));
            obsOfferingEl.appendChild(obsOfferingObsPropertyEll);
        }

        obsOfferingEl.appendChild(obsOfferingFeatureOfInterestEl);
        obsOfferingEl.appendChild(obsOfferingFormatEl);
        obsOfferingEl.appendChild(obsOfferingModelEl);
        obsOfferingEl.appendChild(obsOfferingModeEl);
        return obsOfferingEl;
    }
}
