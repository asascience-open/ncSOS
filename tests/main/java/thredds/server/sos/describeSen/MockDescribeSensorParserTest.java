/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.describeSen;

import java.io.File;
import thredds.server.sos.bean.Extent;
import ucar.nc2.dataset.NetcdfDataset;
import thredds.server.sos.util.ThreddsExtentUtil;
import org.junit.Test;
import thredds.server.sos.service.DatasetMetaData;
import thredds.server.sos.util.XMLDomUtils;
import static org.junit.Assert.*;

/**
 *
 * @author abird
 */
public class MockDescribeSensorParserTest {

    //test that the DOM can load
    @Test
    public void testLoadXMLDescribeSensorIntoDOM() {
        MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser();
        MockDesSen.parseTemplateXML();
        assertTrue(MockDesSen.getDom() != null);

    }

    @Test
    public void testGetSystemIDFromXMLTemplate() {
        MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser();
        MockDesSen.parseTemplateXML();
        String GMLID = MockDesSen.getSystemGMLID();
        assertEquals(getExpectedFileID(), GMLID);

    }

    @Test
    public void testSetSystemIDFromXMLTemplate() {
        MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser();
        MockDesSen.parseTemplateXML();
        MockDesSen.setSystemGMLID("fdgfdgfdgfd");
        String GMLID = MockDesSen.getSystemGMLID();
        assertEquals("fdgfdgfdgfd", GMLID);

    }

    @Test
    public void testSetSystemIDFromSTATIONNetCDF() throws Exception {
        MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser(getdst());
        MockDesSen.parseTemplateXML();
        MockDesSen.setSystemGMLID();
        String GMLID = MockDesSen.getSystemGMLID();
        assertEquals("station-42059", GMLID);

    }

    public String getExpectedFileID() {
        return "Buoy";
    }

    @Test
    public void testGetOrganizationName() {

        MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser();
        MockDesSen.parseTemplateXML();
        MockDesSen.setOrganizationName();
        assertEquals("ASA", MockDesSen.getOrganizationName());
    }

    @Test
    public void testGetOrganizationNameFromSTATIONNetCDF() throws Exception {
        MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser(getdst());
        MockDesSen.parseTemplateXML();
        MockDesSen.setOrganizationName();
        assertEquals("Owned and maintained by National Data Buoy Center", MockDesSen.getOrganizationName());
    }

    @Test
    public void testSetIdentifierName() throws Exception {
        MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser(getdst());
        MockDesSen.parseTemplateXML();      
        MockDesSen.setIdentifierName();
        assertEquals("StationId",MockDesSen.getIdentifierName());
    }

    @Test
    public void testGetIdentifierName() throws Exception {
        MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser(getdst());
        MockDesSen.parseTemplateXML();      
        assertEquals("StationId",MockDesSen.getIdentifierName());
    }

    @Test
    public void testSetIdentifierTermValue() throws Exception {
    MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser(getdst());
    MockDesSen.parseTemplateXML();

    // MockDesSen.setTermValue

    //assertEquals("urn:sura:station:sura.tds.sos:42902",MockDesSen.getTermValue());

    }
    

    public DatasetMetaData getdst() throws Exception {
        NetcdfDataset dataset = NetcdfDataset.openDataset("datasets/used/Station_42059_2008met.nc");
        Extent ext = ThreddsExtentUtil.getExtent(dataset);
        DatasetMetaData dst = new DatasetMetaData(ext, dataset);
        dst.extractData();
        return dst;
    }


    @Test
    public void testWriteDomToXMLFile() throws Exception {
    MockDescribeSensorParser MockDesSen = new MockDescribeSensorParser(getdst());
    MockDesSen.parseTemplateXML();
    String fileName = "C:/describeSensor.xml";
    XMLDomUtils.writeXMLDOMToFile(MockDesSen.getDom(),fileName);
    File xmlFile = new File(fileName);
    assertTrue(xmlFile!=null);
    }
}
