/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.getObs;

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
    
   private static String imedsLocation1 = "C:\\Program Files\\Apache Software Foundation\\Apache Tomcat 6.0.26\\content\\thredds\\public\\imeds\\watlev_TCOON.F.C.nc";
private static String imedsLocationNew = "C:\\Program Files\\Apache Software Foundation\\Apache Tomcat 6.0.26\\content\\thredds\\public\\imeds\\watlev_TCOON.F.C.new.nc";
// profile
private static String ContiguousRaggedMultipleProfiles = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/profile-Contiguous-Ragged-MultipleProfiles-H.3.4/profile-Contiguous-Ragged-MultipleProfiles-H.3.4.nc";
private static String IncompleteMultiDimensionalMultipleProfiles = "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2/profile-Incomplete-MultiDimensional-MultipleProfiles-H.3.2.nc";
//ragged Array - timeseries profile
private static String cfRaggedSingleConventions= "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Ragged-SingeStation-H.5.3/timeSeriesProfile-Ragged-SingeStation-H.5.3.nc";
private static String cfRaggedSingleConventionsNew= "C:/Documents and Settings/abird/My Documents/NetBeansProjects/cfpoint/CFPointConventions/timeSeriesProfile-Ragged-SingeStation-H.5.3/timeSeriesProfile-Ragged-SingeStation-H.5.3.new.nc";


    @Test
    public void testenhanceImedsNew() throws IOException {
      fail("not ready yet");
        NetcdfDataset dataset = NetcdfDataset.openDataset(imedsLocationNew);
          
      MetadataParser md = new MetadataParser();
      Writer write = new CharArrayWriter();
      md.enhance(dataset, write, "request=GetObservation&version=1&service=sos&observedProperty=watlev&offering=TCOON_87707771", imedsLocationNew);
      write.flush();
      write.close();
      assertFalse(write.toString().contains("Exception"));
   }
    
    
    //**********************************
//TIMESERIESPROFILE TEST
   @Test
    public void testenhanceSingleRaggedDataset() throws IOException {
      NetcdfDataset dataset = NetcdfDataset.openDataset(cfRaggedSingleConventions);
          
      MetadataParser md = new MetadataParser();
      Writer write = new CharArrayWriter();
      md.enhance(dataset, write, "request=GetObservation&version=1&service=sos&observedProperty=temperature&offering=Station1", cfRaggedSingleConventions);
      write.flush();
      write.close();
      assertFalse(write.toString().contains("Exception"));
   }
      
        @Test
    public void testenhanceSingleRaggedDatasetNew() throws IOException {
      NetcdfDataset dataset = NetcdfDataset.openDataset(IncompleteMultiDimensionalMultipleProfiles);
          
      MetadataParser md = new MetadataParser();
      Writer write = new CharArrayWriter();
      md.enhance(dataset, write, "request=GetCapabilities&version=1&service=sos", IncompleteMultiDimensionalMultipleProfiles);
      write.flush();
      write.close();
      assertFalse(write.toString().contains("Exception"));
   }

//**********************************
    
    
    
}
