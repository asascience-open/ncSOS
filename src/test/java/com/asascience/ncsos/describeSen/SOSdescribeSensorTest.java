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
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSdescribeSensorTest {
    private static String outputDir = null;
    private static String baseLocalDir = null;
    
    private static final String bdss_1_set = "resources/datasets/sura/watlev_NOAA_NAVD_PRE.nc";
    private static final String bdss_1_query = "procedure=urn:tds:station.sos:NOAA_8724698";
    private static final String bdss_watlev_query = "procedure=urn:tds:sensor.sos:NOAA_8724698::watlev";
    private static final String bdss_2_set = "resources/datasets/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";
    private static final String bdss_2_query = "procedure=urn:tds:station.sos:Station1";
    private static final String bdst_1_set = "resources/datasets/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.nc";
    private static final String bdst_1_query = "procedure=urn:tds:station.sos:Trajectory3";
    private static final String bdst_2_set = "resources/datasets/trajectory-Indexed-Ragged-MultipleTrajectories-H.4.4/trajectory-Indexed-Ragged-MultipleTrajectories-H.4.4.nc";
    private static final String bdst_2_query = "procedure=urn:tds:station.sos:Trajectory7";
    private static final String bdsp_1_set = "resources/datasets/profile-Contiguous-Ragged-MultipleProfiles-H.3.4/profile-Contiguous-Ragged-MultipleProfiles-H.3.4.nc";
    private static final String bdsp_1_query = "procedure=urn:tds:station.sos:Profile4";
    private static final String bdsp_2_set = "resources/datasets/profile-Indexed-Ragged-MultipleProfiles-H.3.5/profile-Indexed-Ragged-MultipleProfiles-H.3.5.nc";
    private static final String bdsp_2_query = "procedure=urn:tds:station.sos:Profile5";
    private static final String bdsg_1_set = "resources/datasets/satellite-sst/SST_Global_2x2deg_20120626_0000.nc";
    private static final String bdsg_1_query = "procedure=urn:tds:station.sos:SST_Global_2x2deg_20120626_0000";
    private static final String bdstp_1_set = "resources/datasets/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1.nc";
    private static final String bdstp_1_query = "procedure=urn:tds:station.sos:Trajectory2";
    private static final String bdstp_2_set = "resources/datasets/trajectoryProfile-Ragged-MultipleTrajectories-H.6.3/trajectoryProfile-Ragged-MultipleTrajectories-H.6.3.nc";
    private static final String bdstp_2_query = "procedure=urn:tds:station.sos:Trajectory3";
    
    private static String baseQuery = "request=DescribeSensor&service=sos&version=1.0.0&responseformat=";
    
    @BeforeClass
    public static void SetupEnviron() throws FileNotFoundException, UnsupportedEncodingException {
        // not really a test, just used to set up the various string values
        if (outputDir != null && baseLocalDir != null) {
            // exit early if the environ is already set
            return;
        }
        String container = "describeSensor";
        InputStream templateInputStream = null;
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDir");
            baseLocalDir = XMLDomUtils.getNodeValue(configDoc, container, "projectDir");
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
        
        baseQuery += URLEncoder.encode("text/xml;subtype=\"sensorML/1.0.1\"", "UTF-8") + "&";
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
        Writer output = new BufferedWriter(new FileWriter(file));
        output.write(write.toString());
        output.close();
    }
    
    /***************************************************************************
     * Tests *******************************************************************
     ***************************************************************************/
    
    @Test
    public void testBasicDescribeSensorStation() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorStation------");
        NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + bdss_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(cdfDataset, baseQuery + bdss_1_query, bdss_1_set), writer);
        fileWriter(outputDir, "watlev_NOAA_NAVD_PRE.xml", writer);
        // test for expected values below
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing component", writer.toString().contains("<sml:component name=\"Sensor watlev\">"));
        assertTrue("station id not as expected", writer.toString().contains("<sml:value>urn:tds:station.sos:NOAA_8724698</sml:value>"));
        System.out.println("------End testBasicDescribeSensorStation------");
    }
    
    @Test
    public void testBasicDescribeSensorStation2() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorStation2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdss_2_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(dataset, baseQuery + bdss_2_query, bdss_2_set), writer);
        fileWriter(outputDir, "timeSeriesProfile-Multidimensional-MultipleStations-H.5.1.xml", writer);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing component", writer.toString().contains("<sml:component name=\"Sensor temperature\">"));
        assertTrue("missing/invalid coords", writer.toString().contains("<gml:coordinates>37.5 -76.5</gml:coordinates>"));
        System.out.println("------End testBasicDescribeSensorStation2------");
    }
    
    @Test
    public void testBasicDescribeSensorTrajectory() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorTrajectory------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdst_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(dataset, baseQuery + bdst_1_query, bdst_1_set), writer);
        fileWriter(outputDir, "trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.xml", writer);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing component", writer.toString().contains("<sml:component name=\"Sensor humidity\">"));
        assertTrue("missing/invalid coords", writer.toString().contains("1990-01-01T00:00:00Z,5.429996490478516,-35.31080627441406"));
        System.out.println("------End testBasicDescribeSensorTrajectory------");
    }
    
    @Test
    public void testBasicDescribeSensorTrajectory2() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorTrajectory2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdst_2_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(dataset, baseQuery + bdst_2_query, bdst_2_set), writer);
        fileWriter(outputDir, "trajectory-Indexed-Ragged-MultipleTrajectories-H.4.4.xml", writer);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing component", writer.toString().contains("<sml:identification xlink:href=\"urn:tds:station.sos:Trajectory7::temperature\"/>"));
        assertTrue("missing/invalid coords", writer.toString().contains("1990-01-01T09:00:00Z,29.956968307495117,-1.6200900077819824"));
        System.out.println("------End testBasicDescribeSensorTrajectory2------");
    }
    
    @Test
    public void testBasicDescribeSensorProfile() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorProfile------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdsp_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(dataset, baseQuery + bdsp_1_query, bdsp_1_set), writer);
        fileWriter(outputDir, "profile-Contiguous-Ragged-MultipleProfiles-H.3.4.xml", writer);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing component", writer.toString().contains("<sml:System gml:id=\"sensor-humidity\">"));
        assertTrue("missing/invalid latitude", writer.toString().contains("<swe:value>104.0</swe:value>"));
        System.out.println("------End testBasicDescribeSensorProfile------");
    }
    
    @Test
    public void testBasicDescribeSensorProfile2() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorProfile2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdsp_2_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(dataset, baseQuery + bdsp_2_query, bdsp_2_set), writer);
        fileWriter(outputDir, "profile-Indexed-Ragged-MultipleProfiles-H.3.5.xml", writer);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing/invalid unit of measurement", writer.toString().contains("<swe:uom code=\"m\"/>"));
        assertTrue("missing/invalid altitude", writer.toString().contains("<swe:value>9.813936233520508</swe:value>"));
        System.out.println("------End testBasicDescribeSensorProfile2------");
    }
    
    @Test
    public void testBasicDescribeSensorSensor() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorSensor------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdss_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(dataset, baseQuery + bdss_watlev_query, bdss_1_set), writer);
        fileWriter(outputDir, "watlev_NOAA_NAVD_PRE_watlev-sensor.xml", writer);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing/invalid identifier", writer.toString().contains("<sml:identifier name=\"coordinates\">"));
        assertTrue("missing/invalid sensor id", writer.toString().contains("<sml:value>urn:tds:sensor.sos:NOAA_8724698::watlev</sml:value>"));
        System.out.println("------End testBasicDescribeSensorSensor------");
    }
    
    @Test
    public void testBasicDescriptSensorGrid() throws IOException {
        System.out.println("\n------Start testBasicDescriptSensorGrid------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdsg_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(dataset, baseQuery + bdsg_1_query, bdsg_1_set), writer);
        fileWriter(outputDir, "SST_Global_2x2deg_20120626_0000.xml", writer);
        assertFalse("exception in output", writer.toString().contains("Exception"));
//        assertTrue("missing/invalid identifier", writer.toString().contains("<sml:identifier name=\"coordinates\">"));
//        assertTrue("missing/invalid sensor id", writer.toString().contains("<sml:value>urn:tds:sensor.sos:NOAA_8724698::watlev</sml:value>"));
        System.out.println("------End testBasicDescriptSensorGrid------");
    }
    
    @Test
    public void testBasicDescribeSensorSection() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdstp_1_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, baseQuery + bdstp_1_query, bdstp_1_set), writer);
            fileWriter(outputDir, "trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1_trajectory2.xml", writer);
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testBasicDescribeSensorSection2() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdstp_2_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhance(dataset, baseQuery + bdstp_2_query, bdstp_2_set), writer);
            fileWriter(outputDir, "trajectoryProfile-Ragged-MultipleTrajectories-H.6.3_trajectory3.xml", writer);
            assertFalse("exception in output", writer.toString().contains("Exception"));
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
