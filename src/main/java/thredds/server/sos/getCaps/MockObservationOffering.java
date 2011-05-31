/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thredds.server.sos.getCaps;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Abird
 */
public class MockObservationOffering implements ObservationOffering {
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
    private List obsPropsList;

    public MockObservationOffering() {
    setAllMockFields();
    }

     private void setAllMockFields() {
        setObservationStationID("station-aa0101");
        setObservationStationDescription("OSTEP1");
        setObservationName("urn:ioos:station:NOAA.NOS.CO-OPS:aa0101");
        setObservationSrsName("urn:ogc:def:crs:epsg::4326");
        setObservationStationLowerCorner("36.8572", "-76.308327");
        setObservationStationUpperCorner("36.8572", "-76.308327");
        setObservationTimeBegin("2009-03-24T19:18:00Z");
        setObservationTimeEnd("");
        setObservationProcedureLink("urn:ioos:station:NOAA.NOS.CO-OPS:aa0101");
        setObservationObservedProperty("http://mmisw.org/ont/cf/parameter/currents");
        setObservationFeatureOfInterest("urn:ioos:station:NOAA.NOS.CO-OPS:aa0101");
        setObservationFormat("text/csv");
        setObservationModel("om:Observation");
        setObservationResponseMode("inline");
        
        List list = new ArrayList();
        list.add("http://mmisw.org/ont/cf/parameter/currents");
        setObservationObserveredList(list);
    }

    public String getObservationStationID() {
      return stationID;
    }

    public void setObservationStationID(String stationID) {
        this.stationID = stationID;
    }

    public String getObservationStationDescription() {
       return stationDescription;
    }

    public void setObservationStationDescription(String stationDescription) {
        this.stationDescription = stationDescription;
    }

    public String getObservationName() {
       return stationName;
    }

    public void setObservationName(String obsName) {
      this.stationName = obsName;
    }

    public String getObservationSrsName() {
        return srsName;
    }

    public void setObservationSrsName(String obsSrsName) {
       this.srsName = obsSrsName;
    }

    public String getObservationStationLowerCorner() {
       return lowerCornerLatLon;
    }

    public void setObservationStationLowerCorner(String lat, String lon) {
        this.lowerCornerLatLon = lat.concat(lon);
    }

    public String getObservationStationUpperCorner() {
        return upperCornerLatLon;
    }

    public void setObservationStationUpperCorner(String lat, String lon) {
       this.upperCornerLatLon = lat.concat(lon);
    }

    public String getObservationTimeBegin() {
        return timeBegin;
    }

    public void setObservationTimeBegin(String DateTime) {
        this.timeBegin = DateTime;
    }

    public String getObservationTimeEnd() {
       return timeEnd;
    }

    public void setObservationTimeEnd(String DateTime) {
        this.timeEnd = DateTime;
    }

    public String getObservationProcedureLink() {
        return procedureLink;
    }

    public void setObservationProcedureLink(String procedureLink) {
        this.procedureLink = procedureLink;
    }

    public String getObservationObservedProperty() {
        return observedProperty;
    }

    public void setObservationObservedProperty(String observedProperty) {
        this.observedProperty = observedProperty;
    }

    public String getObservationFeatureOfInterest() {
        return featureOfInterest;
    }

    public void setObservationFeatureOfInterest(String FeatureOfInterest) {
        this.featureOfInterest = FeatureOfInterest;
    }

    public String getObservationFormat() {
        return obsResponseFormat;
    }

    public void setObservationFormat(String format) {
        this.obsResponseFormat = format;
    }

    public String getObservationModel() {
       return obsResultModel;
    }

    public void setObservationModel(String obsResultModel) {
       this.obsResultModel = obsResultModel;
    }

    public String getObservationResponseMode() {
        return obsResponseMode;
    }

    public void setObservationResponseMode(String responseMode) {
        this.obsResponseMode = responseMode;
    }

    public List getObservationObserveredList() {
        return obsPropsList;
    }

    public void setObservationObserveredList(List list) {
        this.obsPropsList = list;
    }



}
