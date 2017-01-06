package com.asascience.ncsos.outputformatter.go;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asascience.ncsos.cdmclasses.TimeSeriesProfile;
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
          	  boolean isProfile = handler.getCDMDataset() instanceof TimeSeriesProfile;

			  for (int p = 0; p < this.handler.getProcedures().length; p++) {
				  String keyVals = this.handler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, p);
				  for (String block :keyVals.split(BLOCK_SEPERATOR)) {
					  // split on token seperator
					  StringBuilder newBlock = new StringBuilder();
					  boolean appendedHeader = false;
					  for (String token : block.split(TOKEN_SEPERATOR)) {
						  String[] tokenSplit = token.split("=");
						  
						  if(isFirstBlock && appendedHeader){
							  headerStr = headerStr + TOKEN_SEPERATOR;
							  appendedHeader = false;
						  }
						  
						  if (token.contains(baseCDMClass.TIME_STR)) {
							  newBlock.append(token.replaceAll("time=", "")).append(TOKEN_SEPERATOR);
							  if (isFirstBlock){
								  headerStr = headerStr + tokenSplit[0];
								  appendedHeader = true;
							  }

						  } 

						  else if (token.contains(baseCDMClass.STATION_STR)) {
							  int stNum = Integer.parseInt(tokenSplit[1]);
							  newBlock.append(this.handler.getProcedures()[stNum]);
							  if(isFirstBlock){
								  headerStr = headerStr  + tokenSplit[0];
								  appendedHeader = true;

							  }
						  } 
						  else if(token.startsWith("height") && isProfile){
		                        if(isFirstBlock) {
		                        	headerStr = headerStr + "height(" +  ((TimeSeriesProfile )handler.getCDMDataset()).getHeightAxisUnits() +")";
									appendedHeader = true;

		                        }
		                        newBlock.append(TOKEN_SEPERATOR).append(Double.valueOf(tokenSplit[1]));
		                    }
						
						  else {
							  if (obsProps.contains(tokenSplit[0]) && tokenSplit.length > 1) {
							
								  newBlock.append(TOKEN_SEPERATOR).append(tokenSplit[1]);
								  String sensorUnits = this.handler.getUnitsString(tokenSplit[0]);
								  if(sensorUnits == null)
									  sensorUnits = "none";
								  if(isFirstBlock){
									  headerStr = headerStr + this.handler.getVariableStandardName(tokenSplit[0]) + 
											  "(" + sensorUnits +")";
									  appendedHeader = true;

								  }
							  }
						  }
					  }

					  isFirstBlock = false;
					  newString.append(newBlock.toString());
					  newString.append(BLOCK_SEPERATOR);
				  }
				  writer.write(headerStr + BLOCK_SEPERATOR + newString.toString());
			  }
		  }
	  }




	@Override
	public String getContentType() {
		
		return "text/csv";
	}
}
