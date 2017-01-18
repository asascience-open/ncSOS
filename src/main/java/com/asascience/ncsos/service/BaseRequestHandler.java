package com.asascience.ncsos.service;

import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.outputformatter.XmlOutputFormatter;
import com.asascience.ncsos.util.DiscreteSamplingGeometryUtil;
import com.asascience.ncsos.util.ListComprehension;
import com.asascience.ncsos.util.VocabDefinitions;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateTransform;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDataset.Gridset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.*;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

public abstract class BaseRequestHandler {
    public static final String CF_ROLE = "cf_role";
    public static final String GRID = "grid";
    public static final String GRID_MAPPING = "grid_mapping";
    public static final String NAME = "name";
    public static final String PROFILE = "profile";
    public static final String TRAJECTORY = "trajectory";
    public static final String PROFILE_ID = "profile_id";
    public static final String TRAJECTORY_ID = "trajectory_id";
    public static final String UNKNOWN = "unknown";
    public static final String STANDARD_NAME = "standard_name";
    public static final String HREF_NO_STANDARD_NAME_URL = "http://mmisw.org/ont/fake/parameter/";
    public static final String STATION_URN_BASE = "urn:ioos:station:";
    public static final String SENSOR_URN_BASE = "urn:ioos:sensor:";
    public static final String NETWORK_URN_BASE = "urn:ioos:network:";
    public static final String DEFAULT_NAMING_AUTHORITY = "ncsos";
    public static final String STANDARD_NAME_VOCAB ="standard_name_vocabulary";
    public static final String PLATFORM_VOCAB = "platform_vocabulary";
    public static final String TYPE = "type";
    public static final String NETWORK_ALL = "network-all";
    public static final String NAMING_AUTHORITY= "naming_authority";
    public static final String DISCRIMINANT = "discriminant";
    public static final String CELL_METHOD = "cell_methods";
    public static final String INTERVAL = "interval";
    public static final String VERTICAL_DATUM = "vertical_datum";
    public static final String PLATFORM = "platform";
    public static final String SHORT_NAME = "short_name";
    public static final String IOOS_CODE = "ioos_code";
    public static final String INSTRUMENT = "instrument";
    public static final String LONG_NAME = "long_name";
    private static final NumberFormat FORMAT_DEGREE;
    // list of keywords to filter variables on to remove non-data variables from the list
    private static final String[] NON_DATAVAR_NAMES = { "rowsize", "row_size", PROFILE, "info", "time", "z", "alt", "height", "station_info" };
    private FeatureDataset featureDataset;
    private FeatureCollection CDMPointFeatureCollection;
    private GridDataset gridDataSet = null;
    
    // Global Attributes
    protected HashMap<String, Object> global_attributes = new HashMap<String, Object>();
    
    // Variables and other information commonly needed
    protected final NetcdfDataset netCDFDataset;
    protected Variable latVariable, lonVariable, timeVariable, depthVariable;
    protected Variable stationVariable;
    private HashMap<Integer, String> stationNames;
    private HashMap<String, String> urnToStationName;
    private HashMap<String, VariableSimpleIF> sensorNames;
    private HashMap<String, Variable> platformVariableMap;
    private HashMap<String, String> gridVariableMap;
    protected boolean isInitialized;
    protected String crsName;
    private boolean crsInitialized;
    // Exception codes - Table 25 of OGC 06-121r3 (OWS Common)
    protected static String INVALID_PARAMETER       = "InvalidParameterValue";
    protected static String MISSING_PARAMETER       = "MissingParameterValue";
    protected static String OPTION_NOT_SUPPORTED    = "OptionNotSupported";
    protected static String OPERATION_NOT_SUPPORTED = "OperationNotSupported";
    protected static String VERSION_NEGOTIATION    = "VersionNegotiationFailed";


    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(BaseRequestHandler.class);

    static {
        FORMAT_DEGREE = NumberFormat.getNumberInstance();
        FORMAT_DEGREE.setMinimumFractionDigits(1);
        FORMAT_DEGREE.setMaximumFractionDigits(14);
    }
    private FeatureType dataFeatureType;
    protected OutputFormatter formatter;

    /**
     * Takes in a dataset and wraps it based on its feature type.
     * @param netCDFDataset the dataset being acted on
     * @throws IOException
     */
    public BaseRequestHandler(NetcdfDataset netCDFDataset) throws IOException {
    	this(netCDFDataset, true);
    }
    
    public BaseRequestHandler(NetcdfDataset netCDFDataset, boolean initialize) throws IOException{
        // check for non-null dataset
        if(netCDFDataset == null) {
//            _log.error("received null dataset -- probably exception output");
            this.netCDFDataset = null;
            return;
        }
        this.crsInitialized = false;
        this.crsName = null;
        this.netCDFDataset = netCDFDataset;
        isInitialized = false;
        if(initialize){
        	initializeDataset();
        }
    }
    
    
    /**
     * Returns the 'standard_name' attribute of a variable, if it exists
     * @param varName the name of the variable
     * @return the 'standard_name' if it exists, otherwise ""
     */
    public String getVariableStandardName(String varName) {
        return getVariableAttribute(varName, STANDARD_NAME);
    }
    
    
  
    
    /**
     * Returns the 'standard_name' attribute of a variable, if it exists
     * @param varName the name of the variable
     * @return the 'standard_name' if it exists, otherwise ""
     */
    public String getVariableAttribute(String varName, String attributeName) {
        String retval = UNKNOWN;

        for (Variable var : netCDFDataset.getVariables()) {
            if (varName.equalsIgnoreCase(var.getFullName())) {
                Attribute attr = var.findAttribute(attributeName);
                if (attr != null) {
                    retval = attr.getStringValue();
                }
            }
        }

        return retval;
    }
    
    public String getObservedOfferingUrl(String variable){
        String sensorDef = getVariableStandardName(variable);   
        String hrefUrl = null;

        if(sensorDef.equals(UNKNOWN)){
            hrefUrl = HREF_NO_STANDARD_NAME_URL + variable;
        }
        else {
            hrefUrl =  getHrefForParameter(sensorDef);
        }
        return hrefUrl;
    }
    
    
    public  String getHrefForParameter(String standardName) {

    	  String globalStandVocab = this.getGlobalAttributeStr(STANDARD_NAME_VOCAB);
    	  Boolean cfConventions = null;
    	  if(globalStandVocab != null){
    		  String cfConv = this.getGlobalAttributeStr("Convention");
    		  if(cfConv != null && cfConv.toUpperCase().contains("CF")){
    			  cfConventions = true;
    		  }
    	  }
    	  
    	  return VocabDefinitions.GetDefinitionForParameter(standardName, globalStandVocab, cfConventions);

    }
    
    
    
    protected void initializeDataset() throws IOException{
        // get the feature dataset (wraps the dataset in variety of accessor methods)
        findFeatureDataset(FeatureDatasetFactoryManager.findFeatureType(netCDFDataset));
        // verify we could get a dataset (make sure the dataset is CF 1.6 compliant or whatever)
        if (gridDataSet == null && featureDataset == null) {
            _log.error("Unknown feature type! " + FeatureDatasetFactoryManager.findFeatureType(netCDFDataset));
            return;
        }
        // if dataFeatureType is none/null (not GRID) get the point feature collection
        if (dataFeatureType == null || dataFeatureType == FeatureType.NONE) {
            CDMPointFeatureCollection = DiscreteSamplingGeometryUtil.extractFeatureDatasetCollection(featureDataset);
            dataFeatureType = CDMPointFeatureCollection.getCollectionFeatureType();

        }
        // find the global attributes
        parseGlobalAttributes();
        // get the station variable and several other bits needed
        findAndParseStationVariable();
        // get sensor Variable names
        parseSensorNames();
        // get Axis vars (location, time, depth)
        latVariable = netCDFDataset.findCoordinateAxis(AxisType.Lat);
        lonVariable = netCDFDataset.findCoordinateAxis(AxisType.Lon);
        timeVariable = netCDFDataset.findCoordinateAxis(AxisType.Time);
        depthVariable = netCDFDataset.findCoordinateAxis(AxisType.Height);
        isInitialized = true;
    }
    /**
     * Attempts to set the feature dataset based on the dataset's FeatureType
     * @param datasetFT The FeatureType of the netcdf dataset, found with the factory manager
     * @throws IOException 
     */
    private void findFeatureDataset(FeatureType datasetFT) throws IOException {
        if (datasetFT != null) {
            switch (datasetFT) {
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
        }
        
        if (featureDataset == null) {
            // attempt to get the dataset from an any_point
            featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.ANY_POINT, netCDFDataset, null, new Formatter(System.err));
            
            if (featureDataset == null) {
                // null, which means the dataset should be grid...
                featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.GRID, netCDFDataset, null, new Formatter(System.err));
                gridDataSet = DiscreteSamplingGeometryUtil.extractGridDatasetCollection(featureDataset);
                dataFeatureType = FeatureType.GRID;
            }
        }
    }
    
    /**
     * Finds commonly used global attributes in the netcdf file.
     */
    protected void parseGlobalAttributes() {
        String name = null;
        Object value = null;
        for (Attribute a : this.netCDFDataset.getGlobalAttributes()) {
            name = a.getFullName();
            try {
                value = a.getStringValue().trim();
            } catch(NullPointerException e) {
                try {
                    value = a.getNumericValue();
                } catch(Exception ex) {
                    continue;
                }
            }
            this.global_attributes.put(name, value);
        }
        // Fill in required naming authority attribute
        if (!this.global_attributes.containsKey("naming_authority") ) {
            this.global_attributes.put("naming_authority", DEFAULT_NAMING_AUTHORITY);
        }

        if (this.global_attributes.get("naming_authority").equals("")) {
            this.global_attributes.put("naming_authority", DEFAULT_NAMING_AUTHORITY);
        }
        // Fill in required naming authority attribute
        if (!this.global_attributes.containsKey("featureType")) {
            this.global_attributes.put("featureType", "UNKNOWN");
        }
    }

    /**
     * Finds the station variable with several approaches
     * 1) Attempts to find a variable with the attribute "cf_role"; only the
     * station defining variable should have this attribute
     * 2) Looks for a variable with 'grid' and 'name' in its name; GRID datasets
     * do not have a "cf_role" attribute
     * 3) Failing above, will 
     * @throws IOException 
     */
    private void findAndParseStationVariable() throws IOException {
        // get station var
        // check for station, trajectory, profile and grid station info
    	Variable cfRole = null;
    	List<String> platformVars = new ArrayList<String>();
        this.urnToStationName = new HashMap<String, String>();
        this.platformVariableMap = new HashMap<String, Variable>();
        this.gridVariableMap = new HashMap<String, String>();

        this.stationNames = new HashMap<Integer, String>();
        for (Variable var : netCDFDataset.getVariables()) {
            // look for cf_role attr
            if (this.stationVariable == null) {
            	
                for (Attribute attr : var.getAttributes()) {
                	if(attr.getFullName().equalsIgnoreCase(PLATFORM)) {
                		String platName = attr.getStringValue();
                		if(platName != null){
                			for (String platVarName : platName.split(",")){
                				if(!platformVars.contains(platVarName))
                					platformVars.add(platVarName);
                			}
                		}
                		
                	}
                    if(attr.getFullName().equalsIgnoreCase(CF_ROLE)) {
                        cfRole = var;
                    }
                }
            }
            // check name for grid data (does not have cf_role)
            String varName = var.getFullName().toLowerCase();
           
            if (var.getRank() >= 3 || (varName.contains(GRID) && varName.contains(NAME))) {
                this.stationVariable = var;
                parseGridIdsToName();
            }
            
            if (this.stationVariable != null)
                break;
        }
        boolean addedStat = false;
        if(platformVars.isEmpty()){
        	Object plat = this.getGlobalAttribute(PLATFORM);
    		if(plat != null){
    			for (String platVarName : String.valueOf(plat).split(",")){
    				if(!platformVars.contains(platVarName))
    					platformVars.add(platVarName);
    			}
    		}
        }
        if(!platformVars.isEmpty()){
        	//
        	int stationIndex = 0;
        
        	Map<Integer, String> stationNamesForURNMap = new HashMap<Integer, String>();
        	parsePlatformNames(stationNamesForURNMap);
        
        	for (String platformVariableName : platformVars){
        		Variable var = getVariableByName(platformVariableName);
        		String station = null;
        		if(var != null){
        			for(Attribute sname : var.getAttributes()){
        				if(sname.getFullName().equalsIgnoreCase(SHORT_NAME)){
        					station = sname.getStringValue();
        					break;
        				}
        			}
        		}
        		
        		if (var != null && station == null) {
        			station = var.getShortName();


        			stationNames.put(stationIndex, station);

        			platformVariableMap.put(station, var);
        			addedStat = true;

        			this.urnToStationName.put( this.getUrnName(station), stationNamesForURNMap.get(stationIndex));
        			stationIndex++;
        		}
        		}
        	}
        	
        	
        
        if(!addedStat){
            if(this.stationVariable == null){
            	if(cfRole != null){
            		this.stationVariable = cfRole;
            		parsePlatformNames(stationNames);
            	}
            	else if (getGridDataset() == null) {
            		// there is no station variable ... add a single station with index 0?
            		parsePlatformNames(stationNames);
            	}
            	else {
            		parseGridIdsToName();
            	}
            }
    		for(String sName : this.stationNames.values()){
        		this.urnToStationName.put(this.getUrnName(sName), sName);
    		}
        }
    }
    
    /**
     * Finds all variables that are sensor (data) variables and compiles a list of their names.
     */
    private void parseSensorNames() {
        // find all variables who's not a coordinate axis and does not have 'station' in the name
        this.sensorNames = new HashMap<String, VariableSimpleIF>();
//        getFeatureDataset().getDataVariables();
        for (Iterator<VariableSimpleIF> it = getFeatureDataset().getDataVariables().iterator(); it.hasNext();) {
            VariableSimpleIF var = it.next();
            String name = var.getShortName();
            if (name.equalsIgnoreCase(PROFILE) || name.toLowerCase().contains("info") || name.toLowerCase().contains("time") ||
                name.toLowerCase().contains("row") || name.equalsIgnoreCase("z") || name.equalsIgnoreCase("alt") ||
                name.equalsIgnoreCase("height") ||
                name.equalsIgnoreCase("depth") || name.equalsIgnoreCase("lat") || name.equalsIgnoreCase("lon"))
            	continue;
            else
                this.sensorNames.put(name, var);
        }
        if (this.sensorNames.size() < 1) {
            System.err.println("Do not have any sensor names!");
        }
    }


    private String getPlatformName(FeatureCollection fc, int index) {
        // If NCJ can't pull out the FC name, we need to populate it with something
        // like TRAJECTORY-0, STATION-0, PROFILE-0
    	String fcName = fc.getName();
        if (fcName != null && !fcName.equalsIgnoreCase("unknown") && !fcName.isEmpty()) {
            return fc.getName();
        } else {
            return fc.getCollectionFeatureType().name() + "-" + index;
        }
    }

    /**
     * Add station names to hashmap, indexed.
     * @param npfc The nested point feature collection we're iterating through
     * @param stationIndex Current index of the station
     * @return Next station index
     * @throws IOException 
     */
    private int parseNestedCollection(NestedPointFeatureCollection npfc, int stationIndex,
    		Map<Integer,String> stationMap) throws IOException { 
        if (npfc.isMultipleNested()) {
            NestedPointFeatureCollectionIterator npfci = npfc.getNestedPointFeatureCollectionIterator(-1);
            while (npfci.hasNext()) {
                NestedPointFeatureCollection n = npfci.next();
                
                stationMap.put(stationIndex, this.getPlatformName(n, stationIndex));
                stationIndex += 1;
            }
        } else {
            PointFeatureCollectionIterator pfci = npfc.getPointFeatureCollectionIterator(-1);
            while (pfci.hasNext()) {
                PointFeatureCollection n = pfci.next();
                stationMap.put(stationIndex, this.getPlatformName(n, stationIndex));
                stationIndex += 1;
            }
        }
        
        return stationIndex;
    }
    
    /**
     * Reads a point feature collection and adds the station name, indexed
     * @param pfc feature collection to read
     * @param stationIndex current station index
     * @return next station index
     * @throws IOException 
     */
    private int parsePointFeatureCollectionNames(PointFeatureCollection pfc, 
    		int stationIndex, Map<Integer,String> stationMap) throws IOException {
        String name = pfc.getName();
        stationMap.put(stationIndex, name);
        return stationIndex+1;
    }
    
    /**
     * Go through the point feature collection to get the station names.
     * @throws IOException 
     */
    private void parsePlatformNames(Map<Integer,String> stationMap) throws IOException {
        int stationIndex = 0;
        if (CDMPointFeatureCollection instanceof PointFeatureCollection) {
            PointFeatureCollection pfc = (PointFeatureCollection) CDMPointFeatureCollection;
            parsePointFeatureCollectionNames(pfc, stationIndex,stationMap);
        } else if (CDMPointFeatureCollection instanceof NestedPointFeatureCollection) {
            NestedPointFeatureCollection npfc = (NestedPointFeatureCollection) CDMPointFeatureCollection;
            parseNestedCollection(npfc, stationIndex,stationMap);
        }
    }
    
    /**
     * Gets a number of stations based on the size of the grid sets.
     */
    private void parseGridIdsToName() {
    	this.stationNames = new HashMap<Integer, String>();
    	GridDataset gd = getGridDataset();
    	if(gd != null){
    		List<Gridset> gridSets = gd.getGridsets();
    		int i = 0;
    		for (Gridset gs : gridSets) {
    			for(GridDatatype gridV : gs.getGrids()){
    				this.gridVariableMap.put(gridV.getFullName(), "Grid"+i);
    			}
    			this.stationNames.put(i, "Grid"+i);
    			i++;
    		}
    	}
    }
    
    /**
     * Attempts to find the coordinate reference authority
     * @param varName var name to check for crs authority
     * @return the authority name, if there is one
     */
    private String getAuthorityFromVariable(String varName) {
        String retval = null;

        Variable crsVar = null;
        for (Variable var : netCDFDataset.getVariables()) {
            if (var.getFullName().equalsIgnoreCase(varName)) {
                crsVar = var;
                break;
            }
        }
        
        if (crsVar != null) {
            for (Attribute attr : crsVar.getAttributes()) {
                if (attr.getName().toLowerCase().contains("code")) {
                    retval = attr.getValue(0).toString();
                    break;
                }
            }
        }
        
        return retval;
    }
    
    /**
     * Returns the index of the station name
     * @param stationToLookFor the name of the station to find
     * @return the index of the station; -1 if no station with the name exists
     */
    protected int getStationIndex(String stationToLookFor) {
        try {
            if (stationToLookFor == null)
                throw new Exception("Looking for null station");
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get the station names, parsed from a Variable containing "station" and "name"
     * @return list of station names
     */
    protected HashMap<Integer,String> getStationNames() {
        return this.stationNames;
    }
    
    protected HashMap<String, Variable> getPlatformMap(){
    	return this.platformVariableMap;
    }
    public NetcdfDataset getNetCDFDataset() {
		return netCDFDataset;
	}

	/**
     * Return the list of sensor names
     * @return string list of sensor names
     */
    protected HashMap<String, VariableSimpleIF> getSensorNames() {
        return this.sensorNames;
    }

    public VariableSimpleIF getSensorVariable(String sensorName) {
        return this.sensorNames.get(sensorName);
    }

    
    protected HashMap<String, Variable> getPlatformVariableMap() {
		return platformVariableMap;
	}

	/**
     * Return the list of sensor names
     * @return string list of sensor names
     */
    protected List<String> getSensorUrns(String stationName) {
        List<String> urnNames = new ArrayList<String>(this.sensorNames.size());
        for (VariableSimpleIF sensorVar : this.sensorNames.values()) {
            urnNames.add(this.getSensorUrnName(stationName, sensorVar));
        }
        return urnNames;

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
            _log.error("exception in getStationCoords " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the units string of a variable
     * @param varName the name of the variable to look for
     * @return the units string or "none" if the variable could not be found
     */
    protected String getUnitsOfVariable(String varName) {
        String units = "none";
        if (featureDataset != null) {
           VariableSimpleIF ivar = featureDataset.getDataVariable(varName);
           if(ivar != null){
        	   units = ivar.getUnitsString();
           }
        } else {
            Variable var = netCDFDataset.findVariable(varName);
            if (var != null) {
            	units = var.getUnitsString();
            }
        }
       
        return units;
    }
    
    public HashMap<String, String> getUrnToStationName() {
		return urnToStationName;
	}

	protected Attribute[] getAttributesOfVariable(String varName) {
        Variable var;
        if (featureDataset != null) {
            var = (Variable) featureDataset.getDataVariable(varName);
        } else {
            var = netCDFDataset.findVariable(varName);
        }
        if (var != null) {
            return var.getAttributes().toArray(new Attribute[var.getAttributes().size()]);
        }
        return null;
    }
    
    /**
     * Attempts to find a variable in the dataset.
     * @param variableName name of the variable
     * @return either the variable if found or null
     */
    public Variable getVariableByName(String variableName) {
        return this.netCDFDataset.findVariable(variableName);
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
     * Returns the OutputFormatter being used by the request.
     * @return OutputFormatter
     */
    public OutputFormatter getOutputFormatter() {
        return formatter;
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
     * Are the station defined by indices rather than names
     * @return T/F
     */
    public boolean isStationDefinedByIndices() {
        return stationVariable != null && stationVariable.getDataType() != DataType.CHAR && stationVariable.getDataType() != DataType.STRING;
    }
    
    /**
     * Get a list of the station variable attributes
     * @return a list of attributes; empty if there is no station var
     */
    public List<Attribute> getStationAttributes() {
        if (stationVariable != null) 
            return stationVariable.getAttributes();
        else
            return new ArrayList<Attribute>();
    }

    /**
     * Returns the urn of a station
     * @param stationName the station name to add to the name base
     * @return
     */
    public String getUrnName(String stationName) {
    
    	
    	if(stationName != null) {
    		// mapping from station to platform.
        	Map<String,String> urnMap = getUrnToStationName();
        	for(String cName : urnMap.keySet()){
        		String val = urnMap.get(cName);
        		if(val != null && val.equals(stationName)){
        			stationName = cName;
        		}
        	}
    		String[] feature_name = stationName.split(":");
    		if(this.platformVariableMap != null && this.platformVariableMap.containsKey(stationName) ){
    			Variable platformVar = this.platformVariableMap.get(stationName);
    			if(platformVar != null){
    				for(Attribute att : platformVar.getAttributes()){
    					if(att.getFullName().equalsIgnoreCase(IOOS_CODE)){
    						return att.getStringValue();
    					}
    				}
    			}
    		}
    		else if(this.gridVariableMap.containsKey(stationName)){
    			stationName = this.gridVariableMap.get(stationName);
    		}
    		if (feature_name.length > 1 && feature_name[0].equalsIgnoreCase("urn")) {
    			// We already have a URN, so just return it.
    			return stationName;
    		} 
    	}
    	return STATION_URN_BASE + this.global_attributes.get(NAMING_AUTHORITY) + ":" + stationName;
    }
    
    

    /**
     * Go from urn to readable name (as in swe2:field name)
     * @param stName
     * @return 
     */
    public String stationToFieldName(String stName) {
        // get the gml urn
        String urn = this.getUrnName(stName);
        // split on station/sensor
        String[] urnSplit = urn.split("(sensor|station):");
        // get the last index of split
        urn = urnSplit[urnSplit.length - 1];
        // convert to underscore
        String underScorePattern = "[\\+\\-\\s:]+";
        urn = urn.replaceAll(underScorePattern, "_");
        return urn.toLowerCase();
    }
    protected Variable getPlatformVariableFromURN(String stationURN){
    	Variable platformVar = null;
    	Map<String, Variable> platformMap = this.getPlatformVariableMap();
		String foundStationName = null;
		 for (String stationName : this.getStationNames().values()) {
	            if (getUrnName(stationName).equalsIgnoreCase(stationURN) || getUrnNetworkAll().equalsIgnoreCase(stationURN)){
	            	foundStationName = stationName;
	            	break;
	            }
		 }
		 if (foundStationName != null)
			 platformVar = platformMap.get(foundStationName);
    	return platformVar;
    }
    public String getUrnNetworkAll() {
        // returns the network-all urn of the authority
        return NETWORK_URN_BASE + this.global_attributes.get(NAMING_AUTHORITY) + ":all";
    }
    
    /**
     * Returns a composite string of the sensor urn
     * @param stationName name of the station holding the sensor
     * @param sensorName name of the sensor
     * @return urn of the station/sensor combo
     */
    public String getSensorUrnName(String stationName, VariableSimpleIF sensorVar) {
    	// mapping from station to platform.
    	Map<String,String> urnMap = getUrnToStationName();
    	for(String cName : urnMap.keySet()){
    		String val = urnMap.get(cName);
    		if(val != null && val.equals(stationName)){
    			stationName = cName;
    		}
    	}
    	if(this.gridVariableMap != null && this.gridVariableMap.containsKey(stationName)){
    		stationName = this.gridVariableMap.get(stationName);
    	}
        String[] feature_name = stationName.split(":");
        String authority = (String) this.global_attributes.get(NAMING_AUTHORITY);
        if (feature_name.length > 2 && feature_name[0].equalsIgnoreCase("urn")) {
            // We have a station URN, so strip out the name
            stationName = feature_name[feature_name.length - 1];
            authority = feature_name[feature_name.length - 2];
        }

        List<Attribute> sensorAtts= sensorVar.getAttributes();
        String stadName = sensorVar.getShortName();
        String discriminant = "";
        String optionalArgs = "";
        for(Attribute att : sensorAtts){
        	String attName = att.getShortName();
        	String attVal = att.getStringValue();
        	if(attName.equalsIgnoreCase(STANDARD_NAME)){
        		stadName = att.getStringValue();
        	}
        	else if(attName.equalsIgnoreCase(DISCRIMINANT)){
        		discriminant = ":" + attVal;
        	}
          	else if(attName.equalsIgnoreCase(VERTICAL_DATUM)){
        		optionalArgs += optionalArgs.equals("") ? "#" : ";";
        		optionalArgs += VERTICAL_DATUM + "=" + attVal;
        	}
        	else if(attName.equalsIgnoreCase(CELL_METHOD)){
        		optionalArgs += optionalArgs.equals("") ? "#" : ";";
        		optionalArgs += CELL_METHOD + "=" + attVal;
        	} 
        }
        return SENSOR_URN_BASE + authority + ":" + stationName + ":" + stadName + discriminant + optionalArgs;
    }

    /**
     * Finds the CRS/SRS authorities used for the data vars. This method reads
     * through variables in a highly inefficient manner, therefore if a method
     * provided by the netcdf-java api is found that provides the same output it
     * should be favored over this.
     * @return an array of crs/srs authorities if there are any; else null
     */
    public String[] getCRSSRSAuthorities() {
        ArrayList<String> returnList = new ArrayList<String>();
        for (VariableSimpleIF var : featureDataset.getDataVariables()) {
            for (Attribute attr : var.getAttributes()) {
                if (attr.getFullName().equalsIgnoreCase(GRID_MAPPING)) {
                    String stName = attr.getValue(0).toString();
                    String auth = getAuthorityFromVariable(stName);
                    if (auth != null && !returnList.contains(auth)) {
                        returnList.add(auth);
                    }
                }
            }
        }
        if (returnList.size() > 0)
            return returnList.toArray(new String[returnList.size()]);
        else
            return null;
    }
    
    public String getCrsName()  {
        if(!this.crsInitialized) {
            String[] crsArray = getCRSSRSAuthorities();
            if (crsArray != null && crsArray[0] != null) {
                crsName = crsArray[0].replace("EPSG:", "http://www.opengis.net/def/crs/EPSG/0/");
            } else {
                crsName = "http://www.opengis.net/def/crs/EPSG/0/4326";
            }
            this.crsInitialized = true;
        }
        return crsName;
    }

  
    /**
     * 
     * @return 
     */
    public ArrayList<String> getCoordinateNames() {
        ArrayList<String> retval = new ArrayList<String>();
        for (CoordinateTransform ct : netCDFDataset.getCoordinateTransforms()) {
            retval.add(ct.getName());
        }
        return retval;
    }

                      /**
     * Formats degree, using a number formatter
     * @param degree a number to format to a degree
     * @return the number as a degree
     */
    public static String formatDegree(double degree) {
        return FORMAT_DEGREE.format(degree);
    }


    public Object getGlobalAttribute(String key, Object fillvalue) {
        if (this.global_attributes.containsKey(key)) {
            return this.global_attributes.get(key);
        } else {
            return fillvalue;
        }
    }

    public Object getGlobalAttribute(String key) {
        if (this.global_attributes.containsKey(key)) {
            return this.global_attributes.get(key);
        } else {
            return null;
        }
    }
    
    public String getGlobalAttributeStr(String key) {
    	Object ob = getGlobalAttribute(key);
    	String retVal = null;
    	if (ob != null){
    		retVal = String.valueOf(ob);
    	}
    	return retVal;
    }
    /**
     * Attempts to find an attribute from a given variable
     * @param variable variable to look in for the attribute
     * @param attributeName attribute with value desired
     * @param defaultValue default value if attribute does not exist
     * @return the string value of the attribute if exists otherwise defaultValue
     */
    public static String getValueFromVariableAttribute(VariableSimpleIF variable, String attributeName, String defaultValue) {
        Attribute attr = variable.findAttributeIgnoreCase(attributeName);
        if (attr != null) {
            return attr.getStringValue();
        }
        return defaultValue;
    }
    
    /**
     * Get all of the data variables from the dataset. Removes any axis variables or
     * variables that are not strictly measurements.
     * @return list of variable interfaces
     */
    public List<VariableSimpleIF> getDataVariables() {
        List<VariableSimpleIF> retval = ListComprehension.map(this.featureDataset.getDataVariables(), new ListComprehension.Func<VariableSimpleIF, VariableSimpleIF>() {
            public VariableSimpleIF apply(VariableSimpleIF in) {
                // check for direct name comparisons
                for (String name : NON_DATAVAR_NAMES) {
                    String sname = in.getShortName().toLowerCase();
                    if (sname.equalsIgnoreCase(name))
                        return null;
                }
                return in;
            }
        });
        retval = ListComprehension.filterOut(retval, null);
        // get any ancillary variables from the current data variables
        List<String> ancillaryVariables = ListComprehension.map(retval, new ListComprehension.Func<VariableSimpleIF, String>() {
           public String apply(VariableSimpleIF in) {
               Attribute av = in.findAttributeIgnoreCase("ancillary_variables");
               if (av != null)
                   return av.getStringValue();
               return null;
           } 
        });
        final List<String> ancillaryVariablesF = ListComprehension.filterOut(ancillaryVariables, null);
        // remove any ancillary variables from the current retval list
        retval = ListComprehension.map(retval, new ListComprehension.Func<VariableSimpleIF, VariableSimpleIF>() {
           public VariableSimpleIF apply(VariableSimpleIF in) {
                List<Boolean> add = ListComprehension.map(ancillaryVariablesF, in, new ListComprehension.Func2P<String, VariableSimpleIF, Boolean>() {
                  public Boolean apply(String sin, VariableSimpleIF vin) {
                      if (sin.equals(vin.getShortName()))
                          return false;
                      return true;
                  }
               });
                // filter out all of the 'trues' in the list, if there are any 'falses' left, then the
                // variable should not be in the final list
               add = ListComprehension.filterOut(add, true);
               if (add.size() > 0)
                   return null;
               
               return in;
           } 
        });
        return ListComprehension.filterOut(retval, null);
    }

   

}
