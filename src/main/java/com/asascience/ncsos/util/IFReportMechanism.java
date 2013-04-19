/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.util;

/**
 *
 * @author SCowan
 */
public interface IFReportMechanism {
    
    public void ReportInvalid(String valueName, String invalidValue);
    
    public void ReportMissing(String valueName);
    
}
