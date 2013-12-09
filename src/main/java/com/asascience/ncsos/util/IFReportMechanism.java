package com.asascience.ncsos.util;

public interface IFReportMechanism {
    
    public void ReportInvalid(String valueName, String invalidValue);
    
    public void ReportMissing(String valueName);
    
}
