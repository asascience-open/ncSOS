package com.asascience.ncsos.cdmclasses;

import ucar.ma2.Array;
import ucar.nc2.constants.CF;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.nc2.units.DateFormatter;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Station;

import java.io.IOException;
import java.util.*;

import org.joda.time.DateTime;

public class Grid extends baseCDMClass implements iStationData {

    public static final String DEPTH = "depth";
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    private List<String> stationNameList;
    private List<String> stationDescripList;
    private final String[] variableNames;
    private final ArrayList<String> eventTimes;
    private GridDataset GridData;
    private final Map<String, String> latLonRequest;
    DateFormatter dateFormatter = new DateFormatter();

    /**
     * Constructs a new Grid with parameters passed in
     * @param requestedStationNames the names of the stations from the request query string
     * @param eventTime the time(s) from the request query string
     * @param variableNames the observed properties from the request query string
     * @param latLonRequest HashMap that contains the requested Lat,Lon coordinates from the request query string
     */
    public Grid(String[] requestedStationNames, String[] eventTime, String[] variableNames, Map<String, String> latLonRequest) {
        startDate = null;
        endDate = null;
        this.variableNames = (variableNames);

        this.reqStationNames = new ArrayList<String>();
        this.reqStationNames.addAll(Arrays.asList(requestedStationNames));
        
        if (eventTime != null) {
            this.eventTimes = new ArrayList<String>();
            this.eventTimes.addAll(Arrays.asList(eventTime));
        } else
            this.eventTimes = null;
        
        this.latLonRequest = latLonRequest;
        this.stationNameList = new ArrayList<String>();
        this.stationDescripList = new ArrayList<String>();
        
        lowerAlt = upperAlt = 0;
    }

    public Map<String, String> getLatLonRequest() {
		return latLonRequest;
	}

	/**
     * Adds a Date entry to the builder from the dates array
     * @param builder the StringBuilder being built
     * @param dates Date array from which the first date value (0 index) is pulled from
     */
    private String addDateEntry( CalendarDate date) {
        return ("time=")+(date.toString())+",";
       // builder.append(",");
    }

    /**
     * Attempts to collect the depth values from latLons and returns them in an array
     * @param latLons hash map that has lat,lon and maybe depth
     * @return integer array with the indices of the depth values
     */
    private int[] checkAndGetDepthIndices(Map<String, Integer[]> latLons) {
    	// setup for finding our depth values
    	int[] retVal = new int[latLons.get(LAT).length];
    	CoordinateAxis1D depthData = (CoordinateAxis1D) GridData.getDataVariable(DEPTH);
    	if (depthData != null) {
    		double[] depthDbl = depthData.getCoordValues();
    		String[] requestedDepths = null;
    		if(latLonRequest.containsKey(DEPTH)){
    			requestedDepths = latLonRequest.get(DEPTH).split("[,]");

    			int currIndex = 0;
    			for (int i=0;i<retVal.length;i++) {
    				currIndex = (i < requestedDepths.length) ? i : requestedDepths.length - 1;
    				try {
    					retVal[i] = findBestIndex(depthDbl, Double.parseDouble(requestedDepths[currIndex]));
    				} catch (Exception e) {
    					System.out.println("Could not parse: " + requestedDepths[currIndex] + " - " + e.getMessage());
    					retVal[i] = 0;
    				}
    			}
    		}
    	} else {
    		for (int r=0;r<retVal.length;r++) {
    			retVal[r] = 0;
    		}
    	}
    	return retVal;
    }

    
    public Integer getGridZIndex(String grid){
    	Integer zIndex = -1;
    	GridDatatype gridType= this.GridData.findGridDatatype(grid);
    	if(gridType != null){
    		zIndex = gridType.getZDimensionIndex();
    	}
    	return zIndex;
    }
    
    
    public String getDepthUnits(String grid){
    	String units = null;
    	GridDatatype gridType= this.GridData.findGridDatatype(grid);
    	if(gridType != null){
    		int zIndex = gridType.getZDimensionIndex();
    		if(zIndex > -1){
    				units = gridType.getCoordinateSystem().getVerticalAxis().getUnitsString();
    		}
    	}
    	
    	return units;
    }
    public List<Double> getDepths(String grid){
    	GridDatatype gridType= this.GridData.findGridDatatype(grid);
    	List<Double> heightVals = new ArrayList<Double>();
    	if(gridType != null){
    		int zIndex = gridType.getZDimensionIndex();
    		if(zIndex > -1){

    			try {
    				Array heights = gridType.getCoordinateSystem().getVerticalAxis().read();
    				for(int i =0; i < heights.getSize(); i++){
    					heightVals.add(heights.getDouble(i));
    				}
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
    	return heightVals;
    }
    
    /************************/
    /* iStationData Methods */
    /**************************************************************************/
    
    @Override
    public void setData(Object griddedDataset) throws IOException {
        this.GridData = (GridDataset) griddedDataset;

        setStartDate(df.toDateTimeStringISO(GridData.getCalendarDateStart().toDate()));
        setEndDate(df.toDateTimeStringISO(GridData.getCalendarDateEnd().toDate()));

        //check and only add stations that are of interest
        int stCount = 0;
        for (int i = 0; i < GridData.getGrids().size(); i++) {
            List<String> varList = Arrays.asList(variableNames);
            if (varList.contains(GridData.getGrids().get(i).getFullName())) {
                // Does the varname equal?
                stCount++;
                stationNameList.add(GridData.getGrids().get(i).getFullName());
                stationDescripList.add(GridData.getGrids().get(i).getDescription());
                setBoundingBox(GridData.getGrids().get(i).getCoordinateSystem().getLatLonBoundingBox());
            } else {
                // Does the standard_name equal?
                String snd = GridData.getGrids().get(i).findAttValueIgnoreCase(CF.STANDARD_NAME, "NOPE");
                if (varList.contains(snd)) {
                    stCount++;
                    stationNameList.add(GridData.getGrids().get(i).getFullName());
                    stationDescripList.add(GridData.getGrids().get(i).getDescription());
                    setBoundingBox(GridData.getGrids().get(i).getCoordinateSystem().getLatLonBoundingBox());
                }
            }
        }
        setNumberOfStations(stCount);
    }

    public void setBoundingBox(LatLonRect llr) {
        upperLon = llr.getUpperRightPoint().getLongitude();
        upperLat = llr.getUpperRightPoint().getLatitude();
        lowerLon = llr.getLowerLeftPoint().getLongitude();
        lowerLat = llr.getLowerLeftPoint().getLatitude();
    }

    @Override
    public void setInitialLatLonBoundaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDataResponse(int stNum) {
        if (GridData != null) {
            StringBuilder builder = new StringBuilder();
            Array data = null;

            GridDatatype grid = GridData.getGrids().get(0);
            GridCoordSystem gcs = grid.getCoordinateSystem();
            
            String lat_name   =  gcs.getYHorizAxis().getOriginalVariable().getFullName();
            String lon_name   = gcs.getXHorizAxis().getOriginalVariable().getFullName();
            String depth_name = null;
            Integer timeIstart = null;
            Integer timeIend = -1;
            CoordinateAxis1DTime coordTime = gcs.getTimeAxis1D();

            if (this.eventTimes == null){
            	timeIstart = 0;
            	timeIend = (int) (coordTime.getSize() -1);
            }
            else if (eventTimes.size() > 1) {
            	// find all times between two specified
            	CalendarDate dtStart = CalendarDateFormatter.isoStringToCalendarDate(null, eventTimes.get(0));
                CalendarDate dtEnd = CalendarDateFormatter.isoStringToCalendarDate(null, eventTimes.get(1));
                for(Integer timeIndex = 0; timeIndex < coordTime.getSize(); timeIndex++){
                	CalendarDate cD = coordTime.getCalendarDate(timeIndex);
                	if(timeIstart == null && (cD.getDifferenceInMsecs(dtStart) == 0 || cD.isAfter(dtStart))){
                		timeIstart = timeIndex;
                	}
                	if(cD.getDifferenceInMsecs(dtEnd) == 0 || (cD.isBefore(dtEnd) && timeIstart != null)){
                		timeIend = timeIndex;
                	}
                }
            } //if single event time        
            else {
               // get closest time
            	CalendarDate dtStart = CalendarDateFormatter.isoStringToCalendarDate(null, eventTimes.get(0));
            	long cDiff;
            	long savedDiff = Long.MAX_VALUE;
            	Integer currIndex = null;
            	for(Integer timeIndex = 0; timeIndex <= timeIend; timeIndex++){
                	CalendarDate cD = coordTime.getCalendarDate(timeIndex);

            		cDiff = cD.getDifferenceInMsecs(dtStart);
            		
            		if(currIndex == null ||  Math.abs(cDiff) < savedDiff){
            			savedDiff = Math.abs(cDiff);
            			currIndex = timeIndex;
            		}
            	}
            	timeIstart = currIndex;
            	timeIend = currIndex;
            }
            double[] lonDbl = ((CoordinateAxis1D)GridData.getDataVariable(lon_name)).getCoordValues();
            double[] latDbl = ((CoordinateAxis1D)GridData.getDataVariable(lat_name)).getCoordValues();
            double[] depthDbl = null;

            CoordinateAxis1D depthAxis = GridData.getGrids().get(0).getCoordinateSystem().getVerticalAxis();
            if (depthAxis != null) {
                depth_name = depthAxis.getOriginalVariable().getFullName();
                depthDbl = ((CoordinateAxis1D)GridData.getDataVariable(depth_name)).getCoordValues();
            }

            Map<String, Integer[]> latLonDepthHash = findDataIndexs(lonDbl, latDbl, latLonRequest);

            int[] depthHeights = new int[latLonDepthHash.get(LON).length];
            Map<Integer, List<Integer>> allDepths = new HashMap<Integer, List<Integer>>();
            Boolean zeroDepths = true;

            for(String vars : variableNames) {
                if(vars.equalsIgnoreCase(DEPTH)) {
                    // we do want depths
                    zeroDepths = false;
                    if(this.latLonRequest.containsKey(DEPTH)){
                    	depthHeights = checkAndGetDepthIndices(latLonDepthHash);
                    	 for(int i=0; i<depthHeights.length; i++) {
                    		 List<Integer> oneVal = new ArrayList<Integer>();
                    		 oneVal.add(depthHeights[i]);
                    		 allDepths.put(i, oneVal);
                    	 }
                    }
                    else {
                    	
                    	List<Double> depthVals = this.getDepths(grid.getShortName());
                    	List<Integer> depthIndexList = new ArrayList<Integer>();
                    	for(int i=0; i <depthVals.size(); i++){
                    		depthIndexList.add(i);
                    	}
                    	for(int i=0; i<latLonDepthHash.get(LON).length; i++) {
                    		allDepths.put(i, depthIndexList);
                    	}
                    }
                    break;
                }
            }
            
            if (zeroDepths) {
                for(int i=0; i<depthHeights.length; i++) {
                    List<Integer> oneVal = new ArrayList<Integer>();
           		 	oneVal.add(0);
           		 	allDepths.put(i, oneVal);
                }
            }

            for(Integer timeIndex = timeIstart; timeIndex <= timeIend; timeIndex++){
            	for (int k=0; k<latLonDepthHash.get(LAT).length; k++) {
            		for(Integer depthIndex : allDepths.get(k)){

            			CalendarDate cD = coordTime.getCalendarDate(timeIndex);
            			String startStr = "";

            			//modify for requested dates, add in for loop
            			startStr = addDateEntry(cD);

            			// add depth
            			if(depthDbl != null) {
            				startStr += (depth_name)+("=")+(depthDbl[depthIndex])+(",");
            				startStr += (BIN_STR)+(depthIndex)+(",");
            				//builder.append(depth_name).append("=").append(depthDbl[depthIndex]).append(",");
            				//builder.append(BIN_STR).append(depthIndex).append(",");
            			}
            			startStr += (STATION_STR + stNum) +(",");

            			startStr += lat_name + "="+latDbl[latLonDepthHash.get(LAT)[k]]+(",");
            			startStr += lon_name + ("=") + (lonDbl[latLonDepthHash.get(LON)[k]]) +(",");
            			
            			
            			//builder.append(STATION_STR + stNum).append(",");

            			//builder.append(lat_name).append("=").append(latDbl[latLonDepthHash.get(LAT)[k]]).append(",");
            			//builder.append(lon_name).append("=").append(lonDbl[latLonDepthHash.get(LON)[k]]).append(",");
            			// get data slices
            			String dataName;
            			for (int l=0; l<GridData.getGrids().size();l++) {
            				dataName = GridData.getGrids().get(l).getName();
            				if (isInVariableNames(GridData.getGrids().get(l).getName())) {
            					try {
                    				builder.append(startStr);

            						grid = GridData.getGrids().get(l);
            						int latI = latLonDepthHash.get(LAT)[k];
            						int lonI = latLonDepthHash.get(LON)[k];
            						data = grid.readDataSlice(timeIndex, depthIndex, latI, lonI);
            						builder.append(dataName).append("=").append(data.getFloat(0)).append(";");
            					} catch (Exception ex) {
            						System.out.println("Error in reading data slice, index " + l + " - " + ex.getMessage());
            						builder.delete(0, builder.length());
            						builder.append("ERROR= reading data slice from GridData: ").append(ex.getLocalizedMessage());
            						return builder.toString();
            					}
            				}
            			}
            			//if(builder.length() > 0)
            			//	builder.deleteCharAt(builder.length()-1);
            			//builder.append(";");
            		}
            	}
            }
            //builder.append(" ").append("\n");
            return builder.toString();
        }
        return DATA_RESPONSE_ERROR + Grid.class;

    }

    @Override
    public String getStationName(int idNum) {
        return stationNameList.get(idNum);
    }

    @Override
    public String getTimeEnd(int stNum) {
        return getBoundTimeEnd();
    }

    @Override
    public String getTimeBegin(int stNum) {
        return getBoundTimeBegin();
    }

    @Override
    public double getLowerLat(int stNum) {
//        return Double.parseDouble(latLonRequest.get(LAT));
        String[] latStr = latLonRequest.get(LAT).split(("[,]"));
        double retVal = Double.MAX_VALUE;
        for(int i=0; i<latStr.length;i++) {
            try {
                double val;
                if(latStr[i].contains("_")) {
                    String[] bounds = latStr[i].split("_");
                    if(Double.parseDouble(bounds[0]) > Double.parseDouble(bounds[1])) {
                        val = Double.parseDouble(bounds[1]);
                    } else {
                        val = Double.parseDouble(bounds[0]);
                    }
                } else {
                    val = Double.parseDouble(latStr[i]);
                }
                if (val < retVal)
                    retVal = val;
            } catch (Exception e) {
                System.out.println("Error getLowerLat: " + e.getMessage());
            }
        }
        
        return retVal;
    }

    public double getClosestLat(int stNum){
  
        String lat_name   = GridData.getGrids().get(0).getCoordinateSystem().getYHorizAxis().getOriginalVariable().getFullName();
        String lon_name   = GridData.getGrids().get(0).getCoordinateSystem().getXHorizAxis().getOriginalVariable().getFullName();
        double[] lonDbl = ((CoordinateAxis1D)GridData.getDataVariable(lon_name)).getCoordValues();
        double[] latDbl = ((CoordinateAxis1D)GridData.getDataVariable(lat_name)).getCoordValues();
        return latDbl[this.findBestIndexLon(latDbl, (Double.valueOf( this.latLonRequest.get(LAT).split(",")[0])))];

    }
    
    public double getClosestLon(int stNum){
    	   String lon_name   = GridData.getGrids().get(0).getCoordinateSystem().getXHorizAxis().getOriginalVariable().getFullName();
           double[] lonDbl = ((CoordinateAxis1D)GridData.getDataVariable(lon_name)).getCoordValues();
           return lonDbl[this.findBestIndexLon(lonDbl, (Double.valueOf( this.latLonRequest.get(LON).split(",")[0])))];
    }
    
 
    @Override
    public double getLowerLon(int stNum) {
//        return Double.parseDouble(latLonRequest.get(LON));
        String[] lonStr = latLonRequest.get(LON).split(("[,]"));
        double retVal = Double.MAX_VALUE;
        for(int i=0; i<lonStr.length;i++) {
            try {
                double val;
                if(lonStr[i].contains("_")) {
                    String[] bounds = lonStr[i].split("_");
                    if(Double.parseDouble(bounds[0]) > Double.parseDouble(bounds[1])) {
                        val = Double.parseDouble(bounds[1]);
                    } else {
                        val = Double.parseDouble(bounds[0]);
                    }
                } else {
                    val = Double.parseDouble(lonStr[i]);
                }
                if (val < retVal)
                    retVal = val;
            } catch (Exception e) {
                System.out.println("Error getLowerLon: " + e.getMessage());
            }
        }
        
        return retVal;
    }

    public double getUpperLat() {
        return upperLat;
    }
    public double getUpperLon() {
        return upperLon;
    }
    public double getLowerLat() {
        return lowerLat;
    }
    public double getLowerLon() {
        return lowerLon;
    }

    @Override
    public double getUpperLat(int stNum) {
//        return Double.parseDouble(latLonRequest.get(LAT));
        String[] latStr = latLonRequest.get(LAT).split(("[,]"));
        double retVal = -1 * Double.MAX_VALUE;
        for(int i=0; i<latStr.length;i++) {
            try {
                double val;
                if(latStr[i].contains("_")) {
                    String[] bounds = latStr[i].split("_");
                    if(Double.parseDouble(bounds[0]) < Double.parseDouble(bounds[1])) {
                        val = Double.parseDouble(bounds[1]);
                    } else {
                        val = Double.parseDouble(bounds[0]);
                    }
                } else {
                    val = Double.parseDouble(latStr[i]);
                }
                if (val > retVal)
                    retVal = val;
            } catch (Exception e) {
                System.out.println("Error getUpperLat: " + e.getMessage());
            }
        }
        
        return retVal;
    }

    @Override
    public double getUpperLon(int stNum) {
//        return Double.parseDouble(latLonRequest.get(LON));
        String[] lonStr = latLonRequest.get(LON).split(("[,]"));
        double retVal = -1 * Double.MAX_VALUE;
        for(int i=0; i<lonStr.length;i++) {
            try {
                double val;
                if(lonStr[i].contains("_")) {
                    String[] bounds = lonStr[i].split("_");
                    if(Double.parseDouble(bounds[0]) < Double.parseDouble(bounds[1])) {
                        val = Double.parseDouble(bounds[1]);
                    } else {
                        val = Double.parseDouble(bounds[0]);
                    }
                } else {
                    val = Double.parseDouble(lonStr[i]);
                }
                if (val > retVal)
                    retVal = val;
            } catch (Exception e) {
                System.out.println("Error getUpperLon: " + e.getMessage());
            }
        }
        
        return retVal;
    }
    
    @Override
    public String getDescription(int stNum) {
        return stationDescripList.get(stNum);
    }
    
    /**************************************************************************/

    /**
     * 
     * @param nameToCheck
     * @return 
     */
    private Boolean isInVariableNames(String nameToCheck) {
        for (String name : variableNames) {
            if (name.equalsIgnoreCase(nameToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * find lat lon index's in X|Y axis of requested locations
     * @param lonDbl array of longitude values
     * @param latDbl array of latitude values
     * @param latLonRequest map with the latitude and longitude request(s)
     * @return map with arrays of indices for latitude and longitude
     */
    private Map<String, Integer[]> findDataIndexs(double[] lonDbl, double[] latDbl, Map<String, String> latLonRequest) {
        Map<String, Integer[]> latLonIndex = new HashMap<String, Integer[]>();
        String lonVal = latLonRequest.get(LON);
        String latVal = latLonRequest.get(LAT);
        String[] lons, lats;
        
        // check to see if we are looking for multiple or range of lat/lons
        // multiple
        if(lonVal.contains(",")) {
            // multiple lons
            lons = lonVal.split(",");
        } else {
            lons = new String[] { lonVal };
        }
        
        if (latVal.contains(",")) {
            // multiple lats
            lats = latVal.split(",");
        } else {
            lats = new String[] { latVal };            
        }
            
        double[] requestedLons = new double[lons.length];
        double[] requestedLats = new double[lats.length];


        for (int j=0;j<lons.length;j++) {
            try {
                if (lons[j].contains("_")) {
                    String[] bounds = lons[j].split("_");
                    requestedLons = arrayFromValueRange(bounds, lonDbl, requestedLons);
                } else {
                    requestedLons[j] = Double.parseDouble(lons[j]);
                }
            } catch (Exception e) {
                System.out.println("Error in parse: " + e.getMessage());
            }
        }

        for (int k=0;k<lats.length;k++) {
            try {
                if (lats[k].contains("_")) {
                    String[] bounds = lats[k].split("_");
                    requestedLats = arrayFromValueRange(bounds, latDbl, requestedLats);
                } else {
                    requestedLats[k] = Double.parseDouble(lats[k]);
                }
            } catch (Exception e) {
                System.out.println("Error in parse: " + e.getMessage());
            }
        }

        // determine which array to use for loop count
        int requestedArrayLength = (requestedLons.length > requestedLats.length) ? requestedLons.length : requestedLats.length;

        Integer[] retLats = new Integer[requestedArrayLength];
        Integer[] retLons = new Integer[requestedArrayLength];

        // get our indices
        for(int i=0;i<requestedArrayLength;i++) {
            if(requestedLons.length > i) {
                retLons[i] = findBestIndexLon(lonDbl, requestedLons[i]);
            } else {
                retLons[i] = findBestIndexLon(lonDbl, requestedLons[requestedLons.length - 1]);
            }

            if(requestedLats.length > i) {
                retLats[i] = findBestIndex(latDbl, requestedLats[i]);
            } else {
                retLats[i] = findBestIndex(latDbl, requestedLats[requestedLats.length - 1]);
            }
        }

        // put into hash
        latLonIndex.put(LAT, retLats);
        latLonIndex.put(LON, retLons);
        
        return latLonIndex;

    }
    
    /**
     * iterate through the arrayToSearch array for values that lie in out boundaries
     * @param bounds the upper & lower bounds of the desired values
     * @param arrayToSearch array to search for values inside given bounds
     * @param arrayToExpand the array to add the desired values to
     * @return arrayToExpand with the new values added
     */
    private double[] arrayFromValueRange(String[] bounds, double[] arrayToSearch, double[] arrayToExpand) {
        double minVal, maxVal;
        try {
            minVal = Double.parseDouble(bounds[0]);
        } catch (Exception e) {
            minVal = 0;
        }
        try {
            maxVal = Double.parseDouble(bounds[1]);
        } catch (Exception e) {
            maxVal = minVal;
        }
        if (maxVal < minVal) {
            // swap values if the order is reversed
            double temp = minVal;
            minVal = maxVal;
            maxVal = temp;
        }
        // looking for a range of doubles, iterate through the arrayToSearch array for values that lie in out boundaries
        ArrayList<Double> builder = new ArrayList<Double>();
        for (int k=0;k<arrayToExpand.length;k++) {
            builder.add(arrayToExpand[k]);
        }
        for (int l=0;l<arrayToSearch.length;l++) {
            if (arrayToSearch[l] >= minVal && arrayToSearch[l] <= maxVal) {
                builder.add(arrayToSearch[l]);
            }
        }
        arrayToExpand = new double[builder.size()];
        for(int i=0;i<builder.size();i++) {
            arrayToExpand[i] = builder.get(i).doubleValue();
        }
        return arrayToExpand;
    }
    
    /**
     * iterate through valueArray to find the index with the value closest to 'valueToFind'
     * @param valueArray array to iterate through
     * @param valueToFind value to find
     * @return index of value closest to 'valueToFind'
     */
    private int findBestIndex(double[] valueArray, double valueToFind){
        // iterate through the array to find the index with the value closest to 'valueToFind'
        double bestDiffValue = Double.MAX_VALUE;
        int retIndex = -1;
        for(int i=0; i<valueArray.length; i++) {
           double curDiffValue = valueArray[i] - valueToFind;
           if(Math.abs(curDiffValue) < bestDiffValue) {
               bestDiffValue = Math.abs(curDiffValue);
               retIndex = i;
           }
        }
        return retIndex;
    }

    private int findBestIndexLon(double[] valueArray, double valueToFind){
        // iterate through the array to find the index with the value closest to 'valueToFind'
        double bestDiffValue = Double.MAX_VALUE;
        int retIndex = -1;
        double uValueToFind = valueToFind % 360; // make 0-360
        for(int i=0; i<valueArray.length; i++) {
        	double cVal = valueArray[i] % 360;
        	double curDiffValue;
    		curDiffValue = cVal - uValueToFind;
    		double meth2;
        	 if(uValueToFind > cVal){
        		   meth2 = (360 - uValueToFind) + cVal;
        	 }
        	 else{
        		 meth2 = (360 - cVal) + uValueToFind;
        	 }        	   
        	 curDiffValue = Math.min(curDiffValue, meth2);
       
        	if(Math.abs(curDiffValue) < bestDiffValue) {
               bestDiffValue = Math.abs(curDiffValue);
               retIndex = i;
           }
        }
        return retIndex;
    }
    public List<String> getLocationsString(int stNum) {
        List<String> retval = new ArrayList<String>();
        retval.add(this.getLowerLat(stNum) + " " + this.getLowerLon(stNum));
        return retval;
    }

    
}
