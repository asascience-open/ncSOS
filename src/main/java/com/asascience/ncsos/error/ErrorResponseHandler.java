/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.error;

import com.asascience.ncsos.outputformatter.gc.GetCapsFormatter;
import com.asascience.ncsos.service.BaseRequestHandler;
import java.io.IOException;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class ErrorResponseHandler extends BaseRequestHandler {
    
    public ErrorResponseHandler(NetcdfDataset dataset) throws IOException {
        super(dataset);
        
        output = new GetCapsFormatter();
    }
    
    /**
     * Sets the error string to be printed in the response to the user.
     * @param errorString String that will be shown to the user
     */
    public void setErrorExceptionOutput(String errorString) {
        output.setupExceptionOutput(errorString);
    }
    
}
