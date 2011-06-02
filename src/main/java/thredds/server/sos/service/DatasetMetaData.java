/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import thredds.server.sos.getCaps.SOSObservationOffering;

import thredds.server.sos.bean.Extent;
import thredds.server.sos.getCaps.ObservationOffering;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.units.DateFormatter;
import ucar.nc2.units.DateUnit;

/**
 * Contains all the meta data information needed in the response
 * @author Abird
 */
public class DatasetMetaData {

    public static final String GMLPrefix = "urn:sura:station:sura.tds.sos:";
    private Date dStr;
    private final Extent ext;
    private final NetcdfDataset dataset;
    private String title;
    private String history;
    private String institution;
    private String source;
    private List<Attribute> attribs;
    private String location;
    private List<Variable> variables;
    private ArrayList<ObservationOffering> obsList;
    private final String stationPrefix = "station-";
    private Array stationsLats;
    private Array stationsLons;
    private String description;
    private String GMLName;
    private List observeredProperties;
    private String format = "text/xml; subtype=\"om/1.0.0\"";
    private List observeredPropertiesUnits;
    private Array timeVarArray;
    private Array latVarArray;
    private Array lonVarArray;
    private Array obsVarArray;
    private ArrayList<Array> obsVarArrayList;
    private ArrayList<Array> lonVarArrayList;
    private ArrayList<Array> latVarArrayList;
    private ArrayList<Array> timeVarArrayList;
    private Array obsDepth;
    private String[] searchTime;
    private boolean isMultiTime;
    private boolean EPADataSet = false;
    private String[] stationStringNamesList;
    private boolean noNCML;
    private String stationQueryRequested;
    private String threddsPath;

    /*
     * Main constructor
     */
    public DatasetMetaData(Extent ext, NetcdfDataset dataset) {
        //other info
        this.ext = ext;
        this.dataset = dataset;

        attribs = dataset.getGlobalAttributes();
        location = dataset.getLocation();

        timeVarArray = null;
        latVarArray = null;
        lonVarArray = null;
        obsVarArray = null;
        searchTime = null;

        obsVarArrayList = new ArrayList<Array>();
        lonVarArrayList = new ArrayList<Array>();
        latVarArrayList = new ArrayList<Array>();
        timeVarArrayList = new ArrayList<Array>();
    }

    /*
     * test Constructor
     */
    public DatasetMetaData() {
        ext = null;
        dataset = null;
        timeVarArray = null;
        latVarArray = null;
        lonVarArray = null;
        obsVarArray = null;

        obsVarArrayList = new ArrayList<Array>();
        lonVarArrayList = new ArrayList<Array>();
        latVarArrayList = new ArrayList<Array>();
        timeVarArrayList = new ArrayList<Array>();
    }

    /*
     * Create the dataset values for the dataset
     */
    public void setDatasetArrayValues(String[] ObsProperty) throws IOException, InvalidRangeException {
        List<Variable> vars = dataset.getReferencedFile().getVariables();

        for (int j = 0; j < ObsProperty.length; j++) {
            String obsValString = ObsProperty[j];

            for (int i = 0; i < vars.size(); i++) {
                Variable obsVar = vars.get(i);
                String name = obsVar.getName();
                if (name != null) {
                    if (name.contentEquals("time")) {
                        timeVarArray = obsVar.read();
                    } else if (name.contentEquals("lat")) {
                        latVarArray = obsVar.read();
                    } else if (name.contentEquals("lon")) {
                        lonVarArray = obsVar.read();
                    } else if (name.contentEquals(obsValString)) {
                        // if there is a single station
                        if (stationStringNamesList.length == 1)
                        {
                            obsVarArray = obsVar.read();
                        }
                        //if there is more than one station
                        else
                        {
                            StringBuilder strb = createobsArrayFromMultiArrayVariable(obsVar);
                            obsVarArray = obsVar.read(strb.toString());
                            //end test
                        }
                    } else if (name.contentEquals("z")) {
                        obsDepth = obsVar.read();
                    }
                }
            }
            obsVarArrayList.add(obsVarArray);
        }
    }

    private StringBuilder createobsArrayFromMultiArrayVariable(Variable obsVar){
        //values are 1 based not zero
        int[] arrayShape = obsVar.getShapeAll();
        int timeShape = arrayShape[0];
        int stationShape = arrayShape[1];
        //testing for errors
        StringBuilder strb = new StringBuilder();
        strb.append("0:");
        strb.append(Integer.toString(timeShape - 1));
        strb.append(":1, ");
        if (stationQueryRequested!=null){
        for (int i = 0; i < stationStringNamesList.length; i++) {
            if (stationStringNamesList[i].equalsIgnoreCase(stationQueryRequested)){
               strb.append(Integer.toString(i));
            }
        }
        }
        else{
            strb.append("0");         
        }
        return strb;
    }

    /**
     * main entry for the service
     */
    public void extractData() {
        try {
            parseAttributeList();
            parseDatasetObservations();
            createAndAddObservationOfferings();
        } catch (IOException ex) {
            Logger.getLogger(DatasetMetaData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DatasetMetaData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void parseAttributeList() {

        for (int i = 0; i < attribs.size(); i++) {
            Attribute elly = attribs.get(i);

            String attribName = elly.getName();
            String value = elly.getStringValue(0);

            if ((attribName.contentEquals("title")) || (attribName.contentEquals("station_name"))) {
                setTitle(value);
            } else if (attribName.contentEquals("history")) {
                setHistory(value);
            } else if (attribName.contentEquals("institution")) {
                setInstitution(value);
            } else if (attribName.contentEquals("source")) {
                setSource(value);
            } else if (attribName.contentEquals("description")) {
                setDescription(value);
            }
        }
    }

    public List getObservedPropsList() {
        return observeredProperties;
    }

    public void setObservedPropsList(List list) {
        this.observeredProperties = list;
    }

    public String getDescription() {
        if ((description == null) || (description.isEmpty())) {
            return "";
        } else {
            return description;
        }
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public String getTitle() {
        return title;
    }

    public void setHistory(String value) {
        this.history = value;
    }

    public String getHistory() {
        return history;
    }

    public void setInstitution(String value) {
        this.institution = value;
    }

    public String getInstitution() {
        return institution;
    }

    public void setSource(String value) {
        this.source = value;
    }

    public String getSource() {
        return source;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String value) {
        this.location = value;
    }

    public List<Attribute> getAttribs() {
        return attribs;
    }

    /**
     * Go through the list of variables and extract information on the data
     * STATION,LON,LAT,TIME, and observed properties
     * @throws IOException
     */
    public void parseDatasetObservations() throws IOException {
        List<Variable> vars = dataset.getReferencedFile().getVariables();

        List obsProps = new ArrayList();
        List obsPropsUnits = new ArrayList();

        for (int i = 0; i < vars.size(); i++) {
            Variable obsVar = vars.get(i);
            String name = obsVar.getName();

            //invalid ncml or station data string format
            noNCML = false;

            if (((name.contentEquals("station")) || (name.contentEquals("stationid")) || (name.contentEquals("station_name")))) {
                if (name.contentEquals("stationid")) {
                    EPADataSet = true;
                    getListOfStationNames(obsVar);
                } else {
                    if (name.contentEquals("station_name")) {
                        noNCML = true;
                    }
                    EPADataSet = false;
                    getListOfStationNames(obsVar);
                }

            } else if (name.contentEquals("lat")) {
                getListOfStationLats(obsVar);
            } else if (name.contentEquals("lon")) {
                getListOfStationLons(obsVar);
            } else if (name.contentEquals("time")) {
            } else {
                extractObservedProperties(obsVar, name, obsProps, obsPropsUnits);
            }
        }
        setObservedPropsList(obsProps);
        setObservedPropsUnitList(obsPropsUnits);
    }

    private void extractObservedProperties(Variable obsVar, String name, List obsProps, List obsPropsUnits) {
        //get the observed property list
        String obsName = obsVar.getName();
        String unit = obsVar.getUnitsString();
        if (obsName == null) {
            obsName = name;
        }
        obsProps.add(obsName);
        obsPropsUnits.add(unit);
    }

    void getListOfStationNames(Variable obsVar) throws IOException {

        String description = obsVar.getDescription();
        String shortName = obsVar.getShortName();

        List<Variable> myList = new ArrayList<Variable>();
        myList.add(obsVar);

        List<Array> a = dataset.readArrays(myList);
        Array stationArray = a.get(0);

        //if the epa dataset is present do somet clever

        setStringStationNames(stationArray);

    }

    /**
     *
     * @param stationArray
     */
    private void setStringStationNames(Array stationArray) {
        int size = (int) stationArray.getSize();
        String[] stationArrayStr;
        if ((noNCML == true)) {
            stationArrayStr = new String[1];
        } else {
            stationArrayStr = new String[size];
        }

        for (int i = 0; i < size; i++) {
            //if its an epa dataset
            if ((stationArray.getSize() == 1) && (EPADataSet == true)) {
                stationArrayStr[i] = getTitle();
            } //if there is noncml but station data in the nc
            else if (noNCML == true) {
                String stationToStr = stationArray.toString();
                stationArrayStr[0] = stationToStr;
                if (getTitle() == null) {
                    setTitle(stationToStr);
                }
            } //else it is a number
            else {
                stationArrayStr[i] = Integer.toString(stationArray.getInt(i));
            }
        }

        setStringStationNames(stationArrayStr);
    }

    public void setStringStationNames(String[] stationArrayStr) {
        this.stationStringNamesList = stationArrayStr;
    }

    public String[] getStringStationNames() {
        return stationStringNamesList;
    }

    public String getGMLName(String Value) {
        GMLName = (GMLPrefix + Value);
        return GMLName;
    }

    public String getStationPrefix() {
        return stationPrefix;
    }

    /**
     * create and add the obs offering to the obs list to be processed later
     */
    public void createAndAddObservationOfferings() throws Exception {
        obsList = new ArrayList<ObservationOffering>();

        for (int i = 0; i < stationStringNamesList.length; i++) {
            String stationName = stationStringNamesList[i];
            double lon = stationsLons.getDouble(i);
            double lat = stationsLats.getDouble(i);

            //set station info
            SOSObservationOffering newOffering = new SOSObservationOffering();
            newOffering.setObservationStationID(stationPrefix + (stationName));

            //set lat and lon info
            if (stationStringNamesList.length > 0) {
                newOffering.setObservationStationUpperCorner(Double.toString(parseDoubleValue(lat)), Double.toString(parseDoubleValue(lon)));
                newOffering.setObservationStationLowerCorner(Double.toString(parseDoubleValue(lat)), Double.toString(parseDoubleValue(lon)));
            }

            //set time information
            newOffering.setObservationTimeBegin(parseDateString(ext._minTime));
            newOffering.setObservationTimeEnd(parseDateString(ext._maxTime));

            //set description info is available
            newOffering.setObservationStationDescription(getDescription());
            //setGML Name
            newOffering.setObservationName(getGMLName((stationName)));
            newOffering.setObservationSrsName(getGMLName((stationName)));
            newOffering.setObservationProcedureLink(getGMLName((stationName)));

            //set the observed Props list
            newOffering.setObservationObserveredList(getObservedPropsList());

            //set the format
            newOffering.setObservationFormat(getFormatString());

            //set the response model
            newOffering.setObservationModel("");

            obsList.add(newOffering);
        }
    }

    public List<ObservationOffering> getObservationOfferingList() {
        return obsList;
    }

    private void getListOfStationLats(Variable obsVar) throws IOException {
        String description = obsVar.getDescription();
        String shortName = obsVar.getShortName();

        List<Variable> myList = new ArrayList<Variable>();
        myList.add(obsVar);

        List<Array> a = dataset.readArrays(myList);
        Array latArray = a.get(0);

        setStationsLats(latArray);
    }

    private void getListOfStationLons(Variable obsVar) throws IOException {
        String description = obsVar.getDescription();
        String shortName = obsVar.getShortName();

        List<Variable> myList = new ArrayList<Variable>();
        myList.add(obsVar);

        List<Array> a = dataset.readArrays(myList);
        Array lonArray = a.get(0);

        setStationsLons(lonArray);
    }

    public void setStationsLats(Array lats) {
        this.stationsLats = lats;
    }

    public void setStationsLons(Array lons) {
        this.stationsLons = lons;
    }

    public Array getStationLons() {
        return stationsLons;
    }

    public Array getStationLats() {
        return stationsLats;
    }

    public double parseDoubleValue(double value) {
        double actualMod = value;
        try {
            java.text.DecimalFormat df = new java.text.DecimalFormat("###.#####");
            actualMod = df.parse(df.format(value)).doubleValue();
            return actualMod;
        } catch (ParseException ex) {
            Logger.getLogger(DatasetMetaData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return actualMod;
    }

    /**
     *
     * @return iso DateString
     * @throws Exception
     */
    public String parseDateString(String dateTime) throws Exception {
        String dateStr = (dateTime + " " + ext._timeUnits);

        DateUnit du = new DateUnit(dateStr);
        Date b = du.getStandardOrISO(dateStr);

        DateFormatter df = new DateFormatter();
        String isoDateString = df.toDateTimeStringISO(b);

        return isoDateString;
    }

    public String getFormatString() {
        return format;
    }

    private void setObservedPropsUnitList(List list) {
        this.observeredPropertiesUnits = list;
    }

    public List getObservedPropsUnitList() {
        return observeredPropertiesUnits;
    }

    public void closeDataSet() throws IOException {
        dataset.close();
    }
    /*
     * get the actual time data
     */

    public Array getTimeVarData() {
        return timeVarArray;
    }
    /*
     * get the actual lat data
     */

    public Array getLatVarData() {
        return latVarArray;
    }
    /*
     * get the actual lon data
     */

    public Array getLonVarData() {
        return lonVarArray;
    }
    /*
     * get the actual obs data
     */

    public Array getObsPropVarData() {
        return obsVarArray;
    }

    /**
     * get the obs var array list.
     */
    public List<Array> getObsPropVarARRAYData() {
        return obsVarArrayList;
    }

    /*
     * get the obs depth array if available
     */
    public Array getObsDepth() {
        return obsDepth;
    }

    public void setSearchTimes(String[] searchTime, boolean isMultiTime) {
        this.searchTime = searchTime;
        this.isMultiTime = isMultiTime;
    }

    public boolean isSearchTimeAvailable() {
        if (searchTime == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isMultiTime() {
        return isMultiTime;
    }

    public String[] getSearchTimes() {
        return searchTime;
    }

    public void setRequestedStationName(String offering) {
        if (offering != null) {
            String[] splitOffering = offering.split(":");
            int l = splitOffering.length;
            String searchedName = splitOffering[l - 1];
            this.stationQueryRequested = searchedName;
        }
    }

    public String getRequestedStationName() {
        return stationQueryRequested;
    }
    
    public void setThreddsPath(String path) {
      threddsPath = path;
    }
    
    public String getThreddsPath() {
      return threddsPath;
    }
      
            
}
