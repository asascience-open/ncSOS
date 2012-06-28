/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncSOS.getObs;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import java.io.*;
import java.util.HashMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import com.asascience.ncsos.util.XMLDomUtils;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author abird
 */
public class GridObsTest {
    
    //***********************************************
//    public static final String GridReq1 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=mcsst&eventtime=1990-01-01T00:00:00Z/2012-05-17T09:57:00.000-04:00";
//    public static final String GridReq2 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=mcsst&offering=mcsst&eventtime=1990-01-01T00:00:00Z/2012-05-17T09:57:00.000-04:00&lat=29.88603303&lon=-89.0087125";
//    public static final String GridReq3 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=mcsst&eventtime=1990-01-01T00:00:00Z/2012-05-17T09:57:00.000-04:00&lat=29.0&lon=-89.0";
    private static final String sst_1 = "resources/datasets/satellite-sst/20120617.1508.d7.composite.nc";
    private static final String sst_1_reqs = "request=GetObservation&service=sos&version=1.0.0&lat=52.0&lon=-50.0&observedProperty=mcsst&offering=mcsst&eventtime=1990-01-01T00:00:00Z/2012-05-17T09:57:00.000-04:00&responseFormat=oostethysswe";
    private static final String sst_2 = "resources/datasets/satellite-sst/20120617.1716.d7.composite.nc";
    private static final String sst_2_reqs = "request=GetObservation&service=sos&version=1.0.0&lat=52.0,51.004,50.547&lon=-50.0,-52.156,-52.156&observedProperty=mcsst&offering=mcsst&eventtime=1990-01-01T00:00:00Z/2012-05-17T09:57:00.000-04:00&responseFormat=oostethysswe";
    public static String baseLocalDir = null;
    public static String outputDir = null;

    private void fileWriter(String base, String fileName, Writer write) throws IOException {
        File file = new File(base + fileName);
        Writer output = new BufferedWriter(new FileWriter(file));
        output.write(write.toString());
        output.close();
        System.out.println("Your file has been written");
    }
    
    @BeforeClass
    public static void SetupEnviron() throws FileNotFoundException {
        // not really a test, just used to set up the various string values
        if (outputDir != null && baseLocalDir != null) {
            // exit early if the environ is already set
            return;
        }
        String container = "getObsGrid";
        InputStream templateInputStream = null;
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputBase");
            baseLocalDir = XMLDomUtils.getNodeValue(configDoc, container, "projectDir");
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
    }
    
    @Test
    public void testGetObsGridSSTSingleLatLon() throws IOException {
        System.out.println("------SST1------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + sst_1);
        Writer writer = new CharArrayWriter();
        SOSParser parser = new SOSParser();
        HashMap<String, Object> outMap = parser.enhance(dataset, sst_1_reqs, baseLocalDir + sst_1);
        SOSOutputFormatter output = (SOSOutputFormatter)outMap.get("outputHandler");
        assertNotNull("output is null", output);
        output.writeOutput(writer);
        writer.flush();
        writer.close();
        fileWriter(outputDir, "testGetObsGridSSTSingleLatLon_output.xml", writer);
        assertFalse("Have an exception in output", writer.toString().contains("Exception"));
        System.out.println("------SST1-End------");
    }
    
    @Test
    public void testGetObsGridSSTMultipleLatLon() throws IOException {
        System.out.println("------SST2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + sst_2);
        Writer writer = new CharArrayWriter();
        SOSParser parser = new SOSParser();
        HashMap<String, Object> outMap = parser.enhance(dataset, sst_2_reqs, baseLocalDir + sst_2);
        SOSOutputFormatter output = (SOSOutputFormatter)outMap.get("outputHandler");
        assertNotNull("output is null", output);
        output.writeOutput(writer);
        writer.flush();
        writer.close();
        fileWriter(outputDir, "testGetObsGridSSTMultipleLatLon_output.xml", writer);
        assertFalse("Have an exception in output", writer.toString().contains("Exception"));
        System.out.println("------SST2-End------");
    }
    
//    @Test
//    public void testGetObsGridWithoutLatLon() throws IOException {
//        System.out.println("----GRID1------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
//        Writer write = new CharArrayWriter();
//        SOSParser.enhance(dataset, write, GridReq1, "D:/Data/20120417.108.1357.n16.EC1.nc");
//        write.flush();
//        write.close();
//        assertTrue(write.toString().contains("Exception"));
//        String fileName = "Gridded1.xml";
//        fileWriter(base, fileName, write);
//    }
//
//    @Test
//    public void testGetObsGridWithLatLon() throws IOException {
//        System.out.println("----GRID2------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
//        Writer write = new CharArrayWriter();
//        SOSParser.enhance(dataset, write, GridReq2, "D:/Data/20120417.108.1357.n16.EC1.nc");
//        write.flush();
//        write.close();
//        assertFalse(write.toString().contains("Exception"));
//
//        String fileName = "Gridded2.xml";
//        fileWriter(base, fileName, write); 
//    }
//
//    @Test
//    public void testGetObsGridValuesCorrect() throws IOException {
//        System.out.println("----GRID2.1------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
//        //("2012-04-17T13:57:00Z,22.8,29.886033032786887,-89.00871255834629 \n"));
//        Map<String, String> latLonRequest = new HashMap<String, String>();
//        latLonRequest.put("lat", "29.88603303");
//        latLonRequest.put("lon", "-89.0087125");
//
//        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset,new String[]{"mcsst"} , new String[]{"mcsst"}, new String[]{"1990-01-01T00:00:00Z","2012-05-17T09:57:00.000-04:00"}, latLonRequest);
//        handler.parseObservations();
//  
//        String value = XMLDomUtils.getNodeValue(handler.getDocument(),"swe:DataArray", "swe:values");
//        assertEquals("2012-04-17T13:57:00Z,22.8,29.886033032786887,-89.00871255834629 \n",value);
//    }
//
//    @Test
//    public void testGetObsGridWithLatLonTemperature() throws IOException {
//        System.out.println("----GRID3------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
//        Writer write = new CharArrayWriter();
//        SOSParser.enhance(dataset, write, GridReq3, "D:/Data/20120417.108.1357.n16.EC1.nc");
//        write.flush();
//        write.close();
//        assertFalse(write.toString().contains("Exception"));
//        String fileName = "Gridded3.xml";
//        fileWriter(base, fileName, write);
//    }
//    
//    public static final String GridReq4 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=u&offering=u&eventtime=1990-01-01T00:00:00Z/2013-05-17T09:57:00.000-04:00&lat=29.0&lon=-89.0";
//    
//    
//    @Test
//    public void testGetObsGridWithLatLonU() throws IOException {
//        System.out.println("----GRID4------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        Writer write = new CharArrayWriter();
//        SOSParser.enhance(dataset, write, GridReq4, "D:/Data/201204201200_HFRadar_USWC_6km_rtv_NDBC.nc");
//        write.flush();
//        write.close();
//        assertFalse(write.toString().contains("Exception"));
//        String fileName = "Gridded4.xml";
//        fileWriter(base, fileName, write);     
//    }
//    
//    @Test
//    public void testGetObsGridWithLatLonUData() throws IOException {
//        System.out.println("----GRID4.1------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204200800_HFRadar_USWC_6km_rtv_NDBC.nc");
//        Map<String, String> latLonRequest = new HashMap<String, String>();
//        latLonRequest.put("lat", "36.1834");
//        latLonRequest.put("lon", "-123.4881");
//
//        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset,new String[]{"u"} , new String[]{"u"}, new String[]{"1990-01-01T00:00:00Z","2012-05-20T13:00:00Z"}, latLonRequest);
//        handler.parseObservations();
//  
//        String value = XMLDomUtils.getNodeValue(handler.getDocument(),"swe:DataArray", "swe:values");
//        String[] Results = value.split(",");
//        assertEquals("2012-04-20T08:00:00Z",Results[0]);
//        assertEquals("-0.07",Results[1]);
//        assertTrue(Results[2].contains("36.183"));
//        assertTrue(Results[3].contains("-123.488"));
//    }
//    
//     @Test
//    public void testGetObsGridWithLatLonVData() throws IOException {
//        System.out.println("----GRID4.2------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/201204200800_HFRadar_USWC_6km_rtv_NDBC.nc");
//        Map<String, String> latLonRequest = new HashMap<String, String>();
//        latLonRequest.put("lat", "36.1834");
//        latLonRequest.put("lon", "-123.4881");
//
//        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset,new String[]{"v"} , new String[]{"v"}, new String[]{"1990-01-01T00:00:00Z","2012-05-20T13:00:00Z"}, latLonRequest);
//        handler.parseObservations();
//  
//        String value = XMLDomUtils.getNodeValue(handler.getDocument(),"swe:DataArray", "swe:values");
//        String[] Results = value.split(",");
//        assertEquals("2012-04-20T08:00:00Z",Results[0]);
//        assertEquals("-0.26",Results[1]);
//        assertTrue(Results[2].contains("36.183"));
//        assertTrue(Results[3].contains("-123.488"));
//    }
//     
//     public static final String GridReq5 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=salinity&offering=salinity&eventtime=1990-01-01T00:00:00Z/2013-05-17T09:57:00.000-04:00&lat=5&lon=262";
//    
//     
//    @Test
//    public void testGetObsGridWithNoDepthParamSet() throws IOException {
//         System.out.println("----GRID5------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/ncom_relo_amseas_u_2012041800_t036.nc");
//        Writer write = new CharArrayWriter();
//        SOSParser.enhance(dataset, write, GridReq5, "D:/Data/ncom_relo_amseas_u_2012041800_t036.nc");
//        write.flush();
//        write.close();
//        assertFalse(write.toString().contains("Exception"));
//        String fileName = "Gridded5.xml";
//        fileWriter(base, fileName, write);     
//    }
//    
//    public static final String GridReq6 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=salinity&offering=salinity&eventtime=1990-01-01T00:00:00Z/2013-05-17T09:57:00.000-04:00&lat=5&lon=262&depth=1";
//    
//    
//    @Test
//    public void testGetObsGridWithDepthParamSet() throws IOException {
//         System.out.println("----GRID6------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/ncom_relo_amseas_u_2012041800_t036.nc");
//        Writer write = new CharArrayWriter();
//        SOSParser.enhance(dataset, write, GridReq6, "D:/Data/ncom_relo_amseas_u_2012041800_t036.nc");
//        write.flush();
//        write.close();
//        assertFalse(write.toString().contains("Exception"));
//        String fileName = "Gridded6.xml";
//        fileWriter(base, fileName, write);     
//    }
//    
//       @Test
//    public void testGetObsGridWithLatLonNoDepthSalinityData() throws IOException {
//        System.out.println("----GRID6.2------");
//        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/ncom_relo_amseas_u_2012041800_t036.nc");
//        Map<String, String> latLonRequest = new HashMap<String, String>();
//        latLonRequest.put("lat", "31.9730281829");
//        latLonRequest.put("lon", "290.47131347");
//
//        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset,new String[]{"salinity"} , new String[]{"salinity"}, new String[]{"1990-01-01T00:00:00Z","2013-05-20T13:00:00Z"}, latLonRequest);
//        handler.parseObservations();
//  
//        String value = XMLDomUtils.getNodeValue(handler.getDocument(),"swe:DataArray", "swe:values");
//        String[] Results = value.split(",");
//        assertEquals(5, Results.length);
//        assertEquals("2012-04-19T12:00:00Z",Results[0]);
//        assertEquals("36.268",Results[1]);
//        assertEquals("0",Results[2]);
//        assertTrue(Results[3].contains("31.9730"));
//        assertTrue(Results[4].contains("290.471"));
//       
//   
//    }
}
