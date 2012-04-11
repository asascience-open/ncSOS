/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thredds.server.sos.getObs;

import java.util.List;

/**
 *
 * @author Abird
 */
public interface ObservationOffering {
    
/**
 * gets the station ID
 * @return @String
 */
    public String getObservationStationID();
    public void setObservationStationID(String stationID);

/**
 * gets the description
 * @return @String
 */
    public String getObservationStationDescription();
    public void setObservationStationDescription(String stationDescription);

    public List getObservationObserveredList();
    public void setObservationObserveredList(List list);
    
    public String getObservationName();
    public void setObservationName(String obsName);

    public String getObservationSrsName();
    public void setObservationSrsName(String obsSrsName);

    public String getObservationStationLowerCorner();
    public void setObservationStationLowerCorner(String lat,String lon);

    public String getObservationStationUpperCorner();
    public void setObservationStationUpperCorner(String lat,String lon);

    public String getObservationTimeBegin();
    public void setObservationTimeBegin(String DateTime);

    public String getObservationTimeEnd();
    public void setObservationTimeEnd(String DateTime);

    /**
     * adds the link as an attribute
     * @return @String HTTP Link
     */
    public String getObservationProcedureLink();
    public void setObservationProcedureLink(String procedureLink);

     /**
     * adds the link as an attribute
     * @return @String HTTP Link
     */
    public String getObservationObservedProperty();
    public void setObservationObservedProperty(String observedProperty);

     /**
     * adds the link as an attribute
     * @return @String HTTP Link
     */
    public String getObservationFeatureOfInterest();
    public void setObservationFeatureOfInterest(String FeatureOfInterest);

    public String getObservationFormat();
    public void setObservationFormat(String format);

    public String getObservationModel();
    public void setObservationModel(String obsResultModel);

    public String getObservationResponseMode();
    public void setObservationResponseMode(String responseMode);
}
