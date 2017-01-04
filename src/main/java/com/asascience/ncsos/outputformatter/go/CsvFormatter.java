package com.asascience.ncsos.outputformatter.go;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.asascience.ncsos.cdmclasses.baseCDMClass;
import com.asascience.ncsos.go.GetObservationRequestHandler;
import com.asascience.ncsos.outputformatter.OutputFormatter;

public class CsvFormatter extends OutputFormatter {
	private GetObservationRequestHandler handler;
	public CsvFormatter(GetObservationRequestHandler getObservationRequestHandler) {
		this.handler = getObservationRequestHandler;
	}

	
	
	  @Override
	    public void writeOutput(Writer writer) throws IOException {
	        if (!hasError) {
	            List<String> obsProps = this.handler.getRequestedObservedProperties();
	            StringBuilder newString = new StringBuilder();
	            Boolean isFirstBlock = true;
	            String headerStr = "";
	            for (int p = 0; p < this.handler.getProcedures().length; p++) {
	            	String keyVals = this.handler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, p);
	            	for (String block :keyVals.split(BLOCK_SEPERATOR)) {
	            		// split on token seperator
	            		StringBuilder newBlock = new StringBuilder();

	            		for (String token : block.split(TOKEN_SEPERATOR)) {
	            		
	            			if(isFirstBlock && headerStr != ""){
            					headerStr = headerStr + TOKEN_SEPERATOR;
            				}
	            			if (token.contains(baseCDMClass.TIME_STR)) {
	            				newBlock.append(token.replaceAll("time=", "")).append(TOKEN_SEPERATOR);
	            				if (isFirstBlock){
	            					headerStr = headerStr + token.split("=")[0];
	            				}

	            			} 

	            			else if (token.contains(baseCDMClass.STATION_STR)) {
	            				String[] tokenSplit = token.split("=");
	            				int stNum = Integer.parseInt(tokenSplit[1]);
	            				newBlock.append(this.handler.getProcedures()[stNum]);
	            				if(isFirstBlock){
	            					headerStr = headerStr + token.split("=")[0];
	            				}
	            			} 
	            			else {
	            				String[] tokenSplit = token.split("=");
	            				if (obsProps.contains(tokenSplit[0]) && tokenSplit.length > 1) {
	            					newBlock.append(TOKEN_SEPERATOR).append(tokenSplit[1]);
	            		            String sensorUnits = this.handler.getUnitsString(tokenSplit[0]);
	            		            if(sensorUnits == null)
	            		                sensorUnits = "none";
	            		            if(isFirstBlock){
		            					headerStr = headerStr + this.handler.getVariableStandardName(tokenSplit[0]) + 
		            								"(" + sensorUnits +")";
		            				}
	            				}
	            			}
	            		}
	            		
	            		isFirstBlock = false;
	            		newString.append(newBlock.toString());
    					newString.append(BLOCK_SEPERATOR);
	            	}
	            	writer.write(headerStr + BLOCK_SEPERATOR + newString.toString());
	            }}
	  }

	        /**
	         * Go from urn to readable name (as in swe2:field name)
	         * @param stName
	         * @return 
	         */
	        private String stationToFieldName(String stName) {
	            // get the gml urn
	            String urn = this.handler.getUrnName(stName);
	            // split on station/sensor
	            String[] urnSplit = urn.split("(sensor|station):");
	            // get the last index of split
	            urn = urnSplit[urnSplit.length - 1];
	            // convert to underscore
	            String underScorePattern = "[\\+\\-\\s:]+";
	            urn = urn.replaceAll(underScorePattern, "_");
	            return urn.toLowerCase();
	        }



	@Override
	public String getContentType() {
		
		return "text/csv";
	}
}
