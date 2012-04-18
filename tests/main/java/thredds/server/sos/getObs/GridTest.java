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
 * @author abird
 */
public class GridTest {
    
    //***********************************************
    public static final String GridReq1 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=mcsst&eventtime=1990-01-01T00:00:00Z/2012-05-17T09:57:00.000-04:00";
    public static final String GridReq2 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=mcsst&offering=mcsst&eventtime=1990-01-01T00:00:00Z/2012-05-17T09:57:00.000-04:00&lat=29.88603303&lon=-89.0087125";
    public static final String GridReq3 = "request=GetObservation&version=1.0.0&service=sos&observedProperty=temperature&offering=mcsst&eventtime=1990-01-01T00:00:00Z/2012-05-17T09:57:00.000-04:00&lat=29.0&lon=-89.0";
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
    public void testGetObsGridWithoutLatLon() throws IOException {
        System.out.println("----GRID1------");
        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
        Writer write = new CharArrayWriter();
        SOSParser.enhance(dataset, write, GridReq1, "D:/Data/20120417.108.1357.n16.EC1.nc");
        write.flush();
        write.close();
        assertTrue(write.toString().contains("Exception"));
        String fileName = "Gridded1.xml";
        fileWriter(base, fileName, write);
    }

    @Test
    public void testGetObsGridWithLatLon() throws IOException {
        System.out.println("----GRID2------");
        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
        Writer write = new CharArrayWriter();
        SOSParser.enhance(dataset, write, GridReq2, "D:/Data/20120417.108.1357.n16.EC1.nc");
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));

        String fileName = "Gridded2.xml";
        fileWriter(base, fileName, write); 
    }

    @Test
    public void testGetObsGridValuesCorrect() throws IOException {
        System.out.println("----GRID2.1------");
        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
        //("2012-04-17T13:57:00Z,22.8,29.886033032786887,-89.00871255834629 \n"));
        Map<String, String> latLonRequest = new HashMap<String, String>();
        latLonRequest.put("lat", "29.88603303");
        latLonRequest.put("lon", "-89.0087125");

        SOSGetObservationRequestHandler handler = new SOSGetObservationRequestHandler(dataset,new String[]{"mcsst"} , new String[]{"mcsst"}, new String[]{"1990-01-01T00:00:00Z","2012-05-17T09:57:00.000-04:00"}, latLonRequest);
        handler.parseObservations();
  
        String value = XMLDomUtils.getNodeValue(handler.getDocument(),"swe:DataArray", "swe:values");
        assertEquals("2012-04-17T13:57:00Z,22.8,29.886033032786887,-89.00871255834629 \n",value);
    }

    @Test
    public void testGetObsGridWithLatLonTemperature() throws IOException {
        System.out.println("----GRID3------");
        NetcdfDataset dataset = NetcdfDataset.openDataset("D:/Data/20120417.108.1357.n16.EC1.nc");
        Writer write = new CharArrayWriter();
        SOSParser.enhance(dataset, write, GridReq3, "D:/Data/20120417.108.1357.n16.EC1.nc");
        write.flush();
        write.close();
        assertFalse(write.toString().contains("Exception"));
        String fileName = "Gridded3.xml";
        fileWriter(base, fileName, write);
    }
}
