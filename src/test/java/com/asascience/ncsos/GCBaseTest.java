package com.asascience.ncsos;

import com.asascience.ncsos.gc.GetCapabilitiesRequestHandler;
import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.service.Parser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.jdom.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

@RunWith(Parameterized.class)
public class GCBaseTest extends NcSOSTest {

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
        Element result = NcSOSTest.makeTestRequest(file.getAbsolutePath(), output);
    }
}