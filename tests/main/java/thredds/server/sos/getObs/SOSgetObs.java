/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.getObs;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.Writer;
import java.io.CharArrayWriter;
import thredds.server.sos.service.MetadataParser;
import ucar.nc2.dataset.NetcdfDataset;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Abird
 */
public class SOSgetObs {
//ragged Array - timeseries profile

    private static String cfRaggedSingleConventions = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Ragged-SingeStation-H.5.3/timeSeriesProfile-Ragged-SingeStation-H.5.3.nc";
    private static String cfRaggedSingleConventionsNew = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Ragged-SingeStation-H.5.3/timeSeriesProfile-Ragged-SingeStation-H.5.3.new.nc";
    private static String imedsLocation = "C://Program Files//Apache Software Foundation//Apache Tomcat 6.0.26//content//thredds//public//imeds//watlev_NOAA.F.C_IKE_VIMS_3D_WITHWAVE.nc";
    private static String NOAA_NDBC = "C://Documents and Settings//abird//My Documents//NetBeansProjects//ncSOS//tests//main//resources//datasets//NOAA_NDBC_42035_2008met.nc";
    private static String imedsLocation1 = "C://Program Files//Apache Software Foundation//Apache Tomcat 6.0.26//content//thredds//public//imeds//watlev_TCOON.F.C.nc";
    private static String imedsLocationNew = "C://Program Files//Apache Software Foundation//Apache Tomcat 6.0.26//content//thredds//public//imeds//watlev_TCOON.F.C.new.nc";
    private static String audry = "C://files//audry.bpt.nc";
    
    
    //ragged Array - timeseries profile
    private static String RaggedSingleConventions = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Ragged-SingleStation-H.5.3/timeSeriesProfile-Ragged-SingleStation-H.5.3.nc";
    private static String RaggedMultiConventions = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Ragged-MultipeStations-H.5.3/timeSeriesProfile-Ragged-MultipeStations-H.5.3.nc";
    private static String OrthogonalMultidimensionalMultiStations = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1.nc";
    private static String MultiDimensionalSingleStations = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Multidimensional-SingleStation-H.5.2/timeSeriesProfile-Multidimensional-SingleStation-H.5.2.nc";
    private static String MultiDimensionalMultiStations = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";
//point
    private static String cfPoint = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/point-H.1/point-H.1.nc";
// profile
    private static String ContiguousRaggedMultipleProfiles = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/profile-Contiguous-Ragged-MultipleProfiles-H.3.4/profile-Contiguous-Ragged-MultipleProfiles-H.3.4.nc";
    private static String IncompleteMultiDimensionalMultipleProfiles = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2.nc";
    private static String IndexedRaggedMultipleProfiles = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/profile-Indexed-Ragged-MultipleProfiles-H.3.5/profile-Indexed-Ragged-MultipleProfiles-H.3.5.nc";
    private static String OrthogonalMultiDimensionalMultipleProfiles = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1.nc";
    private static String OrthogonalSingleDimensionalSingleProfile = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3.nc";
    public static final String base = "tests/main/java/thredds/server/sos/getObs/output/";

    private void dataAvailableInOutputFile(Writer write) {
        assertTrue("error no values",write.toString().contains("<swe:values>"));
        assertFalse("error no values: error where data should be",write.toString().contains("<swe:values>ERROR!</swe:values>"));
    }

    private void fileWriter(String base, String fileName, Writer write) throws IOException {
        Writer output = null;
        File file = new File(base + fileName);
        output = new BufferedWriter(new FileWriter(file));
        output.write(write.toString());
        output.close();
        System.out.println("Your file has been written");
    }

    @Test
    public void testenhanceImedsNew() throws IOException {
        //fail("not ready yet");
        System.out.println("----imedsLocationNew------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocationNew);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetObservation&version=1&service=sos&observedProperty=watlev&offering=TCOON_87707771", imedsLocationNew);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "imedsLocationNew.xml";
        fileWriter(base, fileName, write);
         dataAvailableInOutputFile(write);
        System.out.println("----------end-----------");
    }
    
    @Test
    public void testenhanceNOAA_NDBC() throws IOException {
        //fail("not ready yet");
        System.out.println("----NOAA_NDBC------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(NOAA_NDBC);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetObservation&version=1&service=sos&observedProperty=watlev&offering=NOAA_8723970", NOAA_NDBC);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "NOAA_NDBC.xml";
        fileWriter(base, fileName, write);
         dataAvailableInOutputFile(write);
        System.out.println("----------end-----------");
    }
    
    @Test
    public void testenhanceAudry() throws IOException {
        //fail("not ready yet");
        System.out.println("----audry------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(audry);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetObservation&version=1&service=sos&observedProperty=watlev&offering=NOAA_8723970", audry);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "audry.xml";
        fileWriter(base, fileName, write);
         dataAvailableInOutputFile(write);
        System.out.println("----------end-----------");
    }
    
    
    //**********************************
//TIMESERIESPROFILE TEST
    public static final String timeSeriesProfileRequest = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station1&eventtime=0";

    @Test
    public void testenhanceSingleRaggedDataset() throws IOException {
        System.out.println("----RaggedSingleConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedSingleConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequest, RaggedSingleConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedSingleConventions.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"height\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testenhanceMultiRaggedDataset() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequest, RaggedMultiConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventions.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"height\">"));
        System.out.println("----------end-----------");
    }
    public static final String timeSeriesProfileRequest2 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station1&eventtime=0";

    @Test
    public void testOrthogonalMultidimensionalMultiStations() throws IOException {
        System.out.println("----OrthogonalMultidimensionalMultiStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalMultidimensionalMultiStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequest2, OrthogonalMultidimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "OrthogonalMultidimensionalMultiStations.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testMultiDimensionalSingleStations() throws IOException {
        System.out.println("----MultiDimensionalSingleStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalSingleStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequest2, MultiDimensionalSingleStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalSingleStations.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testMultiDimensionalMultiStations() throws IOException {
        System.out.println("----MultiDimensionalMultiStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequest2, MultiDimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStations.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
                //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }
    public static final String timeSeriesProfileRequest3 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station2&eventtime=0";

    @Test
    public void testMultiDimensionalMultiStationsStation2() throws IOException {
        System.out.println("----MultiDimensionalMultiStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequest3, MultiDimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStations.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }
    //**********************************

//PROFILE TEST
    
    public static final String profileRequest = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,humidity&eventtime=1990-01-01T00:00:00Z";
    public static final String profileRequestIndexed = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,humidity&eventtime=1990-01-01T01:00:00Z";
    
    @Test
    public void testContiguousRaggedMultipleProfiles() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequest, ContiguousRaggedMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "ContiguousRaggedMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"z\">"));
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

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequest, IncompleteMultiDimensionalMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "IncompleteMultiDimensionalMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"z\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testIndexedRaggedMultipleProfiles() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(IndexedRaggedMultipleProfiles);
        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequestIndexed, IndexedRaggedMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "IndexedRaggedMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"z\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testOrthogonalMultiDimensionalMultipleProfiles() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalMultiDimensionalMultipleProfiles);
        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequest, OrthogonalMultiDimensionalMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "OrthogonalMultiDimensionalMultipleProfiles.xml";
        fileWriter(base, fileName, write);
       dataAvailableInOutputFile(write);
       //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"z\">"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testOrthogonalSingleDimensionalSingleProfile() throws IOException {
        spaceBetweenTests();
        System.out.println("----OrthogonalSingleDimensionalSingleProfile------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalSingleDimensionalSingleProfile);
        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequest, OrthogonalSingleDimensionalSingleProfile);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "OrthogonalSingleDimensionalSingleProfile.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"z\">"));
        System.out.println("----------end-----------");
    }
//**********************************
}
