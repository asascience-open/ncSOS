package com.asascience.ncsos;

import com.asascience.ncsos.util.XMLDomUtils;
import junit.framework.Assert;
import org.jdom.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

@RunWith(Parameterized.class)
public class DSNetworkTest extends NcSOSTest {

    private static HashMap<String,String> kvp = new HashMap<String, String>();

    private Element currentFile;
    private String authority;
    public DSNetworkTest(Element file, String authority){
    	this.currentFile = file;
        this.authority   = authority;
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
        kvp.put("version", "1.0.0");
        kvp.put("service", "SOS");
    }

   	// Create the parameters for the test constructor
    @Parameters
    public static Collection<Object[]> testCases() throws Exception {
    	setUpClass();
        Object[][] data = new Object[fileElements.size()][2];
        int curIndex = 0;
        String authority;
        for (Element e : fileElements) {
            data[curIndex][0] = e;
            authority = e.getAttributeValue("authority","ncsos"); // "ncsos" is the default authority in NcSOS
            data[curIndex][1] = authority;
            curIndex++;
        }
    	return Arrays.asList(data);
    }

    @Test
    public void testAll() {
        HashMap<String,String> pairs = (HashMap<String,String>) kvp.clone();
        pairs.put("procedure", "urn:ioos:network:" + this.authority + ":all");

        File   file     = new File("resources" + systemSeparator + "datasets" + systemSeparator + this.currentFile.getAttributeValue("path"));
        String feature  = this.currentFile.getAttributeValue("feature");
        String output   = new File(outputDir + systemSeparator + file.getName() + ".xml").getAbsolutePath();
        System.out.println("------ " + file + " (" + feature + ") ------");
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, pairs);
        Assert.assertFalse(NcSOSTest.isException(result));
    }
    
}
