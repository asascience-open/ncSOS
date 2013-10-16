/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describeSen;

import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
@RunWith(Parameterized.class) 
public class SOSDescribeSensorNetworkTest {
    private static String outputDir = null;
    private static String exampleOutputDir = null;
    private static List<String> stationTests;
    private final static String systemSeparator = System.getProperty("file.separator").toString();
    private static String query = "request=DescribeSensorHandler&service=sos&version=1.0.0&procedure=urn:ioos:network:authority:all&outputFormat=";
    
    private static  String dataSourceDirectory;
 
    private String currentTest;
    private String testOutFile;
    public SOSDescribeSensorNetworkTest(String currentTest, String testOut){
    	this.currentTest = currentTest;
    	this.testOutFile = testOut;
    }

    public static void setUpClass() throws Exception {
        // not really a test, just used to set up the various string values
        if (outputDir != null ) {
            // exit early if the environ is already set
            return;
        }
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String container = "testConfiguration";
        InputStream templateInputStream = null;
        stationTests = new ArrayList<String>();
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDirectory");
            outputDir += "desc_sen" + systemSeparator;
            exampleOutputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDirectory");
            exampleOutputDir += "examples" + systemSeparator;
            dataSourceDirectory = XMLDomUtils.getNodeValue(configDoc, container, "dataSourceDirectory");
            dataSourceDirectory = new File(dataSourceDirectory).getCanonicalPath();
            System.out.println("source +++++++" + dataSourceDirectory);
           String testStr =  XMLDomUtils.getNodeValue(configDoc, container, "SOSdescribeSensorTest");
           System.out.println(testStr);
           String [] testFiles = testStr.trim().split("\n");
           for(String file : testFiles)
        	   stationTests.add(file);
            
        } finally {
            if (templateInputStream != null) {
                try {
                    templateInputStream.close();
                } catch (IOException e) {
                    // ignore, closing..
                }
            }
        }

        File file = new File(outputDir);
        file.mkdirs();
        
        file = new File(exampleOutputDir);
        file.mkdirs();
        
        query += URLEncoder.encode("text/xml;subtype=\"sensorML/1.0.1\"", "UTF-8") + "&";
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    private static String getCurrentMethod() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i=0; i<ste.length; i++) {
            if (ste[i].getMethodName().contains(("test")))
                return ste[i].getMethodName();
        }
        return "could not find test name";
    }
     
    private void writeOutput(HashMap<String, Object> outMap, Writer write) {
        OutputFormatter output = (OutputFormatter)outMap.get("outputHandler");
        assertNotNull("got null output", output);
        output.writeOutput(write);
    }
    
    private static void fileWriter(String base, String fileName, Writer writer) throws IOException {
        fileWriter(base, fileName, writer, false);
    }
    
    private static void fileWriter(String base, String fileName, Writer write, boolean append) throws IOException {
        File file = new File(base + fileName);
        Writer output = new BufferedWriter(new FileWriter(file, append));
        output.write("\n");
        output.write(write.toString());
        output.close();
    }
    

   	// Create the parameters for the tests
    @Parameters
    public static Collection<Object[]> testCases() throws Exception {
    	setUpClass();

    	Object[] [] data = new Object[stationTests.size()][2];
    	int curIndex = 0;
    	for(String file : stationTests) {
    		data[curIndex][0] = file.trim();
    		data[curIndex][1] = "TestCase_" + curIndex+"_Results.xml";
    		curIndex++;
    	}
    		
    	return Arrays.asList(data);
    }

		

    @Test
    public void testTimeSeriesSet1() {
  
    		System.out.println("\n------" + currentTest + "------");
    		System.out.println("------outputFolder: " +testOutFile);
    		String fullPathTestFile= System.getProperty("user.dir")+"\\resources\\datasets\\aggregationBug\\wqb.ncml";//dataSourceDirectory + systemSeparator + currentTest;
    		try {
    			NetcdfDataset dataset = NetcdfDataset.openDataset(fullPathTestFile);
    			SOSParser parser = new SOSParser();
    			Writer writer = new CharArrayWriter();
//    			writeOutput(parser.enhanceGETRequest(dataset, query, fullPathTestFile), writer);
//    			System.out.println("calling again");
//    			writeOutput(parser.enhanceGETRequest(dataset, query, fullPathTestFile), writer);

    			writeOutput(parser.enhanceGETRequest(dataset, null, fullPathTestFile), writer);
    			System.out.println("calling again");
    			writeOutput(parser.enhanceGETRequest(dataset, null, fullPathTestFile), writer);
    			fileWriter(outputDir, testOutFile, writer);
    			// assert(s)
    			assertFalse("exception in output", writer.toString().contains("Exception"));
    	
    		} catch (IOException ex) {
    			assertTrue(ex.getMessage(), false);
    		} finally {
    			System.out.println("------END " + currentTest + "------");
    		}
    	
    }
    
}
