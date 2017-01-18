package com.asascience.ncsos.outputformatter.go;

import java.util.ArrayList;
import java.util.List;

public class JsonFormatterData {
	String dataVariable;
	String units;
	String heightUnits;
	boolean isProfileData;
	List<Object> dataValues;
	List<Double> heightValues;
	List<Object> timeValues;


	public JsonFormatterData(String dataVariable, String units, String heightUnits){
		isProfileData = heightUnits != null;

		if(isProfileData)
			heightValues = new ArrayList<Double>();
		dataValues = new ArrayList<Object>();
	
		timeValues = new ArrayList<Object>();
		this.heightUnits = heightUnits;
		this.dataVariable = dataVariable;
		this.units = units;
		if(this.units == null)
			this.units = "none";
	}
	
	protected String getDataVariable() {
		return dataVariable;
	}
	protected void setDataType(String dataVariable) {
		this.dataVariable = dataVariable;
	}
	protected String getUnits() {
		return units;
	}
	protected void setUnits(String units) {
		this.units = units;
	}
	protected String getHeightUnits() {
		return heightUnits;
	}
	protected void setHeightUnits(String heightUnits) {
		this.heightUnits = heightUnits;
	}
	
	
	protected List<Double> getHeightValues() {
		return heightValues;
	}

	protected void setHeightValues(List<Double> heightValues) {
		this.heightValues = heightValues;
	}

	protected List<Object> getTimeValues() {
		return timeValues;
	}
	protected void setTimeValues(List<Object> timeValues) {
		this.timeValues = timeValues;
	}
	protected boolean isProfileData() {
		return isProfileData;
	}


	protected List<Object> getDataValues() {
		return dataValues;
	}

	protected void setDataValues(List<Object> dataValues) {
		this.dataValues = dataValues;
	}


}
