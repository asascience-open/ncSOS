package com.asascience.ncsos.util;

public class LogReporter implements IFReportMechanism {

    private final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(LogReporter.class);
    
    public void ReportInvalid(String valueName, String invalidValue) {
        logger.warn("Recieved and invalid value for " + valueName + ": " + invalidValue);
    }

    public void ReportMissing(String valueName) {
        logger.warn("Missing required value: " + valueName);
    }
    
}
