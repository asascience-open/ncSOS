/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.util;

import com.asascience.ncsos.describesen.SOSDescribePlatformM1_0;

/**
 *
 * @author SCowan
 */
public class LogReporter implements IFReportMechanism {

    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SOSDescribePlatformM1_0.class);
    
    public void ReportInvalid(String valueName, String invalidValue) {
        logger.warn("Recieved and invalid value for " + valueName + ": " + invalidValue);
    }

    public void ReportMissing(String valueName) {
        logger.warn("Missing required value: " + valueName);
    }
    
}
