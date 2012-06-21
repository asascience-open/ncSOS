package thredds.server.sos.CDMClasses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ucar.nc2.units.DateFormatter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import thredds.server.sos.getObs.SOSObservationOffering;
import ucar.ma2.Array;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.Station;

/**
 * RPS - ASA
 * @author abird
 * @version 
 *
 * 
 *
 */
public class Grid extends baseCDMClass implements iStationData {

    public static final String DEPTH = "depth";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    private List<String> stationNameList;
    private List<String> stationDescripList;
    private final String[] variableNames;
    private final ArrayList<String> eventTimes;
    private GridDataset GridData;
    private final Map<String, String> latLonRequest;
    DateFormatter dateFormatter = new DateFormatter();

    public Grid(String[] requestedStationNames, String[] eventTime, String[] variableNames, Map<String, String> latLonRequest) {
        startDate = null;
        endDate = null;
        this.variableNames = (variableNames);

        this.reqStationNames = new ArrayList<String>();
        this.reqStationNames.addAll(Arrays.asList(requestedStationNames));
        this.eventTimes = new ArrayList<String>();
        eventTimes.addAll(Arrays.asList(eventTime));
        this.latLonRequest = latLonRequest;
        this.stationNameList = new ArrayList<String>();
        this.stationDescripList = new ArrayList<String>();
    }

    public void addDateEntry(StringBuilder builder, Date[] dates) {
        builder.append(dateFormatter.toDateTimeStringISO(dates[0]));
        builder.append(",");
    }

    public void addDepthEntry(StringBuilder builder, int depthHeight) {
        //add depth data entry to variable if available
        if ((CoordinateAxis1D) GridData.getDataVariable(DEPTH) != null) {
            builder.append(depthHeight);
            builder.append(",");
        }
    }

    public void addVariableEntrys(StringBuilder builder, double[] latDbl, Map<String, Integer> latlon, double[] lonDbl, GridDatatype grid, Array data) {
    }

    public void appendEndOfEntry(StringBuilder builder) {
        builder.append(" ");
        builder.append("\n");
    }

    /**
     * get a valid requested depth
     * @param latlon
     * @return 
     */
//    public int checkAndGetDepthValue(Map<String, Integer> latlon) {
//        /**
//         * initialize depth variable if available, set in the SOS parser is available
//         */
//        double[] depthDbl = null;
//        int depthHeight = -1;
//        for (int i = 0; i < variableNames.length; i++) {
//            if (variableNames[i].equalsIgnoreCase(DEPTH)) {
//                CoordinateAxis1D depthData = (CoordinateAxis1D) GridData.getDataVariable(DEPTH);
//                depthDbl = depthData.getCoordValues();
//                /**
//                 * try and get the depth information if it is there
//                 * if not use the first layer i.e 0
//                 */
//                try {
//                    depthHeight = latlon.get(DEPTH);
//                    //check that the depth is valid
//                    if (getDepthIndex(depthDbl, depthHeight) == -1) {
//                        depthHeight = 0;
//                    }
//
//
//                } catch (Exception e) {
//                    depthHeight = 0;
//                }
//                break;
//            }
//        }
//        if (depthDbl == null) {
//            depthHeight = 0;
//        }
//        return depthHeight;
//    }
    
    public int[] checkAndGetDepthIndices(Map<String, Integer[]> latLons) {
        // setup for finding our depth values
        int[] retVal = new int[latLons.get(LAT).length];
        CoordinateAxis1D depthData = (CoordinateAxis1D) GridData.getDataVariable(DEPTH);
        if (depthData != null) {
            double[] depthDbl = depthData.getCoordValues();
            String[] requestedDepths = latLonRequest.get(DEPTH).split("[,]");
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
        } else {
            for (int r=0;r<retVal.length;r++) {
                retVal[r] = 0;
            }
        }
        return retVal;
    }

    public double[] getLatCoordData() {
        CoordinateAxis1D latData = (CoordinateAxis1D) GridData.getDataVariable(LAT);
        double[] latDbl = latData.getCoordValues();
        return latDbl;
    }

    public double[] getLonCoordData() {
        CoordinateAxis1D lonData = (CoordinateAxis1D) GridData.getDataVariable(LON);
        double[] lonDbl = lonData.getCoordValues();
        return lonDbl;
    }

    @Override
    public void setData(Object griddedDataset) throws IOException {
        this.GridData = (GridDataset) griddedDataset;

        setStartDate(df.toDateTimeStringISO(GridData.getCalendarDateStart().toDate()));
        setEndDate(df.toDateTimeStringISO(GridData.getCalendarDateEnd().toDate()));

        //check and only add stations that are of interest
        int stCount = 0;
        for (int i = 0; i < GridData.getGrids().size(); i++) {
            if (!GridData.getGrids().get(i).getFullName().equalsIgnoreCase("cloud_land_mask")) {
                for (int j = 0; j < variableNames.length; j++) {
                    if (variableNames[j].equalsIgnoreCase("lat") || variableNames[j].equalsIgnoreCase("lon")) {
                    } else {
                        if (variableNames[j].equalsIgnoreCase(GridData.getGrids().get(i).getFullName())) {
                            stCount++;
                            stationNameList.add(GridData.getGrids().get(i).getFullName());
                            stationDescripList.add(GridData.getGrids().get(i).getDescription());
                        }
                    }
                }


            }
        }

        setNumberOfStations(stCount);

        CoordinateAxis1D lonData = (CoordinateAxis1D) GridData.getDataVariable(LON);
        CoordinateAxis1D latData = (CoordinateAxis1D) GridData.getDataVariable(LAT);
        double[] lonDbl = lonData.getCoordValues();
        double[] latDbl = latData.getCoordValues();

        upperLat = (latDbl[latDbl.length - 1]);
        lowerLat = (latDbl[0]);
        upperLon = (lonDbl[lonDbl.length - 1]);
        lowerLon = (lonDbl[0]);

    }

    @Override
    public void setInitialLatLonBounaries(List<Station> tsStationList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * check that the depth requested is valid in the depth array
     * @param depthDbl depth array
     * @param depthHeightRequested
     * @return 
     */
    public int getDepthIndex(double[] depthDbl, int depthHeightRequested) {
        int dataIndex = -1;

        //check that the depth is a valid depth in the grid
        for (int j = 0; j < depthDbl.length; j++) {
            if (depthDbl[j] == (double) depthHeightRequested) {
                return j;
            }
        }

        return dataIndex;
    }

    @Override
    /**
     * get the data response for the grid request
     */
    public String getDataResponse(int stNum) {
        if (GridData != null) {
            StringBuilder builder = new StringBuilder();
            Array data = null;
            double[] lonDbl = getLonCoordData();
            double[] latDbl = getLatCoordData();
            Map<String, Integer[]> latLonDepthHash = findDataIndexs(lonDbl, latDbl, latLonRequest);

            GridDatatype grid = GridData.getGrids().get(0);
            GridCoordSystem gcs = grid.getCoordinateSystem();
            
            int[] depthHeights = new int[latLonDepthHash.get(LON).length];
            Boolean zeroDepths = true;

            for(String vars : variableNames) {
                if(vars.equalsIgnoreCase(DEPTH)) {
                    // we do want depths
                    zeroDepths = false;
                    depthHeights = checkAndGetDepthIndices(latLonDepthHash);
                }
            }
            
            if (zeroDepths) {
                for(int i=0; i<depthHeights.length; i++) {
                    depthHeights[i] = 0;
                }
            }
//            int depthHeight = checkAndGetDepthValue(latLonDepthHash);


            
            
            double[] depthDbl = null;
            CoordinateAxis1D depthAxis = (CoordinateAxis1D) GridData.getDataVariable(DEPTH);
            if(depthAxis != null) {
                depthDbl = depthAxis.getCoordValues();
            }
            
            for (int k=0; k<latLonDepthHash.get(LAT).length; k++) {
                java.util.Date[] dates = null;
                if (gcs.hasTimeAxis1D()) {
                    CoordinateAxis1DTime tAxis1D = gcs.getTimeAxis1D();
                    dates = tAxis1D.getTimeDates();

                } else if (gcs.hasTimeAxis()) {
                    CoordinateAxis tAxis = gcs.getTimeAxis();
                }
                //modify for requested dates, add in for loop
                addDateEntry(builder, dates);
                
                if (isInVariableNames(DEPTH) && depthDbl != null) {
                    builder.append(depthDbl[depthHeights[k]]);
                    builder.append(",");
                }
                
                builder.append(latDbl[latLonDepthHash.get(LAT)[k]]);
                builder.append(",");
                builder.append(lonDbl[latLonDepthHash.get(LON)[k]]);
                builder.append(",");
                // get data slices
                for (int l=0; l<GridData.getGrids().size();l++) {
                    if (isInVariableNames(GridData.getGrids().get(l).getName())) {
                        try {
                            data = grid.readDataSlice(0, depthHeights[k], latLonDepthHash.get(LAT)[k], latLonDepthHash.get(LON)[k]);
                            builder.append(data.getFloat(0));
                            builder.append(",");
                        } catch (Exception e) {
                            System.out.println("Error in reading data slice, index " + l + " - " + e.getMessage());
                        }
                    }
                }
                builder.append("\r\n");
            }
            appendEndOfEntry(builder);
            return builder.toString();
        }
        return DATA_RESPONSE_ERROR + Grid.class;

    }
    
    private Boolean isInVariableNames(String nameToCheck) {
        for (String name : variableNames) {
            if (name.equalsIgnoreCase(nameToCheck)) {
                return true;
            }
        }
        
        return false;
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

    /**
     * get capabilities response
     * @param dataset
     * @param document
     * @param GMLName
     * @param format
     * @return 
     */    
    public static Document getCapsResponse(GridDataset dataset, Document document, String GMLName, String format) {
        System.out.println("grid");
        List<GridDatatype> gridData = dataset.getGrids();
        //dataset.getCalendarDateStart();
        //dataset.getCalendarDateEnd();

        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        DateTime dt;

        for (int i = 0; i < gridData.size(); i++) {
            //check that it is not a cloud land mask
            if (!gridData.get(i).getFullName().equalsIgnoreCase("cloud_land_mask")) {

                CoordinateAxis1D lonData = (CoordinateAxis1D) dataset.getDataVariable(LON);
                CoordinateAxis1D latData = (CoordinateAxis1D) dataset.getDataVariable(LAT);
                double[] lonDbl = lonData.getCoordValues();
                double[] latDbl = latData.getCoordValues();

                SOSObservationOffering newOffering = new SOSObservationOffering();
                newOffering.setObservationStationLowerCorner(Double.toString(latDbl[0]), Double.toString(lonDbl[0]));
                newOffering.setObservationStationUpperCorner(Double.toString(latDbl[latDbl.length - 1]), Double.toString(lonDbl[lonDbl.length - 1]));

                dt = new DateTime(dataset.getCalendarDateStart().toDate());
                newOffering.setObservationTimeBegin(fmt.print(dt));

                dt = new DateTime(dataset.getCalendarDateEnd().toDate());
                newOffering.setObservationTimeEnd(fmt.print(dt));


                newOffering.setObservationStationDescription(gridData.get(i).getDescription());
                newOffering.setObservationFeatureOfInterest(gridData.get(i).getFullName());
                newOffering.setObservationName(GMLName + (gridData.get(i).getName()));
                newOffering.setObservationStationID((gridData.get(i).getName()));
                newOffering.setObservationProcedureLink(GMLName + ((gridData.get(i).getName())));
                newOffering.setObservationSrsName("EPSG:4326");  // TODO?  
                List<String> obsProperty = new ArrayList<String>();
                obsProperty.add(gridData.get(i).getDescription());

                newOffering.setObservationObserveredList(obsProperty);
                newOffering.setObservationFormat(format);
                
                document = CDMUtils.addObsOfferingToDoc(newOffering, document);
            }
        }
        return document;
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

    /**
     * find lat lon index's in X|Y axis of requested locations
     * @param lonDbl
     * @param latDbl
     * @param latLonRequest
     * @return 
     */
    private Map<String, Integer[]> findDataIndexs(double[] lonDbl, double[] latDbl, Map<String, String> latLonRequest) {
        System.out.println("Uh, do we get to findDataIndexs?");
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
                retLons[i] = findBestIndex(lonDbl, requestedLons[i]);
            } else {
                retLons[i] = findBestIndex(lonDbl, requestedLons[requestedLons.length - 1]);
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
    
    private double[] arrayFromValueRange(String[] bounds, double[] arrayToSearch, double[] arrayToExpand) {
        System.out.println("In arrayFromValueRange");
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
        // looking for a range of longitudes, iterate through the longitude array for values that lie in out boundaries
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

    @Override
    public String getDescription(int stNum) {
        return stationDescripList.get(stNum);
    }
}
