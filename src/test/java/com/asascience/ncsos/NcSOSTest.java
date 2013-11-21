package com.asascience.ncsos;

import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.util.XMLDomUtils;
import org.apache.log4j.BasicConfigurator;
import org.jdom.Document;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import static org.junit.Assert.assertNotNull;

/**
 * Author: wilcox.kyle@gmail.com
 *   Date: 11/20/13
 *   Time: 1:36 PM
 */
public class NcSOSTest {

    protected URL query = null;
    protected static String outputDir = null;
    protected static String exampleDir = null;
    protected static List<Element> fileElements;
    protected static HashMap<String,String> kvp;
    protected static String systemSeparator = System.getProperty("file.separator");

    public static void setUpClass() throws Exception {
        kvp = new HashMap<String, String>();
        kvp.put("service", "SOS");
        kvp.put("version", "1.0.0");

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

    protected String getQueryString() {
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String,String> entry : kvp.entrySet()) {
            queryString.append(entry.getKey());
            queryString.append("=");
            queryString.append(entry.getValue());
            queryString.append("&");
        }
        System.out.println(queryString.toString());
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
