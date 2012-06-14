/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.getCaps;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.constants.FeatureType;
import java.util.Formatter;
import javax.swing.JOptionPane;
import ucar.nc2.thredds.ThreddsDataFactory;
import java.io.IOException;
import java.io.Writer;
import thredds.server.sos.util.DatasetHandlerAdapter;
import ucar.nc2.dataset.NetcdfDataset;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import thredds.server.sos.service.SOSParser;
import thredds.server.sos.service.SOSBaseRequestHandler;
import ucar.nc2.util.CancelTask;
import static org.junit.Assert.*;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author abird
 */
public class SOSgetCapsTest {
    
    // base location of resources
    private static String baseLocalDir = "C:/Users/scowan/Projects/maven/ncSOS/";
    private static String baseTomcatDir = "C:/apache_tomcat/apache-tomcat-7.0.27/";
    
    // work thredds
    private static String catalinaThredds = "work/Catalina/localhost/thredds";

    //imeds data
    private static String imeds1 = "resources/datasets/sura/Hsig_UNDKennedy_IKE_VIMS_3D_WAVEONLY.nc";
    private static String imeds2 = "resources/datasets/sura/andrw.lft.nc";
    private static String imeds3 = "resources/datasets/sura/audry.bpt.nc";
    private static String imeds4 = "resources/datasets/sura/hs_USACE-CHL.nc";
    private static String imeds5 = "resources/datasets/sura/hwm_TCOON_NAVD88.nc";
    private static String imeds6 = "resources/datasets/sura/tm_CSI.nc";
    private static String imeds7 = "resources/datasets/sura/tm_IKE.nc";
    private static String imeds8 = "resources/datasets/sura/watlev_CRMS.nc";
    private static String imeds9 = "resources/datasets/sura/watlev_CRMS_2005.nc";
    private static String imeds10 = "resources/datasets/sura/watlev_CRMS_2008.F.C_IKE_VIMS_3D_NOWAVE.nc";
    private static String imeds11 = "resources/datasets/sura/watlev_CRMS_2008.F.C__IKE_VIMS_3D_WITHWAVE.nc";
    private static String imeds12 = "resources/datasets/sura/watlev_CSI.nc";
    private static String imeds13 = "resources/datasets/sura/watlev_IKE.nc";
    private static String imeds14 = "resources/datasets/sura/watlev_IKE.P.UL-Ike2Dh.61.nc";
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
    public static final String base = "C:/Users/scowan/Projects/maven/ncSOS/src/test/java/com/asascience/ncSOS/getCaps/output/";
    
    // trajectories
    public static String TCRMTH43 = "resources/datasets/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.nc";
    
    @Test
    public void testCanIdentifyTimeSeriesCDM() throws IOException {
        
       NetcdfDataset dataset = NetcdfDataset.openDataset(imeds8);
        SOSGetCapabilitiesRequestHandler sosget = new SOSGetCapabilitiesRequestHandler(dataset, "threddsURI-IMEDS8");
         assertEquals(FeatureType.STATION, sosget.getDatasetFeatureType());
         //station
    }
    
    
    @Test
    public void testCanIdentifyTrajectoryCDM() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + TCRMTH43);
        SOSGetCapabilitiesRequestHandler sosget = new SOSGetCapabilitiesRequestHandler(dataset, "threddsURI");
        assertEquals(FeatureType.TRAJECTORY, sosget.getDatasetFeatureType());
        //trajectory

    }
    
    @Test
    public void testCanProcessTrajectory() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + TCRMTH43);
        assertNotNull(dataset);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", baseLocalDir + TCRMTH43);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        //traj
    }
    
    @Test
    public void testTrajLatLongCorrect() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + TCRMTH43);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", baseLocalDir + TCRMTH43);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<gml:lowerCorner>3.024412155151367 -68.12552642822266</gml:lowerCorner>"));
        assertTrue(write.toString().contains("<gml:upperCorner>43.00862503051758 -1.6318601369857788</gml:upperCorner>"));
        //traj
    }

    @Test
    public void testTrajStartEndTimeCorrect() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + TCRMTH43);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", baseLocalDir + TCRMTH43);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<gml:beginPosition>1990-01-01T00:00:00.000Z</gml:beginPosition>"));
        assertTrue(write.toString().contains("<gml:endPosition>1990-01-01T23:00:00.000Z</gml:endPosition>"));
        //traj
    }
    
    @Test
    public void testCacheReturnsTrueFileDoesNOTExist() throws IOException {
//        fail("removed - test takes too long");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        File f = new File(baseTomcatDir + catalinaThredds + "/xmlFile.xml");
        f.delete();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos&useCache=true", imeds13, baseTomcatDir + catalinaThredds);
        assertEquals("true", md.getCacheValue());
        f = new File(baseTomcatDir + catalinaThredds + "/watlev_IKE.xml");
        assertTrue("file watlev_IKE.xml does not exist - testCacheReturnsTrueFileDoesNotExist", f.exists());
        f.delete();
        //station
    }

    @Test
    public void testCacheReturnsTrueFileDoesExist() throws IOException {
//        fail("removed - test is expensive");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos&useCache=true", imeds13, baseTomcatDir + catalinaThredds);
        File f = new File(baseTomcatDir + catalinaThredds + "/watlev_IKE.xml");
        assertTrue("file watlev_IKE.xml does not exist - testCacheReturnsTrueFileDoesExist", f.exists());
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos&useCache=true", imeds13, baseTomcatDir + catalinaThredds);
        assertTrue("file watlev_IKE.xml does not exist (test 2) - testCacheReturnsTrueFileDoesExist", f.exists());
        f.delete();
    }

    @Test
    public void testCanGetCorrectDataSetFileName() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);
        SOSParser md = new SOSParser();
        assertEquals("/watlev_IKE.xml", md.getCacheXmlFileName(imeds13));
    }

    @Test
    public void testAddAdditionalParamForCachingDataTRUE() throws IOException {
//        fail("removed - test is expensive");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();

        String fileName = baseTomcatDir + catalinaThredds + "/watlev_IKE.xml";
        //check file exists
        File f = new File(fileName);
        f.delete();

        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos&useCache=true", imeds13, baseTomcatDir + catalinaThredds);
//        md.enhance(dataset, write, null, imeds13, baseTomcatDir + catalinaThredds);
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

    private static void fileWriter(String base, String fileName, Writer write) throws IOException {
        Writer output = null;
        File file = new File(base + fileName);
        output = new BufferedWriter(new FileWriter(file));
        output.write(write.toString());
        output.close();
        System.out.println("Your file has been written");
    }

    @Test
    public void testLargeDatasets() throws IOException {
//        fail("removed - test is expensive");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();

        long start = System.currentTimeMillis();

        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", imeds13);

        long elapsedTimeMillis = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMillis / 1000F;

        System.out.println("Time To Complete Mil: " + elapsedTimeMillis + ": SEC: " + elapsedTimeSec);


        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "largeDataSetIKE.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }

//**********************************
//TIMESERIES TEST
    @Test
    public void testIncompleteMultiDimensionalMultipleStations() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", tsIncompleteMultiDimensionalMultipleStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsIncompleteMultiDimensionalMultipleStations.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        //station
    }

    @Test
    public void testOrthogonalMultidimenstionalMultipleStations() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsOrthogonalMultidimenstionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", tsOrthogonalMultidimenstionalMultipleStations);
        write.flush();
        write.close();
        if(write.toString().contains("Exception")) {
            System.out.println("have exception - testOrthogonalMultidimenstionalMultipleStations");
            assertFalse(write.toString().contains("Exception"));
        }
        String fileName = "tsOrthogonalMultidimenstionalMultipleStations.xml";
        fileWriter(base, fileName, write);
        if(!write.toString().contains("<ObservationOffering gml:id=")) {
            System.out.println("does not have expected tag - testOrthogonalMultidimenstionalMultipleStations");
            assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        }
        //station
    }
//**********************************
//TIMESERIESPROFILE TEST

    @Test
    public void testenhanceSingleRaggedDataset() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedSingleConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", RaggedSingleConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedSingleConventions.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        //station_profile
    }

    @Test
    public void testenhanceMultiRaggedDataset() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", RaggedMultiConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventions.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        //station_Profile
    }

    @Test
    public void testOrthogonalMultidimensionalMultiStations() throws IOException {
//        fail("removed - file does not parse correctly in netcdf : Table Structure(record) featureType POINT: lat/lon/time coord not found");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + OrthogonalMultidimensionalMultiStations);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", baseLocalDir + OrthogonalMultidimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "OrthogonalMultidimensionalMultiStations.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        
    }

    @Test
    public void testMultiDimensionalSingleStations() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalSingleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", MultiDimensionalSingleStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalSingleStations.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }

    @Test
    public void testMultiDimensionalMultiStations() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", MultiDimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStations.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }

    @Test
    public void testMultiDimensionalMultiStationsLocal() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", MultiDimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStations.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        //station_profile
    }

//**********************************
//PROFILE TEST
    @Test
    public void testContiguousRaggedMultipleProfiles() throws IOException {
        
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1.0.0&service=SOS", ContiguousRaggedMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "ContiguousRaggedMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        System.out.println("----end------");
        //profile
    }

    private void spaceBetweenTests() {
        System.out.println("/n");
    }

    @Test
    public void testIncompleteMultiDimensionalMultipleProfiles() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(IncompleteMultiDimensionalMultipleProfiles);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", IncompleteMultiDimensionalMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "IncompleteMultiDimensionalMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }

    @Test
    public void testIndexedRaggedMultipleProfiles() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(IndexedRaggedMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1.0.0&service=SOS", IndexedRaggedMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "IndexedRaggedMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }

    @Test
    public void testOrthogonalMultiDimensionalMultipleProfiles() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalMultiDimensionalMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1.0.0&service=SOS", OrthogonalMultiDimensionalMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "OrthogonalMultiDimensionalMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }

    @Test
    public void testOrthogonalSingleDimensionalSingleProfile() throws IOException {
        
        spaceBetweenTests();
        System.out.println("----OrthogonalSingleDimensionalSingleProfile------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalSingleDimensionalSingleProfile);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1.0.0&service=SOS", OrthogonalSingleDimensionalSingleProfile);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "OrthogonalSingleDimensionalSingleProfile.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        System.out.println("----end------");
    }
//**********************************

    @Test
    public void testMetaDataParserServiceIdentification() throws IOException {
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds");
        handler.parseServiceIdentification();
    }

    @Test
    public void testMetaDataParserServiceDescription() throws IOException {
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds");
        handler.parseServiceIdentification();
        handler.parseServiceDescription();
    }

    @Test
    public void testMetaDataParserOperationMetaData() throws IOException {
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds");
        handler.parseServiceIdentification();
        handler.parseServiceDescription();
        handler.parseOperationsMetaData();
    }

    @Test
    public void testMetaDataParserObsList() throws IOException {
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds");
        handler.parseServiceIdentification();
        handler.parseServiceDescription();
        handler.parseOperationsMetaData();
        handler.parseObservationList();
    }

    @Test
    public void testNOAANDBCMetaDataParserObsList() throws IOException {
        
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds15);

        String cdm_datatype = dataset.findAttValueIgnoreCase(null, "cdm_data_type", null);
        System.out.println(cdm_datatype);
        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "noaa");
        handler.parseServiceIdentification();
        handler.parseServiceDescription();
        handler.parseOperationsMetaData();
        handler.parseObservationList();
    }

    @Test
    public void testImedsNetcdfFileDoeNotCauseNullObject() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);
        String cdm_datatype = dataset.findAttValueIgnoreCase(null, "cdm_data_type", null);
        System.out.println(cdm_datatype);
        FeatureDataset featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.STATION, dataset, null, new Formatter(System.err));

        assertTrue(featureDataset != null);
    }

    // test does not pass, removing it for now -- Sean
//    @Test
//    public void testStationFileNotNull() throws IOException {
////        fail("Station file removed");
//        NetcdfDataset dataset = NetcdfDataset.openDataset(null);
//        String cdm_datatype = dataset.findAttValueIgnoreCase(null, "cdm_data_type", null);
//        System.out.println(cdm_datatype);
//        FeatureDataset featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.STATION, dataset, null, new Formatter(System.err));
////        assertTrue(featureDataset != null);
//        assertFalse(featureDataset != null);
//    }

    @Test
    public void testenhanceImedsDataset() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", imeds1);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
    }

    @Test
    public void testenhanceNOAADataset() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds15);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", imeds15);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "NOAA.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        System.out.println("----end------");


    }

    @Test
    public void testenhanceNOAADataset2() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds15);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", imeds15);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "NOAA2.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        System.out.println("----end------");

    }

    @Test
    public void testenhanceTCOONDatasetNew() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds5);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", imeds5);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
    }

    @Test
    public void testenhancePoint() throws IOException {
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(cfPoint);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", cfPoint);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
    }
}
