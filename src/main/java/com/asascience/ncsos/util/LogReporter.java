package com.asascience.ncsos.util;

public class LogReporter implements IFReportMechanism {

    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LogReporter.class);
    
    public void ReportInvalid(String valueName, String invalidValue) {
        logger.warn("Recieved and invalid value for " + valueName + ": " + invalidValue);
    }

    public void ReportMissing(String valueName) {
        logger.warn("Missing required value: " + valueName);
    }
    
}
