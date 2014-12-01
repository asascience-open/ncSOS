package com.asascience.ncsos.ds;

import com.asascience.ncsos.cdmclasses.*;
import com.asascience.ncsos.outputformatter.ErrorFormatter;
import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.outputformatter.ds.IoosNetwork10Formatter;
import com.asascience.ncsos.util.ListComprehension;
import com.asascience.ncsos.util.LogReporter;
import com.asascience.ncsos.util.VocabDefinitions;
import ucar.nc2.Attribute;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IoosNetwork10Handler extends Ioos10Handler implements BaseDSInterface {
    
    private final String procedure;
    private final String server;
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IoosNetwork10Handler.class);
    
    private IoosNetwork10Formatter network;
    private String errorString;
    private iStationData stationData;
    
    public IoosNetwork10Handler(NetcdfDataset dataset) throws IOException {
        super(dataset);
        
        this.procedure = null;
        this.server = null;
        setStationData();
    }
    
    public IoosNetwork10Handler(NetcdfDataset dataset, String procedure, String serverURL) throws IOException {
        super(dataset, new LogReporter());
        
        this.procedure = procedure;
        this.server = serverURL;
        setStationData();
    }

    public void setupOutputDocument(OutputFormatter format) throws IOException {
        if (!checkForProcedure(this.procedure)) {
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException("Invalid procedure: " + this.procedure, INVALID_PARAMETER, "procedure");
        } else if (errorString != null) {
            formatter = new ErrorFormatter();
            ((ErrorFormatter)formatter).setException(errorString);
        } else {
            try {
                this.network = (IoosNetwork10Formatter) format;
                describeNetwork();
            } catch (Exception ex) {
                logger.error(ex.toString());
                ex.printStackTrace();
            }
        }
    }
    
    private void describeNetwork() {
        this.network.setVersionMetadata();
        this.network.setDescriptionNode((String)this.getGlobalAttribute("description", "No description found"));
        this.network.setName(this.procedure);
        this.network.removeSmlLocation();
        this.formatSmlIdentification();
        this.formatSmlClassification();
        this.formatSmlValidTime();
        this.formatSmlContacts();
        this.formatGmlBoundedBy();
        this.foramtSmlComponents();
    }
    
    private void formatSmlIdentification() {
        network.addSmlIdentifier("networkID", VocabDefinitions.GetIoosDefinition("networkID"), this.procedure);
        network.addSmlIdentifier("shortName", VocabDefinitions.GetIoosDefinition("shortName"), (String)this.getGlobalAttribute("shortName", "SOS station assets collection of the dataset"));
        network.addSmlIdentifier("longName", VocabDefinitions.GetIoosDefinition("longName"), (String)this.getGlobalAttribute("longName", this.procedure + " Collaction of all station assets available in dataset"));
    }
    
    private void formatSmlClassification() {
        // add platformType, operatorSector and publisher classifications (assuming they are global variables
        network.addSmlClassifier("platformType", VocabDefinitions.GetIoosDefinition("platformType"), 
                "platform", this.checkForRequiredValue("platform_type"));
        network.addSmlClassifier("operatorSector", VocabDefinitions.GetIoosDefinition("operatorSector"), "sector", this.checkForRequiredValue("operator_sector"));
        network.addSmlClassifier("publisher", VocabDefinitions.GetIoosDefinition("publisher"), "organization", this.checkForRequiredValue("publisher"));
        network.addSmlClassifier("parentNetwork", "http://mmisw.org/ont/ioos/definition/parentNetwork", 
                "organization", (String)this.getGlobalAttribute(INSTITUTION, ATTRIBUTE_MISSING ));
        
        // sponsor is optional
        String value = (String)this.getGlobalAttribute("sponsor", null);
        if (value != null) {
            network.addSmlClassifier("sponsor", VocabDefinitions.GetIoosDefinition("sponsor"), "organization", value);
        }
    }
    
    private void formatSmlValidTime() {
        network.setValidTime(this.stationData.getBoundTimeBegin(), this.stationData.getBoundTimeEnd());
    }
    
    private void formatSmlContacts() {
        // waiting for some Q's to be answered from kyle
        // operator == creator (mandatory)
        // publisher == publisher (mandatory)
        // sponsor == sponsor (optional)
        
        HashMap<String, HashMap<String,String>> contactInfo = new HashMap<String, HashMap<String,String>>();
        String role = "http://mmisw.org/ont/ioos/definition/operator";
        String org = this.checkForRequiredValue("creator_name");
        String url = (String)this.getGlobalAttribute("creator_url", null);
        HashMap<String, String> address = createAddressForContact("creator");
        contactInfo.put("address", address);
        HashMap<String, String> phone = new HashMap<String, String>();
        phone.put("voice", (String)this.getGlobalAttribute("creator_phone", null));
        contactInfo.put("phone", phone);
        network.addContactNode(role, org, contactInfo, url);

        contactInfo.clear();
        phone.clear();

        role = "http://mmisw.org/ont/ioos/definition/publisher";
        org = this.checkForRequiredValue("publisher_name");
        url = (String)this.getGlobalAttribute("publisher_url", null);
        address = createAddressForContact("publisher");
        contactInfo.put("address", address);
        phone.put("voice", (String)this.getGlobalAttribute("publisher_phone", null));
        contactInfo.put("phone", phone);
        network.addContactNode(role, org, contactInfo, url);
    }
    
    private LinkedHashMap<String,String> createAddressForContact(String contactPrefix) {
        LinkedHashMap<String,String> address = new LinkedHashMap<String, String>();
        address.put("deliveryPoint", (String)this.getGlobalAttribute(contactPrefix + "_address", null));
        address.put("city", (String)this.getGlobalAttribute(contactPrefix + "_city", null));
        address.put("administrativeArea", (String)this.getGlobalAttribute(contactPrefix + "_state", null));
        address.put("postalCode", (String)this.getGlobalAttribute(contactPrefix+"_zipcode", null));
        address.put("country", this.checkForRequiredValue(contactPrefix + "_country"));
        address.put("electronicMailAddress", this.checkForRequiredValue(contactPrefix + "_email"));
        return address;
    }
    
    private void setStationData() throws IOException {
        List<String> stationNames = new ArrayList<String>(this.getStationNames().size());
        for (String str : this.getStationNames().values()) {
            stationNames.add(str);
        }
        switch(this.getDatasetFeatureType()) {
            case STATION:
                this.stationData = new TimeSeries(stationNames.toArray(new String[stationNames.size()]), null, null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                break;
            case STATION_PROFILE:
                this.stationData = new TimeSeriesProfile(stationNames.toArray(new String[stationNames.size()]), null, null,
                                    false, false, false, null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                break;
            case PROFILE:
                // remove 'Profile' from the station names, since they are arbitrary
                /*
                stationNames = ListComprehension.map(stationNames, new ListComprehension.Func<String, String>() {
                    public String apply(String in) {
                        return in.replaceAll("[A-Za-z]+", "");
                    }
                });
                */
                this.stationData = new Profile(stationNames.toArray(new String[stationNames.size()]), null, null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                break;
            case TRAJECTORY:
                // remove 'Trajectory' from the station names, since they are arbitrary
                /*
                stationNames = ListComprehension.map(stationNames, new ListComprehension.Func<String, String>() {
                    public String apply(String in) {
                        return in.replaceAll("[A-Za-z]+", "");
                    }
                });
                */
                this.stationData = new Trajectory(stationNames.toArray(new String[stationNames.size()]),null,null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                break;
            case SECTION:
                // remove 'Trajectory' from the station names, since they are arbitrary
                /*
                stationNames = ListComprehension.map(stationNames, new ListComprehension.Func<String, String>() {
                    public String apply(String in) {
                        return in.replaceAll("[A-Za-z]+", "");
                    }
                });
                */
                this.stationData = new Section(stationNames.toArray(new String[stationNames.size()]), null, null);
                this.stationData.setData(this.getFeatureTypeDataSet());
                break;
            case GRID:
                List<String> dataVars = new ArrayList<String>();
                for (VariableSimpleIF var : this.getDataVariables()) {
                    dataVars.add(var.getShortName());
                }
                HashMap<String,String> latLon = new HashMap<String, String>();
                latLon.put(Grid.LAT, this.getGridDataset().getBoundingBox().getLatMin() + "_" + this.getGridDataset().getBoundingBox().getLatMax());
                latLon.put(Grid.LON, this.getGridDataset().getBoundingBox().getLonMin() + "_" + this.getGridDataset().getBoundingBox().getLonMax());
                this.stationData = new Grid(stationNames.toArray(new String[stationNames.size()]), null, dataVars.toArray(new String[dataVars.size()]), latLon);
                this.stationData.setData(this.getGridDataset());
                break;
            case POINT:
                logger.error("NcSOS does not support the Point featureType at this time.");
                this.errorString = "NcSOS does not support the Point featureType at this time.";
                break;
            default:
                logger.error("Unsupported feature type: " + this.getDatasetFeatureType().toString());
                this.errorString = "Unsupported feature type: " + this.getDatasetFeatureType().toString();
                break;
        }
    }

    private void formatGmlBoundedBy() {
        network.setBoundedBy(this.getCrsName(), 
                             this.stationData.getBoundLowerLat() + " " + this.stationData.getBoundLowerLon(), 
                             this.stationData.getBoundUpperLat() + " " + this.stationData.getBoundUpperLon());
    }

    private void foramtSmlComponents() {
        if (this.getStationNames().size() > 1) {
            this.formatMultipleComponents();
        } else {
            this.formatSingleComponent();
        }
    }
    
    private String GetIoosDef(String def) {
        return VocabDefinitions.GetIoosDefinition(def);
    }

    private void formatMultipleComponents() {
        for (Map.Entry<Integer,String> station : this.getStationNames().entrySet()) {
            network.addSmlComponent(station.getValue());
            // identifiers for station
            String stationNameFixed = station.getValue().replaceAll("[Pp]rofile|[Tt]rajectory", "");
            // stationID
            network.addIdentifierToComponent(station.getValue(), "stationID", 
            		VocabDefinitions.GetIoosDefinition("stationID"),
                    this.getUrnName(station.getValue()));
            // shortName
            VariableSimpleIF pVar = this.checkForRequiredVariable("platform_short_name");
            network.addIdentifierToComponent(station.getValue(), "shortName", 
            		VocabDefinitions.GetIoosDefinition("shortName"),
            		this.checkForRequiredValue(pVar, stationNameFixed));

            // longName
            pVar = this.checkForRequiredVariable("platform_long_name");
            network.addIdentifierToComponent(station.getValue(), "longName", 
            		VocabDefinitions.GetIoosDefinition("longName"), 
            		this.checkForRequiredValue(pVar, stationNameFixed));

            // wmoid if it exists
            pVar = this.getVariableByName("platform_wmo_code");
            if (pVar != null) {
                network.addIdentifierToComponent(station.getValue(), "wmoId", 
                		VocabDefinitions.GetIoosDefinition("wmoId"), this.checkForRequiredValue(pVar, stationNameFixed));
            }
            // valid time
            network.setComponentValidTime(station.getValue(), this.stationData.getTimeBegin(station.getKey()), this.stationData.getTimeEnd(station.getKey()));
            // location
            if (this.getGridDataset() == null) {
                List<String> locations = this.stationData.getLocationsString(station.getKey());
                if (locations.size() > 1) {
                    network.setComponentLocation(station.getValue(), this.getCrsName(), locations);
                } else if (locations.size() > 0) {
                    network.setComponentLocation(station.getValue(), this.getCrsName(), locations.get(0));
                } else {
                    logger.error("Did not get locations for station data");
                }
            } else {
                // GRID dataset
                String lowerCorner = this.stationData.getLowerLat(station.getKey()) + " " + this.stationData.getLowerLon(station.getKey());
                String upperCorner = this.stationData.getUpperLat(station.getKey()) + " " + this.stationData.getUpperLon(station.getKey());
                network.setComponentLocation(station.getValue(), this.getCrsName(), lowerCorner, upperCorner);
            }
            // outputs
            for (VariableSimpleIF var : this.getDataVariables()) {
                String name = var.getShortName();
                String title = this.procedure.substring(0,this.procedure.lastIndexOf(":")+1) + station.getValue() + ":" + name.replaceAll("\\s+", "_");
                String def = VocabDefinitions.GetDefinitionForParameter(this.checkForRequiredValue(var, "standard_name"));
                String units = this.checkForRequiredValue(var, "units");
                network.addComponentOutput(station.getValue(), name, title, def, (String)this.getGlobalAttribute("featureType"), units);
            }
        }
    }

    private void formatSingleComponent() {
        String strPlatform = (String)this.getGlobalAttribute("platform", null);
        ucar.nc2.Variable identVar;
        if (strPlatform != null) {
            identVar = this.getVariableByName(strPlatform);
        } else { 
            // use the station variable
            identVar = this.stationVariable;
        }
        
        for (Map.Entry<Integer,String> station : this.getStationNames().entrySet()) {
            network.addSmlComponent(station.getValue());
            // identifiers for station
            network.addIdentifierToComponent(station.getValue(), "stationID", GetIoosDef("stationID"), 
                    this.getUrnName(station.getValue()));
            network.addIdentifierToComponent(station.getValue(), "shortName", GetIoosDef("shortName"), 
                    this.checkForRequiredValue(identVar, "short_name"));
            network.addIdentifierToComponent(station.getValue(), "longName", GetIoosDef("longName"), 
                    this.checkForRequiredValue(identVar, "long_name"));
            // wmoid, if it exists
            Attribute identAtt = identVar.findAttribute("wmo_code");
            if (identAtt != null) {
                network.addIdentifierToComponent(station.getValue(), "wmoID", VocabDefinitions.GetIoosDefinition("wmoID"), identAtt.getStringValue());
            }
            // valid time
            network.setComponentValidTime(station.getValue(), this.stationData.getTimeBegin(station.getKey()), this.stationData.getTimeEnd(station.getKey()));
            // location
            if (this.getGridDataset() == null) {
                List<String> locations = this.stationData.getLocationsString(station.getKey());
                if (locations.size() > 1) {
                    network.setComponentLocation(station.getValue(), this.getCrsName(), locations);
                } else if (locations.size() > 0) {
                    network.setComponentLocation(station.getValue(), this.getCrsName(), locations.get(0));
                } else {
                    logger.error("Did not get locations for station data");
                }
            } else {
                // GRID data
                String lowerCorner = ((Grid)this.stationData).getLowerLat() + " " + ((Grid)this.stationData).getLowerLon();
                String upperCorner = ((Grid)this.stationData).getUpperLat() + " " +((Grid)this.stationData).getUpperLon();
                network.setComponentLocation(station.getValue(), this.getCrsName(), lowerCorner, upperCorner);
            }
            // outputs
            for (VariableSimpleIF var : this.getDataVariables()) {
                String name = var.getShortName();
                String title = this.procedure.substring(0,this.procedure.lastIndexOf(":")+1) + station.getValue() + ":" + name.replaceAll("\\s+", "_");
                String def = VocabDefinitions.GetDefinitionForParameter(this.checkForRequiredValue(var, "standard_name"));
                String units = this.checkForRequiredValue(var, "units");
                network.addComponentOutput(station.getValue(), name, title, def, (String)this.getGlobalAttribute("featureType"), units);
            }
        }
    }
}
