package com.asascience.ncsos.outputformatter.go;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asascience.ncsos.cdmclasses.Grid;
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
			  Map<String, List<Double>> heightMap = new HashMap<String, List<Double>>();

          	  boolean isProfile = handler.getCDMDataset() instanceof TimeSeriesProfile;
          	  boolean is3dGrid =  this.handler.is3dGrid(this.handler.getCDMDataset().getStationName(0));
          	  List<String> allObsInHeader = new ArrayList<String>();
			  for (int p = 0; p < this.handler.getProcedures().length; p++) {
				  String keyVals = this.handler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, p);
				  for (String block :keyVals.split(BLOCK_SEPERATOR)) {
					  // split on token seperator
					  StringBuilder newBlock = new StringBuilder();
					  boolean appendedHeader = false;
					  Integer stNum = null;
					  Integer bin = null;
					  String currTime = null;
					  String lat = null;
					  String lon = null;
					  for (String token : block.split(TOKEN_SEPERATOR)) {
						  String[] tokenSplit = token.split("=");
						  
						  if(isFirstBlock && appendedHeader){
							  headerStr = headerStr + TOKEN_SEPERATOR;
							  appendedHeader = false;
						  }
						  
						  if (token.contains(baseCDMClass.TIME_STR)) {
							  currTime  = tokenSplit[1];
							  if (isFirstBlock){
								  headerStr = headerStr + tokenSplit[0];
								  appendedHeader = true;
							  }

						  } 

						  else if (token.contains(baseCDMClass.STATION_STR)) {
							  stNum = Integer.parseInt(tokenSplit[1]);
							  
							  if(isFirstBlock){
								  headerStr = headerStr  + tokenSplit[0];
								  appendedHeader = true;

							  }
						  } 
						  else if (tokenSplit[0].equals("lat") || tokenSplit[0].equals("lon")){
							  if(isFirstBlock){
								  headerStr = headerStr  + tokenSplit[0];
								  appendedHeader = true;
							  }
							  if(tokenSplit[0].equals("lat"))
								  lat = tokenSplit[1];
							  else
								  lon = tokenSplit[1];
						  }
					
						  else if(token.startsWith("BIN") && (isProfile || is3dGrid)){
							  bin = Integer.valueOf(tokenSplit[1]);
						  }
						  
						  else {
							  if (obsProps.contains(tokenSplit[0]) && tokenSplit.length > 1) {
								  String station = this.handler.stationToFieldName(this.handler.getProcedures()[stNum]);
								  String stationSensor = station + "_"+ tokenSplit[0];
								  if(isFirstBlock)
									  headerStr += "variable"+TOKEN_SEPERATOR;
								  if((isProfile || is3dGrid) && !heightMap.containsKey(stationSensor)) {
									  if(isProfile){
										  if(isFirstBlock)
											  headerStr = headerStr + "height(" +  ((TimeSeriesProfile )handler.getCDMDataset()).getHeightAxisUnits() +")"+TOKEN_SEPERATOR;
										  heightMap.put(stationSensor, ((TimeSeriesProfile )
													handler.getCDMDataset()).getProfileHeightsForStation(String.valueOf(stNum)));
										 
									  }
									  else{
										  Grid grid = ((Grid) this.handler.getCDMDataset());
										  heightMap.put(stationSensor, grid.getDepths(tokenSplit[0]));
										  if(isFirstBlock)
											  headerStr = headerStr + "height(" +  ((Grid) this.handler.getCDMDataset()).getDepthUnits(tokenSplit[0])  +")"+TOKEN_SEPERATOR;
									  }
									  appendedHeader = true;

								  }
								  newBlock.append(currTime).append(TOKEN_SEPERATOR);
								  if(lat != null){
									  newBlock.append(lat).append(TOKEN_SEPERATOR);
								  }
								  if(lon != null){
									  newBlock.append(lon).append(TOKEN_SEPERATOR);
								  }
								  newBlock.append(stationSensor);
								  if(isProfile || is3dGrid){
									  if (bin != null)
										  newBlock.append(TOKEN_SEPERATOR).append(heightMap.get(stationSensor).get(bin));
									  else{
										  newBlock.append(TOKEN_SEPERATOR).append("");
									  }
								  }
								  newBlock.append(TOKEN_SEPERATOR).append(tokenSplit[1]).append(BLOCK_SEPERATOR);
								  String sensorUnits = this.handler.getUnitsString(tokenSplit[0]);
								  if(sensorUnits == null)
									  sensorUnits = "none";
								  if(isFirstBlock){
									  headerStr += "value";
								  }
								  if(!allObsInHeader.contains(tokenSplit[0])){
									  headerStr = this.handler.stationToFieldName(this.handler.getProcedures()[stNum]) + "_"+ tokenSplit[0] + " => " +this.handler.getVariableStandardName(tokenSplit[0]) + 
											  "(" + sensorUnits +")"+"\n" + headerStr;
									  allObsInHeader.add(tokenSplit[0]);
									  appendedHeader = true;

								  }
								  isFirstBlock = false;

							  }
						  }
					  }

					  newString.append(newBlock.toString());
					  //newString.append(BLOCK_SEPERATOR);
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
