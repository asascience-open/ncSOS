/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos;

import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.service.Parser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class GOGridTest {
    
    private static final String defaultAuthority = "authority";
    
    private static final String sst_1 = "resources/datasets/satellite-sst/SST_Global_2x2deg_20120626_0000.nc";
    private static String sst_1_reqs = String.format("request=GetObservation&service=sos&acceptVersions=1.0.0&lat=-52.0&lon=0.0&observedProperty=sst&offering=network-all&procedure=urn:ioos:station:%1$s:Grid0&eventtime=1990-01-01T00:00:00Z/2013-05-17T09:57:00.000-04:00&responseformat=", defaultAuthority);
    private static final String sst_2 = "resources/datasets/satellite-sst/SST_Global_2x2deg_20120627_0000.nc";
    private static String sst_2_reqs = String.format("request=GetObservation&service=sos&acceptVersions=1.0.0&lat=-54.0,-52.0,-50.0&lon=-120.0,0.0,74.0&observedProperty=sst&offering=network-all&procedure=urn:ioos:station:%1$s:Grid0&eventtime=1990-01-01T00:00:00Z/2013-05-17T09:57:00.000-04:00&responseformat=", defaultAuthority);
    private static String sst_2_network_all = String.format("request=GetObservation&service=sos&acceptVersions=1.0.0&lat=-54.0,-52.0,-50.0&lon=-120.0,0.0,74.0&observedProperty=sst&offering=network-all&procedure=urn:ioos:network:%1$s:all&eventtime=1990-01-01T00:00:00Z/2013-05-17T09:57:00.000-04:00&responseformat=", defaultAuthority);
    
    // NODC Tests
    private static final String datasets = "resources/datasets/";
    private static final String sst_pathfinder = datasets + "nodc/00000110200000-NODC-L4_GHRSST-SSTskin-AVHRR_Pathfinder-PFV5.0_Daily_Climatology_1982_2008_DayNightCombined-v02.0-fv01.0.nc";
    private static String sst_pathfinder_grid0 = String.format("request=GetObservation&service=sos&acceptVersions=1.0.0&lat=56.3611&lon=176.2466&observedProperty=analysed_sst&offering=Grid0&procedure=urn:ioos:station:%1$s:Grid0&responseformat=", "org.ghrsst");
    private static String sst_pathfinder_network_all = String.format("request=GetObservation&service=sos&acceptVersions=1.0.0&lat=56.3611&lon=176.2466&observedProperty=sea_ice_fraction&offering=network-all&procedure=urn:ioos:network:%1$s:all&responseformat=", "org.ghrsst");
    
    private static String baseLocalDir = null;
    private static String outputDir = null;
    private static String exampleOutputDir = null;
    
    @BeforeClass
    public static void SetupEnviron() {
        // not really a test, just used to set up the various string values
        if (outputDir != null && baseLocalDir != null) {
            // exit early if the environ is already set
            return;
        }
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String container = "testConfiguration";
        InputStream templateInputStream = null;
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDirectory");
            outputDir += "get_obs/";
            baseLocalDir = XMLDomUtils.getNodeValue(configDoc, container, "projectDirectory");
            exampleOutputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDirectory");
            exampleOutputDir += "examples/";
        } catch (FileNotFoundException fnfex) {
            System.out.println(fnfex.getMessage());
        } finally {
            if (templateInputStream != null) {
                try {
                    templateInputStream.close();
                } catch (IOException e) {
                    // ignore, closing..
                }
            }
        }
        
        File file = new File(outputDir);
        file.mkdirs();
        
        file = new File(exampleOutputDir);
        file.mkdirs();
    }
    
    private static void fileWriter(String base, String fileName, Writer write) {
        try {
            File file = new File(base + fileName);
            Writer output = new BufferedWriter(new FileWriter(file));
            output.write(write.toString());
            output.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    private static String getCurrentMethod() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i=0; i<ste.length; i++) {
            if (ste[i].getMethodName().contains(("test")))
                return ste[i].getMethodName();
        }
        return "could not find test name";
    }
    
    @Test
    public void testGetObsGridSSTSingleLatLon() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + sst_1);
            Writer writer = new CharArrayWriter();
            Parser parser = new Parser();
            try {
                sst_1_reqs += URLEncoder.encode("text/xml;subtype=\"om/1.0.0\"", "UTF-8");
            } catch (Exception e) {
                System.out.println("couldn't encode for sst1 - " + e.getMessage());
            }
            HashMap<String, Object> outMap = parser.enhanceGETRequest(dataset, sst_1_reqs, baseLocalDir + sst_1);
            OutputFormatter output = (OutputFormatter)outMap.get("outputHandler");
            assertNotNull("output is null", output);
            output.writeOutput(writer);
            writer.flush();
            writer.close();
            fileWriter(outputDir, "testGetObsGridSSTSingleLatLon_output.xml", writer);
            // write as an example
            fileWriter(exampleOutputDir, "GetObservation-Grid-om1.0.0.xml", writer);
            assertFalse("Have an exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testGetObsGridSSTMultipleLatLon() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + sst_2);
            Writer writer = new CharArrayWriter();
            Parser parser = new Parser();
            try {
                sst_2_reqs += URLEncoder.encode("text/xml;subtype=\"om/1.0.0\"", "UTF-8");
            } catch (Exception e) {
                System.out.println("couldn't encode for sst1 - " + e.getMessage());
            }
            HashMap<String, Object> outMap = parser.enhanceGETRequest(dataset, sst_2_reqs, baseLocalDir + sst_2);
            OutputFormatter output = (OutputFormatter)outMap.get("outputHandler");
            assertNotNull("output is null", output);
            output.writeOutput(writer);
            writer.flush();
            writer.close();
            fileWriter(outputDir, "testGetObsGridSSTMultipleLatLon_output.xml", writer);
            assertFalse("Have an exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testGetObsGridNetworkAll() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + sst_2);
            Writer writer = new CharArrayWriter();
            Parser parser = new Parser();
            try {
                sst_2_network_all += URLEncoder.encode("text/xml;subtype=\"om/1.0.0\"", "UTF-8");
            } catch (Exception e) {
                System.out.println("couldn't encode for sst1 - " + e.getMessage());
            }
            HashMap<String, Object> outMap = parser.enhanceGETRequest(dataset, sst_2_network_all, baseLocalDir + sst_2);
            OutputFormatter output = (OutputFormatter)outMap.get("outputHandler");
            assertNotNull("output is null", output);
            output.writeOutput(writer);
            writer.flush();
            writer.close();
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            assertFalse("Have an exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testNodcPathfinderSSTGrid0() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + sst_pathfinder);
            Writer writer = new CharArrayWriter();
            Parser parser = new Parser();
            try {
                sst_pathfinder_grid0 += URLEncoder.encode("text/xml;subtype=\"om/1.0.0\"", "UTF-8");
            } catch (Exception e) {
                System.out.println("couldn't encode for sst1 - " + e.getMessage());
            }
            HashMap<String, Object> outMap = parser.enhanceGETRequest(dataset, sst_pathfinder_grid0, baseLocalDir + sst_pathfinder);
            OutputFormatter output = (OutputFormatter)outMap.get("outputHandler");
            assertNotNull("output is null", output);
            output.writeOutput(writer);
            writer.flush();
            writer.close();
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            assertFalse("Have an exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testNodcPathfinderSSTNetworkAll() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + sst_pathfinder);
            Writer writer = new CharArrayWriter();
            Parser parser = new Parser();
            try {
                sst_pathfinder_network_all += URLEncoder.encode("text/xml;subtype=\"om/1.0.0\"", "UTF-8");
            } catch (Exception e) {
                System.out.println("couldn't encode for sst1 - " + e.getMessage());
            }
            HashMap<String, Object> outMap = parser.enhanceGETRequest(dataset, sst_pathfinder_network_all, baseLocalDir + sst_pathfinder);
            OutputFormatter output = (OutputFormatter)outMap.get("outputHandler");
            assertNotNull("output is null", output);
            output.writeOutput(writer);
            writer.flush();
            writer.close();
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            assertFalse("Have an exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
//    @Test
//    public void testInsertClassNameHere() {
//        System.out.println("\n------" + getCurrentMethod() + "------");
//        
//        try {
//            
//        } catch (IOException ex) {
//            System.out.println(ex.getMessage());
//        } finally {
//            System.out.println("------END " + getCurrentMethod() + "------");
//        }
//    }
}
