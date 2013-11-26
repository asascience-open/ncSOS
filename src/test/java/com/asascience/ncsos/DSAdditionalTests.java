package com.asascience.ncsos;

import org.jdom.Element;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;

public class DSAdditionalTests extends NcSOSTest {

    private static Element currentFile = null;
    private static HashMap<String,String> kvp = new HashMap<String, String>();

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir += "DescribeSensor" + NcSOSTest.systemSeparator;
        exampleDir += "DescribeSensor" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        // Use the first 'file' element for testing
        currentFile = fileElements.get(0);

        kvp.put("outputFormat", URLEncoder.encode("text/xml;subtype=\"sensorML/1.0.1/profiles/ioos_sos/1.0\"", "UTF-8"));
        kvp.put("request",      "DescribeSensor");
        kvp.put("procedure",    "urn:ioos:station:ncsos:Station-0");
        kvp.put("version",      "1.0.0");
        kvp.put("service",      "SOS");
    }

    @Test
    public void testBadProcedureParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("procedure", "99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.INVALID_PARAMETER, NcSOSTest.getExceptionCode(result));
        Assert.assertEquals("procedure", NcSOSTest.getExceptionLocator(result));
    }
    @Test
    public void testNoProcedureParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("procedure");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.MISSING_PARAMETER, NcSOSTest.getExceptionCode(result));
        Assert.assertEquals("procedure", NcSOSTest.getExceptionLocator(result));
    }

    @Test
    public void testBadOutputFormatParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("outputFormat", "99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.INVALID_PARAMETER, NcSOSTest.getExceptionCode(result));
        Assert.assertEquals("outputFormat", NcSOSTest.getExceptionLocator(result));
    }
    @Test
    public void testNoOutputFormatParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("outputFormat");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.MISSING_PARAMETER, NcSOSTest.getExceptionCode(result));
        Assert.assertEquals("outputFormat", NcSOSTest.getExceptionLocator(result));
    }

    @Test
    public void testBadVersionParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("version", "99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.INVALID_PARAMETER, NcSOSTest.getExceptionCode(result));
        Assert.assertEquals("version", NcSOSTest.getExceptionLocator(result));
    }
    @Test
    public void testNoVersionParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("version");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.MISSING_PARAMETER, NcSOSTest.getExceptionCode(result));
        Assert.assertEquals("version", NcSOSTest.getExceptionLocator(result));
    }

    @Test
    public void testBadServiceParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("service", "NOTSOS");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.OPERATION_NOT_SUPPORTED, NcSOSTest.getExceptionCode(result));
        Assert.assertEquals("service", NcSOSTest.getExceptionLocator(result));
    }
    @Test
    public void testNoServiceParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.remove("service");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.MISSING_PARAMETER, NcSOSTest.getExceptionCode(result));
        Assert.assertEquals("service", NcSOSTest.getExceptionLocator(result));
    }
}
