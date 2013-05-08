/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;

/**
 * Provides common methods for Describe Sensor request handling classes.
 * @author SCowan
 * @version 1.0.0
 */
public interface ISOSDescribeSensor {
    /**
     * Tells the output handler to set up the response to a Describe Sensor request
     * so that when writeOutput is called by the controller, all data for the response
     * assembly is available.
     * @param output the formatter to be used for the response
     */
    public void setupOutputDocument(SOSOutputFormatter output);
}
