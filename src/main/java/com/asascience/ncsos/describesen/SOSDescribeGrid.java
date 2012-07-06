/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import java.io.IOException;
import ucar.ma2.Array;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSDescribeGrid extends SOSDescribeStation implements SOSDescribeIF {
    
    String upperLatitude, upperLongitude, lowerLatitude, lowerLongitude;
    
    public SOSDescribeGrid( NetcdfDataset dataset, String procedure ) {
        super(dataset, procedure);
        upperLatitude = upperLongitude = lowerLatitude = lowerLongitude = "";
        // get the bounding box
        try {
            Array coordArray;
            for (Variable var : dataset.getVariables()) {
                String varName = var.getFullName().toLowerCase();
                if (varName.equals("la1")) {
                    coordArray = var.read();
                    lowerLatitude = coordArray.getObject(0).toString();
                }
                else if (varName.equals("la2")) {
                    coordArray = var.read();
                    upperLatitude = coordArray.getObject(0).toString();
                }
                else if (varName.equals("lo1")) {
                    coordArray = var.read();
                    lowerLongitude = coordArray.getObject(0).toString();
                }
                else if (varName.equals("lo2")) {
                    coordArray = var.read();
                    upperLongitude = coordArray.getObject(0).toString();
                }
                else if (varName.contains("name")) {
                    // get our station var -- grid_name?
                    stationVariable = var;
                }
            }
        } catch (IOException ex) {}
    }
    
    /*********************
     * Interface Methods *
     *********************/
    @Override
    public void SetupOutputDocument(DescribeSensorFormatter output) {
        // system node
        output.setSystemId("station-" + stationName);
        // set description
        formatSetDescription(output);
        // identification node
        formatSetIdentification(output);
        // classification node
        formatSetClassification(output);
        // contact node
        formatSetContactNodes(output);
        // history node
        formatSetHistoryNodes(output);
        // location node
        formatSetLocationNode(output);
        // remove unwanted nodes
        RemoveUnusedNodes(output);
    }

    /*****************************
     * Private/Protected Methods *
     *****************************/
    private void RemoveUnusedNodes(DescribeSensorFormatter output) {
        output.deletePosition();
        output.deletePositions();
        output.deleteTimePosition();
    }
    
    @Override
    protected void formatSetLocationNode(DescribeSensorFormatter output) {
        String[] upperCorner = new String[]{ upperLatitude, upperLongitude };
        String[] lowerCorner = new String[]{ lowerLatitude, lowerLongitude };
        
        output.setLocationNodeWithBoundingBox(lowerCorner, upperCorner);
    }
}
