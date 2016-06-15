package com.asascience.ncsos.ds;

import com.asascience.ncsos.cdmclasses.*;
import com.asascience.ncsos.outputformatter.ErrorFormatter;
import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.outputformatter.ds.IoosPlatform10Formatter;
import com.asascience.ncsos.util.LogReporter;
import com.asascience.ncsos.util.VocabDefinitions;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.EnumTypedef;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IoosPlatform10Handler extends Ioos10Handler implements BaseDSInterface {
    
    private final String procedure;
    private final String stationName;
    private final String urlBase;
    private iStationData stationData;
    private String errorString;
    private boolean locationLineFlag;
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IoosPlatform10Handler.class);
    private final static String QUERY = "?service=SOS&request=DescribeSensor&version=1.0.0&outputFormat=text/xml;subtype=\"sensorML/1.0.1\"&procedure=";
    
    private IoosPlatform10Formatter platform;
    
    public IoosPlatform10Handler(NetcdfDataset dataset, String procedure, String serverURL) throws IOException {
        super(dataset, new LogReporter());
        this.procedure = procedure;
        this.stationName = procedure; //.substring(procedure.lastIndexOf(":")+1);
        this.urlBase = serverURL;
        this.locationLineFlag = false;
        this.setStationData();
    }
    
    public void setupOutputDocument(OutputFormatter output) throws IOException {
        if (errorString != null) {
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException(errorString);
        } else {
            try {
                this.platform = (IoosPlatform10Formatter) output;
                describePlatform();
            } catch (ClassCastException ex) {
                logger.error(ex.toString());
            }
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Describe Platform">
    private void describePlatform() {
        platform.setVersionMetadata();
        platform.setDescriptionNode((String)this.getGlobalAttribute("summary", "no description"));
        platform.setName(this.procedure);
        this.formatSmlIdentification();
        this.formatSmlClassification();
        this.formatSmlValidTime();
        this.formatSmlNetworkProcedures();

        this.formatSmlContacts();
        this.formatSmlHistory();
        this.formatSmlDocumentation();
        this.formatGmlBoundedBy();
        if (this.getGridDataset() != null) {
        	// leave location blank for gridded data sets to avoid schema violation
            // this.formatSmlLocationBbox();
        } else if (locationLineFlag) {
            this.formatSmlLocationLine();
        } else {
            this.formatSmlLocationPoint();
        }
        this.formatSmlComponents();
    }
    
    
    
    private void formatGmlBoundedBy() {
   
        platform.setBoundedBy(this.getCrsName(), 
                this.stationData.getBoundLowerLat() + " " + this.stationData.getBoundLowerLon(), 
                this.stationData.getBoundUpperLat() + " " + this.stationData.getBoundUpperLon());
    }
    
    private void formatSmlIdentification() {
    	  for (Map.Entry<Integer,String> station : this.getStationNames().entrySet()) {
  
              Variable platformVar = this.getPlatformVariableMap().get(station.getValue());
              String stationUrn =  this.getUrnName(station.getValue());
              String stationLabel = ATTRIBUTE_MISSING;
              String [] splitUrn = stationUrn.split(":");
              if(splitUrn.length > 1)
              	stationLabel = splitUrn[splitUrn.length -1];
              // identifiers for station
              platform.addSmlIdentifier("stationID", VocabDefinitions.GetIoosDefinition("stationID"), 
                      stationUrn);
              platform.addSmlIdentifier("shortName", VocabDefinitions.GetIoosDefinition("shortName"), 
                      stationLabel);
              platform.addSmlIdentifier("longName", VocabDefinitions.GetIoosDefinition("longName"), 
                      this.checkForRequiredValue(platformVar, "long_name"));
              // wmoid, if it exists
              if(platformVar != null){
              	Attribute identAtt = platformVar.findAttribute("wmo_code");
              	if (identAtt != null) {
              		platform.addSmlIdentifier("wmoID", VocabDefinitions.GetIoosDefinition("wmoID"), identAtt.getStringValue());
              	}
              }
    	  }

    }
    
   
    
    private void formatSmlClassification() {
        // add platformType, operatorSector and publisher classifications (assuming they are global variables
        platform.addSmlClassifier("platformType", VocabDefinitions.GetIoosDefinition("platformType"), 
        		"platform", this.getPlatformType(this.stationName));
        platform.addSmlClassifier("operatorSector", VocabDefinitions.GetIoosDefinition("operatorSector"),
        		"sector", this.checkForRequiredValue("creator_sector"));
        platform.addSmlClassifier("publisher", VocabDefinitions.GetIoosDefinition("publisher"), 
        		"organization", this.checkForRequiredValue("publisher_name"));
        platform.addSmlClassifier("parentNetwork", "http://mmisw.org/ont/ioos/definition/parentNetwork", 
                "organization", (String)this.getGlobalAttribute(INSTITUTION, ATTRIBUTE_MISSING ));
        
       
        String value;
        // sponsor is optional
        String contribRole = (String)this.getGlobalAttribute("contributor_role", null);
        String contribName = (String)this.getGlobalAttribute("contributor_name", null);
        if (contribRole == null || contribName == null) {
            value = ATTRIBUTE_MISSING;
        }
        else {
        	value = contribName + " - "+ contribRole;
        }
        platform.addSmlClassifier("sponsor", VocabDefinitions.GetIoosDefinition("sponsor"), "organization", value);
        
    }
    
    private void formatSmlValidTime() {
        platform.setValidTime(this.stationData.getBoundTimeBegin(), this.stationData.getBoundTimeEnd());
    }
    
    private void formatSmlNetworkProcedures() {
        platform.addSmlCapabilitiesNetwork(OutputFormatter.SYSTEM, this.getUrnNetworkAll());
    }
    
    private void formatSmlContacts() {
        // waiting for some Q's to be answered from kyle
        // operator == creator (mandatory)
        // publisher == publisher (mandatory)
        // sponsor == sponsor (optional)
        
        HashMap<String, HashMap<String,String>> contactInfo = new HashMap<String, HashMap<String,String>>();
        String role = "http://mmisw.org/ont/ioos/definition/operator";
        String org = this.checkForRequiredValue("creator_name");
        String url = (String)this.getGlobalAttribute("creator_url", "No global attribute 'creator_url' found.");
        LinkedHashMap<String, String> address = createAddressForContact("creator");
        contactInfo.put("address", address);
        HashMap<String, String> phone = new HashMap<String, String>();
        phone.put("voice", (String)this.getGlobalAttribute("creator_phone", "No global attribute 'creator_phone' found."));
        contactInfo.put("phone", phone);
        platform.addContactNode(role, org, contactInfo, url);

        contactInfo.clear();
        phone.clear();

        role = "http://mmisw.org/ont/ioos/definition/publisher";
        org = this.checkForRequiredValue("publisher_name");
        url = (String)this.getGlobalAttribute("publisher_url", "No global attribute 'publisher_url' found.");
        address = createAddressForContact("publisher");
        contactInfo.put("address", address);
        phone.put("voice", (String)this.getGlobalAttribute("publisher_phone", "No global attribute 'publisher_phone' found."));
        contactInfo.put("phone", phone);
        platform.addContactNode(role, org, contactInfo, url);
    }
    
    private LinkedHashMap<String,String> createAddressForContact(String contactPrefix) {
        LinkedHashMap<String,String> address = new LinkedHashMap<String, String>();
        address.put("deliveryPoint", (String)this.getGlobalAttribute(contactPrefix + "_address"));
        address.put("city", (String)this.getGlobalAttribute(contactPrefix + "_city"));
        address.put("administrativeArea", (String)this.getGlobalAttribute(contactPrefix + "_state"));
        address.put("postalCode", (String)this.getGlobalAttribute(contactPrefix+"_zipcode"));
        address.put("country", this.checkForRequiredValue(contactPrefix + "_country"));
        address.put("electronicMailAddress", this.checkForRequiredValue(contactPrefix + "_email"));
        return address;
    }
    
    private void formatSmlHistory() {
        // not entirely sure how this should be implemented from dataset info
        // suppose to show deoployment dates...
    }

    private void formatSmlDocumentation() {
        // need to get documentation from the dataset
    }
  
    private void formatSmlLocationPoint() {
        // get the lat/lon of the station
        // position
        String pos = this.stationData.getBoundLowerLat() + " " + this.stationData.getBoundLowerLon();
        platform.setSmlPosLocationPoint(this.getCrsName(), pos);
    }
    
    private void formatSmlLocationLine() {
        // get the lat/lon pairs for the station
        platform.setSmlPosLocationLine(this.getCrsName(), this.stationData.getLocationsString(0));
    }

    private void formatSmlLocationBbox() {
        for (Map.Entry<Integer,String> station : this.getStationNames().entrySet()) {
            String stationName = station.getValue();
            Integer stationKey = station.getKey();
            if (stationName.equalsIgnoreCase(this.stationName) ||
                this.getUrnName(stationName).equalsIgnoreCase(this.stationName)) {
                String lc = this.stationData.getLowerLat(stationKey) + " " + 
                             this.stationData.getLowerLon(stationKey);
                String uc = this.stationData.getUpperLat(stationKey) + " " + 
                             this.stationData.getUpperLon(stationKey);
                platform.setSmlPosLocationBbox(this.getCrsName(), lc, uc);
            }
        }
    }
    
    private void formatSmlComponents() {
        // create a component for each data variable
        String name, id, sensorUrn, url, description, outputName, definition, uom;
        for (ucar.nc2.VariableSimpleIF var : this.getDataVariables()) {
            name = "Sensor " + var.getShortName();
            id = "sensor-" + var.getShortName();
            sensorUrn = this.getSensorUrnName(this.stationName, var);
            description = null;
            Attribute insAtt = var.findAttributeIgnoreCase(INSTRUMENT);
            if(insAtt != null){
            	Variable instrument = this.getVariableByName(insAtt.getStringValue());
            	if(instrument != null){
            		Attribute instLongName = instrument.findAttributeIgnoreCase(LONG_NAME);
            		if(instLongName != null){
            			description = instLongName.getStringValue();
            		}
            	}
            }
            if(description == null){
            	Attribute instLongName = var.findAttributeIgnoreCase(LONG_NAME);
        		if(instLongName != null){
        			description = instLongName.getStringValue();
        		}
        		else 
        			description = ATTRIBUTE_MISSING;
            }
            // describe sensor url
            url = this.urlBase + QUERY + sensorUrn;
          
            platform.addSmlComponent(name, id, description, sensorUrn, url);
            // set the poutput for the component
            outputName = this.getVariableStandardName(var.getShortName());
            definition = getHrefForParameter(this.checkForRequiredValue(var, STANDARD_NAME));
            uom = this.checkForRequiredValue(var, "units");
            platform.addSmlOuptutToComponent(name, outputName, definition, uom);
        }
    }
    //</editor-fold>
    
    private void setStationData() throws IOException {
        String cStationName = this.stationName;
        if(this.getUrnToStationName().containsKey(this.stationName)){
        	cStationName = this.getUrnToStationName().get(this.stationName);
        }
        switch(this.getDatasetFeatureType()) {
            case STATION:
                this.stationData = new TimeSeries(new String[] {cStationName}, null, null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                break;
            case STATION_PROFILE:
                this.stationData = new TimeSeriesProfile(new String[] {cStationName }, null, 
                                                         null, false, false, false, null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                break;
            case PROFILE:
                this.stationData = new Profile(new String[] {cStationName }, null, null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                break;
            case TRAJECTORY:
                this.stationData = new Trajectory(new String[] {cStationName },null,null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                this.locationLineFlag = true;
                break;
            case SECTION:
                this.stationData = new Section(new String[] {cStationName }, null, null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                this.locationLineFlag = true;
                break;
            case GRID:
                HashMap<String,String> latLon = new HashMap<String, String>();
                latLon.put(Grid.LAT, this.getGridDataset().getBoundingBox().getLatMin() + "_" + this.getGridDataset().getBoundingBox().getLatMax());
                latLon.put(Grid.LON, this.getGridDataset().getBoundingBox().getLonMin() + "_" + this.getGridDataset().getBoundingBox().getLonMax());
                List<String> dataVars = new ArrayList<String>();
                for (VariableSimpleIF var : this.getDataVariables()) {
                    dataVars.add(var.getShortName());
                }
                this.stationData = new Grid(new String[] {cStationName.replaceAll("[A-Za-z]+", "") }, null, dataVars.toArray(new String[dataVars.size()]), latLon);
                this.stationData.setData(this.getGridDataset());
                break;
            default:
                logger.error("Unsupported feature type in Describe Platform M1_0: " + this.getDatasetFeatureType().toString());
                this.errorString = "Unsupported feature type for DS response";
        }
    }
    
}
