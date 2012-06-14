package thredds.server.sos.CDMClasses;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMException;
import thredds.server.sos.getObs.ObservationOffering;

/**
 * RPS - ASA
 * @author abird
 * @version
 * 
 * this class is for common util functions that are used across the CDM classes
 * 
 */
public class CDMUtils {
    
    
    /**
     * Add the offering to the get caps document
     * @param offering
     * @param document
     * @return 
     */
    public static Document addObsOfferingToDoc(ObservationOffering offering,Document document ) {

        NodeList obsOfferingList = document.getElementsByTagName("ObservationOfferingList");
        Element obsOfferEl = (Element) obsOfferingList.item(0);
        obsOfferEl.appendChild(constructObsOfferingNodes(offering,document));
        offering = null;
        return document;
    }

    /**
     * constructs the node to be added to the document
     * @param offering
     * @param document
     * @return 
     */
    public static Element constructObsOfferingNodes(ObservationOffering offering,Document document) {
        //Create the observation offering
        Element obsOfferingEl = document.createElement("ObservationOffering");
        //add the station ID to the created element
        obsOfferingEl.setAttribute("gml:id", offering.getObservationStationID());

        //create the description and add the offering info
        Element obsOfferingDescripEl = document.createElement("gml:description");
        obsOfferingDescripEl.appendChild(document.createTextNode(offering.getObservationStationDescription()));

        //create the obs name and add it to the element
        Element obsOfferingNameEl = document.createElement("gml:name");
        obsOfferingNameEl.appendChild(document.createTextNode(offering.getObservationName()));

        //create the source name el and add data
        Element obsOfferingSrsNameEl = document.createElement("gml:srsName");
        obsOfferingSrsNameEl.appendChild(document.createTextNode(offering.getObservationSrsName()));

        //create bounded area node
        Element obsOfferingBoundedByEl = document.createElement("gml:boundedBy");
        // create the envelope node and add attribute srs name
        Element obsOfferingEnvelopeEl = document.createElement("gml:Envelope");
        obsOfferingEnvelopeEl.setAttribute("srsName", offering.getObservationSrsName());
        //create the lower coner node
        Element obsOfferinglowerCornerEl = document.createElement("gml:lowerCorner");
        obsOfferinglowerCornerEl.appendChild(document.createTextNode(offering.getObservationStationLowerCorner()));
        //create the upper corner node
        Element obsOfferingUpperCornerEl = document.createElement("gml:upperCorner");
        obsOfferingUpperCornerEl.appendChild(document.createTextNode(offering.getObservationStationUpperCorner()));

        //add the upper and lower to the envelope node
        obsOfferingEnvelopeEl.appendChild(obsOfferinglowerCornerEl);
        obsOfferingEnvelopeEl.appendChild(obsOfferingUpperCornerEl);
        //add the envelope node to the bounded by node
        obsOfferingBoundedByEl.appendChild(obsOfferingEnvelopeEl);

        //create time node
        Element obsOfferingTimeEl = document.createElement("time");
        //create time period node
        Element obsOfferingTimePeriodEl = document.createElement("gml:TimePeriod");
        //create begin position node
        Element obsOfferingTimeBeginEl = document.createElement("gml:beginPosition");
        obsOfferingTimeBeginEl.appendChild(document.createTextNode(offering.getObservationTimeBegin()));
        //create end position node
        Element obsOfferingTimeEndEl = document.createElement("gml:endPosition");
        checkEndDateElementNode(offering, obsOfferingTimeEndEl,document);

        //add time begin to time period
        obsOfferingTimePeriodEl.appendChild(obsOfferingTimeBeginEl);
        //add time end to time period
        obsOfferingTimePeriodEl.appendChild(obsOfferingTimeEndEl);
        //add time period to time
        obsOfferingTimeEl.appendChild(obsOfferingTimePeriodEl);

        //create procedure node and add element
        Element obsOfferingProcedureEl = document.createElement("procedure");
        obsOfferingProcedureEl.setAttribute("xlink:href", offering.getObservationProcedureLink());

        //create feature of interest node and add element
        Element obsOfferingFeatureOfInterestEl = document.createElement("featureOfInterest");
        obsOfferingFeatureOfInterestEl.setAttribute("xlink:href", offering.getObservationFeatureOfInterest());

        //create response format
        Element obsOfferingFormatEl = document.createElement("responseFormat");
        obsOfferingFormatEl.appendChild(document.createTextNode(offering.getObservationFormat()));

        //create response model
        Element obsOfferingModelEl = document.createElement("responseModel");
        obsOfferingModelEl.appendChild(document.createTextNode(offering.getObservationModel()));

        //create response model
        Element obsOfferingModeEl = document.createElement("responseMode");
        obsOfferingModeEl.appendChild(document.createTextNode(offering.getObservationResponseMode()));

        //add the new elements to the XML doc
        obsOfferingEl.appendChild(obsOfferingDescripEl);
        obsOfferingEl.appendChild(obsOfferingNameEl);
        obsOfferingEl.appendChild(obsOfferingSrsNameEl);
        obsOfferingEl.appendChild(obsOfferingBoundedByEl);
        obsOfferingEl.appendChild(obsOfferingTimeEl);
        obsOfferingEl.appendChild(obsOfferingProcedureEl);

        //create obs property node and add element
        for (int i = 0; i < offering.getObservationObserveredList().size(); i++) {
            Element obsOfferingObsPropertyEll = document.createElement("observedProperty");
            obsOfferingObsPropertyEll.setAttribute("xlink:href", (String) offering.getObservationObserveredList().get(i));
            obsOfferingEl.appendChild(obsOfferingObsPropertyEll);
        }

        obsOfferingEl.appendChild(obsOfferingFeatureOfInterestEl);
        obsOfferingEl.appendChild(obsOfferingFormatEl);
        obsOfferingEl.appendChild(obsOfferingModelEl);
        obsOfferingEl.appendChild(obsOfferingModeEl);
        return obsOfferingEl;
    }
 
    /**
     * Checks the end node for an end date
     * @param offering
     * @param obsOfferingTimeEndEl
     * @param document
     * @throws DOMException 
     */
    private static void checkEndDateElementNode(ObservationOffering offering, Element obsOfferingTimeEndEl,Document document) throws DOMException {
        //check the string to see if it either needs attribute of element
        if ((offering.getObservationTimeEnd().isEmpty()) || (offering.getObservationTimeEnd().length() < 2) || (offering.getObservationTimeEnd().contentEquals(""))) {
            obsOfferingTimeEndEl.setAttribute("indeterminatePosition", "unknown");
        } else {
            obsOfferingTimeEndEl.appendChild(document.createTextNode(offering.getObservationTimeEnd()));
        }
    }
    
}
