/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.getObs;

import java.lang.String;
import java.util.Map;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.Writer;
import java.io.CharArrayWriter;
import thredds.server.sos.service.SOSParser;
import ucar.nc2.dataset.NetcdfDataset;
import java.io.IOException;
import java.util.HashMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import thredds.server.sos.util.XMLDomUtils;
import static org.junit.Assert.*;

/**
 *
 * @author Abird
 */
public class SOSgetObsTest {

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
    public static final String base = "C:/Users/scowan/Projects/maven/ncSOS/src/test/java/com/asascience/ncSOS/getObs/output/";

    private void dataAvailableInOutputFile(Writer write) {
        assertTrue("error no values", write.toString().contains("<swe:values>"));
        assertFalse("error no values: error where data should be", write.toString().contains("<swe:values>ERROR!</swe:values>"));
    }

    private void fileWriter(String base, String fileName, Writer write) throws IOException {
        Writer output = null;
        File file = new File(base + fileName);
        output = new BufferedWriter(new FileWriter(file));
        output.write(write.toString());
        output.close();
        System.out.println("Your file has been written");
    }
    
    //***********************************************
    //IMEDS FILES
    public static final String imeds1Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=hs&offering=UNDKennedy_S,UNDKennedy_X,UNDKennedy_Z&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds1() throws IOException {
        System.out.println("----IMEDS1------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds1);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds1Req, imeds1);
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
    public static final String imeds2Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=watlev&offering=Site-79-2&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    /* test removed until it can be fixed -- Sean
    @Test
    public void testenhanceImeds2() throws IOException {
//        fail("removed - file is not properly read by netcdf");
        System.out.println("----IMEDS2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds2);
//        assertNotNull(dataset);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds2Req, imeds2);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds2.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        System.out.println("----------end-----------");
    }
    * 
    */
    public static final String imeds3Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=watlev&offering=Site-79-2&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    /* test removed until it can fixed -- Sean
    @Test
    public void testenhanceImeds3() throws IOException {
//        fail("removed - files is not properly read by netcdf");
        System.out.println("----IMEDS3------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds3);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds3Req, imeds3);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds3.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        System.out.println("----------end-----------");
    }
    * 
    */
    public static final String imeds4Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=hs&offering=USACE-CHL_2410508B,USACE-CHL_2410513B,USACE-CHL_2410510B&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds4() throws IOException {
        System.out.println("----IMEDS4------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds4);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds4Req, imeds4);
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
    public static final String imeds5Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=hwm&offering=TCOON_87747701,TCOON_87705701,TCOON_87705201,TCOON_87704751,TCOON_87708221&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds5() throws IOException {
        System.out.println("----IMEDS5------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds5);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds5Req, imeds5);
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
    public static final String imeds6Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=tp&offering=CSI_15,CSI_06,CSI_09&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds6() throws IOException {
        System.out.println("----IMEDS6------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds6);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds6Req, imeds6);
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
    public static final String imeds7Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=tp&offering=CSI_06,CSI_09,NDBC_42020,NDBC_42019,USACE-CHL_2410513B,NDBC_42059&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds7() throws IOException {
        System.out.println("----IMEDS7------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds7);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds7Req, imeds7);
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
    public static final String imeds8Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=watlev&offering=CRMS_CS20-106,CRMS_CS20-15R,CRMS_DCPBS03,CRMS_DCPBS04&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds8() throws IOException {
        System.out.println("----IMEDS8------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds8);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds8Req, imeds8);
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
    public static final String imeds9Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=watlev&offering=CRMS_CS20-106,CRMS_CS20-15R,CRMS_DCPBS03,CRMS_DCPBS04&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds9() throws IOException {
        System.out.println("----IMEDS9------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds9);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds9Req, imeds9);
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
    public static final String imeds10Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=watlev&offering=CRMS_CRMS0161-H01,CRMS_DCPBA07,CRMS_CRMS0174-H01,&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds10() throws IOException {
        System.out.println("----IMEDS10------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds10);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds10Req, imeds10);
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
    public static final String imeds11Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=watlev&offering=CRMS_CRMS0161-H01,CRMS_DCPBA07,CRMS_CRMS0174-H01,&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds11() throws IOException {
        System.out.println("----IMEDS11------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds11);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds11Req, imeds11);
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
    public static final String imeds12Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=watlev&offering=CRMS_CRMS0161-H01,CRMS_DCPBA07,CRMS_CRMS0174-H01,&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    /* test removed until it can be fixed -- Sean
    @Test
    public void testenhanceImeds12() throws IOException {
//        fail("removed - Data File Does Not work");
        System.out.println("----IMEDS12------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds12);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds12Req, imeds12);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imeds12.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);

        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0161-H01\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_CRMS0174-H01\">"));
        assertTrue("station", write.toString().contains("srsName=\"urn:tds:station.sos:CRMS_DCPBA07\">"));

        System.out.println("----------end-----------");
    }
    * 
    */
    public static final String imeds13Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=watlev&offering=CSI_06,CSI_09&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds13() throws IOException {

        System.out.println("----IMEDS13------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds13);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds13Req, imeds13);
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
        md.enhance(dataset, write, imeds13Req, imeds14);
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
    public static final String imeds15Req = "request=GetObservation&version=1.0.0&service=sos&observedProperty=watlev&offering=NOAA_8727235,NOAA_8729501&eventtime=1990-01-01T00:00:00Z/2009-01-01T00:00:00Z";

    @Test
    public void testenhanceImeds15() throws IOException {

        System.out.println("----IMEDS15------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imeds15);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, imeds15Req, imeds15);
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
    public static final String timeSeriestOrth = "request=GetObservation&version=1.0.0&service=sos&observedProperty=alt&offering=Station-1&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesIncomplete = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesMulti = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,alt&offering=Station-9";
    public static final String timeSeriesIncompleteWithTime = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesIncompleteMulti = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z/1990-01-01T10:00:00Z";
    public static final String timeSeriesIncompleteMultiInvalid = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9&eventtime=1990-02-01T00:00:00Z/1990-05-01T10:00:00Z";
    public static final String timeSeriesIncompleteMultiStation = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9,Station-8&eventtime=1990-01-01T00:00:00Z/1990-01-01T8:00:00Z";
    public static final String timeSeriesIncompleteMultiStationx3 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=urn:tds:station.sos:Station-9,urn:tds:station.sos:Station-8,urn:tds:station.sos:Station-7&eventtime=1990-01-01T00:00:00Z/1990-01-01T8:00:00Z";

    @Test
    public void testMultiTimeCreateDataStruct3Stations() throws IOException {

        System.out.println("----tsData------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesIncompleteMultiStationx3, tsIncompleteMultiDimensionalMultipleStations);
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
        md.enhance(dataset, write, timeSeriesIncompleteMultiStation, tsIncompleteMultiDimensionalMultipleStations);
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
        md.enhance(dataset, write, timeSeriesIncompleteMultiInvalid, tsIncompleteMultiDimensionalMultipleStations);
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
        md.enhance(dataset, write, timeSeriesIncompleteMulti, tsIncompleteMultiDimensionalMultipleStations);
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
        md.enhance(dataset, write, timeSeriesIncomplete, tsIncompleteMultiDimensionalMultipleStations);
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
        md.enhance(dataset, write, timeSeriesIncompleteWithTime, tsIncompleteMultiDimensionalMultipleStations);
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
        md.enhance(dataset, write, timeSeriestOrth, tsOrthogonalMultidimenstionalMultipleStations);
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
    public static final String timeSeriesProfileRequestSingle = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesProfileRequestMulti = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";
    public static final String timeSeriesProfileRequestMultiStation = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station1,Station2&eventTime=1990-01-01T00:00:00Z/1990-01-01T05:00:00Z";

    /* test removed until it can be fixed -- Sean
    @Test
    public void testenhanceSingleRaggedDataset() throws IOException {
//        fail("removed - issue with time series profile netcdf file - no temperature");

        System.out.println("----RaggedSingleConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedSingleConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequestSingle, RaggedSingleConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedSingleConventions.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"height\">"));
        System.out.println("----------end-----------");
    }
    * 
    */

    @Test
    public void testMultiTimeSeriesProfileRequest() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequestMulti, RaggedMultiConventions);
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
    public static final String timeSeriesProfileRequestMultiInvalidDates = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-04-01T00:00:00Z/1990-08-01T02:00:00Z";

    @Test
    public void testMultiTimeSeriesProfileRequestInvalidDates() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequestMultiInvalidDates, RaggedMultiConventions);
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
        md.enhance(dataset, write, timeSeriesProfileRequestSingle, RaggedMultiConventions);
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
    public static final String timeSeriesProfileRequest2 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station1&eventTime=1990-01-01T00:00:00Z";

    @Test
    public void testMultiDimensionalSingleStations() throws IOException {
        System.out.println("----MultiDimensionalSingleStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalSingleStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequest2, MultiDimensionalSingleStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalSingleStations.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }
    public static final String timeSeriesTimeRequestT2 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station1&eventtime=1990-01-01T02:00:00Z";
    public static final String timeSeriesTimeRequestT1 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station1&eventtime=1990-01-01T00:00:00Z";

    @Test
    public void testMultiDimensionalSingleStationsTimeTestT2() throws IOException {
        System.out.println("----MultiDimensionalSingleStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesTimeRequestT2, RaggedMultiConventions);
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
        md.enhance(dataset, write, timeSeriesTimeRequestT1, RaggedMultiConventions);
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
        md.enhance(dataset, write, timeSeriesProfileRequest2, MultiDimensionalMultiStations);
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
    public static final String timeSeriesProfileRequest3 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station2&eventTime=1990-01-01T04:00:00Z";

    @Test
    public void testMultiDimensionalMultiStationsStation2() throws IOException {
        System.out.println("----MultiDimensionalMultiStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequest3, MultiDimensionalMultiStations);
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
        md.enhance(dataset, write, timeSeriesProfileRequestMultiStation, MultiDimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStationRequest.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"alt\">"));

        assertTrue("Station 1 T0A0", write.toString().contains("1990-01-01T00:00:00Z,0.0"));
        assertTrue("Station 1 T0A1", write.toString().contains("1990-01-01T00:00:00Z,0.7000000000000001"));
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
    public static final String profileRequest = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,humidity&offering=0&eventTime=1990-01-01T00:00:00Z";
    public static final String profileRequestIndexed = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,humidity&offering=1&eventTime=1990-01-01T01:00:00Z/1990-01-01T04:00:00Z";
    public static final String profileRequestMultiTime = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,humidity&offering=1,2,3&eventTime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";
    public static final String profileRequestMultiTime2 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,humidity&offering=1,2&eventTime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";
    public static final String profileRequestMultiTime3 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,humidity&offering=3&eventTime=1990-01-01T00:00:00Z/1990-01-01T02:00:00Z";

    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime3() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequestMultiTime3, ContiguousRaggedMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "ContiguousRaggedMultipleProfilesMultiTime3.xml";
        fileWriter(base, fileName, write);
        //dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added", write.toString().contains("<swe:field name=\"z\">"));
        assertTrue("data missing", write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_3\"/>"));
        assertFalse("data missing", write.toString().contains("1990-01-01T01:00:00Z,"));
        assertFalse("data missing", write.toString().contains("1990-01-01T02:00:00Z,"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequestMultiTime, ContiguousRaggedMultipleProfiles);
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
        md.enhance(dataset, write, profileRequestMultiTime2, ContiguousRaggedMultipleProfiles);
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
        md.enhance(dataset, write, profileRequest, ContiguousRaggedMultipleProfiles);
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

    private void spaceBetweenTests() {
        System.out.println("\n");
    }

    @Test
    public void testIncompleteMultiDimensionalMultipleProfiles() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(IncompleteMultiDimensionalMultipleProfiles);

        SOSParser md = new SOSParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequest, IncompleteMultiDimensionalMultipleProfiles);
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
        md.enhance(dataset, write, profileRequestIndexed, IndexedRaggedMultipleProfiles);
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
        md.enhance(dataset, write, profileRequest, OrthogonalMultiDimensionalMultipleProfiles);
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
        md.enhance(dataset, write, profileRequest, OrthogonalSingleDimensionalSingleProfile);
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
