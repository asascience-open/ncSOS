///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package thredds.server.sos.getCaps;
//
//import java.io.BufferedWriter;
//import java.io.CharArrayWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import ucar.nc2.ft.FeatureDataset;
//import ucar.nc2.ft.FeatureDatasetFactoryManager;
//import ucar.nc2.constants.FeatureType;
//import java.util.Formatter;
//import javax.swing.JOptionPane;
//import ucar.nc2.thredds.ThreddsDataFactory;
//import java.io.IOException;
//import java.io.Writer;
//import thredds.server.sos.util.DatasetHandlerAdapter;
//import ucar.nc2.dataset.NetcdfDataset;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import thredds.server.sos.service.SOSParser;
//import thredds.server.sos.service.SOSBaseRequestHandler;
//import ucar.nc2.util.CancelTask;
//import static org.junit.Assert.*;
//import ucar.unidata.geoloc.LatLonRect;
//
///**
// *
// * @author abird
// */
//public class GridCapsTest {
//
//    public static final String base = "tests/main/java/thredds/server/sos/getCaps/output/";
//
//    private static void fileWriter(String base, String fileName, Writer write) throws IOException {
//        Writer output = null;
//        File file = new File(base + fileName);
//        output = new BufferedWriter(new FileWriter(file));
//        output.write(write.toString());
//        output.close();
//        System.out.println("Your file has been written");
//    }
//
//    @Test
//    public void testCanIdentifyGriddedCDM() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
//        SOSGetCapabilitiesRequestHandler sosget = new SOSGetCapabilitiesRequestHandler(dataset, "threddsURI");
//        assertEquals(FeatureType.GRID, sosget.getDatasetFeatureType());
//    }
//
//    @Test
//    public void testParseGriddedCDM() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
//        SOSParser md = new SOSParser();
//        Writer write = new CharArrayWriter();
//        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", "D:/Data/20120417.108.1357.n16.EC1.nc");
//        write.flush();
//        write.close();
//        assertFalse(write.toString().contains("Exception"));
//        String fileName = "gridded.xml";
//        fileWriter(base, fileName, write);
//        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
//    }
//
//    @Test
//    public void testParseGriddedHFRadarCDM() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        SOSParser md = new SOSParser();
//        Writer write = new CharArrayWriter();
//        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", "D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        write.flush();
//        write.close();
//        assertFalse(write.toString().contains("Exception"));
//        String fileName = "griddedHF.xml";
//        fileWriter(base, fileName, write);
//        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
//    }
//
//    @Test
//    public void testHFRadarUExistsInGetCaps() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        SOSParser md = new SOSParser();
//        Writer write = new CharArrayWriter();
//        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", "D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        write.flush();
//        write.close();
//        assertTrue(write.toString().contains("<ObservationOffering gml:id=\"u\">"));
//    }
//
//    @Test
//    public void testHFRadarVExistsInGetCaps() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        SOSParser md = new SOSParser();
//        Writer write = new CharArrayWriter();
//        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", "D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        write.flush();
//        write.close();
//        assertTrue(write.toString().contains("<ObservationOffering gml:id=\"v\">"));
//    }
//
//    @Test
//    public void testHFRadarLatMinCorrect() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        SOSParser md = new SOSParser();
//        Writer write = new CharArrayWriter();
//        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", "D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        write.flush();
//        write.close();
//        assertTrue(write.toString().contains("<gml:lowerCorner>30.25"));
//    }
//
//    @Test
//    public void testHFRadarLatMaxCorrect() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        SOSParser md = new SOSParser();
//        Writer write = new CharArrayWriter();
//        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", "D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        write.flush();
//        write.close();
//        assertTrue(write.toString().contains("<gml:upperCorner>49.9920"));
//    }
//
//    @Test
//    public void testHFRadarLonMinCorrect() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        SOSParser md = new SOSParser();
//        Writer write = new CharArrayWriter();
//        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", "D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        write.flush();
//        write.close();
//        assertTrue(write.toString().contains("-130.360"));
//    }
//
//    @Test
//    public void testHFRadarLonMaxCorrect() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        SOSParser md = new SOSParser();
//        Writer write = new CharArrayWriter();
//        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", "D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        write.flush();
//        write.close();
//        assertTrue(write.toString().contains("-115.80556"));
//    }
//    
//    @Test
//    public void testDepthNCGriddedGetCaps() throws IOException {
//          NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/ncom_relo_amseas_u_2012041800_t036.nc");
//        SOSParser md = new SOSParser();
//        Writer write = new CharArrayWriter();
//        md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", "D:/Data/ncom_relo_amseas_u_2012041800_t036.nc");
//        write.flush();
//        write.close();
//        assertFalse(write.toString().contains("Exception"));
//        String fileName = "griddedDepth.xml";
//        fileWriter(base, fileName, write);
//        assertTrue(write.toString().contains("<ObservationOffering gml:id="));
//    }
//}
