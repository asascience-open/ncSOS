/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.error;

import com.asascience.ncsos.outputformatter.gc.GetCapsOutputter;
import com.asascience.ncsos.service.SOSBaseRequestHandler;
import java.io.IOException;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSErrorResponseHandler extends SOSBaseRequestHandler {
    
    public SOSErrorResponseHandler(NetcdfDataset dataset) throws IOException {
        super(dataset);
        
        output = new GetCapsOutputter();
    }
    
    /**
     * Sets the error string to be printed in the response to the user.
     * @param errorString String that will be shown to the user
     */
    public void setErrorExceptionOutput(String errorString) {
        output.setupExceptionOutput(errorString);
    }
    
}
