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
import thredds.server.sos.service.SOSGetCapabilitiesRequestHandler;
import java.io.IOException;
import java.io.Writer;
import thredds.server.sos.util.DatasetHandlerAdapter;
import ucar.nc2.dataset.NetcdfDataset;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import thredds.server.sos.service.MetadataParser;
import thredds.server.sos.service.SOSBaseRequestHandler;
import ucar.nc2.util.CancelTask;
import static org.junit.Assert.*;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author abird
 */
public class SOSgetCaps {

     //timeseries
    private static String tsIncompleteMultiDimensionalMultipleStations = "tests/main/resources/datasets/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2.nc";
    private static String tsMultidimensionalMultipeStations = "tests/main/resources/datasets/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";

    
    private static String imedsLocation = "C://Program Files//Apache Software Foundation//Apache Tomcat 6.0.26//content//thredds//public//imeds//watlev_NOAA.F.C_IKE_VIMS_3D_WITHWAVE.nc";
    private static String NOAA_NDBC = "C://Documents and Settings//abird//My Documents//NetBeansProjects//ncSOS//tests//main//resources//datasets//NOAA_NDBC_42035_2008met.nc";
    private static String imedsLocation1 = "C://Program Files//Apache Software Foundation//Apache Tomcat 6.0.26//content//thredds//public//imeds//watlev_TCOON.F.C.nc";
    private static String imedsLocationNew = "C://Program Files//Apache Software Foundation//Apache Tomcat 6.0.26//content//thredds//public//imeds//watlev_TCOON.F.C.new.nc";
//ragged Array - timeseries profile
    private static String RaggedSingleConventions = "tests/main/resources/datasets/timeSeriesProfile-Ragged-SingleStation-H.5.3/timeSeriesProfile-Ragged-SingleStation-H.5.3.nc";
    private static String RaggedMultiConventions = "tests/main/resources/datasets/timeSeriesProfile-Ragged-MultipeStations-H.5.3/timeSeriesProfile-Ragged-MultipeStations-H.5.3.nc";
    private static String OrthogonalMultidimensionalMultiStations = "tests/main/resources/datasets/timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Orthogonal-Multidimensional-MultipeStations-H.5.1.nc";
    private static String MultiDimensionalSingleStations = "tests/main/resources/datasets/timeSeriesProfile-Multidimensional-SingleStation-H.5.2/timeSeriesProfile-Multidimensional-SingleStation-H.5.2.nc";
    private static String MultiDimensionalMultiStations = "tests/main/resources/datasets/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";
//point
    private static String cfPoint = "tests/main/resources/datasets/point-H.1/point-H.1.nc";
// profile
    private static String ContiguousRaggedMultipleProfiles = "tests/main/resources/datasets/profile-Contiguous-Ragged-MultipleProfiles-H.3.4/profile-Contiguous-Ragged-MultipleProfiles-H.3.4.nc";
    private static String IncompleteMultiDimensionalMultipleProfiles = "tests/main/resources/datasets/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2.nc";
    private static String IndexedRaggedMultipleProfiles = "tests/main/resources/datasets/profile-Indexed-Ragged-MultipleProfiles-H.3.5/profile-Indexed-Ragged-MultipleProfiles-H.3.5.nc";
    private static String OrthogonalMultiDimensionalMultipleProfiles = "tests/main/resources/datasets/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1/profile-Orthogonal-MultiDimensional-MultipleProfiles-H.3.1.nc";
    private static String OrthogonalSingleDimensionalSingleProfile = "tests/main/resources/datasets/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3/profile-Orthogonal-SingleDimensional-SingleProfile-H.3.3.nc";
    
    
    
    public static final String base = "tests/main/java/thredds/server/sos/getCaps/output/";

    private void fileWriter(String base, String fileName, Writer write) throws IOException {
        Writer output = null;
        File file = new File(base + fileName);
        output = new BufferedWriter(new FileWriter(file));
        output.write(write.toString());
        output.close();
        System.out.println("Your file has been written");
    }
//**********************************
//TIMESERIES TEST
    @Test
    public void testIncompleteMultiDimensionalMultipleStations() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", tsIncompleteMultiDimensionalMultipleStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsIncompleteMultiDimensionalMultipleStations.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }
    
    @Test
    public void testMultidimensionalMultipeStations() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsMultidimensionalMultipeStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", tsMultidimensionalMultipeStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsMultidimensionalMultipeStations.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }
//**********************************
//TIMESERIESPROFILE TEST
    @Test
    public void testenhanceSingleRaggedDataset() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedSingleConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", RaggedSingleConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedSingleConventions.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }

    @Test
    public void testenhanceMultiRaggedDataset() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", RaggedMultiConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventions.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }

    @Test
    public void testOrthogonalMultidimensionalMultiStations() throws IOException {
        fail("file invalid i think");
        NetcdfDataset dataset = NetcdfDataset.openDataset(OrthogonalMultidimensionalMultiStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", OrthogonalMultidimensionalMultiStations);
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

        MetadataParser md = new MetadataParser();
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

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", MultiDimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStations.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
    }
    

//**********************************
//PROFILE TEST
    @Test
    public void testContiguousRaggedMultipleProfiles() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1.0.0&service=SOS", ContiguousRaggedMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "ContiguousRaggedMultipleProfiles.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        System.out.println("----end------");
    }

    private void spaceBetweenTests() {
        System.out.println("\n");
    }

    @Test
    public void testIncompleteMultiDimensionalMultipleProfiles() throws IOException {       
        NetcdfDataset dataset = NetcdfDataset.openDataset(IncompleteMultiDimensionalMultipleProfiles);

        MetadataParser md = new MetadataParser();
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
        MetadataParser md = new MetadataParser();
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
        MetadataParser md = new MetadataParser();
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
        MetadataParser md = new MetadataParser();
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
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocation);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds");
        handler.parseServiceIdentification();
    }

    @Test
    public void testMetaDataParserServiceDescription() throws IOException {
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocation);

        SOSGetCapabilitiesRequestHandler handler = new SOSGetCapabilitiesRequestHandler(dataset, "imeds");
        handler.parseServiceIdentification();
        handler.parseServiceDescription();
    }

    @Test
    public void testMetaDataParserOperationMetaData() throws IOException {
        //3.SOSgetcaps
        //2.sos metadata parser
        //1.sos controller  
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocation);

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
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocation);

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
        NetcdfDataset dataset = NetcdfDataset.openDataset(NOAA_NDBC);

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
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocation);
        String cdm_datatype = dataset.findAttValueIgnoreCase(null, "cdm_data_type", null);
        System.out.println(cdm_datatype);
        FeatureDataset featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.STATION, dataset, null, new Formatter(System.err));

        assertTrue(featureDataset != null);
    }

    @Test
    public void testStationFileNotNull() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset("C://Documents and Settings//abird//My Documents//NetBeansProjects//ncSOS//tests//main//resources//datasets//Station_42080_2008met.nc");
        String cdm_datatype = dataset.findAttValueIgnoreCase(null, "cdm_data_type", null);
        System.out.println(cdm_datatype);
        FeatureDataset featureDataset = FeatureDatasetFactoryManager.wrap(FeatureType.STATION, dataset, null, new Formatter(System.err));
        assertTrue(featureDataset != null);
    }

    @Test
    public void testenhanceImedsDataset() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocation);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", imedsLocation);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
    }

    @Test
    public void testenhanceNOAADataset() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(NOAA_NDBC);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", NOAA_NDBC);
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
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocation);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", imedsLocation);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "NOAA2.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        System.out.println("----end------");
        
    }

    
    
    @Test
    public void testenhanceTCOONDataset() throws IOException {
        fail("issue with feature type: using imedsLocation Tcoon New");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocation1);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", imedsLocation1);
        assertFalse(write.toString().contains("Exception"));
        String fileName = "TCOON.xml";
        fileWriter(base, fileName, write);
        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
        System.out.println("----end------");
    }

    @Test
    public void testenhanceTCOONDatasetNew() throws IOException {

        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocationNew);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", imedsLocationNew);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
    }

    @Test
    public void testenhancePoint() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(cfPoint);
        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", cfPoint);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
    }
}
