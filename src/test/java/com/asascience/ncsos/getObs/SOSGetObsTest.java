/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.getObs;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
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
public class SOSGetObsTest {
    
    private static String base = null;
    private static String exampleOutputDir = null;
   
    // final strings
    private static final String baseRequest = "request=GetObservation&version=1.0.0&service=sos&responseformat=text%2Fxml%3Bsubtype%3D%22om%2F1.0.0%22";
    
    private static final String imeds1 = "resources/datasets/sura/Hsig_UNDKennedy_IKE_VIMS_3D_WAVEONLY.nc";
    private static final String imeds1Req = baseRequest + "&observedProperty=hs&offering=UNDKennedy_S,UNDKennedy_X,UNDKennedy_Z&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String imeds4 = "resources/datasets/sura/hs_USACE-CHL.nc";
    private static final String imeds4Req = baseRequest + "&observedProperty=hs&offering=USACE-CHL_2410508B,USACE-CHL_2410513B,USACE-CHL_2410510B&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    private static final String imeds5 = "resources/datasets/sura/hwm_TCOON_NAVD88.nc";
    private static final String imeds5Req = baseRequest + "&observedProperty=hwm&offering=TCOON_87747701,TCOON_87705701,TCOON_87705201,TCOON_87704751,TCOON_87708221&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    private static final String imeds6 = "resources/datasets/sura/tm_CSI.nc";
    private static final String imeds6Req = baseRequest + "&observedProperty=tp&offering=CSI_15,CSI_06,CSI_09&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String imeds7 = "resources/datasets/sura/tm_IKE.nc";
    private static final String imeds7Req = baseRequest + "&observedProperty=tp&offering=CSI_06,CSI_09,NDBC_42020,NDBC_42019,USACE-CHL_2410513B,NDBC_42059&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String imeds8 = "resources/datasets/sura/watlev_CRMS.nc";
    private static final String imeds8Req = baseRequest + "&observedProperty=watlev&offering=CRMS_CS20-106,CRMS_CS20-15R,CRMS_DCPBS03,CRMS_DCPBS04&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String imeds9 = "resources/datasets/sura/watlev_CRMS_2005.nc";
    private static final String imeds9Req = baseRequest + "&observedProperty=watlev&offering=CRMS_CS20-106,CRMS_CS20-15R,CRMS_DCPBS03,CRMS_DCPBS04&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String imeds10 = "resources/datasets/sura/watlev_CRMS_2008.F.C_IKE_VIMS_3D_NOWAVE.nc";
    private static final String imeds10Req = baseRequest + "&observedProperty=watlev&offering=CRMS_CRMS0161-H01,CRMS_DCPBA07,CRMS_CRMS0174-H01,&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String imeds11 = "resources/datasets/sura/watlev_CRMS_2008.F.C__IKE_VIMS_3D_WITHWAVE.nc";
    private static final String imeds11Req = baseRequest + "&observedProperty=watlev&offering=CRMS_CRMS0161-H01,CRMS_DCPBA07,CRMS_CRMS0174-H01,&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String imeds12 = "resources/datasets/sura/watlev_CSI.nc";
    private static final String imeds12Req = baseRequest + "&observedProperty=watlev&offering=CSI_03,CSI_06,CSI_09,CSI_15&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String imeds13 = "resources/datasets/sura/watlev_IKE.nc";
    private static final String imeds13Req = baseRequest + "&observedProperty=watlev&offering=CSI_06,CSI_09&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String imeds14 = "resources/datasets/sura/watlev_IKE.P.UL-Ike2Dh.61.nc";
    
    private static final String imeds15 = "resources/datasets/sura/watlev_NOAA_NAVD_PRE.nc";
    private static final String imeds15Req = baseRequest + "&observedProperty=watlev&offering=NOAA_8727235,NOAA_8729501&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";
    
    private static final String tsIncompleteMultiDimensionalMultipleStations = "resources/datasets/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2.nc";
    private static final String timeSeriesIncomplete = baseRequest + "&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z";
    private static final String timeSeriesIncompleteWithTime = baseRequest + "&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z";
    private static final String timeSeriesIncompleteMulti = baseRequest + "&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z/1990-01-01T10:00:00Z";
    private static final String timeSeriesIncompleteMultiInvalid = baseRequest + "&observedProperty=temperature&offering=Station-9&eventtime=1990-02-01T00:00:00Z/1990-05-01T10:00:00Z";
    private static final String timeSeriesIncompleteMultiStation = baseRequest + "&observedProperty=temperature&offering=Station-9,Station-8&eventtime=1990-01-01T00:00:00Z/1990-01-01T8:00:00Z";
    private static final String timeSeriesIncompleteMultiStationx3 = baseRequest + "&observedProperty=temperature&offering=urn:tds:station.sos:Station-9,urn:tds:station.sos:Station-8,urn:tds:station.sos:Station-7&eventtime=1990-01-01T00:00:00Z/1990-01-01T8:00:00Z";
    
    private static final String tsOrthogonalMultidimenstionalMultipleStations = "resources/datasets/timeSeries-Orthogonal-Multidimenstional-MultipleStations-H.2.1/timeSeries-Orthogonal-Multidimenstional-MultipleStations-H.2.1.nc";
    private static final String timeSeriestOrth = baseRequest + "&observedProperty=alt&offering=Station-1&eventtime=1990-01-01T00:00:00Z";
    
    private static final String RaggedMultiConventions = "resources/datasets/timeSeriesProfile-Ragged-MultipeStations-H.5.3/timeSeriesProfile-Ragged-MultipeStations-H.5.3.nc";
    private static final String MultiDimensionalSingleStations = "resources/datasets/timeSeriesProfile-Multidimensional-SingleStation-H.5.2/timeSeriesProfile-Multidimensional-SingleStation-H.5.2.nc";
    private static final String MultiDimensionalMultiStations = "resources/datasets/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";
    
    private static final String ContiguousRaggedMultipleProfiles = "resources/datasets/profile-Contiguous-Ragged-MultipleProfiles-H.3.4/profile-Contiguous-Ragged-MultipleProfiles-H.3.4.nc";
    private static final String profileRequest = baseRequest + "&observedProperty=temperature,humidity&offering=0&eventTime=1990-01-01T00:00:00Z";
    private static final String profileRequestMultiTime = baseRequest + "&observedProperty=temperature,humidity&offering=1,2,3&eventTime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";
    private static final String profileRequestMultiTime2 = baseRequest + "&observedProperty=temperature,humidity&offering=1,2&eventTime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";
    private static final String profileRequestMultiTime3 = baseRequest + "&service=sos&observedProperty=temperature,humidity&offering=3&eventTime=1990-01-01T00:00:00Z/1990-01-01T03:00:00Z";
    
    private static final String IncompleteMultiDimensionalMultipleProfiles = "resources/datasets/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2.nc";
    
    private static final String OrthogonalMultiDimensionalMultipleProfiles = "resources/datasets/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1.nc";
    
    private static final String OrthogonalSingleDimensionalSingleProfile = "resources/datasets/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3.nc";
    
    private static final String IndexedRaggedMultipleProfiles = "resources/datasets/profile-Indexed-Ragged-MultipleProfiles-H.3.5/profile-Indexed-Ragged-MultipleProfiles-H.3.5.nc";
    private static final String profileRequestIndexed = baseRequest + "&observedProperty=temperature,humidity&offering=1&eventTime=1990-01-01T01:00:00Z/1990-01-01T04:00:00Z";
    
    private static final String timeSeriesProfileRequestSingle = baseRequest + "&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-01-01T00:00:00Z";
    private static final String timeSeriesProfileRequestMulti = baseRequest + "&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";
    private static final String timeSeriesProfileRequestMultiStation = baseRequest + "&observedProperty=temperature&offering=Station1,Station2&eventTime=1990-01-01T00:00:00Z/1990-01-01T05:00:00Z";

    private static final String timeSeriesProfileRequestMultiInvalidDates = baseRequest + "&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-04-01T00:00:00Z/1990-08-01T02:00:00Z";
    private static final String timeSeriesProfileRequest2 = baseRequest + "&observedProperty=temperature&offering=Station1&eventTime=1990-01-01T00:00:00Z";
    private static final String timeSeriesTimeRequestT2 = baseRequest + "&observedProperty=temperature&offering=Station1&eventtime=1990-01-01T02:00:00Z";
    private static final String timeSeriesTimeRequestT1 = baseRequest + "&observedProperty=temperature&offering=Station1&eventtime=1990-01-01T00:00:00Z";
    private static final String timeSeriesProfileRequest3 = baseRequest + "&observedProperty=temperature&offering=Station2&eventTime=1990-01-01T04:00:00Z";
    
    private static final String trajectoryIncompleteMultidimensionalMultipleTrajectories = "resources/datasets/trajectory-Incomplete-Multidimensional-MultipleTrajectories-H.4.1/trajectory-Incomplete-Multidimensional-MultipleTrajectories-H.4.1.nc";
    private static final String trajectoryIncompleteRequest1 = baseRequest + "&observedProperty=temperature&offering=Trajectory1&eventTime=1970-01-01T00:00:00Z/2012-07-11T00:00:00";
    
    private static final String trajectoryContiguousRaggedMultipleTrajectories = "resources/datasets/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3/trajectory-Contiguous-Ragged-MultipleTrajectories-H.4.3.nc";
    private static final String trajectoryContiguousRequest1 = baseRequest + "&observedProperty=temperature&offering=Trajectory1&eventtime=1970-01-01T00:00:00Z/2012-07-11T00:00:00";
    
    private static final String trajectoryProfileMultidimensionalMultipleTrajectories = "resources/datasets/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1/trajectoryProfile-Multidimensional-MultipleTrajectories-H.6.1.nc";
    private static final String sectionRequest1 = baseRequest + "&observedProperty=salinity&offering=Trajectory2&eventTime=1990-01-01T00:00:00Z";
    
    @BeforeClass
    public static void SetupEnviron() {
        // early return if the vars we are setting are already set
        if (base != null) {
            return;
        }
        String container = "getObs";
        InputStream templateInputStream = null;
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            
            base = XMLDomUtils.getNodeValue(configDoc, container, "outputBase");
            
            container = "examples";
            exampleOutputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDir");
        } catch (FileNotFoundException fnfex) {
            System.out.println(fnfex.getMessage());
        } finally {
            if (templateInputStream != null) {
                try {
                    templateInputStream.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        
        File file = new File(base);
        file.mkdirs();
    }
    
    /******************************
     * Private, Non-Junit Methods *
     ******************************/
    
    private static void dataAvailableInOutputFile(Writer write) {
        assertTrue("error no values in output", write.toString().contains("<swe:values>"));
        assertFalse("error no values: ERROR string in values", write.toString().contains("<swe:values>ERROR!</swe:values>"));
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
    
    private static void writeOutput(HashMap<String, Object> outMap, Writer writer) {
        SOSOutputFormatter output = (SOSOutputFormatter)outMap.get("outputHandler");
        assertNotNull("got null output", output);
        output.writeOutput(writer);
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
    public void testEnhanceIMEDS1() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds1Req, imeds1),write);
            write.flush();
            write.close();String fileName = "imeds1.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:UNDKennedy_X\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:UNDKennedy_S\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:UNDKennedy_Z\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMDEDS4() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds4);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds4Req, imeds4),write);
            write.flush();
            write.close();
            String fileName = "imeds4.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:USACE-CHL_2410513B\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:USACE-CHL_2410508B\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:USACE-CHL_2410510B\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS5() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds5);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds5Req, imeds5),write);
            write.flush();
            write.close();
            String fileName = "imeds5.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:TCOON_87747701\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:TCOON_87705701\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:TCOON_87704751\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS6() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds6);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds6Req, imeds6),write);
            write.flush();
            write.close();
            String fileName = "imeds6.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_15\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_06\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS7() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds7);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds7Req, imeds7),write);
            write.flush();
            write.close();
            String fileName = "imeds7.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:NDBC_42020\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:USACE-CHL_2410513B\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:NDBC_42059\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS8() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds8);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds8Req, imeds8),write);
            write.flush();
            write.close();
            String fileName = "imeds8.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBS04\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBS03\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CS20-15R\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CS20-106\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS9() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds9);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds9Req, imeds9),write);
            write.flush();
            write.close();
            String fileName = "imeds9.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBS04\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBS03\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CS20-15R\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CS20-106\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS10() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds10);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds10Req, imeds10),write);
            write.flush();
            write.close();
            String fileName = "imeds10.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0161-H01\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0174-H01\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBA07\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS11() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds11);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds11Req, imeds11),write);
            write.flush();
            write.close();
            String fileName = "imeds11.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0161-H01\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0174-H01\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBA07\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS12() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds12);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds12Req, imeds12),write);
            write.flush();
            write.close();
            String fileName = "imeds12.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_03\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_06\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_15\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS13() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds13Req, imeds13),write);
            write.flush();
            write.close();
            String fileName = "imeds13.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_06\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS14() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds14);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds13Req, imeds14),write);
            write.flush();
            write.close();
            String fileName = "imeds14.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_06\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testEnhanceIMEDS15() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(imeds15);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, imeds15Req, imeds15),write);
            write.flush();
            write.close();
            String fileName = "imeds15.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:NOAA_8727235\">"));
            assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:NOAA_8729501\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiTimeCreateDataStruct3Stations() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesIncompleteMultiStationx3, tsIncompleteMultiDimensionalMultipleStations),write);
            write.flush();
            write.close();
            String fileName = "tsDatax3.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            //check dates
            assertTrue("Time1 entered", write.toString().contains("1990-01-01T00:00:00Z,"));
            assertTrue("Time2 entered", write.toString().contains("1990-01-01T07:00:00Z,"));
            assertTrue("Time3 entered", write.toString().contains("1990-01-01T06:00:00Z,"));
            //check data
            assertTrue("Station8 Data T0", write.toString().contains("1990-01-01T00:00:00Z,32.0"));
            assertTrue("Station8 Data T2", write.toString().contains("1990-01-01T02:00:00Z,36.0"));
            assertTrue("Station9 Data T0", write.toString().contains("1990-01-01T00:00:00Z,37.0"));
            assertTrue("Station9 Data T2", write.toString().contains("1990-01-01T02:00:00Z,6.0"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiTimeCreateDataStruct() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesIncompleteMultiStation, tsIncompleteMultiDimensionalMultipleStations),write);
            write.flush();
            write.close();
            String fileName = "tsData.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            //check dates
            assertTrue("Time1 entered", write.toString().contains("1990-01-01T00:00:00Z,"));
            assertTrue("Time2 entered", write.toString().contains("1990-01-01T07:00:00Z,"));
            assertTrue("Time3 entered", write.toString().contains("1990-01-01T06:00:00Z,"));
            //check data
            assertTrue("Station8 Data T0", write.toString().contains("1990-01-01T00:00:00Z,32.0"));
            assertTrue("Station8 Data T2", write.toString().contains("1990-01-01T02:00:00Z,36.0"));
            assertTrue("Station9 Data T0", write.toString().contains("1990-01-01T00:00:00Z,37.0"));
            assertTrue("Station9 Data T2", write.toString().contains("1990-01-01T02:00:00Z,6.0"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesIncompleteDataMultiTimeInvalid() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesIncompleteMultiInvalid, tsIncompleteMultiDimensionalMultipleStations),write);
            write.flush();
            write.close();
            String fileName = "tsIncompleteMultiDimensionalMultipleStationsMultiTimeInvalid.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            //dataAvailableInOutputFile(write);

            assertFalse("Time1 entered", write.toString().contains("1990-01-01T00:00:00Z,"));
            assertFalse("Time2 entered", write.toString().contains("1990-01-01T10:00:00Z,"));

            assertFalse("Time3 entered", write.toString().contains("1990-01-01T11:00:00Z,"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesIncompleteDataMultiTime() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesIncompleteMulti, tsIncompleteMultiDimensionalMultipleStations),write);
            write.flush();
            write.close();
            String fileName = "tsIncompleteMultiDimensionalMultipleStationsMultiTime.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);

            assertTrue("Time1 not entered", write.toString().contains("1990-01-01T00:00:00Z,"));
            assertTrue("Time2 not entered", write.toString().contains("1990-01-01T10:00:00Z,"));

            assertFalse("Time3 entered when not!", write.toString().contains("1990-01-01T11:00:00Z,"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesIncomplete() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesIncomplete, tsIncompleteMultiDimensionalMultipleStations),write);
            write.flush();
            write.close();
            String fileName = "tsIncompleteMultiDimensionalMultipleStations.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesIncompleteTime() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesIncompleteWithTime, tsIncompleteMultiDimensionalMultipleStations),write);
            write.flush();
            write.close();
            String fileName = "tsIncompleteMultiDimensionalMultipleStationsWithTime.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTimeSeriesOrthogonalMultidimenstionalMultipleStations() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(tsOrthogonalMultidimenstionalMultipleStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriestOrth, tsOrthogonalMultidimenstionalMultipleStations),write);
            write.flush();
            write.close();
            String fileName = "tsOrthogonalMultidimenstionalMultipleStations.xml";
            fileWriter(base, fileName, write);
            // write as an example
            fileWriter(exampleOutputDir, "GetObservation-TimeSeries-om1.0.0.xml", write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiTimeSeriesProfileRequest() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesProfileRequestMulti, RaggedMultiConventions),write);
            write.flush();
            write.close();
            String fileName = "RaggedMultiConventionsMultiTime.xml";
            fileWriter(base, fileName, write);
            // write as an example
            fileWriter(exampleOutputDir, "GetObservation-TimeSeriesProfile-om1.0.0.xml", write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

            assertTrue("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.7"));
            assertTrue("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.9"));


            assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,6.7"));
            assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,7.0"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiTimeSeriesProfileRequestInvalidDates() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesProfileRequestMultiInvalidDates, RaggedMultiConventions),write);
            write.flush();
            write.close();
            String fileName = "RaggedMultiConventionsMultiTimeInvalidDates.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in outptu", write.toString().contains("Exception"));
            //dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

            assertFalse("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.7,0.5"));
            assertFalse("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.9,1.5"));


            assertFalse("data missing", write.toString().contains("1990-01-01T02:00:00Z,6.7,0.5"));
            assertFalse("data missing", write.toString().contains("1990-01-01T02:00:00Z,7.0,1.5"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testenhanceMultiRaggedDataset() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesProfileRequestSingle, RaggedMultiConventions),write);
            write.flush();
            write.close();
            String fileName = "RaggedMultiConventions.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

            assertTrue("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.7"));
            assertTrue("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.9"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiDimensionalSingleStations() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalSingleStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesProfileRequest2, MultiDimensionalSingleStations),write);
            write.flush();
            write.close();
            String fileName = "MultiDimensionalSingleStations.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiDimensionalSingleStationsTimeTestT2() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesTimeRequestT2, RaggedMultiConventions),write);
            write.flush();
            write.close();
            String fileName = "MultiDimensionalSingleStationsT2.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

            assertFalse("too much data", write.toString().contains("1990-01-01T00:00:00Z,6.7"));
            assertFalse("too much data", write.toString().contains("1990-01-01T00:00:00Z,6.9"));


            assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,6.7"));
            assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,7.0"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiDimensionalSingleStationsTimeTestT1() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesTimeRequestT1, RaggedMultiConventions),write);
            write.flush();
            write.close();
            String fileName = "MultiDimensionalSingleStationsT1.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

            assertTrue("too much data", write.toString().contains("1990-01-01T00:00:00Z,6.7"));
            assertTrue("too much data", write.toString().contains("1990-01-01T00:00:00Z,6.9"));


            assertFalse("data missing", write.toString().contains("1990-01-01T02:00:00Z,6.7"));
            assertFalse("data missing", write.toString().contains("1990-01-01T02:00:00Z,7.0"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiDimensionalMultiStations() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesProfileRequest2, MultiDimensionalMultiStations),write);
            write.flush();
            write.close();
            String fileName = "MultiDimensionalMultiStations.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiDimensionalMultiStationsStation2() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesProfileRequest3, MultiDimensionalMultiStations),write);
            write.flush();
            write.close();
            String fileName = "MultiDimensionalMultiStations2.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testMultiDimensionalMultiStation() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, timeSeriesProfileRequestMultiStation, MultiDimensionalMultiStations),write);
            write.flush();
            write.close();
            String fileName = "MultiDimensionalMultiStationRequest.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));

            assertTrue("Station 1 T0A0", write.toString().contains("1990-01-01T00:00:00Z,0.0"));
            assertTrue("Station 1 T0A1", write.toString().contains("1990-01-01T00:00:00Z,0.7"));
            assertTrue("Station 1 T0A2", write.toString().contains("1990-01-01T00:00:00Z,1.6"));
            //
            assertTrue("Station 1 T3A0", write.toString().contains("1990-01-01T03:00:00Z,9.0"));
            assertTrue("Station 1 T3A1", write.toString().contains("1990-01-01T03:00:00Z,9.3"));
            assertTrue("Station 1 T3A2", write.toString().contains("1990-01-01T03:00:00Z,11.4"));
            assertTrue("Station 1 T3A3", write.toString().contains("1990-01-01T03:00:00Z,11.8"));

            assertTrue("Station 2 T0A0", write.toString().contains("1990-01-01T04:00:00Z,12.0"));
            assertTrue("Station 2 T0A1", write.toString().contains("1990-01-01T04:00:00Z,13.5"));
            assertTrue("Station 2 T0A2", write.toString().contains("1990-01-01T04:00:00Z,14.0"));
            assertTrue("Station 2 T3A0", write.toString().contains("1990-01-01T04:00:00Z,14.9"));
            //
            assertTrue("Station 2 T3A1", write.toString().contains("1990-01-01T05:00:00Z,15.0"));
            assertTrue("Station 2 T3A2", write.toString().contains("1990-01-01T05:00:00Z,16.3"));
            assertTrue("Station 2 T3A3", write.toString().contains("1990-01-01T05:00:00Z,17.8"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime3() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, profileRequestMultiTime3, ContiguousRaggedMultipleProfiles), write);
            write.flush();
            write.close();
            String fileName = "ContiguousRaggedMultipleProfilesMultiTime3.xml";
            fileWriter(base, fileName, write);
            // write as an example
            fileWriter(exampleOutputDir, "GetObservation-Profile-om1.0.0.xml", write);
            //dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertFalse(write.toString().contains("Exception"));
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
            assertTrue("data missing - feature of interest", write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_3\"/>"));
            assertFalse("bad data included - time stamp", write.toString().contains("1990-01-01T01:00:00Z,"));
            assertFalse("bad data included - time stamp", write.toString().contains("1990-01-01T02:00:00Z,"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, profileRequestMultiTime, ContiguousRaggedMultipleProfiles),write);
            write.flush();
            write.close();
            assertFalse(write.toString().contains("Exception"));
            String fileName = "ContiguousRaggedMultipleProfilesMultiTime.xml";
            fileWriter(base, fileName, write);
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
            assertFalse("data missing", write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_0\"/>"));
            assertTrue("data missing", write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_1\"/>"));
            assertTrue("data missing", write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_2\"/>"));
            assertTrue("data missing", write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_3\"/>"));
            assertTrue("data missing", write.toString().contains("1990-01-01T01:00:00Z,"));
            assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime2() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, profileRequestMultiTime2, ContiguousRaggedMultipleProfiles),write);
            write.flush();
            write.close();
            String fileName = "ContiguousRaggedMultipleProfilesMultiTime2.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
            assertFalse("data missing", write.toString().contains("1990-01-01T00:00:00Z,"));
            assertTrue("data missing", write.toString().contains("1990-01-01T01:00:00Z,"));
            assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testContiguousRaggedMultipleProfiles() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, profileRequest, ContiguousRaggedMultipleProfiles),write);
            write.flush();
            write.close();
            String fileName = "ContiguousRaggedMultipleProfiles.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testIncompleteMultiDimensionalMultipleProfiles() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(IncompleteMultiDimensionalMultipleProfiles);

            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, profileRequest, IncompleteMultiDimensionalMultipleProfiles),write);
            write.flush();
            write.close();
            String fileName = "IncompleteMultiDimensionalMultipleProfiles.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testIndexedRaggedMultipleProfiles() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(IndexedRaggedMultipleProfiles);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, profileRequestIndexed, IndexedRaggedMultipleProfiles),write);
            write.flush();
            write.close();
            String fileName = "IndexedRaggedMultipleProfiles.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testOrthogonalMultiDimensionalMultipleProfiles() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalMultiDimensionalMultipleProfiles);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, profileRequest, OrthogonalMultiDimensionalMultipleProfiles),write);
            write.flush();
            write.close();
            String fileName = "OrthogonalMultiDimensionalMultipleProfiles.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testOrthogonalSingleDimensionalSingleProfile() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalSingleDimensionalSingleProfile);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, profileRequest, OrthogonalSingleDimensionalSingleProfile),write);
            write.flush();
            write.close();
            String fileName = "OrthogonalSingleDimensionalSingleProfile.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
            assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTrajectoryContiguousRaggedMultipleTrajectories() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(trajectoryContiguousRaggedMultipleTrajectories);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, trajectoryContiguousRequest1, trajectoryContiguousRaggedMultipleTrajectories),write);
            write.flush();
            write.close();
            String fileName = "trajectoryContiguousRaggedMultipleTrajectories.xml";
            fileWriter(base, fileName, write);
            // write as an example
            fileWriter(exampleOutputDir, "GetObservation-Trajectory-om1.0.0.xml", write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTrajectoryIncompleteMultidimensionalMultipleTrajectories() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(trajectoryIncompleteMultidimensionalMultipleTrajectories);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, trajectoryIncompleteRequest1, trajectoryIncompleteMultidimensionalMultipleTrajectories),write);
            write.flush();
            write.close();
            String fileName = "trajectoryIncompleteMultidimensionalMultipleTrajectories_request1.xml";
            fileWriter(base, fileName, write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testTrajectoryProfileMultidimensionalMultipleTrajectories1() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(trajectoryProfileMultidimensionalMultipleTrajectories);
            SOSParser md = new SOSParser();
            Writer write = new CharArrayWriter();
            writeOutput(md.enhance(dataset, sectionRequest1, trajectoryProfileMultidimensionalMultipleTrajectories),write);
            write.flush();
            write.close();
            String fileName = "trajectoryProfileMultidimensionalMultipleTrajectories_request1.xml";
            fileWriter(base, fileName, write);
            // write as an example
            fileWriter(exampleOutputDir, "GetObservation-Section-om1.0.0.xml", write);
            assertFalse("exception in output", write.toString().contains("Exception"));
            dataAvailableInOutputFile(write);
            //check depth was entered auto
//            assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
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
