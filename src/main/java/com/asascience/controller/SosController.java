/*
based on iso controller NOAA - ASA
 * @author abird
 */
package thredds.server.sos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import thredds.server.sos.util.DatasetHandlerAdapter;

import java.io.IOException;
import java.io.Writer;
import org.apache.log4j.Level;
import thredds.server.sos.service.SOSParser;

import ucar.nc2.dataset.NetcdfDataset;

/**
 * Controller for SOS service
 * Author: abird Date: feb 8, 2011
 * <p/>
 */
//@Deprecated
@Controller
@RequestMapping("/sos")
public class SosController implements ISosContoller {

    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SosController.class);
    private static org.slf4j.Logger _logServerStartup = org.slf4j.LoggerFactory.getLogger("serverStartup");

    protected String getPath() {
        return "Sos/";
    }

    public void init() throws ServletException {
        _logServerStartup.info("SOS Service - initialization start");
    }

    public void destroy() {
        NetcdfDataset.shutdown();
        _logServerStartup.info("SOS Service - destroy done");
    }

    /** 
     * Generate SOS for the underlying NetcdfDataset
     * 
     * @param request incoming url request 
     * @param response outgoing web based response
     * @throws ServletException if ServletException occurred
     * @throws IOException if IOException occurred 
     */
    @RequestMapping(params = {})
    @Override
    public void handleSOSRequest(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        _log.info("Handling SOS metadata request.");

        NetcdfDataset dataset = null;

        try {
            //see http://tomcat.apache.org/tomcat-5.5-doc/config/context.html ----- workdir    
            String tempdir = System.getProperty("java.io.tmpdir");
            
            //return netcdf dataset
            dataset = DatasetHandlerAdapter.openDataset(req, res);
            
            //set the response type
            res.setContentType("text/xml");
            Writer writer = res.getWriter();
            //TODO create new service
            SOSParser md = new SOSParser();
            md.enhance(dataset, writer, req.getQueryString(), req.getRequestURL().toString(),tempdir);          
            _log.info(req.getRequestURL().toString()+"?"+req.getQueryString().toString());
            writer.flush();
            writer.close();

        } catch (Exception e) {
            _log.error(e.getMessage());

            //close the dataset remove memory hang
        } finally {
            DatasetHandlerAdapter.closeDataset(dataset);
        }

    }
}
