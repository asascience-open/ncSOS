/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSDescribeStation implements SOSDescribeIF {
    
    protected Variable stationVariable;
    protected Attribute platformType;
    protected String stationName;
    protected String description;
    protected double[] stationCoords;
    protected ArrayList<Variable> contactVariables;
    protected ArrayList<Variable> documentVariables;
    protected ArrayList<Variable> historyVariables;
    protected final String procedure;
    
    public SOSDescribeStation( NetcdfDataset dataset, String procedure ) {
        Variable lat, lon;
        lat = lon = null;
        // get desired variables
        for (Variable var : dataset.getVariables()) {
            if (var.getFullName().matches("(station)[_]*(name)")) {
                stationVariable = var;
            }
            else if (var.getFullName().toLowerCase().contains("lat")) {
                lat = var;
            }
            else if (var.getFullName().toLowerCase().contains("lon")) {
                lon = var;
            }
            else if (var.getFullName().toLowerCase().contains("contact")) {
                if (contactVariables == null)
                    contactVariables = new ArrayList<Variable>();
                contactVariables.add(var);
            }
            else if (var.getFullName().toLowerCase().contains("doc")) {
                if (documentVariables == null)
                    documentVariables = new ArrayList<Variable>();
                documentVariables.add(var);
            }
            else if (var.getFullName().toLowerCase().contains("history")) {
                if (historyVariables == null)
                    historyVariables = new ArrayList<Variable>();
                historyVariables.add(var);
            }
        }
        
        this.procedure = procedure;
        
        String[] procSplit = procedure.split(":");
        stationName = procSplit[procSplit.length - 1];
        
        // get our platform type
        platformType = dataset.findGlobalAttributeIgnoreCase("platformtype");
        
        // set our coords
        stationCoords = getStationCoords(lat, lon);
        
        // description
        description = dataset.findAttValueIgnoreCase(null, "description", "no description");

    }

    /*********************
     * Interface Methods *
     *********************/
    public void SetupOutputDocument(DescribeSensorFormatter output) {
        // system node
        output.setSystemId("station-" + stationName);
        // set description
        formatSetDescription(output);
        // identification node
        formatSetIdentification(output);
        // classification node
        formatSetClassification(output);
        // contact node
        formatSetContactNodes(output);
        // document node
        formatSetDocumentNodes(output);
        // history node
        formatSetHistoryNodes(output);
        // location node
        formatSetLocationNode(output);
        // remove unwanted nodes
        RemoveUnusedNodes(output);
    }
    
    /*******************
     * Private/Protected Methods *
     *******************/

    protected void formatSetClassification(DescribeSensorFormatter output) {
        if (platformType != null) {
            output.addToClassificationNode(platformType.getName(), "", platformType.getStringValue());
        } else {
            output.deleteClassificationNode();
        }
    }

    protected void formatSetContactNodes(DescribeSensorFormatter output) {
        if (contactVariables != null) {
            HashMap<String, HashMap<String, String>> domainContactInfo;
            HashMap<String, String> subDomainInfo;
            String role, orginizationName;
            for (Variable cVar : contactVariables) {
                domainContactInfo = new HashMap<String, HashMap<String, String>>();
                role = orginizationName = "";
                // iterate through attributes of contact variable to get desired info
                for (Attribute attr : cVar.getAttributes()) {
                    if (attr.getName().contains("role")) {
                        role = attr.getStringValue();
                    } else if (attr.getName().contains("orginization")) {
                        orginizationName = attr.getStringValue();
                    } else {
                        // split on '_'; first index is domain name, second is node name
                        String[] attrSplit = attr.getName().split("_");
                        if (attrSplit.length < 2) {
                            output.setupExceptionOutput("error in contact attribute: " + attr.getName());
                            return;
                        }
                        if (domainContactInfo.containsKey(attrSplit[0])) {
                            ((HashMap<String,String>)domainContactInfo.get(attrSplit[0])).put(attrSplit[1], attr.getStringValue());
                        } else {
                            subDomainInfo = new HashMap<String, String>();
                            subDomainInfo.put(attrSplit[1],attr.getStringValue());
                            domainContactInfo.put(attrSplit[0],subDomainInfo);
                        }
                    }
                }
                output.addContactNode(role, orginizationName, domainContactInfo);
            }
        }
    }
    
    protected void formatSetIdentification(DescribeSensorFormatter formatter) {
        ArrayList<String> identNames = new ArrayList<String>();
        ArrayList<String> identDefinitions = new ArrayList<String>();
        ArrayList<String> identValues = new ArrayList<String>();
        identNames.add("StationId"); identDefinitions.add("stationID"); identValues.add(procedure);
        for (Attribute attr : stationVariable.getAttributes()) {
            identNames.add(attr.getName()); identDefinitions.add(""); identValues.add(attr.getStringValue());
        }
        formatter.setIdentificationNode(identNames.toArray(new String[identNames.size()]),
                identDefinitions.toArray(new String[identDefinitions.size()]),
                identValues.toArray(new String[identValues.size()]));
    }
    
    private double[] getStationCoords(Variable lat, Variable lon) {
        try {
            // get the lat/lon of the station
            // station id should be the last value in the procedure
            int stationIndex = -1;
            // get the station index in the array
            char[] charArray = (char[]) stationVariable.read().get1DJavaArray(char.class);
            // find the length of the strings, assumes that the array has only a rank of 2; string length should be the 1st index
            int[] aShape = stationVariable.read().getShape();
            String[] names = new String[aShape[0]];
            StringBuilder strB = null;
            int ni = 0;
            for (int i=0;i<charArray.length;i++) {
                if(i % aShape[1] == 0) {
                    if (strB != null)
                        names[ni++] = strB.toString();
                    strB = new StringBuilder();
                }
                // ignore null
                if (charArray[i] != '\u0000')
                    strB.append(charArray[i]);
            }
            // now find our station index
            for (int j=0; j<names.length; j++) {
                if(names[j].equalsIgnoreCase(stationName)) {
                    stationIndex = j;
                    break;
                }
            }

            if (stationIndex < 0) {
                return null;
            }

            // find lat/lon values for the station
            double[] coords = new double[] { Double.NaN, Double.NaN };
            coords[0] = lat.read().getDouble(stationIndex);
            coords[1] = lon.read().getDouble(stationIndex);

            return coords;
        } catch (Exception e) {
            System.out.println("exception in getStationCoords " + e.getMessage());
            return null;
        }
    }
    
    private void RemoveUnusedNodes(DescribeSensorFormatter output) {
        output.deletePosition();
        output.deleteTimePosition();
    }

    protected void formatSetDescription(DescribeSensorFormatter output) {
        output.setDescriptionNode(description);
    }

    protected void formatSetDocumentNodes(DescribeSensorFormatter output) {
        if (documentVariables != null) {
            // add document nodes for each variable
            ArrayList<String> descriptions, docs, formats;
            formats = new ArrayList<String>();
            descriptions = new ArrayList<String>();
            docs = new ArrayList<String>();
            for (Variable var : historyVariables) {
                String format, description, doc;
                format = description = doc = "";
                for (Attribute attr : var.getAttributes()) {
                    if (attr.getName().toLowerCase().contains("format")) {
                        format = attr.getStringValue();
                    } else if (attr.getName().toLowerCase().contains("description")) {
                        description = attr.getStringValue();
                    } else {
                        doc = attr.getStringValue();
                    }
                }
                formats.add(format);
                descriptions.add(description);
                docs.add(doc);
            }
            output.setDocumentationNode(descriptions.toArray(new String[descriptions.size()]),
                    formats.toArray(new String[formats.size()]),
                    docs.toArray(new String[docs.size()]));
        } else {
            output.deleteDocumentationNode();
        }
    }

    protected void formatSetHistoryNodes(DescribeSensorFormatter output) {
        if (historyVariables != null) {
            // add history nodes for each variable
            ArrayList<String> names, descriptions, dates, docs;
            names = new ArrayList<String>();
            descriptions = new ArrayList<String>();
            dates = new ArrayList<String>();
            docs = new ArrayList<String>();
            for (Variable var : historyVariables) {
                String name, description, date, doc;
                name = description = date = doc = "";
                for (Attribute attr : var.getAttributes()) {
                    if (attr.getName().toLowerCase().contains("name")) {
                        name = attr.getStringValue();
                    } else if (attr.getName().toLowerCase().contains("description")) {
                        description = attr.getStringValue();
                    } else if (attr.getName().toLowerCase().contains("date")) {
                        date = attr.getStringValue();
                    } else {
                        doc = attr.getStringValue();
                    }
                }
                names.add(name);
                descriptions.add(description);
                dates.add(date);
                docs.add(doc);
            }
            output.setHistoryEvents(names.toArray(new String[names.size()]),
                    dates.toArray(new String[dates.size()]),
                    descriptions.toArray(new String[descriptions.size()]),
                    docs.toArray(new String[docs.size()]));
        } else {
            output.deleteHistoryNode();
        }
    }

    protected void formatSetLocationNode(DescribeSensorFormatter output) {
        output.setLocationNode(stationName, stationCoords);
    }
}
