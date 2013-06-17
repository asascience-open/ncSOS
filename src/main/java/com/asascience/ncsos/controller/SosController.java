/*
based on iso controller NOAA - ASA
 * @author abird
 */
package com.asascience.ncsos.controller;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import com.asascience.ncsos.util.DatasetHandlerAdapter;
import java.io.IOException;
import java.io.Writer;
import java.util.Formatter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.BasicConfigurator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import thredds.servlet.DatasetHandler;
import ucar.nc2.NetcdfFile;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

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
    
    private HashMap<String, Object> respMap;

    protected String getPath() {
        return "Sos/";
    }

    public void init() throws ServletException {
        BasicConfigurator.configure();
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
        
        respMap = new HashMap<String, Object>();

        try {
            //see http://tomcat.apache.org/tomcat-5.5-doc/config/context.html ----- workdir    
            String tempdir = System.getProperty("java.io.tmpdir");
            
         
            //return netcdf dataset
            dataset = DatasetHandlerAdapter.openDataset(req, res);
            //set the response type
            Writer writer = res.getWriter();
            //TODO create new service???
            SOSParser md = new SOSParser();
            respMap = md.enhanceGETRequest(dataset, req.getQueryString(), req.getRequestURL().toString(),tempdir);
            res.setContentType(respMap.get("responseContentType").toString());
            
            // tell our handler to write out the response
            SOSOutputFormatter output = (SOSOutputFormatter)respMap.get("outputHandler");
            output.writeOutput(writer);
            
            // log and flush writer
//            _log.info(req.getRequestURL().toString()+"?"+req.getQueryString().toString());
            writer.flush();
            writer.close();

        } catch (Exception e) {
            _log.error(e.getMessage());
            //close the dataset remove memory hang
        } finally {
        	// This is a workaround for a bug in thredds. On the second request 
        	// for a ncml object the request will fail due to an error
        	// with the cached object. In order to get around this, the
        	// cache for the ncml files must be cleared.
        	if(dataset.getReferencedFile().getLocation().toLowerCase().endsWith("xml") ||
        		dataset.getReferencedFile().getLocation().toLowerCase().endsWith("ncml"))
                dataset.getReferencedFile().setFileCache(null);
        	
           DatasetHandlerAdapter.closeDataset(dataset);
            
        }

    }
}
