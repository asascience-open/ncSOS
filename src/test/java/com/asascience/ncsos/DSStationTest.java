package com.asascience.ncsos;

import com.asascience.ncsos.service.Parser;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.asascience.ncsos.outputformatter.OutputFormatter;

import static org.junit.Assert.*;

import com.asascience.ncsos.util.XMLDomUtils;
import org.jdom.Element;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import ucar.nc2.dataset.NetcdfDataset;

@RunWith(Parameterized.class)
public class DSStationTest extends NcSOSTest {

    private Element currentFile;
    private String platform;
    public DSStationTest(Element file, String platform) {
        this.currentFile    = file;
        this.platform       = platform;
    }

    public static void setUpClass() throws Exception {
        NcSOSTest.setUpClass();

        // Modify the outputs
        outputDir  += "DescribeSensor-Station" + NcSOSTest.systemSeparator;
        exampleDir += "DescribeSensor-Station" + NcSOSTest.systemSeparator;

        // Create output directories if they don't exist
        new File(outputDir).mkdirs();
        new File(exampleDir).mkdirs();

        kvp.put("outputFormat", URLEncoder.encode("text/xml;subtype=\"sensorML/1.0.1/profiles/ioos_sos/1.0\"", "UTF-8"));
        kvp.put("request", "DescribeSensor");
        kvp.put("version", "1.0.0");
    }

    // Create the parameters for the test constructor
    @Parameters
    public static Collection<Object[]> testCases() throws Exception {
        setUpClass();
        Object[][] data = new Object[fileElements.size()][2];
        int curIndex = 0;
        for (Element e : fileElements) {
            // Ignore GRID datasets
            if (e.getAttributeValue("feature").toLowerCase() != "grid") {
                for (Element p : (List<Element>) e.getChildren("platform")) {
                    data[curIndex][0] = e;
                    data[curIndex][1] = p.getAttributeValue("id");
                }
            }
            curIndex++;
        }
        return Arrays.asList(data);
    }

    @Test
    public void testAll() {
        kvp.put("procedure", this.platform);

        File   file     = new File("resources" + systemSeparator + "datasets" + systemSeparator + this.currentFile.getAttributeValue("path"));
        String feature  = this.currentFile.getAttributeValue("feature");
        String output   = new File(outputDir + systemSeparator + file.getName() + ".xml").getAbsolutePath();
        System.out.println("------ " + file + " (" + feature + ") ------");
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output);
    }

}
