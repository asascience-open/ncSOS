package com.asascience.ncsos;

import junit.framework.Assert;
import org.jdom.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

@RunWith(Parameterized.class)
public class GCBaseTest extends NcSOSTest {

    private static HashMap<String,String> kvp = new HashMap<String, String>();

    private Element currentFile;
    public GCBaseTest(Element file){
        this.currentFile = file;
    }

    public static void setUpClass() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir += "GetCapabilities" + NcSOSTest.systemSeparator;
        exampleDir += "GetCapabilities" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        kvp.put("request", "GetCapabilities");
        kvp.put("service", "SOS");
    }

    // Create the parameters for the test constructor
    @Parameterized.Parameters
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
        Assert.assertFalse(NcSOSTest.isException(result));
    }
}