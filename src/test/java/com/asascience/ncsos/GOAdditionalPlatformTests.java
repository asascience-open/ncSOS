package com.asascience.ncsos;

import org.jdom.Element;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;

public class GOAdditionalPlatformTests extends NcSOSTest {

    private static Element currentFile = null;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir += "GetObservation-Platform" + NcSOSTest.systemSeparator;
        exampleDir += "GetObservation-Platform" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        // Use the first non 'grid' element for testing
        currentFile = null;
        for (Element e : fileElements) {
            if (!e.getAttributeValue("feature").toLowerCase().equals("grid")) {
                currentFile = e;
                break;
            }
        }

        kvp.put("responseFormat",   URLEncoder.encode("text/xml;schema=\"om/1.0.0/profiles/ioos_sos/1.0\"", "UTF-8"));
        kvp.put("request",          "GetObservation");
        kvp.put("version",          "1.0.0");
        kvp.put("service",          "SOS");
        kvp.put("procedure",        currentFile.getChild("platform").getAttributeValue("id"));
        kvp.put("offering",         "network-all");
        kvp.put("observedProperty", currentFile.getChild("platform").getChild("sensor").getAttributeValue("standard"));
    }

    @Test
    public void testBadProcedureParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("procedure", "99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
    @Test
    public void testOnlyProcedureParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = new HashMap<String, String>();
        pairs.put("procedure", kvp.get("procedure"));

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }

    @Test
    public void testBadOfferingParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("offering", "99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
    @Test
    public void testNoOfferingParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("offering");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
    @Test
    public void testOnlyOfferingParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = new HashMap<String, String>();
        pairs.put("offering", kvp.get("offering"));

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }

    @Test
    public void testBadResponseFormatParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("responseFormat", "99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
    @Test
    public void testNoResponseFormatParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("responseFormat");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }

    @Test
    public void testBadVersionParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("version", "99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
    @Test
    public void testNoVersionParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("version");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }

    @Test
    public void testBadServiceParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("service", "NOTSOS");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
    @Test
    public void testNoServiceParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("service");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
    }
}
