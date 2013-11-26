package com.asascience.ncsos;

import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.service.Parser;
import com.asascience.ncsos.util.XMLDomUtils;
import org.apache.log4j.BasicConfigurator;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
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
    protected static String systemSeparator = System.getProperty("file.separator");
    private   static String OUTPUT_FORMATTER = "outputFormatter";

    // Exception codes - Table 25 of OGC 06-121r3 (OWS Common)
    protected static String INVALID_PARAMETER       = "InvalidParameterValue";
    protected static String MISSING_PARAMETER       = "MissingParameterValue";
    protected static String OPTION_NOT_SUPPORTED    = "OptionNotSupported";
    protected static String OPERATION_NOT_SUPPORTED = "OperationNotSupported";
    protected static String VERSION_NEGOTIATION    = "VersionNegotiationFailed";

    private static Namespace OWS_NS = Namespace.getNamespace("ows","http://www.opengis.net/ows/1.1");


    public static void setUpClass() throws Exception {

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        try {
            File configFile = new File("resources/tests_config.xml");
            InputStream templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir      = configDoc.getRootElement().getChild("outputDirectory").getValue();
            exampleDir     = configDoc.getRootElement().getChild("examplesDirectory").getValue();

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

            OutputFormatter outputFormat = (OutputFormatter) parser.enhanceGETRequest(dataset, getQueryString(kvp), dataset_path).get(OUTPUT_FORMATTER);
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

    protected static String getExceptionText(Element e) {
        try {
            return e.getChild("Exception", OWS_NS).getChild("ExceptionText", OWS_NS).getValue();
        } catch (NullPointerException n) {
            return "";
        }
    }

    protected static String getExceptionCode(Element e) {
        try {
            return e.getChild("Exception", OWS_NS).getAttributeValue("exceptionCode");
        } catch (NullPointerException n) {
            return "";
        }
    }

    protected static String getExceptionLocator(Element e) {
        try {
            return e.getChild("Exception", OWS_NS).getAttributeValue("locator");
        } catch (NullPointerException n) {
            return "";
        }
    }

    protected static boolean isException(Element e) {
        if (e.getNamespace() == OWS_NS && e.getName().equals("ExceptionReport")) {
            return true;
        }
        return false;
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
