/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncSOS.service;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import thredds.server.sos.getCaps.SOSGetCapabilitiesRequestHandler;
import thredds.server.sos.getObs.SOSGetObservationRequestHandler;
import thredds.server.sos.util.XMLDomUtils;
import ucar.nc2.dataset.NetcdfDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Reads and parses a request coming in from thredds
 * @author scowan
 */
public class SOSParser {
    private ArrayList<String> queryParameters;
    private Logger _log;
    private Map<String, String> coordsHash;
    
    public SOSParser() {
        queryParameters = new ArrayList<String>();
        _log = LoggerFactory.getLogger(SOSParser.class);
        coordsHash = new HashMap<String, String>();
    }
    
    /// enhance - provides direct access to parsing a sos request and 
    ///
    public void enhance(final NetcdfDataset dataset, final Writer writer, final String query, String threddsURI) {
        enhance(dataset, writer, query, threddsURI, null);
    }

    public void enhance(final NetcdfDataset dataset, final Writer writer, final String query, String threddsURI, String savePath) {
    }
    
}
