/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 *
 * @author SCowan
 */
public final class VocabDefinitions {
    
    private static final String CF_PARAMETERS = "resources/cf_parameters.txt";
    private static HashSet<String> cfSet;
    private static HashSet<String> ioosDefs;
    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(VocabDefinitions.class);
    
    private VocabDefinitions() {}
    
    /**
     * Determines the necessary term for the parameter.
     * @param param the name of the parameter to look for, expected lowercase w/ "_"
     * @return if in CF table: http://mmissw.org/ont/cf/parameter/param else http://mmisw.org/ont/ioos/parameter/param
     */
    public static String GetDefinitionForParameter(String param) {
        if (cfSet == null)
            CreateCFSet();
        
        if (cfSet.contains(param))
            return "http://mmisw.org/ont/cf/parameter/" + param;
        
        // default
        return "http://mmisw.org/ont/ioos/parameter/" + param;
    }
    
    public static String GetIoosDefinition(String def) {
        if (ioosDefs == null)
            CreateIoosDefs();
        
        if (ioosDefs.contains(def.toLowerCase()))
            return "http://mmisw.org/ont/ioos/definition/" + def;
            
        return def;
    }
    
    private static void CreateCFSet() {
        try {
            InputStream fin = VocabDefinitions.class.getClassLoader().getResourceAsStream(CF_PARAMETERS);
            InputStreamReader freader = new InputStreamReader(fin);
//            FileReader fin = new FileReader(CF_PARAMETERS);
            cfSet = new HashSet<String>();
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[1];
            while(freader.read(buffer) > 0) {
                if (buffer[0] == ';') {
                    cfSet.add(builder.toString());
                    builder.setLength(0);
                } else {
                    builder.append(buffer[0]);
                }
            }
        } catch (Exception ex) {
            _log.error(ex.toString());
        }
    }
    
    private static void CreateIoosDefs() {
        // short list of definitions at http://mmisw.org/ont/ioos/definition
        ioosDefs = new HashSet<String>();
        // longName
        ioosDefs.add("longname"); ioosDefs.add("long_name"); ioosDefs.add("long name");
        // networkId
        ioosDefs.add("networkid"); ioosDefs.add("network_id"); ioosDefs.add("network id");
        // operator
        ioosDefs.add("operator");
        // operatorSector
        ioosDefs.add("operatorsector"); ioosDefs.add("operator_sector"); ioosDefs.add("operator sector");
        // parentNetwork
        ioosDefs.add("parentnetwork"); ioosDefs.add("parent_network"); ioosDefs.add("parent network");
        // platformType
        ioosDefs.add("platformtype"); ioosDefs.add("platform_type"); ioosDefs.add("platform type");
        // publisher
        ioosDefs.add("publisher");
        // qualityControlDescription
        ioosDefs.add("qualitycontroldescription"); ioosDefs.add("quality_control_description"); ioosDefs.add("quality control description");
        // sensorID
        ioosDefs.add("sensorid"); ioosDefs.add("sensor_id"); ioosDefs.add("sensor id");
        // shortName
        ioosDefs.add("shortname"); ioosDefs.add("short_name"); ioosDefs.add("short name");
        // sponsor
        ioosDefs.add("sponsor");
        // stationID
        ioosDefs.add("stationid"); ioosDefs.add("station_id"); ioosDefs.add("station id");
        // wmoID
        ioosDefs.add("wmoid"); ioosDefs.add("wmo_id"); ioosDefs.add("wmo id");
    }
    
}
