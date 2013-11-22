package com.asascience.ncsos;

import org.jdom.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DSNetworkTest extends NcSOSTest {

    private Element currentFile;
    public DSNetworkTest(Element file){
    	this.currentFile = file;
    }

    public static void setUpClass() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir += "DescribeSensor-Network" + NcSOSTest.systemSeparator;
        exampleDir += "DescribeSensor-Network" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        kvp.put("outputFormat", URLEncoder.encode("text/xml;subtype=\"sensorML/1.0.1/profiles/ioos_sos/1.0\"", "UTF-8"));
        kvp.put("request", "DescribeSensor");
        kvp.put("procedure", "urn:ioos:network:ncsos:all");
        kvp.put("version", "1.0.0");
        kvp.put("service", "SOS");
    }

   	// Create the parameters for the test constructor
    @Parameters
    public static Collection<Object[]> testCases() throws Exception {
    	setUpClass();
        Object[][] data = new Object[fileElements.size()][1];
        int curIndex = 0;
        for (Element e : fileElements) {
            data[curIndex][0] = e;
            curIndex++;
        }
    	return Arrays.asList(data);
    }

    @Test
    public void testAll() {
        File   file     = new File("resources" + systemSeparator + "datasets" + systemSeparator + this.currentFile.getAttributeValue("path"));
        String feature  = this.currentFile.getAttributeValue("feature");
        String output   = new File(outputDir + systemSeparator + file.getName() + ".xml").getAbsolutePath();
        System.out.println("------ " + file + " (" + feature + ") ------");
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, kvp);
    }
    
}
