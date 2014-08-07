package com.asascience.ncsos;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
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
public class GOBasePlatformTest extends NcSOSTest {

    private static HashMap<String,String> kvp = new HashMap<String, String>();
    private static String outputDir;
    private static String exampleDir;

    private Element currentFile;
    private String  procedure;
    private String  offering;
    private String  testType;
    private String  observedProperty;
    public GOBasePlatformTest(Element file, String offering, String procedure, String observedProperty, String testType) {
        this.currentFile      = file;
        this.procedure        = procedure;
        this.offering         = offering;
        this.observedProperty = observedProperty;
        this.testType         = testType;

    }

    public static void setUpClass() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir  = baseOutputDir  +  NcSOSTest.systemSeparator + "GetObservation-Platform" + NcSOSTest.systemSeparator;
        exampleDir = baseExampleDir +  NcSOSTest.systemSeparator + "GetObservation-Platform" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        kvp.put("responseFormat", URLEncoder.encode("text/xml;subtype=\"om/1.0.0/profiles/ioos_sos/1.0\"", "UTF-8"));
        kvp.put("request",  "GetObservation");
        kvp.put("version", "1.0.0");
        kvp.put("service", "SOS");
    }

    // Create the parameters for the test constructor
    @Parameters
    public static Collection<Object[]> testCases() throws Exception {
        setUpClass();

        // Ignore GRID datasets
        int nonGrids = 0;
        for (Element e : new ArrayList<Element>(fileElements)) {
            if (!e.getAttributeValue("feature").toLowerCase().equals("timeseries")) {
                fileElements.remove(e);
            } else {
                for (Element p : (List<Element>) e.getChildren("platform")) {
                    nonGrids = nonGrids + (p.getChildren("sensor").size() * 4);
                    // Three tests using all observedProperties
                    nonGrids = nonGrids + 3;
                }
            }
        }

        Object[][] data = new Object[nonGrids][5];
        int curIndex = 0;
        List<String> observedPropertyList;
        for (Element e : fileElements) {

            String networkOffering = "urn:ioos:network:" + e.getAttributeValue("authority","ncsos") + ":all";

            for (Element p : (List<Element>) e.getChildren("platform")) {

                String procedure = p.getAttributeValue("id");
                String offering  = procedure;
                observedPropertyList = new ArrayList<String>();

                for (Element s : (List<Element>) p.getChildren("sensor")) {
                    // Keep track of the observedProperties so we can make a request
                    // with all of them outside of this forloop.
                    String observedProperty = s.getAttributeValue("standard");
                    observedPropertyList.add(observedProperty);

                    // Make 4 requests for each individual sensors
                    // They should all return the same thing

                    // A request where the offering and procedure are identical
                    data[curIndex][0] = e;
                    data[curIndex][1] = offering;
                    data[curIndex][2] = procedure;
                    data[curIndex][3] = observedProperty;
                    data[curIndex][4] = "platform_offering_platform_procedure";
                    curIndex++;
                    // A request with the offering as network:all
                    data[curIndex][0] = e;
                    data[curIndex][1] = networkOffering;
                    data[curIndex][2] = procedure;
                    data[curIndex][3] = observedProperty;
                    data[curIndex][4] = "network_offering_platform_procedure";
                    curIndex++;
                    // A request with only the offering
                    data[curIndex][0] = e;
                    data[curIndex][1] = offering;
                    data[curIndex][2] = null;
                    data[curIndex][3] = observedProperty;
                    data[curIndex][4] = "platform_offering_no_procedure";
                    curIndex++;
                    
                    
//                    // A request with the observedProperty set to the URL of an IOOS vocabluary
                    
                    data[curIndex][0] = e;
                    data[curIndex][1] = offering;
                    data[curIndex][2] = procedure;
                    data[curIndex][3] =  VocabDefinitions.GetDefinitionForParameter(observedProperty);
                    data[curIndex][4] = "platform_offering_platform_procedure_ioos_vocab";
                    curIndex++;
             
                    
                }

                
                // Now 3 requests with all observedProperties

                // A request where the offering and procedure are identical
                data[curIndex][0] = e;
                data[curIndex][1] = offering;
                data[curIndex][2] = procedure;
                data[curIndex][3] = StringUtils.join(observedPropertyList, ',');
                data[curIndex][4] = "platform_offering_platform_procedure";
                curIndex++;
                // A request with the offering as network:all
                data[curIndex][0] = e;
                data[curIndex][1] = networkOffering;
                data[curIndex][2] = procedure;
                data[curIndex][3] = StringUtils.join(observedPropertyList, ',');
                data[curIndex][4] = "network_offering_platform_procedure";
                curIndex++;
                // A request with only the offering
                data[curIndex][0] = e;
                data[curIndex][1] = offering;
                data[curIndex][2] = null;
                data[curIndex][3] = StringUtils.join(observedPropertyList, ',');
                data[curIndex][4] = "platform_offering_no_procedure";
                curIndex++;

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
        pairs.put("observedProperty", this.observedProperty);
        pairs.put("offering",  this.offering);

        File   file     = new File("resources" + systemSeparator + "datasets" + systemSeparator + this.currentFile.getAttributeValue("path"));
        String feature  = this.currentFile.getAttributeValue("feature");
        String output;
        if(this.observedProperty.contains("http")){
            int lastSlash = observedProperty.lastIndexOf("/");
            output = new File(outputDir + systemSeparator + file.getName() + "_URL" + this.observedProperty.substring(lastSlash+1) + "_" +  this.testType + ".xml").getAbsolutePath();
        }
        else
            output   = new File(outputDir + systemSeparator + file.getName() + "_" + this.observedProperty + "_" +  this.testType + ".xml").getAbsolutePath();
        System.out.println("------ " + file + " (" + feature + ") ------");
        System.out.println("------ " + pairs + " ------");
        System.out.println("output: " + output);
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        if (currentFile.getAttributeValue("feature").equalsIgnoreCase("point")) {
            // NcSOS does not support POINT features at this time!
            Assert.assertTrue(NcSOSTest.isException(result));
        } else {
            Assert.assertFalse(NcSOSTest.isException(result));
            
        }
    }

}
