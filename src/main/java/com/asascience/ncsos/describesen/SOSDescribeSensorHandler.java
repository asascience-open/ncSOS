/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.geotoolkit.util.collection.CheckedHashMap;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSDescribeSensorHandler extends SOSBaseRequestHandler {
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSDescribeSensorHandler.class);
    private String procedure;
    private String description;
    private ArrayList<Attribute> contactInfo;
    private ArrayList<Attribute> documentationInfo;
    
    // stuff needed for station vars/requests
    private Variable stationVar;
    private Attribute platformType;
    private ArrayList<Attribute> historyInfo;
    private double[] stationCoords;
    private String[] components;
//    private Variable historyVar;  might want to consider having some vars hold info regarding history
    
    private final String ACCEPTABLE_RESPONSE_FORMAT = "text/xml;subtype=\"sensorML/1.0.1\"";
    
    /**
     * Creates a DescribeSensor handler that will parse the information and setup the output handle
     * @param dataset
     * @param responseFormat
     * @param procedure
     * @throws IOException 
     */
    public SOSDescribeSensorHandler(NetcdfDataset dataset, String responseFormat, String procedure) throws IOException {
        super(dataset);
        
        output = new DescribeSensorFormatter();
        
        // make sure that the responseFormat we recieved is acceptable
        if (!responseFormat.equalsIgnoreCase(ACCEPTABLE_RESPONSE_FORMAT)) {
            // return exception
            output.setupExceptionOutput("Unhandled response format " + responseFormat);
            return;
        }
        
        this.procedure = procedure;
        
        description = dataset.findAttValueIgnoreCase(null, "description", "empty");
        
        getContactInfoAttributes(dataset.getGlobalAttributes());
        getDocumentationAttributes(dataset.getGlobalAttributes());
        
        // find out needed info based on whether this is a station or sensor look up
        if (this.procedure.contains("station")) {
            setNeededInfoForStation(dataset);
            // setup our output after having collected all of our needed info
            DescribeSensorFormatter formatter = (DescribeSensorFormatter)output;
            formatSetIdentification(formatter);
            // set our classification
            if (platformType != null) {
                formatter.addToClassificationNode(platformType.getName(), "", platformType.getStringValue());
            } else {
                formatter.deleteClassificationNode();
            }
            // set our contact node(s)
            // set documentation -- temporary just delete it
            formatter.deleteDocumentationNode();
            // set history
            if (historyInfo.size() < 1) {
                formatter.deleteHistoryNode();
            } else {
                ArrayList<String> name, description, date, url;
                name = new ArrayList<String>();
                description = new ArrayList<String>();
                date = new ArrayList<String>();
                url = new ArrayList<String>();
                for (Iterator<Attribute> it = historyInfo .iterator(); it.hasNext();) {
                    Attribute attr = it.next();
                    name.add(attr.getName());
                    description.add(attr.getStringValue());
                    date.add(null);
                    url.add(null);
                }
                formatter.setHistoryEvents(name.toArray(new String[name.size()]),
                        date.toArray(new String[date.size()]),
                        description.toArray(new String[description.size()]),
                        url.toArray(new String[url.size()]));
            }
            // set location
            formatter.setLocationNode((procedure.split(":"))[procedure.length()-1], stationCoords);
            // set components
            HashMap<String, HashMap<String, String>> componentMap = new HashMap<String, HashMap<String, String>>();
            for (String str : components) {
                HashMap<String, String> internal = new HashMap<String, String>();
                // add identification, documentation, system
                internal.put("identification", str);
                internal.put("documentation", str);
                internal.put("system", str);
                componentMap.put(str, internal);
            }
            formatter.setComponentsNode(componentMap);
            
        } else if (this.procedure.contains("sensor")) {
            setNeededInfoForSensor(dataset);
        } else {
            output.setupExceptionOutput("Unknown procedure (not a station or sensor): " + this.procedure);
            return;
        }
    }

    /**
     * Exception version, used to create skeleton SOSDescribeSensorHandler that can throw an exception
     * @param dataset dataset, mostly unused
     * @throws IOException 
     */
    public SOSDescribeSensorHandler(NetcdfDataset dataset) throws IOException {
        super(dataset);
    }
    
    private void formatSetIdentification(DescribeSensorFormatter formatter) {
        formatter.setDescriptionNode(description);
            ArrayList<String> identNames = new ArrayList<String>();
            ArrayList<String> identDefinitions = new ArrayList<String>();
            ArrayList<String> identValues = new ArrayList<String>();
            identNames.add("StationId"); identDefinitions.add("stationID"); identValues.add(procedure);
            for (Attribute attr : stationVar.getAttributes()) {
                identNames.add(attr.getName()); identDefinitions.add(""); identValues.add(attr.getStringValue());
            }
            formatter.setIdentificationNode(identNames.toArray(new String[identNames.size()]),
                    identDefinitions.toArray(new String[identDefinitions.size()]),
                    identValues.toArray(new String[identValues.size()]));
    }
    
    private void setNeededInfoForStation( NetcdfDataset dataset ) throws IOException {
        // get our station variable
        for (Variable var : dataset.getVariables()) {
            if (var.getFullName().matches("(station)[_]*(name)")) {
                stationVar = var;
                break;
            }
        }
        
        // classification
        platformType = dataset.findGlobalAttributeIgnoreCase("platformtype");
        
        // get history attributes
        historyInfo = new ArrayList<Attribute>();
        for (Attribute attr : dataset.getGlobalAttributes()) {
            String name = attr.getName();
            if (name.contains("history") || name.contains("deployment")) {
                historyInfo.add(attr);
            }
        }
        
        // get the lat/lon of the station
        // station id should be the last value in the procedure
        String stationId = (procedure.split(":"))[0];
        int stationIndex = -1;
        // get the station index in the array
        Array stationArray = stationVar.read();
        for (int i=0; i<stationArray.getSize(); i++) {
            if (stationArray.getObject(i).toString().equalsIgnoreCase(stationId)) {
                stationIndex = i;
                break;
            }
        }
        
        if (stationIndex < 0) {
            output.setupExceptionOutput("Unable to find index of station: " + stationId);
            return;
        }
        
        // find lat/lon values for the station
        double lat, lon;
        lat = lon = Double.NaN;
        for (Variable var : dataset.getVariables()) {
            if (var.getFullName().toLowerCase().contains("lat")) {
                lat = var.read().getDouble(stationIndex);
            }
            if (var.getFullName().toLowerCase().contains("lon")) {
                lon = var.read().getDouble(stationIndex);
            }
        }
        
        // set our station coords
        stationCoords = new double[] { lat, lon };
        
        // lastly get our components
        ArrayList<String> dataVarNames = new ArrayList<String>();
        for (Iterator<VariableSimpleIF> it = getFeatureDataset().getDataVariables().iterator(); it.hasNext();) {
            Variable var = (Variable) it.next();
            dataVarNames.add(var.getFullName());
        }
        components = new String[dataVarNames.size()];
        for (int i=0; i<components.length; i++) {
            components[i] = dataVarNames.get(i);
        }
    }
    
    private void setNeededInfoForSensor( NetcdfDataset dataset ) {
        
    }
    
    private void getContactInfoAttributes( List<Attribute> globalAttrs ) {
        // look for creator attributes, as well as institution
        contactInfo = new ArrayList<Attribute>();
        for (Attribute attr : globalAttrs) {
            String name = attr.getName();
            if (name.contains("creator") ||
                    name.contains("institution") ||
                    name.contains("author") || 
                    name.contains("contact")) {
                contactInfo.add(attr);
            }
        }
    }
    
    private void getDocumentationAttributes( List<Attribute> globalAttrs ) {
        // look for any attributes related to documentation
        documentationInfo = new ArrayList<Attribute>();
        for (Attribute attr : globalAttrs) {
            String name = attr.getName();
            if (name.contains("doc")) {
                documentationInfo.add(attr);
            }
        }
    }
}
