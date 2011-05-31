/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thredds.server.metadata.controller;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import java.util.List;
import javax.servlet.ServletException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import java.io.IOException;
import java.net.URL;
import org.jdom.Document;

/**
 * TEST a number of the SOS properties including GUI functionality issue kyles end
 * @author abird
 */
public class SosControllerTest {

    /**
     * Test of getPath method, of class SosController.
     */
    @Test
    public void testGetPath() {
       fail("NOT entered yet");
    }

    @Test
    public void testSaxBuilderForNULL42099() throws IOException, JDOMException {
    SAXBuilder parser = new SAXBuilder();
    Document doc = parser.build("http://192.168.100.123:8080/thredds/sos/testAll/Station_42099_2008met.nc");
    assertTrue(doc!=null);
    String baseURI = doc.getBaseURI();
    System.out.println(baseURI);
    String toString = doc.toString();
    //System.out.println(toString);
   URL url = new URL(baseURI);
    Document doc2 = parser.build(url);
    assertTrue(doc2!=null);
    }

     @Test
    public void testSaxBuilderForNULL42059() throws IOException, JDOMException {
    SAXBuilder parser = new SAXBuilder();
    Document doc = parser.build("http://192.168.100.123:8080/thredds/sos/testAll/Station_42059_2008met.nc?service=SOS&version=1.0.0&request=GetCapabilities");
    assertTrue(doc!=null);
    String baseURI = doc.getBaseURI();
    System.out.println(baseURI);
    List content = doc.getContent();
    String toString = doc.toString();
    //System.out.println(toString);
    URL url = new URL(baseURI);
    Document doc2 = parser.build(url);
    assertTrue(doc2!=null);

    }

     @Test
    public void testSaxBuilderForNULL42080() throws IOException, JDOMException {
    SAXBuilder parser = new SAXBuilder();
    Document doc = parser.build("http://192.168.100.123:8080/thredds/sos/testAll/Station_42080_2008met.nc?service=SOS&version=1.0.0&request=GetCapabilities");
    assertTrue(doc!=null);
    String baseURI = doc.getBaseURI();
    System.out.println(baseURI);
    String toString = doc.toString();
    //System.out.println(toString);
    URL url = new URL(baseURI);
    Document doc2 = parser.build(url);
    assertTrue(doc2!=null);
    }

    @Test
    public void testMockResAndReq() throws ServletException, IOException {

    fail("NOT WORKING AS YET");

    MockHttpServletRequest mockReq = new MockHttpServletRequest();
    MockHttpServletResponse mockRes = new MockHttpServletResponse();

    
    mockReq.setPathInfo("http://192.168.100.123:8080/thredds/sos/testAll/Station_42080_2008met.nc");
    mockReq.setQueryString("request=GetCapabilities&service=SOS&version=1.0.0");
    mockReq.setContextPath("/thredds");
    mockReq.setServletPath("/sos");
    mockReq.setRequestURI("/thredds/sos/testAll/Station_42080_2008met.nc");
    mockReq.setMethod("GET");
    mockReq.setRequestURL("http://localhost:8080/thredds/sos/testAll/epa+seamap_04-08_B177.nc");
    mockReq.setPathTranslated("C:/Program Files/Apache Software Foundation/Apache Tomcat 6.0.26/webapps/thredds/testAll/epa+seamap_04-08_B177.nc");

    assertTrue(mockReq.getPathInfo()!=null);
    SosController sos = new SosController();
    sos.handleMetadataRequest(mockReq, mockRes);


    }



}