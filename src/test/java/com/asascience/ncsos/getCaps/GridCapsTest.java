/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thredds.server.sos.getCaps;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.util.HashMap;
import org.apache.log4j.BasicConfigurator;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author abird
 */
public class GridCapsTest {

    private static String baseLocalDir = null;
    private static String outputDir = null;
    private static String exampleOutputDir = null;
    
    private static final String baseRequest = "request=GetCapabilities&version=1.0.0&service=sos";
    
    private static final String testGetCapsSST1 = "resources/datasets/satellite-sst/SST_Global_2x2deg_20120626_0000.nc";
    private static final String testGetCapsSST2 = "resources/datasets/satellite-sst/SST_Global_2x2deg_20120627_0000.nc";

    @BeforeClass
    public static void SetupEnviron() {
        // not really a test, just used to set up the various string values
        if (outputDir != null && baseLocalDir != null) {
            // exit early if the environ is already set
            return;
        }
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String container = "testConfiguration";
        InputStream templateInputStream = null;
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDirectory");
            outputDir += "get_caps/";
            baseLocalDir = XMLDomUtils.getNodeValue(configDoc, container, "projectDirectory");
            exampleOutputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDirectory");
            exampleOutputDir += "examples/";
        } catch (FileNotFoundException fnfex) {
            System.out.println(fnfex.getMessage());
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
    }
    
    private static void fileWriter(String base, String fileName, Writer write) {
        try {
            File file = new File(base + fileName);
            Writer output = new BufferedWriter(new FileWriter(file));
            output.write(write.toString());
            output.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
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
        SOSOutputFormatter output = (SOSOutputFormatter)outMap.get("outputHandler");
        assertNotNull("got null output", output);
        output.writeOutput(write);
    }

    @Test
    public void testParseSST1() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(testGetCapsSST1);
            SOSParser md = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, testGetCapsSST1),writer);
            writer.flush();
            writer.close();
            String fileName = "getCapsSST1.xml";
            fileWriter(outputDir, fileName, writer);
            // write as an example
            fileWriter(exampleOutputDir, "GetCapabilities-Grid-om1.0.0.xml", writer);
            assertFalse(writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
    @Test
    public void testParseSST2() {
        System.out.println("\n------" + getCurrentMethod() + "------");
        
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(testGetCapsSST2);
            SOSParser md = new SOSParser();
            Writer writer = new CharArrayWriter();
            writeOutput(md.enhanceGETRequest(dataset, baseRequest, testGetCapsSST2),writer);
            writer.flush();
            writer.close();
            String fileName = "getCapsSST2.xml";
            fileWriter(outputDir, fileName, writer);
            assertFalse(writer.toString().contains("Exception"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("------END " + getCurrentMethod() + "------");
        }
    }
    
//    @Test
//    public void testInsertClassNameHere() {
//        System.out.println("\n------" + getCurrentMethod() + "------");
//        
//        try {
//            
//        } catch (IOException ex) {
//            System.out.println(ex.getMessage());
//        } finally {
//            System.out.println("------END " + getCurrentMethod() + "------");
//        }
//    }

}
