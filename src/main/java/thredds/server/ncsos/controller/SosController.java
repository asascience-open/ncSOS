package thredds.server.ncsos.controller;

import com.asascience.ncsos.outputformatter.ErrorFormatter;
import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.asascience.ncsos.service.Parser;
import com.asascience.ncsos.util.DatasetHandlerAdapter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ucar.nc2.dataset.NetcdfDataset;
import thredds.servlet.ThreddsConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

@Controller
@RequestMapping("/sos")
public class SosController implements ISosContoller {

    private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SosController.class);
    private static org.slf4j.Logger _logServerStartup = org.slf4j.LoggerFactory.getLogger("serverStartup");

    private boolean allow = false;

    private HashMap<String, Object> respMap;

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
    @RequestMapping(value="/**", params = {})
    @Override
    public void handleSOSRequest(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {

        NetcdfDataset dataset = null;

        allow = ThreddsConfig.getBoolean("NCSOS.allow", false);
        if (!allow) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "ncSOS service not enabled");
            return;
        }

        respMap = new HashMap<String, Object>();
        Writer writer = res.getWriter();
        try {
            //see http://tomcat.apache.org/tomcat-5.5-doc/config/context.html ----- workdir    
            String tempdir = System.getProperty("java.io.tmpdir");
         
            dataset = DatasetHandlerAdapter.openDataset(req, res);

            Parser md = new Parser();
            respMap = md.enhanceGETRequest(dataset, req.getQueryString(), req.getRequestURL()+"?".toString(),tempdir); 
            OutputFormatter output = (OutputFormatter)respMap.get("outputFormatter");
            res.setContentType(output.getContentType().toString());            
            output.writeOutput(writer);            
            writer.flush();
            writer.close();
         

        } 
        
        catch (Exception e) {
            _log.error("Something went wrong", e);

            ErrorFormatter  output = new ErrorFormatter();
            res.setContentType(output.getContentType().toString());        
            output.setException(e.getMessage());


            output.writeOutput(writer);            
            writer.flush();
            writer.close();
            //close the dataset remove memory hang
        } finally {  
        
            DatasetHandlerAdapter.closeDataset(dataset);
            
            
            
        }
       

    }
}
