/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.ncsos.getobs;

import java.util.List;

/**
 * Interface of getters and setter for the SOSObservationOffering struct. See
 * SOSObservationOffering for documentation on each function.
 * @author Abird
 */
public interface ObservationOffering {
    
    public String getObservationStationID();
    public void setObservationStationID(String stationID);
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
    public String getObservationProcedureLink();
    public void setObservationProcedureLink(String procedureLink);
    public String getObservationObservedProperty();
    public void setObservationObservedProperty(String observedProperty);
    public String getObservationFeatureOfInterest();
    public void setObservationFeatureOfInterest(String FeatureOfInterest);
    public String getObservationModel();
    public void setObservationModel(String obsResultModel);
    public String getObservationResponseMode();
    public void setObservationResponseMode(String responseMode);
}
