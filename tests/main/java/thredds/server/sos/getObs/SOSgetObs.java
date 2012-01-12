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
//ragged Array - timeseries profile
    private static String RaggedSingleConventions = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Ragged-SingeStation-H.5.3/timeSeriesProfile-Ragged-SingeStation-H.5.3.nc";
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
        assertTrue(write.toString().contains("<swe:values>"));
        System.out.println("----------end-----------");
    }

   //**********************************
//TIMESERIESPROFILE TEST
    public static final String timeSeriesRequest = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,height&offering=Station1&eventtime=0";
    
    @Test
    public void testenhanceSingleRaggedDataset() throws IOException {
        System.out.println("----RaggedSingleConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedSingleConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesRequest, RaggedSingleConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedSingleConventions.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<swe:values>"));
        assertFalse(write.toString().contains("ERROR!"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testenhanceMultiRaggedDataset() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesRequest, RaggedMultiConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventions.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<swe:values>"));
        assertFalse(write.toString().contains("ERROR!"));
        System.out.println("----------end-----------");
    }

    @Test
    public void testOrthogonalMultidimensionalMultiStations() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalMultidimensionalMultiStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesRequest, OrthogonalMultidimensionalMultiStations);
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

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesRequest, MultiDimensionalSingleStations);
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

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesRequest, MultiDimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStations.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }
   //**********************************
}
