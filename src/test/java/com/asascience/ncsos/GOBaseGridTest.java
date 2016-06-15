package com.asascience.ncsos;

import junit.framework.Assert;

import org.jdom.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asascience.ncsos.util.VocabDefinitions;

import java.io.File;
import java.net.URLEncoder;
import java.util.*;

@RunWith(Parameterized.class)
public class GOBaseGridTest extends NcSOSTest {

    private static HashMap<String,String> kvp = new HashMap<String, String>();
    private static String outputDir;
    private static String exampleDir;

    private Element currentFile;
    private String  procedure;
    private String  offering;
    private String  testType;
    private String  latitude;
    private String  longitude;
    private String  observedProperty;
    public GOBaseGridTest(Element file, String offering, String procedure, String observedProperty, String latitude, String longitude, String testType) {
        this.currentFile      = file;
        this.offering         = offering;
        this.procedure        = procedure;
        this.observedProperty = observedProperty;
        this.latitude         = latitude;
        this.longitude        = longitude;
        this.testType         = testType;
    }

    public static void setUpClass() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir  = baseOutputDir  +  NcSOSTest.systemSeparator + "GetObservation-Grid" + NcSOSTest.systemSeparator;
        exampleDir = baseExampleDir +  NcSOSTest.systemSeparator + "GetObservation-Grid" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        // IOOS not supported on GRIDs
        kvp.put("responseFormat", URLEncoder.encode("text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0\"", "UTF-8"));
        // Old OOSTETHYS
        kvp.put("responseFormat", URLEncoder.encode("text/xml;subtype=\"om/1.0.0\"", "UTF-8"));

        kvp.put("request",  "GetObservation");
        kvp.put("version", "1.0.0");
        kvp.put("service", "SOS");
    }

    // Create the parameters for the test constructor
    @Parameters
    public static Collection<Object[]> testCases() throws Exception {
        setUpClass();

        // Ignore GRID datasets
        int grids = 0;
        for (Element e : new ArrayList<Element>(fileElements)) {
            if (!e.getAttributeValue("feature").toLowerCase().equals("grid")) {
                fileElements.remove(e);
            } else {
                for (Element s : (List<Element>) e.getChildren("sensor")) {
                    grids = grids +  s.getChildren("values").size();
                }
            }
        }

        Object[][] data = new Object[grids*3][7];
        int curIndex = 0;
        for (Element e : fileElements) {

            String authority = e.getAttributeValue("authority","ncsos");
            String networkOffering = "urn:ioos:network:" + authority + ":all";

            for (Element s : (List<Element>) e.getChildren("sensor")) {

                String standard = VocabDefinitions.GetDefinitionForParameter(
                		s.getAttributeValue("standard"),
                		"http://mmisw.org/ont/cf/parameter/",
                		true);

                for (Element v : (List<Element>) s.getChildren("values")) {

                    // Make 3 requests for each individual sensors
                    // They should all return the same thing

                    String lat = v.getAttributeValue("lat");
                    String lon = v.getAttributeValue("lon");

                    // A request where the offering and procedure are both Grid0
                    data[curIndex][0] = e;
                    data[curIndex][1] = "urn:ioos:station:" + authority + ":Grid0";;
                    data[curIndex][2] = "urn:ioos:station:" + authority + ":Grid0";
                    data[curIndex][3] = standard;
                    data[curIndex][4] = lat;
                    data[curIndex][5] = lon;
                    data[curIndex][6] = "grid0_offering_grid0_procedure" + lat + lon;
                    curIndex++;
                    // A request with the offering as network:all
                    data[curIndex][0] = e;
                    data[curIndex][1] = networkOffering;
                    data[curIndex][2] = "urn:ioos:station:" + authority + ":Grid0";
                    data[curIndex][3] = standard;
                    data[curIndex][4] = lat;
                    data[curIndex][5] = lon;
                    data[curIndex][6] = "network_offering_grid0_procedure" + lat + lon;
                    curIndex++;
                    // A request with only the offering
                    data[curIndex][0] = e;
                    data[curIndex][1] = "urn:ioos:station:" + authority + ":Grid0";
                    data[curIndex][2] = null;
                    data[curIndex][3] = standard;
                    data[curIndex][4] = lat;
                    data[curIndex][5] = lon;
                    data[curIndex][6] = "grid0_offering_no_procedure" + lat + lon;
                    curIndex++;
                }
            }
        }

        return Arrays.asList(data);
    }

    @Test
    public void testAll() {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        if (this.procedure != null) {
            pairs.put("procedure", this.procedure);
        }
        pairs.put("observedProperty",  this.observedProperty);
        pairs.put("offering",  this.offering);
        pairs.put("latitude",  this.latitude);
        pairs.put("longitude", this.longitude);
      
        File   file     = new File("resources" + systemSeparator + "datasets" + systemSeparator + this.currentFile.getAttributeValue("path"));
        String feature  = this.currentFile.getAttributeValue("feature");
        String output = null;
        if(this.observedProperty.contains("http")){
            int lastSlash = observedProperty.lastIndexOf("/");
            output = new File(outputDir + systemSeparator + file.getName() + "_URL" + this.observedProperty.substring(lastSlash+1) + "_" +  this.testType + ".xml").getAbsolutePath();
        }
        else
            output   = new File(outputDir + systemSeparator + file.getName() + "_" + this.observedProperty + "_" +  this.testType + ".xml").getAbsolutePath();

        System.out.println("------ " + file + " (" + feature + ") ------");
        System.out.println("------ " + this.testType + " ------");
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertFalse(NcSOSTest.isException(result));
    }

}
