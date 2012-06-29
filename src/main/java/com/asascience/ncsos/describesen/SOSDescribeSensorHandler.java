/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.service.SOSBaseRequestHandler;
import java.io.IOException;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSDescribeSensorHandler extends SOSBaseRequestHandler {
    
    private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(SOSDescribeSensorHandler.class);
    
    public SOSDescribeSensorHandler(NetcdfDataset netCDFDataset) throws IOException {
        super(netCDFDataset);
        if(netCDFDataset == null) {
            _log.error("received null dataset");
        }
        
    }
    
}
