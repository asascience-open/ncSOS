/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.getCaps;

import com.asascience.ncsos.getcaps.SOSGetCapabilitiesRequestHandler;
import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.util.Formatter;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 * Test suite for Get Capabilities requests.
 * @author abird
 * @author scowan
 */
public class SOSgetCapsTest {
    
    // base location of resources
    private static String baseLocalDir = null;
    private static String baseTomcatDir = null;
    private static String exampleOutputDir = null;
    
    private static String baseRequest = "request=GetCapabilities&version=1.0.0&service=sos&sections=all";
    
    private static String OperationsRequest = "request=GetCapabilities&version=1.0.0&service=sos&sections=OperationsMetadata";
    private static String ServiceIdRequest = "request=GetCapabilities&version=1.0.0&service=sos&sections=ServiceIdentification";
    private static String ServiceProvRequest = "request=GetCapabilities&version=1.0.0&service=sos&sections=ServiceProvider";
    private static String ContentsRequest = "request=GetCapabilities&version=1.0.0&service=sos&sections=Contents";
    private static String OpsAndContentsRequest = "request=GetCapabilities&version=1.0.0&service=sos&sections=OperationsMetadata,Contents";
    private static String BadSectionRequest = "request=GetCapabilities&version=1.0.0&service=sos&sections=BadSection";
    
    // work thredds
    private static String catalinaThredds = "work/Catalina/localhost/thredds";

    //imeds data
    private static String imeds1 = "resources/datasets/sura/Hsig_UNDKennedy_IKE_VIMS_3D_WAVEONLY.nc";
    private static String imeds5 = "resources/datasets/sura/hwm_TCOON_NAVD88.nc";
    private static String imeds8 = "resources/datasets/sura/watlev_CRMS.nc";
    private static String imeds13 = "resources/datasets/sura/watlev_IKE.nc";
    private static String imeds15 = "resources/datasets/sura/watlev_NOAA_NAVD_PRE.nc";
    //timeseries
    private static String tsIncompleteMultiDimensionalMultipleStations = "resources/datasets/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2.nc";
    private static String tsOrthogonalMultidimenstionalMultipleStations = "resources/datasets/timeSeries-Orthogonal-Multidimenstional-MultipleStations-H.2.1/timeSeries-Orthogonal-Multidimenstional-MultipleStations-H.2.1.nc";
//ragged Array - timeseries profile
    private static String RaggedSingleConventions = "resources/datasets/timeSeriesProfile-Ragged-SingleStation-H.5.3/timeSeriesProfile-Ragged-SingleStation-H.5.3.nc";
    private static String RaggedMultiConventions = "resources/datasets/timeSeriesProfile-Ragged-MultipeStations-H.5.3/timeSeriesProfile-Ragged-MultipeStations-H.5.3.nc";
    private static String OrthogonalMultidimensionalMultiStations = "resources/datasets/timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1.nc";
    private static String MultiDimensionalSingleStations = "resources/datasets/timeSeriesProfile-Multidimensional-SingleStation-H.5.2/timeSeriesProfile-Multidimensional-SingleStation-H.5.2.nc";
    private static String MultiDimensionalMultiStations = "resources/datasets/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";
//point
    private static String cfPoint = "resources/datasets/point-H.1/point-H.1.nc";
// profile
    private static String ContiguousRaggedMultipleProfiles = "resources/datasets/profile-Contiguous-Ragged-MultipleProfiles-H.3.4/profile-Contiguous-Ragged-MultipleProfiles-H.3.4.nc";
    private static String IncompleteMultiDimensionalMultipleProfiles = "resources/datasets/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2.nc";
    private static String IndexedRaggedMultipleProfiles = "resources/datasets/profile-Indexed-Ragged-MultipleProfiles-H.3.5/profile-Indexed-Ragged-MultipleProfiles-H.3.5.nc";
    private static String OrthogonalMultiDimensionalMultipleProfiles = "resources/datasets/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1.nc";
    private static String OrthogonalSingleDimensionalSingleProfile = "resources/datasets/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3.nc";
    // testing external file from hawaii
    private static String ExternalTestFileStation = "resources/datasets/sura/wqbkn_2012_08_01.nc";

    private static String base = null;
    
    // trajectories
    private static String TCRMTH43 = "resources/datasets/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.nc";
    
    // section
    private static String SectionMultidimensionalMultiTrajectories = "resources/datasets/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1.nc";
    
    // Grid
    private static String GridSST26 = "resources/datasets/satellite-sst/SST_Global_2x2deg_20120626_0000.nc";
    
    // NODC
    private static final String nodc_resource = "resources/datasets/nodc/";
    private static final String nodc_pathfinder = nodc_resource + "00000110200000-NODC-L4_GHRSST-SSTskin-AVHRR_Pathfinder-PFV5.0_Daily_Climatology_1982_2008_DayNightCombined-v02.0-fv01.0.nc";
    private static final String nodc_aoml_tsg = nodc_resource + "aoml_tsg.nc";
    private static final String nodc_bodega_lab_buoy = nodc_resource + "BodegaMarineLabBuoy.nc";
    private static final String nodc_bodega_lab_combined = nodc_resource + "BodegaMarineLabBuoyCombined.nc";
    private static final String nodc_sat_altimeter = nodc_resource + "jason2_satelliteAltimeter.nc";
    private static final String nodc_kachemak = nodc_resource + "KachemakBay.nc";
    private static final String nodc_okeanos = nodc_resource + "Okeanos.nc";
    private static final String nodc_usgs_int_wave = nodc_resource + "usgs_internal_wave_timeSeries.nc";
    private static final String nodc_wod_obs = nodc_resource + "wodObservedLevels.nc";
    private static final String nodc_wod_std = nodc_resource + "wodStandardLevels.nc";
    
    //
    
    @BeforeClass
    public static void SetupEnviron() throws FileNotFoundException {
        // not really a test, just used to set up the various string values
        if (base != null && baseLocalDir != null && baseTomcatDir != null && exampleOutputDir != null) {
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
            base = XMLDomUtils.getNodeValue(configDoc, container, "outputDirectory");
            // add a get_caps for output dir
            base += "get_caps/";
            baseLocalDir = XMLDomUtils.getNodeValue(configDoc, container, "projectDirectory");
            baseTomcatDir = XMLDomUtils.getNodeValue(configDoc, container, "tomcatLocation");
            exampleOutputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDirectory");
            // add examples for output dir
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
        
        File file = new File(base);
        file.mkdirs();
        
        file = new File(exampleOutputDir);
        file.mkdirs();
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
        System.out.println("Your file has been written");
    }    
    
    private static String getCurrentMethod() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i=0; i<ste.length; i++) {
            if (ste[i].getMethodName().contains(("test")))
                return ste[i].getMethodName();
        }
        return "could not find test name";
    }
    

    
    public void testCanIdentifyTimeSeriesCDM() throws IOException {
        
       NetcdfDataset dataset = NetcdfDataset.openDataset(imeds8);
        SOSGetCapabilitiesRequestHandler sosget = new SOSGetCapabilitiesRequestHandler(dataset, "threddsURI-IMEDS8", "All");
         assertEquals(FeatureType.STATION, sosget.getDatasetFeatureType());
         //station
    }
    
    
    @Test
    public void testCanIdentifyTrajectoryCDM() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + TCRMTH43);
        SOSGetCapabilitiesRequestHandler sosget = new SOSGetCapabilitiesRequestHandler(dataset, "threddsURI", "All");
        assertEquals(FeatureType.TRAJECTORY, sosget.getDatasetFeatureType());
        //trajectory

        System.out.println("------END " + getCurrentMethod() + "------");
    }
    
    @Test
    public void testCanProcessTrajectory() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + TCRMTH43);
        String fileName = "trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.xml";
        assertNotNull(dataset);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + TCRMTH43),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        // write as an example
        fileWriter(exampleOutputDir, "GetCapabilities-Trajectory.xml", write);
        //traj
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }
    
    @Test
    public void testTrajLatLongCorrect() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + TCRMTH43);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + TCRMTH43),write);
        write.flush();
        write.close();
        String fileName = "trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.xml";
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<gml:lowerCorner>3.024412155151367 -68.12552642822266</gml:lowerCorner>"));
        assertTrue(write.toString().contains("<gml:upperCorner>43.00862503051758 -1.6318601369857788</gml:upperCorner>"));
        //traj
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testTrajStartEndTimeCorrect() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + TCRMTH43);
        String fileName = "trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.xml";
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + TCRMTH43),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<gml:beginPosition>1990-01-01T00:00:00Z</gml:beginPosition>"));
        assertTrue(write.toString().contains("<gml:endPosition>1990-01-01T23:00:00Z</gml:endPosition>"));
        //traj
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }
    
    // caching doesn't quite work just yet
    @Test
    public void testCacheReturnsTrueFileDoesNOTExist() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest + "&useCache=true", tsIncompleteMultiDimensionalMultipleStations, baseTomcatDir + catalinaThredds),write);
        fileWriter(base, getCurrentMethod() + ".xml", write);
        assertFalse("Exception in output", write.toString().contains("Exception"));
        // check that the cache file was correctly made
        String[] strSplit = tsIncompleteMultiDimensionalMultipleStations.split("/");
        String filename = strSplit[strSplit.length-1];
        strSplit = filename.split("\\.");
        filename = "";
        for (int i=0;i<strSplit.length-1;i++) {
            filename += strSplit[i] + ".";
        }
        filename += "xml";
        File file = new File(baseTomcatDir + catalinaThredds + "/" + filename);
        assertTrue("Cached file not created", file.exists());
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }
    
    @Test
    public void testReadInOperationsMeatadataFromCache() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest + "&useCache=true&sections=OperationsMetadata", tsIncompleteMultiDimensionalMultipleStations, baseTomcatDir + catalinaThredds),write);
        fileWriter(base, getCurrentMethod() + ".xml", write);
        assertFalse("Exception in output", write.toString().contains("Exception"));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testCacheReturnsTrueFileDoesExist() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest + "&useCache=true", tsIncompleteMultiDimensionalMultipleStations, baseTomcatDir + catalinaThredds),write);
        fileWriter(base, getCurrentMethod() + ".xml", write);
        assertFalse("Exception in output", write.toString().contains("Exception"));
        // remove the cache file
        String[] strSplit = tsIncompleteMultiDimensionalMultipleStations.split("/");
        String filename = strSplit[strSplit.length-1];
        strSplit = filename.split("\\.");
        filename = "";
        for (int i=0;i<strSplit.length-1;i++) {
            filename += strSplit[i] + ".";
        }
        filename += "xml";
        File file = new File(baseTomcatDir + catalinaThredds + "/" + filename);
        assertTrue("Cached file does NOT exist", file.exists());
        assertTrue("Could not delete the cached file", file.delete());
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Ignore
    @Test
    public void testAddAdditionalParamForCachingDataTRUE() throws IOException {
        fail("removed - caching temporarily unavailable");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();

        String fileName = baseTomcatDir + catalinaThredds + "/watlev_IKE.xml";
        //check file exists
        File f = new File(fileName);
        f.delete();

        writeOutput(md.enhanceGETRequest(dataset, baseRequest + "&useCache=true", imeds13, baseTomcatDir + catalinaThredds),write);
//        HashMap<String, Object> outMap = md.enhanceGETRequest(dataset, null, imeds13, baseTomcatDir + catalinaThredds);
        write.flush();
        write.close();
        if (write.toString().contains("Exception")) {
            System.out.println("have exception - testAddAdditionalParamForCachingDataTRUE");
            assertFalse(write.toString().contains("Exception"));
        }
        f = new File(fileName);
//        f.createNewFile();
        if (!f.exists()) {
            System.out.println("file does not exist - testAddAdditionalParamForCachingDataTRUE");
            assertTrue(f.exists());
        }
    }

    @Test
    public void testLargeDatasets() throws IOException {
//        fail("removed - test is expensive");
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);
        String fileName = "largeDataSetIKE.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();

        long start = System.currentTimeMillis();

        writeOutput(md.enhanceGETRequest(dataset, baseRequest, imeds13),write);

        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));

        long elapsedTimeMillis = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMillis / 1000F;
        System.out.println("\nTime To Complete Mil: " + elapsedTimeMillis + ": SEC: " + elapsedTimeSec);
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

//**********************************
//TIMESERIES TEST
    @Test
    public void testIncompleteMultiDimensionalMultipleStations() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);
        String fileName = "tsIncompleteMultiDimensionalMultipleStations.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, tsIncompleteMultiDimensionalMultipleStations),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        //station
        // write as an example
        fileWriter(exampleOutputDir, "GetCapabilities-TimeSeries.xml", write);
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testOrthogonalMultidimenstionalMultipleStations() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsOrthogonalMultidimenstionalMultipleStations);
        String fileName = "tsOrthogonalMultidimenstionalMultipleStations.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, tsOrthogonalMultidimenstionalMultipleStations),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        if(write.toString().contains("Exception")) {
            System.out.println("have exception - testOrthogonalMultidimenstionalMultipleStations");
            assertFalse(write.toString().contains("Exception"));
        }
        if(!write.toString().contains("<sos:ObservationOffering gml:id=")) {
            System.out.println("does not have expected tag - testOrthogonalMultidimenstionalMultipleStations");
            assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        }
        //station
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }
//**********************************
//TIMESERIESPROFILE TEST

    @Test
    public void testenhanceSingleRaggedDataset() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedSingleConventions);
        String fileName = "RaggedSingleConventions.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, RaggedSingleConventions),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        //station_profile
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testenhanceMultiRaggedDataset() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);
        String fileName = "RaggedMultiConventions.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, RaggedMultiConventions),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        //station_Profile
        // write as an example
        fileWriter(exampleOutputDir, "GetCapabilities-TimeSeriesProfile.xml", write);
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testOrthogonalMultidimensionalMultiStations() throws IOException {
//        fail("removed - file does not parse correctly in netcdf : Table Structure(record) featureType POINT: lat/lon/time coord not found");
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + OrthogonalMultidimensionalMultiStations);
        String fileName = "OrthogonalMultidimensionalMultiStations.xml";
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + OrthogonalMultidimensionalMultiStations),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testMultiDimensionalSingleStations() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalSingleStations);
        String fileName = "MultiDimensionalSingleStations.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, MultiDimensionalSingleStations),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testMultiDimensionalMultiStations() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);
        String fileName = "MultiDimensionalMultiStations.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, MultiDimensionalMultiStations),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testMultiDimensionalMultiStationsLocal() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);
        String fileName = "MultiDimensionalMultiStations.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, MultiDimensionalMultiStations),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        //station_profile
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

//**********************************
//PROFILE TEST
    @Test
    public void testContiguousRaggedMultipleProfiles() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        String fileName = "ContiguousRaggedMultipleProfiles.xml";
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, ContiguousRaggedMultipleProfiles),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        // write as an example
        fileWriter(exampleOutputDir, "GetCapabilities-Profile.xml", write);
        
        System.out.println("------END " + getCurrentMethod() + "------");
        //profile
    }

    private void spaceBetweenTests() {
        System.out.println("/n");
    }

    @Test
    public void testIncompleteMultiDimensionalMultipleProfiles() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(IncompleteMultiDimensionalMultipleProfiles);
        String fileName = "IncompleteMultiDimensionalMultipleProfiles.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, IncompleteMultiDimensionalMultipleProfiles),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testIndexedRaggedMultipleProfiles() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(IndexedRaggedMultipleProfiles);
        String fileName = "IndexedRaggedMultipleProfiles.xml";
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, IndexedRaggedMultipleProfiles),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testOrthogonalMultiDimensionalMultipleProfiles() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalMultiDimensionalMultipleProfiles);
        String fileName = "OrthogonalMultiDimensionalMultipleProfiles.xml";
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, OrthogonalMultiDimensionalMultipleProfiles),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testOrthogonalSingleDimensionalSingleProfile() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalSingleDimensionalSingleProfile);
        String fileName = "OrthogonalSingleDimensionalSingleProfile.xml";
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, OrthogonalSingleDimensionalSingleProfile),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }
//**********************************

    @Test
    public void testMetaDataParserServiceIdentification() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds", "All");
        handler.parseGetCapabilitiesDocument();
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testMetaDataParserServiceDescription() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds", "All");
        handler.parseGetCapabilitiesDocument();
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testMetaDataParserOperationMetaData() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds", "All");
        handler.parseGetCapabilitiesDocument();
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testMetaDataParserObsList() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds", "All");
        handler.parseGetCapabilitiesDocument();
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testNOAANDBCMetaDataParserObsList() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds15);

        String cdm_datatype = dataset.findAttValueIgnoreCase(null, "cdm_data_type", null);
        System.out.println(cdm_datatype);
        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "noaa", "All");
        handler.parseGetCapabilitiesDocument();
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testImedsNetcdfFileDoeNotCauseNullObject() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);
        String cdm_datatype = dataset.findAttValueIgnoreCase(null, "cdm_data_type", null);
        System.out.println(cdm_datatype);
        FeatureDataset featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.STATION, dataset, null, new Formatter(System.err));

        assertTrue(featureDataset != null);
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Ignore  // test is failing, need to look into it
    @Test
    public void testStationFileNotNull() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
//        fail("Station file removed");
        NetcdfDataset dataset = NetcdfDataset.openDataset(null);
        String cdm_datatype = dataset.findAttValueIgnoreCase(null, "cdm_data_type", null);
        System.out.println(cdm_datatype);
        FeatureDataset featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.STATION, dataset, null, new Formatter(System.err));
//        assertTrue(featureDataset != null);
        assertFalse(featureDataset != null);
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testenhanceImedsDataset() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, imeds1),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testenhanceNOAADataset() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds15);

        String fileName = "NOAA.xml";
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, imeds15),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testenhanceNOAADataset2() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds15);
        String fileName = "NOAA2.xml";

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, imeds15),write);
        write.flush();
        write.close();
        fileWriter(base, fileName, write);
        assertFalse(write.toString().contains("Exception"));
        assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testenhanceTCOONDatasetNew() throws IOException {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds5);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, imeds5),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }

    @Test
    public void testenhancePoint() throws IOException {
        // currently unsupported, expect exception
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(cfPoint);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhanceGETRequest(dataset, baseRequest, cfPoint),write);
        write.flush();
        write.close();
        fileWriter(base, "testEnhancePoint.xml", write);
        assertTrue(write.toString().contains("Exception"));
        
        System.out.println("------END " + getCurrentMethod() + "------");
    }
    
    @Test
    public void testSectionMultidimensionalMultiTrajectories() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(SectionMultidimensionalMultiTrajectories);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, SectionMultidimensionalMultiTrajectories),write);
            write.flush();
            write.close();
            String fileName = "trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1.xml";
            fileWriter(base, fileName, write);
            // write as an example
            assertFalse(write.toString().contains("Exception"));
            assertTrue(write.toString().contains("<sos:ObservationOffering gml:id="));
            fileWriter(exampleOutputDir, "GetCapabilities-Section.xml", write);
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
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + ExternalTestFileStation);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + ExternalTestFileStation),write);
            write.flush();
            write.close();
            String fileName = "External_Hawaii.xml";
            fileWriter(base, fileName, write);
            assertFalse(write.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testNodcPathfinderSST() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_pathfinder);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_pathfinder),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertFalse("Exception in output", write.toString().contains("Exception"));
            
            // write as an example
            fileWriter(exampleOutputDir, "Nodc-GetCapabilities-PathfinderSST.xml", write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testNodcAomlTsg() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_aoml_tsg);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_aoml_tsg),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertFalse("Exception in output", write.toString().contains("Exception"));
            
            // write as an example
            fileWriter(exampleOutputDir, "Nodc-GetCapabilities-AomlTsg.xml", write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testNodcBodegaMarineLabBuoy() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_bodega_lab_buoy);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_bodega_lab_buoy),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertFalse("Exception in output", write.toString().contains("Exception"));
            
            // write as an example
            fileWriter(exampleOutputDir, "Nodc-GetCapabilities-BodegaMarineLabBuoy.xml", write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    // the feature set cannot be wrapped, for some reason...
    @Ignore
    @Test
    public void testNodcBodegaMarineLabBuoyCombined() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_bodega_lab_combined);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_bodega_lab_combined),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertFalse("Exception in output", write.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testNodcJasonSatelliteAltimeter() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_sat_altimeter);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_sat_altimeter),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertFalse("Exception in output", write.toString().contains("Exception"));
            
            // write as an example
            fileWriter(exampleOutputDir, "Nodc-GetCapabilities-JasonSatelliteAltimeter.xml", write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testNodcKachemakBay() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_kachemak);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_kachemak),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertTrue("Feature set is now supported", write.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    // test causes JVM to run out of heap memory, but in checking to see if this can be skipped with an exception
    @Test
    public void testNodcOkeanos() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_okeanos);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_okeanos),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertFalse("Exception in output", write.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    // The feature type cannot be wrapped, for some reason...
    @Test
    public void testNodcUsgsInternalWave() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_usgs_int_wave);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_usgs_int_wave),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertFalse("Exception in output", write.toString().contains("Exception"));
            
            // write as an example
            fileWriter(exampleOutputDir, "Nodc-GetCapabilities-USGSInternalWave.xml", write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testNodcWodObservedLevels() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_wod_obs);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_wod_obs),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertFalse("Exception in output", write.toString().contains("Exception"));
            
            // write as an example
            fileWriter(exampleOutputDir, "Nodc-GetCapabilities-WodObservedLevels.xml", write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testNodcWodStandardLevels() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + nodc_wod_std);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + nodc_wod_std),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            assertFalse("Exception in output", write.toString().contains("Exception"));
            
            // write as an example
            fileWriter(exampleOutputDir, "Nodc-GetCapabilities-WodStandardLevels.xml", write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testSectionServiceIdentification() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + OrthogonalMultidimensionalMultiStations);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, ServiceIdRequest, baseLocalDir + OrthogonalMultidimensionalMultiStations),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            String testOut = write.toString();
            assertFalse("Exception in output", testOut.contains("Exception"));
            assertFalse("ServiceProvider in response", testOut.contains("ows:ServiceProvider"));
            assertFalse("OperationsMetadata in response", testOut.contains("ows:OperationsMetadata"));
            assertFalse("Contents in response", testOut.contains("<sos:Contents>"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testSectionServiceProvider() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + OrthogonalMultidimensionalMultiStations);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, ServiceProvRequest, baseLocalDir + OrthogonalMultidimensionalMultiStations),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            String testOut = write.toString();
            assertFalse("Exception in output", testOut.contains("Exception"));
            assertFalse("ServiceIdnetification in response", testOut.contains("ows:ServiceIdentification"));
            assertFalse("OperationsMetadata in response", testOut.contains("ows:OperationsMetadata"));
            assertFalse("Contents in response", testOut.contains("<sos:Contents>"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testSectionOperationsMetadata() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + OrthogonalMultidimensionalMultiStations);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, OperationsRequest, baseLocalDir + OrthogonalMultidimensionalMultiStations),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            String testOut = write.toString();
            assertFalse("Exception in output", testOut.contains("Exception"));
            assertFalse("ServiceIdnetification in response", testOut.contains("ows:ServiceIdentification"));
            assertFalse("ServiceProvider in response", testOut.contains("ows:ServiceProvider"));
            assertFalse("Contents in response", testOut.contains("<sos:Contents>"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testSectionContents() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + OrthogonalMultidimensionalMultiStations);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, ContentsRequest, baseLocalDir + OrthogonalMultidimensionalMultiStations),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            String testOut = write.toString();
            assertFalse("Exception in output", testOut.contains("Exception"));
            assertFalse("ServiceIdnetification in response", testOut.contains("ows:ServiceIdentification"));
            assertFalse("ServiceProvider in response", testOut.contains("ows:ServiceProvider"));
            assertFalse("OperationsMetadata in response", testOut.contains("ows:OperationsMetadata"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testSectionOpsMetaAndContents() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + OrthogonalMultidimensionalMultiStations);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, OpsAndContentsRequest, baseLocalDir + OrthogonalMultidimensionalMultiStations),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            String testOut = write.toString();
            assertFalse("Exception in output", testOut.contains("Exception"));
            assertFalse("ServiceIdnetification in response", testOut.contains("ows:ServiceIdentification"));
            assertFalse("ServiceProvider in response", testOut.contains("ows:ServiceProvider"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testBadSectionRequest() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + OrthogonalMultidimensionalMultiStations);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, BadSectionRequest, baseLocalDir + OrthogonalMultidimensionalMultiStations),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            String testOut = write.toString();
            assertTrue("Exception not in output", testOut.contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testSatelliteSSTGrid() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + GridSST26);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, baseLocalDir + GridSST26),write);
            write.flush();
            write.close();
            String fileName = getCurrentMethod() + ".xml";
            fileWriter(base, fileName, write);
            String testOut = write.toString();
            assertFalse("Exception in output", testOut.contains("Exception"));
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
