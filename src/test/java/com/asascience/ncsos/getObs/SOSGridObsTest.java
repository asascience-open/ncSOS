/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.getObs;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
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
public class SOSGridObsTest {
    
    private static final String sst_1 = "resources/datasets/satellite-sst/SST_Global_2x2deg_20120626_0000.nc";
    private static String sst_1_reqs = "request=GetObservation&service=sos&version=1.0.0&lat=-52.0&lon=0.0&observedProperty=sst&offering=sst&eventtime=1990-01-01T00:00:00Z/2013-05-17T09:57:00.000-04:00&responseformat=";
    private static final String sst_2 = "resources/datasets/satellite-sst/SST_Global_2x2deg_20120627_0000.nc";
    private static String sst_2_reqs = "request=GetObservation&service=sos&version=1.0.0&lat=-54.0,-52.0,-50.0&lon=-120.0,0.0,74.0&observedProperty=sst&offering=sst&eventtime=1990-01-01T00:00:00Z/2013-05-17T09:57:00.000-04:00&responseformat=";
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
        String container = "getObsGrid";
        InputStream templateInputStream = null;
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputBase");
            baseLocalDir = XMLDomUtils.getNodeValue(configDoc, container, "projectDir");
            
            container = "examples";
            exampleOutputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDir");
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
            SOSParser parser = new SOSParser();
            try {
                sst_1_reqs += URLEncoder.encode("text/xml;subtype=\"om/1.0.0\"", "UTF-8");
            } catch (Exception e) {
                System.out.println("couldn't encode for sst1 - " + e.getMessage());
            }
            HashMap<String, Object> outMap = parser.enhance(dataset, sst_1_reqs, baseLocalDir + sst_1);
            SOSOutputFormatter output = (SOSOutputFormatter)outMap.get("outputHandler");
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
            SOSParser parser = new SOSParser();
            try {
                sst_2_reqs += URLEncoder.encode("text/xml;subtype=\"om/1.0.0\"", "UTF-8");
            } catch (Exception e) {
                System.out.println("couldn't encode for sst1 - " + e.getMessage());
            }
            HashMap<String, Object> outMap = parser.enhance(dataset, sst_2_reqs, baseLocalDir + sst_2);
            SOSOutputFormatter output = (SOSOutputFormatter)outMap.get("outputHandler");
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
