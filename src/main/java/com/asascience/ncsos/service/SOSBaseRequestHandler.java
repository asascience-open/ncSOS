package com.asascience.ncsos.service;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.util.DiscreteSamplingGeometryUtil;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.ft.*;
import ucar.nc2.units.DateFormatter;

/**
 * Provides access to the netcdf dataset wrappers that allow for easier access
 * to information that is specific or needed for specific feature types.
 * @author tkunicki
 * @modified scowan
 */
public abstract class SOSBaseRequestHandler {

    private static final String STATION_GML_BASE = "urn:tds:station.sos:";
    private static final String SENSOR_GML_BASE = "urn:tds:sensor.sos:";
    private final NetcdfDataset netCDFDataset;
    private FeatureDataset featureDataset;
    private final static NumberFormat FORMAT_DEGREE;
    private FeatureCollection CDMPointFeatureCollection;
    private GridDataset gridDataSet = null;
    
    // Global Attributes
    protected String Access, DataContactEmail, DataContactName, DataContactPhone, DataPage, InventoryContactEmail, InventoryContactName, InventoryContactPhone;
    protected String PrimaryOwnership, Region;
    private String title;
    private String history;
    private String description;
    private String featureOfInterestBaseQueryURL;
    
    // Variables and other information commonly needed
    protected Variable latVariable, lonVariable, timeVariable, depthVariable;
    protected Variable stationVariable;
    private HashMap<Integer, String> stationNames;
    private List<String> sensorNames;
    
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
            _log.error("received null dataset -- probably exception output");
            this.netCDFDataset = null;
            return;
        }
        this.netCDFDataset = netCDFDataset;
        
        if (FeatureDatasetFactoryManager.findFeatureType(netCDFDataset) != null) {
            switch (FeatureDatasetFactoryManager.findFeatureType(netCDFDataset)) {
                case STATION_PROFILE:
                    featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.STATION_PROFILE, netCDFDataset, null, new Formatter(System.err));
                    break;
                case PROFILE:
                    featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.PROFILE, netCDFDataset, null, new Formatter(System.err));
                    break;
                case STATION:
                    featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.STATION, netCDFDataset, null, new Formatter(System.err));
                    break;
                case TRAJECTORY:
                    featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.TRAJECTORY, netCDFDataset, null, new Formatter(System.err));
                    break;
                case SECTION:
                    featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.SECTION, netCDFDataset, null, new Formatter(System.err));
                    break;
                case POINT:
                    featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.POINT, netCDFDataset, null, new Formatter(System.err));
                    break;
                case GRID:
                    featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.GRID, netCDFDataset, null, new Formatter(System.err));
                    gridDataSet = DiscreteSamplingGeometryUtil.extractGridDatasetCollection(featureDataset);
                    dataFeatureType = FeatureType.GRID;
                    break;
                default:
                    featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.ANY_POINT, netCDFDataset, null, new Formatter(System.err));
                    break;
            }
            if (featureDataset == null) {
                featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.ANY_POINT, netCDFDataset, null, new Formatter(System.err));
            }
        } else {
            System.out.println("findfeaturetype is null, somehow... getting dataset from any_point");
            featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.ANY_POINT, netCDFDataset, null, new Formatter(System.err));
            if (featureDataset == null) {
                // null, which means the dataset should be grid...
                System.out.println("Attempting to get GRID data after null findfeaturetype");
                featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.GRID, netCDFDataset, null, new Formatter(System.err));
                gridDataSet = DiscreteSamplingGeometryUtil.extractGridDatasetCollection(featureDataset);
                dataFeatureType = FeatureType.GRID;
            }
        }
        
        System.out.println("Datafeaturetype = " + dataFeatureType);
        
        if (gridDataSet == null && featureDataset == null) {
            System.err.println("Unknown feature type! " + FeatureDatasetFactoryManager.findFeatureType(netCDFDataset));
            return;
        }
        
        if (dataFeatureType == null || dataFeatureType == FeatureType.NONE) {
            CDMPointFeatureCollection = DiscreteSamplingGeometryUtil.extractFeatureDatasetCollection(featureDataset);
            dataFeatureType = CDMPointFeatureCollection.getCollectionFeatureType();
        }
        
        

        parseGlobalAttributes();
        
        findAndParseStationVariable();
        
        // get sensor Variable names
        parseSensorNames();
        
        // get other needed vars
        latVariable = netCDFDataset.findCoordinateAxis(AxisType.Lat);
        lonVariable = netCDFDataset.findCoordinateAxis(AxisType.Lon);
        timeVariable = netCDFDataset.findCoordinateAxis(AxisType.Time);
        depthVariable = netCDFDataset.findCoordinateAxis(AxisType.Height);
    }

    private void parseGlobalAttributes() {
        
        Access = netCDFDataset.findAttValueIgnoreCase(null, "license", "NONE");
        DataContactEmail = netCDFDataset.findAttValueIgnoreCase(null, "publisher_email", "");
        DataContactName = netCDFDataset.findAttValueIgnoreCase(null, "publisher_name", "");
        DataContactPhone = netCDFDataset.findAttValueIgnoreCase(null, "publisher_phone", "");
        DataPage = netCDFDataset.findAttValueIgnoreCase(null, "publisher_url", "");
        InventoryContactEmail = netCDFDataset.findAttValueIgnoreCase(null, "creator_email","");
        InventoryContactName = netCDFDataset.findAttValueIgnoreCase(null,"creator_name","");
        InventoryContactPhone = netCDFDataset.findAttValueIgnoreCase(null,"creator_phone","");
        PrimaryOwnership = netCDFDataset.findAttValueIgnoreCase(null, "source", "");
        Region = netCDFDataset.findAttValueIgnoreCase(null, "institution", "");
        // old attributes, still looking up for now
        title = netCDFDataset.findAttValueIgnoreCase(null, "title", "");
        history = netCDFDataset.findAttValueIgnoreCase(null, "history", "");
        description = netCDFDataset.findAttValueIgnoreCase(null, "description", "");
        featureOfInterestBaseQueryURL = netCDFDataset.findAttValueIgnoreCase(null, "featureOfInterestBaseQueryURL", null);
    }

    private void findAndParseStationVariable() throws IOException {
        // get station var
        // check for station, trajectory, profile and grid station info
        for (Variable var : netCDFDataset.getVariables()) {
            // look for cf_role attr
            if (this.stationVariable == null) {
                for (Attribute attr : var.getAttributes()) {
                    if(attr.getName().equalsIgnoreCase("cf_role")) {
                        this.stationVariable = var;
                        String attrValue = attr.getStringValue().toLowerCase();
                        // parse name based on role
                        if (attrValue.contains("trajectory") && stationVariable.getDataType() == DataType.INT)
                            parseTrajectoryIdsToNames();
                        else if (attrValue.contains("profile") && stationVariable.getDataType() == DataType.INT)
                            parseProfileIdsToNames();
                        else
                            parseStationNames();
                        break;
                    }
                }
            }
            // check name for grid data (does not have cf_role)
            String varName = var.getFullName().toLowerCase();
            if (varName.contains("grid") && varName.contains("name")) {
                this.stationVariable = var;
                parseGridIdsToName();
            }
            
            if (this.stationVariable != null)
                break;
        }
        
        if (this.stationVariable == null && getGridDataset() == null) {
            // there is no station variable ... add a single station with index 0?
//            parseDefaultStationName();
            parseStationNames();
        } else if (this.stationVariable == null) {
            parseGridIdsToName();
        }
    }
    
    /**
     * Finds all variables that are sensor (data) variables and compiles a list of their names.
     * @param dataset the dataset to search through
     */
    private void parseSensorNames() {
        // find all variables who's not a coordinate axis and does not have 'station' in the name
        this.sensorNames = new ArrayList<String>();
        getFeatureDataset().getDataVariables();
        for (Iterator<VariableSimpleIF> it = getFeatureDataset().getDataVariables().iterator(); it.hasNext();) {
            VariableSimpleIF var = it.next();
            this.sensorNames.add(var.getShortName());
        }
    }

    private int parseNestedCollection(NestedPointFeatureCollection npfc, int stationIndex) throws IOException { 
        if (npfc.isMultipleNested()) {
            NestedPointFeatureCollectionIterator npfci = npfc.getNestedPointFeatureCollectionIterator(-1);
            while (npfci.hasNext()) {
                NestedPointFeatureCollection nnpfc = npfci.next();
                String name = nnpfc.getName().replaceAll("[\\s]+", "");
                stationNames.put(stationIndex, name);
                stationIndex += 1;
            }
        } else {
            PointFeatureCollectionIterator pfci = npfc.getPointFeatureCollectionIterator(-1);
            while (pfci.hasNext()) {
                stationIndex = parsePointFeatureCollectionNames(pfci.next(), stationIndex);
            }
        }
        
        return stationIndex;
    }
    
    private int parsePointFeatureCollectionNames(PointFeatureCollection pfc, int stationIndex) throws IOException {
        String name = pfc.getName().replaceAll("[\\s]+", "");
        stationNames.put(stationIndex, name);
        return stationIndex+1;
    }
    
    private void parseStationNames() throws IOException {
        this.stationNames = new HashMap<Integer, String>();
        int stationIndex = 0;
        if (CDMPointFeatureCollection instanceof PointFeatureCollection) {
            PointFeatureCollection pfc = (PointFeatureCollection) CDMPointFeatureCollection;
            stationIndex = parsePointFeatureCollectionNames(pfc, stationIndex);
        } else if (CDMPointFeatureCollection instanceof NestedPointFeatureCollection) {
            NestedPointFeatureCollection npfc = (NestedPointFeatureCollection) CDMPointFeatureCollection;
            stationIndex = parseNestedCollection(npfc, stationIndex);
        }
    }
    
    private void parseTrajectoryIdsToNames() throws IOException {
        this.stationNames = new HashMap<Integer, String>();
        int stationindex = 0;
        // neseted feature collection
        NestedPointFeatureCollection npfc = (NestedPointFeatureCollection) CDMPointFeatureCollection;
        if (npfc.isMultipleNested()) {
            // iterate through it
            NestedPointFeatureCollectionIterator npfci = npfc.getNestedPointFeatureCollectionIterator(-1);
            for (;npfci.hasNext();) {
                NestedPointFeatureCollection n = npfci.next();
                String name = n.getName().replaceAll("[\\s]+", "");
                this.stationNames.put(stationindex++, "Trajectory"+name);
            }
        } else {
            // iterate through it
            PointFeatureCollectionIterator pfci = npfc.getPointFeatureCollectionIterator(-1);
            for (;pfci.hasNext();) {
                PointFeatureCollection n = pfci.next();
                String name = n.getName().replaceAll("[\\s]+", "");
                this.stationNames.put(stationindex++, "Trajectory"+name);
            }
        }
    }
    
    private void parseProfileIdsToNames() throws IOException {
        this.stationNames = new HashMap<Integer, String>();
        int stationindex = 0;
        if (CDMPointFeatureCollection instanceof PointFeatureCollection) {
            // point feature collection; iterate
            PointFeatureCollection pfc = (PointFeatureCollection) CDMPointFeatureCollection;
            PointFeatureIterator pfi = pfc.getPointFeatureIterator(-1);
            for (;pfi.hasNext();) {
                PointFeature pf = pfi.next();
                this.stationNames.put(stationindex, "Profile" + stationindex);
                stationindex++;
            }
        } else {
            // nested feature collection
            NestedPointFeatureCollection npfc = (NestedPointFeatureCollection) CDMPointFeatureCollection;
            if (npfc.isMultipleNested()) {
                // iterate
                NestedPointFeatureCollectionIterator npfci = npfc.getNestedPointFeatureCollectionIterator(-1);
                for (;npfci.hasNext();) {
                    NestedPointFeatureCollection n = npfci.next();
                    this.stationNames.put(stationindex, "Profile" + stationindex);
                    stationindex++;
                }
            } else {
                // iterate
                PointFeatureCollectionIterator pfci = npfc.getPointFeatureCollectionIterator(-1);
                for (;pfci.hasNext();) {
                    PointFeatureCollection n = pfci.next();
                    this.stationNames.put(stationindex, "Profile" + stationindex);
                    stationindex++;
                }
            }
        }
        
    }
    
    private void parseGridIdsToName() {
        this.stationNames = new HashMap<Integer, String>();
        for (int i=0; i<getGridDataset().getGridsets().size(); i++) {
            this.stationNames.put(i, "Grid"+i);
        }
    }
    
    /**
     * Returns the index of the station name
     * @param stationToLookFor the name of the station to find
     * @return the index of the station; -1 if no station with the name exists
     */
    protected int getStationIndex(String stationToLookFor) {
        // look for the station in the hashmap and return its index
        int retval = -1;
        
        if (stationNames != null && stationNames.containsValue(stationToLookFor)) {
            int index = 0;
            for (String stationName : stationNames.values()) {
                if (stationToLookFor.equalsIgnoreCase(stationName)) {
                    retval = index;
                    break;
                }
                index++;
            }
        }
        
        return retval;
    }
    
    /**
     * Get the station names, parsed from a Variable containing "station" and "name"
     * @return list of station names
     */
    protected HashMap<Integer,String> getStationNames() {
        return this.stationNames;
    }
    
    /**
     * Return the list of sensor names
     * @return string list of sensor names
     */
    protected List<String> getSensorNames() {
        return this.sensorNames;
    }
    
    /**
     * 
     * @param stationIndex
     * @return 
     */
    protected final double[] getStationCoords(int stationIndex) {
        try {
            // get the lat/lon of the station
            if (stationIndex >= 0) {
                double[] coords = new double[] { Double.NaN, Double.NaN };
            
                // find lat/lon values for the station
                coords[0] = latVariable.read().getDouble(stationIndex);
                coords[1] = lonVariable.read().getDouble(stationIndex);

                return coords;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("exception in getStationCoords " + e.getMessage());
            return null;
        }
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
    public static String getGMLNameBase() {
        return STATION_GML_BASE;
    }

    /**
     * Returns the urn of a station
     * @param stationName the station name to add to the name base
     * @return
     */
    public static String getGMLName(String stationName) {
        return STATION_GML_BASE + stationName;
    }
    
    /**
     * Returns the base string of the a sensor urn (does not include station and sensor name)
     * @return sensor urn base string
     */
    public static String getGMLSensorNameBase() {
        return SENSOR_GML_BASE;
    }
    
    /**
     * Returns a composite string of the sensor urn
     * @param stationName name of the station holding the sensor
     * @param sensorName name of the sensor
     * @return urn of the station/sensor combo
     */
    public static String getSensorGMLName(String stationName, String sensorName) {
        return SENSOR_GML_BASE + stationName + ":" + sensorName;
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

}
