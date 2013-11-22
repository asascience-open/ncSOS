package com.asascience.ncsos;

import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.service.Parser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jdom.*;
import org.junit.rules.TestName;
import ucar.nc2.dataset.NetcdfDataset;

public class GCAdditionalTests extends NcSOSTest {

    private static Element currentFile = null;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir += "GetCapabilities" + NcSOSTest.systemSeparator;
        exampleDir += "GetCapabilities" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        // Use the first 'file' element for testing
        currentFile = fileElements.get(0);

        kvp.put("request", "GetCapabilities");
    }

    @Test
    public void testBadVersionParameter() throws NoSuchMethodException {
        kvp.put("version", "99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output);
    }

    @Test
    public void testBadAcceptVersionsParameter() throws NoSuchMethodException {
        kvp.put("acceptVersion", "99999");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output);
    }

    @Test
    public void testNoParameters() throws NoSuchMethodException {
        kvp = new HashMap<String, String>();

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output);
    }

    @Test
    public void testBadServiceParameter() throws NoSuchMethodException {
        kvp.put("service", "NOTSOS");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output);
    }
    @Test
    public void testNoServiceParameter() throws NoSuchMethodException {
        kvp.remove("service");

        File file = new File("resources" + systemSeparator + "datasets" + systemSeparator + currentFile.getAttributeValue("path"));
        String output   = new File(outputDir + systemSeparator + testName.getMethodName() + ".xml").getAbsolutePath();
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output);
    }
}
