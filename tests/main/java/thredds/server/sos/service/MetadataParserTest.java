/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thredds.server.sos.service;

import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.Test;
import ucar.nc2.dataset.NetcdfDataset;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @author abird
 */
public class MetadataParserTest {


    @Test
    public void CanGetWriterContainingData() throws IOException {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A04.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,null);

    writer.flush();
    writer.close();

    assertTrue(writer!=null);
    System.out.println(writer.toString());
    }

    @Test
    public void testNullQueryStringResultInGetCaps() throws IOException  {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A04.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,null);

    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()!=null);
    writer.flush();
    writer.close(); 
    }

    @Test
    public void testGetCapsQueryStringResultInGetCaps() throws IOException {
    String query = "service=SOS&version=1.0.0&request=GetCapabilities";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A04.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);

    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()!=null);
    writer.flush();
    writer.close();

    }

    @Test
    public void testSplitterCheckService() {
    String query = "service=SOS&version=1.0.0&request=GetCapabilities";
    MetadataParser.splitQuery(query);
    assertEquals("SOS", MetadataParser.getService());
    }

    @Test
    public void testSplitterCheckVersion() {
     String query = "service=SOS&version=1.0.0&request=GetCapabilities";
    MetadataParser.splitQuery(query);
    assertEquals("1.0.0", MetadataParser.getVersion());
    }

    @Test
    public void testSplitterCheckRequest() {
     String query = "service=SOS&version=1.0.0&request=GetCapabilities";
    MetadataParser.splitQuery(query);
    assertEquals("GetCapabilities", MetadataParser.getRequest());
    }

    @Test
    public void testGetCapsUsingSplitString() throws IOException {
    String query = "service=SOS&version=1.0.0&request=GetCapabilities";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A04.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);

    assertEquals("GetCapabilities", MetadataParser.getRequest());

    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()!=null);
    writer.flush();
    writer.close();    
    }

    @Test
    public void testInvalidQueryStringNULLRequest() throws IOException {
    String query = "service=SOS&version=1.0.0&";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A04.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);

    assertTrue(MetadataParser.getRequest() ==null);
    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()==null);
    writer.flush();
    writer.close();
    }

     @Test
    public void testInvalidQueryStringInvalidRequest() throws IOException {
    String query = "service=SOS&version=1.0.0&request=somethingotherthanwhatiwant";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A04.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);

    assertFalse(MetadataParser.getRequest().equalsIgnoreCase("getcapabilities"));
    assertEquals("somethingotherthanwhatiwant", MetadataParser.getRequest());
    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()==null);
    writer.flush();
    writer.close();
    }

    @Test
    public void testInvalidQueryStringInvalidTwoFields() throws IOException {
    String query = "version=1.0.0";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A04.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);

    assertTrue(MetadataParser.getRequest()==null);

    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()==null);
    writer.flush();
    writer.close();
    }

    @Test
    public void testInvalidQueryStringInvalidRequestString() throws IOException {
    String query = "service=SOS&version=1.0.0&request=GetCapabil";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A04.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);

    
    assertEquals("GetCapabil", MetadataParser.getRequest());
    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()==null);
    writer.flush();
    writer.close();
    }

    @Test
    public void testSplitterCheckObsProperty() {
    String query = "service=SOS&version=1.0.0&request=GetObservation&observedProperty=air_tempss";
    MetadataParser.splitQuery(query);
    assertEquals("air_tempss", MetadataParser.getObservedProperties()[0]);
    }

    @Test
    public void testKyleGUIRequest() {
        String query = "request=GetObservation&service=SOS&version=1.0.0&responseFormat=text%2Fxml%3B+subtype%3D%22om%2F1.0.0%22&offering=urn%3Asura%3Astation%3Asura.tds.sos%3A42099&observedproperty=sea_surface_height,wind_speed&eventtime=2008-12-28T15:37:00Z/2008-12-31T18:34:00Z";
        MetadataParser.splitQuery(query);

        assertEquals("GetObservation", MetadataParser.getRequest());

        assertEquals("sea_surface_height,wind_speed", MetadataParser.getObservedProperty());

        assertEquals("2008-12-28T15:37:00Z", MetadataParser.getEventTime()[0]);
        assertEquals("2008-12-31T18:34:00Z", MetadataParser.getEventTime()[1]);

    }

    @Test
    public void testMultipleObsProperties() {
        String query = "observedproperty=sea_surface_height,wind_speed";
        MetadataParser.splitQuery(query);

        assertTrue(MetadataParser.getObservedProperties().length==2);
        String[] ans = MetadataParser.getObservedProperties();
        assertEquals("sea_surface_height", ans[0]);
        assertEquals("wind_speed", ans[1]);
    }

    @Test
    public void testMultiple3obsProps() {
         String query = "request=GetObservation&service=SOS&observedproperty=temp,salt,chl";
        MetadataParser.splitQuery(query);

        assertTrue(MetadataParser.getObservedProperties().length==3);
        String[] ans = MetadataParser.getObservedProperties();
        assertEquals("temp", ans[0]);
        assertEquals("salt", ans[1]);
        assertEquals("chl", ans[2]);
    }

    @Test
    public void testMultipleTimeProperties() {
       String query = "eventtime=2008-12-28T15:37:00Z/2008-12-31T18:34:00Z&request=GetObservation&service=SOS&observedproperty=temp,salt,chl";
       MetadataParser.splitQuery(query);
       String[] ans = MetadataParser.getEventTime();
       assertEquals("2008-12-28T15:37:00Z", ans[0]);
       assertEquals("2008-12-31T18:34:00Z", ans[1]);
    }

    @Test
    public void testSingleEventTime() {
       String query = "eventtime=2008-12-28T15:37:00Z&request=GetObservation&service=SOS&observedproperty=temp,salt,chl";
       MetadataParser.splitQuery(query);
       assertFalse(MetadataParser.isMultiEventTime());

       String[] ans = MetadataParser.getEventTime();
       assertEquals("2008-12-28T15:37:00Z", ans[0]);
    }

    @Test
    public void testEDCQueryString() {
        String query = "request=GetObservation&service=SOS&version=1.0.0&responseFormat=text%2Fxml%3B+subtype%3D%22om%2F1.0.0%22&offering=urn%3Asura%3Astation%3Asura.tds.sos%3A42056&observedproperty=sea_temp,sig_wvht&eventtime=2008-06-01T03:05:00Z/2008-07-27T02:54:00Z";

       MetadataParser.splitQuery(query);
       assertTrue(MetadataParser.isMultiEventTime());

       //check request
       String request = MetadataParser.getRequest();
       assertEquals("GetObservation", request);

       //check service
       String service = MetadataParser.getService();
       assertEquals("SOS", service);

       //check version
       String version = MetadataParser.getVersion();
       assertEquals("1.0.0", version);

       String offering = MetadataParser.getOffering();
       assertEquals("urn:sura:station:sura.tds.sos:42056", offering);


       //check props
       String[] obsProp = MetadataParser.getObservedProperties();
       assertEquals("sea_temp",obsProp[0]);
       assertEquals("sig_wvht",obsProp[1]);

       //check event time
       String[] eTime = MetadataParser.getEventTime();
       assertEquals("2008-06-01T03:05:00Z", eTime[0]);
       assertEquals("2008-07-27T02:54:00Z", eTime[1]);

    }


    @Test
    public void testEDCDataFileAndEnhance() throws IOException {
    String query = "request=GetObservation&service=SOS&version=1.0.0&responseFormat=text%2Fxml%3B+subtype%3D%22om%2F1.0.0%22&offering=urn%3Asura%3Astation%3Asura.tds.sos%3A42056&observedproperty=sea_temp,sig_wvht&eventtime=2008-06-01T03:05:00Z/2008-07-27T02:54:00Z";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42056_2008met.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);


    assertEquals("GetObservation", MetadataParser.getRequest());
    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()==null);
    writer.flush();
    writer.close();
    }

    @Test
    public void testMultiSearchTimesWork() throws IOException {
    String query = "&service=SOS&version=1.0.0&request=GetObservation&service=SOS&observedproperty=air_temp&eventtime=2007-12-31T19:50:00Z/2008-12-31T17:49:00Z";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42056_2008met.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);

    assertEquals("GetObservation", MetadataParser.getRequest());
    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()==null);
    writer.flush();
    writer.close();
    }

    @Test
    public void testVimsMultiStation() throws IOException {
    String query = "request=GetObservation&service=SOS&version=1.0.0&responseFormat=text%2Fxml%3B+subtype%3D%22om%2F1.0.0%22&offering=urn%3Asura%3Astation%3Asura.tds.sos%3A8726724&observedproperty=msl&eventtime=2008-09-07T11:31:00Z/2008-09-14T18:30:00Z";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);

    assertEquals("GetObservation", MetadataParser.getRequest());
    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()==null);

    //final close and check
    writer.flush();
    writer.close();
    }

    @Test
    public void testVimsError() throws IOException {
        
     String query = "service=SOS&version=1.0.0&request=GetObservation&observedProperty=msl";

    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Writer writer = new StringWriter();

    MetadataParser.enhance(dataset, writer,query);

    assertEquals("GetObservation", MetadataParser.getRequest());
    assertTrue(MetadataParser.getxmlString()!=null);
    assertTrue(MetadataParser.getGetCapsParser()==null);

    //final close and check
    writer.flush();
    writer.close();
    }


}