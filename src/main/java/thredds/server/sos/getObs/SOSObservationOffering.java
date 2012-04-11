/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thredds.server.sos.getObs;

import java.util.List;

/**
 * This object is an observation offering object containing all the info needed for the section
 * @author abird
 */
public final class SOSObservationOffering implements ObservationOffering {
   private String stationID;
    private String stationDescription;
    private String stationName;
    private String srsName;
    private String lowerCornerLatLon;
    private String upperCornerLatLon;
    private String timeBegin;
    private String timeEnd;
    private String procedureLink;
    private String observedProperty;
    private String featureOfInterest;
    private String obsResponseFormat;
    private String obsResultModel;
    private String obsResponseMode;
    private List observeredProperties;

    public SOSObservationOffering() {
        setObservationStationID(" ");
        setObservationStationDescription(" ");
        setObservationName(" ");
        setObservationSrsName(" ");
        setObservationStationLowerCorner(" ", " ");
        setObservationStationUpperCorner(" ", " ");
        setObservationTimeBegin(" ");
        setObservationTimeEnd(" ");
        setObservationProcedureLink(" ");
        setObservationObservedProperty(" ");
        setObservationFeatureOfInterest(" ");
        setObservationFormat(" ");
        setObservationModel(" ");
        setObservationResponseMode(" ");
    }

    @Override
    public List getObservationObserveredList(){
        return observeredProperties;
    }

    @Override
    public void setObservationObserveredList(List list){
        this.observeredProperties = list;
    }

    @Override
     public String getObservationStationID() {
      return stationID;
    }

    @Override
    public void setObservationStationID(String stationID) {
        this.stationID = stationID;
    }

    @Override
    public String getObservationStationDescription() {
       return stationDescription;
    }

    @Override
    public void setObservationStationDescription(String stationDescription) {
        this.stationDescription = stationDescription;
    }

    @Override
    public String getObservationName() {
       return stationName;
    }

    @Override
    public void setObservationName(String obsName) {
      this.stationName = obsName;
    }

    @Override
    public String getObservationSrsName() {
        return srsName;
    }

    @Override
    public void setObservationSrsName(String obsSrsName) {
       this.srsName = obsSrsName;
    }

    @Override
    public String getObservationStationLowerCorner() {
       return lowerCornerLatLon;
    }

    @Override
    public void setObservationStationLowerCorner(String lat, String lon) {
        this.lowerCornerLatLon = lat.concat(" "+lon);
    }

    @Override
    public String getObservationStationUpperCorner() {
        return upperCornerLatLon;
    }

    @Override
    public void setObservationStationUpperCorner(String lat, String lon) {
       this.upperCornerLatLon = lat.concat(" "+lon);
    }

    @Override
    public String getObservationTimeBegin() {
        return timeBegin;
    }

    @Override
    public void setObservationTimeBegin(String DateTime) {
        this.timeBegin = DateTime;
    }

    @Override
    public String getObservationTimeEnd() {
       return timeEnd;
    }

    @Override
    public void setObservationTimeEnd(String DateTime) {
        this.timeEnd = DateTime;
    }

    @Override
    public String getObservationProcedureLink() {
        return procedureLink;
    }

    @Override
    public void setObservationProcedureLink(String procedureLink) {
        this.procedureLink = procedureLink;
    }

    @Override
    public String getObservationObservedProperty() {
        return observedProperty;
    }

    @Override
    public void setObservationObservedProperty(String observedProperty) {
        this.observedProperty = observedProperty;
    }

    @Override
    public String getObservationFeatureOfInterest() {
        return featureOfInterest;
    }

    @Override
    public void setObservationFeatureOfInterest(String FeatureOfInterest) {
        this.featureOfInterest = FeatureOfInterest;
    }

    @Override
    public String getObservationFormat() {
        return obsResponseFormat;
    }

    @Override
    public void setObservationFormat(String format) {
        this.obsResponseFormat = format;
    }

    @Override
    public String getObservationModel() {
       return obsResultModel;
    }

    @Override
    public void setObservationModel(String obsResultModel) {
       this.obsResultModel = obsResultModel;
    }

    @Override
    public String getObservationResponseMode() {
        return obsResponseMode;
    }

    @Override
    public void setObservationResponseMode(String responseMode) {
        this.obsResponseMode = responseMode;
    }

}
