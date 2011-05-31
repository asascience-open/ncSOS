/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thredds.server.sos.service;

import java.util.ArrayList;
import java.util.List;
import thredds.server.sos.getCaps.ObservationOffering;
import thredds.server.sos.util.ThreddsExtentUtil;
import thredds.server.sos.bean.Extent;
import ucar.nc2.dataset.NetcdfDataset;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Abird
 */
public class DatasetMetaDataTest {

    @Test
    public void testGetAttribsFromDataSet() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.extractData();

    assertTrue(dst.getAttribs()!=null);
    }    

    @Test
    public void testgetTitleFromDataset() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.extractData();

    assertEquals("VIMS Selfe Predictions - Demo Test Case",dst.getTitle());
    }

    @Test
    public void testgetValidDescriptionFromDataset() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.extractData();

    assertEquals("Station 42059 - Eastern Caribbean",dst.getDescription());
    }

     @Test
    public void testgetValidDescriptionFromDatasetEPA() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/epa/epa+seamap_04-08_B07.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);
    dst.extractData();

    assertEquals("",dst.getDescription());
    }

    @Test
    public void testgetNullSetDescriptionFromDataset() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.extractData();

    assertEquals("",dst.getDescription());
    }


    @Test
    public void testCanGetStationNameList() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.parseDatasetObservations();
    
    String expected = (getExpectedStringStationNames()[10]);
    String actual = (dst.getStringStationNames()[10]);

    assertEquals(expected,actual);
    }

    private String[] getExpectedStringStationNames() {
    String[] StationStrings = {"8724698", "8726724", "8727333", "8727520", "8729210", "8729678", "8735180", "8735181", "8737048", "8737373",
    "8741041", "8741196", "8741533", "8742221", "8743281", "8744117", "8745557", "8747437", "8747766", "8760551", "8760922",
    "8760943", "8761305", "8761720", "8761724", "8761927", "8762075", "8762372", "8764025", "8764044", "8764227", "8764311",
    "8765251", "8766072", "8767816", "8767961", "8768094", "8770475", "8770520", "8770559", "8770570", "8770613", "8770743",
    "8770777", "8770933", "8770971", "8771013", "8771450", "8771510", "8772447", "8773701", "8774513", "8774770", "8775188",
    "8775237", "8775283", "8775296", "8775792", "8775870"};
    return StationStrings;
    }

    @Test
    public void testGetAlistOfObservationOfferingsUsingStationNames() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.parseDatasetObservations();

    dst.createAndAddObservationOfferings();
    assertTrue(dst.getObservationOfferingList()!=null);
    }

    @Test
    public void testGetListofObsOfferingChckSize() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.parseDatasetObservations();

    dst.createAndAddObservationOfferings();

    assertTrue(dst.getObservationOfferingList()!=null);
    assertTrue(dst.getObservationOfferingList().size()>5);
    assertTrue(dst.getObservationOfferingList().size()==59);
    }

     @Test
    public void testGetCorrectStationValueFromObs() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.parseDatasetObservations();

    dst.createAndAddObservationOfferings();

    //it is zero based
    ObservationOffering offering = dst.getObservationOfferingList().get(7);


     assertEquals("station-8735181",offering.getObservationStationID());
     }

    @Test
    public void testGetCorrectStationLat() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.parseDatasetObservations();

    assertEquals(30.25,dst.parseDoubleValue(dst.getStationLats().getDouble(6)),0);

    assertEquals(27.58,dst.parseDoubleValue(dst.getStationLats().getDouble(58)),0);


    assertEquals( 27.6333,dst.parseDoubleValue(dst.getStationLats().getDouble(57)),0);

     }

    @Test
    public void testGetCorrectStationLon() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.parseDatasetObservations();

    assertEquals(-86.865,dst.parseDoubleValue(dst.getStationLons().getDouble(5)),0);

    assertEquals( -97.21667,dst.parseDoubleValue(dst.getStationLons().getDouble(58)),0);

    assertEquals( -97.2367,dst.parseDoubleValue(dst.getStationLons().getDouble(57)),0);
     }

    @Test
    public void testGetCorrectDateString() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/content/thredds/public/testdata/VIMS.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    String isoDateStr = dst.parseDateString(ext._minTime);
    
    assertTrue(isoDateStr!=null);
    assertTrue(isoDateStr.contains("2008"));
    assertTrue(isoDateStr.contains("T"));
    assertEquals("2008-08-21T00:00:00Z", isoDateStr);

    }

    @Test
    public void testGetTestGMGLName() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.extractData();

    assertEquals("urn:sura:station:sura.tds.sos:42059",dst.getGMLName("42059"));
    }

    @Test
    public void testGetTheObservedProperties() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    dst.parseDatasetObservations();

    assertEquals(getExpectedObservationList(),dst.getObservedPropsList());
    }

    public List getExpectedObservationList(){
        List list_of_strings = new ArrayList ();
        list_of_strings.add("air_temp");
        list_of_strings.add("apd");
        list_of_strings.add("dew_point");
        list_of_strings.add("dom_wv_period");
        list_of_strings.add("gust");
        list_of_strings.add("mean_wv_dir");
        list_of_strings.add("sea_level_pressure");
        list_of_strings.add("sea_temp");
        list_of_strings.add("sig_wvht");
        list_of_strings.add("visibility");
        list_of_strings.add("water_level");
        list_of_strings.add("wdir");
        list_of_strings.add("wspd");
        return list_of_strings;
    }

    @Test
    public void testGetCorrectResponseFormat() throws Exception {
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);

    DatasetMetaData dst = new DatasetMetaData(ext, dataset);

    assertEquals(getExpectedFormatString(),dst.getFormatString());
    }

    private String getExpectedFormatString(){
        return   "text/xml; subtype=\"om/1.0.0\"";
    }

   @Test
    public void testGetObsArrays() throws Exception {
    //setup
    NetcdfDataset dataset = NetcdfDataset.openDataset("C:/Documents and Settings/abird/Desktop/netcdfTestFiles/station/Station_42059_2008met.nc");
    Extent ext = ThreddsExtentUtil.getExtent(dataset);
    //extract the data
    DatasetMetaData dst = new DatasetMetaData(ext, dataset);
    dst.extractData();
    //create
    String[] obsVal= {"air_temp"};
    dst.setDatasetArrayValues(obsVal);

    assertTrue(dst.getTimeVarData()!=null);
    assertTrue(dst.getObsPropVarData()!=null);

    assertTrue(dst.getObsPropVarARRAYData().size()==1);
    }





}