package com.asascience.ncsos.service;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.util.DiscreteSamplingGeometryUtil;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Formatter;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.units.DateFormatter;

/**
 * Provides access to the netcdf dataset wrappers that allow for easier access
 * to information that is specific or needed for specific feature types.
 * @author tkunicki
 * @modified scowan
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

    /**
     * Takes in a dataset and wraps it based on its feature type.
     * @param netCDFDataset the dataset being acted on
     * @throws IOException
     */
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

    private NetcdfDataset getNetCDFDataset() {
        return netCDFDataset;
    }

    /**
     * Returns the dataset, wrapped according to its feature type
     * @return wrapped dataset
     */
    public FeatureDataset getFeatureDataset() {
        return featureDataset;
    }

    /**
     * If the dataset has feature type of Grid, this will return the wrapped dataset
     * @return wrapped dataset
     */
    public GridDataset getGridDataset() {
        return gridDataSet;
    }
    
    /**
     * Returns the SOSOutputFormatter being used by the request.
     * @return SOSOutputFormatter
     */
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

    /**
     * Returns the value of the title attribute of the dataset
     * @return @String 
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the value of the history attribute of the dataset
     * @return @String
     */
    public String getHistory() {
        return history;
    }

    /**
     * Returns the value of the institution/orginization attribute of the dataset
     * @return @String
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * Returns the value of the source attribute of the dataset
     * @return
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the value of the description attribute of the dataset
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns base urn of a station procedure
     * @return
     */
    public String getGMLNameBase() {
        return STATION_GML_BASE;
    }

    /**
     * Returns the urn of a station
     * @param stationName the station name to add to the name base
     * @return
     */
    public String getGMLName(String stationName) {
        return STATION_GML_BASE + stationName;
    }

    /**
     * Gets the location of the datatset; allows access to dataset.getLocation()
     * @return
     */
    public String getLocation() {
        return netCDFDataset.getLocation();
    }

    /**
     * Returns the base query url of the feature of interest
     * @return
     */
    public String getFeatureOfInterestBase() {
        if (featureOfInterestBaseQueryURL == null || featureOfInterestBaseQueryURL.contentEquals("null")) {
            return "";
        }
        return featureOfInterestBaseQueryURL;
    }

    /**
     * Adds the station name to the feature of interest base
     * @param stationName name of the station to add
     * @return the feature of interest base query url with the station
     */
    public String getFeatureOfInterest(String stationName) {
        return featureOfInterestBaseQueryURL == null
                ? stationName
                : featureOfInterestBaseQueryURL + stationName;
    }

    /**
     * Formats degree, using a number formatter
     * @param degree a number to format to a degree
     * @return the number as a degree
     */
    public static String formatDegree(double degree) {
        return FORMAT_DEGREE.format(degree);
    }

    private static String formatDateTimeISO(Date date) {
        // assuming not thread safe...  true?
        return (new DateFormatter()).toDateTimeStringISO(date);
    }

    private void finished() {
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
