package com.asascience.ncsos.util;

//import org.apache.log4j.Logger;
import thredds.servlet.DatasetHandler;
import thredds.servlet.ServletUtil;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.PointFeatureCollection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
public class DatasetHandlerAdapter {
	  static final Logger _log= LogManager.getLogger(DatasetHandlerAdapter.class.getName());

  //  private static final Logger _log = Logger.getLogger(DatasetHandlerAdapter.class);
    /** 
     * Open a NetcdfDataset based on the incoming url request.
     * 
     * @param req incoming url request
     * @param res outgoing web based response
     * @return dataset a NetcdfDataset as specifing in the request
     */
    public static NetcdfDataset openDataset(final HttpServletRequest req,
            final HttpServletResponse res) throws Exception {

        NetcdfFile netcdfFile = null;
        NetcdfDataset dataset = null;
        String datasetPath = null;
        String servletPath = req.getServletPath();
        if(servletPath !=  null)
            datasetPath = servletPath.substring("/sos".length()  , servletPath.length());
        if (datasetPath == null) { // passing in a dataset URL, presumably
            // opendap
            datasetPath = ServletUtil.getParameterIgnoreCase(req, "dataset");
            _log.debug("opendap datasetPath: " + datasetPath);
            try {
                dataset = NetcdfDataset.openDataset(datasetPath);
            } catch (IOException e) {
                throw new Exception("Failed to open dataset <" + datasetPath + ">: "
                        + e.getMessage());
            }
            
        } else {
            try {
            	
                            netcdfFile = DatasetHandler.getNetcdfFile(req, res, datasetPath);
            //    _log.debug("netcdfFile location: " + netcdfFile.getLocation());
               dataset = new NetcdfDataset(netcdfFile);
            
            } catch (IOException e) {
               throw new Exception("Failed to open dataset <" + datasetPath + ">: "
                        + e.getMessage());
            }
        }
        return dataset;

    }

    /** 
     * Close a NetcdfDataset.
     * 
     * @param dataset the NetcdfDataset to close 
     */
    public static void closeDataset(final NetcdfDataset dataset) {
        if (dataset == null) {
            return;
        }
        try {
            dataset.close();
        } catch (IOException ioe) {
            _log.warn("Failed to properly close the dataset", ioe);
        }
    }
    
    /**
     * Encapsulates calcBounds in a try-catch block. Returns whether or not the attempt was succesful
     * @since authored by Sean Cowan - 10.16.2012
     * @param collection the feature collection to calc bounds on
     * @return T if calc bounds was successful, false otherwise
     */
    public static boolean calcBounds(final PointFeatureCollection collection) {
        try {
            collection.calcBounds();
        } catch (Exception ex) {
            _log.error("Could not calculate the bounds of the PointFeatureCollection " + collection.getName() + "\n" + ex.getLocalizedMessage());
            return false;
        } catch (Error err) {
            _log.error("Could not caluclate the bounds of the PointFeatureCollection " + collection.getName() + "\n" + err.getLocalizedMessage());
            return false;
        }
        
        return true;
    }

    /**
     * 
     * @since authored by Sean Cowan - 10.16.2012
     * @param featureDataset
     * @return 
     */
    public static boolean calcBounds(FeatureDataset featureDataset) {
        try {
            featureDataset.calcBounds();
        } catch (Exception ex) {
            _log.error("Could not calculate the bounds of the FeatureDataset " + featureDataset.getTitle() + "\n" + ex.getLocalizedMessage());
            return false;
        }
        
        return true;
    }
}
