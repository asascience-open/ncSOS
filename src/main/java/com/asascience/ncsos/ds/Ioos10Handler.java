package com.asascience.ncsos.ds;

import com.asascience.ncsos.cdmclasses.iStationData;
import com.asascience.ncsos.service.BaseRequestHandler;
import com.asascience.ncsos.util.IFReportMechanism;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Ioos10Handler extends BaseRequestHandler {
    
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Ioos10Handler.class);
    
    protected final IFReportMechanism reporter;
    protected final static String ATTRIBUTE_MISSING = "Attribute not present in source data";
    protected final static String ATTRIBUTE_VALUE_MISSING = "Attribute value not defined in source data";
    protected final static String INSTITUTION = "institution";

    public Ioos10Handler(NetcdfDataset dataset) throws IOException {
        super(dataset);
        reporter = null;
    }
    
    public Ioos10Handler(NetcdfDataset dataset, IFReportMechanism report) throws IOException {
        super(dataset);
        reporter = report;
    }
    
    protected boolean checkForProcedure(String procedure) {
        String validProcedure;
        if (procedure.contains("station") || procedure.contains("sensor")) {
            for (String stname : this.getStationNames().values()) {
                if (procedure.contains("sensor")) {
                    for (VariableSimpleIF senVar : this.getSensorNames().values()) {
                        validProcedure = this.getSensorUrnName(stname, senVar);
                        logger.debug("Comparing " + procedure + " to " + validProcedure);
                        if (procedure.equalsIgnoreCase(validProcedure))
                            return true;
                    }
                } else {
                    validProcedure = this.getUrnName(stname);
                    logger.debug("Comparing " + procedure + " to " + validProcedure);
                    if (procedure.equalsIgnoreCase(validProcedure))
                        return true;
                }
            }
        } else if (procedure.contains("network")) {
            validProcedure = this.getUrnNetworkAll();
            logger.debug("Comparing " + procedure + " to " + validProcedure);
            if (procedure.equalsIgnoreCase(validProcedure))
                return true;
        }
            
        return false;
    }
    
    
    public String getPlatformType(String procedure){
    	String platformRetVal = null;
     	String platformType = null;
    	String platformVocab = this.getGlobalAttributeStr(PLATFORM_VOCAB);
    	if(platformVocab != null){
    		Map<String, Variable> platformMap = this.getPlatformVariableMap();
    		String foundStationName = null;
    		 for (String stationName : this.getStationNames().values()) {
    	            if (getUrnName(stationName).equalsIgnoreCase(procedure) || getUrnNetworkAll().equalsIgnoreCase(procedure)){
    	            	foundStationName = stationName;
    	            	break;
    	            }
    		 }
    		 if (foundStationName != null){
    			 Variable stationVar = platformMap.get(foundStationName);
    			 if(stationVar != null){
    				 List<Attribute> platformAtts = stationVar.getAttributes();
    				 for(Attribute cAtt : platformAtts){
    					 if(cAtt.getShortName().equalsIgnoreCase(TYPE)){
    						 platformType = cAtt.getStringValue();
    						 break;
    					 }
    				 }
    			 }
    		 }
    	}
    	if(platformVocab != null && platformType != null)
    		platformRetVal = platformVocab + platformType;
    	else 
    		// return legacy platform def.
    		platformRetVal = this.checkForRequiredValue("platform_type");
    	
    	return platformRetVal;
    }
    
    
    protected String checkForRequiredValue(String globalName) {
        String retval = (String)this.getGlobalAttribute(globalName);
        
        try {
            if (retval == null) {
                reporter.ReportMissing(globalName);
                return ATTRIBUTE_MISSING;
            } else if (retval.equalsIgnoreCase("")) {
                reporter.ReportInvalid(globalName, "");
                return ATTRIBUTE_VALUE_MISSING;
            }
        } catch (Exception ex) { }
        
        return retval;
    }
    
    protected String checkForRequiredValue(VariableSimpleIF var, String attribueName) {
    	if(var != null){
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
    	}
        
        return ATTRIBUTE_MISSING;
    }
    
    protected VariableSimpleIF checkForRequiredVariable(String varName) {
        VariableSimpleIF retval = this.getVariableByName(varName);
        if (retval == null && reporter != null) {
            reporter.ReportMissing(varName);
        }
        return retval;
    }
    
}
