/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncSOS.getObs;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import java.io.*;
import java.util.HashMap;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import com.asascience.ncsos.util.XMLDomUtils;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author Abird
 */
public class SOSgetObsTest {
    
    private static final String datasetFolder = "resources/datasets/";

    //imeds data
    private static String imeds1 = "resources/datasets/sura/Hsig_UNDKennedy_IKE_VIMS_3D_WAVEONLY.nc";
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
//    private static String RaggedSingleConventions = "resources/datasets/timeSeriesProfile-Ragged-SingleStation-H.5.3/timeSeriesProfile-Ragged-SingleStation-H.5.3.nc";
    private static String RaggedMultiConventions = "resources/datasets/timeSeriesProfile-Ragged-MultipeStations-H.5.3/timeSeriesProfile-Ragged-MultipeStations-H.5.3.nc";
//    private static String OrthogonalMultidimensionalMultiStations = "resources/datasets/timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1.nc";
    private static String MultiDimensionalSingleStations = "resources/datasets/timeSeriesProfile-Multidimensional-SingleStation-H.5.2/timeSeriesProfile-Multidimensional-SingleStation-H.5.2.nc";
    private static String MultiDimensionalMultiStations = "resources/datasets/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";
    //point
//    private static String cfPoint = "resources/datasets/point-H.1/point-H.1.nc";
// profile
    private static String ContiguousRaggedMultipleProfiles = "resources/datasets/profile-Contiguous-Ragged-MultipleProfiles-H.3.4/profile-Contiguous-Ragged-MultipleProfiles-H.3.4.nc";
    private static String IncompleteMultiDimensionalMultipleProfiles = "resources/datasets/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2.nc";
    private static String IndexedRaggedMultipleProfiles = "resources/datasets/profile-Indexed-Ragged-MultipleProfiles-H.3.5/profile-Indexed-Ragged-MultipleProfiles-H.3.5.nc";
    private static String OrthogonalMultiDimensionalMultipleProfiles = "resources/datasets/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1.nc";
    private static String OrthogonalSingleDimensionalSingleProfile = "resources/datasets/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3.nc";
    // satellite sst data
//    private static String sst1 = datasetFolder + "satellite-sst/20120617.1508.d7.composite.nc";
//    private static String sst2 = datasetFolder + "satellite-sst/20120617..d7.composite.nc";
//    private static String sst3 = datasetFolder + "satellite-sst/20120617..d7.composite.nc";
    public static String base = null;
    
    public static final String baseRequest = "responseformat=oostethysswe&request=GetObservation&version=1.0.0&service=sos";
    
    public static final String profileRequest = baseRequest + "&observedProperty=temperature,humidity&offering=0&eventTime=1990-01-01T00:00:00Z";
    public static final String profileRequestIndexed = baseRequest + "&observedProperty=temperature,humidity&offering=1&eventTime=1990-01-01T01:00:00Z/1990-01-01T04:00:00Z";
    public static final String profileRequestMultiTime = baseRequest + "&observedProperty=temperature,humidity&offering=1,2,3&eventTime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";
    public static final String profileRequestMultiTime2 = baseRequest + "&observedProperty=temperature,humidity&offering=1,2&eventTime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";
    public static final String profileRequestMultiTime3 = baseRequest + "&service=sos&observedProperty=temperature,humidity&offering=3&eventTime=1990-01-01T00:00:00Z/1990-01-01T03:00:00Z";

    private void dataAvailableInOutputFile(Writer write) {
        assertTrue("error no values", write.toString().contains("<swe:values>"));
        assertFalse("error no values: error where data should be", write.toString().contains("<swe:values>ERROR!</swe:values>"));
    }

    private void fileWriter(String base, String fileName, Writer write) throws IOException {
        File file = new File(base + fileName);
        Writer output = new BufferedWriter(new FileWriter(file));
        output.write(write.toString());
        output.close();
        System.out.println("Your file has been written");
    }
    
    private void writeOutput(HashMap<String, Object> outMap, Writer write) {
        SOSOutputFormatter output = (SOSOutputFormatter)outMap.get("outputHandler");
        assertNotNull("got null output", output);
        output.writeOutput(write);
    }
    
    @BeforeClass
    public static void SetupEnviron() throws FileNotFoundException {
        // not really a test, just used to set up the various string values
        if (base != null) {
            // exit early if the environ is already set
            return;
        }
        String container = "getObs";
        InputStream templateInputStream = null;
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            base = XMLDomUtils.getNodeValue(configDoc, container, "outputBase");
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
    }
    
    private void spaceBetweenTests() {
        System.out.println("\n");
    }
    
    //***********************************************
    //IMEDS FILES
    public static final String imeds1Req = baseRequest + "&observedProperty=hs&offering=UNDKennedy_S,UNDKennedy_X,UNDKennedy_Z&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds1() throws IOException {
        System.out.println("----IMEDS1------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds1Req, imeds1),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds1.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:UNDKennedy_X\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:UNDKennedy_S\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:UNDKennedy_Z\">"));

        System.out.println("----------end-----------");
    }
    
    public static final String imeds4Req = baseRequest + "&observedProperty=hs&offering=USACE-CHL_2410508B,USACE-CHL_2410513B,USACE-CHL_2410510B&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds4() throws IOException {
        System.out.println("----IMEDS4------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds4);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds4Req, imeds4),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds4.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:USACE-CHL_2410513B\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:USACE-CHL_2410508B\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:USACE-CHL_2410510B\">"));

        System.out.println("----------end-----------");
    }
    public static final String imeds5Req = baseRequest + "&observedProperty=hwm&offering=TCOON_87747701,TCOON_87705701,TCOON_87705201,TCOON_87704751,TCOON_87708221&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds5() throws IOException {
        System.out.println("----IMEDS5------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds5);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds5Req, imeds5),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds5.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:TCOON_87747701\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:TCOON_87705701\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:TCOON_87704751\">"));

        System.out.println("----------end-----------");
    }
    public static final String imeds6Req = baseRequest + "&observedProperty=tp&offering=CSI_15,CSI_06,CSI_09&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds6() throws IOException {
        System.out.println("----IMEDS6------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds6);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds6Req, imeds6),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds6.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_15\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_06\">"));

        System.out.println("----------end-----------");
    }
    public static final String imeds7Req = baseRequest + "&observedProperty=tp&offering=CSI_06,CSI_09,NDBC_42020,NDBC_42019,USACE-CHL_2410513B,NDBC_42059&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds7() throws IOException {
        System.out.println("----IMEDS7------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds7);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds7Req, imeds7),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds7.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:NDBC_42020\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:USACE-CHL_2410513B\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:NDBC_42059\">"));

        System.out.println("----------end-----------");
    }
    public static final String imeds8Req = baseRequest + "&observedProperty=watlev&offering=CRMS_CS20-106,CRMS_CS20-15R,CRMS_DCPBS03,CRMS_DCPBS04&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds8() throws IOException {
        System.out.println("----IMEDS8------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds8);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds8Req, imeds8),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds8.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBS04\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBS03\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CS20-15R\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CS20-106\">"));

        System.out.println("----------end-----------");
    }
    public static final String imeds9Req = baseRequest + "&observedProperty=watlev&offering=CRMS_CS20-106,CRMS_CS20-15R,CRMS_DCPBS03,CRMS_DCPBS04&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds9() throws IOException {
        System.out.println("----IMEDS9------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds9);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds9Req, imeds9),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds9.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBS04\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBS03\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CS20-15R\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CS20-106\">"));

        System.out.println("----------end-----------");
    }
    public static final String imeds10Req = baseRequest + "&observedProperty=watlev&offering=CRMS_CRMS0161-H01,CRMS_DCPBA07,CRMS_CRMS0174-H01,&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds10() throws IOException {
        System.out.println("----IMEDS10------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds10);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds10Req, imeds10),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds10.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0161-H01\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0174-H01\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBA07\">"));

        System.out.println("----------end-----------");
    }
    public static final String imeds11Req = baseRequest + "&observedProperty=watlev&offering=CRMS_CRMS0161-H01,CRMS_DCPBA07,CRMS_CRMS0174-H01,&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds11() throws IOException {
        System.out.println("----IMEDS11------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds11);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds11Req, imeds11),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds11.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0161-H01\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0174-H01\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBA07\">"));

        System.out.println("----------end-----------");
    }
    public static final String imeds12Req = baseRequest + "&observedProperty=watlev&offering=CSI_03,CSI_06,CSI_09,CSI_15&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds12() throws IOException {
        System.out.println("----IMEDS12------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds12);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds12Req, imeds12),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds12.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_03\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_06\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_15\">"));

        System.out.println("----------end-----------");
    }
    
    public static final String imeds13Req = baseRequest + "&observedProperty=watlev&offering=CSI_06,CSI_09&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds13() throws IOException {
        
        System.out.println("----IMEDS13------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds13Req, imeds13),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds13.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_06\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));

        System.out.println("----------end-----------");
    }

    @Test
    public void testenhanceImeds14() throws IOException {

        System.out.println("----IMEDS14------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds14);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds13Req, imeds14),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds14.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_06\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CSI_09\">"));

        System.out.println("----------end-----------");
    }
    public static final String imeds15Req = baseRequest + "&observedProperty=watlev&offering=NOAA_8727235,NOAA_8729501&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds15() throws IOException {

        System.out.println("----IMEDS15------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds15);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, imeds15Req, imeds15),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds15.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:NOAA_8727235\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:NOAA_8729501\">"));

        System.out.println("----------end-----------");
    }   
    //**********************************
//TIMESERIES TEST
    public static final String timeSeriestOrth = baseRequest + "&observedProperty=alt&offering=Station-1&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesIncomplete = baseRequest + "&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesMulti = baseRequest + "&observedProperty=temperature,alt&offering=Station-9";
    public static final String timeSeriesIncompleteWithTime = baseRequest + "&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesIncompleteMulti = baseRequest + "&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z/1990-01-01T10:00:00Z";
    public static final String timeSeriesIncompleteMultiInvalid = baseRequest + "&observedProperty=temperature&offering=Station-9&eventtime=1990-02-01T00:00:00Z/1990-05-01T10:00:00Z";
    public static final String timeSeriesIncompleteMultiStation = baseRequest + "&observedProperty=temperature&offering=Station-9,Station-8&eventtime=1990-01-01T00:00:00Z/1990-01-01T8:00:00Z";
    public static final String timeSeriesIncompleteMultiStationx3 = baseRequest + "&observedProperty=temperature&offering=urn:tds:station.sos:Station-9,urn:tds:station.sos:Station-8,urn:tds:station.sos:Station-7&eventtime=1990-01-01T00:00:00Z/1990-01-01T8:00:00Z";

    @Test
    public void testMultiTimeCreateDataStruct3Stations() throws IOException {

        System.out.println("----tsData------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesIncompleteMultiStationx3, tsIncompleteMultiDimensionalMultipleStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsDatax3.xml";
        fileWriter(base, fileName, write);
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
        System.out.println("----------end-----------");
    }

    @Test
    public void testMultiTimeCreateDataStruct() throws IOException {

        System.out.println("----tsData------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesIncompleteMultiStation, tsIncompleteMultiDimensionalMultipleStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsData.xml";
        fileWriter(base, fileName, write);
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
        System.out.println("----------end-----------");
    }

    @Test
    public void testTimeSeriesIncompleteDataMultiTimeInvalid() throws IOException {
        System.out.println("----tsIncompleteMultiDimensionalMultipleStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesIncompleteMultiInvalid, tsIncompleteMultiDimensionalMultipleStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsIncompleteMultiDimensionalMultipleStationsMultiTimeInvalid.xml";
        fileWriter(base, fileName, write);
        //dataAvailableInOutputFile(write);

        assertFalse("Time1 entered", write.toString().contains("1990-01-01T00:00:00Z,"));
        assertFalse("Time2 entered", write.toString().contains("1990-01-01T10:00:00Z,"));

        assertFalse("Time3 entered", write.toString().contains("1990-01-01T11:00:00Z,"));

        System.out.println("----------end-----------");
    }

    @Test
    public void testTimeSeriesIncompleteDataMultiTime() throws IOException {
        System.out.println("----tsIncompleteMultiDimensionalMultipleStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesIncompleteMulti, tsIncompleteMultiDimensionalMultipleStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsIncompleteMultiDimensionalMultipleStationsMultiTime.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("Time1 not entered", write.toString().contains("1990-01-01T00:00:00Z,"));
        assertTrue("Time2 not entered", write.toString().contains("1990-01-01T10:00:00Z,"));

        assertFalse("Time3 entered when not!", write.toString().contains("1990-01-01T11:00:00Z,"));

        System.out.println("----------end-----------");
    }

    @Test
    public void testTimeSeriesIncomplete() throws IOException {
        System.out.println("----tsIncompleteMultiDimensionalMultipleStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesIncomplete, tsIncompleteMultiDimensionalMultipleStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsIncompleteMultiDimensionalMultipleStations.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        //assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testTimeSeriesIncompleteTime() throws IOException {
        System.out.println("----tsIncompleteMultiDimensionalMultipleStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesIncompleteWithTime, tsIncompleteMultiDimensionalMultipleStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsIncompleteMultiDimensionalMultipleStationsWithTime.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        //assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testTimeSeriesOrthogonalMultidimenstionalMultipleStations() throws IOException {
        System.out.println("----tsOrthogonalMultidimenstionalMultipleStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsOrthogonalMultidimenstionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriestOrth, tsOrthogonalMultidimenstionalMultipleStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsOrthogonalMultidimenstionalMultipleStations.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        //assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }
    //**********************************
//TIMESERIESPROFILE TEST
    public static final String timeSeriesProfileRequestSingle = baseRequest + "&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesProfileRequestMulti = baseRequest + "&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";
    public static final String timeSeriesProfileRequestMultiStation = baseRequest + "&observedProperty=temperature&offering=Station1,Station2&eventTime=1990-01-01T00:00:00Z/1990-01-01T05:00:00Z";

    
    @Test
    public void testMultiTimeSeriesProfileRequest() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesProfileRequestMulti, RaggedMultiConventions),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventionsMultiTime.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

        assertTrue("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.7"));
        assertTrue("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.9"));


        assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,6.7"));
        assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,7.0"));

        System.out.println("----------end-----------");
    }
    public static final String timeSeriesProfileRequestMultiInvalidDates = baseRequest + "&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-04-01T00:00:00Z/1990-08-01T02:00:00Z";

    @Test
    public void testMultiTimeSeriesProfileRequestInvalidDates() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesProfileRequestMultiInvalidDates, RaggedMultiConventions),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventionsMultiTimeInvalidDates.xml";
        fileWriter(base, fileName, write);
        //dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

        assertFalse("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.7,0.5"));
        assertFalse("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.9,1.5"));


        assertFalse("data missing", write.toString().contains("1990-01-01T02:00:00Z,6.7,0.5"));
        assertFalse("data missing", write.toString().contains("1990-01-01T02:00:00Z,7.0,1.5"));

        System.out.println("----------end-----------");
    }

    @Test
    public void testenhanceMultiRaggedDataset() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesProfileRequestSingle, RaggedMultiConventions),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventions.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

        assertTrue("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.7"));
        assertTrue("data missing", write.toString().contains("1990-01-01T00:00:00Z,6.9"));

        System.out.println("----------end-----------");
    }
    public static final String timeSeriesProfileRequest2 = baseRequest + "&observedProperty=temperature&offering=Station1&eventTime=1990-01-01T00:00:00Z";

    @Test
    public void testMultiDimensionalSingleStations() throws IOException {
        System.out.println("----MultiDimensionalSingleStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalSingleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesProfileRequest2, MultiDimensionalSingleStations),write);
        write.flush();
        write.close();
        String fileName = "MultiDimensionalSingleStations.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        assertFalse(write.toString().contains("Exception"));
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }
    public static final String timeSeriesTimeRequestT2 = baseRequest + "&observedProperty=temperature&offering=Station1&eventtime=1990-01-01T02:00:00Z";
    public static final String timeSeriesTimeRequestT1 = baseRequest + "&observedProperty=temperature&offering=Station1&eventtime=1990-01-01T00:00:00Z";

    @Test
    public void testMultiDimensionalSingleStationsTimeTestT2() throws IOException {
        System.out.println("----MultiDimensionalSingleStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesTimeRequestT2, RaggedMultiConventions),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalSingleStationsT2.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

        assertFalse("too much data", write.toString().contains("1990-01-01T00:00:00Z,6.7"));
        assertFalse("too much data", write.toString().contains("1990-01-01T00:00:00Z,6.9"));


        assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,6.7"));
        assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,7.0"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testMultiDimensionalSingleStationsTimeTestT1() throws IOException {
        System.out.println("----MultiDimensionalSingleStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesTimeRequestT1, RaggedMultiConventions),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalSingleStationsT1.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));

        assertTrue("too much data", write.toString().contains("1990-01-01T00:00:00Z,6.7"));
        assertTrue("too much data", write.toString().contains("1990-01-01T00:00:00Z,6.9"));


        assertFalse("data missing", write.toString().contains("1990-01-01T02:00:00Z,6.7"));
        assertFalse("data missing", write.toString().contains("1990-01-01T02:00:00Z,7.0"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testMultiDimensionalMultiStations() throws IOException {
        System.out.println("----MultiDimensionalMultiStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesProfileRequest2, MultiDimensionalMultiStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStations.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }
    public static final String timeSeriesProfileRequest3 = baseRequest + "&observedProperty=temperature&offering=Station2&eventTime=1990-01-01T04:00:00Z";

    @Test
    public void testMultiDimensionalMultiStationsStation2() throws IOException {
        System.out.println("----MultiDimensionalMultiStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesProfileRequest3, MultiDimensionalMultiStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStations2.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testMultiDimensionalMultiStation() throws IOException {
        System.out.println("----MultiDimensionalMultiStations------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, timeSeriesProfileRequestMultiStation, MultiDimensionalMultiStations),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStationRequest.xml";
        fileWriter(base, fileName, write);
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
        
        System.out.println("----------end-----------");
    }
    //**********************************
    //PROFILE TEST

    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime3() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, profileRequestMultiTime3, ContiguousRaggedMultipleProfiles), write);
        write.flush();
        write.close();
        String fileName = "ContiguousRaggedMultipleProfilesMultiTime3.xml";
        fileWriter(base, fileName, write);
        //dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertFalse(write.toString().contains("Exception"));
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        assertTrue("data missing - feature of interest", write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_3\"/>"));
        assertFalse("bad data included - time stamp", write.toString().contains("1990-01-01T01:00:00Z,"));
        assertFalse("bad data included - time stamp", write.toString().contains("1990-01-01T02:00:00Z,"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        
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

        System.out.println("----------end-----------");
    }

    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime2() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, profileRequestMultiTime2, ContiguousRaggedMultipleProfiles),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "ContiguousRaggedMultipleProfilesMultiTime2.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        assertFalse("data missing", write.toString().contains("1990-01-01T00:00:00Z,"));
        assertTrue("data missing", write.toString().contains("1990-01-01T01:00:00Z,"));
        assertTrue("data missing", write.toString().contains("1990-01-01T02:00:00Z,"));

        System.out.println("----------end-----------");
    }

    @Test
    public void testContiguousRaggedMultipleProfiles() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, profileRequest, ContiguousRaggedMultipleProfiles),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "ContiguousRaggedMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testIncompleteMultiDimensionalMultipleProfiles() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(IncompleteMultiDimensionalMultipleProfiles);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, profileRequest, IncompleteMultiDimensionalMultipleProfiles),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "IncompleteMultiDimensionalMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testIndexedRaggedMultipleProfiles() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(IndexedRaggedMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, profileRequestIndexed, IndexedRaggedMultipleProfiles),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "IndexedRaggedMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testOrthogonalMultiDimensionalMultipleProfiles() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalMultiDimensionalMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, profileRequest, OrthogonalMultiDimensionalMultipleProfiles),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "OrthogonalMultiDimensionalMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testOrthogonalSingleDimensionalSingleProfile() throws IOException {
        spaceBetweenTests();
        System.out.println("----OrthogonalSingleDimensionalSingleProfile------");
        
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalSingleDimensionalSingleProfile);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        writeOutput(md.enhance(dataset, profileRequest, OrthogonalSingleDimensionalSingleProfile),write);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "OrthogonalSingleDimensionalSingleProfile.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        System.out.println("----------end-----------");
    }
//**********************************
}
