package com.asascience.ncsos;

import org.jdom.Element;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class GOAdditionalGridTests extends NcSOSTest {

    private static Element currentFile = null;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir += "GetObservation-Grid" + NcSOSTest.systemSeparator;
        exampleDir += "GetObservation-Grid" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        // Use the first 'grid' feature for testing
        currentFile = null;
        for (Element e : fileElements) {
            if (e.getAttributeValue("feature").toLowerCase().equals("grid")) {
                currentFile = e;
                break;
            }
        }

        kvp.put("responseFormat",   URLEncoder.encode("text/xml;schema=\"om/1.0.0/profiles/ioos_sos/1.0\"", "UTF-8"));
        kvp.put("request",          "GetObservation");
        kvp.put("version",          "1.0.0");
        kvp.put("service",          "SOS");
        kvp.put("procedure",        "urn:ioos:station:ncsos:Grid0");
        kvp.put("offering",         "network-all");
        kvp.put("observedProperty", currentFile.getChild("sensor").getAttributeValue("standard"));
        kvp.put("latitude",         "0");
        kvp.put("longitude",        "0");
    }

    @Test
    public void testBadLatitudeParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("latitude", "NOT CORRECT");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
    @Test
    public void testNoLatitudeParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("latitude");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }

    @Test
    public void testBadLongitudeParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("longitude", "NOT CORRECT");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
    @Test
    public void testNoLongitudeParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("longitude");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
}
