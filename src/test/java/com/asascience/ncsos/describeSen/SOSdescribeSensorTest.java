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
import org.apache.log4j.BasicConfigurator;
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
    private static String exampleOutputDir = null;
 
    private static final String bdss_1_set = "resources/datasets/sura/watlev_NOAA_NAVD_PRE.nc";
    private static final String bdss_1_query = "procedure=urn:ioos:station:authority:NOAA_8779748";
    private static final String bdss_watlev_query = "procedure=urn:ioos:sensor:authority:NOAA_8724698:watlev";
    private static final String bdss_1_query_bad = "procedure=urn:ioos:station:authority:badstationname";
    
    private static final String bdss_2_set = "resources/datasets/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";
    private static final String bdss_2_query = "procedure=urn:ioos:station:authority:Station1";
    private static final String bdss_2_bad_sensor = "procedure=urn:ioos:sensor:authority:Station1:badsensor";
    
    private static final String bdst_1_set = "resources/datasets/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.nc";
    private static final String bdst_1_query = "procedure=urn:ioos:station:authority:Trajectory3";
    private static final String bdst_1_query_bad = "procedure=urn:ioos:station:authority:Trajectory100";
    
    private static final String bdst_2_set = "resources/datasets/trajectory-Indexed-Ragged-MultipleTrajectories-H.4.4/trajectory-Indexed-Ragged-MultipleTrajectories-H.4.4.nc";
    private static final String bdst_2_query = "procedure=urn:ioos:station:authority:Trajectory7";
    
    private static final String bdsp_1_set = "resources/datasets/profile-Contiguous-Ragged-MultipleProfiles-H.3.4/profile-Contiguous-Ragged-MultipleProfiles-H.3.4.nc";
    private static final String bdsp_1_query = "procedure=urn:ioos:station:authority:Profile3";
    private static final String bdsp_1_query_bad = "procedure=urn:ioos:station:authority:Profile100";
    
    private static final String bdsp_2_set = "resources/datasets/profile-Indexed-Ragged-MultipleProfiles-H.3.5/profile-Indexed-Ragged-MultipleProfiles-H.3.5.nc";
    private static final String bdsp_2_query = "procedure=urn:ioos:station:authority:Profile5";
    
    private static final String bdsp_3_set = "resources/datasets/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1.nc";
    private static final String bdsp_3_query = "procedure=urn:ioos:station:authority:Profile32";
    
    private static final String bdsg_1_set = "resources/datasets/satellite-sst/SST_Global_2x2deg_20120626_0000.nc";
    private static final String bdsg_1_query = "procedure=urn:ioos:station:authority:Grid0";
    
    private static final String bdstp_1_set = "resources/datasets/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1.nc";
    private static final String bdstp_1_query = "procedure=urn:ioos:station:authority:Trajectory2";
    private static final String bdstp_1_query_bad = "procedure=urn:ioos:station:authority:Trajectory200";
    
    private static final String bdstp_2_set = "resources/datasets/trajectoryProfile-Ragged-MultipleTrajectories-H.6.3/trajectoryProfile-Ragged-MultipleTrajectories-H.6.3.nc";
    private static final String bdstp_2_query = "procedure=urn:ioos:station:authority:Trajectory3";
    
    private static final String ext_hawaii_set = "resources/datasets/sura/wqbkn_2012_08_01.nc";
    private static final String ext_hawaii_query = "procedure=urn:ioos:station:authority:WQBKN";
    
    private static final String andrw_set = "resources/datasets/sura/andrw.lft.nc";
    private static final String andrw_query = "procedure=urn:ioos:station:authority:Site-79-2";
    
    private static final String usace_set = "resources/datasets/sura/hs_USACE-CHL.nc";
    private static final String usace_query = "procedure=urn:ioos:station:authority:USACE-CHL_2410504B";
    
    private static final String undkennedy_set = "resources/datasets/sura/Hsig_UNDKennedy_IKE_VIMS_3D_WAVEONLY.nc";
    private static final String undkennedy_query = "procedure=urn:ioos:station:authority:UNDKennedy_R";
    
    private static final String hwm_tcoon_set = "resources/datasets/sura/hwm_TCOON_NAVD88.nc";
    private static final String hwm_tcoon_query = "procedure=urn:ioos:station:authority:TCOON_8779770";
    
    private static final String tm_csi_set = "resources/datasets/sura/tm_CSI.nc";
    private static final String tm_csi_query = "procedure=urn:ioos:station:authority:CSI_03";
    
    private static final String tm_ike_set = "resources/datasets/sura/tm_IKE.nc";
    private static final String tm_ike_query = "procedure=urn:ioos:station:authority:CSI_09";
    
    private static final String crms_set = "resources/datasets/sura/watlev_CRMS.nc";
    private static final String crms_query = "procedure=urn:ioos:station:authority:CRMS_BA04-07";
    
    private static final String crms_2005_set = "resources/datasets/sura/watlev_CRMS_2005.nc";
    private static final String crms_2005_query = "procedure=urn:ioos:station:authority:CRMS_BA04-10";
    
    private static final String crms_2008_set = "resources/datasets/sura/watlev_CRMS_2008.F.C__IKE_VIMS_3D_WITHWAVE.nc";
    private static final String crms_2008_query = "procedure=urn:ioos:station:authority:CRMS_BA04-56";
    
    private static final String crms_2008_nowave_set = "resources/datasets/sura/watlev_CRMS_2008.F.C_IKE_VIMS_3D_NOWAVE.nc";
    private static final String crms_2008_nowave_query = "procedure=urn:ioos:station:authority:CRMS_BS08-09";
    
    private static final String crms_2008_basecycle_set = "resources/datasets/sura/watlev_CRMS_2008.F.C_mod_base_cycle_5.nc";
    private static final String crms_2008_basecycle_query = "procedure=urn:ioos:station:authority:CRMS_CS20-03";
    
    private static final String watlev_csi_set = "resources/datasets/sura/watlev_CSI.nc";
    private static final String watlev_csi_query = "procedure=urn:ioos:station:authority:CSI_15";
    
    private static final String watlev_ike_set = "resources/datasets/sura/watlev_IKE.nc";
    private static final String watlev_ike_query = "procedure=urn:ioos:station:authority:CRMS_BA01-01";
    
    private static final String watlev_ike_61_set = "resources/datasets/sura/watlev_IKE.P.UL-Ike2Dh.61.nc";
    private static final String watlev_ike_61_query = "procedure=urn:ioos:station:authority:USACE-CHL_2410504B";
    
    private static final String watlev_noaa_set = "resources/datasets/sura/watlev_NOAA.F.C.nc";
    private static final String watlev_noaa_query = "procedure=urn:ioos:station:authority:NOAA_8724698";
    
    private static final String noaa_navd_set = "resources/datasets/sura/watlev_NOAA_NAVD_PRE.nc";
    private static final String noaa_navd_query = "procedure=urn:ioos:station:authority:NOAA_8726347";
    
    private static final String jason_satellite_set = "resources/datasets/nodc/jason2_satelliteAltimeter.nc";
    private static final String jason_satellite_query = "procedure=urn:ioos:station:authority:unknown";
    
    private static final String bodega_marinelab_set = "resources/datasets/nodc/BodegaMarineLabBuoy.nc";
    private static final String bodega_marinelab_query = "procedure=urn:ioos:station:authority:Cordell Bank Buoy";
    
    private static final String bad_requests_set = "resources/datasets/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1.nc";
    private static final String bad_request_control_query = "procedure=urn:ioos:station:authority:Trajectory2";
    private static final String bad_request_responseformat_query = "request=DescribeSensor&service=sos&version=1.0.0&responseFormat=text/xml;subtype=\"5\"";
    private static final String bad_request_responseformat_mispelled_query = "request=DescribeSensor&service=sos&version=1.0.0&respnseformat=";
    private static final String bad_request_request_query = "request=DescrbeSensor&service=sos&version=1.0.0&responseFormat=";
    private static final String bad_request_request_mispelled_query = "reqst=DescribeSensor&service=sos&version=1.0.0&responseFormat=";
    private static final String bad_request_version_query = "request=DescribeSensor&service=SOS&version=1.0.&responseformat=";
    private static final String bad_request_version_misspelled_query = "request=DescribeSensor&service=SOS&vrsion=1.0.0&responseformat=";
    private static final String bad_request_service_query = "request=DescribeSensor&service=s0s&version=1.0.0&responseformat=";
    private static final String bad_request_service_misspelled_query = "request=DescribeSensor&servce=sos&version=1.0.0&responseformat=";
    private static final String bad_request_procedure_query = "request=DescribeSensor&service=sos&version=1.0.0&procedure=urn:tds:station:trajectory2&responseformat=";
    private static final String bad_request_procedure_misspelled_query = "request=DescribeSensor&service=sos&version=1.0.0&procdure=urn:ioos:station:authority:trajectory2&responseformat=";
    
    private static String baseQuery = "request=DescribeSensor&service=sos&version=1.0.0&responseformat=";
    
    @BeforeClass
    public static void SetupEnviron() throws FileNotFoundException, UnsupportedEncodingException {
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
            outputDir += "desc_sen/";
            baseLocalDir = XMLDomUtils.getNodeValue(configDoc, container, "projectDirectory");
            exampleOutputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDirectory");
            exampleOutputDir += "examples/";
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
    
    private static void fileWriter(String base, String fileName, Writer write, boolean append) throws IOException {
        File file = new File(base + fileName);
        Writer output = new BufferedWriter(new FileWriter(file, append));
        output.flush();
        try {
            output.write("\n");
            output.write(write.toString());
        } catch (Exception ex) {
            System.err.println(ex.toString());
            for (StackTraceElement elem : ex.getStackTrace()) {
                System.err.println("\t" + elem.toString());
            }
        } finally {
            output.close();
        }
    }
    
    /***************************************************************************
     * Tests *******************************************************************
     ***************************************************************************/
    
    @Test
    public void testBadRequestStringsDescribeSensor() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            String valid_response_format = URLEncoder.encode("text/xml;subtype=\"sensorML/1.0.1\"", "UTF-8");
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bad_requests_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            String testOut = null;
            // first test - bad_request_control_query - should return w/o exception
            writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bad_request_control_query, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, false);
            // no output, just check that there is no exception
            assertFalse("exception in output", writer.toString().contains("Exception"));
            // 2nd test - bad_request_responseformat_query - checks to see what is returned when an invalid response format is returned
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_responseformat_query + "&" + bad_request_control_query, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_responseformat_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_responseformat_query", testOut.contains("invalid argument responseFormat"));
            // 3rd test - bad_request_responseformat_mispelled_query - checks to see what is returned when responseformat is misspelled
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_responseformat_mispelled_query + valid_response_format + "&" + bad_request_control_query, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_responseformat_mispelled_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_responseformat_mispelled_query", testOut.contains("responseFormat"));
            // 4th test - bad_request_request_query - checks to see what is returned when an invalid request is sent
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_request_query + valid_response_format + "&" + bad_request_control_query, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_request_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_request_query", testOut.contains("Unrecognized request"));
            // 5th test - bad_request_request_mispelled_query - checks to see what is returned when request is misspelled
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_request_mispelled_query + valid_response_format + "&" + bad_request_control_query, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_request_mispelled_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_request_mispelled_query", testOut.contains("parameter 'request'"));
            // 6th test - bad_request_version_query - checks to see what is returned when the version specified is invalid
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_version_query + valid_response_format + "&" + bad_request_control_query, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_version_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_version_query", testOut.contains("'version'"));
            // 7th test - bad_request_version_misspelled_query - checks to see what is returned when version is misspelled
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_version_misspelled_query + valid_response_format + "&" + bad_request_control_query, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_version_misspelled_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_version_misspelled_query", testOut.contains("'version'"));
            // 8th test - bad_request_service_query - checks to see what is returned when the service requested is invalid
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_service_query + valid_response_format + "&" + bad_request_control_query, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_service_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_service_query", testOut.contains("service parameter"));
            // 9th test - bad_request_service_misspelled_query - checks to see what is returned when service is misspelled
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_service_misspelled_query + valid_response_format + "&" + bad_request_control_query, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_service_misspelled_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_service_misspelled_query", testOut.contains("service parameter"));
            // 10th test - bad_request_procedure_query - checks to see what is returned when the procedure specified is invalid
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_procedure_query + valid_response_format, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_procedure_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_procedure_query", testOut.contains("Procedure parameter"));
            // 11th test - bad_request_procedure_misspelled_query - checks to see what is returned when procedure is misspelled
            writer.close();
            writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, bad_request_procedure_misspelled_query + valid_response_format, bad_requests_set), writer);
            // add to test output
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, true);
            // test to make sure we got an exception
            testOut = writer.toString();
            assertTrue("no exception in output - bad_request_procedure_misspelled_query", testOut.contains("Exception"));
            assertTrue("unexpected exception - bad_request_procedure_misspelled_query", testOut.contains("procedure must be"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testBasicDescribeSensorStation() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorStation------");
        NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + bdss_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + bdss_1_query, bdss_1_set), writer);
        fileWriter(outputDir, "watlev_NOAA_NAVD_PRE.xml", writer, false);
        // test for expected values below
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing component", writer.toString().contains("<sml:component name=\"Sensor watlev\">"));
        assertTrue("station id not as expected", writer.toString().contains("<sml:value>urn:ioos:station:authority:NOAA_8779748</sml:value>"));
        System.out.println("------End testBasicDescribeSensorStation------");
    }
    
    @Test
    public void testBasicDescribeSensorStation2() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorStation2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdss_2_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdss_2_query, bdss_2_set), writer);
        fileWriter(outputDir, "timeSeriesProfile-Multidimensional-MultipleStations-H.5.1.xml", writer, false);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing component", writer.toString().contains("<sml:component name=\"Sensor temperature\">"));
        assertTrue("missing/invalid coords", writer.toString().contains("37.5 -76.5"));
        // write as an example
        fileWriter(exampleOutputDir, "DescribeSensor-TimeSeriesProfile-sensorML1.0.1.xml", writer, false);
        System.out.println("------End testBasicDescribeSensorStation2------");
    }
    
    @Test
    public void testBasicDescribeSensorTrajectory() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorTrajectory------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdst_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdst_1_query, bdst_1_set), writer);
        fileWriter(outputDir, "trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.xml", writer, false);
        // write as an example
        fileWriter(exampleOutputDir, "DescribeSensor-Trajectory-sensorML1.0.1.xml", writer, false);
        assertFalse("exception in output", writer.toString().contains("Exception"));
//        assertTrue("missing component", writer.toString().contains("<sml:component name=\"Sensor humidity\">"));
//        assertTrue("missing/invalid coords", writer.toString().contains("1990-01-01T00:00:00Z,5.429996490478516,-35.31080627441406"));
        System.out.println("------End testBasicDescribeSensorTrajectory------");
    }
    
    @Test
    public void testBasicDescribeSensorTrajectory2() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorTrajectory2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdst_2_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdst_2_query, bdst_2_set), writer);
        fileWriter(outputDir, "trajectory-Indexed-Ragged-MultipleTrajectories-H.4.4.xml", writer, false);
        assertFalse("exception in output", writer.toString().contains("Exception"));
//        assertTrue("missing component", writer.toString().contains("<sml:identification xlink:href=\"urn:ioos:station:authority:Trajectory7::temperature\"/>"));
//        assertTrue("missing/invalid coords", writer.toString().contains("1990-01-01T09:00:00Z,29.956968307495117,-1.6200900077819824"));
        System.out.println("------End testBasicDescribeSensorTrajectory2------");
    }
    
    @Test
    public void testBasicDescribeSensorProfile() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorProfile------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdsp_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdsp_1_query, bdsp_1_set), writer);
        fileWriter(outputDir, "profile-Contiguous-Ragged-MultipleProfiles-H.3.4.xml", writer, false);
        // write as an example
        assertFalse("exception in output", writer.toString().contains("Exception"));
//        assertTrue("missing component", writer.toString().contains("<sml:System gml:id=\"sensor-humidity\">"));
//        assertTrue("missing/invalid latitude", writer.toString().contains("<swe:value>134.0</swe:value>"));
        fileWriter(exampleOutputDir, "DescribeSensor-Profile-sensorML1.0.1.xml", writer, false);
        System.out.println("------End testBasicDescribeSensorProfile------");
    }
    
    @Test
    public void testBasicDescribeSensorProfile2() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorProfile2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdsp_2_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdsp_2_query, bdsp_2_set), writer);
        fileWriter(outputDir, "profile-Indexed-Ragged-MultipleProfiles-H.3.5.xml", writer, false);
        assertFalse("exception in output", writer.toString().contains("Exception"));
//        assertTrue("missing/invalid unit of measurement", writer.toString().contains("<swe:uom code=\"m\"/>"));
//        assertTrue("missing/invalid altitude", writer.toString().contains("<swe:value>9.813936233520508</swe:value>"));
        System.out.println("------End testBasicDescribeSensorProfile2------");
    }
    
    @Test
    public void testBasicDescribeSensorProfile3() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorProfile2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdsp_3_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdsp_3_query, bdsp_3_set), writer);
        fileWriter(outputDir, "profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1.xml", writer, false);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        System.out.println("------End testBasicDescribeSensorProfile2------");
    }
    
    @Test
    public void testBasicDescribeSensorSensor() throws IOException {
        System.out.println("\n------Start testBasicDescribeSensorSensor------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdss_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdss_watlev_query, bdss_1_set), writer);
        fileWriter(outputDir, "watlev_NOAA_NAVD_PRE_watlev-sensor.xml", writer, false);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing/invalid identifier", writer.toString().contains("<sml:identifier name=\"coordinates\">"));
        assertTrue("missing/invalid sensor id", writer.toString().contains("<sml:value>urn:ioos:sensor:authority:NOAA_8724698:watlev</sml:value>"));
        // write as an example
        fileWriter(exampleOutputDir, "DescribeSensor-Sensor-sensorML1.0.1.xml", writer, false);
        System.out.println("------End testBasicDescribeSensorSensor------");
    }
    
    @Test
    public void testBasicDescriptSensorGrid() throws IOException {
        System.out.println("\n------Start testBasicDescriptSensorGrid------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdsg_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdsg_1_query, bdsg_1_set), writer);
        fileWriter(outputDir, "SST_Global_2x2deg_20120626_0000.xml", writer, false);
        assertFalse("exception in output", writer.toString().contains("Exception"));
//        assertTrue("missing/invalid identifier", writer.toString().contains("<sml:identifier name=\"coordinates\">"));
//        assertTrue("missing/invalid sensor id", writer.toString().contains("<sml:value>urn:ioos:sensor:authority:NOAA_8724698::watlev</sml:value>"));
        // write as an example
        fileWriter(exampleOutputDir, "DescribeSensor-Grid-sensorML1.0.1.xml", writer, false);
        System.out.println("------End testBasicDescriptSensorGrid------");
    }
    
    @Test
    public void testBasicDescribeSensorSection() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdstp_1_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdstp_1_query, bdstp_1_set), writer);
            fileWriter(outputDir, "trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1_trajectory2.xml", writer, false);
            assertFalse("exception in output", writer.toString().contains("Exception"));
            // write as an example
            fileWriter(exampleOutputDir, "DescribeSensor-Section-sensorML1.0.1.xml", writer, false);
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
            writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdstp_2_query, bdstp_2_set), writer);
            fileWriter(outputDir, "trajectoryProfile-Ragged-MultipleTrajectories-H.6.3_trajectory3.xml", writer, false);
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testBadStationNameProfile() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bdsp_1_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(dataset, baseQuery + bdsp_1_query_bad, bdsp_1_set), writer);
            fileWriter(outputDir, "profile-bad-station-request.xml", writer, false);
            assertTrue("no exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testBadSensorNameStation() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + bdss_2_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + bdss_2_bad_sensor, bdss_2_set), writer);
            fileWriter(outputDir, "station-bad-sensor-request.xml", writer, false);
            // test for expected values below
            assertTrue("no exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testBadStationNameStation() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + bdss_1_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + bdss_1_query_bad, bdss_1_set), writer);
            fileWriter(outputDir, "station-bad-station-request.xml", writer, false);
            // test for expected values below
            assertTrue("no exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testBadStationNameTrajectory() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + bdst_1_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + bdst_1_query_bad, bdst_1_set), writer);
            fileWriter(outputDir, "trajectory-bad-station-request.xml", writer, false);
            // test for expected values below
            assertTrue("no exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testBadStationNameTrajectoryProfile() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + bdstp_1_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + bdstp_1_query_bad, bdstp_1_set), writer);
            fileWriter(outputDir, "section-bad-station-request.xml", writer, false);
            // test for expected values below
            assertTrue("no exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testExternalHawaiiFile() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + ext_hawaii_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + ext_hawaii_query, ext_hawaii_set), writer);
            fileWriter(outputDir, "station-hawaii-external.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
            // write as an example for TimeSeries
            fileWriter(exampleOutputDir, "DescribeSensor-TimeSeries-sensorML1.0.1.xml", writer, false);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testBadDataset() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + andrw_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + andrw_query, andrw_set), writer);
            fileWriter(outputDir, "andrw-lft.xml", writer, false);
            // test for expected values below
            assertTrue("no exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testUSACE() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + usace_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + usace_query, usace_set), writer);
            fileWriter(outputDir, "hs-usace-chl.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testUNDKennedyIke() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + undkennedy_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + undkennedy_query, undkennedy_set), writer);
            fileWriter(outputDir, "hsig-undkennedy-ike-vims-3d-waveonly.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTCOONNavd() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + hwm_tcoon_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + hwm_tcoon_query, hwm_tcoon_set), writer);
            fileWriter(outputDir, "hwm-tcoon-navd.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTMCSI() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + tm_csi_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + tm_csi_query, tm_csi_set), writer);
            fileWriter(outputDir, "tm-csi.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTMIKE() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + tm_ike_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + tm_ike_query, tm_ike_set), writer);
            fileWriter(outputDir, "tm-ike.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testWatlevCRMS() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + crms_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + crms_query, crms_set), writer);
            fileWriter(outputDir, "watlev-crms.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testWatlevCRMS2005() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + crms_2005_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + crms_2005_query, crms_2005_set), writer);
            fileWriter(outputDir, "watlev-crms-2005.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testCRMS2008IKEWave() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + crms_2008_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + crms_2008_query, crms_2008_set), writer);
            fileWriter(outputDir, "crms-2008-ike-wave.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testWatlevCRMS2008NoWave() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + crms_2008_nowave_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + crms_2008_nowave_query, crms_2008_nowave_set), writer);
            fileWriter(outputDir, "crms-2008-ike-nowave.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testCRMS2008BaseCycle() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + crms_2008_basecycle_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + crms_2008_basecycle_query, crms_2008_basecycle_set), writer);
            fileWriter(outputDir, "crms-2008-base-cycle.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testWatlevCSI() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + watlev_csi_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + watlev_csi_query, watlev_csi_set), writer);
            fileWriter(outputDir, "watlev-csi.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testWatlevIke() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + watlev_ike_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + watlev_ike_query, watlev_ike_set), writer);
            fileWriter(outputDir, "watlev-ike.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testWatlevIke61() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + watlev_ike_61_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + watlev_ike_61_query, watlev_ike_61_set), writer);
            fileWriter(outputDir, "watlev-ike-61.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testWatlevNOAA() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + watlev_noaa_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + watlev_noaa_query, watlev_noaa_set), writer);
            fileWriter(outputDir, "watlev-noaa.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testWatlevNOAANAVD() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + noaa_navd_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + noaa_navd_query, noaa_navd_set), writer);
            fileWriter(outputDir, "watlev-noaa-navd.xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testJasonSatelliteAltemeter() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + jason_satellite_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + jason_satellite_query, jason_satellite_set), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, false);
            // test for expected values below
            assertFalse("exception in output", writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testCordellBankBuoy() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + bodega_marinelab_set);
            SOSParser parser = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(parser.enhanceGETRequest(cdfDataset, baseQuery + bodega_marinelab_query, bodega_marinelab_set), writer);
            fileWriter(outputDir, getCurrentMethod() + ".xml", writer, false);
            // test for expected values below
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
