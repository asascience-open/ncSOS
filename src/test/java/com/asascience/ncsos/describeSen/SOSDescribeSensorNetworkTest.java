/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describeSen;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import org.junit.*;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSDescribeSensorNetworkTest {
    private static String outputDir = null;
    private static String baseLocalDir = null;
    private static String exampleOutputDir = null;
    private static String query = "request=DescribeSensor&service=sos&version=1.0.0&procedure=urn:tds:network:all&responseformat=";
    
    private static final String data_folder = "resources/datasets/";
    
    // time series tests (STATION)
    private static final String ts_test_set1 = data_folder + "timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2.nc";
    private static final String ts_test_set2 = data_folder + "timeSeries-Orthogonal-Multidimenstional-MultipleStations-H.2.1/timeSeries-Orthogonal-Multidimenstional-MultipleStations-H.2.1.nc";
    
    // time series profiles tests (STATION_PROFILE)
    private static final String tsp_test_set1 = data_folder + "timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";
    private static final String tsp_test_set2 = data_folder + "timeSeriesProfile-Multidimensional-SingleStation-H.5.2/timeSeriesProfile-Multidimensional-SingleStation-H.5.2.nc";
    private static final String tsp_test_set3 = data_folder + "timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1.nc";
    private static final String tsp_test_set4 = data_folder + "timeSeriesProfile-Ragged-MultipeStations-H.5.3/timeSeriesProfile-Ragged-MultipeStations-H.5.3.nc";
    private static final String tsp_test_set5 = data_folder + "timeSeriesProfile-Ragged-MultipeStations-H.5.3/timeSeriesProfile-Ragged-MultipeStations-H.5.3.nc";
    
    // trajectory tests (TRAJECTORY)
    private static final String t_test_set1 = data_folder + "trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.nc";
    private static final String t_test_set2 = data_folder + "trajectory-Incomplete-Multidimensional-MultipleTrajectories-H.4.1/trajectory-Incomplete-Multidimensional-MultipleTrajectories-H.4.1.nc";
    private static final String t_test_set3 = data_folder + "trajectory-Incomplete-Multidimensional-SingleTrajectory-H.4.2/trajectory-Incomplete-Multidimensional-SingleTrajectory-H.4.2.nc";
    private static final String t_test_set4 = data_folder + "trajectory-Indexed-Ragged-MultipleTrajectories-H.4.4/trajectory-Indexed-Ragged-MultipleTrajectories-H.4.4.nc";
    
    // grid tests (GRID)
    private static final String g_test_set1 = data_folder + "satellite-sst/SST_Global_2x2deg_20120626_0000.nc";
    private static final String g_test_set2 = data_folder + "satellite-sst/SST_Global_2x2deg_20120627_0000.nc";
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // not really a test, just used to set up the various string values
        if (outputDir != null && baseLocalDir != null) {
            // exit early if the environ is already set
            return;
        }
        String container = "describeNetwork";
        InputStream templateInputStream = null;
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDir");
            baseLocalDir = XMLDomUtils.getNodeValue(configDoc, container, "projectDir");
            
            container = "examples";
            exampleOutputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDir");
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
        
        query += URLEncoder.encode("text/xml;subtype=\"sensorML/1.0.1\"", "UTF-8") + "&";
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    private static String getCurrentMethod() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i=0; i<ste.length; i++) {
            if (ste[i].getMethodName().contains(("test")))
                return ste[i].getMethodName();
        }
        return "could not find test name";
    }
     
    private void writeOutput(HashMap<String, Object> outMap, Writer write) {
        SOSOutputFormatter output = (SOSOutputFormatter)outMap.get("outputHandler");
        assertNotNull("got null output", output);
        output.writeOutput(write);
    }
    
    private static void fileWriter(String base, String fileName, Writer write) throws IOException {
        File file = new File(base + fileName);
        Writer output = new BufferedWriter(new FileWriter(file, true));
        output.write("\n");
        output.write(write.toString());
        output.close();
    }
    
    /***************************************************************************
     * Tests *******************************************************************
     ***************************************************************************/
    
    @Test
    public void testTimeSeriesSet1() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + ts_test_set1);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, ts_test_set1), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
            // write as an example
            fileWriter(exampleOutputDir, "DescribeSensor-Network-All-TimeSeries-sensorML1.0.1.xml", writer);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesSet2() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + ts_test_set2);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, ts_test_set2), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesProfileSet1() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + tsp_test_set1);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, tsp_test_set1), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
            // write as an example
            fileWriter(exampleOutputDir, "DescribeSensor-Network-All-TimeSeriesProfile-sensorML1.0.1.xml", writer);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesProfileSet2() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + tsp_test_set2);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, tsp_test_set2), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesProfileSet3() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + tsp_test_set3);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, tsp_test_set3), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesProfileSet4() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + tsp_test_set4);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, tsp_test_set4), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesProfileSet5() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + tsp_test_set5);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, tsp_test_set5), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTrajectorySet1() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + t_test_set1);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, t_test_set1), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
            // write as an example
            fileWriter(exampleOutputDir, "DescribeSensor-Network-All-Trajectory-sensorML1.0.1.xml", writer);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTrajectorySet2() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + t_test_set2);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, t_test_set2), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTrajectorySet3() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + t_test_set3);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, t_test_set3), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTrajectorySet4() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + t_test_set4);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, t_test_set4), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testGridSet1() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + g_test_set1);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, g_test_set1), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testGridSet2() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + g_test_set2);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, query, g_test_set2), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer);
            // assert(s)
            assertFalse("exception in output", writer.toString().contains("Exception"));
            // write as an example
            fileWriter(exampleOutputDir, "DescribeSensor-Network-All-Grid-sensorML1.0.1.xml", writer);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage(), false);
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
