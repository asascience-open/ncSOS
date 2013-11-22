package com.asascience.ncsos;

import org.jdom.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class DSStationTest extends NcSOSTest {

    private Element currentFile;
    private String procedure;
    public DSStationTest(Element file, String procedure) {
        this.currentFile    = file;
        this.procedure       = procedure;
    }

    public static void setUpClass() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir  += "DescribeSensor-Platform" + NcSOSTest.systemSeparator;
        exampleDir += "DescribeSensor-Platform" + NcSOSTest.systemSeparator;

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

        // Ignore GRID datasets
        int nonGrids = 0;
        for (Element e : new ArrayList<Element>(fileElements)) {
            if (e.getAttributeValue("feature").toLowerCase().equals("grid")) {
                fileElements.remove(e);
            } else {
                // Add each platform as a test
                nonGrids = nonGrids + e.getChildren("platform").size();
            }
        }

        Object[][] data = new Object[nonGrids][2];
        int curIndex = 0;
        for (Element e : fileElements) {
            for (Element p : (List<Element>) e.getChildren("platform")) {
                data[curIndex][0] = e;
                data[curIndex][1] = p.getAttributeValue("id");
                curIndex++;
            }
        }

        return Arrays.asList(data);
    }

    @Test
    public void testAll() {
        kvp.put("procedure", this.procedure);

        File   file     = new File("resources" + systemSeparator + "datasets" + systemSeparator + this.currentFile.getAttributeValue("path"));
        String feature  = this.currentFile.getAttributeValue("feature");
        String output   = new File(outputDir + systemSeparator + file.getName() + "_" + this.procedure + ".xml").getAbsolutePath();
        System.out.println("------ " + file + " (" + feature + ") ------");
        System.out.println("------ " + this.procedure + " ------");
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output, kvp);
    }

}
