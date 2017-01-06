package com.asascience.ncsos.outputformatter.go;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asascience.ncsos.cdmclasses.TimeSeriesProfile;
import com.asascience.ncsos.cdmclasses.baseCDMClass;
import com.asascience.ncsos.go.GetObservationRequestHandler;
import com.asascience.ncsos.outputformatter.OutputFormatter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFormatter extends OutputFormatter {
	private GetObservationRequestHandler handler;
	
	
	public JsonFormatter(
			GetObservationRequestHandler getObservationRequestHandler) {
		this.handler = getObservationRequestHandler;
	}
	
	public void createDataStructs(Map<String, Map<String, JsonFormatterData>> stationData,
			Map<String, Integer> stationToNum){

		List<String> obsProps = handler.getRequestedObservedProperties();
		String time_keyname = baseCDMClass.TIME_STR.replace("=","");
		String station_keyname = baseCDMClass.STATION_STR.replace("=", "");
		String binKeyname = TimeSeriesProfile.BIN_STR.replace("=","");
		Map<String, List<Double>> heightMap = new HashMap<String, List<Double>>();
		boolean isProfile = handler.getCDMDataset() instanceof TimeSeriesProfile;


		for (int p = 0; p < handler.getProcedures().length; p++) {
			String keyVals = handler.getValueBlockForAllObs(BLOCK_SEPERATOR, DECIMAL_SEPERATOR, TOKEN_SEPERATOR, p);
			for (String block :keyVals.split(BLOCK_SEPERATOR)) {
				String blockAr[] = block.split(TOKEN_SEPERATOR);
				String station = null;
				String time = null;
				int stationNum = -1;
				int bin = 0; // default when not a profile
				for (String token : blockAr) {
					String[] tokenSplit = token.split("=");
					if (tokenSplit[0].equals(station_keyname)) {
						stationNum = Integer.parseInt(tokenSplit[1]);
						station = handler.getProcedures()[stationNum];
					}
					else if (tokenSplit[0].equals(time_keyname)) {
						time = tokenSplit[1];
					}
					else if(tokenSplit[0].equals(binKeyname)){
						bin = Integer.parseInt(tokenSplit[1]);
					}
				}
				if(station == null || time == null) continue;

				if(!stationData.containsKey(station)){						
					stationToNum.put(station, stationNum);
					stationData.put(station, new HashMap<String, JsonFormatterData>());
				}

				for (String token : blockAr) {
					String[] tokenSplit = token.split("=");
					String var = tokenSplit[0];
					if (var.equals(time_keyname) || var.equals(station_keyname)) {
						continue;
					}
					if (obsProps.contains(var) && tokenSplit.length > 1) {
						String varValue = tokenSplit[1];

						String varStandard = handler.getVariableStandardName(var);
						Map<String, JsonFormatterData> cData = stationData.get(station);

						if (!cData.containsKey(varStandard)){
							String heightUnits = null;
							if(isProfile){
								heightUnits = ((TimeSeriesProfile )
										handler.getCDMDataset()).getHeightAxisUnits();

								heightMap.put(station, ((TimeSeriesProfile )
										handler.getCDMDataset()).getProfileHeightsForStation(station));
							}
							// 
							JsonFormatterData jdata = new JsonFormatterData(varStandard,
									(handler.getUnitsString(var)), heightUnits);
							cData.put(varStandard, jdata);
						}
						JsonFormatterData data = cData.get(varStandard);

						data.getTimeValues().add(time);
						data.getDataValues().add(varValue);
						if(isProfile)
							data.getHeightValues().add(heightMap.get(station).get(bin));

					}
				}
			}
		}
	}
	
	  @Override
	  public void writeOutput(Writer writer) throws IOException {
		  JsonGenerator jsonGen = new JsonFactory().createGenerator(writer);
		  Map<String, Map<String, JsonFormatterData>> stationData = new HashMap<String, Map<String, JsonFormatterData>>();
		  Map<String, Integer> stationToNum = new HashMap<String, Integer>();
		  String times = "times";
		  String units = "units";
		  String stationstr = "station";
		  String values = "values";
		  String coordinates = "coordinates";
		  String heightstr = "height";
		  if (!hasError) {
			  this.createDataStructs(stationData, stationToNum);
	  
		  }
		  
		  /*{
		   * station {
		   *   coordinates   []
		   *   name 
		   * }
		   * data {
		   *   variable_name {
		   *      times []
		   *      units: []
		   *      values: []
		   *    }
		   * }
		   * }
		   */
		  jsonGen.writeStartObject();

		  for(String stationKey : stationData.keySet()){
			  int stationNum = stationToNum.get(stationKey);
			  jsonGen.writeObjectFieldStart(stationstr);
			  jsonGen.writeStringField("name", stationKey);
			  jsonGen.writeArrayFieldStart(coordinates);
			  jsonGen.writeNumber((this.handler.getCDMDataset().getLowerLat(stationNum)));
			  jsonGen.writeNumber((this.handler.getCDMDataset().getLowerLon(stationNum)));
			  jsonGen.writeEndArray();

			  jsonGen.writeObjectFieldStart("data");
			  Map<String, JsonFormatterData> cData = stationData.get(stationKey);
			  for(String var : cData.keySet()){
				  JsonFormatterData data = cData.get(var);
				  jsonGen.writeObjectFieldStart(var);
				  jsonGen.writeArrayFieldStart(times);
				  List<Object> timesL = data.getTimeValues();
				  for(Object t : timesL){
					  jsonGen.writeString((String)t);
				  }
				  jsonGen.writeEndArray();
				  
				  if(data.isProfileData){
					  jsonGen.writeObjectFieldStart(heightstr);
					  jsonGen.writeStringField(units, data.getHeightUnits());
					  List<Double> heightL = data.getHeightValues();
					  jsonGen.writeArrayFieldStart(values);
					  for(Double h: heightL){
						  jsonGen.writeNumber(h);
					  }
					  jsonGen.writeEndArray();
					  jsonGen.writeEndObject();
				  }
				  jsonGen.writeArrayFieldStart(values);
				  List<Object> dAr = data.getDataValues();
				  for(Object dv : dAr){
						  jsonGen.writeNumber(Double.valueOf((String) dv));					  
				  }
				  jsonGen.writeEndArray();
				  jsonGen.writeStringField(units, data.getUnits());
				  jsonGen.writeEndObject();

			  }
			  jsonGen.writeEndObject();
			  jsonGen.writeEndObject();
			  
			  
		  }
		  jsonGen.writeEndObject();
		  jsonGen.flush();

	  }

	 
	@Override
	public String getContentType() {
		return "text/json";
	}
}
