package com.asascience.ncsos;

import org.jdom.Element;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.util.HashMap;

public class GCAdditionalTests extends NcSOSTest {

    private static HashMap<String,String> kvp = new HashMap<String, String>();
    private static Element currentFile = null;
    private static String outputDir;
    private static String exampleDir;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir  = baseOutputDir  +  NcSOSTest.systemSeparator + "GetCapabilities" + NcSOSTest.systemSeparator;
        exampleDir = baseExampleDir +  NcSOSTest.systemSeparator + "GetCapabilities" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        // Use the first 'file' element for testing
        currentFile = fileElements.get(0);

        kvp.put("request", "GetCapabilities");
        kvp.put("service", "SOS");
    }

    @Test
    public void testNoParameters() throws NoSuchMethodException {
        HashMap<String,String> pairs = new HashMap<String, String>();

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.MISSING_PARAMETER, NcSOSTest.getExceptionCode(result));
        Assert.assertEquals("request", NcSOSTest.getExceptionLocator(result));
    }

    @Test
    public void testBadVersionParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("version","99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        // NcSOS just ignores the "version" parameter and returns a valid GetCaps document
        Assert.assertFalse(NcSOSTest.isException(result));
    }

    @Test
    public void testBadAcceptVersionsParameter() throws NoSuchMethodException {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("acceptVersions","99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertTrue(NcSOSTest.isException(result));
        Assert.assertEquals(NcSOSTest.VERSION_NEGOTIATION, NcSOSTest.getExceptionCode(result));
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
