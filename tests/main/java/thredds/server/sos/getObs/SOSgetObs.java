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

//imeds data
   private static String NOAA_NDBC = "C://Documents and Settings//abird//My Documents//NetBeansProjects//ncSOS//tests//main//resources//datasets//NOAA_NDBC_42035_2008met.nc";
    private static String imedsLocationNew = "C://Program Files//Apache Software Foundation//Apache Tomcat 6.0.26//content//thredds//public//imeds//watlev_TCOON.F.C.new.nc";
    private static String audry = "C://files//audry.bpt.nc";

    //timeseries
    private static String tsIncompleteMultiDimensionalMultipleStations = "tests/main/resources/datasets/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2/timeSeries-Incomplete-MultiDimensional-MultipleStations-H.2.2.nc";
    private static String tsOrthogonalMultidimenstionalMultipleStations = "tests/main/resources/datasets/timeSeries-Orthogonal-Multidimenstional-MultipleStations-H.2.1/timeSeries-Orthogonal-Multidimenstional-MultipleStations-H.2.1.nc";
    
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
        fail("not ready yet");
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
        fail("bad file - not ready yet");
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
        fail("possible bad file - not ready yet");
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
//TIMESERIES TEST
    
    public static final String timeSeriestOrth = "request=GetObservation&version=1.0.0&service=sos&observedProperty=alt&offering=Station-1&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesIncomplete = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z";
    public static final String timeSeriesMulti = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature,alt&offering=Station-9";
    public static final String timeSeriesIncompleteWithTime = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z";
     
    
    public static final String timeSeriesIncompleteMulti = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9&eventtime=1990-01-01T00:00:00Z/1990-01-01T10:00:00Z";
     public static final String timeSeriesIncompleteMultiInvalid = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9&eventtime=1990-02-01T00:00:00Z/1990-05-01T10:00:00Z";
    
     public static final String timeSeriesIncompleteMultiStation = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station-9,Station-8&eventtime=1990-01-01T00:00:00Z/1990-01-01T8:00:00Z";
    
     @Test
    public void testMultiTimeCreateDataStruct() throws IOException {
         
         System.out.println("----tsData------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesIncompleteMultiStation, tsIncompleteMultiDimensionalMultipleStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsData.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        
        //check dates
        assertTrue("Time1 entered",write.toString().contains("1990-01-01T00:00:00Z,"));
        assertTrue("Time2 entered",write.toString().contains("1990-01-01T07:00:00Z,"));        
        assertTrue("Time3 entered",write.toString().contains("1990-01-01T06:00:00Z,"));
        
        //check data
        assertTrue("Station8 Data T0",write.toString().contains("1990-01-01T00:00:00Z,32.0,4.554936"));
        assertTrue("Station8 Data T2",write.toString().contains("1990-01-01T02:00:00Z,36.0,4.554936"));
        
        assertTrue("Station9 Data T0",write.toString().contains("1990-01-01T00:00:00Z,37.0,0.47794318"));
        assertTrue("Station9 Data T2",write.toString().contains("1990-01-01T02:00:00Z,6.0,0.47794318"));
        
        
        System.out.println("----------end-----------");
    }
     
     
    @Test
    public void testTimeSeriesIncompleteDataMultiTimeInvalid() throws IOException {
        System.out.println("----tsIncompleteMultiDimensionalMultipleStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesIncompleteMultiInvalid, tsIncompleteMultiDimensionalMultipleStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsIncompleteMultiDimensionalMultipleStationsMultiTimeInvalid.xml";
        fileWriter(base, fileName, write);
        //dataAvailableInOutputFile(write);
        
        assertFalse("Time1 entered",write.toString().contains("1990-01-01T00:00:00Z,"));
        assertFalse("Time2 entered",write.toString().contains("1990-01-01T10:00:00Z,"));
        
        assertFalse("Time3 entered",write.toString().contains("1990-01-01T11:00:00Z,"));
        
        System.out.println("----------end-----------");
    }
    
    @Test
    public void testTimeSeriesIncompleteDataMultiTime() throws IOException {
        System.out.println("----tsIncompleteMultiDimensionalMultipleStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesIncompleteMulti, tsIncompleteMultiDimensionalMultipleStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "tsIncompleteMultiDimensionalMultipleStationsMultiTime.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        
        assertTrue("Time1 not entered",write.toString().contains("1990-01-01T00:00:00Z,"));
        assertTrue("Time2 not entered",write.toString().contains("1990-01-01T10:00:00Z,"));
        
        assertFalse("Time3 entered when not!",write.toString().contains("1990-01-01T11:00:00Z,"));
        
        System.out.println("----------end-----------");
    }
    
    
    @Test
    public void testTimeSeriesIncomplete() throws IOException {
        System.out.println("----tsIncompleteMultiDimensionalMultipleStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(tsIncompleteMultiDimensionalMultipleStations);

        MetadataParser md = new MetadataParser();
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

        MetadataParser md = new MetadataParser();
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

        MetadataParser md = new MetadataParser();
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
    
     
    @Test
    public void testenhanceSingleRaggedDataset() throws IOException {
        fail("issue with time series profile netcdf file - no temperature");
        
        System.out.println("----RaggedSingleConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedSingleConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequestSingle, RaggedSingleConventions);
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
    public void testMultiTimeSeriesProfileRequest() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequestMulti, RaggedMultiConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventionsMultiTime.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"height\">"));
        
       assertTrue("data missing",write.toString().contains("1990-01-01T00:00:00Z,6.7,0.5")); 
       assertTrue("data missing",write.toString().contains("1990-01-01T00:00:00Z,6.9,1.5"));                             

        
       assertTrue("data missing",write.toString().contains("1990-01-01T02:00:00Z,6.7,0.5")); 
       assertTrue("data missing",write.toString().contains("1990-01-01T02:00:00Z,7.0,1.5")); 
        
        System.out.println("----------end-----------");
    }
    
     public static final String timeSeriesProfileRequestMultiInvalidDates = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=uri.sos:Station1&eventtime=1990-04-01T00:00:00Z/1990-08-01T02:00:00Z";            
    
     
     @Test
    public void testMultiTimeSeriesProfileRequestInvalidDates() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequestMultiInvalidDates, RaggedMultiConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventionsMultiTimeInvalidDates.xml";
        fileWriter(base, fileName, write);
        //dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"height\">"));
        
       assertFalse("data missing",write.toString().contains("1990-01-01T00:00:00Z,6.7,0.5")); 
       assertFalse("data missing",write.toString().contains("1990-01-01T00:00:00Z,6.9,1.5"));                             

        
       assertFalse("data missing",write.toString().contains("1990-01-01T02:00:00Z,6.7,0.5")); 
       assertFalse("data missing",write.toString().contains("1990-01-01T02:00:00Z,7.0,1.5")); 
        
        System.out.println("----------end-----------");
    }
     
    
    @Test
    public void testenhanceMultiRaggedDataset() throws IOException {
        System.out.println("----RaggedMultiConventions------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequestSingle, RaggedMultiConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "RaggedMultiConventions.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"height\">"));
        
       assertTrue("data missing",write.toString().contains("1990-01-01T00:00:00Z,6.7,0.5")); 
       assertTrue("data missing",write.toString().contains("1990-01-01T00:00:00Z,6.9,1.5"));                             
        
        System.out.println("----------end-----------");
    }
    public static final String timeSeriesProfileRequest2 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station1&eventTime=1990-01-01T00:00:00Z";

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

    
    public static final String timeSeriesTimeRequestT2 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station1&eventtime=1990-01-01T02:00:00Z";
public static final String timeSeriesTimeRequestT1 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station1&eventtime=1990-01-01T00:00:00Z";

    
    @Test
    public void testMultiDimensionalSingleStationsTimeTestT2() throws IOException {
        System.out.println("----MultiDimensionalSingleStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesTimeRequestT2, RaggedMultiConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalSingleStationsT2.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);       
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"height\">"));
        
        assertFalse("to much data",write.toString().contains("1990-01-01T00:00:00Z,6.7,0.5")); 
        assertFalse("to much data",write.toString().contains("1990-01-01T00:00:00Z,6.9,1.5")); 
        
       
        assertTrue("data missing",write.toString().contains("1990-01-01T02:00:00Z,6.7,0.5")); 
        assertTrue("data missing",write.toString().contains("1990-01-01T02:00:00Z,7.0,1.5")); 
        System.out.println("----------end-----------");
    }

    @Test
    public void testMultiDimensionalSingleStationsTimeTestT1() throws IOException {
        System.out.println("----MultiDimensionalSingleStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(RaggedMultiConventions);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesTimeRequestT1, RaggedMultiConventions);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalSingleStationsT1.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"height\">"));
        
        assertTrue("to much data",write.toString().contains("1990-01-01T00:00:00Z,6.7,0.5")); 
        assertTrue("to much data",write.toString().contains("1990-01-01T00:00:00Z,6.9,1.5")); 
        
       
        assertFalse("data missing",write.toString().contains("1990-01-01T02:00:00Z,6.7,0.5")); 
        assertFalse("data missing",write.toString().contains("1990-01-01T02:00:00Z,7.0,1.5")); 
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
    public static final String timeSeriesProfileRequest3 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=Station2&eventTime=1990-01-01T04:00:00Z";

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
        String fileName = "MultiDimensionalMultiStations2.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
    }
    
    @Test
    public void testMultiDimensionalMultiStation() throws IOException {
        System.out.println("----MultiDimensionalMultiStations------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(MultiDimensionalMultiStations);

        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, timeSeriesProfileRequestMultiStation, MultiDimensionalMultiStations);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "MultiDimensionalMultiStationRequest.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
        System.out.println("----------end-----------");
        
        assertTrue("Station 1 T0A0",write.toString().contains("1990-01-01T00:00:00Z,0.0,0.0")); 
        assertTrue("Station 1 T0A1",write.toString().contains("1990-01-01T00:00:00Z,0.7000000000000001,17.5"));         
        assertTrue("Station 1 T0A2",write.toString().contains("1990-01-01T00:00:00Z,1.6,40")); 
        //
        assertTrue("Station 1 T3A0",write.toString().contains("1990-01-01T03:00:00Z,9.0,225.0")); 
        assertTrue("Station 1 T3A1",write.toString().contains("1990-01-01T03:00:00Z,9.3,232.5"));         
        assertTrue("Station 1 T3A2",write.toString().contains("1990-01-01T03:00:00Z,11.4,285.0")); 
        assertTrue("Station 1 T3A3",write.toString().contains("1990-01-01T03:00:00Z,11.8,295"));         
        
        assertTrue("Station 2 T0A0",write.toString().contains("1990-01-01T04:00:00Z,12.0,300.0")); 
        assertTrue("Station 2 T0A1",write.toString().contains("1990-01-01T04:00:00Z,13.5,337.5"));         
        assertTrue("Station 2 T0A2",write.toString().contains("1990-01-01T04:00:00Z,14.0,350")); 
        assertTrue("Station 2 T3A0",write.toString().contains("1990-01-01T04:00:00Z,14.9,372.5")); 
        //
        assertTrue("Station 2 T3A1",write.toString().contains("1990-01-01T05:00:00Z,15.0,375.0"));         
        assertTrue("Station 2 T3A2",write.toString().contains("1990-01-01T05:00:00Z,16.3,407.5")); 
        assertTrue("Station 2 T3A3",write.toString().contains("1990-01-01T05:00:00Z,17.8,445.0")); 
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
        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequestMultiTime3, ContiguousRaggedMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "ContiguousRaggedMultipleProfilesMultiTime3.xml";
        fileWriter(base, fileName, write);
        //dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"z\">"));   
        assertTrue("data missing",write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_3\"/>"));
        assertFalse("data missing",write.toString().contains("1990-01-01T01:00:00Z,")); 
        assertFalse("data missing",write.toString().contains("1990-01-01T02:00:00Z,")); 
        System.out.println("----------end-----------");
    }
     
    
    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequestMultiTime, ContiguousRaggedMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "ContiguousRaggedMultipleProfilesMultiTime.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"z\">"));   
        assertFalse("data missing",write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_0\"/>"));
        assertTrue("data missing",write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_1\"/>"));
        assertTrue("data missing",write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_2\"/>"));
        assertTrue("data missing",write.toString().contains("<om:featureOfInterest xlink:href=\"PROFILE_3\"/>"));
        assertTrue("data missing",write.toString().contains("1990-01-01T01:00:00Z,")); 
        assertTrue("data missing",write.toString().contains("1990-01-01T02:00:00Z,")); 
        
        System.out.println("----------end-----------");
    }
    
    @Test
    public void testContiguousRaggedMultipleProfilesMultiTime2() throws IOException {
        spaceBetweenTests();
        System.out.println("----ContiguousRaggedMultipleProfiles------");
        NetcdfDataset dataset = NetcdfDataset.openDataset(ContiguousRaggedMultipleProfiles);
        MetadataParser md = new MetadataParser();
        Writer write = new CharArrayWriter();
        md.enhance(dataset, write, profileRequestMultiTime2, ContiguousRaggedMultipleProfiles);
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "ContiguousRaggedMultipleProfilesMultiTime2.xml";
        fileWriter(base, fileName, write);
        dataAvailableInOutputFile(write);
        //check depth was entered auto
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"z\">"));   
        assertFalse("data missing",write.toString().contains("1990-01-01T00:00:00Z,")); 
        assertTrue("data missing",write.toString().contains("1990-01-01T01:00:00Z,")); 
        assertTrue("data missing",write.toString().contains("1990-01-01T02:00:00Z,")); 
        
        System.out.println("----------end-----------");
    }
    
    
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
        assertTrue("depth not added",write.toString().contains("<swe:field name=\"alt\">"));
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
