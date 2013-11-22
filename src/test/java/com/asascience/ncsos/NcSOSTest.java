package com.asascience.ncsos;

import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.service.Parser;
import com.asascience.ncsos.util.XMLDomUtils;
import org.apache.log4j.BasicConfigurator;
import org.jdom.Document;
import org.jdom.Element;
import ucar.nc2.dataset.NetcdfDataset;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NcSOSTest {

    protected URL query = null;
    protected static String outputDir = null;
    protected static String exampleDir = null;
    protected static List<Element> fileElements;
    protected static HashMap<String,String> kvp;
    protected static String systemSeparator = System.getProperty("file.separator");

    public static void setUpClass() throws Exception {
        kvp = new HashMap<String, String>();

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        try {
            File configFile = new File("resources/tests_config.xml");
            InputStream templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir      = XMLDomUtils.getNodeValue(configDoc, "testConfiguration", "outputDirectory");
            exampleDir     = XMLDomUtils.getNodeValue(configDoc, "testConfiguration", "examplesDirectory");

            Element testFilesElement = XMLDomUtils.getNestedChild(configDoc.getRootElement(), "TestFiles");
            fileElements = testFilesElement.getChildren();

            templateInputStream.close();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    protected static Element makeTestRequest(String dataset_path, String output, HashMap<String,String> kvp) {
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(dataset_path);
            Parser parser = new Parser();
            Writer writer = new CharArrayWriter();

            OutputFormatter outputFormat = (OutputFormatter) parser.enhanceGETRequest(dataset, getQueryString(kvp), dataset_path).get("outputHandler");
            outputFormat.writeOutput(writer);

            // Write to disk
            System.out.println("------ Saving output: " + output +" ------");
            NcSOSTest.fileWriter(output, writer);

            // Now we need to load the resulting XML and do some actual tests.
            Element root = XMLDomUtils.loadFile(output).getRootElement();
            return root;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    protected static String getQueryString(HashMap<String,String> kvp) {
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String,String> entry : kvp.entrySet()) {
            queryString.append(entry.getKey());
            queryString.append("=");
            queryString.append(entry.getValue());
            queryString.append("&");
        }
        return queryString.toString();
    }

    protected static void fileWriter(String filePath, Writer writer) throws IOException {
        NcSOSTest.fileWriter(filePath, writer, false);
    }

    protected static void fileWriter(String filePath, Writer writer, boolean append) throws IOException {
        File file = new File(filePath);
        Writer output = new BufferedWriter(new FileWriter(file, append));
        output.write(writer.toString());
        output.close();
    }
}
