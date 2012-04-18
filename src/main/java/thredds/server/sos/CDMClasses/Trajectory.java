package thredds.server.sos.CDMClasses;

import java.io.IOException;
import java.util.List;
import ucar.unidata.geoloc.Station;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMException;
import thredds.server.sos.getObs.ObservationOffering;
import thredds.server.sos.getObs.SOSObservationOffering;
import thredds.server.sos.service.StationData;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.TrajectoryFeature;
import ucar.nc2.ft.TrajectoryFeatureCollection;

/**
 * RPS - ASA
 * @author abird
 * @version 
 *
 * 
 *
 */
public class Trajectory extends baseCDMClass implements iStationData {

    @Override
    public void setData(Object featureCollection) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setInitialLatLonBounaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDataResponse(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getStationName(int idNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getLowerLat(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getLowerLon(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getUpperLat(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getUpperLon(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTimeEnd(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTimeBegin(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * gets the trajectory response for the getcaps request
     * @param dataset
     * @param document
     * @param featureOfInterest
     * @param GMLName
     * @param format
     * @param observedPropertyList
     * @return
     * @throws IOException 
     */
    public static Document getResponse(FeatureCollection dataset,Document document,String featureOfInterest,String GMLName,String format,List<String> observedPropertyList) throws IOException {
        //PointFeatureIterator trajIter;


        while (((TrajectoryFeatureCollection) dataset).hasNext()) {
            TrajectoryFeature tFeature = ((TrajectoryFeatureCollection) dataset).next();
            tFeature.calcBounds();
            
            //trajIter = tFeature.getPointFeatureIterator(-1);
            //attributes
            SOSObservationOffering newOffering = new SOSObservationOffering();
            newOffering.setObservationStationLowerCorner(Double.toString(tFeature.getBoundingBox().getLatMin()), Double.toString(tFeature.getBoundingBox().getLonMin()));
            newOffering.setObservationStationUpperCorner(Double.toString(tFeature.getBoundingBox().getLatMax()), Double.toString(tFeature.getBoundingBox().getLonMax()));

            //check the data
            if (tFeature.getDateRange() != null) {
                newOffering.setObservationTimeBegin(tFeature.getDateRange().getStart().toDateTimeStringISO());
                newOffering.setObservationTimeEnd(tFeature.getDateRange().getEnd().toDateTimeStringISO());
            } //find the dates out!
            else {
                System.out.println("no dates yet");
            }
            
            newOffering.setObservationStationDescription(tFeature.getCollectionFeatureType().toString());
            newOffering.setObservationFeatureOfInterest(featureOfInterest+(tFeature.getName()));
            newOffering.setObservationName(GMLName+(tFeature.getName()));
            newOffering.setObservationStationID((tFeature.getName()));
            newOffering.setObservationProcedureLink(GMLName+((tFeature.getName())));
            newOffering.setObservationSrsName("EPSG:4326");  // TODO?  
            newOffering.setObservationObserveredList(observedPropertyList);
            newOffering.setObservationFormat(format);
            
            document = CDMUtils.addObsOfferingToDoc(newOffering,document);
        }

        return document;
    }

    @Override
    public String getDescription(int stNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
