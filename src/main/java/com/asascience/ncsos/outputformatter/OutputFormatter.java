package com.asascience.ncsos.outputformatter;
import java.io.IOException;
import java.io.Writer;

public abstract class OutputFormatter {
    protected static final String BLOCK_SEPERATOR = "\n";
    protected static final String TOKEN_SEPERATOR = ",";
    protected static final String DECIMAL_SEPERATOR = ".";
    protected Boolean hasError = false;
    /**
     * Writes prepared output to the writer (usually will be a response stream from a http request
     *
     * @param writer the stream where the output will be written to.
     */
    public abstract void writeOutput(Writer writer) throws IOException;
    
    /**
     * The Content-type of this response
     */
    public abstract String getContentType();


}
