/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thredds.server.sos.getObs;

import java.io.IOException;
import static org.junit.Assert.*;
import thredds.server.sos.bean.Extent;
import ucar.nc2.dataset.NetcdfDataset;
import thredds.server.sos.util.ThreddsExtentUtil;
import org.junit.Test;
import thredds.server.sos.service.DatasetMetaData;

import thredds.server.sos.util.XMLDomUtils;
/**
 *
 * @author abird
 */
public class MockGetObservationParserTest {


    @Test
    public void testTemplateFileLocation() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     MockGetObservationParser MockGetCapP= new MockGetObservationParser(dst);

     String serverLocation = MockGetCapP.getTemplateLocation();

     assertEquals("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/webapps/thredds/templates/sosGetCapabilities.xml", serverLocation);

    }


    @Test
    public void testVIMSGetRequestedStationFromMultiStationCollection() throws IOException, Exception {
       //setup netcdf file
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     //get data
     dst.extractData();
     //set temp prop values
     String[] observedProperty={"msl"};
     
     String[] time={"2008-09-07T11:31:00Z","2008-09-14T18:30:00Z"};
     dst.setSearchTimes(time, true);
     dst.setRequestedStationName("urn:sura:station:sura.tds.sos:8726724");
     dst.setDatasetArrayValues(observedProperty);
     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);

        double c = dst.getObsPropVarARRAYData().get(0).getDouble(0);
        double d = dst.getObsPropVarARRAYData().get(0).getDouble(1);
        double e = dst.getObsPropVarARRAYData().get(0).getDouble(2);

   }


     @Test
    public void testMultiTimeNotFound() throws IOException, Exception {
        //setup netcdf file
        NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/Station_42056_2008met.nc");
        Extent ext = ThreddsExtentUtil.getExtent(dataset);
        DatasetMetaData dst = new DatasetMetaData(ext, dataset);
        //get data
        dst.extractData();
        //set temp prop values
        String[] observedProperty = {"air_temp"};
        dst.setDatasetArrayValues(observedProperty);

        String[] time = {"2007-12-31T19:50:00Z", "2008-12-31T17:49:00Z"};
        dst.setSearchTimes(time, true);

        MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
        MockGetObs.parseTemplateXML();
        MockGetObs.parseObservations(observedProperty);

        assertTrue(MockGetObs.isSearchTimeAvailable());

        String values = MockGetObs.getResultValues();
        assertFalse(values.contains("TIME ERROR"));
        assertFalse(values.contains("ERROR!"));
        XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "C:/Station_42056_2008metTIMETEST.xml");

    }


    @Test
    public void testGetObsCanCreateDom() {
        MockGetObservationParser MockGetObs = new MockGetObservationParser();
        MockGetObs.parseTemplateXML();
        assertTrue(MockGetObs.getDom() != null);
    }

    @Test
    public void testGetSystemIDFromXMLTemplate() {
        MockGetObservationParser MockGetObs = new MockGetObservationParser();
        MockGetObs.parseTemplateXML();
        String gmlId = MockGetObs.getSystemGMLID();
        assertEquals("SPOT5_DATA", gmlId);
    }

    @Test
    public void testSetSystemIDFromXMLTemplate() {
        MockGetObservationParser MockGetObs = new MockGetObservationParser();
        MockGetObs.parseTemplateXML();
        MockGetObs.setSystemGMLID("GMLID VALUE");
        String GMLID = MockGetObs.getSystemGMLID();
        assertEquals("GMLID VALUE", GMLID);
    }

    @Test
    public void testSetSystemGMLIDFromStationNetCDFFile() throws Exception {
    MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
    MockGetObs.parseTemplateXML();
    MockGetObs.setSystemGMLID();
    String GMLID = MockGetObs.getSystemGMLID();
    assertEquals("station-42059", GMLID);
    }

    public DatasetMetaData getdst() throws Exception {
        NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
        Extent ext = ThreddsExtentUtil.getExtent(dataset);
        DatasetMetaData dst = new DatasetMetaData(ext, dataset);
        dst.extractData();
        return dst;
    }

    @Test
    public void testSetDescriptionFromNETCDFFILE() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     MockGetObs.setCollectionDescription();
     String descrip = MockGetObs.getCollectionDescription();
     assertEquals("Station 42059 - Eastern Caribbean", descrip);
    }

    @Test
    public void testSetNameFromNetCDFFile() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     MockGetObs.setCollectionName();
     String descrip = MockGetObs.getCollectionName();
     assertEquals("NOAA NDBC Station 42059 for 2008 (meterological)", descrip);
    }

    @Test
    public void testSetBoundedBySourceName() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     MockGetObs.setCollectionSourceName();
     String descrip = MockGetObs.getCollectionSourceName();
     assertEquals("urn:sura:station:sura.tds.sos:42059", descrip);
    }

    @Test
    public void testSetEnvelopeLowerLatLon() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     MockGetObs.setCollectionLowerCornerEnvelope();
     String descrip = MockGetObs.getCollectionLowerCornerEnvelope();
     assertEquals("15.006 -67.496 0", descrip);
    }

    @Test
    public void testSetEnvelopeUpperLatLon() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     MockGetObs.setCollectionUpperCornerEnvelope();
     String descrip = MockGetObs.getCollectionUpperCornerEnvelope();
     assertEquals("15.006 -67.496 0", descrip);
    }

    @Test
    public void testCheckObservationOfferingsForObsPropertyNOTValid() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();

     String[] observedProperty={"air_temperature"};
     boolean isContainedValidOption = MockGetObs.parseObsListForRequestedProperty(observedProperty);
     assertFalse(isContainedValidOption);

    }

    @Test
    public void testCheckObservationOfferingsForObsPropertyISValid() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();

     String[] observedProperty={"air_temp"};
     boolean isContainedValidOption = MockGetObs.parseObsListForRequestedProperty(observedProperty);
     assertTrue(isContainedValidOption);
    }

    @Test
    public void testGetCreatedObsNodeValid() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);

     String node = "om:Observation";
      String container = "om:member";
    assertTrue(MockGetObs.isNodeAvailable(node,container));
    }


    @Test
    public void testGetCreatedObsDescriptionNode() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);

     String descrip = MockGetObs.getObservationDescription();
     assertEquals("Station 42059 - Eastern Caribbean", descrip);
    }

    @Test
    public void testGetCreatedObsNameNode() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);

     String name = MockGetObs.getObservationName();
     assertEquals("Station 42059 - Eastern Caribbean", name);
    }


    @Test
    public void testCreateBoundedBy() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);
      String container = "om:Observation";

     assertTrue(MockGetObs.isNodeAvailable("gml:boundedBy",container));
    }

    @Test
    public void testCreateBoundedByEnvelopeSRSName() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);

     String name = MockGetObs.getObservationEnvelopeSrsName();
     assertEquals("urn:sura:station:sura.tds.sos:42059", name);
    }


    @Test
    public void testCreateBoundedByGPSCoorsLowerCorner() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);

     String name = MockGetObs.getObservationLowerCorner();
     assertEquals("15.006 -67.496 0", name);
    }

    @Test
    public void testCreateBoundedByGPSCoorsUpperCorner() throws Exception {
    MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
    String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);

     String name = MockGetObs.getObservationUpperCorner();
     assertEquals("15.006 -67.496 0", name);
    }

    @Test
    public void testCheckCreateSamplingTime() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);
     String container = "om:Observation";

     assertTrue(MockGetObs.isNodeAvailable("om:samplingTime",container));
    }

     @Test
    public void testCreateSamplingTimeInstantType() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);

     String name = MockGetObs.getObservationTimeInstant();
     assertEquals("DATA_TIME", name);
    }

    @Test
    public void testCreateTimePosition() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);

     String name = MockGetObs.getObservationTimePosition();
     assertEquals("2008-01-01T00:50:00Z", name);
    }

    @Test
    public void testCreateProcedure() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);
     String container = "om:Observation";

     assertTrue(MockGetObs.isNodeAvailable("om:procedure",container));    
    }

    @Test
    public void testCheckProcedure() throws Exception {
    MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);

     String name = MockGetObs.getObservationProcedure();
     assertEquals("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc", name);
    }

    @Test
    public void testAddObservedPropertyNode() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
    String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);
     String container = "om:Observation";

     assertTrue(MockGetObs.isNodeAvailable("om:observedProperty",container));
    }

    @Test
    public void testAddFeatureOfInterest() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);
     String container = "om:Observation";
     assertTrue(MockGetObs.isNodeAvailable("om:featureOfInterest",container));
    }

    @Test
    public void testCheckObservedProperty() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);
     String name = MockGetObs.getObservationObservedProperty();
     assertEquals("http://marinemetadata.org/cf#"+observedProperty[0], name);
    }



    @Test
    public void testAddResult() throws Exception {
     MockGetObservationParser MockGetObs = new MockGetObservationParser(getdst());
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     MockGetObs.parseObservations(observedProperty);
     String container = "om:Observation";
     assertTrue(MockGetObs.isNodeAvailable("om:result",container));
    }

    @Test
    public void testNEEDTOADDMORETESTS() throws Exception {
     //fail("Need to add data block!!!!!");
    }

    @Test
    public void testCreateValuesParserNodeStringErrorObs() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"sea_water_blahblah"};
     dst.setDatasetArrayValues(observedProperty);

     String values = MockGetObs.createObsValuesString();
     assertEquals("ERROR!", values);

    }


    @Test
    public void testCreateValuesParserNodeStringERROR() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"sea_surface_height"};
     dst.setDatasetArrayValues(observedProperty);

     String values = MockGetObs.createObsValuesString();
     assertTrue(values!=null);
     assertTrue(values.equalsIgnoreCase("ERROR!"));
    }

     @Test
    public void testCreateValuesParserNodeString() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     String[] observedProperty={"air_temp"};
     dst.setDatasetArrayValues(observedProperty);

     String values = MockGetObs.createObsValuesString();
     assertTrue(values!=null);
     assertFalse(values.equalsIgnoreCase("ERROR!"));
    }

     @Test
    public void testCreateAndAddValue() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     String[] observedProperty={"air_temp"};
     dst.setDatasetArrayValues(observedProperty);
         
     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
    
     MockGetObs.parseObservations(observedProperty);
     String container = "om:Observation";
     assertTrue(MockGetObs.isNodeAvailable("swe:values",container));
    }

    @Test
    public void testTimeObsSNULL() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);

     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();

     //set the single time string array
     dst.setSearchTimes(null,false);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     assertFalse(MockGetObs.isSearchTimeAvailable());

    }

    @Test
    public void testTimeObsSingleCheck() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);

     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();

     //set the single time string array
     String[] multiTime={"temp"};
     dst.setSearchTimes(multiTime,false);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     assertTrue(MockGetObs.isSearchTimeAvailable());

    }

    @Test
    public void testTimeObsMultiCheck() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);

     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();

     //seet the time strings
     String[] multiTime={"2 times","Asdsasdasd"};

     dst.setSearchTimes(multiTime,true);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
    }


//-----------------------------------------------------------------------------
    @Test
    public void testWriteDomToFile() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     String[] observedProperty={"air_temp"};
     dst.setDatasetArrayValues(observedProperty);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "c:/getObs.xml");
    }

    @Test
    public void testWriteDomToFile2() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     String[] observedProperty={"air_temp"};
     dst.setDatasetArrayValues(observedProperty);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "c:/getObs2.xml");
    }

    @Test
    public void testWriteDomToFile2Props() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     String[] observedProperty={"sea_temp","wspd"};
     dst.setDatasetArrayValues(observedProperty);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "c:/getObs2PropsStation.xml");
    }

    @Test
    public void testWriteDomToFileNDBC() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/NOAA_NDBC_42035_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     String[] observedProperty={"sea_temp","wspd"};
     dst.setDatasetArrayValues(observedProperty);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "c:/getObs2PropsNDBC.xml");
    }

     @Test
    public void testWriteDomToFileEPA() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A07.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     String[] observedProperty={"temp","salt","chl"};
     dst.setDatasetArrayValues(observedProperty);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "c:/getObs3PropsEPA.xml");
    }

       @Test
    public void testWriteDomToFileEPACheckDepth() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A07.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     String[] observedProperty={"temp","salt","chl"};
     dst.setDatasetArrayValues(observedProperty);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     assertTrue(MockGetObs.getIsDepth());
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "c:/getObs3PropsEPA.xml");
    }

     @Test
    public void testWriteDomToFileEPAjustDepth() throws Exception {
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_A07.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     dst.extractData();
     String[] observedProperty={"temp"};
     dst.setDatasetArrayValues(observedProperty);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     assertTrue(MockGetObs.getIsDepth());
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "c:/getObs3PropsEPADepth.xml");
    }

     @Test
    public void testWriteDomToFileStationSingleTime() throws Exception {
     //setup netcdf file
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     //get data
     dst.extractData();
     //set temp prop values
     String[] observedProperty={"air_temp"};
     dst.setDatasetArrayValues(observedProperty);

     String[] time={"2008-01-01T01:49:59Z"};
     dst.setSearchTimes(time, false);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);

     assertTrue(MockGetObs.isSearchTimeAvailable());
      
     String values = MockGetObs.getResultValues();
     //assertTrue(values.length()>0);
     assertEquals("42059,2008-01-01T01:49:59Z,15.006,-67.496,27.1", values);
    }

    @Test
    public void testWriteDomToFileStationMultiTime() throws Exception {
     //setup netcdf file
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     //get data
     dst.extractData();
     //set temp prop values
     String[] observedProperty={"air_temp"};
     dst.setDatasetArrayValues(observedProperty);

     String[] time={"2008-01-01T00:50:00Z","2008-01-01T01:49:59Z"};
     dst.setSearchTimes(time, true);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);

     assertTrue(MockGetObs.isSearchTimeAvailable());

     String values = MockGetObs.getResultValues();

     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "c:/4059MultiTimeStation.xml");
     assertEquals("42059,2008-01-01T00:50:00Z,15.006,-67.496,26.5 \n42059,2008-01-01T01:49:59Z,15.006,-67.496,27.1 "+"\n", values);
     
     }

    @Test
    public void testWriteDomToFileStationMultiTimeAndMultiProps() throws Exception {
     //setup netcdf file
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     //get data
     dst.extractData();
     //set temp prop values
     String[] observedProperty={"air_temp","apd"};
     dst.setDatasetArrayValues(observedProperty);

     String[] time={"2008-01-01T00:50:00Z","2008-01-01T01:49:59Z"};
     dst.setSearchTimes(time, true);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);

     assertTrue(MockGetObs.isSearchTimeAvailable());

     String values = MockGetObs.getResultValues();

     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "c:/4059MTimeStationMProps.xml");
     assertEquals("42059,2008-01-01T00:50:00Z,15.006,-67.496,26.5,5.19 \n42059,2008-01-01T01:49:59Z,15.006,-67.496,27.1,5.13 \n", values);

     }

    @Test
    public void testWriteDomToFileKyleNewMay() throws Exception {
     //setup netcdf file
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/WT6.1.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     //get data
     dst.extractData();
     //set temp prop values
     String[] observedProperty={"PN_43f6","WTEMP_e23e"};
     dst.setDatasetArrayValues(observedProperty);

     String[] time={"1990-07-18T07:45:00Z","2006-10-16T10:10:00Z"};
     dst.setSearchTimes(time, true);

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);

     assertTrue(MockGetObs.isSearchTimeAvailable());

     String values = MockGetObs.getResultValues();
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "C:/WT6.1.xml");
     }

    @Test
    public void testVIMSMultipleStationsExist() throws IOException, Exception {

     //setup netcdf file
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     //get data
     dst.extractData();
     //set temp prop values
     String[] observedProperty={"msl"};
     dst.setRequestedStationName("urn:sura:station:sura.tds.sos:8726724");
     dst.setDatasetArrayValues(observedProperty);

     String[] time={"2008-09-07T11:31:00Z","2008-09-14T18:30:00Z"};
     dst.setSearchTimes(time, true);
     

     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);

     assertTrue(MockGetObs.isSearchTimeAvailable());
     String values = MockGetObs.getResultValues();
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "C:/Vims.xml");

     String[] stationNames = dst.getStringStationNames();

     assertEquals("8724698", stationNames[0]);
     assertEquals("8726724", stationNames[1]);

    }


    @Test
    public void testVIMSGetRequestedStationFromMultiStationCollectionSourceName() throws IOException, Exception {
       //setup netcdf file
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     //get data
     dst.extractData();
     //set temp prop values
     String[] observedProperty={"msl"};
      dst.setRequestedStationName("urn:sura:station:sura.tds.sos:8726724");
     dst.setDatasetArrayValues(observedProperty);
     String[] time={"2008-09-07T11:31:00Z","2008-09-14T18:30:00Z"};
     dst.setSearchTimes(time, true);
    
     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "C:/VimsStation.xml");

     assertEquals(MockGetObs.getCollectionSourceName(), "urn:sura:station:sura.tds.sos:8726724");

    }

      @Test
    public void testVIMSGetRequestedStationFromMultiStationCollectionNULLCheck() throws IOException, Exception {
       //setup netcdf file
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     //get data
     dst.extractData();
     dst.setRequestedStationName(null);
     //set temp prop values
     String[] observedProperty={"msl"};
     dst.setDatasetArrayValues(observedProperty);
     String[] time={"2008-09-07T11:31:00Z","2008-09-14T18:30:00Z"};
     dst.setSearchTimes(time, true);
     
     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "C:/VimsStation.xml");
     assertEquals(MockGetObs.getCollectionSourceName(), "urn:sura:station:sura.tds.sos:8724698");
     }




   @Test
    public void testVIMSGetRequestedStationFromMultiStationCollectionLatLonCheck() throws IOException, Exception {
       //setup netcdf file
     NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
     Extent ext = ThreddsExtentUtil.getExtent(dataset);
     DatasetMetaData dst = new DatasetMetaData(ext, dataset);
     //get data
     dst.extractData();
     //set temp prop values
     String[] observedProperty={"msl"};
     dst.setRequestedStationName("urn:sura:station:sura.tds.sos:8726724");
     dst.setDatasetArrayValues(observedProperty);
     String[] time={"2008-09-07T11:31:00Z","2008-09-14T18:30:00Z"};
     dst.setSearchTimes(time, true);
     
     MockGetObservationParser MockGetObs = new MockGetObservationParser(dst);
     MockGetObs.parseTemplateXML();
     MockGetObs.parseObservations(observedProperty);
     XMLDomUtils.writeXMLDOMToFile(MockGetObs.getDom(), "C:/VimsStation.xml");
     assertEquals(MockGetObs.getCollectionSourceName(), "urn:sura:station:sura.tds.sos:8726724");    

     assertEquals("27.9783 -82.8317 0", MockGetObs.getCollectionLowerCornerEnvelope());
     assertEquals("27.9783 -82.8317 0", MockGetObs.getCollectionUpperCornerEnvelope());
     
   }

  

}
