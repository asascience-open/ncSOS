package thredds.server.sos.service;

import com.asascience.ncSOS.outputFormatters.SOSOutputFormatter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Formatter;
import thredds.server.sos.util.DiscreteSamplingGeometryUtil;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.units.DateFormatter;

/**
 *
 * @author tkunicki
 */
public abstract class SOSBaseRequestHandler {

    private static final String STATION_GML_BASE = "urn:tds:station.sos:";
    private final NetcdfDataset netCDFDataset;
    private FeatureDataset featureDataset;
    private String title;
    private String history;
    private String institution;
    private String source;
    private String description;
    private String featureOfInterestBaseQueryURL;
    private final static NumberFormat FORMAT_DEGREE;
    private FeatureCollection CDMPointFeatureCollection;
    private GridDataset gridDataSet = null;
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSBaseRequestHandler.class);

    static {
        FORMAT_DEGREE = NumberFormat.getNumberInstance();
        FORMAT_DEGREE.setMinimumFractionDigits(1);
        FORMAT_DEGREE.setMaximumFractionDigits(14);
    }
    private FeatureType dataFeatureType;
    protected SOSOutputFormatter output;

    public SOSBaseRequestHandler(NetcdfDataset netCDFDataset) throws IOException {
        if(netCDFDataset == null) {
            _log.error("received null dataset");
        }
        this.netCDFDataset = netCDFDataset;

        featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.ANY, netCDFDataset, null, new Formatter(System.err));
        if (featureDataset == null) {
            _log.info("featureDataset is null, may be a GRID dataset");
        }
        //try and get dataset
        CDMPointFeatureCollection = DiscreteSamplingGeometryUtil.extractFeatureDatasetCollection(featureDataset);
        
        //if its null try using grid?
        if (CDMPointFeatureCollection == null) {
            gridDataSet = DiscreteSamplingGeometryUtil.extractGridDatasetCollection(featureDataset);
            if (gridDataSet != null) {
                _log.info("FeatureType is GRID");
                dataFeatureType = FeatureType.GRID;
            }
            else {
                System.out.println("Uknown feature type!");            
            }
        } else {
            dataFeatureType = CDMPointFeatureCollection.getCollectionFeatureType();
        }

        parseGlobalAttributes();
    }

    private void parseGlobalAttributes() {

        title = netCDFDataset.findAttValueIgnoreCase(null, "title", "Empty Title");
        history = netCDFDataset.findAttValueIgnoreCase(null, "history", "Empty History");
        institution = netCDFDataset.findAttValueIgnoreCase(null, "institution", "Empty Insitution");
        source = netCDFDataset.findAttValueIgnoreCase(null, "source", "Empty Source");
        description = netCDFDataset.findAttValueIgnoreCase(null, "description", "Empty Description");
        featureOfInterestBaseQueryURL = netCDFDataset.findAttValueIgnoreCase(null, "featureOfInterestBaseQueryURL", null);
    }

    public NetcdfDataset getNetCDFDataset() {
        return netCDFDataset;
    }

    public FeatureDataset getFeatureDataset() {
        return featureDataset;
    }

    public GridDataset getGridDataset() {
        return gridDataSet;
    }
    
    public SOSOutputFormatter getOutputHandler() {
        return output;
    }

    /**
     * Gets the dataset currently in use
     * @return feature collection dataset
     */
    public FeatureCollection getFeatureTypeDataSet() {
        return CDMPointFeatureCollection;
    }

    /**
     * gets the feature type of the dataset in question
     * @return feature type of dataset
     */
    public FeatureType getDatasetFeatureType() {
        return dataFeatureType;
    }

    public String getTitle() {
        return title;
    }

    public String getHistory() {
        return history;
    }

    public String getInstitution() {
        return institution;
    }

    public String getSource() {
        return source;
    }

    public String getDescription() {
        return description;
    }

    public String getGMLID(String stationName) {
        return stationName;
    }

    public String getGMLNameBase() {
        return STATION_GML_BASE;
    }

    public String getGMLName(String stationName) {
        return STATION_GML_BASE + stationName;
    }

    public String getLocation() {
        return netCDFDataset.getLocation();
    }

    public String getFeatureOfInterestBase() {
        if (featureOfInterestBaseQueryURL == null || featureOfInterestBaseQueryURL.contentEquals("null")) {
            return "";
        }
        return featureOfInterestBaseQueryURL;
    }

    public String getFeatureOfInterest(String stationName) {
        return featureOfInterestBaseQueryURL == null
                ? stationName
                : featureOfInterestBaseQueryURL + stationName;
    }

    public static String formatDegree(double degree) {
        return FORMAT_DEGREE.format(degree);
    }

    public static String formatDateTimeISO(Date date) {
        // assuming not thread safe...  true?
        return (new DateFormatter()).toDateTimeStringISO(date);
    }

    public void finished() {
        try {
            featureDataset.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            netCDFDataset.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        title = null;
        history = null;;
        institution = null;;
        source = null;
        description = null;
        featureOfInterestBaseQueryURL = null;
        CDMPointFeatureCollection =null;

    }
}
