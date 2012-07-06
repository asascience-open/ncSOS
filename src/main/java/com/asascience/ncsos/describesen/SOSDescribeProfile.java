/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describesen;

import com.asascience.ncsos.outputformatter.DescribeSensorFormatter;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSDescribeProfile extends SOSDescribeStation implements SOSDescribeIF {
    
    public SOSDescribeProfile( NetcdfDataset dataset, String procedure ) {
        super(dataset,procedure);
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
        // document node
        formatSetDocumentNodes(output);
        // history node
        formatSetHistoryNodes(output);
        // valid time node
        formatSetValidTimeNode(output);
        // positions node
        formatSetPositionsNode(output);
        // remove unwanted nodes
        RemoveUnusedNodes(output);
    }

    /*******************
     * Private Methods *
     *******************/
    
    private void RemoveUnusedNodes(DescribeSensorFormatter output) {
        // delete unwanted nodes
        output.deleteLocationNode();
        output.deletePosition();
        output.deleteTimePosition();
    }

    private void formatSetValidTimeNode(DescribeSensorFormatter output) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void formatSetPositionsNode(DescribeSensorFormatter output) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
