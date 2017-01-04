package com.asascience.ncsos.outputformatter.go;
import java.io.IOException;
import java.io.Writer;

import com.asascience.ncsos.go.GetObservationRequestHandler;
import com.asascience.ncsos.outputformatter.OutputFormatter;

public class JsonFormatter extends OutputFormatter {

	public JsonFormatter(
			GetObservationRequestHandler getObservationRequestHandler) {
		// TODO Auto-generated constructor stub
	}

	
	  @Override
	    public void writeOutput(Writer writer) throws IOException {
	        if (!hasError) {
	        }
	    }


	@Override
	public String getContentType() {
		return "text/json";
	}
}
