/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.service.SOSBaseRequestHandler;
import com.asascience.ncsos.util.IFReportMechanism;
import java.io.IOException;
import ucar.nc2.Attribute;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class BaseDescribeSensor extends SOSBaseRequestHandler {
    
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BaseDescribeSensor.class);
    
    protected final IFReportMechanism reporter;
    protected final static String DEFAULT_STRING = "UNKNOWN";
    protected final static String URN_BASE = "urn:ioos:";
    
    public BaseDescribeSensor(NetcdfDataset dataset) throws IOException {
        super(dataset);
        reporter = null;
    }
    
    public BaseDescribeSensor(NetcdfDataset dataset, IFReportMechanism report) throws IOException {
        super(dataset);
        reporter = report;
    }
    
    protected boolean checkForProcedure(String procedure) {
        String validProcedure;
        if (procedure.contains("station") || procedure.contains("sensor")) {
            for (String stname : this.getStationNames().values()) {
                if (procedure.contains("sensor")) {
                    for (String senname : this.getSensorNames()) {
                        validProcedure = SOSBaseRequestHandler.getSensorGMLName(stname, senname);
                        logger.debug("Comparing " + procedure + " to " + validProcedure);
                        if (procedure.equalsIgnoreCase(validProcedure))
                            return true;
                    }
                } else {
                    validProcedure = SOSBaseRequestHandler.getGMLName(stname);
                    logger.debug("Comparing " + procedure + " to " + validProcedure);
                    if (procedure.equalsIgnoreCase(validProcedure))
                        return true;
                }
            }
        } else if (procedure.contains("network")) {
            validProcedure = URN_BASE + "network:" + SOSBaseRequestHandler.namingAuthority + ":all";
            logger.debug("Comparing " + procedure + " to " + validProcedure);
            if (procedure.equalsIgnoreCase(validProcedure))
                return true;
        }
            
        return false;
    }
    
    protected String checkForRequiredValue(String globalName) {
        String retval = this.getGlobalAttribute(globalName, null);
        
        try {
            if (retval == null) {
                reporter.ReportMissing(globalName);
                return DEFAULT_STRING;
            } else if (retval.equalsIgnoreCase("")) {
                reporter.ReportInvalid(globalName, "");
                return DEFAULT_STRING;
            }
        } catch (Exception ex) { }
        
        return retval;
    }
    
    protected String checkForRequiredValue(VariableSimpleIF var, String attribueName) {
        try {
            for (Attribute attr : var.getAttributes()) {
                if (attr.getShortName().equalsIgnoreCase(attribueName))
                    return attr.getStringValue();
            }
        } catch (Exception ex) {
            try {
                reporter.ReportMissing("Missing expected variable in checkForRequiredValue(VariableSimpleIF, String)");
            } catch (Exception ex2) { }
        }
        
        try {
            reporter.ReportMissing(attribueName + " from variable " + var.getShortName());
        } catch (Exception ex) { }
        
        return DEFAULT_STRING;
    }
    
    protected VariableSimpleIF checkForRequiredVariable(String varName) {
        VariableSimpleIF retval = this.getVariableByName(varName);
        if (retval == null && reporter != null) {
            reporter.ReportMissing(varName);
        }
        return retval;
    }
    
}
